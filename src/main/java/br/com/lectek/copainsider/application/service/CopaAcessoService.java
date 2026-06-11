package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CopaAcessoService {

    // Copa Pass concede acesso a todas estas ferramentas automaticamente
    private static final Set<String> COPA_PASS_INCLUI = Set.of(
            "acesso-calendario-comparador",
            "guia-selecao-portugal",
            "guia-selecao-brasil",
            "copa-em-20-factos",
            "historico-confronto"
    );

    private final CopaAcessoJPARepository repo;

    public CopaAcessoService(CopaAcessoJPARepository repo) {
        this.repo = repo;
    }

    public boolean temAcesso(String email, String produtoSlug) {
        if (email == null || email.isBlank()) return false;
        String norm = email.toLowerCase();
        return repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, produtoSlug)
                || repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, "copa-pass");
    }

    @Transactional
    public void concederAcesso(String email, String produtoSlug, String transacao) {
        salvar(email, produtoSlug, transacao);
        if ("copa-pass".equals(produtoSlug)) {
            COPA_PASS_INCLUI.forEach(slug -> salvar(email, slug, transacao));
        }
    }

    private void salvar(String email, String slug, String transacao) {
        String norm = email.toLowerCase();
        if (!repo.existsByEmailIgnoreCaseAndProdutoSlug(norm, slug)) {
            repo.save(new CopaAcessoEntity(norm, slug, transacao));
        }
    }
}
