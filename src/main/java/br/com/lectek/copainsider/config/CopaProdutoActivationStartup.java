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

    // slug → [hotmartUrl, precoEur, precoBrl, tipo, ordem, nomePtBr, selecaoCode, idiomaDefault, hotmartProductId]
    private static final Map<String, Object[]> PRODUTOS = new LinkedHashMap<>();

    static {
        // ── Pacotes ───────────────────────────────────────────────────────────
        PRODUTOS.put("copa-pass",
                new Object[]{"https://pay.hotmart.com/G106266908X", new BigDecimal("7.99"),
                        new BigDecimal("49.90"), TipoCopaProduto.COPA_PASS, 0,
                        "Dossier Copa 2026 — Pacote Completo", null, null, null});

        PRODUTOS.put("copa-em-20-factos",
                new Object[]{"https://pay.hotmart.com/T106266827K", new BigDecimal("3.90"),
                        new BigDecimal("9.90"), TipoCopaProduto.ANALISE_PREMIUM, 1,
                        "Copa em 20 Factos", null, null, null});

        PRODUTOS.put("historico-confronto",
                new Object[]{"https://pay.hotmart.com/L106252926D", new BigDecimal("4.90"),
                        new BigDecimal("6.90"), TipoCopaProduto.DUELO_HISTORICO, 2,
                        "Histórico do Confronto", null, null, null});

        // ── Produto único "Guia da Minha Seleção" — ID Hotmart 8028523 ────────
        // Este é o único produto Hotmart para todos os guias de seleção.
        // Todos os cards da loja apontam para este mesmo link de pagamento.
        // Após a compra o cliente escolhe a seleção em "Meus Ebooks".
        PRODUTOS.put("guia-selecao",
                new Object[]{"https://pay.hotmart.com/M106536496C", new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 9,
                        "Guia da Minha Seleção", null, null, 8028523L});

        // ── Cards individuais por seleção — exibição na loja, gerados pela API ─
        // URL é sempre null: o link vai via /ebook/iniciar/{code} no controller.
        // fixarExistente() limpa qualquer URL antiga que possa existir no DB.
        PRODUTOS.put("guia-selecao-brasil",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 10,
                        "🇧🇷 Guia da Seleção do Brasil", "BRA", "pt-BR", null});

        PRODUTOS.put("guia-selecao-portugal",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 11,
                        "🇵🇹 Guia da Seleção de Portugal", "POR", "pt-PT", null});

        PRODUTOS.put("guia-selecao-argentina",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 12,
                        "🇦🇷 Guia da Seleção da Argentina", "ARG", "es", null});

        PRODUTOS.put("guia-selecao-franca",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 13,
                        "🇫🇷 Guia da Seleção da França", "FRA", "fr", null});

        PRODUTOS.put("guia-selecao-espanha",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 14,
                        "🇪🇸 Guia da Seleção da Espanha", "ESP", "es", null});

        PRODUTOS.put("guia-selecao-inglaterra",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 15,
                        "🏴󠁧󠁢󠁥󠁮󠁧󠁿 Guia da Seleção da Inglaterra", "ENG", "en", null});

        PRODUTOS.put("guia-selecao-alemanha",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 16,
                        "🇩🇪 Guia da Seleção da Alemanha", "ALE", "de", null});

        PRODUTOS.put("guia-selecao-italia",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 17,
                        "🇮🇹 Guia da Seleção da Itália", "ITA", "it", null});

        PRODUTOS.put("guia-selecao-uruguai",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 18,
                        "🇺🇾 Guia da Seleção do Uruguai", "URU", "es", null});

        PRODUTOS.put("guia-selecao-mexico",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 19,
                        "🇲🇽 Guia da Seleção do México", "MEX", "es", null});

        PRODUTOS.put("guia-selecao-marrocos",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 20,
                        "🇲🇦 Guia da Seleção de Marrocos", "MAR", "fr", null});

        PRODUTOS.put("guia-selecao-eua",
                new Object[]{null, new BigDecimal("9.99"),
                        new BigDecimal("9.99"), TipoCopaProduto.GUIA_SELECAO, 21,
                        "🇺🇸 Guia da Seleção dos EUA", "USA", "en", null});
    }

    private final CopaProdutoJPARepository repo;

    public CopaProdutoActivationStartup(CopaProdutoJPARepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public void run(ApplicationArguments args) {
        for (var entry : PRODUTOS.entrySet()) {
            String slug  = entry.getKey();
            Object[] cfg = entry.getValue();
            repo.findBySlugIgnoringAtivo(slug)
                    .ifPresentOrElse(
                            p  -> fixarExistente(p, cfg, slug),
                            () -> inserirNovo(slug, cfg));
        }

        var ativos = repo.findByAtivoTrueOrderByOrdemAsc();
        log.info("CopaProdutoStartup: {} produtos ativos", ativos.size());
    }

    private void fixarExistente(CopaProdutoEntity p, Object[] cfg, String slug) {
        String hotmartUrl    = (String)     cfg[0];
        BigDecimal precoEur  = (BigDecimal) cfg[1];
        BigDecimal precoBrl  = (BigDecimal) cfg[2];
        int ordem            = (int)        cfg[4];
        String selecaoCode   = (String)     cfg[6];
        String idiomaDefault = (String)     cfg[7];
        Long hotmartId       = (Long)       cfg[8];
        boolean changed      = false;

        if (!p.isAtivo())                                        { p.setAtivo(true);                  changed = true; }
        if (p.getOrdem() != ordem)                               { p.setOrdem(ordem);                 changed = true; }
        if (selecaoCode != null && p.getSelecaoCode() == null)   { p.setSelecaoCode(selecaoCode);     changed = true; }
        if (idiomaDefault != null && p.getIdiomaDefault() == null) { p.setIdiomaDefault(idiomaDefault); changed = true; }
        if (hotmartId != null && p.getHotmartProductId() == null)  { p.setHotmartProductId(hotmartId);  changed = true; }
        if (actualizarPrecos(p, precoEur, precoBrl))             { changed = true; }
        if (actualizarUrl(p, hotmartUrl))                        { changed = true; }

        if (changed) {
            repo.save(p);
            log.info("CopaProdutoStartup: actualizado slug={}", slug);
        }
    }

    private boolean actualizarPrecos(CopaProdutoEntity p, BigDecimal precoEur, BigDecimal precoBrl) {
        boolean changed = false;
        if (precoEur != null && !precoEur.equals(p.getPrecoEur())) { p.setPrecoEur(precoEur); changed = true; }
        if (precoBrl != null && !precoBrl.equals(p.getPreco()))    { p.setPreco(precoBrl);    changed = true; }
        return changed;
    }

    private boolean actualizarUrl(CopaProdutoEntity p, String hotmartUrl) {
        // Cards de seleção individuais (url=null) não têm URL própria — limpa URLs antigas
        if (hotmartUrl == null && !urlAusente(p.getHotmartUrl())) { p.setHotmartUrl(null); return true; }
        if (hotmartUrl != null && urlAusente(p.getHotmartUrl()))  { p.setHotmartUrl(hotmartUrl); return true; }
        return false;
    }

    private void inserirNovo(String slug, Object[] cfg) {
        String hotmartUrl    = (String)          cfg[0];
        BigDecimal precoEur  = (BigDecimal)      cfg[1];
        BigDecimal precoBrl  = (BigDecimal)      cfg[2];
        TipoCopaProduto tipo = (TipoCopaProduto) cfg[3];
        int ordem            = (int)             cfg[4];
        String nome          = (String)          cfg[5];
        String selecaoCode   = (String)          cfg[6];
        String idiomaDefault = (String)          cfg[7];
        Long hotmartId       = (Long)            cfg[8];

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
        novo.setSelecaoCode(selecaoCode);
        novo.setIdiomaDefault(idiomaDefault);
        novo.setHotmartProductId(hotmartId);
        repo.save(novo);
        log.info("CopaProdutoStartup: inserido slug={}", slug);
    }

    private static boolean urlAusente(String url) {
        return url == null || url.isBlank() || "#".equals(url);
    }
}
