package br.com.lectek.copainsider.adapters.inbound.web.webhook;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaCompraEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaCompraJPARepository;
import br.com.lectek.copainsider.application.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class HotmartWebhookController {

    private static final Logger log = LoggerFactory.getLogger(HotmartWebhookController.class);

    private final CopaCompraJPARepository compraRepo;
    private final MailService mailService;

    @Value("${hotmart.hottok:}")
    private String hottok;

    public HotmartWebhookController(CopaCompraJPARepository compraRepo, MailService mailService) {
        this.compraRepo = compraRepo;
        this.mailService = mailService;
    }

    @PostMapping("/hotmart")
    public ResponseEntity<Void> handle(
            @RequestParam(value = "hottok", required = false) String hottokParam,
            @RequestBody HotmartWebhookPayload payload) {

        if (!isHottokValid(hottokParam, payload.hottok())) {
            log.warn("[hotmart] hottok inválido recebido");
            return ResponseEntity.status(401).build();
        }

        if (payload.data() == null || payload.data().purchase() == null) {
            log.debug("[hotmart] evento sem purchase — ignorado");
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

        String email   = purchase.buyer()   != null ? purchase.buyer().email()   : null;
        String nome    = purchase.buyer()   != null ? purchase.buyer().name()    : "Comprador";
        String produto = purchase.product() != null ? purchase.product().name()  : "Produto Copa Insider";
        BigDecimal valor = purchase.price() != null && purchase.price().value() != null
                ? BigDecimal.valueOf(purchase.price().value()) : BigDecimal.ZERO;
        String moeda = purchase.price() != null ? purchase.price().currencyValue() : "BRL";

        CopaCompraEntity compra = new CopaCompraEntity(transacao, email, nome, produto, valor, moeda, "APPROVED");
        compraRepo.save(compra);

        enviarEmailConfirmacao(compra, email, nome, produto, transacao, valor, moeda);
        return ResponseEntity.ok().build();
    }

    private void enviarEmailConfirmacao(CopaCompraEntity compra, String email,
                                        String nome, String produto,
                                        String transacao, BigDecimal valor, String moeda) {
        if (email == null || email.isBlank()) return;
        try {
            mailService.sendTemplate(
                    email,
                    "Sua compra foi confirmada — Copa Insider",
                    "mail/compra-confirmada",
                    Map.of("nome", nome, "produto", produto,
                           "transacao", transacao, "valor", valor, "moeda", moeda),
                    null
            );
            compra.marcarEmailEnviado();
            compraRepo.save(compra);
            log.info("[hotmart] email enviado para {}", email);
        } catch (Exception e) {
            log.error("[hotmart] erro ao enviar email para {}: {}", email, e.getMessage());
        }
    }
}
