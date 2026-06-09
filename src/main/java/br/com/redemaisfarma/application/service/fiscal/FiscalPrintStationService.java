package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.FiscalPrintStationRepository;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintStationRole;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalPrintStationService {

    private static final SecureRandom RNG = new SecureRandom();

    private final FiscalPrintStationRepository repository;
    private final PasswordEncoder passwordEncoder;

    public FiscalPrintStationService(
            final FiscalPrintStationRepository fiscalPrintStationRepository,
            final PasswordEncoder passwordEncoderValue
    ) {
        this.repository = fiscalPrintStationRepository;
        this.passwordEncoder = passwordEncoderValue;
    }

    @Transactional(readOnly = true)
    public List<StationSummary> list() {
        return repository.findAllByOrderByActiveDescCodeAsc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public FiscalPrintStationEntity save(final StationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Estacao de impressao obrigatoria.");
        }
        final String code = normalizeCode(input.code());
        if (code == null) {
            throw new IllegalArgumentException(
                    "Informe o codigo da estacao de impressao."
            );
        }
        final String displayName = blankToNull(input.displayName());
        if (displayName == null) {
            throw new IllegalArgumentException(
                    "Informe o nome da estacao de impressao."
            );
        }

        final FiscalPrintStationEntity entity = resolveEntity(input.id(), code);
        entity.setCode(code);
        entity.setDisplayName(displayName);
        entity.setPrinterName(blankToNull(input.printerName()));
        entity.setRole(
                input.role() == null
                        ? FiscalPrintStationRole.FLEX
                        : input.role()
        );
        entity.setActive(input.active());
        entity.setNotes(blankToNull(input.notes()));
        return repository.save(entity);
    }

    @Transactional
    public FiscalPrintStationEntity updateActive(
            final Long stationId,
            final boolean active
    ) {
        final FiscalPrintStationEntity entity = repository.findById(stationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Estacao de impressao nao encontrada."
                ));
        entity.setActive(active);
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<FiscalPrintStationEntity> findBestStationEntity(
            final FiscalPrintChannel printChannel
    ) {
        if (printChannel == null || printChannel == FiscalPrintChannel.NONE) {
            return Optional.empty();
        }
        return repository.findByActiveTrueOrderByCodeAsc()
                .stream()
                .filter(station -> supports(station.getRole(), printChannel))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<FiscalPrintStationEntity> findEntity(final Long stationId) {
        if (stationId == null) {
            return Optional.empty();
        }
        return repository.findById(stationId);
    }

    @Transactional(readOnly = true)
    public Optional<FiscalPrintStationEntity> findActiveEntityByCode(
            final String code
    ) {
        final String normalized = normalizeCode(code);
        if (normalized == null) {
            return Optional.empty();
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(normalized);
    }

    @Transactional
    public FiscalPrintStationEntity recordHeartbeat(final String code) {
        final FiscalPrintStationEntity entity = findActiveEntityByCode(code)
                .orElseThrow(() -> new NoSuchElementException(
                        "Estacao de impressao ativa nao encontrada."
                ));
        entity.setLastHeartbeatAt(LocalDateTime.now());
        return repository.save(entity);
    }

    @Transactional
    public GeneratedStationCredential rotateApiKey(final Long stationId) {
        final FiscalPrintStationEntity entity = repository.findById(stationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Estacao de impressao nao encontrada."
                ));
        final String rawApiKey = generateApiKey();
        final LocalDateTime rotatedAt = LocalDateTime.now();
        entity.setApiKeyHash(passwordEncoder.encode(rawApiKey));
        entity.setApiKeyLastRotatedAt(rotatedAt);
        repository.save(entity);
        return new GeneratedStationCredential(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                rawApiKey,
                rotatedAt
        );
    }

    @Transactional(readOnly = true)
    public Optional<FiscalPrintStationEntity> authenticateAgent(
            final String code,
            final String apiKey
    ) {
        final String normalizedCode = normalizeCode(code);
        final String normalizedApiKey = blankToNull(apiKey);
        if (normalizedCode == null || normalizedApiKey == null) {
            return Optional.empty();
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(normalizedCode)
                .filter(this::hasConfiguredApiKey)
                .filter(station -> passwordEncoder.matches(
                        normalizedApiKey,
                        station.getApiKeyHash()
                ));
    }

    private FiscalPrintStationEntity resolveEntity(
            final Long id,
            final String code
    ) {
        if (id != null) {
            return repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Estacao de impressao nao encontrada."
                    ));
        }
        return repository.findByCodeIgnoreCase(code)
                .orElseGet(FiscalPrintStationEntity::new);
    }

    private boolean supports(
            final FiscalPrintStationRole role,
            final FiscalPrintChannel channel
    ) {
        if (role == null || role == FiscalPrintStationRole.FLEX) {
            return true;
        }
        if (channel == FiscalPrintChannel.IMMEDIATE) {
            return role == FiscalPrintStationRole.IMMEDIATE_ONLY;
        }
        return role == FiscalPrintStationRole.DELIVERY_ONLY;
    }

    private String normalizeCode(final String value) {
        final String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9_-]", "");
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private StationSummary toSummary(final FiscalPrintStationEntity entity) {
        return new StationSummary(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getPrinterName(),
                entity.getRole(),
                entity.isActive(),
                entity.getLastHeartbeatAt(),
                entity.getNotes(),
                hasConfiguredApiKey(entity),
                entity.getApiKeyLastRotatedAt()
        );
    }

    private boolean hasConfiguredApiKey(final FiscalPrintStationEntity entity) {
        return entity != null
                && entity.getApiKeyHash() != null
                && !entity.getApiKeyHash().isBlank();
    }

    private String generateApiKey() {
        final byte[] bytes = new byte[24];
        RNG.nextBytes(bytes);
        return "pst_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public record StationInput(
            Long id,
            String code,
            String displayName,
            String printerName,
            FiscalPrintStationRole role,
            boolean active,
            String notes
    ) {
    }

    public record StationSummary(
            Long id,
            String code,
            String displayName,
            String printerName,
            FiscalPrintStationRole role,
            boolean active,
            java.time.LocalDateTime lastHeartbeatAt,
            String notes,
            boolean apiKeyConfigured,
            java.time.LocalDateTime apiKeyLastRotatedAt
    ) {
    }

    public record GeneratedStationCredential(
            Long stationId,
            String code,
            String displayName,
            String apiKey,
            LocalDateTime rotatedAt
    ) {
    }
}
