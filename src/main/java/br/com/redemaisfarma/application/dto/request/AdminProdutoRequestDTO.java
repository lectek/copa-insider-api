package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class AdminProdutoRequestDTO {

    @NotBlank(message = "{produto.nome.notBlank}")
    @Size(max = 100, message = "{produto.nome.size}")
    private String nome;

    @Size(max = 1000, message = "{produto.descricao.size}")
    private String descricao;

    @NotNull(message = "{produto.preco.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{produto.preco.min}")
    private BigDecimal preco;

    @Size(max = 200, message = "{produto.imagem.size}")
    @Pattern(
            regexp = "(^$)|(^https?://.*$)|(^/.*$)",
            message = "{produto.imagem.pattern}"
    )
    private String imagem;

    @Size(max = 10, message = "Envie no maximo 10 imagens por produto.")
    private List<
            @Size(max = 200, message = "{produto.imagem.size}")
            @Pattern(
                    regexp = "(^$)|(^https?://.*$)|(^/.*$)",
                    message = "{produto.imagem.pattern}"
            ) String> imagens;

    @NotBlank(message = "{produto.categoria.notBlank}")
    @Size(max = 50, message = "{produto.categoria.size}")
    private String categoria;

    @Size(max = 50, message = "{produto.codigoBarras.size}")
    @Pattern(regexp = "(^$)|(\\d{8}|\\d{12,14})", message = "{produto.codigoBarras.pattern}")
    private String codigoBarras;

    @NotNull(message = "{produto.estoque.notNull}")
    @Min(value = 0, message = "{produto.estoque.min}")
    private Integer estoque;

    @Min(value = 2, message = "{produto.alertaEstoqueLimite.min}")
    @Max(value = 100000, message = "{produto.alertaEstoqueLimite.max}")
    private Integer alertaEstoqueLimite;

    @Size(max = 32, message = "{produto.tarjaMedicacao.size}")
    private String tarjaMedicacao;

    private Boolean exigeReceita = Boolean.FALSE;

    @Pattern(regexp = "(^$)|(\\d{8})", message = "{produto.fiscalNcm.pattern}")
    private String fiscalNcm;

    @Pattern(regexp = "(^$)|(\\d{7})", message = "{produto.fiscalCest.pattern}")
    private String fiscalCest;

    @Pattern(regexp = "(^$)|(\\d{4})", message = "{produto.fiscalCfop.pattern}")
    private String fiscalCfop;

    @Min(value = 0, message = "{produto.fiscalOrigem.min}")
    @Max(value = 8, message = "{produto.fiscalOrigem.max}")
    private Integer fiscalOrigem;

    @Pattern(regexp = "(^$)|(\\d{2,3})", message = "{produto.fiscalIcmsCst.pattern}")
    private String fiscalIcmsCst;

    @Pattern(regexp = "(^$)|(\\d{3})", message = "{produto.fiscalCsosn.pattern}")
    private String fiscalCsosn;

    @Pattern(regexp = "(^$)|(\\d{2})", message = "{produto.fiscalPisCst.pattern}")
    private String fiscalPisCst;

    @Pattern(regexp = "(^$)|(\\d{2})", message = "{produto.fiscalCofinsCst.pattern}")
    private String fiscalCofinsCst;

    private Boolean ativo = Boolean.FALSE;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public List<String> getImagens() {
        return imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getAlertaEstoqueLimite() {
        return alertaEstoqueLimite;
    }

    public void setAlertaEstoqueLimite(Integer alertaEstoqueLimite) {
        this.alertaEstoqueLimite = alertaEstoqueLimite;
    }

    public String getTarjaMedicacao() {
        return tarjaMedicacao;
    }

    public void setTarjaMedicacao(String tarjaMedicacao) {
        this.tarjaMedicacao = tarjaMedicacao;
    }

    public Boolean getExigeReceita() {
        return exigeReceita;
    }

    public void setExigeReceita(Boolean exigeReceita) {
        this.exigeReceita = exigeReceita;
    }

    public String getFiscalNcm() {
        return fiscalNcm;
    }

    public void setFiscalNcm(String fiscalNcm) {
        this.fiscalNcm = fiscalNcm;
    }

    public String getFiscalCest() {
        return fiscalCest;
    }

    public void setFiscalCest(String fiscalCest) {
        this.fiscalCest = fiscalCest;
    }

    public String getFiscalCfop() {
        return fiscalCfop;
    }

    public void setFiscalCfop(String fiscalCfop) {
        this.fiscalCfop = fiscalCfop;
    }

    public Integer getFiscalOrigem() {
        return fiscalOrigem;
    }

    public void setFiscalOrigem(Integer fiscalOrigem) {
        this.fiscalOrigem = fiscalOrigem;
    }

    public String getFiscalIcmsCst() {
        return fiscalIcmsCst;
    }

    public void setFiscalIcmsCst(String fiscalIcmsCst) {
        this.fiscalIcmsCst = fiscalIcmsCst;
    }

    public String getFiscalCsosn() {
        return fiscalCsosn;
    }

    public void setFiscalCsosn(String fiscalCsosn) {
        this.fiscalCsosn = fiscalCsosn;
    }

    public String getFiscalPisCst() {
        return fiscalPisCst;
    }

    public void setFiscalPisCst(String fiscalPisCst) {
        this.fiscalPisCst = fiscalPisCst;
    }

    public String getFiscalCofinsCst() {
        return fiscalCofinsCst;
    }

    public void setFiscalCofinsCst(String fiscalCofinsCst) {
        this.fiscalCofinsCst = fiscalCofinsCst;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
