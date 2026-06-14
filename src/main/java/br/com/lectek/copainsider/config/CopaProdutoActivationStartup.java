package br.com.lectek.copainsider.config;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CopaProdutoActivationStartup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CopaProdutoActivationStartup.class);

    // slug → [hotmartUrl, precoEur, precoBrl, tipo, ordem, nomePtBr]
    private static final Map<String, Object[]> PRODUTOS_ESSENCIAIS = new LinkedHashMap<>();

    static {
        PRODUTOS_ESSENCIAIS.put("copa-pass",
                new Object[]{"https://pay.hotmart.com/G106266908X", new BigDecimal("7.99"),
                        new BigDecimal("49.90"), TipoCopaProduto.COPA_PASS, 0,
                        "Dossier Copa 2026 — Pacote Completo"});
        PRODUTOS_ESSENCIAIS.put("copa-em-20-factos",
                new Object[]{"https://pay.hotmart.com/T106266827K", new BigDecimal("3.90"),
                        new BigDecimal("9.90"), TipoCopaProduto.ANALISE_PREMIUM, 1,
                        "Copa em 20 Factos"});
        PRODUTOS_ESSENCIAIS.put("guia-selecao-portugal",
                new Object[]{"https://pay.hotmart.com/J106242736P", new BigDecimal("3.99"),
                        new BigDecimal("9.90"), TipoCopaProduto.GUIA_SELECAO, 2,
                        "Guia da Selecção de Portugal"});
        PRODUTOS_ESSENCIAIS.put("guia-selecao-brasil",
                new Object[]{"https://pay.hotmart.com/M106250934Y", new BigDecimal("3.99"),
                        new BigDecimal("9.90"), TipoCopaProduto.GUIA_SELECAO, 3,
                        "Guia da Selecção do Brasil"});
        PRODUTOS_ESSENCIAIS.put("historico-confronto",
                new Object[]{"https://pay.hotmart.com/L106252926D", new BigDecimal("4.90"),
                        new BigDecimal("6.90"), TipoCopaProduto.DUELO_HISTORICO, 4,
                        "Histórico do Confronto"});
    }

    private final CopaProdutoJPARepository repo;

    public CopaProdutoActivationStartup(CopaProdutoJPARepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public void run(ApplicationArguments args) {
        for (var entry : PRODUTOS_ESSENCIAIS.entrySet()) {
            String slug         = entry.getKey();
            Object[] cfg        = entry.getValue();
            repo.findBySlugIgnoringAtivo(slug)
                    .ifPresentOrElse(
                            p  -> fixarExistente(p, cfg, slug),
                            () -> inserirNovo(slug, cfg));
        }

        var ativos = repo.findByAtivoTrueOrderByOrdemAsc();
        log.warn("CopaProdutoStartup: {} produtos ativos: {}",
                ativos.size(),
                ativos.stream().map(p -> p.getSlug() + "(ordem=" + p.getOrdem() + ")").toList());
    }

    private void fixarExistente(CopaProdutoEntity p, Object[] cfg, String slug) {
        String hotmartUrl   = (String)     cfg[0];
        BigDecimal precoEur = (BigDecimal) cfg[1];
        int ordem           = (int)        cfg[4];
        boolean changed     = false;

        if (!p.isAtivo()) { p.setAtivo(true); changed = true; }
        if (urlAusente(p.getHotmartUrl())) { p.setHotmartUrl(hotmartUrl); changed = true; }
        if (p.getPrecoEur() == null)        { p.setPrecoEur(precoEur);    changed = true; }
        if (p.getOrdem() != ordem)          { p.setOrdem(ordem);           changed = true; }

        if (changed) {
            repo.save(p);
            log.warn("CopaProdutoStartup: CORRIGIU slug={}", slug);
        }
    }

    private void inserirNovo(String slug, Object[] cfg) {
        String hotmartUrl       = (String)          cfg[0];
        BigDecimal precoEur     = (BigDecimal)      cfg[1];
        BigDecimal precoBrl     = (BigDecimal)      cfg[2];
        TipoCopaProduto tipo    = (TipoCopaProduto) cfg[3];
        int ordem               = (int)             cfg[4];
        String nome             = (String)          cfg[5];

        CopaProdutoEntity novo = new CopaProdutoEntity();
        novo.setSlug(slug);
        novo.setTipo(tipo);
        novo.setPreco(precoBrl);
        novo.setPrecoEur(precoEur);
        novo.setNomePtBr(nome);
        novo.setNomePtPt(nome);
        novo.setNomeEn(nome);
        novo.setHotmartUrl(hotmartUrl);
        novo.setAtivo(true);
        novo.setOrdem(ordem);
        repo.save(novo);
        log.warn("CopaProdutoStartup: INSERIU slug={}", slug);
    }

    private static boolean urlAusente(String url) {
        return url == null || url.isBlank() || "#".equals(url);
    }
}
