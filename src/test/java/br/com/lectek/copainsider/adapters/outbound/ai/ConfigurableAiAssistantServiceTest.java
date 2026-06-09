package br.com.lectek.copainsider.adapters.outbound.ai;

import br.com.lectek.copainsider.adapters.outbound.ai.openai.OpenAiAiAssistantService;
import br.com.lectek.copainsider.application.config.AppAiOpenAiProperties;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.delivery.DeliveryPricingService;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConfigurableAiAssistantServiceTest {

    @Test
    void shouldUseRealDeliveryQuoteWhenCustomerSendsCep() {
        final AppAiOpenAiProperties openAiProperties = new AppAiOpenAiProperties();
        final OpenAiAiAssistantService openAiAssistantService = mock(OpenAiAiAssistantService.class);
        final OllamaAiAssistantService ollamaAiAssistantService = mock(OllamaAiAssistantService.class);
        final DeliveryPricingService deliveryPricingService = mock(DeliveryPricingService.class);
        final AppSettingService appSettingService = mock(AppSettingService.class);
        when(appSettingService.getInt("entrega.rota.velocidade_media_kmh", 28)).thenReturn(28);
        when(deliveryPricingService.quoteForAddress("58058-320")).thenReturn(
                DeliveryQuoteVM.available(
                        "58058-320",
                        new BigDecimal("2.80"),
                        new BigDecimal("5.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("2.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("20.00"),
                        new BigDecimal("20.00"),
                        "Entrega gratis para este endereco",
                        "Ate 5.00 km a entrega e gratis; depois cobramos R$ 2,00 por km excedente."
                )
        );

        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "auto",
                openAiProperties,
                openAiAssistantService,
                ollamaAiAssistantService,
                deliveryPricingService,
                appSettingService
        );

        final String first = service.answer("sess-frete", "quero calcular o frete");
        final String second = service.answer("sess-frete", "58058320");

        assertThat(first).contains("Me envie seu CEP ou o endereco");
        assertThat(second).contains("calculei o frete para 58058-320");
        assertThat(second).contains("Entrega gratis para este endereco");
        assertThat(second).contains("Frete prioritario em R$");
        verify(deliveryPricingService).quoteForAddress("58058-320");
        verifyNoInteractions(openAiAssistantService, ollamaAiAssistantService);
    }

    @Test
    void shouldAnswerAddressQuestionWithoutCallingProvider() {
        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "auto",
                new AppAiOpenAiProperties(),
                mock(OpenAiAiAssistantService.class),
                mock(OllamaAiAssistantService.class),
                mock(DeliveryPricingService.class),
                mock(AppSettingService.class)
        );

        final String answer = service.answer("sess-endereco", "posso enviar a rua e numero?");

        assertThat(answer).contains("Pode sim").contains("rua, numero, bairro e cidade");
    }

    @Test
    void shouldDelegateToOllamaWhenMessageIsNotAboutDelivery() {
        final AppAiOpenAiProperties openAiProperties = new AppAiOpenAiProperties();
        final OpenAiAiAssistantService openAiAssistantService = mock(OpenAiAiAssistantService.class);
        final OllamaAiAssistantService ollamaAiAssistantService = mock(OllamaAiAssistantService.class);
        final DeliveryPricingService deliveryPricingService = mock(DeliveryPricingService.class);
        final AppSettingService appSettingService = mock(AppSettingService.class);
        when(ollamaAiAssistantService.answer("sess-generic", "me ajuda com produtos"))
                .thenReturn("Resposta do Ollama.");

        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "ollama",
                openAiProperties,
                openAiAssistantService,
                ollamaAiAssistantService,
                deliveryPricingService,
                appSettingService
        );

        final String answer = service.answer("sess-generic", "me ajuda com produtos");

        assertThat(answer).isEqualTo("Resposta do Ollama.");
        verify(ollamaAiAssistantService).answer("sess-generic", "me ajuda com produtos");
        verifyNoInteractions(openAiAssistantService, deliveryPricingService);
    }

    @Test
    void shouldDelegateToOpenAiWhenConfiguredAndMessageIsGeneric() {
        final AppAiOpenAiProperties openAiProperties = new AppAiOpenAiProperties();
        openAiProperties.setApiKey("sk-test");
        final OpenAiAiAssistantService openAiAssistantService = mock(OpenAiAiAssistantService.class);
        final OllamaAiAssistantService ollamaAiAssistantService = mock(OllamaAiAssistantService.class);
        when(openAiAssistantService.answer("sess-openai", "qual o seu nome?"))
                .thenReturn("Meu nome e Alysson.");

        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "auto",
                openAiProperties,
                openAiAssistantService,
                ollamaAiAssistantService,
                mock(DeliveryPricingService.class),
                mock(AppSettingService.class)
        );

        final String answer = service.answer("sess-openai", "qual o seu nome?");

        assertThat(answer).isEqualTo("Meu nome e Alysson.");
        verify(openAiAssistantService).answer("sess-openai", "qual o seu nome?");
        verifyNoInteractions(ollamaAiAssistantService);
    }

    @Test
    void shouldFallbackToOllamaWhenOpenAiQuotaIsExceededInAutoMode() {
        final AppAiOpenAiProperties openAiProperties = new AppAiOpenAiProperties();
        openAiProperties.setApiKey("sk-test");
        final OpenAiAiAssistantService openAiAssistantService = mock(OpenAiAiAssistantService.class);
        final OllamaAiAssistantService ollamaAiAssistantService = mock(OllamaAiAssistantService.class);
        when(openAiAssistantService.answer("sess-cota", "quais produtos voce recomenda?"))
                .thenThrow(new IllegalStateException(
                        "Falha ao consultar OpenAI: OpenAI Responses API HTTP 429: "
                                + "{\"error\":{\"code\":\"insufficient_quota\"}}"
                ));
        when(ollamaAiAssistantService.answer("sess-cota", "quais produtos voce recomenda?"))
                .thenReturn("Resposta local de contingencia.");

        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "auto",
                openAiProperties,
                openAiAssistantService,
                ollamaAiAssistantService,
                mock(DeliveryPricingService.class),
                mock(AppSettingService.class)
        );

        final String answer = service.answer("sess-cota", "quais produtos voce recomenda?");

        assertThat(answer).isEqualTo("Resposta local de contingencia.");
        verify(openAiAssistantService).answer("sess-cota", "quais produtos voce recomenda?");
        verify(ollamaAiAssistantService).answer("sess-cota", "quais produtos voce recomenda?");
    }

    @Test
    void shouldKeepOpenAiFailureWhenProviderIsExplicitlyOpenAi() {
        final AppAiOpenAiProperties openAiProperties = new AppAiOpenAiProperties();
        openAiProperties.setApiKey("sk-test");
        final OpenAiAiAssistantService openAiAssistantService = mock(OpenAiAiAssistantService.class);
        final OllamaAiAssistantService ollamaAiAssistantService = mock(OllamaAiAssistantService.class);
        when(openAiAssistantService.answer("sess-openai", "quais produtos voce recomenda?"))
                .thenThrow(new IllegalStateException(
                        "Falha ao consultar OpenAI: OpenAI Responses API HTTP 429: "
                                + "{\"error\":{\"code\":\"insufficient_quota\"}}"
                ));

        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "openai",
                openAiProperties,
                openAiAssistantService,
                ollamaAiAssistantService,
                mock(DeliveryPricingService.class),
                mock(AppSettingService.class)
        );

        assertThatThrownBy(() -> service.answer("sess-openai", "quais produtos voce recomenda?"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTP 429");
        verify(openAiAssistantService).answer("sess-openai", "quais produtos voce recomenda?");
        verifyNoInteractions(ollamaAiAssistantService);
    }

    @Test
    void shouldRejectGenericConversationWhenOpenAiIsNotConfigured() {
        final ConfigurableAiAssistantService service = new ConfigurableAiAssistantService(
                "auto",
                new AppAiOpenAiProperties(),
                mock(OpenAiAiAssistantService.class),
                mock(OllamaAiAssistantService.class),
                mock(DeliveryPricingService.class),
                mock(AppSettingService.class)
        );

        assertThatThrownBy(() -> service.answer("sess-sem-openai", "quais produtos voce recomenda?"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI nao configurado");
    }
}
