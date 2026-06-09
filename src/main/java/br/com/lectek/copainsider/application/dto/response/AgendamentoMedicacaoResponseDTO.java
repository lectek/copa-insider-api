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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="AgendamentoMedicacaoResponseDTO", description="Dados de agendamento de medica\u00e7\u00e3o do cliente")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class AgendamentoMedicacaoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico do agendamento", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="agendamentoId")
    private UUID agendamentoId;
    @Schema(description="ID do cliente", example="12345")
    @JsonProperty(value="clienteId")
    private Long clienteId;
    @Schema(description="Data de in\u00edcio do agendamento", type="string", format="date", example="2025-07-04")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="dataInicio")
    private LocalDate dataInicio;
    @Schema(description="Data de t\u00e9rmino do agendamento", type="string", format="date", example="2025-07-10")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="dataFim")
    private LocalDate dataFim;
    @Schema(description="Lista de hor\u00e1rios de administra\u00e7\u00e3o no formato HH:mm:ss", example="[\"08:00:00\", \"20:00:00\"]")
    @JsonProperty(value="horarios")
    private List<String> horarios;
    @Schema(description="Dosagem prevista para cada administra\u00e7\u00e3o", example="500mg")
    @JsonProperty(value="dosagem")
    private String dosagem;
    @Schema(description="Frequ\u00eancia em dias entre administra\u00e7\u00f5es", example="1")
    @JsonProperty(value="frequenciaDias")
    private Integer frequenciaDias;
    @Schema(description="Status do agendamento", example="ATIVO")
    @JsonProperty(value="status")
    private StatusAgendamento status;
    @Schema(description="Lista de registros de confirma\u00e7\u00e3o de dose e a\u00e7\u00f5es")
    @JsonProperty(value="historico")
    private List<HistoricoDoseDTO> historico;
    @Schema(description="Data/hora de cria\u00e7\u00e3o do agendamento", type="string", format="date-time", example="2025-07-04T09:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="criadoEm")
    private LocalDateTime criadoEm;
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-07-05T10:15:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="atualizadoEm")
    private LocalDateTime atualizadoEm;

    public AgendamentoMedicacaoResponseDTO() {
    }

    public AgendamentoMedicacaoResponseDTO(UUID agendamentoId, Long clienteId, LocalDate dataInicio, LocalDate dataFim, List<String> horarios, String dosagem, Integer frequenciaDias, StatusAgendamento status, List<HistoricoDoseDTO> historico, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.agendamentoId = agendamentoId;
        this.clienteId = clienteId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horarios = horarios;
        this.dosagem = dosagem;
        this.frequenciaDias = frequenciaDias;
        this.status = status;
        this.historico = historico;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getAgendamentoId() {
        return this.agendamentoId;
    }

    public void setAgendamentoId(UUID agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDate getDataInicio() {
        return this.dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return this.dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public List<String> getHorarios() {
        return this.horarios;
    }

    public void setHorarios(List<String> horarios) {
        this.horarios = horarios;
    }

    public String getDosagem() {
        return this.dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public Integer getFrequenciaDias() {
        return this.frequenciaDias;
    }

    public void setFrequenciaDias(Integer frequenciaDias) {
        this.frequenciaDias = frequenciaDias;
    }

    public StatusAgendamento getStatus() {
        return this.status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public List<HistoricoDoseDTO> getHistorico() {
        return this.historico;
    }

    public void setHistorico(List<HistoricoDoseDTO> historico) {
        this.historico = historico;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgendamentoMedicacaoResponseDTO)) {
            return false;
        }
        AgendamentoMedicacaoResponseDTO that = (AgendamentoMedicacaoResponseDTO)o;
        return Objects.equals(this.agendamentoId, that.agendamentoId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.dataInicio, that.dataInicio) && Objects.equals(this.dataFim, that.dataFim) && Objects.equals(this.horarios, that.horarios) && Objects.equals(this.dosagem, that.dosagem) && Objects.equals(this.frequenciaDias, that.frequenciaDias) && this.status == that.status && Objects.equals(this.historico, that.historico) && Objects.equals(this.criadoEm, that.criadoEm) && Objects.equals(this.atualizadoEm, that.atualizadoEm);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.agendamentoId, this.clienteId, this.dataInicio, this.dataFim, this.horarios, this.dosagem, this.frequenciaDias, this.status, this.historico, this.criadoEm, this.atualizadoEm});
    }

    public String toString() {
        return "AgendamentoMedicacaoResponseDTO{agendamentoId=" + String.valueOf(this.agendamentoId) + ", clienteId=" + this.clienteId + ", dataInicio=" + String.valueOf(this.dataInicio) + ", dataFim=" + String.valueOf(this.dataFim) + ", horarios=" + String.valueOf(this.horarios) + ", dosagem='" + this.dosagem + "', frequenciaDias=" + this.frequenciaDias + ", status=" + String.valueOf((Object)this.status) + ", historico=" + String.valueOf(this.historico) + ", criadoEm=" + String.valueOf(this.criadoEm) + ", atualizadoEm=" + String.valueOf(this.atualizadoEm) + "}";
    }

    public static enum StatusAgendamento {
        ATIVO,
        PAUSADO,
        CONCLUIDO,
        CANCELADO;

    }

    @Schema(name="HistoricoDoseDTO", description="Registro de confirma\u00e7\u00e3o de dose administrada")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class HistoricoDoseDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID \u00fanico do registro de dose", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty(value="registroId")
        private UUID registroId;
        @Schema(description="Data e hora da administra\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T08:00:00")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty(value="registroEm")
        private LocalDateTime registroEm;
        @Schema(description="Indicador se a dose foi confirmada pelo usu\u00e1rio", example="true")
        @JsonProperty(value="confirmado")
        private Boolean confirmado;
        @Schema(description="Observa\u00e7\u00f5es do registro", example="Paciente relatou leve tontura ap\u00f3s dose")
        @JsonProperty(value="observacoes")
        private String observacoes;

        public HistoricoDoseDTO() {
        }

        public HistoricoDoseDTO(UUID registroId, LocalDateTime registroEm, Boolean confirmado, String observacoes) {
            this.registroId = registroId;
            this.registroEm = registroEm;
            this.confirmado = confirmado;
            this.observacoes = observacoes;
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

        public Boolean getConfirmado() {
            return this.confirmado;
        }

        public void setConfirmado(Boolean confirmado) {
            this.confirmado = confirmado;
        }

        public String getObservacoes() {
            return this.observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HistoricoDoseDTO)) {
                return false;
            }
            HistoricoDoseDTO that = (HistoricoDoseDTO)o;
            return Objects.equals(this.registroId, that.registroId) && Objects.equals(this.registroEm, that.registroEm) && Objects.equals(this.confirmado, that.confirmado) && Objects.equals(this.observacoes, that.observacoes);
        }

        public int hashCode() {
            return Objects.hash(this.registroId, this.registroEm, this.confirmado, this.observacoes);
        }

        public String toString() {
            return "HistoricoDoseDTO{registroId=" + String.valueOf(this.registroId) + ", registroEm=" + String.valueOf(this.registroEm) + ", confirmado=" + this.confirmado + ", observacoes='" + this.observacoes + "'}";
        }
    }
}

