package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockSubscriptionRepository extends JpaRepository<ProductStockSubscriptionEntity, Long> {
    List<ProductStockSubscriptionEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
    boolean existsByProdutoIdAndRecipientEmailIgnoreCase(Long produtoId, String recipientEmail);
    Optional<ProductStockSubscriptionEntity> findFirstByProdutoIdAndRecipientEmailIgnoreCase(
            Long produtoId,
            String recipientEmail
    );
    Optional<ProductStockSubscriptionEntity> findFirstByProdutoIdAndRecipientEmailIgnoreCaseAndStatus(
            Long produtoId,
            String recipientEmail,
            String status
    );
}
