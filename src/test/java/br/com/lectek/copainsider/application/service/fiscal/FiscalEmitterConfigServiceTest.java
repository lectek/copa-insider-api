package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.FiscalEmitterConfigRepository;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import br.com.lectek.copainsider.domain.fiscal.FiscalTaxRegime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalEmitterConfigServiceTest {

    @Mock
    private FiscalEmitterConfigRepository repository;

    private FiscalEmitterConfigService service;

    @BeforeEach
    void setUp() {
        service = new FiscalEmitterConfigService(repository);
    }

    @Test
    void loadReturnsDefaultConfigWhenProviderHasNoRecord() {
        when(repository.findByProvider(FiscalProvider.FOCUS_NFE))
                .thenReturn(Optional.empty());

        FiscalEmitterConfigService.FiscalEmitterConfig config =
                service.load(FiscalProvider.FOCUS_NFE);

        Assertions.assertThat(config.provider()).isEqualTo(FiscalProvider.FOCUS_NFE);
        Assertions.assertThat(config.enabled()).isFalse();
        Assertions.assertThat(config.environment())
                .isEqualTo(FiscalEnvironment.HOMOLOGACAO);
        Assertions.assertThat(config.apiBaseUrl())
                .isEqualTo("https://homologacao.focusnfe.com.br");
        Assertions.assertThat(config.apiTokenConfigured()).isFalse();
    }

    @Test
    void saveKeepsStoredSecretsWhenNewSecretFieldsAreBlank() {
        FiscalEmitterConfigEntity existing = new FiscalEmitterConfigEntity();
        existing.setProvider(FiscalProvider.FOCUS_NFE);
        existing.setEnabled(true);
        existing.setEnvironment(FiscalEnvironment.HOMOLOGACAO);
        existing.setCompanyLegalName("Rede Mais Farma LTDA");
        existing.setCompanyTradeName("Rede Mais Farma");
        existing.setCnpj("12345678000199");
        existing.setStateRegistration("123456789");
        existing.setTaxRegime(FiscalTaxRegime.SIMPLES_NACIONAL);
        existing.setApiToken("token-existente");
        existing.setCscId("000001");
        existing.setCsc("csc-existente");
        existing.setCertificateBase64("certificado");
        existing.setNfeSeries(1);
        existing.setNfceSeries(1);

        when(repository.findByProvider(FiscalProvider.FOCUS_NFE))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(FiscalEmitterConfigEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FiscalEmitterConfigService.FiscalEmitterConfig config = service.save(
                FiscalProvider.FOCUS_NFE,
                new FiscalEmitterConfigService.FiscalEmitterConfigInput(
                        true,
                        FiscalEnvironment.PRODUCAO,
                        "Rede Mais Farma LTDA",
                        "Rede Mais Farma",
                        "12.345.678/0001-99",
                        "123456789",
                        "",
                        FiscalTaxRegime.SIMPLES_NACIONAL,
                        "",
                        "https://copainsider.com/webhooks/fiscal",
                        3,
                        5,
                        "",
                        false,
                        "000001",
                        "",
                        false,
                        "",
                        "",
                        false
                )
        );

        Assertions.assertThat(existing.getApiToken()).isEqualTo("token-existente");
        Assertions.assertThat(existing.getCsc()).isEqualTo("csc-existente");
        Assertions.assertThat(existing.getCertificateBase64()).isEqualTo("certificado");
        Assertions.assertThat(existing.getApiBaseUrl())
                .isEqualTo("https://api.focusnfe.com.br");
        Assertions.assertThat(config.environment()).isEqualTo(FiscalEnvironment.PRODUCAO);
        Assertions.assertThat(config.nfeSeries()).isEqualTo(3);
        Assertions.assertThat(config.nfceSeries()).isEqualTo(5);
        Assertions.assertThat(config.apiTokenConfigured()).isTrue();
        Assertions.assertThat(config.cscConfigured()).isTrue();
        verify(repository).save(existing);
    }
}
