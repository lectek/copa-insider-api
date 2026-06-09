/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public record ProdutoCardDTO(Long id, String nome, String imagem, String imagemWebp, String codigoBarras, @JsonProperty(value="preco") BigDecimal precoAtual, BigDecimal precoPromocional, Boolean disponivel, Integer estoque, String categoria) {
    private static final String PLACEHOLDER_IMAGE = "/img/produtos/placeholder-generico.png";

    public static ProdutoCardDTO from(ProdutoEntity p) {
        Objects.requireNonNull(p, "ProdutoEntity nulo");
        BigDecimal promo = ProdutoCardDTO.normalize(p.getPrecoPromocional());
        BigDecimal venda = ProdutoCardDTO.normalize(p.getPrecoVenda());
        BigDecimal exibido = ProdutoCardDTO.choosePrecoExibido(promo, venda);
        return new ProdutoCardDTO(p.getId(), ProdutoCardDTO.nvl(p.getNome(), "Produto"), ProdutoCardDTO.resolveImage(p.getImagem()), ProdutoCardDTO.nvlEmpty(p.getImagemWebp()), ProdutoCardDTO.nvlEmpty(p.getCodigoBarras()), exibido, ProdutoCardDTO.hasPositive(promo) ? promo : null, p.getDisponivel(), p.getEstoque(), ProdutoCardDTO.nvlEmpty(p.getCategoria()));
    }

    public static ProdutoCardDTO from(View v) {
        Objects.requireNonNull(v, "View nula");
        BigDecimal promo = ProdutoCardDTO.normalize(v.precoPromocional());
        BigDecimal venda = ProdutoCardDTO.normalize(v.precoVenda());
        BigDecimal exibido = ProdutoCardDTO.choosePrecoExibido(promo, venda);
        return new ProdutoCardDTO(v.id(), ProdutoCardDTO.nvl(v.nome(), "Produto"), ProdutoCardDTO.resolveImage(v.imagem()), ProdutoCardDTO.nvlEmpty(v.imagemWebp()), ProdutoCardDTO.nvlEmpty(v.codigoBarras()), exibido, ProdutoCardDTO.hasPositive(promo) ? promo : null, v.disponivel(), v.estoque(), ProdutoCardDTO.nvlEmpty(v.categoria()));
    }

    public static List<ProdutoCardDTO> mapToDtoList(List<?> itens) {
        if (itens == null || itens.isEmpty()) {
            return List.of();
        }
        return itens.stream().map(o -> {
            if (o instanceof ProdutoCardDTO) {
                ProdutoCardDTO dto = (ProdutoCardDTO)o;
                return dto;
            }
            if (o instanceof ProdutoEntity) {
                ProdutoEntity e = (ProdutoEntity)o;
                return ProdutoCardDTO.from(e);
            }
            if (o instanceof View) {
                View v = (View)o;
                return ProdutoCardDTO.from(v);
            }
            throw new IllegalArgumentException("Tipo n\u00e3o suportado em mapToDtoList: " + String.valueOf(o.getClass()));
        }).toList();
    }

    private static BigDecimal normalize(BigDecimal v) {
        if (v == null) {
            return null;
        }
        BigDecimal n = v.setScale(2, RoundingMode.HALF_UP);
        return n.signum() == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : n;
    }

    private static boolean hasPositive(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    private static BigDecimal choosePrecoExibido(BigDecimal promo, BigDecimal venda) {
        if (ProdutoCardDTO.hasPositive(promo)) {
            return promo;
        }
        if (venda != null) {
            return venda;
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static String nvlEmpty(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    private static String resolveImage(String raw) {
        if (raw == null || raw.isBlank()) {
            return PLACEHOLDER_IMAGE;
        }
        String r = raw.trim();
        if (r.startsWith("http://") || r.startsWith("https://") || r.startsWith("//")) {
            return r;
        }
        if (r.startsWith("/")) {
            return r;
        }
        if (r.matches("^(media|images|img|assets)/.*")) {
            return "/" + r;
        }
        r = r.replaceFirst("^[/\\\\]+", "");
        return "/media/products/" + r;
    }

    public static interface View {
        public Long id();

        public String nome();

        public String imagem();

        public String imagemWebp();

        public String codigoBarras();

        public BigDecimal precoVenda();

        public BigDecimal precoPromocional();

        public Boolean disponivel();

        public Integer estoque();

        public String categoria();
    }
}
