/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.lectek.copainsider.application.dto.response;

import br.com.lectek.copainsider.domain.enums.StatusPedido;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="AtualizarPedidoResponseDTO", description="Dados de resposta ap\u00f3s atualiza\u00e7\u00e3o de pedido")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class AtualizarPedidoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico do pedido", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="pedidoId")
    private UUID pedidoId;
    @Schema(description="Status do pedido", example="CONFIRMADO")
    @JsonProperty(value="status")
    private StatusPedido status;
    @Schema(description="Motivo da atualiza\u00e7\u00e3o de status", example="Pagamento confirmado pelo gateway")
    @JsonProperty(value="motivo")
    private String motivo;
    @Schema(description="Timestamp da atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T12:34:56")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataAtualizacao")
    private LocalDateTime dataAtualizacao;
    @Schema(description="Valor total atualizado do pedido", example="250.75")
    @JsonProperty(value="valorTotal")
    private BigDecimal valorTotal;
    @Schema(description="Lista de itens do pedido atualizado")
    @JsonProperty(value="itens")
    private List<ItemAtualizadoDTO> itens;
    @Schema(description="ID do cliente", example="123")
    @JsonProperty(value="clienteId")
    private Long clienteId;
    @Schema(description="ID do tenant (multi-inquilino)", example="copainsider-001")
    @JsonProperty(value="tenantId")
    private String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Detalhes de entrega atualizados")
    @JsonProperty(value="entrega")
    private InfoEntregaDTO entrega;

    public AtualizarPedidoResponseDTO() {
    }

    public AtualizarPedidoResponseDTO(UUID pedidoId, StatusPedido status, String motivo, LocalDateTime dataAtualizacao, BigDecimal valorTotal, List<ItemAtualizadoDTO> itens, Long clienteId, String tenantId, UUID traceId, InfoEntregaDTO entrega) {
        this.pedidoId = pedidoId;
        this.status = status;
        this.motivo = motivo;
        this.dataAtualizacao = dataAtualizacao;
        this.valorTotal = valorTotal;
        this.itens = itens;
        this.clienteId = clienteId;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.entrega = entrega;
    }

    public UUID getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public StatusPedido getStatus() {
        return this.status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getMotivo() {
        return this.motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemAtualizadoDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemAtualizadoDTO> itens) {
        this.itens = itens;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public InfoEntregaDTO getEntrega() {
        return this.entrega;
    }

    public void setEntrega(InfoEntregaDTO entrega) {
        this.entrega = entrega;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AtualizarPedidoResponseDTO)) {
            return false;
        }
        AtualizarPedidoResponseDTO that = (AtualizarPedidoResponseDTO)o;
        return Objects.equals(this.pedidoId, that.pedidoId) && this.status == that.status && Objects.equals(this.motivo, that.motivo) && Objects.equals(this.dataAtualizacao, that.dataAtualizacao) && Objects.equals(this.valorTotal, that.valorTotal) && Objects.equals(this.itens, that.itens) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.entrega, that.entrega);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.pedidoId, this.status, this.motivo, this.dataAtualizacao, this.valorTotal, this.itens, this.clienteId, this.tenantId, this.traceId, this.entrega});
    }

    public String toString() {
        return "AtualizarPedidoResponseDTO{pedidoId=" + String.valueOf(this.pedidoId) + ", status=" + String.valueOf((Object)this.status) + ", motivo='" + this.motivo + "', dataAtualizacao=" + String.valueOf(this.dataAtualizacao) + ", valorTotal=" + String.valueOf(this.valorTotal) + ", itens=" + String.valueOf(this.itens) + ", clienteId=" + this.clienteId + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", entrega=" + String.valueOf(this.entrega) + "}";
    }

    @Schema(name="InfoEntregaDTO", description="Detalhes da entrega atualizada")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class InfoEntregaDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="Endere\u00e7o de entrega completo", example="Rua das Flores, 123, Ap 45")
        @JsonProperty(value="endereco")
        private String endereco;
        @Schema(description="Data prevista de entrega", type="string", format="date-time", example="2025-07-06T14:00:00")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty(value="dataPrevista")
        private LocalDateTime dataPrevista;

        public InfoEntregaDTO() {
        }

        public InfoEntregaDTO(String endereco, LocalDateTime dataPrevista) {
            this.endereco = endereco;
            this.dataPrevista = dataPrevista;
        }

        public String getEndereco() {
            return this.endereco;
        }

        public void setEndereco(String endereco) {
            this.endereco = endereco;
        }

        public LocalDateTime getDataPrevista() {
            return this.dataPrevista;
        }

        public void setDataPrevista(LocalDateTime dataPrevista) {
            this.dataPrevista = dataPrevista;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof InfoEntregaDTO)) {
                return false;
            }
            InfoEntregaDTO that = (InfoEntregaDTO)o;
            return Objects.equals(this.endereco, that.endereco) && Objects.equals(this.dataPrevista, that.dataPrevista);
        }

        public int hashCode() {
            return Objects.hash(this.endereco, this.dataPrevista);
        }

        public String toString() {
            return "InfoEntregaDTO{endereco='" + this.endereco + "', dataPrevista=" + String.valueOf(this.dataPrevista) + "}";
        }
    }

    @Schema(name="ItemAtualizadoDTO", description="Detalhes do item ap\u00f3s atualiza\u00e7\u00e3o")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class ItemAtualizadoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID do produto no item", example="789")
        @JsonProperty(value="produtoId")
        private Long produtoId;
        @Schema(description="Quantidade atualizada do item", example="3")
        @JsonProperty(value="quantidade")
        private Integer quantidade;
        @Schema(description="Pre\u00e7o unit\u00e1rio atualizado", example="50.25")
        @JsonProperty(value="precoUnitario")
        private BigDecimal precoUnitario;
        @Schema(description="Total do item (quantidade * preco)", example="150.75")
        @JsonProperty(value="total")
        private BigDecimal total;

        public ItemAtualizadoDTO() {
        }

        public ItemAtualizadoDTO(Long produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal total) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
            this.total = total;
        }

        public Long getProdutoId() {
            return this.produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public Integer getQuantidade() {
            return this.quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }

        public BigDecimal getPrecoUnitario() {
            return this.precoUnitario;
        }

        public void setPrecoUnitario(BigDecimal precoUnitario) {
            this.precoUnitario = precoUnitario;
        }

        public BigDecimal getTotal() {
            return this.total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ItemAtualizadoDTO)) {
                return false;
            }
            ItemAtualizadoDTO that = (ItemAtualizadoDTO)o;
            return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.quantidade, that.quantidade) && Objects.equals(this.precoUnitario, that.precoUnitario) && Objects.equals(this.total, that.total);
        }

        public int hashCode() {
            return Objects.hash(this.produtoId, this.quantidade, this.precoUnitario, this.total);
        }

        public String toString() {
            return "ItemAtualizadoDTO{produtoId=" + this.produtoId + ", quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + ", total=" + String.valueOf(this.total) + "}";
        }
    }
}

