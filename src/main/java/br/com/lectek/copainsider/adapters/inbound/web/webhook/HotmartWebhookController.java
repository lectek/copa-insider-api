package br.com.lectek.copainsider.adapters.inbound.web.webhook;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaCompraEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaCompraJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.account.UserAccountService;
import br.com.lectek.copainsider.application.service.CopaAcessoService;
import br.com.lectek.copainsider.application.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
public class HotmartWebhookController {

    private static final Logger log = LoggerFactory.getLogger(HotmartWebhookController.class);

    private final CopaCompraJPARepository  compraRepo;
    private final CopaProdutoJPARepository produtoRepo;
    private final UsuarioRepository        usuarioRepo;
    private final UserAccountService       userAccountService;
    private final CopaAcessoService        acessoService;
    private final MailService              mailService;

    @Value("${hotmart.hottok:}")
    private String hottok;

    @Value("${app.web.base-url:https://allaboutworldcup2026.com}")
    private String baseUrl;

    public HotmartWebhookController(
            CopaCompraJPARepository compraRepo,
            CopaProdutoJPARepository produtoRepo,
            UsuarioRepository usuarioRepo,
            UserAccountService userAccountService,
            CopaAcessoService acessoService,
            MailService mailService) {
        this.compraRepo         = compraRepo;
        this.produtoRepo        = produtoRepo;
        this.usuarioRepo        = usuarioRepo;
        this.userAccountService = userAccountService;
        this.acessoService      = acessoService;
        this.mailService        = mailService;
    }

    @PostMapping("/hotmart")
    public ResponseEntity<Void> handle(
            @RequestParam(value = "hottok", required = false) String hottokParam,
            @RequestBody HotmartWebhookPayload payload) {

        if (!isHottokValid(hottokParam, payload.hottok())) {
            log.warn("[hotmart] hottok inválido");
            return ResponseEntity.status(401).build();
        }
        if (payload.data() == null || payload.data().purchase() == null) {
            return ResponseEntity.ok().build();
        }

        String event = payload.data().event();
        HotmartWebhookPayload.Purchase purchase = payload.data().purchase();
        log.info("[hotmart] evento={} transacao={}", event, purchase.transaction());

        if (!"PURCHASE_APPROVED".equals(event) && !"PURCHASE_COMPLETE".equals(event)) {
            return ResponseEntity.ok().build();
        }
        return processarCompra(purchase);
    }

    private boolean isHottokValid(String fromParam, String fromBody) {
        if (hottok.isBlank()) return true;
        String received = (fromParam != null && !fromParam.isBlank()) ? fromParam : fromBody;
        return hottok.equals(received);
    }

    private ResponseEntity<Void> processarCompra(HotmartWebhookPayload.Purchase purchase) {
        String transacao = purchase.transaction();
        if (transacao == null || compraRepo.existsByTransacao(transacao)) {
            log.info("[hotmart] transação duplicada ou nula: {}", transacao);
            return ResponseEntity.ok().build();
        }

        // ── Dados do comprador ─────────────────────────────────────────────
        HotmartWebhookPayload.Buyer buyer = purchase.buyer();
        String email = buyer != null ? buyer.email()                          : null;
        String nome  = buyer != null && buyer.name() != null ? buyer.name()   : "Cliente";
        String cpf   = normalizarCpf(buyer != null ? buyer.document() : null);
        String fone  = extrairTelefone(buyer);

        BigDecimal valor = purchase.price() != null && purchase.price().value() != null
                ? BigDecimal.valueOf(purchase.price().value()) : BigDecimal.ZERO;
        String moeda = purchase.price() != null ? purchase.price().currencyValue() : "BRL";

        String produtoNome = purchase.product() != null ? purchase.product().name() : "Produto Copa Insider";
        String produtoSlug = resolverSlug(purchase.product());

        // ── Guardar compra ─────────────────────────────────────────────────
        CopaCompraEntity compra = new CopaCompraEntity(
                transacao, email, nome, produtoNome, valor, moeda, "APPROVED");
        compraRepo.save(compra);

        if (email == null || email.isBlank()) {
            return ResponseEntity.ok().build();
        }

        ContaCriada conta = criarContaSeNecessario(email, nome, cpf, fone);

        if (produtoSlug != null) {
            acessoService.concederAcesso(email, produtoSlug, transacao);
            log.info("[hotmart] acesso '{}' concedido a {}", produtoSlug, email);
        }

        enviarEmail(compra, conta, valor, moeda, produtoSlug);
        return ResponseEntity.ok().build();
    }

    private record ContaCriada(String email, String nome, String produto,
                                String transacao, boolean nova, String senha) {}

    private ContaCriada criarContaSeNecessario(String email, String nome, String cpf, String fone) {
        if (usuarioRepo.existsByEmailIgnoreCase(email)) {
            return new ContaCriada(email, nome, "", "", false, null);
        }
        String senhaTemp = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        try {
            var u = userAccountService.register(nome, email, cpf, senhaTemp);
            if (fone != null) u.setTelefone(fone);
            log.info("[hotmart] conta criada para {}", email);
            return new ContaCriada(email, nome, "", "", true, senhaTemp);
        } catch (Exception ex) {
            log.warn("[hotmart] erro ao criar conta para {}: {}", email, ex.getMessage());
            return new ContaCriada(email, nome, "", "", false, null);
        }
    }

    private void enviarEmail(CopaCompraEntity compra, ContaCriada conta,
                              BigDecimal valor, String moeda, String produtoSlug) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("nome",            compra.getCompradorNome());
            vars.put("email",           compra.getCompradorEmail());
            vars.put("produto",         compra.getProdutoNome());
            vars.put("transacao",       compra.getTransacao());
            vars.put("valor",           valor);
            vars.put("moeda",           moeda);
            vars.put("novaContaCriada", conta.nova());
            vars.put("senhaTemp",       conta.senha() != null ? conta.senha() : "");
            vars.put("loginUrl",        baseUrl + "/auth/login");
            vars.put("perfilUrl",       baseUrl + "/cliente/dados");
            vars.put("plataformaUrl",   baseUrl);
            vars.put("produtoSlug",     produtoSlug != null ? produtoSlug : "");

            String assunto = conta.nova()
                    ? "Bem-vindo ao Copa Insider — as suas credenciais"
                    : "Compra confirmada — Copa Insider";

            mailService.sendTemplate(
                    compra.getCompradorEmail(), assunto, "mail/compra-confirmada", vars, null);
            compra.marcarEmailEnviado();
            compraRepo.save(compra);
            log.info("[hotmart] email enviado para {}", compra.getCompradorEmail());
        } catch (Exception e) {
            log.error("[hotmart] erro ao enviar email para {}: {}", compra.getCompradorEmail(), e.getMessage());
        }
    }

    private String resolverSlug(HotmartWebhookPayload.Product product) {
        if (product == null || product.id() == null) return null;
        return produtoRepo.findByHotmartProductId(product.id())
                .map(p -> p.getSlug())
                .orElse(null);
    }

    private static String normalizarCpf(String doc) {
        if (doc == null) return null;
        String digits = doc.replaceAll("\\D", "");
        return digits.length() == 11 ? digits : null;
    }

    private static String extrairTelefone(HotmartWebhookPayload.Buyer buyer) {
        if (buyer == null || buyer.phone() == null) return null;
        String num = buyer.phone().number();
        String cc  = buyer.phone().countryCode();
        if (num == null || num.isBlank()) return null;
        return (cc != null && !cc.isBlank()) ? "+" + cc + num : num;
    }
}
