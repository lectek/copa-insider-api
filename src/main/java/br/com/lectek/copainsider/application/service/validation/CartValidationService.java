package br.com.lectek.copainsider.application.service.validation;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartValidationService {
    public static final int MAX_QUANTITY_PER_PRODUCT = 10;
    public static final int MAX_DISTINCT_ITEMS = 20;
    public static final BigDecimal MAX_CART_VALUE = new BigDecimal("5000");

    private final ProdutoRepository produtoRepository;

    public CartValidationService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public CartValidationResult validate(List<CartEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return new CartValidationResult(true, "");
        }
        Map<Long, Integer> totals = new HashMap<>();
        for (CartEntry entry : entries) {
            if (entry.produtoId() == null || entry.quantidade() <= 0) {
                continue;
            }
            totals.merge(entry.produtoId(), entry.quantidade(), (existing, next) -> existing + next);
        }
        Map<Long, ProdutoEntity> produtos = loadProdutos(totals.keySet());
        BigDecimal cartTotal = BigDecimal.ZERO;
        int distinct = 0;
        for (Map.Entry<Long, Integer> entry : totals.entrySet()) {
            Integer quantidade = entry.getValue();
            if (quantidade == null || quantidade <= 0) {
                continue;
            }
            Long produtoId = entry.getKey();
            if (produtoId == null) {
                continue;
            }
            ProdutoEntity produto = produtos.get(produtoId);
            if (produto == null) {
                return new CartValidationResult(false, "Produto nao encontrado.");
            }
            if (quantidade > MAX_QUANTITY_PER_PRODUCT) {
                return new CartValidationResult(false, "Limite de " + MAX_QUANTITY_PER_PRODUCT + " unidades por produto.");
            }
            CartIssue issue = validateProduto(produto, quantidade);
            if (issue.invalid) {
                return new CartValidationResult(false, issue.message);
            }
            BigDecimal preco = produto.getPrecoVenda() == null ? BigDecimal.ZERO : produto.getPrecoVenda();
            cartTotal = cartTotal.add(preco.multiply(BigDecimal.valueOf(quantidade)));
            distinct++;
        }
        if (cartTotal.compareTo(MAX_CART_VALUE) > 0) {
            return new CartValidationResult(false, "Valor maximo do carrinho R$ " + MAX_CART_VALUE + " atingido.");
        }
        if (distinct > MAX_DISTINCT_ITEMS) {
            return new CartValidationResult(false, "Limite de " + MAX_DISTINCT_ITEMS + " produtos diferentes atingido.");
        }
        return new CartValidationResult(true, "");
    }

    private Map<Long, ProdutoEntity> loadProdutos(Iterable<Long> ids) {
        if (ids == null) {
            return Map.of();
        }
        List<Long> list = toList(ids);
        if (list.isEmpty()) {
            return Map.of();
        }
        return produtoRepository.findAllByIdIn(list).stream()
                .collect(Collectors.toMap(ProdutoEntity::getId, p -> p));
    }

    private List<Long> toList(Iterable<Long> values) {
        List<Long> out = new ArrayList<>();
        if (values == null) {
            return out;
        }
        for (Long id : values) {
            if (id != null) {
                out.add(id);
            }
        }
        return out;
    }

    private CartIssue validateProduto(ProdutoEntity produto, Integer quantidade) {
        if (!ProductPublicEligibility.isPubliclySellable(produto)) {
            return new CartIssue(true, "Produto indisponivel para venda.");
        }
        Integer estoque = produto.getEstoque();
        if (estoque != null && quantidade != null && quantidade > estoque) {
            return new CartIssue(true, "Quantidade maior que estoque.");
        }
        return new CartIssue(false, "");
    }

    public record CartEntry(Long produtoId, int quantidade) {
    }

    public record CartValidationResult(boolean valid, String message) {
    }

    private static class CartIssue {
        private final boolean invalid;
        private final String message;

        private CartIssue(boolean invalid, String message) {
            this.invalid = invalid;
            this.message = message;
        }
    }
}
