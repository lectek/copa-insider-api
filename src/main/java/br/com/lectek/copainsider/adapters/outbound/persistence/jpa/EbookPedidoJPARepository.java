package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EbookPedidoEntity;
import br.com.lectek.copainsider.domain.enums.EbookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EbookPedidoJPARepository extends JpaRepository<EbookPedidoEntity, Long> {

    boolean existsByTransacao(String transacao);

    Optional<EbookPedidoEntity> findByTransacao(String transacao);

    Optional<EbookPedidoEntity> findByDownloadToken(String downloadToken);

    List<EbookPedidoEntity> findByCompradorEmailOrderByCriadoEmDesc(String compradorEmail);

    List<EbookPedidoEntity> findByCompradorEmailAndStatusOrderByCriadoEmDesc(
            String compradorEmail, EbookStatus status);

    Optional<EbookPedidoEntity> findFirstBySelecaoCodeAndIdiomaAndStatus(
            String selecaoCode, String idioma, EbookStatus status);
}
