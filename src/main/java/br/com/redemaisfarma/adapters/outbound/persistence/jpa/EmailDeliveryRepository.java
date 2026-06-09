package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailDeliveryRepository extends JpaRepository<EmailDelivery, Long> {

    // Deixe o Pageable limitar/ordenar
    List<EmailDelivery> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
