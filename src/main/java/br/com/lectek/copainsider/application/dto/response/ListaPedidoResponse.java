package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(name = "ListaPedidoResponse", description = "Resposta contendo lista de pedidos e metadados")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaPedidoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Lista de pedidos retornados", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{listaPedido.pedidos.notNull}")
    @Valid
    @JsonProperty("pedidos")
    private List<@Valid PedidoResponseDTO> pedidos;

    @Schema(description = "Dados de paginação dos resultados", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{listaPedido.paginacao.notNull}")
    @Valid
    @JsonProperty("paginacao")
    private PaginacaoDTO paginacao;

    @Schema(description = "Critérios de ordenação usados na consulta", example = "[\"dataPedido,DESC\"]")
    @JsonProperty("ordenacao")
    private List<@Pattern(regexp = "[a-zA-Z0-9_]+,(ASC|DESC)", message = "{listaPedido.ordenacao.pattern}") String> ordenacao;

    @Schema(description = "Filtros aplicados na geração da lista")
    @JsonProperty("filtrosAplicados")
    private Map<@NotBlank String, @NotBlank String> filtrosAplicados;

    @Schema(description = "Status da consulta de pedidos", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{listaPedido.status.notBlank}")
    @JsonProperty("statusConsulta")
    private String statusConsulta;

    @Schema(description = "Data/hora de geração da resposta", type = "string", format = "date-time",
            example = "2025-07-04T17:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{listaPedido.geradoEm.notNull}")
    @PastOrPresent(message = "{listaPedido.geradoEm.pastOrPresent}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("geradoEm")
    private LocalDateTime geradoEm;

    @Schema(description = "Tempo de processamento da consulta (ms)", example = "150")
    @PositiveOrZero(message = "{listaPedido.tempoProcessamento.min}")
    @JsonProperty("tempoProcessamentoMs")
    private Long tempoProcessamentoMs;

    @Schema(description = "Usuário que solicitou a lista", example = "joao.silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{listaPedido.usuarioSolicitante.notBlank}")
    @JsonProperty("usuarioSolicitante")
    private String usuarioSolicitante;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "copainsider-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{listaPedido.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de correlação (UUID)", example = "5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    /* ======================= Tipos internos ======================= */

    @Schema(name = "PaginacaoDTO", description = "Dados de paginação")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginacaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Página atual (zero-based)", example = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{paginacao.pagina.notNull}")
        @Min(value = 0, message = "{paginacao.pagina.min}")
        @JsonProperty("paginaAtual")
        private Integer paginaAtual;

        @Schema(description = "Tamanho da página", example = "50",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{paginacao.tamanho.notNull}")
        @Min(value = 1, message = "{paginacao.tamanho.min}")
        @JsonProperty("tamanhoPagina")
        private Integer tamanhoPagina;

        @Schema(description = "Total de páginas", example = "20",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{paginacao.totalPaginas.notNull}")
        @Min(value = 0, message = "{paginacao.totalPaginas.min}")
        @JsonProperty("totalPaginas")
        private Integer totalPaginas;

        @Schema(description = "Total de registros encontrados", example = "1000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{paginacao.totalRegistros.notNull}")
        @Min(value = 0, message = "{paginacao.totalRegistros.min}")
        @JsonProperty("totalRegistros")
        private Long totalRegistros;
    }
}
