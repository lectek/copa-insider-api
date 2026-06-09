/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="ConfirmacaoMedicacaoRequestDTO", description="Dados de confirma\u00e7\u00e3o de medica\u00e7\u00e3o do cliente")
public class ConfirmacaoMedicacaoRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente", example="123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.clienteId.notNull}")
    @Min(value=1L, message="{confirmacaoMedicacao.clienteId.min}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{confirmacaoMedicacao.clienteId.notNull}") @Min(value=1L, message="{confirmacaoMedicacao.clienteId.min}") Long clienteId;
    @Schema(description="ID da confirma\u00e7\u00e3o", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="confirmacaoId", access=JsonProperty.Access.READ_ONLY)
    private UUID confirmacaoId;
    @Schema(description="ID do lembrete associado", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="lembreteId")
    private UUID lembreteId;
    @Schema(description="Nome do medicamento", example="Dipirona 500mg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{confirmacaoMedicacao.nomeMedicamento.notBlank}")
    @Size(max=100, message="{confirmacaoMedicacao.nomeMedicamento.size}")
    @JsonProperty(value="nomeMedicamento")
    private @NotBlank(message="{confirmacaoMedicacao.nomeMedicamento.notBlank}") @Size(max=100, message="{confirmacaoMedicacao.nomeMedicamento.size}") String nomeMedicamento;
    @Schema(description="Hor\u00e1rio agendado para ingest\u00e3o do medicamento", type="string", format="date-time", example="2025-07-04T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.horarioAgendado.notNull}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="horarioAgendado")
    private @NotNull(message="{confirmacaoMedicacao.horarioAgendado.notNull}") LocalDateTime horarioAgendado;
    @Schema(description="Hor\u00e1rio em que a medica\u00e7\u00e3o foi confirmada", type="string", format="date-time", example="2025-07-04T08:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.horarioConfirmado.notNull}")
    @PastOrPresent(message="{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="horarioConfirmado")
    private @NotNull(message="{confirmacaoMedicacao.horarioConfirmado.notNull}") @PastOrPresent(message="{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}") LocalDateTime horarioConfirmado;
    @Schema(description="Indicador se o medicamento foi tomado", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.tomou.notNull}")
    @JsonProperty(value="tomou")
    private @NotNull(message="{confirmacaoMedicacao.tomou.notNull}") Boolean tomou;
    @Schema(description="Indica se a confirma\u00e7\u00e3o foi autom\u00e1tica pelo sistema", example="false")
    @NotNull(message="{confirmacaoMedicacao.automatico.notNull}")
    @JsonProperty(value="automatico")
    private @NotNull(message="{confirmacaoMedicacao.automatico.notNull}") Boolean automatico = Boolean.FALSE;
    @Schema(description="Observa\u00e7\u00f5es adicionais", example="Cliente relatou leve tontura")
    @Size(max=500, message="{confirmacaoMedicacao.observacao.size}")
    @JsonProperty(value="observacao")
    private @Size(max=500, message="{confirmacaoMedicacao.observacao.size}") String observacao;
    @Schema(description="ID do tenant", example="redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{confirmacaoMedicacao.tenantId.notBlank}")
    @Size(max=100, message="{confirmacaoMedicacao.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{confirmacaoMedicacao.tenantId.notBlank}") @Size(max=100, message="{confirmacaoMedicacao.tenantId.size}") String tenantId;
    @Schema(description="Token de rastreamento", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Data e hora de cria\u00e7\u00e3o do registro", type="string", format="date-time", example="2025-07-04T08:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.dataCriacao.notNull}")
    @PastOrPresent(message="{confirmacaoMedicacao.dataCriacao.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataCriacao", access=JsonProperty.Access.READ_ONLY)
    private @NotNull(message="{confirmacaoMedicacao.dataCriacao.notNull}") @PastOrPresent(message="{confirmacaoMedicacao.dataCriacao.pastOrPresent}") LocalDateTime dataCriacao;
    @Schema(description="Data e hora da \u00faltima atualiza\u00e7\u00e3o do registro", type="string", format="date-time", example="2025-07-04T08:10:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataAtualizacao", access=JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataAtualizacao;

    @AssertTrue(message="{confirmacaoMedicacao.horarios.ordemValida}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{confirmacaoMedicacao.horarios.ordemValida}") boolean isOrdemHorariosValida() {
        if (this.horarioAgendado == null || this.horarioConfirmado == null) {
            return false;
        }
        return !this.horarioConfirmado.isBefore(this.horarioAgendado);
    }

    @AssertTrue(message="{confirmacaoMedicacao.automatico.requerLembrete}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{confirmacaoMedicacao.automatico.requerLembrete}") boolean isAutomaticoComLembrete() {
        if (this.automatico == null) {
            return false;
        }
        if (this.automatico.booleanValue()) {
            return this.lembreteId != null;
        }
        return true;
    }

    @AssertTrue(message="{confirmacaoMedicacao.tomou.requerConfirmado}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{confirmacaoMedicacao.tomou.requerConfirmado}") boolean isTomouRegras() {
        if (this.tomou == null) {
            return false;
        }
        if (this.tomou.booleanValue()) {
            return this.horarioConfirmado != null;
        }
        return true;
    }

    public ConfirmacaoMedicacaoRequestDTO() {
    }

    public ConfirmacaoMedicacaoRequestDTO(Long clienteId, UUID confirmacaoId, UUID lembreteId, String nomeMedicamento, LocalDateTime horarioAgendado, LocalDateTime horarioConfirmado, Boolean tomou, Boolean automatico, String observacao, String tenantId, UUID traceId, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        this.clienteId = clienteId;
        this.confirmacaoId = confirmacaoId;
        this.lembreteId = lembreteId;
        this.nomeMedicamento = nomeMedicamento;
        this.horarioAgendado = horarioAgendado;
        this.horarioConfirmado = horarioConfirmado;
        this.tomou = tomou;
        this.automatico = automatico;
        this.observacao = observacao;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getConfirmacaoId() {
        return this.confirmacaoId;
    }

    public void setConfirmacaoId(UUID confirmacaoId) {
        this.confirmacaoId = confirmacaoId;
    }

    public UUID getLembreteId() {
        return this.lembreteId;
    }

    public void setLembreteId(UUID lembreteId) {
        this.lembreteId = lembreteId;
    }

    public String getNomeMedicamento() {
        return this.nomeMedicamento;
    }

    public void setNomeMedicamento(String nomeMedicamento) {
        this.nomeMedicamento = nomeMedicamento;
    }

    public LocalDateTime getHorarioAgendado() {
        return this.horarioAgendado;
    }

    public void setHorarioAgendado(LocalDateTime horarioAgendado) {
        this.horarioAgendado = horarioAgendado;
    }

    public LocalDateTime getHorarioConfirmado() {
        return this.horarioConfirmado;
    }

    public void setHorarioConfirmado(LocalDateTime horarioConfirmado) {
        this.horarioConfirmado = horarioConfirmado;
    }

    public Boolean getTomou() {
        return this.tomou;
    }

    public void setTomou(Boolean tomou) {
        this.tomou = tomou;
    }

    public Boolean getAutomatico() {
        return this.automatico;
    }

    public void setAutomatico(Boolean automatico) {
        this.automatico = automatico;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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

    public LocalDateTime getDataCriacao() {
        return this.dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ConfirmacaoMedicacaoRequestDTO that = (ConfirmacaoMedicacaoRequestDTO)o;
        return Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.confirmacaoId, that.confirmacaoId) && Objects.equals(this.lembreteId, that.lembreteId) && Objects.equals(this.nomeMedicamento, that.nomeMedicamento) && Objects.equals(this.horarioAgendado, that.horarioAgendado) && Objects.equals(this.horarioConfirmado, that.horarioConfirmado) && Objects.equals(this.tomou, that.tomou) && Objects.equals(this.automatico, that.automatico) && Objects.equals(this.observacao, that.observacao) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.dataCriacao, that.dataCriacao) && Objects.equals(this.dataAtualizacao, that.dataAtualizacao);
    }

    public int hashCode() {
        return Objects.hash(this.clienteId, this.confirmacaoId, this.lembreteId, this.nomeMedicamento, this.horarioAgendado, this.horarioConfirmado, this.tomou, this.automatico, this.observacao, this.tenantId, this.traceId, this.dataCriacao, this.dataAtualizacao);
    }

    public String toString() {
        return "ConfirmacaoMedicacaoRequestDTO{clienteId=" + this.clienteId + ", confirmacaoId=" + String.valueOf(this.confirmacaoId) + ", lembreteId=" + String.valueOf(this.lembreteId) + ", nomeMedicamento='" + this.nomeMedicamento + "', horarioAgendado=" + String.valueOf(this.horarioAgendado) + ", horarioConfirmado=" + String.valueOf(this.horarioConfirmado) + ", tomou=" + this.tomou + ", automatico=" + this.automatico + ", observacao='" + this.observacao + "', tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", dataCriacao=" + String.valueOf(this.dataCriacao) + ", dataAtualizacao=" + String.valueOf(this.dataAtualizacao) + "}";
    }
}


