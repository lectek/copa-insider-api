/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.PositiveOrZero
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="EmailSuporteResponseDTO", description="Dados retornados ap\u00f3s envio de ticket de suporte")
public class EmailSuporteResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Protocolo do ticket gerado", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.protocolo.notNull}")
    @JsonProperty(value="protocolo")
    private @NotNull(message="{emailSuporteResponse.protocolo.notNull}") UUID protocolo;
    @Schema(description="ID do cliente que abriu o ticket", example="12345", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.clienteId.notNull}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{emailSuporteResponse.clienteId.notNull}") Long clienteId;
    @Schema(description="Status do ticket de suporte", example="ABERTO", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.status.notNull}")
    @JsonProperty(value="status")
    private @NotNull(message="{emailSuporteResponse.status.notNull}") StatusTicket status;
    @Schema(description="Prioridade do ticket", example="HIGH", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.prioridade.notNull}")
    @JsonProperty(value="prioridade")
    private @NotNull(message="{emailSuporteResponse.prioridade.notNull}") PrioridadeTicket prioridade;
    @Schema(description="Canal de origem do ticket", example="EMAIL", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.canalOrigem.notNull}")
    @JsonProperty(value="canalOrigem")
    private @NotNull(message="{emailSuporteResponse.canalOrigem.notNull}") CanalOrigem canalOrigem;
    @Schema(description="Conte\u00fado da mensagem enviada", example="N\u00e3o estou conseguindo finalizar meu pedido.", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{emailSuporteResponse.mensagemRecebida.notBlank}")
    @Size(min=5, max=2000, message="{emailSuporteResponse.mensagemRecebida.size}")
    @JsonProperty(value="mensagemRecebida")
    private @NotBlank(message="{emailSuporteResponse.mensagemRecebida.notBlank}") @Size(min=5, max=2000, message="{emailSuporteResponse.mensagemRecebida.size}") String mensagemRecebida;
    @Schema(description="Tickets relacionados (escalonamento)")
    @JsonProperty(value="ticketsRelacionados")
    private List<@NotNull UUID> ticketsRelacionados;
    @Schema(description="Data/hora de abertura do ticket", type="string", format="date-time", example="2025-07-04T14:30:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.dataEnvio.notNull}")
    @PastOrPresent(message="{emailSuporteResponse.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEnvio")
    private @NotNull(message="{emailSuporteResponse.dataEnvio.notNull}") @PastOrPresent(message="{emailSuporteResponse.dataEnvio.pastOrPresent}") LocalDateTime dataEnvio;
    @Schema(description="Prazo de resposta SLA (em horas)", example="24", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.prazoResposta.notNull}")
    @PositiveOrZero(message="{emailSuporteResponse.prazoResposta.positiveOrZero}")
    @JsonProperty(value="prazoRespostaHoras")
    private @NotNull(message="{emailSuporteResponse.prazoResposta.notNull}") @PositiveOrZero(message="{emailSuporteResponse.prazoResposta.positiveOrZero}") Integer prazoRespostaHoras;
    @Schema(description="Deadline de resposta (data/hora)", type="string", format="date-time", example="2025-07-05T14:30:00", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="{emailSuporteResponse.deadline.notNull}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="deadlineResposta")
    private @NotNull(message="{emailSuporteResponse.deadline.notNull}") LocalDateTime deadlineResposta;
    @Schema(description="Usu\u00e1rios/agentes designados ao ticket")
    @JsonProperty(value="agentesDesignados")
    private List<@NotBlank String> agentesDesignados;
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o do ticket", type="string", format="date-time", example="2025-07-04T15:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="ultimaAtualizacao")
    private LocalDateTime ultimaAtualizacao;
    @Schema(description="Token de correla\u00e7\u00e3o (UUID)", example="5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;

    public EmailSuporteResponseDTO() {
    }

    public EmailSuporteResponseDTO(UUID protocolo, Long clienteId, StatusTicket status, PrioridadeTicket prioridade, CanalOrigem canalOrigem, String mensagemRecebida, List<UUID> ticketsRelacionados, LocalDateTime dataEnvio, Integer prazoRespostaHoras, LocalDateTime deadlineResposta, List<String> agentesDesignados, LocalDateTime ultimaAtualizacao, UUID traceId) {
        this.protocolo = protocolo;
        this.clienteId = clienteId;
        this.status = status;
        this.prioridade = prioridade;
        this.canalOrigem = canalOrigem;
        this.mensagemRecebida = mensagemRecebida;
        this.ticketsRelacionados = ticketsRelacionados;
        this.dataEnvio = dataEnvio;
        this.prazoRespostaHoras = prazoRespostaHoras;
        this.deadlineResposta = deadlineResposta;
        this.agentesDesignados = agentesDesignados;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.traceId = traceId;
    }

    public UUID getProtocolo() {
        return this.protocolo;
    }

    public void setProtocolo(UUID protocolo) {
        this.protocolo = protocolo;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public StatusTicket getStatus() {
        return this.status;
    }

    public void setStatus(StatusTicket status) {
        this.status = status;
    }

    public PrioridadeTicket getPrioridade() {
        return this.prioridade;
    }

    public void setPrioridade(PrioridadeTicket prioridade) {
        this.prioridade = prioridade;
    }

    public CanalOrigem getCanalOrigem() {
        return this.canalOrigem;
    }

    public void setCanalOrigem(CanalOrigem canalOrigem) {
        this.canalOrigem = canalOrigem;
    }

    public String getMensagemRecebida() {
        return this.mensagemRecebida;
    }

    public void setMensagemRecebida(String mensagemRecebida) {
        this.mensagemRecebida = mensagemRecebida;
    }

    public List<UUID> getTicketsRelacionados() {
        return this.ticketsRelacionados;
    }

    public void setTicketsRelacionados(List<UUID> ticketsRelacionados) {
        this.ticketsRelacionados = ticketsRelacionados;
    }

    public LocalDateTime getDataEnvio() {
        return this.dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Integer getPrazoRespostaHoras() {
        return this.prazoRespostaHoras;
    }

    public void setPrazoRespostaHoras(Integer prazoRespostaHoras) {
        this.prazoRespostaHoras = prazoRespostaHoras;
    }

    public LocalDateTime getDeadlineResposta() {
        return this.deadlineResposta;
    }

    public void setDeadlineResposta(LocalDateTime deadlineResposta) {
        this.deadlineResposta = deadlineResposta;
    }

    public List<String> getAgentesDesignados() {
        return this.agentesDesignados;
    }

    public void setAgentesDesignados(List<String> agentesDesignados) {
        this.agentesDesignados = agentesDesignados;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return this.ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailSuporteResponseDTO)) {
            return false;
        }
        EmailSuporteResponseDTO that = (EmailSuporteResponseDTO)o;
        return Objects.equals(this.protocolo, that.protocolo) && Objects.equals(this.clienteId, that.clienteId) && this.status == that.status && this.prioridade == that.prioridade && this.canalOrigem == that.canalOrigem && Objects.equals(this.mensagemRecebida, that.mensagemRecebida) && Objects.equals(this.ticketsRelacionados, that.ticketsRelacionados) && Objects.equals(this.dataEnvio, that.dataEnvio) && Objects.equals(this.prazoRespostaHoras, that.prazoRespostaHoras) && Objects.equals(this.deadlineResposta, that.deadlineResposta) && Objects.equals(this.agentesDesignados, that.agentesDesignados) && Objects.equals(this.ultimaAtualizacao, that.ultimaAtualizacao) && Objects.equals(this.traceId, that.traceId);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.protocolo, this.clienteId, this.status, this.prioridade, this.canalOrigem, this.mensagemRecebida, this.ticketsRelacionados, this.dataEnvio, this.prazoRespostaHoras, this.deadlineResposta, this.agentesDesignados, this.ultimaAtualizacao, this.traceId});
    }

    public String toString() {
        return "EmailSuporteResponseDTO{protocolo=" + String.valueOf(this.protocolo) + ", clienteId=" + this.clienteId + ", status=" + String.valueOf((Object)this.status) + ", prioridade=" + String.valueOf((Object)this.prioridade) + ", canalOrigem=" + String.valueOf((Object)this.canalOrigem) + ", mensagemRecebida='" + this.mensagemRecebida + "', ticketsRelacionados=" + String.valueOf(this.ticketsRelacionados) + ", dataEnvio=" + String.valueOf(this.dataEnvio) + ", prazoRespostaHoras=" + this.prazoRespostaHoras + ", deadlineResposta=" + String.valueOf(this.deadlineResposta) + ", agentesDesignados=" + String.valueOf(this.agentesDesignados) + ", ultimaAtualizacao=" + String.valueOf(this.ultimaAtualizacao) + ", traceId=" + String.valueOf(this.traceId) + "}";
    }

    public static enum StatusTicket {
        ABERTO,
        EM_ANDAMENTO,
        FECHADO;

    }

    public static enum PrioridadeTicket {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL;

    }

    public static enum CanalOrigem {
        EMAIL,
        CHAT,
        TELEFONE,
        APP;

    }
}

