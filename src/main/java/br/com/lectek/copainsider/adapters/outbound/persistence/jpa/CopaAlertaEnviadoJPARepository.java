package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAlertaEnviadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CopaAlertaEnviadoJPARepository extends JpaRepository<CopaAlertaEnviadoEntity, Long> {
    boolean existsByPartidaIdAndEmail(Long partidaId, String email);
}
