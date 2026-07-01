package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaCompraJPARepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CopaAcessoService {

    public enum ResgateResult { OK, JA_ATIVO, CODIGO_INVALIDO, SEM_PRODUTOS }

    private static final String COPA_PASS = "copa-pass";

    private static final Set<String> COPA_PASS_INCLUI = Set.of(
            "acesso-calendario-comparador",
            "guia-selecao-portugal",
            "guia-selecao-brasil",
            "copa-em-20-factos",
            "historico-confronto"
    );

    private final CopaAcessoJPARepository  repo;
    private final CopaCompraJPARepository  compraRepo;

    public CopaAcessoService(CopaAcessoJPARepository repo, CopaCompraJPARepository compraRepo) {
        this.repo      = repo;
        this.compraRepo = compraRepo;
    }

    public boolean isContaDev() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                           || "ROLE_DEVELOPER".equals(a.getAuthority()));
    }

    public boolean temAcesso(String email, String produtoSlug) {
        if (isContaDev()) return true;
        if (email == null || email.isBlank()) return false;
        String norm = email.toLowerCase();
        return repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, produtoSlug)
                || repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, COPA_PASS);
    }

    @Transactional
    public void concederAcesso(String email, String produtoSlug, String transacao) {
        salvar(email, produtoSlug, transacao);
        if (COPA_PASS.equals(produtoSlug)) {
            COPA_PASS_INCLUI.forEach(slug -> salvar(email, slug, transacao));
        }
    }

    /**
     * Resgata um código de 16 caracteres (transação Hotmart) para o email fornecido.
     * Fluxo:
     *  1. Valida que o código existe em copa_compra
     *  2. Procura acessos já concedidos para essa transação em copa_acesso
     *  3. Se encontrados, copia os mesmos slugs para o novo email
     *  4. Se copa_acesso ainda está vazio (webhook pendente), usa copa_compra.produto_slug
     */
    @Transactional
    public ResgateResult resgatar(String email, String codigo) {
        if (codigo == null || codigo.isBlank()) return ResgateResult.CODIGO_INVALIDO;

        String cod  = codigo.trim().toUpperCase();
        String norm = email.toLowerCase();

        // 1. Valida código
        var compraOpt = compraRepo.findByTransacaoIgnoreCase(cod);
        if (compraOpt.isEmpty()) return ResgateResult.CODIGO_INVALIDO;

        // 2. Procura acessos existentes para esta transação (já gravados pelo webhook)
        List<CopaAcessoEntity> acessos = repo.findByTransacaoIgnoreCase(cod);

        if (!acessos.isEmpty()) {
            // Verifica se o email já tem TODOS esses acessos
            boolean tudoAtivo = acessos.stream()
                    .allMatch(a -> repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, a.getProdutoSlug()));
            if (tudoAtivo) return ResgateResult.JA_ATIVO;

            acessos.forEach(a -> salvar(norm, a.getProdutoSlug(), cod));
            return ResgateResult.OK;
        }

        // 3. Fallback: usa produto_slug da compra (webhook ainda não processou copa_acesso)
        String slug = compraOpt.get().getProdutoSlug();
        if (slug == null || slug.isBlank()) return ResgateResult.SEM_PRODUTOS;

        if (repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, slug)) return ResgateResult.JA_ATIVO;

        salvar(norm, slug, cod);
        if (COPA_PASS.equals(slug)) {
            COPA_PASS_INCLUI.forEach(s -> salvar(norm, s, cod));
        }
        return ResgateResult.OK;
    }

    private void salvar(String email, String slug, String transacao) {
        String norm = email.toLowerCase();
        if (!repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, slug)) {
            repo.save(new CopaAcessoEntity(norm, slug, transacao));
        }
    }
}
