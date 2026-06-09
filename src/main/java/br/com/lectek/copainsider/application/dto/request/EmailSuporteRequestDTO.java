package br.com.lectek.copainsider.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@Schema(name = "EmailSuporteRequestDTO", description = "Dados para envio de e-mail ao suporte técnico")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class EmailSuporteRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da solicitação de suporte",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("solicitacaoId")
    private UUID solicitacaoId;

    @Schema(description = "Nome completo do cliente",
            example = "Maria Oliveira", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporte.nome.notBlank}")
    @Size(min = 5, max = 100, message = "{emailSuporte.nome.size}")
    @Pattern(regexp = ".*\\s+.*", message = "{emailSuporte.nome.nomeCompleto}")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "E-mail do cliente",
            example = "maria@exemplo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporte.email.notBlank}")
    @Email(message = "{emailSuporte.email.valid}")
    @Size(max = 150, message = "{emailSuporte.email.size}")
    @JsonProperty("email")
    private String email;

    @Schema(description = "Assunto da mensagem",
            example = "Erro no pedido de medicamento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporte.assunto.notBlank}")
    @Size(max = 150, message = "{emailSuporte.assunto.size}")
    @JsonProperty("assunto")
    private String assunto;

    @Schema(description = "Categoria de suporte",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "TECNICO")
    @NotNull(message = "{emailSuporte.categoria.notNull}")
    @JsonProperty("categoria")
    private Categoria categoria;

    @Schema(description = "Prioridade da solicitação",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "HIGH")
    @NotNull(message = "{emailSuporte.prioridade.notNull}")
    @JsonProperty("prioridade")
    private Priority prioridade;

    @Schema(description = "Conteúdo da mensagem de suporte",
            example = "Ao tentar finalizar o pedido, recebo erro 500.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporte.mensagem.notBlank}")
    @Size(min = 10, max = 2000, message = "{emailSuporte.mensagem.size}")
    @JsonProperty("mensagem")
    private String mensagem;

    @Schema(description = "Lista de anexos (nomes de arquivo ou URLs)", example = "[\"erro.png\", \"https://exemplo.com/log.txt\"]")
    @Size(max = 5, message = "{emailSuporte.anexos.size}")
    @Valid
    @JsonProperty("anexos")
    private List<
            @NotBlank
            @Size(max = 200)
            @Pattern(
                regexp = "^(https?://.+|[\\w\\-. ]+\\.[A-Za-z0-9]{2,10})$",
                message = "{emailSuporte.anexos.pattern}"
            )
            String> anexos;

    @Schema(description = "ID do tenant", example = "copainsider-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{emailSuporte.tenantId.notBlank}")
    @Size(max = 60, message = "{emailSuporte.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Data/hora do envio",
            type = "string", format = "date-time",
            example = "2025-07-04T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{emailSuporte.dataEnvio.notNull}")
    @PastOrPresent(message = "{emailSuporte.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEnvio")
    private LocalDateTime dataEnvio;

    @Schema(description = "E-mail para cópia oculta (BCC)",
            example = "suporte-gestor@lektec.com.br")
    @Email(message = "{emailSuporte.bcc.valid}")
    @Size(max = 150, message = "{emailSuporte.bcc.size}")
    @JsonProperty("bcc")
    private String bcc;

    // Enums mantidos aqui; se precisar reaproveitar, pode extrair para o pacote domain
    public enum Categoria { PEDIDO, TECNICO, FINANCEIRO, OUTROS }
    public enum Priority  { LOW, MEDIUM, HIGH }
}

