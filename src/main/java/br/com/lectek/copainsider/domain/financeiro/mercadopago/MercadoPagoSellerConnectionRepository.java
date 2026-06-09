package br.com.lectek.copainsider.domain.financeiro.mercadopago;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MercadoPagoSellerConnectionRepository
        extends JpaRepository<MercadoPagoSellerConnection, Long> {

    Optional<MercadoPagoSellerConnection> findByOwnerReferenceIgnoreCase(
            String ownerReference
    );

    Optional<MercadoPagoSellerConnection> findBySellerUserId(
            String sellerUserId
    );

    List<MercadoPagoSellerConnection> findAllByOrderByUpdatedAtDesc();
}
