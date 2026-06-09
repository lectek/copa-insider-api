package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/mercadopago")
public class MercadoPagoWebhookController {

    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    public MercadoPagoWebhookController(
            final MercadoPagoCheckoutService mercadoPagoCheckoutServiceValue
    ) {
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutServiceValue;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> receber(
            @RequestParam final Map<String, String> queryParams,
            @RequestBody(required = false) final Map<String, Object> body
    ) {
        final String type = firstNonBlank(
                queryParams.get("type"),
                text(body == null ? null : body.get("type"))
        );
        final String topic = firstNonBlank(
                queryParams.get("topic"),
                text(body == null ? null : body.get("action"))
        );
        final String sellerUserId = firstNonBlank(
                queryParams.get("user_id"),
                text(body == null ? null : body.get("user_id"))
        );
        final String paymentId = resolvePaymentId(queryParams, body);

        final MercadoPagoCheckoutService.PaymentSyncResult result =
                mercadoPagoCheckoutService.handleWebhookNotification(
                        type,
                        topic,
                        sellerUserId,
                        paymentId
                );

        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", true);
        response.put("updated", result.updated());
        if (result.pedidoId() != null) {
            response.put("pedidoId", result.pedidoId());
        }
        if (!text(result.paymentStatus()).isBlank()) {
            response.put("paymentStatus", result.paymentStatus());
        }
        return ResponseEntity.ok(response);
    }

    private String resolvePaymentId(
            final Map<String, String> queryParams,
            final Map<String, Object> body
    ) {
        final String fromQuery = firstNonBlank(
                queryParams.get("data.id"),
                queryParams.get("id")
        );
        if (!fromQuery.isBlank()) {
            return fromQuery;
        }
        if (body == null) {
            return "";
        }
        final Object data = body.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            return text(dataMap.get("id"));
        }
        return text(body.get("id"));
    }

    private String firstNonBlank(final String first, final String second) {
        return text(first).isBlank() ? text(second) : first.trim();
    }

    private String text(final Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
