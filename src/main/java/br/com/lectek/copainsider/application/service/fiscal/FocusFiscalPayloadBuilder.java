package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalTaxRegime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FocusFiscalPayloadBuilder {

    private static final String HOMOLOGATION_ITEM_DESCRIPTION =
            "NOTA FISCAL EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL";

    private final ObjectMapper objectMapper;

    public FocusFiscalPayloadBuilder(final ObjectMapper objectMapperValue) {
        this.objectMapper = objectMapperValue;
    }

    public String build(
            final PedidoFiscalSnapshotEntity snapshot,
            final FiscalEmitterConfigEntity config
    ) {
        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "Snapshot fiscal obrigatorio para montar o payload."
            );
        }
        if (config == null) {
            throw new IllegalArgumentException(
                    "Configuracao do emitente obrigatoria."
            );
        }
        if (snapshot.getSuggestedDocumentModel() == FiscalDocumentModel.NFE_55) {
            throw new IllegalArgumentException(
                    "NF-e automatica ainda depende de destinatario juridico com endereco estruturado."
            );
        }

        final String issuerCnpj = digits(
                firstNonBlank(snapshot.getIssuerCnpj(), config.getCnpj())
        );
        if (issuerCnpj == null || issuerCnpj.length() != 14) {
            throw new IllegalArgumentException(
                    "CNPJ do emitente invalido para emissao fiscal."
            );
        }

        final JsonNode payload = readPayload(snapshot.getPayloadJson());
        final JsonNode itemsNode = payload.path("items");
        if (!itemsNode.isArray() || itemsNode.isEmpty()) {
            throw new IllegalArgumentException(
                    "Snapshot fiscal sem itens para emissao."
            );
        }

        final BigDecimal orderTotal = normalizeMoney(snapshot.getTotalAmount());
        if (orderTotal.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Total do pedido invalido para emissao fiscal."
            );
        }

        final ObjectNode root = objectMapper.createObjectNode();
        root.put("natureza_operacao", "Venda de mercadoria");
        root.put("finalidade_emissao", 1);
        root.put("tipo_documento", 1);
        root.put("local_destino", 1);
        root.put("consumidor_final", 1);
        root.put("presenca_comprador", 1);
        root.put("modalidade_frete", 9);
        root.put("cnpj_emitente", issuerCnpj);
        root.put("valor_produtos", orderTotal);
        root.put("valor_total", orderTotal);

        final BigDecimal shippingAmount = normalizeMoney(snapshot.getShippingAmount());
        if (shippingAmount.signum() > 0) {
            root.put("valor_frete", shippingAmount);
        }

        applyRecipient(root, snapshot);
        root.set(
                "items",
                buildItems(itemsNode, config.getTaxRegime(), config.getEnvironment())
        );
        root.set(
                "formas_pagamento",
                buildPayments(snapshot.getPaymentMethod(), orderTotal)
        );
        return write(root);
    }

    private void applyRecipient(
            final ObjectNode root,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        final String document = digits(snapshot.getRecipientDocument());
        if (document != null && document.length() == 11) {
            root.put("cpf_destinatario", document);
        }
        final String name = blankToNull(snapshot.getRecipientName());
        if (name != null) {
            root.put("nome_destinatario", name);
        }
        final String email = blankToNull(snapshot.getRecipientEmail());
        if (email != null) {
            root.put("email_destinatario", email);
        }
        if (document != null) {
            root.put("indicador_inscricao_estadual_destinatario", 9);
        }
    }

    private ArrayNode buildItems(
            final JsonNode itemsNode,
            final FiscalTaxRegime taxRegime,
            final FiscalEnvironment environment
    ) {
        final ArrayNode items = objectMapper.createArrayNode();
        int index = 1;
        for (JsonNode itemNode : itemsNode) {
            items.add(buildItem(itemNode, index, taxRegime, environment));
            index++;
        }
        return items;
    }

    private ObjectNode buildItem(
            final JsonNode itemNode,
            final int index,
            final FiscalTaxRegime taxRegime,
            final FiscalEnvironment environment
    ) {
        final JsonNode fiscalNode = itemNode.path("fiscal");
        final String code = firstNonBlank(
                blankToNull(itemNode.path("codigoBarras").asText(null)),
                blankToNull(itemNode.path("produtoId").asText(null)),
                "ITEM-" + index
        );
        final String name = blankToNull(itemNode.path("nome").asText(null));
        if (name == null) {
            throw new IllegalArgumentException(
                    "Item do snapshot fiscal sem descricao."
            );
        }

        final BigDecimal quantity = normalizeMoney(
                itemNode.path("quantidade").decimalValue()
        );
        final BigDecimal unitValue = normalizeMoney(
                itemNode.path("valorUnitario").decimalValue()
        );
        final BigDecimal grossValue = normalizeMoney(
                itemNode.path("subtotal").decimalValue()
        );
        if (quantity.signum() <= 0 || unitValue.signum() < 0
                || grossValue.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Item do snapshot fiscal possui valores invalidos."
            );
        }

        final ObjectNode item = objectMapper.createObjectNode();
        item.put("numero_item", index);
        item.put("codigo", code);
        item.put(
                "descricao",
                environment == FiscalEnvironment.HOMOLOGACAO
                        ? HOMOLOGATION_ITEM_DESCRIPTION
                        : name
        );
        item.put("unidade_comercial", resolveUnit(itemNode));
        item.put("unidade_tributavel", resolveUnit(itemNode));
        item.put("quantidade_comercial", quantity);
        item.put("quantidade_tributavel", quantity);
        item.put("valor_unitario_comercial", unitValue);
        item.put("valor_unitario_tributavel", unitValue);
        item.put("valor_bruto", grossValue);
        item.put("cfop", requireDigits(fiscalNode, "cfop", 4));
        item.put("codigo_ncm", requireDigits(fiscalNode, "ncm", 8));
        final String cest = digits(blankToNull(fiscalNode.path("cest").asText(null)));
        if (cest != null) {
            item.put("cest", cest);
        }
        final Integer origin = fiscalNode.path("origem").isMissingNode()
                ? null
                : fiscalNode.path("origem").isNull()
                ? null
                : fiscalNode.path("origem").asInt();
        if (origin == null || origin < 0 || origin > 8) {
            throw new IllegalArgumentException(
                    "Origem fiscal obrigatoria para todos os itens."
            );
        }
        item.put("icms_origem", origin);
        item.put(
                "icms_situacao_tributaria",
                resolveIcmsCode(fiscalNode, taxRegime)
        );
        item.put(
                "pis_situacao_tributaria",
                requireDigits(fiscalNode, "pisCst", 2)
        );
        item.put(
                "cofins_situacao_tributaria",
                requireDigits(fiscalNode, "cofinsCst", 2)
        );
        return item;
    }

    private ArrayNode buildPayments(
            final String paymentMethod,
            final BigDecimal totalAmount
    ) {
        final ArrayNode payments = objectMapper.createArrayNode();
        final ObjectNode payment = objectMapper.createObjectNode();
        payment.put("forma_pagamento", resolvePaymentCode(paymentMethod));
        payment.put("valor_pagamento", normalizeMoney(totalAmount));
        payments.add(payment);
        return payments;
    }

    private String resolveUnit(final JsonNode itemNode) {
        final String unit = blankToNull(itemNode.path("unidade").asText(null));
        return unit == null ? "UN" : unit;
    }

    private String resolveIcmsCode(
            final JsonNode fiscalNode,
            final FiscalTaxRegime taxRegime
    ) {
        if (taxRegime == FiscalTaxRegime.SIMPLES_NACIONAL
                || taxRegime == FiscalTaxRegime.SIMPLES_NACIONAL_EXCESSO_SUBLIMITE) {
            return requireDigits(fiscalNode, "csosn", 3);
        }
        return requireDigits(fiscalNode, "icmsCst", 3);
    }

    private String resolvePaymentCode(final String paymentMethod) {
        final String normalized = paymentMethod == null
                ? ""
                : paymentMethod.trim().toUpperCase();
        if (normalized.contains("PIX")) {
            return "17";
        }
        if (normalized.contains("CARTAO_CREDITO")
                || normalized.contains("CREDITO")) {
            return "03";
        }
        if (normalized.contains("CARTAO_DEBITO")
                || normalized.contains("DEBITO")) {
            return "04";
        }
        if (normalized.contains("BOLETO")) {
            return "15";
        }
        if (normalized.contains("DINHEIRO")) {
            return "01";
        }
        return "99";
    }

    private JsonNode readPayload(final String payloadJson) {
        try {
            if (payloadJson == null || payloadJson.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(payloadJson);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Snapshot fiscal invalido para emissao.",
                    ex
            );
        }
    }

    private String write(final ObjectNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao serializar payload fiscal.",
                    ex
            );
        }
    }

    private String requireDigits(
            final JsonNode fiscalNode,
            final String fieldName,
            final int maxLength
    ) {
        final String value = digits(blankToNull(fiscalNode.path(fieldName).asText(null)));
        if (value == null || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Campo fiscal obrigatorio ausente ou invalido: " + fieldName
            );
        }
        return value;
    }

    private BigDecimal normalizeMoney(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String firstNonBlank(final String... values) {
        for (String value : values) {
            final String normalized = blankToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String digits(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.replaceAll("\\D", "");
        return normalized.isBlank() ? null : normalized;
    }
}
