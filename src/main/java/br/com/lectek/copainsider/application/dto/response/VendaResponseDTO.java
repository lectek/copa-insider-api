package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "VendaResponseDTO", description = "Detalhes de uma venda registrada")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID da venda", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.id.notNull}")
    @JsonProperty("idVenda")
    private UUID idVenda;

    @Schema(description = "Data/hora da venda", type = "string", format = "date-time",
            example = "2025-07-04T14:20:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.dataHora.notNull}")
    @PastOrPresent(message = "{venda.dataHora.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataHora")
    private LocalDateTime dataHora;

    @Schema(description = "Token de rastreamento (UUID)")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Informações do cliente", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.cliente.notNull}")
    @Valid
    @JsonProperty("cliente")
    private ClienteResponseDTO cliente;

    @Schema(description = "Itens da venda", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{venda.itens.notEmpty}")
    @Valid
    @JsonProperty("itens")
    @JsonAlias("itensDetalhados")
    private List<@Valid ItemVendaDTO> itens;

    @Schema(description = "Quantidade total de produtos vendidos", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.qtdTotalProdutos.notNull}")
    @Min(value = 1, message = "{venda.qtdTotalProdutos.min}")
    @JsonProperty("qtdTotalProdutos")
    private Integer qtdTotalProdutos;

    @Schema(description = "Valor total bruto da venda", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.total.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{venda.total.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.total.digits}")
    @JsonProperty("total")
    private BigDecimal total;

    @Schema(description = "Desconto aplicado", example = "5.00")
    @PositiveOrZero(message = "{venda.descontoAplicado.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.descontoAplicado.digits}")
    @JsonProperty("descontoAplicado")
    private BigDecimal descontoAplicado;

    @Schema(description = "Valor de frete", example = "10.00")
    @PositiveOrZero(message = "{venda.frete.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.frete.digits}")
    @JsonProperty("frete")
    private BigDecimal frete;

    @Schema(description = "Valor líquido da venda (total - desconto + frete)", example = "105.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.valorLiquido.notNull}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{venda.valorLiquido.min}")
    @Digits(integer = 14, fraction = 2, message = "{venda.valorLiquido.digits}")
    @JsonProperty("valorLiquido")
    private BigDecimal valorLiquido;

    @Schema(description = "Forma de pagamento", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{venda.formaPagamento.notBlank}")
    @Size(max = 50, message = "{venda.formaPagamento.size}")
    @JsonProperty("formaPagamento")
    private String formaPagamento;

    @Schema(description = "QR code para pagamento", example = "https://.../qrcode.png")
    @Size(max = 500, message = "{venda.qrCodePagamento.size}")
    @JsonProperty("qrCodePagamento")
    private String qrCodePagamento;

    @Schema(description = "Status da venda", example = "CONFIRMADA", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"PENDENTE", "CONFIRMADA", "CANCELADA", "ESTORNADA"})
    @NotNull(message = "{venda.status.notNull}")
    @JsonProperty("status")
    private StatusVenda status;

    @Schema(description = "Canal de origem do pedido", example = "APP", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"SITE", "BALCAO", "DELIVERY", "APP"})
    @NotNull(message = "{venda.origemPedido.notNull}")
    @JsonProperty("origemPedido")
    private OrigemPedido origemPedido;

    @Schema(description = "Responsável pelo atendimento", example = "atendente123")
    @Size(max = 100, message = "{venda.responsavelAtendimento.size}")
    @JsonProperty("responsavelAtendimento")
    private String responsavelAtendimento;

    @Schema(description = "Entregador da venda", example = "entregador456")
    @Size(max = 100, message = "{venda.entregador.size}")
    @JsonProperty("entregador")
    private String entregador;

    @Schema(description = "Modo de entrega", example = "ENTREGA", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"RETIRADA", "ENTREGA"})
    @NotNull(message = "{venda.modoEntrega.notNull}")
    @JsonProperty("modoEntrega")
    private ModoEntrega modoEntrega;

    @Schema(description = "Código de rastreamento", example = "TRACK123456")
    @Size(max = 100, message = "{venda.codigoRastreamento.size}")
    @JsonProperty("codigoRastreamento")
    private String codigoRastreamento;

    @Schema(description = "Data/hora estimada de entrega", type = "string", format = "date-time",
            example = "2025-07-05T15:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEntregaEstimada")
    private LocalDateTime dataEntregaEstimada;

    @Schema(description = "Canal de atendimento", example = "APP",
            allowableValues = {"SITE", "BALCAO", "DELIVERY", "APP"})
    @JsonProperty("canalAtendimento")
    private CanalAtendimento canalAtendimento;

    @Schema(description = "Mensagem do cliente", example = "Por favor, deixe na portaria.")
    @Size(max = 500, message = "{venda.mensagemCliente.size}")
    @JsonProperty("mensagemCliente")
    private String mensagemCliente;

    @Schema(description = "Mensagem interna", example = "Verificar cupom fidelidade.")
    @Size(max = 500, message = "{venda.mensagemInterna.size}")
    @JsonProperty("mensagemInterna")
    private String mensagemInterna;

    /* ========================== Enums ========================== */

    public enum StatusVenda {
        PENDENTE, CONFIRMADA, CANCELADA, ESTORNADA
    }

    public enum OrigemPedido {
        SITE, BALCAO, DELIVERY, APP
    }

    public enum ModoEntrega {
        RETIRADA, ENTREGA
    }

    public enum CanalAtendimento {
        SITE, BALCAO, DELIVERY, APP
    }
}

