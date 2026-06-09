package br.com.lectek.copainsider.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "MensagemIARequestDTO",
        description = "Dados enviados à IA CopaInsider para geração de resposta inteligente"
)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString(exclude = {"mensagem", "contexto", "tenantId"})
public class MensagemIARequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // --- Identificação e rastreamento ---

    @Schema(description = "ID único da requisição (gerado pelo servidor)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(value = "requestId", access = JsonProperty.Access.READ_ONLY)
    private UUID requestId;

    @Schema(description = "ID do cliente solicitante", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "{mensagemIA.clienteId.positive}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Conteúdo textual enviado à IA",
            example = "Qual a dosagem recomendada para dor de cabeça?",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mensagemIA.mensagem.notBlank}")
    @Size(min = 1, max = 2000, message = "{mensagemIA.mensagem.size}")
    @JsonProperty("mensagem")
    private String mensagem;

    @Schema(description = "Histórico de mensagens para manter contexto com a IA")
    @Size(max = 20, message = "{mensagemIA.contexto.size}")
    @Valid
    @JsonProperty("contexto")
    private List<
            @NotBlank
            @Size(max = 2000, message = "{mensagemIA.contexto.item.size}")
            String> contexto;

    // --- Configuração do modelo IA ---

    @Schema(description = "Modelo de IA a ser utilizado",
            example = "gpt-4o",
            allowableValues = {"gpt-4", "gpt-4o", "gpt-4-mini", "gpt-3.5-turbo"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mensagemIA.modelo.notBlank}")
    @Size(max = 50, message = "{mensagemIA.modelo.size}")
    @JsonProperty("modelo")
    @Builder.Default
    private String modelo = "gpt-4";

    @Schema(description = "Temperatura (criatividade) da geração de resposta", example = "0.7")
    @DecimalMin(value = "0.0", message = "{mensagemIA.temperatura.min}")
    @DecimalMax(value = "1.0", message = "{mensagemIA.temperatura.max}")
    @JsonProperty("temperatura")
    @Builder.Default
    private Double temperatura = 0.7;

    @Schema(description = "Número máximo de tokens de resposta", example = "512")
    @Min(value = 1, message = "{mensagemIA.maxTokens.min}")
    @Max(value = 2048, message = "{mensagemIA.maxTokens.max}")
    @JsonProperty("maxTokens")
    @Builder.Default
    private Integer maxTokens = 512;

    @Schema(description = "Se a interação deve ser salva para auditoria", example = "true")
    @JsonProperty("salvarLog")
    @Builder.Default
    private Boolean salvarLog = Boolean.TRUE;

    // --- Metadados ---

    @Schema(description = "Identificador do tenant (multi-inquilino)", example = "copainsider-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{mensagemIA.tenantId.notBlank}")
    @Size(max = 100, message = "{mensagemIA.tenantId.size}")
    @Pattern(regexp = "^[a-z0-9-]{3,100}$", message = "{mensagemIA.tenantId.pattern}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento para troubleshooting",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Data/hora da requisição (preenchido pelo servidor)",
            type = "string", format = "date-time",
            example = "2025-07-04T15:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    @PastOrPresent(message = "{mensagemIA.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataEnvio", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataEnvio;
}

