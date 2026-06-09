package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.FiscalPrintStationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalPrintStationServiceTest {

    @Mock
    private FiscalPrintStationRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private FiscalPrintStationService service;

    @BeforeEach
    void setUp() {
        service = new FiscalPrintStationService(repository, passwordEncoder);
    }

    @Test
    void rotateApiKeyStoresEncodedValueAndReturnsRawCredential() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setId(4L);
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        station.setActive(true);

        when(repository.findById(4L)).thenReturn(Optional.of(station));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-key");
        when(repository.save(any(FiscalPrintStationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FiscalPrintStationService.GeneratedStationCredential credential =
                service.rotateApiKey(4L);

        final ArgumentCaptor<String> rawCaptor =
                ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(rawCaptor.capture());

        Assertions.assertThat(credential.stationId()).isEqualTo(4L);
        Assertions.assertThat(credential.code()).isEqualTo("CAIXA-1");
        Assertions.assertThat(credential.apiKey()).isEqualTo(rawCaptor.getValue());
        Assertions.assertThat(credential.apiKey()).startsWith("pst_");
        Assertions.assertThat(station.getApiKeyHash()).isEqualTo("encoded-key");
        Assertions.assertThat(station.getApiKeyLastRotatedAt())
                .isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void authenticateAgentReturnsActiveStationWhenApiKeyMatches() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        station.setActive(true);
        station.setApiKeyHash("encoded-key");

        when(repository.findByCodeIgnoreCaseAndActiveTrue("CAIXA-1"))
                .thenReturn(Optional.of(station));
        when(passwordEncoder.matches(eq("pst_token"), eq("encoded-key")))
                .thenReturn(true);

        final Optional<FiscalPrintStationEntity> authenticated =
                service.authenticateAgent("caixa-1", "pst_token");

        Assertions.assertThat(authenticated).contains(station);
    }

    @Test
    void authenticateAgentRejectsStationWithoutConfiguredApiKey() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setCode("CAIXA-1");
        station.setActive(true);

        when(repository.findByCodeIgnoreCaseAndActiveTrue("CAIXA-1"))
                .thenReturn(Optional.of(station));

        final Optional<FiscalPrintStationEntity> authenticated =
                service.authenticateAgent("CAIXA-1", "pst_token");

        Assertions.assertThat(authenticated).isEmpty();
    }
}
