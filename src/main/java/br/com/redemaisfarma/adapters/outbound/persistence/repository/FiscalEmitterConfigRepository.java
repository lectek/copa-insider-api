package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.redemaisfarma.domain.fiscal.FiscalProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalEmitterConfigRepository
        extends JpaRepository<FiscalEmitterConfigEntity, Long> {

    Optional<FiscalEmitterConfigEntity> findByProvider(FiscalProvider provider);
}
