/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Positive
 *  jakarta.validation.constraints.PositiveOrZero
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="MensagemIAResponseDTO", description="Dados retornados de intera\u00e7\u00e3o com IA RedeMaisFarma")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class MensagemIAResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID da intera\u00e7\u00e3o com IA", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{mensagemIAResponse.interacaoId.notNull}")
    @JsonProperty(value="interacaoId")
    private @NotNull(message="{mensagemIAResponse.interacaoId.notNull}") UUID interacaoId;
    @Schema(description="ID do cliente", example="12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{mensagemIAResponse.clienteId.notNull}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{mensagemIAResponse.clienteId.notNull}") Long clienteId;
    @Schema(description="Token de correla\u00e7\u00e3o (UUID)", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{mensagemIAResponse.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{mensagemIAResponse.tenantId.notBlank}") String tenantId;
    @Schema(description="Data/hora da intera\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T15:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{mensagemIAResponse.dataHoraInteracao.notNull}")
    @PastOrPresent(message="{mensagemIAResponse.dataHoraInteracao.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataHoraInteracao")
    private @NotNull(message="{mensagemIAResponse.dataHoraInteracao.notNull}") @PastOrPresent(message="{mensagemIAResponse.dataHoraInteracao.pastOrPresent}") LocalDateTime dataHoraInteracao;
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T15:05:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="atualizadoEm")
    private LocalDateTime atualizadoEm;
    @Schema(description="Pergunta enviada pelo usu\u00e1rio", example="Qual a dosagem recomendada para dor de cabe\u00e7a?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{mensagemIAResponse.perguntaUsuario.notBlank}")
    @Size(max=2000, message="{mensagemIAResponse.perguntaUsuario.size}")
    @JsonProperty(value="perguntaUsuario")
    private @NotBlank(message="{mensagemIAResponse.perguntaUsuario.notBlank}") @Size(max=2000, message="{mensagemIAResponse.perguntaUsuario.size}") String perguntaUsuario;
    @Schema(description="Resposta fornecida pela IA", example="Recomenda-se 500mg a cada 6 horas.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{mensagemIAResponse.respostaIA.notBlank}")
    @Size(max=4000, message="{mensagemIAResponse.respostaIA.size}")
    @JsonProperty(value="respostaIA")
    private @NotBlank(message="{mensagemIAResponse.respostaIA.notBlank}") @Size(max=4000, message="{mensagemIAResponse.respostaIA.size}") String respostaIA;
    @Schema(description="Tipo de resposta da IA", example="EXPLICATIVA", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"EXPLICATIVA", "CONCISA", "PASSO_A_PASSO"})
    @NotNull(message="{mensagemIAResponse.tipoResposta.notNull}")
    @JsonProperty(value="tipoResposta")
    private @NotNull(message="{mensagemIAResponse.tipoResposta.notNull}") TipoResposta tipoResposta;
    @Schema(description="Explica\u00e7\u00e3o ou esclarecimento adicional", example="Evite uso excessivo...")
    @Size(max=2000, message="{mensagemIAResponse.explicacaoSugerida.size}")
    @JsonProperty(value="explicacaoSugerida")
    private @Size(max=2000, message="{mensagemIAResponse.explicacaoSugerida.size}") String explicacaoSugerida;
    @Schema(description="Plano/modelo de IA usado", example="GPT-4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{mensagemIAResponse.planoIA.notBlank}")
    @Size(max=50, message="{mensagemIAResponse.planoIA.size}")
    @JsonProperty(value="planoIA")
    private @NotBlank(message="{mensagemIAResponse.planoIA.notBlank}") @Size(max=50, message="{mensagemIAResponse.planoIA.size}") String planoIA;
    @Schema(description="Quantidade de tokens consumidos na resposta IA", example="150")
    @PositiveOrZero(message="{mensagemIAResponse.consumoAtual.positiveOrZero}")
    @JsonProperty(value="consumoAtual")
    private @PositiveOrZero(message="{mensagemIAResponse.consumoAtual.positiveOrZero}") Integer consumoAtual;
    @Schema(description="Limite mensal de tokens do plano IA", example="1000000")
    @Positive(message="{mensagemIAResponse.limiteMensal.positive}")
    @JsonProperty(value="limiteMensal")
    private @Positive(message="{mensagemIAResponse.limiteMensal.positive}") Integer limiteMensal;
    @Schema(description="Canal de origem da intera\u00e7\u00e3o IA", example="APP", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"WEB", "APP", "ADMIN"})
    @NotNull(message="{mensagemIAResponse.canalOrigem.notNull}")
    @JsonProperty(value="canalOrigem")
    private @NotNull(message="{mensagemIAResponse.canalOrigem.notNull}") CanalOrigem canalOrigem;
    @Schema(description="Contexto de intera\u00e7\u00f5es anteriores")
    @Valid
    @JsonProperty(value="historicoInteracoes")
    private List<HistoricoInteracaoDTO> historicoInteracoes;

    public MensagemIAResponseDTO() {
    }

    public MensagemIAResponseDTO(UUID interacaoId, Long clienteId, UUID traceId, String tenantId, LocalDateTime dataHoraInteracao, LocalDateTime atualizadoEm, String perguntaUsuario, String respostaIA, TipoResposta tipoResposta, String explicacaoSugerida, String planoIA, Integer consumoAtual, Integer limiteMensal, CanalOrigem canalOrigem, List<HistoricoInteracaoDTO> historicoInteracoes) {
        this.interacaoId = interacaoId;
        this.clienteId = clienteId;
        this.traceId = traceId;
        this.tenantId = tenantId;
        this.dataHoraInteracao = dataHoraInteracao;
        this.atualizadoEm = atualizadoEm;
        this.perguntaUsuario = perguntaUsuario;
        this.respostaIA = respostaIA;
        this.tipoResposta = tipoResposta;
        this.explicacaoSugerida = explicacaoSugerida;
        this.planoIA = planoIA;
        this.consumoAtual = consumoAtual;
        this.limiteMensal = limiteMensal;
        this.canalOrigem = canalOrigem;
        this.historicoInteracoes = historicoInteracoes;
    }

    public UUID getInteracaoId() {
        return this.interacaoId;
    }

    public void setInteracaoId(UUID interacaoId) {
        this.interacaoId = interacaoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getDataHoraInteracao() {
        return this.dataHoraInteracao;
    }

    public void setDataHoraInteracao(LocalDateTime dataHoraInteracao) {
        this.dataHoraInteracao = dataHoraInteracao;
    }

    public LocalDateTime getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getPerguntaUsuario() {
        return this.perguntaUsuario;
    }

    public void setPerguntaUsuario(String perguntaUsuario) {
        this.perguntaUsuario = perguntaUsuario;
    }

    public String getRespostasIA() {
        return this.respostaIA;
    }

    public String getRespostaIA() {
        return this.respostaIA;
    }

    public void setRespostaIA(String respostaIA) {
        this.respostaIA = respostaIA;
    }

    public TipoResposta getTipoResposta() {
        return this.tipoResposta;
    }

    public void setTipoResposta(TipoResposta tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public String getExplicacaoSugerida() {
        return this.explicacaoSugerida;
    }

    public void setExplicacaoSugerida(String explicacaoSugerida) {
        this.explicacaoSugerida = explicacaoSugerida;
    }

    public String getPlanoIA() {
        return this.planoIA;
    }

    public void setPlanoIA(String planoIA) {
        this.planoIA = planoIA;
    }

    public Integer getConsumoAtual() {
        return this.consumoAtual;
    }

    public void setConsumoAtual(Integer consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    public Integer getLimiteMensal() {
        return this.limiteMensal;
    }

    public void setLimiteMensal(Integer limiteMensal) {
        this.limiteMensal = limiteMensal;
    }

    public CanalOrigem getCanalOrigem() {
        return this.canalOrigem;
    }

    public void setCanalOrigem(CanalOrigem canalOrigem) {
        this.canalOrigem = canalOrigem;
    }

    public List<HistoricoInteracaoDTO> getHistoricoInteracoes() {
        return this.historicoInteracoes;
    }

    public void setHistoricoInteracoes(List<HistoricoInteracaoDTO> historicoInteracoes) {
        this.historicoInteracoes = historicoInteracoes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MensagemIAResponseDTO)) {
            return false;
        }
        MensagemIAResponseDTO that = (MensagemIAResponseDTO)o;
        return Objects.equals(this.interacaoId, that.interacaoId) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.dataHoraInteracao, that.dataHoraInteracao) && Objects.equals(this.atualizadoEm, that.atualizadoEm) && Objects.equals(this.perguntaUsuario, that.perguntaUsuario) && Objects.equals(this.respostaIA, that.respostaIA) && this.tipoResposta == that.tipoResposta && Objects.equals(this.explicacaoSugerida, that.explicacaoSugerida) && Objects.equals(this.planoIA, that.planoIA) && Objects.equals(this.consumoAtual, that.consumoAtual) && Objects.equals(this.limiteMensal, that.limiteMensal) && this.canalOrigem == that.canalOrigem && Objects.equals(this.historicoInteracoes, that.historicoInteracoes);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.interacaoId, this.clienteId, this.traceId, this.tenantId, this.dataHoraInteracao, this.atualizadoEm, this.perguntaUsuario, this.respostaIA, this.tipoResposta, this.explicacaoSugerida, this.planoIA, this.consumoAtual, this.limiteMensal, this.canalOrigem, this.historicoInteracoes});
    }

    public String toString() {
        return "MensagemIAResponseDTO{interacaoId=" + String.valueOf(this.interacaoId) + ", clienteId=" + this.clienteId + ", traceId=" + String.valueOf(this.traceId) + ", tenantId='" + this.tenantId + "', dataHoraInteracao=" + String.valueOf(this.dataHoraInteracao) + ", atualizadoEm=" + String.valueOf(this.atualizadoEm) + ", perguntaUsuario='" + this.perguntaUsuario + "', respostaIA='" + this.respostaIA + "', tipoResposta=" + String.valueOf((Object)this.tipoResposta) + ", explicacaoSugerida='" + this.explicacaoSugerida + "', planoIA='" + this.planoIA + "', consumoAtual=" + this.consumoAtual + ", limiteMensal=" + this.limiteMensal + ", canalOrigem=" + String.valueOf((Object)this.canalOrigem) + ", historicoInteracoes=" + String.valueOf(this.historicoInteracoes) + "}";
    }

    @Schema(enumAsRef=true, description="Tipo de resposta gerada pela IA")
    public static enum TipoResposta {
        EXPLICATIVA,
        CONCISA,
        PASSO_A_PASSO;

    }

    @Schema(enumAsRef=true, description="Canal de origem da intera\u00e7\u00e3o")
    public static enum CanalOrigem {
        WEB,
        APP,
        ADMIN;

    }

    @Schema(name="HistoricoInteracaoDTO", description="Registro de intera\u00e7\u00f5es anteriores")
    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    public static class HistoricoInteracaoDTO
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID \u00fanico do registro", example="7fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message="{historicoInteracao.registroId.notNull}")
        @JsonProperty(value="registroId")
        private @NotNull(message="{historicoInteracao.registroId.notNull}") UUID registroId;
        @Schema(description="Pergunta do usu\u00e1rio", example="Qual hor\u00e1rio de funcionamento?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message="{historicoInteracao.pergunta.notBlank}")
        @Size(max=2000, message="{historicoInteracao.pergunta.size}")
        @JsonProperty(value="pergunta")
        private @NotBlank(message="{historicoInteracao.pergunta.notBlank}") @Size(max=2000, message="{historicoInteracao.pergunta.size}") String pergunta;
        @Schema(description="Resposta da IA", example="Funcionamos 24h.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message="{historicoInteracao.resposta.notBlank}")
        @Size(max=4000, message="{historicoInteracao.resposta.size}")
        @JsonProperty(value="resposta")
        private @NotBlank(message="{historicoInteracao.resposta.notBlank}") @Size(max=4000, message="{historicoInteracao.resposta.size}") String resposta;
        @Schema(description="Timestamp do registro", type="string", format="date-time", example="2025-07-04T14:59:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message="{historicoInteracao.dataHora.notNull}")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty(value="dataHora")
        private @NotNull(message="{historicoInteracao.dataHora.notNull}") LocalDateTime dataHora;

        public HistoricoInteracaoDTO() {
        }

        public HistoricoInteracaoDTO(UUID registroId, String pergunta, String resposta, LocalDateTime dataHora) {
            this.registroId = registroId;
            this.pergunta = pergunta;
            this.resposta = resposta;
            this.dataHora = dataHora;
        }

        public UUID getRegistroId() {
            return this.registroId;
        }

        public void setRegistroId(UUID registroId) {
            this.registroId = registroId;
        }

        public String getPergunta() {
            return this.pergunta;
        }

        public void setPergunta(String pergunta) {
            this.pergunta = pergunta;
        }

        public String getResposta() {
            return this.resposta;
        }

        public void setResposta(String resposta) {
            this.resposta = resposta;
        }

        public LocalDateTime getDataHora() {
            return this.dataHora;
        }

        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HistoricoInteracaoDTO)) {
                return false;
            }
            HistoricoInteracaoDTO that = (HistoricoInteracaoDTO)o;
            return Objects.equals(this.registroId, that.registroId) && Objects.equals(this.pergunta, that.pergunta) && Objects.equals(this.resposta, that.resposta) && Objects.equals(this.dataHora, that.dataHora);
        }

        public int hashCode() {
            return Objects.hash(this.registroId, this.pergunta, this.resposta, this.dataHora);
        }

        public String toString() {
            return "HistoricoInteracaoDTO{registroId=" + String.valueOf(this.registroId) + ", pergunta='" + this.pergunta + "', resposta='" + this.resposta + "', dataHora=" + String.valueOf(this.dataHora) + "}";
        }
    }
}


