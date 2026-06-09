package br.com.lectek.copainsider.application.view;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public record ProductCardVM(
        Long id,
        String nome,
        String imagem,
        BigDecimal preco,
        BigDecimal precoDe,
        Integer descontoPercentual,
        boolean disponivel,
        Integer estoque,
        String categoria
) {

    private static final String PLACEHOLDER_IMAGE = "/img/produtos/placeholder-generico.png";

    public static ProductCardVM of(ProdutoEntity p) {
        if (p == null) return null;
        BigDecimal precoAtual = safePrecoAtual(p);
        BigDecimal precoAnterior = safePrecoAnterior(p, precoAtual);
        return new ProductCardVM(
                p.getId(),
                nvl(p.getNome(), "Produto"),
                resolveImage(nvl(p.getImagem(), "")),
                precoAtual,
                precoAnterior,
                safeDescontoPercentual(p, precoAnterior, precoAtual),
                safeDisponivel(p),
                p.getEstoque(),
                nvl(p.getCategoria(), "")
        );
    }

    public static List<ProductCardVM> fromList(List<ProdutoEntity> ps) {
        if (ps == null || ps.isEmpty()) return List.of();
        return ps.stream().map(ProductCardVM::of).collect(Collectors.toList());
    }

    public static List<ProductCardVM> fromListInOrder(List<ProdutoEntity> ps, List<Long> orderIds) {
        if (ps == null || ps.isEmpty() || orderIds == null || orderIds.isEmpty()) {
            return fromList(ps);
        }
        final Map<Long, ProductCardVM> map = new HashMap<>();
        ps.forEach(p -> map.put(p.getId(), of(p)));

        final List<ProductCardVM> out = new ArrayList<>();
        for (Long id : orderIds) {
            final ProductCardVM vm = map.get(id);
            if (vm != null) out.add(vm);
        }
        return out;
    }

    public static ProductPageVM fromPage(Page<ProdutoEntity> page) {
        return new ProductPageVM(
                fromList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static BigDecimal safePrecoAtual(ProdutoEntity p) {
        BigDecimal promocional = normalize(p.getPrecoPromocional());
        if (isPositive(promocional)) {
            return promocional;
        }
        BigDecimal venda = normalize(p.getPrecoVenda());
        return venda != null ? venda : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal safePrecoAnterior(ProdutoEntity p, BigDecimal precoAtual) {
        BigDecimal venda = normalize(p.getPrecoVenda());
        if (venda == null || precoAtual == null) {
            return null;
        }
        return venda.compareTo(precoAtual) > 0 ? venda : null;
    }

    private static Integer safeDescontoPercentual(
            ProdutoEntity p,
            BigDecimal precoAnterior,
            BigDecimal precoAtual
    ) {
        Integer desconto = p.getDescontoPercentual();
        if (desconto != null && desconto > 0) {
            return desconto;
        }
        if (!isPositive(precoAnterior) || !isPositive(precoAtual)
                || precoAnterior.compareTo(precoAtual) <= 0) {
            return null;
        }
        BigDecimal diferenca = precoAnterior.subtract(precoAtual);
        return diferenca
                .multiply(BigDecimal.valueOf(100))
                .divide(precoAnterior, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private static boolean safeDisponivel(ProdutoEntity p) {
        return Boolean.TRUE.equals(p.getDisponivel());
    }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String resolveImage(String raw) {
        if (raw == null || raw.isBlank()) return PLACEHOLDER_IMAGE;
        final String r = raw.trim();
        if (r.startsWith("http://") || r.startsWith("https://") || r.startsWith("//")) return r;
        if (r.startsWith("/")) return r;
        if (r.matches("^(media|images|img|assets)/.*")) return "/" + r;
        return "/media/products/" + r.replaceFirst("^[/\\\\]+", "");
    }

    public record ProductPageVM(
            List<ProductCardVM> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) { }
}
