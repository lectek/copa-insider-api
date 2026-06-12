package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SalaChatMensagemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalaChatMensagemJPARepository extends JpaRepository<SalaChatMensagemEntity, Long> {
    List<SalaChatMensagemEntity> findTop100ByJogoIdAndAtivoTrueOrderByCriadoEmAsc(Long jogoId);
}
