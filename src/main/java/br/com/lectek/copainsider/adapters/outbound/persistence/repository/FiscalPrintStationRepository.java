package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalPrintStationRepository
        extends JpaRepository<FiscalPrintStationEntity, Long> {

    Optional<FiscalPrintStationEntity> findByCodeIgnoreCase(String code);

    Optional<FiscalPrintStationEntity> findByCodeIgnoreCaseAndActiveTrue(
            String code
    );

    List<FiscalPrintStationEntity> findAllByOrderByActiveDescCodeAsc();

    List<FiscalPrintStationEntity> findByActiveTrueOrderByCodeAsc();
}
