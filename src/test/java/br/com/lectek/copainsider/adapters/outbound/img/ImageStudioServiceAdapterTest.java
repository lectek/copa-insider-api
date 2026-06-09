package br.com.lectek.copainsider.adapters.outbound.img;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.lectek.copainsider.adapters.outbound.ai.openai.OpenAiImagesClient;
import br.com.lectek.copainsider.application.config.AppAiOpenAiProperties;
import br.com.lectek.copainsider.application.port.inbound.ImageStudioUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageStudioServiceAdapterTest {

    @Test
    void shouldUseOpenAiWhenAutoProviderHasKey() throws Exception {
        AppAiOpenAiProperties properties = new AppAiOpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiImagesClient openAiImagesClient = mock(OpenAiImagesClient.class);
        when(openAiImagesClient.generate(anyString()))
                .thenReturn(new ImageStudioUseCase.GeneratedImageResult(
                        "data:image/png;base64,abc",
                        "image/png",
                        "openai",
                        "Foto fiel do produto em fundo branco.",
                        "prompt revisado"
                ));

        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "auto",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                240,
                properties,
                null,
                openAiImagesClient
        );

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                "Foto fiel do produto em fundo branco.",
                Map.of("codigo", "7896000000001"),
                null,
                true,
                true
        );

        ImageStudioUseCase.GeneratedImageResult result = adapter.generateSync(request);

        assertThat(result.provider()).isEqualTo("openai");
        assertThat(result.reference()).startsWith("data:image/png;base64,");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(openAiImagesClient).generate(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("Foto fiel do produto em fundo branco.")
                .contains("Codigo: 7896000000001.")
                .contains("Reproduza o formato fisico real do produto")
                .contains("sem texto extra fora da embalagem oficial");
    }

    @Test
    void shouldCapGeneratedPollinationsUrlToConfiguredLimit() {
        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "pollinations",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                240,
                new AppAiOpenAiProperties(),
                null,
                null
        );

        String veryLongDescription = "Dipirona sodica 500mg com acao analgesica e antitermica para alivio da dor e febre "
                + "uso adulto e pediatrico sob orientacao profissional em embalagem promocional com detalhes extensos "
                + "sobre composicao, modo de usar, restricoes e informacoes de bula repetidas para simular cadastro legado";

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                null,
                Map.of(
                        "descricao", veryLongDescription,
                        "categoria", "Medicacoes",
                        "codigo", "7896000000001"
                ),
                null,
                true,
                true
        );

        String url = adapter.generateSync(request).reference();

        assertThat(url).startsWith("https://image.pollinations.ai/prompt/");
        assertThat(url).contains("model=flux");
        assertThat(url.length()).isLessThanOrEqualTo(240);
    }

    @Test
    void shouldSanitizePathSeparatorsInPromptFields() {
        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "pollinations",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                512,
                new AppAiOpenAiProperties(),
                null,
                null
        );

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                null,
                Map.of(
                        "descricao", "COLETOR CRISTAL TAMPA CRISTALC/PA",
                        "categoria", "MEDICACOES",
                        "codigo", "7896000000001"
                ),
                null,
                true,
                true
        );

        String url = adapter.generateSync(request).reference();

        assertThat(url).startsWith("https://image.pollinations.ai/prompt/");
        assertThat(url).doesNotContain("%2F");
        assertThat(url).contains("CRISTALC%20PA");
    }

    @Test
    void shouldRejectAutoProviderWhenOpenAiIsNotConfigured() {
        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "auto",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                512,
                new AppAiOpenAiProperties(),
                null,
                null
        );

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                "Foto fiel do produto em fundo branco.",
                Map.of("codigo", "7896000000001"),
                null,
                true,
                true
        );

        assertThatThrownBy(() -> adapter.generateSync(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI nao configurado");
    }
}
