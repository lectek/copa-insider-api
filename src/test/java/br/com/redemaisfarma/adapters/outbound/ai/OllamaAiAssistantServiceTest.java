package br.com.redemaisfarma.adapters.outbound.ai;

import br.com.redemaisfarma.adapters.outbound.ai.ollama.OllamaChatClient;
import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OllamaAiAssistantServiceTest {

    @Test
    void fallbackShouldIntroduceAlyssonWhenNameIsRequested() {
        final AppAiOllamaProperties properties = new AppAiOllamaProperties();
        properties.setEnabled(false);
        final OllamaAiAssistantService service =
                new OllamaAiAssistantService(properties, mock(OllamaChatClient.class));

        final String answer = service.answer("sess-1", "Qual o seu nome?");

        assertThat(answer).contains("Alysson");
    }

    @Test
    void fallbackShouldMentionAlyssonInGenericReply() {
        final AppAiOllamaProperties properties = new AppAiOllamaProperties();
        properties.setEnabled(false);
        final OllamaAiAssistantService service =
                new OllamaAiAssistantService(properties, mock(OllamaChatClient.class));

        final String answer = service.answer("sess-1", "Me ajuda");

        assertThat(answer).contains("entrega, pedido, produto ou pagamento");
    }

    @Test
    void fallbackShouldKeepDeliveryContextForAddressAndCep() {
        final AppAiOllamaProperties properties = new AppAiOllamaProperties();
        properties.setEnabled(false);
        final OllamaAiAssistantService service =
                new OllamaAiAssistantService(properties, mock(OllamaChatClient.class));

        final String first = service.answer("sess-entrega", "quero calcular o frete");
        final String second = service.answer("sess-entrega", "posso enviar a rua e numero?");
        final String third = service.answer("sess-entrega", "58058320");

        assertThat(first).contains("CEP").contains("endereco");
        assertThat(second).contains("Pode sim").contains("rua, numero, bairro e cidade");
        assertThat(third).contains("58058-320");
    }

    @Test
    void shouldSendConversationHistoryToOllamaWhenSessionContinues() throws Exception {
        final AppAiOllamaProperties properties = new AppAiOllamaProperties();
        properties.setEnabled(true);
        final OllamaChatClient client = mock(OllamaChatClient.class);
        when(client.chat(eq("sess-1"), anyList()))
                .thenReturn("Me envie seu CEP ou endereco completo.")
                .thenReturn("Recebi o CEP 58058-320.");

        final OllamaAiAssistantService service = new OllamaAiAssistantService(properties, client);

        service.answer("sess-1", "quero calcular o frete");
        service.answer("sess-1", "58058320");

        verify(client, times(2)).chat(eq("sess-1"), anyList());
        verify(client).chat(eq("sess-1"), argThat(messages ->
                messages instanceof List<?>
                        && messages.size() == 3
                        && "user".equals(((OllamaChatClient.ChatMessage) messages.get(0)).role())
                        && "assistant".equals(((OllamaChatClient.ChatMessage) messages.get(1)).role())
                        && "user".equals(((OllamaChatClient.ChatMessage) messages.get(2)).role())
                        && "quero calcular o frete".equals(((OllamaChatClient.ChatMessage) messages.get(0)).content())
                        && "Me envie seu CEP ou endereco completo."
                        .equals(((OllamaChatClient.ChatMessage) messages.get(1)).content())
                        && "58058320".equals(((OllamaChatClient.ChatMessage) messages.get(2)).content())
        ));
    }
}
