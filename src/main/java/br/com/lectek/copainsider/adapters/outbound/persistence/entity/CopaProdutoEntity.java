package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "copa_produto")
public class CopaProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoCopaProduto tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "nome_pt_br", length = 200)
    private String nomePtBr;

    @Column(name = "nome_pt_pt", length = 200)
    private String nomePtPt;

    @Column(name = "nome_en", length = 200)
    private String nomeEn;

    @Column(name = "desc_pt_br", columnDefinition = "TEXT")
    private String descPtBr;

    @Column(name = "desc_pt_pt", columnDefinition = "TEXT")
    private String descPtPt;

    @Column(name = "desc_en", columnDefinition = "TEXT")
    private String descEn;

    @Column(name = "hotmart_url", length = 500)
    private String hotmartUrl;

    @Column(name = "hotmart_product_id")
    private Long hotmartProductId;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(name = "slug_time1", length = 100)
    private String slugTime1;

    @Column(name = "slug_time2", length = 100)
    private String slugTime2;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private int ordem = 0;

    @Column(name = "preco_eur", precision = 8, scale = 2)
    private BigDecimal precoEur;

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public TipoCopaProduto getTipo() { return tipo; }
    public BigDecimal getPreco() { return preco; }
    public String getNomePtBr() { return nomePtBr; }
    public String getNomePtPt() { return nomePtPt; }
    public String getNomeEn() { return nomeEn; }
    public String getDescPtBr() { return descPtBr; }
    public String getDescPtPt() { return descPtPt; }
    public String getDescEn() { return descEn; }
    public String getHotmartUrl() { return hotmartUrl; }
    public Long getHotmartProductId() { return hotmartProductId; }
    public String getImagemUrl() { return imagemUrl; }
    public String getSlugTime1() { return slugTime1; }
    public String getSlugTime2() { return slugTime2; }
    public boolean isAtivo() { return ativo; }
    public int getOrdem() { return ordem; }

    public void setSlug(String slug) { this.slug = slug; }
    public void setTipo(TipoCopaProduto tipo) { this.tipo = tipo; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public void setNomePtBr(String v) { this.nomePtBr = v; }
    public void setNomePtPt(String v) { this.nomePtPt = v; }
    public void setNomeEn(String v) { this.nomeEn = v; }
    public void setDescPtBr(String v) { this.descPtBr = v; }
    public void setDescPtPt(String v) { this.descPtPt = v; }
    public void setDescEn(String v) { this.descEn = v; }
    public void setHotmartUrl(String v) { this.hotmartUrl = v; }
    public void setHotmartProductId(Long v) { this.hotmartProductId = v; }
    public void setImagemUrl(String v) { this.imagemUrl = v; }
    public void setSlugTime1(String v) { this.slugTime1 = v; }
    public void setSlugTime2(String v) { this.slugTime2 = v; }
    public BigDecimal getPrecoEur() { return precoEur; }
    public void setPrecoEur(BigDecimal v) { this.precoEur = v; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
