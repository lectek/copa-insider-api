package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.DoacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoacaoJPARepository extends JpaRepository<DoacaoEntity, Long> {
    Optional<DoacaoEntity> findByStripeSessionId(String stripeSessionId);
}
