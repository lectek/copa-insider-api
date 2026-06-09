package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalTaxRegime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FocusFiscalPayloadBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FocusFiscalPayloadBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new FocusFiscalPayloadBuilder(objectMapper);
    }

    @Test
    void buildCreatesNfcePayloadFromSnapshot() throws Exception {
        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setSuggestedDocumentModel(FiscalDocumentModel.NFCE_65);
        snapshot.setIssuerCnpj("12345678000199");
        snapshot.setRecipientDocument("123.456.789-01");
        snapshot.setRecipientName("Joao da Silva");
        snapshot.setRecipientEmail("joao@teste.com");
        snapshot.setPaymentMethod("PIX");
        snapshot.setShippingAmount(BigDecimal.ZERO);
        snapshot.setTotalAmount(new BigDecimal("39.90"));
        snapshot.setPayloadJson("""
                {
                  "items": [
                    {
                      "produtoId": 77,
                      "nome": "Dipirona 500mg",
                      "codigoBarras": "7891234567890",
                      "unidade": "UN",
                      "quantidade": 2,
                      "subtotal": 39.90,
                      "valorUnitario": 19.95,
                      "fiscal": {
                        "ncm": "30049069",
                        "cest": "1300100",
                        "cfop": "5102",
                        "origem": 0,
                        "csosn": "102",
                        "pisCst": "01",
                        "cofinsCst": "01"
                      }
                    }
                  ]
                }
                """);

        final FiscalEmitterConfigEntity config = new FiscalEmitterConfigEntity();
        config.setCnpj("12345678000199");
        config.setEnvironment(FiscalEnvironment.PRODUCAO);
        config.setTaxRegime(FiscalTaxRegime.SIMPLES_NACIONAL);

        final String payload = builder.build(snapshot, config);
        final JsonNode root = objectMapper.readTree(payload);

        Assertions.assertThat(root.path("cnpj_emitente").asText())
                .isEqualTo("12345678000199");
        Assertions.assertThat(root.path("cpf_destinatario").asText())
                .isEqualTo("12345678901");
        Assertions.assertThat(root.path("formas_pagamento"))
                .hasSize(1);
        Assertions.assertThat(root.path("formas_pagamento").get(0)
                .path("forma_pagamento").asText()).isEqualTo("17");
        Assertions.assertThat(root.path("items"))
                .hasSize(1);
        Assertions.assertThat(root.path("items").get(0)
                .path("codigo_ncm").asText()).isEqualTo("30049069");
        Assertions.assertThat(root.path("items").get(0)
                .path("cfop").asText()).isEqualTo("5102");
    }

    @Test
    void buildRejectsAutomaticNfe55() {
        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setSuggestedDocumentModel(FiscalDocumentModel.NFE_55);
        final FiscalEmitterConfigEntity config = new FiscalEmitterConfigEntity();

        Assertions.assertThatThrownBy(() -> builder.build(snapshot, config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NF-e automatica");
    }
}
