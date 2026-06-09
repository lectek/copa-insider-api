package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProductStockSubscriptionRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductStockSubscriptionService {

    private final ProductStockSubscriptionRepository repository;
    private final ProdutoRepository produtoRepository;
    private final ObjectMapper objectMapper;

    public ProductStockSubscriptionService(ProductStockSubscriptionRepository repository,
                                           ProdutoRepository produtoRepository,
                                           ObjectMapper objectMapper) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.objectMapper = objectMapper;
    }

    public ProductStockSubscriptionEntity subscribe(Long produtoId, String email, String nome) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail de notificação é obrigatório.");
        }
        ProdutoEntity produto = produtoRepository.findStockSubscribableById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto indisponivel para assinatura de estoque."));
        String normalizedEmail = email.trim().toLowerCase();
        if (repository.existsByProdutoIdAndRecipientEmailIgnoreCase(produtoId, normalizedEmail)) {
            return repository.findFirstByProdutoIdAndRecipientEmailIgnoreCaseAndStatus(
                            produtoId,
                            normalizedEmail,
                            "SUBSCRIBED"
                    )
                    .orElseGet(() -> repository.findFirstByProdutoIdAndRecipientEmailIgnoreCase(
                                    produtoId,
                                    normalizedEmail
                            )
                            .map(existing -> {
                                existing.setStatus("SUBSCRIBED");
                                existing.setNotifiedAt(null);
                                existing.setProductSnapshot(buildSnapshot(produto));
                                return repository.save(existing);
                            })
                            .orElseThrow(() -> new IllegalStateException(
                                    "Falha ao recuperar inscricao existente para o produto."
                            )));
        }
        ProductStockSubscriptionEntity entity = new ProductStockSubscriptionEntity();
        entity.setProduto(produto);
        entity.setRecipientEmail(normalizedEmail);
        entity.setRecipientName(nome != null ? nome.trim() : null);
        entity.setProductSnapshot(buildSnapshot(produto));
        return repository.save(entity);
    }

    public List<ProductStockSubscriptionEntity> findPending(int limit) {
        Pageable page = PageRequest.of(0, Math.max(1, limit));
        return repository.findByStatusOrderByCreatedAtAsc("SUBSCRIBED", page);
    }

    public void markNotified(ProductStockSubscriptionEntity subscription) {
        subscription.setStatus("NOTIFIED");
        subscription.setNotifiedAt(LocalDateTime.now());
        repository.save(subscription);
    }

    private String buildSnapshot(ProdutoEntity produto) {
        try {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("produtoId", produto.getId());
            snapshot.put("nome", produto.getNome());
            snapshot.put("categoria", produto.getCategoria());
            snapshot.put("preco", produto.getPrecoVenda());
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
