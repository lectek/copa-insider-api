/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.DecimalMax
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.PositiveOrZero
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Schema(name="RelatorioResponseDTO", description="Dados agregados do relat\u00f3rio gerado")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class RelatorioResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Data de in\u00edcio do relat\u00f3rio", type="string", format="date", example="2025-07-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.dataInicio.notNull}")
    @JsonProperty(value="dataInicio")
    @JsonFormat(pattern="yyyy-MM-dd")
    private @NotNull(message="{relatorio.dataInicio.notNull}") LocalDate dataInicio;
    @Schema(description="Data de fim do relat\u00f3rio", type="string", format="date", example="2025-07-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.dataFim.notNull}")
    @JsonProperty(value="dataFim")
    @JsonFormat(pattern="yyyy-MM-dd")
    private @NotNull(message="{relatorio.dataFim.notNull}") LocalDate dataFim;
    @Schema(description="Categoria principal de filtro", example="FARMACEUTICOS")
    @Size(max=100, message="{relatorio.categoriaFiltro.size}")
    @JsonProperty(value="categoriaFiltro")
    private @Size(max=100, message="{relatorio.categoriaFiltro.size}") String categoriaFiltro;
    @Schema(description="Total de vendas no per\u00edodo", example="15000.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.totalVendas.notNull}")
    @DecimalMin(value="0.00", inclusive=true, message="{relatorio.totalVendas.min}")
    @Digits(integer=14, fraction=2, message="{relatorio.totalVendas.digits}")
    @JsonProperty(value="totalVendas")
    private @NotNull(message="{relatorio.totalVendas.notNull}") @DecimalMin(value="0.00", inclusive=true, message="{relatorio.totalVendas.min}") @Digits(integer=14, fraction=2, message="{relatorio.totalVendas.digits}") BigDecimal totalVendas;
    @Schema(description="Margem de lucro (%)", example="25.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.margemLucro.notNull}")
    @DecimalMin(value="0.00", inclusive=true, message="{relatorio.margemLucro.min}")
    @DecimalMax(value="100.00", inclusive=true, message="{relatorio.margemLucro.max}")
    @Digits(integer=3, fraction=2, message="{relatorio.margemLucro.digits}")
    @JsonProperty(value="margemLucro")
    private @NotNull(message="{relatorio.margemLucro.notNull}") @DecimalMin(value="0.00", inclusive=true, message="{relatorio.margemLucro.min}") @DecimalMax(value="100.00", inclusive=true, message="{relatorio.margemLucro.max}") @Digits(integer=3, fraction=2, message="{relatorio.margemLucro.digits}") BigDecimal margemLucro;
    @Schema(description="Quantidade de produtos vendidos", example="1200", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.quantidadeProdutosVendidos.notNull}")
    @Min(value=0L, message="{relatorio.quantidadeProdutosVendidos.min}")
    @JsonProperty(value="quantidadeProdutosVendidos")
    private @NotNull(message="{relatorio.quantidadeProdutosVendidos.notNull}") @Min(value=0L, message="{relatorio.quantidadeProdutosVendidos.min}") Long quantidadeProdutosVendidos;
    @Schema(description="Total de pedidos realizados", example="300", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.totalPedidos.notNull}")
    @Min(value=0L, message="{relatorio.totalPedidos.min}")
    @JsonProperty(value="totalPedidos")
    private @NotNull(message="{relatorio.totalPedidos.notNull}") @Min(value=0L, message="{relatorio.totalPedidos.min}") Long totalPedidos;
    @Schema(description="Ticket m\u00e9dio", example="50.25")
    @DecimalMin(value="0.00", inclusive=true, message="{relatorio.mediaTicket.min}")
    @Digits(integer=14, fraction=2, message="{relatorio.mediaTicket.digits}")
    @JsonProperty(value="mediaTicket")
    private @DecimalMin(value="0.00", inclusive=true, message="{relatorio.mediaTicket.min}") @Digits(integer=14, fraction=2, message="{relatorio.mediaTicket.digits}") BigDecimal mediaTicket;
    @Schema(description="Vendas por categoria (categoria -> valor)", example="{\"REMEDIOS\":10000.00,\"PERFUMARIA\":5000.50}")
    @JsonProperty(value="vendasPorCategoria")
    private Map<@NotBlank @Size(max=100) String, @NotNull @Digits(integer=14, fraction=2) BigDecimal> vendasPorCategoria;
    @Schema(description="Lucro por produto (produtoId -> valor)", example="{\"001\":2000.00,\"002\":1500.00}")
    @JsonProperty(value="lucroPorProduto")
    private Map<@NotBlank @Size(max=64) String, @NotNull @Digits(integer=14, fraction=2) BigDecimal> lucroPorProduto;
    @Schema(description="Produtos mais vendidos no per\u00edodo")
    @JsonProperty(value="produtosMaisVendidos")
    private List<@NotBlank @Size(max=150) String> produtosMaisVendidos;
    @Schema(description="Data/hora de gera\u00e7\u00e3o do relat\u00f3rio", type="string", format="date-time", example="2025-08-01T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{relatorio.dataGeracaoRelatorio.notNull}")
    @PastOrPresent(message="{relatorio.dataGeracaoRelatorio.pastOrPresent}")
    @JsonProperty(value="dataGeracaoRelatorio")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private @NotNull(message="{relatorio.dataGeracaoRelatorio.notNull}") @PastOrPresent(message="{relatorio.dataGeracaoRelatorio.pastOrPresent}") LocalDateTime dataGeracaoRelatorio;
    @Schema(description="Usu\u00e1rio solicitante", example="joao.silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{relatorio.usuarioSolicitante.notBlank}")
    @Size(max=100, message="{relatorio.usuarioSolicitante.size}")
    @JsonProperty(value="usuarioSolicitante")
    private @NotBlank(message="{relatorio.usuarioSolicitante.notBlank}") @Size(max=100, message="{relatorio.usuarioSolicitante.size}") String usuarioSolicitante;
    @Schema(description="ID do tenant (multi-inquilino)", example="copainsider-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{relatorio.tenantId.notBlank}")
    @Size(max=100, message="{relatorio.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{relatorio.tenantId.notBlank}") @Size(max=100, message="{relatorio.tenantId.size}") String tenantId;
    @Schema(description="Token de correla\u00e7\u00e3o (UUID)", example="5fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Dura\u00e7\u00e3o da gera\u00e7\u00e3o do relat\u00f3rio (ms)", example="1500")
    @PositiveOrZero(message="{relatorio.duracaoConsulta.min}")
    @JsonProperty(value="duracaoConsulta")
    private @PositiveOrZero(message="{relatorio.duracaoConsulta.min}") Long duracaoConsulta;
    @Schema(description="M\u00e9todo de exporta\u00e7\u00e3o do relat\u00f3rio", example="PDF", allowableValues={"PDF", "EXCEL", "CSV", "JSON"})
    @JsonProperty(value="metodoExportacao")
    private MetodoExportacao metodoExportacao;
    @Schema(description="Status da gera\u00e7\u00e3o do relat\u00f3rio", example="SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"SUCCESS", "EMPTY", "ERROR"})
    @NotNull(message="{relatorio.statusConsulta.notNull}")
    @JsonProperty(value="statusConsulta")
    private @NotNull(message="{relatorio.statusConsulta.notNull}") StatusConsulta statusConsulta;

    @AssertTrue(message="{relatorio.periodo.valido}")
    public @AssertTrue(message="{relatorio.periodo.valido}") boolean isPeriodoValido() {
        if (this.dataInicio == null || this.dataFim == null) {
            return true;
        }
        return !this.dataFim.isBefore(this.dataInicio);
    }

    public RelatorioResponseDTO() {
    }

    public RelatorioResponseDTO(LocalDate dataInicio, LocalDate dataFim, String categoriaFiltro, BigDecimal totalVendas, BigDecimal margemLucro, Long quantidadeProdutosVendidos, Long totalPedidos, BigDecimal mediaTicket, Map<String, BigDecimal> vendasPorCategoria, Map<String, BigDecimal> lucroPorProduto, List<String> produtosMaisVendidos, LocalDateTime dataGeracaoRelatorio, String usuarioSolicitante, String tenantId, UUID traceId, Long duracaoConsulta, MetodoExportacao metodoExportacao, StatusConsulta statusConsulta) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.categoriaFiltro = categoriaFiltro;
        this.totalVendas = totalVendas;
        this.margemLucro = margemLucro;
        this.quantidadeProdutosVendidos = quantidadeProdutosVendidos;
        this.totalPedidos = totalPedidos;
        this.mediaTicket = mediaTicket;
        this.vendasPorCategoria = vendasPorCategoria;
        this.lucroPorProduto = lucroPorProduto;
        this.produtosMaisVendidos = produtosMaisVendidos;
        this.dataGeracaoRelatorio = dataGeracaoRelatorio;
        this.usuarioSolicitante = usuarioSolicitante;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.duracaoConsulta = duracaoConsulta;
        this.metodoExportacao = metodoExportacao;
        this.statusConsulta = statusConsulta;
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

    public String getCategoriaFiltro() {
        return this.categoriaFiltro;
    }

    public void setCategoriaFiltro(String categoriaFiltro) {
        this.categoriaFiltro = categoriaFiltro;
    }

    public BigDecimal getTotalVendas() {
        return this.totalVendas;
    }

    public void setTotalVendas(BigDecimal totalVendas) {
        this.totalVendas = totalVendas;
    }

    public BigDecimal getMargemLucro() {
        return this.margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    public Long getQuantidadeProdutosVendidos() {
        return this.quantidadeProdutosVendidos;
    }

    public void setQuantidadeProdutosVendidos(Long quantidadeProdutosVendidos) {
        this.quantidadeProdutosVendidos = quantidadeProdutosVendidos;
    }

    public Long getTotalPedidos() {
        return this.totalPedidos;
    }

    public void setTotalPedidos(Long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public BigDecimal getMediaTicket() {
        return this.mediaTicket;
    }

    public void setMediaTicket(BigDecimal mediaTicket) {
        this.mediaTicket = mediaTicket;
    }

    public Map<String, BigDecimal> getVendasPorCategoria() {
        return this.vendasPorCategoria;
    }

    public void setVendasPorCategoria(Map<String, BigDecimal> vendasPorCategoria) {
        this.vendasPorCategoria = vendasPorCategoria;
    }

    public Map<String, BigDecimal> getLucroPorProduto() {
        return this.lucroPorProduto;
    }

    public void setLucroPorProduto(Map<String, BigDecimal> lucroPorProduto) {
        this.lucroPorProduto = lucroPorProduto;
    }

    public List<String> getProdutosMaisVendidos() {
        return this.produtosMaisVendidos;
    }

    public void setProdutosMaisVendidos(List<String> produtosMaisVendidos) {
        this.produtosMaisVendidos = produtosMaisVendidos;
    }

    public LocalDateTime getDataGeracaoRelatorio() {
        return this.dataGeracaoRelatorio;
    }

    public void setDataGeracaoRelatorio(LocalDateTime dataGeracaoRelatorio) {
        this.dataGeracaoRelatorio = dataGeracaoRelatorio;
    }

    public String getUsuarioSolicitante() {
        return this.usuarioSolicitante;
    }

    public void setUsuarioSolicitante(String usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
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

    public Long getDuracaoConsulta() {
        return this.duracaoConsulta;
    }

    public void setDuracaoConsulta(Long duracaoConsulta) {
        this.duracaoConsulta = duracaoConsulta;
    }

    public MetodoExportacao getMetodoExportacao() {
        return this.metodoExportacao;
    }

    public void setMetodoExportacao(MetodoExportacao metodoExportacao) {
        this.metodoExportacao = metodoExportacao;
    }

    public StatusConsulta getStatusConsulta() {
        return this.statusConsulta;
    }

    public void setStatusConsulta(StatusConsulta statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RelatorioResponseDTO)) {
            return false;
        }
        RelatorioResponseDTO that = (RelatorioResponseDTO)o;
        return Objects.equals(this.dataInicio, that.dataInicio) && Objects.equals(this.dataFim, that.dataFim) && Objects.equals(this.categoriaFiltro, that.categoriaFiltro) && Objects.equals(this.totalVendas, that.totalVendas) && Objects.equals(this.margemLucro, that.margemLucro) && Objects.equals(this.quantidadeProdutosVendidos, that.quantidadeProdutosVendidos) && Objects.equals(this.totalPedidos, that.totalPedidos) && Objects.equals(this.mediaTicket, that.mediaTicket) && Objects.equals(this.vendasPorCategoria, that.vendasPorCategoria) && Objects.equals(this.lucroPorProduto, that.lucroPorProduto) && Objects.equals(this.produtosMaisVendidos, that.produtosMaisVendidos) && Objects.equals(this.dataGeracaoRelatorio, that.dataGeracaoRelatorio) && Objects.equals(this.usuarioSolicitante, that.usuarioSolicitante) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.duracaoConsulta, that.duracaoConsulta) && this.metodoExportacao == that.metodoExportacao && this.statusConsulta == that.statusConsulta;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.dataInicio, this.dataFim, this.categoriaFiltro, this.totalVendas, this.margemLucro, this.quantidadeProdutosVendidos, this.totalPedidos, this.mediaTicket, this.vendasPorCategoria, this.lucroPorProduto, this.produtosMaisVendidos, this.dataGeracaoRelatorio, this.usuarioSolicitante, this.tenantId, this.traceId, this.duracaoConsulta, this.metodoExportacao, this.statusConsulta});
    }

    public String toString() {
        return "RelatorioResponseDTO{dataInicio=" + String.valueOf(this.dataInicio) + ", dataFim=" + String.valueOf(this.dataFim) + ", categoriaFiltro='" + this.categoriaFiltro + "', totalVendas=" + String.valueOf(this.totalVendas) + ", margemLucro=" + String.valueOf(this.margemLucro) + ", quantidadeProdutosVendidos=" + this.quantidadeProdutosVendidos + ", totalPedidos=" + this.totalPedidos + ", mediaTicket=" + String.valueOf(this.mediaTicket) + ", vendasPorCategoria=" + String.valueOf(this.vendasPorCategoria) + ", lucroPorProduto=" + String.valueOf(this.lucroPorProduto) + ", produtosMaisVendidos=" + String.valueOf(this.produtosMaisVendidos) + ", dataGeracaoRelatorio=" + String.valueOf(this.dataGeracaoRelatorio) + ", usuarioSolicitante='" + this.usuarioSolicitante + "', tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", duracaoConsulta=" + this.duracaoConsulta + ", metodoExportacao=" + String.valueOf((Object)this.metodoExportacao) + ", statusConsulta=" + String.valueOf((Object)this.statusConsulta) + "}";
    }

    public static enum MetodoExportacao {
        PDF,
        EXCEL,
        CSV,
        JSON;

    }

    public static enum StatusConsulta {
        SUCCESS,
        EMPTY,
        ERROR;

    }
}


