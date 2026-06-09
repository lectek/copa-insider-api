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
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Schema(name="ProdutoRequestDTO", description="Dados para cria\u00e7\u00e3o/atualiza\u00e7\u00e3o de produto")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ProdutoRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID p\u00fablico (UUID) do produto", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="produtoId")
    private UUID produtoId;
    @Schema(description="SKU do produto", example="PRD-001-ABC", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.sku.notBlank}")
    @Size(max=50, message="{produto.sku.size}")
    @JsonProperty(value="sku")
    private @NotBlank(message="{produto.sku.notBlank}") @Size(max=50, message="{produto.sku.size}") String sku;
    @Schema(description="Nome do produto", example="Dipirona 500mg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.nome.notBlank}")
    @Size(max=100, message="{produto.nome.size}")
    @JsonProperty(value="nome")
    private @NotBlank(message="{produto.nome.notBlank}") @Size(max=100, message="{produto.nome.size}") String nome;
    @Schema(description="Descri\u00e7\u00e3o detalhada do produto", example="Analg\u00e9sico e antit\u00e9rmico", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.descricao.notBlank}")
    @Size(max=1000, message="{produto.descricao.size}")
    @JsonProperty(value="descricao")
    private @NotBlank(message="{produto.descricao.notBlank}") @Size(max=1000, message="{produto.descricao.size}") String descricao;
    @Schema(description="Pre\u00e7o unit\u00e1rio do produto", example="12.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.preco.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{produto.preco.min}")
    @JsonProperty(value="preco")
    private @NotNull(message="{produto.preco.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{produto.preco.min}") BigDecimal preco;
    @Schema(description="URL da imagem do produto (obrigatória apenas quando o produto estiver ativo para venda)", example="https://cdn.farma/img/produto1.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max=200, message="{produto.imagem.size}")
    @Pattern(regexp="(^$)|(^https?://.*$)", message="{produto.imagem.pattern}")
    @JsonProperty(value="imagem")
    private @Size(max=200, message="{produto.imagem.size}") @Pattern(regexp="(^$)|(^https?://.*$)", message="{produto.imagem.pattern}") String imagem;
    @Schema(description="Categoria do produto", example="ANALGESICO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.categoria.notBlank}")
    @Size(max=50, message="{produto.categoria.size}")
    @JsonProperty(value="categoria")
    private @NotBlank(message="{produto.categoria.notBlank}") @Size(max=50, message="{produto.categoria.size}") String categoria;
    @Schema(description="C\u00f3digo de barras (EAN/UPC)", example="7891234567895")
    @Size(max=50, message="{produto.codigoBarras.size}")
    @Pattern(regexp="^(\\d{8}|\\d{12,14})$", message="{produto.codigoBarras.pattern}")
    @JsonProperty(value="codigoBarras")
    private @Size(max=50, message="{produto.codigoBarras.size}") @Pattern(regexp="^(\\d{8}|\\d{12,14})$", message="{produto.codigoBarras.pattern}") String codigoBarras;
    @Schema(description="Quantidade em estoque", example="100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.estoque.notNull}")
    @Min(value=0L, message="{produto.estoque.min}")
    @JsonProperty(value="estoque")
    private @NotNull(message="{produto.estoque.notNull}") @Min(value=0L, message="{produto.estoque.min}") Integer estoque;
    @Schema(description="Peso do produto (g)", example="200")
    @Min(value=1L, message="{produto.peso.min}")
    @JsonProperty(value="peso")
    private @Min(value=1L, message="{produto.peso.min}") Integer peso;
    @Schema(description="Dimens\u00f5es (CxLxA em cm), formato '10,5,2'", example="10,5,2")
    @Pattern(regexp="^\\d{1,4}(,\\d{1,4}){2}$", message="{produto.dimensoes.pattern}")
    @JsonProperty(value="dimensoes")
    private @Pattern(regexp="^\\d{1,4}(,\\d{1,4}){2}$", message="{produto.dimensoes.pattern}") String dimensoes;
    @Schema(description="Produto ativo para venda (quando true exige imagem)", example="false")
    @JsonProperty(value="ativo")
    private Boolean ativo = Boolean.FALSE;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{produto.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Data/hora de cria\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T10:00:00")
    @PastOrPresent(message="{produto.criadoEm.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="criadoEm", access=JsonProperty.Access.READ_ONLY)
    private @PastOrPresent(message="{produto.criadoEm.pastOrPresent}") LocalDateTime criadoEm;
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-07-05T12:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="atualizadoEm", access=JsonProperty.Access.READ_ONLY)
    private LocalDateTime atualizadoEm;

    public ProdutoRequestDTO() {
    }

    public ProdutoRequestDTO(UUID produtoId, String sku, String nome, String descricao, BigDecimal preco, String imagem, String categoria, String codigoBarras, Integer estoque, Integer peso, String dimensoes, Boolean ativo, String tenantId, UUID traceId, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.produtoId = produtoId;
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagem = imagem;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.estoque = estoque;
        this.peso = peso;
        this.dimensoes = dimensoes;
        this.ativo = ativo;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getProdutoId() {
        return this.produtoId;
    }

    public void setProdutoId(UUID produtoId) {
        this.produtoId = produtoId;
    }

    public String getSku() {
        return this.sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return this.preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getImagem() {
        return this.imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public Integer getEstoque() {
        return this.estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getPeso() {
        return this.peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public String getDimensoes() {
        return this.dimensoes;
    }

    public void setDimensoes(String dimensoes) {
        this.dimensoes = dimensoes;
    }

    public Boolean getAtivo() {
        return this.ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
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
        if (!(o instanceof ProdutoRequestDTO)) {
            return false;
        }
        ProdutoRequestDTO that = (ProdutoRequestDTO)o;
        return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.sku, that.sku) && Objects.equals(this.nome, that.nome) && Objects.equals(this.descricao, that.descricao) && Objects.equals(this.preco, that.preco) && Objects.equals(this.imagem, that.imagem) && Objects.equals(this.categoria, that.categoria) && Objects.equals(this.codigoBarras, that.codigoBarras) && Objects.equals(this.estoque, that.estoque) && Objects.equals(this.peso, that.peso) && Objects.equals(this.dimensoes, that.dimensoes) && Objects.equals(this.ativo, that.ativo) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.criadoEm, that.criadoEm) && Objects.equals(this.atualizadoEm, that.atualizadoEm);
    }

    public int hashCode() {
        return Objects.hash(this.produtoId, this.sku, this.nome, this.descricao, this.preco, this.imagem, this.categoria, this.codigoBarras, this.estoque, this.peso, this.dimensoes, this.ativo, this.tenantId, this.traceId, this.criadoEm, this.atualizadoEm);
    }

    public String toString() {
        return "ProdutoRequestDTO{produtoId=" + String.valueOf(this.produtoId) + ", sku='" + this.sku + "', nome='" + this.nome + "', descricao='" + this.descricao + "', preco=" + String.valueOf(this.preco) + ", imagem='" + this.imagem + "', categoria='" + this.categoria + "', codigoBarras='" + this.codigoBarras + "', estoque=" + this.estoque + ", peso=" + this.peso + ", dimensoes='" + this.dimensoes + "', ativo=" + this.ativo + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", criadoEm=" + String.valueOf(this.criadoEm) + ", atualizadoEm=" + String.valueOf(this.atualizadoEm) + "}";
    }
}

