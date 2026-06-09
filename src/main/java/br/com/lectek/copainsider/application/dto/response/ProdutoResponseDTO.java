/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.DecimalMax
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Future
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.PositiveOrZero
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name="ProdutoResponseDTO", description="Detalhes do produto cadastrado no sistema")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ProdutoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID num\u00e9rico interno da entidade Produto (chave do banco)", example="123")
    @JsonProperty(value="entityId")
    private Long entityId;
    @Schema(description="ID \u00fanico p\u00fablico (UUID) do produto", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.id.notNull}")
    @JsonProperty(value="id")
    private @NotNull(message="{produto.id.notNull}") UUID id;
    @Schema(description="Nome do produto", example="Dipirona 500mg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.nome.notBlank}")
    @Size(max=100, message="{produto.nome.size}")
    @JsonProperty(value="nome")
    private @NotBlank(message="{produto.nome.notBlank}") @Size(max=100, message="{produto.nome.size}") String nome;
    @Schema(description="Descri\u00e7\u00e3o do produto", example="Analg\u00e9sico e antit\u00e9rmico", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.descricao.notBlank}")
    @Size(max=1000, message="{produto.descricao.size}")
    @JsonProperty(value="descricao")
    private @NotBlank(message="{produto.descricao.notBlank}") @Size(max=1000, message="{produto.descricao.size}") String descricao;
    @Schema(description="Pre\u00e7o unit\u00e1rio", example="12.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.preco.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{produto.preco.min}")
    @Digits(integer=12, fraction=2, message="{produto.preco.digits}")
    @JsonProperty(value="preco")
    private @NotNull(message="{produto.preco.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{produto.preco.min}") @Digits(integer=12, fraction=2, message="{produto.preco.digits}") BigDecimal preco;
    @Schema(description="URL da imagem do produto", example="https://cdn.farma.com.br/img/dipirona.jpg")
    @Size(max=255, message="{produto.imagem.size}")
    @JsonProperty(value="imagem")
    private @Size(max=255, message="{produto.imagem.size}") String imagem;
    @Schema(description="Lista ordenada de imagens do produto, com a imagem principal na primeira posicao")
    @JsonProperty(value="imagens")
    private List<String> imagens;
    @Schema(description="Categoria do produto", example="ANALGESICOS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.categoria.notBlank}")
    @JsonProperty(value="categoria")
    private @NotBlank(message="{produto.categoria.notBlank}") String categoria;
    @Schema(description="Quantidade atual em estoque", example="150", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.estoqueAtual.notNull}")
    @Min(value=0L, message="{produto.estoqueAtual.min}")
    @JsonProperty(value="estoqueAtual")
    private @NotNull(message="{produto.estoqueAtual.notNull}") @Min(value=0L, message="{produto.estoqueAtual.min}") Integer estoqueAtual;
    @Schema(description="Limite personalizado para alerta de estoque baixo", example="12")
    @JsonProperty(value="alertaEstoqueLimite")
    private Integer alertaEstoqueLimite;
    @Schema(description="Data de validade do produto", type="string", format="date", example="2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.validade.notNull}")
    @Future(message="{produto.validade.future}")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="validade")
    private @NotNull(message="{produto.validade.notNull}") @Future(message="{produto.validade.future}") LocalDate validade;
    @Schema(description="C\u00f3digo de barras", example="7896000033445", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{produto.codigoBarras.notBlank}")
    @Pattern(regexp="^(\\d{8}|\\d{12,14})$", message="{produto.codigoBarras.pattern}")
    @JsonProperty(value="codigoBarras")
    private @NotBlank(message="{produto.codigoBarras.notBlank}") @Pattern(regexp="^(\\d{8}|\\d{12,14})$", message="{produto.codigoBarras.pattern}") String codigoBarras;
    @Schema(description="Marca do produto", example="Gen\u00e9rico")
    @Size(max=100, message="{produto.marca.size}")
    @JsonProperty(value="marca")
    private @Size(max=100, message="{produto.marca.size}") String marca;
    @Schema(description="Fornecedor do produto", example="LekTeC Ind\u00fastria")
    @Size(max=100, message="{produto.fornecedor.size}")
    @JsonProperty(value="fornecedor")
    private @Size(max=100, message="{produto.fornecedor.size}") String fornecedor;
    @Schema(description="Quantidade total vendida", example="1200")
    @PositiveOrZero(message="{produto.quantidadeVendida.min}")
    @JsonProperty(value="quantidadeVendida")
    private @PositiveOrZero(message="{produto.quantidadeVendida.min}") Long quantidadeVendida;
    @Schema(description="Data/hora de cadastro", type="string", format="date-time", example="2025-01-01T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{produto.dataCadastro.notNull}")
    @PastOrPresent(message="{produto.dataCadastro.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataCadastro")
    private @NotNull(message="{produto.dataCadastro.notNull}") @PastOrPresent(message="{produto.dataCadastro.pastOrPresent}") LocalDateTime dataCadastro;
    @Schema(description="Data/hora da \u00faltima atualiza\u00e7\u00e3o", type="string", format="date-time", example="2025-06-15T12:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataAtualizacao")
    private LocalDateTime dataAtualizacao;
    @Schema(description="Flag de produto em destaque", example="true")
    @JsonProperty(value="produtoDestaque")
    private Boolean produtoDestaque = Boolean.FALSE;
    @Schema(description="Flag de recomenda\u00e7\u00e3o IA", example="true")
    @JsonProperty(value="produtoRecomendadoIA")
    private Boolean produtoRecomendadoIA = Boolean.FALSE;
    @Schema(description="Flag de produto controlado (venda restrita)", example="false")
    @JsonProperty(value="produtoControlado")
    private Boolean produtoControlado = Boolean.FALSE;
    @Schema(description="Avalia\u00e7\u00e3o m\u00e9dia do produto (0.0 - 5.0)", example="4.5")
    @DecimalMin(value="0.0", inclusive=true, message="{produto.avaliacaoMedia.min}")
    @DecimalMax(value="5.0", inclusive=true, message="{produto.avaliacaoMedia.max}")
    @Digits(integer=1, fraction=2, message="{produto.avaliacaoMedia.digits}")
    @JsonProperty(value="avaliacaoMedia")
    private @DecimalMin(value="0.0", inclusive=true, message="{produto.avaliacaoMedia.min}") @DecimalMax(value="5.0", inclusive=true, message="{produto.avaliacaoMedia.max}") @Digits(integer=1, fraction=2, message="{produto.avaliacaoMedia.digits}") BigDecimal avaliacaoMedia;
    @Schema(description="Lista de tags do produto", example="[\"dor-de-cabeca\", \"antitermico\"]")
    @JsonProperty(value="tags")
    private List<@NotBlank @Size(max=50) String> tags;
    @Schema(description="Situa\u00e7\u00e3o do produto", example="ATIVO", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"ATIVO", "INATIVO", "DESCONTINUADO", "ESGOTADO"})
    @NotNull(message="{produto.situacao.notNull}")
    @JsonProperty(value="situacao")
    private @NotNull(message="{produto.situacao.notNull}") SituacaoProduto situacao;

    public ProdutoResponseDTO() {
    }

    public ProdutoResponseDTO(UUID id, String nome, String descricao, BigDecimal preco, String imagem, String categoria, Integer estoqueAtual, LocalDate validade, String codigoBarras, String marca, String fornecedor, Long quantidadeVendida, LocalDateTime dataCadastro, LocalDateTime dataAtualizacao, Boolean produtoDestaque, Boolean produtoRecomendadoIA, Boolean produtoControlado, BigDecimal avaliacaoMedia, List<String> tags, SituacaoProduto situacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagem = imagem;
        this.categoria = categoria;
        this.estoqueAtual = estoqueAtual;
        this.validade = validade;
        this.codigoBarras = codigoBarras;
        this.marca = marca;
        this.fornecedor = fornecedor;
        this.quantidadeVendida = quantidadeVendida;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
        this.produtoDestaque = produtoDestaque;
        this.produtoRecomendadoIA = produtoRecomendadoIA;
        this.produtoControlado = produtoControlado;
        this.avaliacaoMedia = avaliacaoMedia;
        this.tags = tags;
        this.situacao = situacao;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    @JsonProperty(value="imagemUrl")
    public String getImagemUrl() {
        return this.imagem;
    }

    public List<String> getImagens() {
        return this.imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getEstoqueAtual() {
        return this.estoqueAtual;
    }

    public void setEstoqueAtual(Integer estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public Integer getAlertaEstoqueLimite() {
        return this.alertaEstoqueLimite;
    }

    public void setAlertaEstoqueLimite(Integer alertaEstoqueLimite) {
        this.alertaEstoqueLimite = alertaEstoqueLimite;
    }

    public LocalDate getValidade() {
        return this.validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getMarca() {
        return this.marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getFornecedor() {
        return this.fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Long getQuantidadeVendida() {
        return this.quantidadeVendida;
    }

    public void setQuantidadeVendida(Long quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }

    public LocalDateTime getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Boolean getProdutoDestaque() {
        return this.produtoDestaque;
    }

    public void setProdutoDestaque(Boolean produtoDestaque) {
        this.produtoDestaque = produtoDestaque;
    }

    public Boolean getProdutoRecomendadoIA() {
        return this.produtoRecomendadoIA;
    }

    public void setProdutoRecomendadoIA(Boolean produtoRecomendadoIA) {
        this.produtoRecomendadoIA = produtoRecomendadoIA;
    }

    public Boolean getProdutoControlado() {
        return this.produtoControlado;
    }

    public void setProdutoControlado(Boolean produtoControlado) {
        this.produtoControlado = produtoControlado;
    }

    public BigDecimal getAvaliacaoMedia() {
        return this.avaliacaoMedia;
    }

    public void setAvaliacaoMedia(BigDecimal avaliacaoMedia) {
        this.avaliacaoMedia = avaliacaoMedia;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public SituacaoProduto getSituacao() {
        return this.situacao;
    }

    public void setSituacao(SituacaoProduto situacao) {
        this.situacao = situacao;
    }

    public static enum SituacaoProduto {
        ATIVO,
        INATIVO,
        DESCONTINUADO,
        ESGOTADO;

    }
}
