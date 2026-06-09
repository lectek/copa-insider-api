package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTerminalServiceTest {

    private static final String KEY_POS_CONFIG = "pg.pos_config";
    private static final String DESC_POS_CONFIG = "Configuracao da maquineta";

    @Mock
    private AppSettingService settings;

    private PaymentTerminalService service;
    private String storedJson;

    @BeforeEach
    void setUp() {
        service = new PaymentTerminalService(settings, new ObjectMapper());
        storedJson = "";

        when(settings.getOrDefault(eq(KEY_POS_CONFIG), anyString()))
                .thenAnswer(invocation -> {
                    final String defaultValue = invocation.getArgument(1);
                    return storedJson == null || storedJson.isBlank()
                            ? defaultValue
                            : storedJson;
                });

        lenient().when(settings.upsert(eq(KEY_POS_CONFIG), anyString(),
                eq(DESC_POS_CONFIG)))
                .thenAnswer(invocation -> {
                    storedJson = invocation.getArgument(1);
                    return null;
                });
    }

    @Test
    void shouldLoadDefaultConfigWhenSettingIsMissing() {
        final PaymentTerminalService.TerminalConfig config = service.loadConfig();

        assertThat(config.enabled()).isFalse();
        assertThat(config.mode()).isEqualTo("mock");
        assertThat(config.provider()).isEqualTo("custom");
        assertThat(config.timeoutMs()).isEqualTo(10000);
        assertThat(config.secretConfigured()).isFalse();
    }

    @Test
    void shouldRequireEndpointWhenModeIsWebhook() {
        assertThatThrownBy(() -> service.saveConfig(
                new PaymentTerminalService.TerminalConfigInput(
                        true,
                        "webhook",
                        "stone",
                        "",
                        "T1",
                        "M1",
                        10000,
                        null,
                        false
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Endpoint obrigatorio");
    }

    @Test
    void shouldSaveConfigAndKeepSecretConfiguredFlag() {
        final PaymentTerminalService.TerminalConfig saved = service.saveConfig(
                new PaymentTerminalService.TerminalConfigInput(
                        true,
                        "mock",
                        "stone",
                        "",
                        "T1",
                        "M1",
                        9000,
                        "token-abc",
                        false
                )
        );

        assertThat(saved.enabled()).isTrue();
        assertThat(saved.provider()).isEqualTo("stone");
        assertThat(saved.timeoutMs()).isEqualTo(9000);
        assertThat(saved.secretConfigured()).isTrue();
        assertThat(storedJson).contains("token-abc");
    }

    @Test
    void shouldKeepCurrentSecretWhenInputSecretIsNull() {
        service.saveConfig(new PaymentTerminalService.TerminalConfigInput(
                true,
                "mock",
                "stone",
                "",
                "T1",
                "M1",
                10000,
                "token-abc",
                false
        ));

        final PaymentTerminalService.TerminalConfig saved = service.saveConfig(
                new PaymentTerminalService.TerminalConfigInput(
                        true,
                        "mock",
                        "stone",
                        "",
                        "T2",
                        "M2",
                        10000,
                        null,
                        false
                )
        );

        assertThat(saved.secretConfigured()).isTrue();
        assertThat(storedJson).contains("token-abc");
    }

    @Test
    void shouldClearSecretWhenRequested() {
        service.saveConfig(new PaymentTerminalService.TerminalConfigInput(
                true,
                "mock",
                "stone",
                "",
                "T1",
                "M1",
                10000,
                "token-abc",
                false
        ));

        final PaymentTerminalService.TerminalConfig saved = service.saveConfig(
                new PaymentTerminalService.TerminalConfigInput(
                        true,
                        "mock",
                        "stone",
                        "",
                        "T1",
                        "M1",
                        10000,
                        null,
                        true
                )
        );

        assertThat(saved.secretConfigured()).isFalse();
        assertThat(storedJson).doesNotContain("token-abc");
    }

    @Test
    void shouldReturnManualResultWhenIntegrationIsDisabled() {
        final PaymentTerminalService.TerminalPaymentResult result = service.authorize(
                new PaymentTerminalService.TerminalPaymentRequest(
                        BigDecimal.TEN,
                        "CARTAO_CREDITO",
                        "PDV-1",
                        "PDV",
                        Map.of()
                )
        );

        assertThat(result.approved()).isTrue();
        assertThat(result.status()).isEqualTo("manual");
    }

    @Test
    void shouldApproveInMockModeWhenEnabled() {
        service.saveConfig(new PaymentTerminalService.TerminalConfigInput(
                true,
                "mock",
                "stone",
                "",
                "T1",
                "M1",
                10000,
                null,
                false
        ));

        final PaymentTerminalService.TerminalPaymentResult result = service.authorize(
                new PaymentTerminalService.TerminalPaymentRequest(
                        BigDecimal.valueOf(20L),
                        "CARTAO_DEBITO",
                        "PDV-2",
                        "PDV",
                        Map.of("origin", "test")
                )
        );

        assertThat(result.approved()).isTrue();
        assertThat(result.status()).isEqualTo("approved");
        assertThat(result.transactionId()).startsWith("MOCK-");
    }

    @Test
    void shouldReturnErrorWhenWebhookIsUnavailable() {
        service.saveConfig(new PaymentTerminalService.TerminalConfigInput(
                true,
                "webhook",
                "stone",
                "http://127.0.0.1:1/authorize",
                "T1",
                "M1",
                1000,
                null,
                false
        ));

        final PaymentTerminalService.TerminalPaymentResult result = service.authorize(
                new PaymentTerminalService.TerminalPaymentRequest(
                        BigDecimal.valueOf(30L),
                        "CARTAO_CREDITO",
                        "PDV-3",
                        "PDV",
                        Map.of()
                )
        );

        assertThat(result.approved()).isFalse();
        assertThat(result.status()).isEqualTo("error");
        assertThat(result.message()).contains("Falha ao conectar");
    }
}
