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
 *  jakarta.validation.constraints.FutureOrPresent
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.format.annotation.DateTimeFormat$ISO
 */
package br.com.lectek.copainsider.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(name="FiltroRelatorioRequestDTO", description="Filtros para relat\u00f3rios operacionais")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class FiltroRelatorioRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente", example="123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{filtroRelatorio.clienteId.notNull}")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{filtroRelatorio.clienteId.notNull}") Long clienteId;
    @Schema(description="ID da filial", example="10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{filtroRelatorio.filialId.notNull}")
    @JsonProperty(value="filialId")
    private @NotNull(message="{filtroRelatorio.filialId.notNull}") Long filialId;
    @Schema(description="Data inicial (inclusive)", type="string", format="date", example="2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{filtroRelatorio.dataInicial.notNull}")
    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
    @PastOrPresent(message="{filtroRelatorio.dataInicial.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="dataInicial")
    private @NotNull(message="{filtroRelatorio.dataInicial.notNull}") @PastOrPresent(message="{filtroRelatorio.dataInicial.pastOrPresent}") LocalDate dataInicial;
    @Schema(description="Data final (inclusive)", type="string", format="date", example="2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{filtroRelatorio.dataFinal.notNull}")
    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message="{filtroRelatorio.dataFinal.futureOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="dataFinal")
    private @NotNull(message="{filtroRelatorio.dataFinal.notNull}") @FutureOrPresent(message="{filtroRelatorio.dataFinal.futureOrPresent}") LocalDate dataFinal;
    @Schema(description="Lista de categorias de produto para filtrar")
    @Size(max=10, message="{filtroRelatorio.categoriasProduto.size}")
    @JsonProperty(value="categoriasProduto")
    private @Size(max=10, message="{filtroRelatorio.categoriasProduto.size}") List<@NotBlank(message="{filtroRelatorio.categoriasProduto.notBlank}") @Size(max=60, message="{filtroRelatorio.categoriasProduto.item.size}") String> categoriasProduto;
    @Schema(description="ID do tenant (multi-inquilino)", example="copainsider-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{filtroRelatorio.tenantId.notBlank}")
    @Size(max=60, message="{filtroRelatorio.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{filtroRelatorio.tenantId.notBlank}") @Size(max=60, message="{filtroRelatorio.tenantId.size}") String tenantId;
    @Schema(description="Token de rastreamento (UUID) para logs e auditoria", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="N\u00famero da p\u00e1gina (zero-based)", example="0")
    @Min(value=0L, message="{filtroRelatorio.page.min}")
    @JsonProperty(value="page")
    private @Min(value=0L, message="{filtroRelatorio.page.min}") Integer page = 0;
    @Schema(description="Tamanho da p\u00e1gina (1..1000)", example="50")
    @Min(value=1L, message="{filtroRelatorio.size.min}")
    @Max(value=1000L, message="{filtroRelatorio.size.max}")
    @JsonProperty(value="size")
    private @Min(value=1L, message="{filtroRelatorio.size.min}") @Max(value=1000L, message="{filtroRelatorio.size.max}") Integer size = 50;
    @Schema(description="Crit\u00e9rios de ordena\u00e7\u00e3o no formato \"campo,ASC|DESC\" (ex.: dataInicial,DESC)")
    @Size(max=5, message="{filtroRelatorio.sort.size}")
    @JsonProperty(value="sort")
    private @Size(max=5, message="{filtroRelatorio.sort.size}") List<@Pattern(regexp="^[a-zA-Z0-9_\\.]+,(ASC|DESC)$", message="{filtroRelatorio.sort.pattern}") String> sort;

    @AssertTrue(message="{filtroRelatorio.periodo.coerente}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{filtroRelatorio.periodo.coerente}") boolean isPeriodoValido() {
        if (this.dataInicial == null || this.dataFinal == null) {
            return false;
        }
        return !this.dataInicial.isAfter(this.dataFinal);
    }

    public FiltroRelatorioRequestDTO() {
    }

    public FiltroRelatorioRequestDTO(Long clienteId, Long filialId, LocalDate dataInicial, LocalDate dataFinal, List<String> categoriasProduto, String tenantId, UUID traceId, Integer page, Integer size, List<String> sort) {
        this.clienteId = clienteId;
        this.filialId = filialId;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.categoriasProduto = categoriasProduto;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getFilialId() {
        return this.filialId;
    }

    public void setFilialId(Long filialId) {
        this.filialId = filialId;
    }

    public LocalDate getDataInicial() {
        return this.dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return this.dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public List<String> getCategoriasProduto() {
        return this.categoriasProduto;
    }

    public void setCategoriasProduto(List<String> categoriasProduto) {
        this.categoriasProduto = categoriasProduto;
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

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<String> getSort() {
        return this.sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FiltroRelatorioRequestDTO)) {
            return false;
        }
        FiltroRelatorioRequestDTO that = (FiltroRelatorioRequestDTO)o;
        return Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.filialId, that.filialId) && Objects.equals(this.dataInicial, that.dataInicial) && Objects.equals(this.dataFinal, that.dataFinal) && Objects.equals(this.categoriasProduto, that.categoriasProduto) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.page, that.page) && Objects.equals(this.size, that.size) && Objects.equals(this.sort, that.sort);
    }

    public int hashCode() {
        return Objects.hash(this.clienteId, this.filialId, this.dataInicial, this.dataFinal, this.categoriasProduto, this.tenantId, this.traceId, this.page, this.size, this.sort);
    }

    public String toString() {
        return "FiltroRelatorioRequestDTO{clienteId=" + this.clienteId + ", filialId=" + this.filialId + ", dataInicial=" + String.valueOf(this.dataInicial) + ", dataFinal=" + String.valueOf(this.dataFinal) + ", categoriasProduto=" + String.valueOf(this.categoriasProduto) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", page=" + this.page + ", size=" + this.size + ", sort=" + String.valueOf(this.sort) + "}";
    }
}


