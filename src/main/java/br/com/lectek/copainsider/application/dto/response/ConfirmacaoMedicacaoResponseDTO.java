/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.DecimalMax
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="ConfirmacaoMedicacaoResponseDTO", description="Detalhes da confirma\u00e7\u00e3o de medica\u00e7\u00e3o")
public class ConfirmacaoMedicacaoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico da confirma\u00e7\u00e3o", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="confirmacaoId")
    private UUID confirmacaoId;
    @Schema(description="ID do cliente", example="12345", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.clienteId.notNull}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{confirmacaoMedicacao.clienteId.notNull}") Long clienteId;
    @Schema(description="Nome do medicamento", example="Dipirona 500mg", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{confirmacaoMedicacao.medicamento.notBlank}")
    @Size(max=100, message="{confirmacaoMedicacao.medicamento.size}")
    @JsonProperty(value="medicamento")
    private @NotBlank(message="{confirmacaoMedicacao.medicamento.notBlank}") @Size(max=100, message="{confirmacaoMedicacao.medicamento.size}") String medicamento;
    @Schema(description="Data de in\u00edcio do tratamento", type="string", format="date", example="2025-07-01")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="dataInicioTratamento")
    private LocalDate dataInicioTratamento;
    @Schema(description="Hor\u00e1rio agendado", type="string", format="date-time", example="2025-07-04T08:00:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.horarioAgendado.notNull}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="horarioAgendado")
    private @NotNull(message="{confirmacaoMedicacao.horarioAgendado.notNull}") LocalDateTime horarioAgendado;
    @Schema(description="Hor\u00e1rio de confirma\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T08:05:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.horarioConfirmado.notNull}")
    @PastOrPresent(message="{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="horarioConfirmado")
    private @NotNull(message="{confirmacaoMedicacao.horarioConfirmado.notNull}") @PastOrPresent(message="{confirmacaoMedicacao.horarioConfirmado.pastOrPresent}") LocalDateTime horarioConfirmado;
    @Schema(description="Indicador de tomada do medicamento", example="true", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.confirmado.notNull}")
    @JsonProperty(value="confirmado")
    private @NotNull(message="{confirmacaoMedicacao.confirmado.notNull}") Boolean confirmado;
    @Schema(description="M\u00e9todo de confirma\u00e7\u00e3o", example="MANUAL", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.metodoConfirmacao.notNull}")
    @JsonProperty(value="metodoConfirmacao")
    private @NotNull(message="{confirmacaoMedicacao.metodoConfirmacao.notNull}") MetodoConfirmacao metodoConfirmacao;
    @Schema(description="ID do lembrete associado (UUID)", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="lembreteId")
    private UUID lembreteId;
    @Schema(description="Desconto autom\u00e1tico aplicado (%)", example="5.0")
    @DecimalMin(value="0.0", inclusive=true, message="{confirmacaoMedicacao.descontoAplicado.min}")
    @DecimalMax(value="100.0", inclusive=true, message="{confirmacaoMedicacao.descontoAplicado.max}")
    @JsonProperty(value="descontoAplicado")
    private @DecimalMin(value="0.0", inclusive=true, message="{confirmacaoMedicacao.descontoAplicado.min}") @DecimalMax(value="100.0", inclusive=true, message="{confirmacaoMedicacao.descontoAplicado.max}") BigDecimal descontoAplicado = BigDecimal.ZERO;
    @Schema(description="Observa\u00e7\u00f5es adicionais", example="Paciente relatou leve tontura.")
    @Size(max=500, message="{confirmacaoMedicacao.observacoes.size}")
    @JsonProperty(value="observacoes")
    private @Size(max=500, message="{confirmacaoMedicacao.observacoes.size}") String observacoes;
    @Schema(description="Status de ades\u00e3o ao tratamento", example="EM_ANDAMENTO", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.statusAdesao.notNull}")
    @JsonProperty(value="statusAdesao")
    private @NotNull(message="{confirmacaoMedicacao.statusAdesao.notNull}") StatusAdesao statusAdesao;
    @Schema(description="Timestamp de cria\u00e7\u00e3o do registro", type="string", format="date-time", example="2025-07-04T08:05:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{confirmacaoMedicacao.criadoEm.notNull}")
    @PastOrPresent(message="{confirmacaoMedicacao.criadoEm.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="criadoEm")
    private @NotNull(message="{confirmacaoMedicacao.criadoEm.notNull}") @PastOrPresent(message="{confirmacaoMedicacao.criadoEm.pastOrPresent}") LocalDateTime criadoEm;
    @Schema(description="Timestamp da \u00faltima atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T08:10:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="atualizadoEm")
    private LocalDateTime atualizadoEm;
    @Schema(description="ID do tenant (multi-inquilino)", example="copainsider-001", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{confirmacaoMedicacao.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{confirmacaoMedicacao.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Hist\u00f3rico de confirma\u00e7\u00f5es")
    @JsonProperty(value="historico")
    private List<@Valid HistoricoConfirmacaoDTO> historico;

    public ConfirmacaoMedicacaoResponseDTO() {
    }

    public ConfirmacaoMedicacaoResponseDTO(UUID confirmacaoId, Long clienteId, String medicamento, LocalDate dataInicioTratamento, LocalDateTime horarioAgendado, LocalDateTime horarioConfirmado, Boolean confirmado, MetodoConfirmacao metodoConfirmacao, UUID lembreteId, BigDecimal descontoAplicado, String observacoes, StatusAdesao statusAdesao, LocalDateTime criadoEm, LocalDateTime atualizadoEm, String tenantId, UUID traceId, List<HistoricoConfirmacaoDTO> historico) {
        this.confirmacaoId = confirmacaoId;
        this.clienteId = clienteId;
        this.medicamento = medicamento;
        this.dataInicioTratamento = dataInicioTratamento;
        this.horarioAgendado = horarioAgendado;
        this.horarioConfirmado = horarioConfirmado;
        this.confirmado = confirmado;
        this.metodoConfirmacao = metodoConfirmacao;
        this.lembreteId = lembreteId;
        this.descontoAplicado = descontoAplicado;
        this.observacoes = observacoes;
        this.statusAdesao = statusAdesao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.historico = historico;
    }

    public UUID getConfirmacaoId() {
        return this.confirmacaoId;
    }

    public void setConfirmacaoId(UUID confirmacaoId) {
        this.confirmacaoId = confirmacaoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getMedicamento() {
        return this.medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public LocalDate getDataInicioTratamento() {
        return this.dataInicioTratamento;
    }

    public void setDataInicioTratamento(LocalDate dataInicioTratamento) {
        this.dataInicioTratamento = dataInicioTratamento;
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

    public Boolean getConfirmado() {
        return this.confirmado;
    }

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }

    public MetodoConfirmacao getMetodoConfirmacao() {
        return this.metodoConfirmacao;
    }

    public void setMetodoConfirmacao(MetodoConfirmacao metodoConfirmacao) {
        this.metodoConfirmacao = metodoConfirmacao;
    }

    public UUID getLembreteId() {
        return this.lembreteId;
    }

    public void setLembreteId(UUID lembreteId) {
        this.lembreteId = lembreteId;
    }

    public BigDecimal getDescontoAplicado() {
        return this.descontoAplicado;
    }

    public void setDescontoAplicado(BigDecimal descontoAplicado) {
        this.descontoAplicado = descontoAplicado;
    }

    public String getObservacoes() {
        return this.observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusAdesao getStatusAdesao() {
        return this.statusAdesao;
    }

    public void setStatusAdesao(StatusAdesao statusAdesao) {
        this.statusAdesao = statusAdesao;
    }

    public LocalDateTime getCriadoEm() {
        return this.criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
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

    public List<HistoricoConfirmacaoDTO> getHistorico() {
        return this.historico;
    }

    public void setHistorico(List<HistoricoConfirmacaoDTO> historico) {
        this.historico = historico;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfirmacaoMedicacaoResponseDTO)) {
            return false;
        }
        ConfirmacaoMedicacaoResponseDTO that = (ConfirmacaoMedicacaoResponseDTO)o;
        return Objects.equals(this.confirmacaoId, that.confirmacaoId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.medicamento, that.medicamento) && Objects.equals(this.dataInicioTratamento, that.dataInicioTratamento) && Objects.equals(this.horarioAgendado, that.horarioAgendado) && Objects.equals(this.horarioConfirmado, that.horarioConfirmado) && Objects.equals(this.confirmado, that.confirmado) && this.metodoConfirmacao == that.metodoConfirmacao && Objects.equals(this.lembreteId, that.lembreteId) && Objects.equals(this.descontoAplicado, that.descontoAplicado) && Objects.equals(this.observacoes, that.observacoes) && this.statusAdesao == that.statusAdesao && Objects.equals(this.criadoEm, that.criadoEm) && Objects.equals(this.atualizadoEm, that.atualizadoEm) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.historico, that.historico);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.confirmacaoId, this.clienteId, this.medicamento, this.dataInicioTratamento, this.horarioAgendado, this.horarioConfirmado, this.confirmado, this.metodoConfirmacao, this.lembreteId, this.descontoAplicado, this.observacoes, this.statusAdesao, this.criadoEm, this.atualizadoEm, this.tenantId, this.traceId, this.historico});
    }

    public String toString() {
        return "ConfirmacaoMedicacaoResponseDTO{confirmacaoId=" + String.valueOf(this.confirmacaoId) + ", clienteId=" + this.clienteId + ", medicamento='" + this.medicamento + "', dataInicioTratamento=" + String.valueOf(this.dataInicioTratamento) + ", horarioAgendado=" + String.valueOf(this.horarioAgendado) + ", horarioConfirmado=" + String.valueOf(this.horarioConfirmado) + ", confirmado=" + this.confirmado + ", metodoConfirmacao=" + String.valueOf((Object)this.metodoConfirmacao) + ", lembreteId=" + String.valueOf(this.lembreteId) + ", descontoAplicado=" + String.valueOf(this.descontoAplicado) + ", observacoes='" + this.observacoes + "', statusAdesao=" + String.valueOf((Object)this.statusAdesao) + ", criadoEm=" + String.valueOf(this.criadoEm) + ", atualizadoEm=" + String.valueOf(this.atualizadoEm) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", historico=" + String.valueOf(this.historico) + "}";
    }

    public static enum MetodoConfirmacao {
        MANUAL,
        AUTOMATICO;

    }

    public static enum StatusAdesao {
        EM_ANDAMENTO,
        CONCLUIDA,
        ATRASADA,
        CANCELADA;

    }

    @Schema(name="HistoricoConfirmacaoDTO", description="Registro de cada confirma\u00e7\u00e3o de dose")
    public static class HistoricoConfirmacaoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID \u00fanico do registro", example="6fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty(value="registroId")
        private UUID registroId;
        @Schema(description="Hor\u00e1rio do registro", type="string", format="date-time", example="2025-07-04T08:05:00", requiredMode=Schema.RequiredMode.REQUIRED)
        @NotNull(message="{historicoConfirmacao.registroEm.notNull}")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty(value="registroEm")
        private @NotNull(message="{historicoConfirmacao.registroEm.notNull}") LocalDateTime registroEm;
        @Schema(description="Detalhes adicionais do registro", example="Confirma\u00e7\u00e3o via app m\u00f3vel")
        @Size(max=200, message="{historicoConfirmacao.detalhes.size}")
        @JsonProperty(value="detalhes")
        private @Size(max=200, message="{historicoConfirmacao.detalhes.size}") String detalhes;

        public HistoricoConfirmacaoDTO() {
        }

        public HistoricoConfirmacaoDTO(UUID registroId, LocalDateTime registroEm, String detalhes) {
            this.registroId = registroId;
            this.registroEm = registroEm;
            this.detalhes = detalhes;
        }

        public UUID getRegistroId() {
            return this.registroId;
        }

        public void setRegistroId(UUID registroId) {
            this.registroId = registroId;
        }

        public LocalDateTime getRegistroEm() {
            return this.registroEm;
        }

        public void setRegistroEm(LocalDateTime registroEm) {
            this.registroEm = registroEm;
        }

        public String getDetalhes() {
            return this.detalhes;
        }

        public void setDetalhes(String detalhes) {
            this.detalhes = detalhes;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HistoricoConfirmacaoDTO)) {
                return false;
            }
            HistoricoConfirmacaoDTO that = (HistoricoConfirmacaoDTO)o;
            return Objects.equals(this.registroId, that.registroId) && Objects.equals(this.registroEm, that.registroEm) && Objects.equals(this.detalhes, that.detalhes);
        }

        public int hashCode() {
            return Objects.hash(this.registroId, this.registroEm, this.detalhes);
        }

        public String toString() {
            return "HistoricoConfirmacaoDTO{registroId=" + String.valueOf(this.registroId) + ", registroEm=" + String.valueOf(this.registroEm) + ", detalhes='" + this.detalhes + "'}";
        }
    }
}

