package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.pdf.IText8EbookBuilder;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EbookPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EbookPedidoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDataAggregator;
import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDataResult;
import br.com.lectek.copainsider.domain.enums.EbookStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EbookGeracaoService {

    private static final Logger log = LoggerFactory.getLogger(EbookGeracaoService.class);

    private final EbookPedidoJPARepository repository;
    private final SelecaoDataAggregator    aggregator;
    private final EbookTemplateEngine      templateEngine;
    private final IText8EbookBuilder       pdfBuilder;
    private final MailService              mailService;
    private final ApplicationContext       ctx;

    @Value("${app.ebook.storage-dir:/tmp/ebooks}")
    private String storageDir;

    @Value("${app.web.base-url:https://allaboutworldcup2026.com}")
    private String baseUrl;

    public EbookGeracaoService(EbookPedidoJPARepository repository,
                                SelecaoDataAggregator aggregator,
                                EbookTemplateEngine templateEngine,
                                IText8EbookBuilder pdfBuilder,
                                MailService mailService,
                                ApplicationContext ctx) {
        this.repository     = repository;
        this.aggregator     = aggregator;
        this.templateEngine = templateEngine;
        this.pdfBuilder     = pdfBuilder;
        this.mailService    = mailService;
        this.ctx            = ctx;
    }

    // ── Registo do pedido ─────────────────────────────────────────────────────

    @Transactional
    public EbookPedidoEntity registrarPedido(String transacao, String email, String nome,
                                              String selecaoCode, String selecaoNome, String idioma) {
        if (repository.existsByTransacao(transacao)) {
            log.info("[ebook] pedido ja registado para transacao {}", transacao);
            return repository.findByTransacao(transacao).orElseThrow();
        }

        EbookStatus statusInicial = (selecaoCode != null && idioma != null)
                ? EbookStatus.PENDENTE
                : EbookStatus.AGUARDANDO_SELECAO;

        EbookPedidoEntity pedido = new EbookPedidoEntity(transacao, email, nome, statusInicial);
        if (selecaoCode != null && idioma != null) {
            pedido.definirSelecao(selecaoCode, selecaoNome, idioma);
        }

        repository.save(pedido);
        log.info("[ebook] pedido registado — transacao={} status={}", transacao, statusInicial);
        return pedido;
    }

    // ── Geração assíncrona ────────────────────────────────────────────────────

    @Async("ebookExecutor")
    public void gerarAsync(Long pedidoId) {
        EbookPedidoEntity pedido = repository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            log.error("[ebook] pedido {} nao encontrado", pedidoId);
            return;
        }
        if (pedido.getSelecaoCode() == null || pedido.getIdioma() == null) {
            log.warn("[ebook] pedido {} sem selecao/idioma — aguardando cliente", pedidoId);
            return;
        }

        // Reutilizar PDF em cache para a mesma (selecao, idioma)
        repository.findFirstBySelecaoCodeAndIdiomaAndStatus(
                pedido.getSelecaoCode(), pedido.getIdioma(), EbookStatus.PRONTO)
            .filter(c -> c.getPdfCaminho() != null && new java.io.File(c.getPdfCaminho()).exists())
            .ifPresent(cached -> {
                String token = UUID.randomUUID().toString().replace("-", "");
                pedido.marcarPronto(cached.getPdfCaminho(), token);
                repository.save(pedido);
                log.info("[ebook] PDF reutilizado do cache — selecao={} idioma={}",
                        pedido.getSelecaoCode(), pedido.getIdioma());
                enviarEmailEntrega(pedido, token);
            });

        if (pedido.getStatus() == EbookStatus.PRONTO) return;

        pedido.marcarGerando();
        repository.save(pedido);
        log.info("[ebook] iniciando geracao — selecao={} idioma={}",
                pedido.getSelecaoCode(), pedido.getIdioma());

        try {
            SelecaoDataResult dados  = aggregator.agregar(pedido.getSelecaoCode(), pedido.getIdioma());
            pedido.atualizarProgresso(50);
            repository.save(pedido);

            EbookConteudo conteudo = templateEngine.gerar(dados, pedido.getIdioma());
            pedido.atualizarProgresso(75);
            repository.save(pedido);

            String token   = UUID.randomUUID().toString().replace("-", "");
            String caminho = pdfBuilder.construir(conteudo, token, storageDir);
            pedido.atualizarProgresso(95);

            pedido.marcarPronto(caminho, token);
            repository.save(pedido);
            log.info("[ebook] gerado com sucesso — token={}", token);
            enviarEmailEntrega(pedido, token);

        } catch (Exception e) {
            log.error("[ebook] erro ao gerar ebook para pedido {}: {}", pedidoId, e.getMessage(), e);
            pedido.marcarErro();
            repository.save(pedido);
        }
    }

    // ── Selecionar e gerar (chamado pelo controller após cliente escolher) ─────

    @Transactional
    public void selecionarEGerar(String transacao, String selecaoCode, String selecaoNome, String idioma) {
        EbookPedidoEntity pedido = repository.findByTransacao(transacao)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + transacao));

        pedido.definirSelecao(selecaoCode, selecaoNome, idioma);
        repository.save(pedido);

        // Invocar via ApplicationContext para respeitar o proxy @Async
        ctx.getBean(EbookGeracaoService.class).gerarAsync(pedido.getId());
    }

    // ── Email de entrega ──────────────────────────────────────────────────────

    private void enviarEmailEntrega(EbookPedidoEntity pedido, String token) {
        try {
            String downloadUrl = baseUrl + "/ebook/download/" + token;
            java.util.Map<String, Object> vars = new java.util.HashMap<>();
            vars.put("nome",             pedido.getCompradorNome());
            vars.put("selecao",          pedido.getSelecaoNome());
            vars.put("idioma",           pedido.getIdioma());
            vars.put("downloadUrl",      downloadUrl);
            vars.put("minhasComprasUrl", baseUrl + "/ebook/meus-ebooks");

            mailService.sendTemplate(
                    pedido.getCompradorEmail(),
                    "O seu Guia da Selecao esta pronto!",
                    "mail/ebook-pronto",
                    vars,
                    null);
        } catch (Exception e) {
            log.error("[ebook] erro ao enviar email para {}: {}", pedido.getCompradorEmail(), e.getMessage());
        }
    }
}
