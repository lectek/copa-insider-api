package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SalaVotoJogadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface SalaVotoJogadorJPARepository extends JpaRepository<SalaVotoJogadorEntity, Long> {

    Optional<SalaVotoJogadorEntity> findByJogoIdAndSessionIdAndJogadorSlug(
            Long jogoId, String sessionId, String jogadorSlug);

    @Query("SELECT v.jogadorSlug, AVG(v.nota), COUNT(v) FROM SalaVotoJogadorEntity v " +
           "WHERE v.jogoId = :jogoId GROUP BY v.jogadorSlug")
    List<Object[]> calcularMediasPorJogo(Long jogoId);

    @Query("SELECT AVG(v.nota), COUNT(v) FROM SalaVotoJogadorEntity v " +
           "WHERE v.jogoId = :jogoId AND v.jogadorSlug = :slug")
    Object[] calcularMediaJogador(Long jogoId, String slug);
}
