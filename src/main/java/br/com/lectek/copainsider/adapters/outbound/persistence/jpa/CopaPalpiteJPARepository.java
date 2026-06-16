package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaPalpiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CopaPalpiteJPARepository extends JpaRepository<CopaPalpiteEntity, Long> {
    Optional<CopaPalpiteEntity> findByEmailIgnoreCaseAndPartidaId(String email, Long partidaId);
}
