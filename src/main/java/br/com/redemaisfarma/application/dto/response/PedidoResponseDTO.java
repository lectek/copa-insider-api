/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonFormat$Shape
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 */
package br.com.redemaisfarma.application.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="PedidoResponseDTO", description="Dados completos de um pedido realizado")
public class PedidoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do pedido", example="123", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="id")
    private Long id;
    @Schema(description="Data e hora da cria\u00e7\u00e3o do pedido", example="2025-07-05T10:15:30", type="string", format="date-time")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataCriacao")
    private LocalDateTime dataCriacao;
    @Schema(description="Data e hora da entrega (se aplic\u00e1vel)", example="2025-07-06T15:00:00", type="string", format="date-time")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEntrega")
    private LocalDateTime dataEntrega;
    @Schema(description="Status atual do pedido", example="EM_PREPARO")
    @JsonProperty(value="status")
    private String status;
    @Schema(description="Valor total do pedido", example="249.90")
    @JsonProperty(value="total")
    private BigDecimal total;
    @Schema(description="Observa\u00e7\u00f5es adicionais do pedido", example="Cliente pediu sem sacola.")
    @JsonProperty(value="observacao")
    private String observacao;
    @Schema(description="Cliente que realizou o pedido")
    @JsonProperty(value="cliente")
    private ClienteDTO cliente;
    @Schema(description="Atendente respons\u00e1vel pelo pedido")
    @JsonProperty(value="atendente")
    private AtendenteDTO atendente;
    @Schema(description="Lista de itens do pedido")
    @JsonProperty(value="itens")
    private List<ItemPedidoResponseDTO> itens;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataCriacao() {
        return this.dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataEntrega() {
        return this.dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public ClienteDTO getCliente() {
        return this.cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public AtendenteDTO getAtendente() {
        return this.atendente;
    }

    public void setAtendente(AtendenteDTO atendente) {
        this.atendente = atendente;
    }

    public List<ItemPedidoResponseDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PedidoResponseDTO)) {
            return false;
        }
        PedidoResponseDTO that = (PedidoResponseDTO)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.dataCriacao, that.dataCriacao) && Objects.equals(this.dataEntrega, that.dataEntrega) && Objects.equals(this.status, that.status) && Objects.equals(this.total, that.total) && Objects.equals(this.observacao, that.observacao) && Objects.equals(this.cliente, that.cliente) && Objects.equals(this.atendente, that.atendente) && Objects.equals(this.itens, that.itens);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.dataCriacao, this.dataEntrega, this.status, this.total, this.observacao, this.cliente, this.atendente, this.itens);
    }

    public String toString() {
        return "PedidoResponseDTO{id=" + this.id + ", dataCriacao=" + String.valueOf(this.dataCriacao) + ", dataEntrega=" + String.valueOf(this.dataEntrega) + ", status='" + this.status + "', total=" + String.valueOf(this.total) + ", observacao='" + this.observacao + "', cliente=" + String.valueOf(this.cliente) + ", atendente=" + String.valueOf(this.atendente) + ", itens=" + String.valueOf(this.itens) + "}";
    }
}

