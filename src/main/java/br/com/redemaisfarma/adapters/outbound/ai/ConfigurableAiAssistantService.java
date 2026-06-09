package br.com.redemaisfarma.adapters.outbound.ai;

import br.com.redemaisfarma.adapters.outbound.ai.openai.OpenAiAiAssistantService;
import br.com.redemaisfarma.application.config.AppAiOpenAiProperties;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.delivery.DeliveryPricingService;
import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import br.com.redemaisfarma.application.view.DeliveryQuoteVM;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ConfigurableAiAssistantService implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableAiAssistantService.class);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Pattern CEP_PATTERN = Pattern.compile("\\b(\\d{5})-?(\\d{3})\\b");
    private static final Pattern ADDRESS_START_PATTERN = Pattern.compile(
            "(?iu)\\b(rua|r\\.|avenida|av\\.?|travessa|alameda|rodovia|estrada|praca)\\b"
    );
    private static final String SETTING_ROUTE_SPEED = "entrega.rota.velocidade_media_kmh";
    private static final int DEFAULT_ROUTE_SPEED_KMH = 28;

    private final String provider;
    private final AppAiOpenAiProperties openAiProperties;
    private final OpenAiAiAssistantService openAiAssistantService;
    private final OllamaAiAssistantService ollamaAiAssistantService;
    private final DeliveryPricingService deliveryPricingService;
    private final AppSettingService appSettingService;
    private final Map<String, AssistantSessionState> sessionStates = new ConcurrentHashMap<>();

    public ConfigurableAiAssistantService(
            @Value("${app.ai.assistant.provider:auto}") final String provider,
            final AppAiOpenAiProperties openAiProperties,
            final OpenAiAiAssistantService openAiAssistantService,
            final OllamaAiAssistantService ollamaAiAssistantService,
            final DeliveryPricingService deliveryPricingService,
            final AppSettingService appSettingService
    ) {
        this.provider = provider == null ? "auto" : provider.trim().toLowerCase();
        this.openAiProperties = openAiProperties;
        this.openAiAssistantService = openAiAssistantService;
        this.ollamaAiAssistantService = ollamaAiAssistantService;
        this.deliveryPricingService = deliveryPricingService;
        this.appSettingService = appSettingService;
    }

    @Override
    public String answer(final String sessionId, final String message) {
        final String safeSessionId = normalizeSessionId(sessionId);
        final String safeMessage = normalizeMessage(message);
        final String deliveryReply = maybeAnswerDeliveryQuestion(safeSessionId, safeMessage);
        if (deliveryReply != null) {
            return deliveryReply;
        }

        if (shouldUseOpenAi()) {
            try {
                return openAiAssistantService.answer(safeSessionId, safeMessage);
            } catch (IllegalStateException ex) {
                if (shouldFallbackAfterOpenAiFailure(ex)) {
                    log.warn("OpenAI indisponivel no modo auto; usando fallback local: {}", ex.getMessage());
                    return ollamaAiAssistantService.answer(safeSessionId, safeMessage);
                }
                throw ex;
            }
        }
        if (!"ollama".equals(provider)) {
            throw new IllegalStateException(
                    "OpenAI nao configurado para o atendimento. Defina OPENAI_API_KEY."
            );
        }
        return ollamaAiAssistantService.answer(safeSessionId, safeMessage);
    }

    private boolean shouldUseOpenAi() {
        if ("openai".equals(provider)) {
            if (!openAiProperties.isConfigured()) {
                throw new IllegalStateException(
                        "OpenAI nao configurado para o atendimento. Defina OPENAI_API_KEY."
                );
            }
            return true;
        }
        if ("ollama".equals(provider)) {
            return false;
        }
        if (!openAiProperties.isConfigured()) {
            throw new IllegalStateException(
                    "OpenAI nao configurado para o atendimento. Defina OPENAI_API_KEY."
            );
        }
        return true;
    }

    private boolean shouldFallbackAfterOpenAiFailure(final IllegalStateException ex) {
        if (!"auto".equals(provider) || ex == null) {
            return false;
        }
        final String message = normalizeMessage(ex.getMessage()).toLowerCase(Locale.ROOT);
        return message.contains("falha ao consultar openai")
                || message.contains("consulta openai interrompida")
                || message.contains("openai responses api http")
                || message.contains("insufficient_quota")
                || message.contains("rate_limit")
                || message.contains("429");
    }

    private String maybeAnswerDeliveryQuestion(
            final String sessionId,
            final String message
    ) {
        if (message.isBlank()) {
            return null;
        }

        final AssistantSessionState state = sessionStates.computeIfAbsent(
                sessionId,
                ignored -> new AssistantSessionState()
        );
        final String normalized = message.toLowerCase(PT_BR);

        if (asksToSendAddress(normalized)) {
            return "Pode sim. Me mande rua, numero, bairro e cidade. Se tiver CEP, melhor ainda, porque a estimativa fica mais precisa.";
        }

        final boolean deliveryIntent = mentionsDelivery(normalized);
        final String addressCandidate = extractAddressCandidate(message, normalized);
        if (!addressCandidate.isBlank()) {
            final DeliveryQuoteVM quote = deliveryPricingService.quoteForAddress(addressCandidate);
            state.lastDeliveryAddress = quote.referenceAddress().isBlank()
                    ? addressCandidate
                    : quote.referenceAddress();
            state.lastDeliveryQuote = quote;
            return formatDeliveryQuoteReply(
                    quote,
                    state.lastDeliveryAddress,
                    isCepOnly(addressCandidate)
            );
        }

        if (deliveryIntent && state.lastDeliveryQuote != null) {
            return formatDeliveryQuoteReply(
                    state.lastDeliveryQuote,
                    state.lastDeliveryAddress,
                    isCepOnly(state.lastDeliveryAddress)
            );
        }

        if (deliveryIntent) {
            return "Claro. Eu consigo calcular frete e prazo para voce. Me envie seu CEP ou o endereco com rua, numero, bairro e cidade.";
        }

        return null;
    }

    private String formatDeliveryQuoteReply(
            final DeliveryQuoteVM quote,
            final String referenceAddress,
            final boolean cepOnly
    ) {
        final String safeReferenceAddress = normalizeMessage(referenceAddress);
        if (quote == null || !quote.available()) {
            final StringBuilder unavailable = new StringBuilder(
                    "Nao consegui calcular o frete para esse endereco agora."
            );
            if (!safeReferenceAddress.isBlank()) {
                unavailable.append(" Referencia informada: ").append(safeReferenceAddress).append('.');
            }
            if (quote != null && !quote.detail().isBlank()) {
                unavailable.append(' ').append(quote.detail());
            }
            unavailable.append(" Se puder, me envie rua, numero, bairro, cidade e CEP que eu tento de novo.");
            return unavailable.toString().trim();
        }

        final StringBuilder response = new StringBuilder();
        response.append("Perfeito, calculei o frete para ");
        response.append(safeReferenceAddress.isBlank() ? "o endereco informado" : safeReferenceAddress);
        response.append(". ");
        response.append(quote.summary()).append('.');

        final long travelMinutes = estimateTravelMinutes(quote.distanceKm());
        if (travelMinutes > 0) {
            response.append(" Tempo estimado de deslocamento: ");
            response.append(formatTravelMinutes(travelMinutes)).append('.');
        }

        response.append(" Frete prioritario em ");
        response.append(formatCurrency(quote.priorityShippingAmount())).append('.');

        if (!quote.detail().isBlank()) {
            response.append(' ').append(quote.detail());
        }
        if (cepOnly) {
            response.append(" Se quiser uma estimativa ainda mais precisa, pode me mandar tambem rua e numero.");
        }
        return response.toString().trim();
    }

    private long estimateTravelMinutes(final BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }
        final int averageSpeedKmh = Math.max(
                1,
                appSettingService.getInt(SETTING_ROUTE_SPEED, DEFAULT_ROUTE_SPEED_KMH)
        );
        return Math.max(
                1L,
                Math.round(distanceKm.doubleValue() / averageSpeedKmh * 60.0d)
        );
    }

    private String formatTravelMinutes(final long minutes) {
        if (minutes < 60L) {
            return "cerca de " + minutes + " min";
        }
        final long hours = minutes / 60L;
        final long remainingMinutes = minutes % 60L;
        if (remainingMinutes == 0L) {
            return "cerca de " + hours + " h";
        }
        return "cerca de " + hours + " h " + remainingMinutes + " min";
    }

    private String formatCurrency(final BigDecimal value) {
        final BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(PT_BR).format(
                safeValue.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private String extractAddressCandidate(
            final String message,
            final String normalized
    ) {
        final String cep = extractCep(message);
        final Matcher addressMatcher = ADDRESS_START_PATTERN.matcher(message);
        if (addressMatcher.find()) {
            String candidate = message.substring(addressMatcher.start()).trim();
            candidate = candidate.replaceAll("[\\s,.;:!?]+$", "");
            final boolean hasUsefulStructure = candidate.matches("(?iu).*(\\d|,).+");
            if (hasUsefulStructure && !asksToSendAddress(normalized)) {
                return candidate;
            }
        }
        return cep == null ? "" : cep;
    }

    private boolean isCepOnly(final String value) {
        return value != null && CEP_PATTERN.matcher(value.trim()).matches();
    }

    private String extractCep(final String message) {
        if (message == null) {
            return null;
        }
        final Matcher matcher = CEP_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + "-" + matcher.group(2);
    }

    private boolean mentionsDelivery(final String normalized) {
        return normalized.contains("frete")
                || normalized.contains("prazo")
                || normalized.contains("entrega")
                || normalized.contains("cep")
                || normalized.contains("endereco")
                || normalized.contains("endere\u00e7o")
                || normalized.contains("rua")
                || normalized.contains("numero");
    }

    private boolean asksToSendAddress(final String normalized) {
        return normalized.contains("posso enviar")
                && (normalized.contains("rua")
                || normalized.contains("endereco")
                || normalized.contains("endere\u00e7o")
                || normalized.contains("numero"));
    }

    private String normalizeSessionId(final String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "alysson-anonymous";
        }
        return sessionId.trim();
    }

    private String normalizeMessage(final String message) {
        if (message == null) {
            return "";
        }
        return message.replace("\r\n", "\n").trim();
    }

    private static final class AssistantSessionState {
        private String lastDeliveryAddress;
        private DeliveryQuoteVM lastDeliveryQuote;
    }
}
