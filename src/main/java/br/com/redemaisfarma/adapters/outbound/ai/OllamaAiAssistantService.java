package br.com.redemaisfarma.adapters.outbound.ai;

import br.com.redemaisfarma.adapters.outbound.ai.ollama.OllamaChatClient;
import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OllamaAiAssistantService implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(OllamaAiAssistantService.class);
    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final Pattern CEP_PATTERN = Pattern.compile("\\b(\\d{5})-?(\\d{3})\\b");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "\\b(rua|r\\.|avenida|av\\.?|travessa|alameda|praca|rodovia|bairro|numero|n\\.?|casa|apto|apartamento)\\b"
    );
    private static final Pattern GREETING_PATTERN = Pattern.compile(
            "\\b(oi|ola|bom dia|boa tarde|boa noite)\\b"
    );
    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("#?\\d{4,}");

    private final AppAiOllamaProperties props;
    private final OllamaChatClient client;
    private final Map<String, Deque<OllamaChatClient.ChatMessage>> historyBySession = new ConcurrentHashMap<>();
    private final Map<String, ConversationIntent> intentBySession = new ConcurrentHashMap<>();

    public OllamaAiAssistantService(
            final AppAiOllamaProperties props,
            final OllamaChatClient client
    ) {
        this.props = props;
        this.client = client;
    }

    @Override
    public String answer(final String sessionId, final String message) {
        final String safeSessionId = normalizeSessionId(sessionId);
        final String safeMessage = normalizeMessage(message);
        if (safeMessage.isBlank()) {
            return openingReply();
        }

        remember(safeSessionId, new OllamaChatClient.ChatMessage("user", safeMessage));
        final ConversationIntent intent = resolveIntent(safeSessionId, safeMessage);

        if (props.isEnabled()) {
            try {
                String reply = client.chat(safeSessionId, snapshot(safeSessionId));
                if (needsReplyRepair(intent, safeMessage, reply)) {
                    reply = fallback(safeMessage, intent);
                }
                remember(safeSessionId, new OllamaChatClient.ChatMessage("assistant", reply));
                intentBySession.put(safeSessionId, intent);
                return reply;
            } catch (Exception ex) {
                log.warn("Ollama falhou: {}", ex.toString());
            }
        }

        final String fallbackReply = fallback(safeMessage, intent);
        remember(safeSessionId, new OllamaChatClient.ChatMessage("assistant", fallbackReply));
        intentBySession.put(safeSessionId, intent);
        return fallbackReply;
    }

    private String fallback(
            final String message,
            final ConversationIntent intent
    ) {
        final String normalized = message.toLowerCase(Locale.ROOT);
        if (isNameQuestion(normalized)) {
            return "Eu sou Alysson, o atendente virtual da RedeMaisFarma. Fico por aqui para te ajudar com o que voce precisar.";
        }
        if (isGreetingOnly(normalized)) {
            return "Oi! Que bom falar com voce. Eu sou Alysson e posso te ajudar com entrega, pedidos, produtos e pagamento. Me conta o que voce precisa.";
        }

        return switch (intent) {
            case DELIVERY -> deliveryReply(message, normalized);
            case ORDER -> orderReply(normalized);
            case PRODUCT -> "Claro. Me diga o nome do produto ou o codigo de barras e eu sigo com a busca para voce.";
            case PAYMENT -> "Claro. Posso te ajudar com pagamento. Me diga se voce quer saber sobre PIX, cartao, checkout ou confirmacao do pedido.";
            case NONE -> "Claro. Fico feliz em ajudar. Se quiser, posso te orientar sobre entrega, pedido, produto ou pagamento.";
        };
    }

    private ConversationIntent resolveIntent(final String sessionId, final String message) {
        final String normalized = message.toLowerCase(Locale.ROOT);
        final ConversationIntent current = intentBySession.getOrDefault(sessionId, ConversationIntent.NONE);
        if (mentionsDelivery(normalized)
                || looksLikeCep(message)
                || looksLikeAddress(normalized)
                || asksToSendAddress(normalized)) {
            return ConversationIntent.DELIVERY;
        }
        if (mentionsOrder(normalized)
                || looksLikeOrderNumber(normalized)
                || current == ConversationIntent.ORDER && looksLikeIdentifier(normalized)) {
            return ConversationIntent.ORDER;
        }
        if (mentionsPayment(normalized)) {
            return ConversationIntent.PAYMENT;
        }
        if (mentionsProduct(normalized)) {
            return ConversationIntent.PRODUCT;
        }
        return current;
    }

    private boolean needsReplyRepair(
            final ConversationIntent intent,
            final String message,
            final String reply
    ) {
        final String safeReply = normalizeMessage(reply).toLowerCase(Locale.ROOT);
        if (safeReply.isBlank()) {
            return true;
        }
        if (intent == ConversationIntent.DELIVERY
                && (looksLikeCep(message)
                || looksLikeAddress(message.toLowerCase(Locale.ROOT))
                || asksToSendAddress(message.toLowerCase(Locale.ROOT)))) {
            return safeReply.contains("o que voce precisa")
                    || safeReply.contains("eu sou alysson")
                    || safeReply.contains("como posso ajudar")
                    || safeReply.contains("posso buscar produtos");
        }
        return false;
    }

    private String deliveryReply(final String rawMessage, final String normalized) {
        if (asksToSendAddress(normalized)) {
            return "Pode sim. Me envie rua, numero, bairro e cidade. Se tiver CEP, pode mandar tambem, porque a estimativa fica mais precisa.";
        }

        final String cep = extractCep(rawMessage);
        if (cep != null) {
            return "Perfeito, recebi o CEP " + cep + ". Ja consigo seguir com ele como referencia. Se quiser uma estimativa mais precisa, pode me mandar tambem rua e numero.";
        }

        if (looksLikeAddress(normalized)) {
            return "Perfeito, recebi o endereco. Se voce tiver o CEP, pode mandar tambem para eu deixar a estimativa mais precisa.";
        }

        if (mentionsDelivery(normalized)) {
            return "Claro. Eu posso calcular frete e prazo para voce. Me envie seu CEP ou o endereco com rua, numero, bairro e cidade.";
        }

        return "Me envie seu CEP ou o endereco com rua, numero, bairro e cidade que eu sigo com frete e prazo.";
    }

    private String orderReply(final String normalized) {
        if (looksLikeOrderNumber(normalized)) {
            return "Perfeito, recebi o numero do pedido. Vou usar esse identificador para consultar o status.";
        }
        if (mentionsOrder(normalized)) {
            return "Me informe o numero do pedido, por exemplo #12345, que eu consulto o status para voce.";
        }
        return "Se quiser consultar um pedido, me envie o numero dele que eu sigo com o atendimento.";
    }

    private void remember(
            final String sessionId,
            final OllamaChatClient.ChatMessage message
    ) {
        if (message == null || normalizeMessage(message.content()).isBlank()) {
            return;
        }
        final Deque<OllamaChatClient.ChatMessage> history = historyBySession.computeIfAbsent(
                sessionId,
                ignored -> new ArrayDeque<>()
        );
        synchronized (history) {
            history.addLast(message);
            while (history.size() > MAX_HISTORY_MESSAGES) {
                history.removeFirst();
            }
        }
    }

    private List<OllamaChatClient.ChatMessage> snapshot(final String sessionId) {
        final Deque<OllamaChatClient.ChatMessage> history = historyBySession.get(sessionId);
        if (history == null) {
            return List.of();
        }
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    private static boolean isNameQuestion(final String normalized) {
        return normalized.contains("seu nome")
                || normalized.contains("como voce se chama")
                || normalized.contains("como vc se chama")
                || normalized.contains("quem e voce")
                || normalized.contains("quem e vc");
    }

    private static boolean isGreetingOnly(final String normalized) {
        return GREETING_PATTERN.matcher(normalized).find()
                && !mentionsDelivery(normalized)
                && !mentionsOrder(normalized)
                && !mentionsPayment(normalized)
                && !mentionsProduct(normalized)
                && normalized.split("\\s+").length <= 4;
    }

    private static boolean mentionsDelivery(final String normalized) {
        return normalized.contains("frete")
                || normalized.contains("prazo")
                || normalized.contains("entrega")
                || normalized.contains("cep")
                || normalized.contains("endereco")
                || normalized.contains("rota");
    }

    private static boolean mentionsOrder(final String normalized) {
        return normalized.contains("pedido")
                || normalized.contains("status do pedido")
                || normalized.contains("acompanhar");
    }

    private static boolean mentionsPayment(final String normalized) {
        return normalized.contains("pagamento")
                || normalized.contains("pix")
                || normalized.contains("cartao")
                || normalized.contains("checkout");
    }

    private static boolean mentionsProduct(final String normalized) {
        return normalized.contains("produto")
                || normalized.contains("codigo de barras")
                || normalized.contains("estoque")
                || normalized.contains("disponivel")
                || normalized.contains("tem ")
                || normalized.contains("vitamina")
                || normalized.contains("medicamento");
    }

    private static boolean asksToSendAddress(final String normalized) {
        return normalized.contains("posso enviar")
                && (normalized.contains("rua")
                || normalized.contains("endereco")
                || normalized.contains("numero"));
    }

    private static boolean looksLikeCep(final String rawMessage) {
        return extractCep(rawMessage) != null;
    }

    private static String extractCep(final String rawMessage) {
        if (rawMessage == null) {
            return null;
        }
        final Matcher matcher = CEP_PATTERN.matcher(rawMessage);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + "-" + matcher.group(2);
    }

    private static boolean looksLikeAddress(final String normalized) {
        return ADDRESS_PATTERN.matcher(normalized).find();
    }

    private static boolean looksLikeOrderNumber(final String normalized) {
        return ORDER_NUMBER_PATTERN.matcher(normalized).matches();
    }

    private static boolean looksLikeIdentifier(final String normalized) {
        return normalized.matches("[#a-z0-9\\-]{4,}");
    }

    private static String normalizeSessionId(final String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "alysson-anonymous";
        }
        return sessionId.trim();
    }

    private static String normalizeMessage(final String message) {
        if (message == null) {
            return "";
        }
        return message.replace("\r\n", "\n").trim();
    }

    private static String openingReply() {
        return "Oi! Eu sou Alysson, o atendente virtual da RedeMaisFarma. Estou por aqui para te ajudar com produtos, entrega, pagamento e pedidos.";
    }

    private enum ConversationIntent {
        NONE,
        DELIVERY,
        ORDER,
        PRODUCT,
        PAYMENT
    }
}
