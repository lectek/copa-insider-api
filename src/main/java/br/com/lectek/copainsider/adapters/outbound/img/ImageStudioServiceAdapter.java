package br.com.lectek.copainsider.adapters.outbound.img;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.lectek.copainsider.adapters.outbound.ai.openai.OpenAiImagesClient;
import br.com.lectek.copainsider.adapters.outbound.ai.openai.OpenAiResponsesClient;
import br.com.lectek.copainsider.application.config.AppAiOpenAiProperties;
import br.com.lectek.copainsider.application.port.inbound.ImageStudioUseCase;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageStudioServiceAdapter implements ImageStudioUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImageStudioServiceAdapter.class);
    private static final String PROVIDER_AUTO = "auto";
    private static final String PROVIDER_OPENAI = "openai";
    private static final String PROVIDER_POLLINATIONS = "pollinations";
    private static final String PROVIDER_STUB = "stub";
    private static final String DEFAULT_STUB_BASE_URL = "http://localhost:8080/assets/autogen";
    private static final int DEFAULT_MIN_PROMPT_CHARS = 32;
    private static final int DEFAULT_MAX_PROMPT_FIELD_CHARS = 140;

    private final String provider;
    private final String pollinationsBaseUrl;
    private final String pollinationsModel;
    private final int pollinationsWidth;
    private final int pollinationsHeight;
    private final boolean pollinationsNoLogo;
    private final boolean pollinationsPrivate;
    private final int pollinationsMaxUrlLength;
    private final AppAiOpenAiProperties openAiProperties;
    private final OpenAiResponsesClient openAiResponsesClient;
    private final OpenAiImagesClient openAiImagesClient;

    public ImageStudioServiceAdapter(
            @Value("${app.ai.image.provider:auto}") final String provider,
            @Value("${app.ai.image.pollinations.base-url:https://image.pollinations.ai/prompt}") final String pollinationsBaseUrl,
            @Value("${app.ai.image.pollinations.model:flux}") final String pollinationsModel,
            @Value("${app.ai.image.pollinations.width:1024}") final int pollinationsWidth,
            @Value("${app.ai.image.pollinations.height:1024}") final int pollinationsHeight,
            @Value("${app.ai.image.pollinations.nologo:true}") final boolean pollinationsNoLogo,
            @Value("${app.ai.image.pollinations.private:false}") final boolean pollinationsPrivate,
            @Value("${app.ai.image.pollinations.max-url-length:240}") final int pollinationsMaxUrlLength,
            final AppAiOpenAiProperties openAiProperties,
            final OpenAiResponsesClient openAiResponsesClient,
            final OpenAiImagesClient openAiImagesClient
    ) {
        this.provider = provider == null ? PROVIDER_AUTO : provider.trim().toLowerCase();
        this.pollinationsBaseUrl = blankToDefault(
                pollinationsBaseUrl,
                "https://image.pollinations.ai/prompt"
        );
        this.pollinationsModel = blankToDefault(pollinationsModel, "flux");
        this.pollinationsWidth = Math.clamp(pollinationsWidth, 256, 2048);
        this.pollinationsHeight = Math.clamp(pollinationsHeight, 256, 2048);
        this.pollinationsNoLogo = pollinationsNoLogo;
        this.pollinationsPrivate = pollinationsPrivate;
        this.pollinationsMaxUrlLength = Math.clamp(pollinationsMaxUrlLength, 160, 2048);
        this.openAiProperties = openAiProperties;
        this.openAiResponsesClient = openAiResponsesClient;
        this.openAiImagesClient = openAiImagesClient;
    }

    @Override
    public GeneratedImageResult generateSync(final ImageGenRequestDTO request) {
        final String resolvedProvider = resolveProvider();
        if (PROVIDER_STUB.equals(resolvedProvider)) {
            return stubResult(request);
        }
        if (PROVIDER_POLLINATIONS.equals(resolvedProvider)) {
            return pollinationsResult(request);
        }
        return openAiResult(request);
    }

    private GeneratedImageResult stubResult(final ImageGenRequestDTO request) {
        final String key = request.vars() != null
                ? String.valueOf(request.vars().getOrDefault("codigo", "produto"))
                : "produto";
        return new GeneratedImageResult(
                DEFAULT_STUB_BASE_URL + "/" + request.preset() + "-" + key + ".png",
                "image/png",
                PROVIDER_STUB,
                buildPrompt(request),
                null
        );
    }

    private GeneratedImageResult openAiResult(final ImageGenRequestDTO request) {
        final String sourcePrompt = buildPrompt(request);
        String generationPrompt = sourcePrompt;
        if (request.inputImageUrl() != null && !request.inputImageUrl().isBlank()) {
            try {
                final String describedPrompt = openAiResponsesClient.describeProductPrompt(request);
                if (describedPrompt != null && !describedPrompt.isBlank()) {
                    generationPrompt = describedPrompt;
                }
            } catch (InterruptedException ex) {
                log.warn("Falha ao analisar imagem base na OpenAI. Fallback para prompt textual: {}", ex.getMessage());
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                log.warn("Falha ao analisar imagem base na OpenAI. Fallback para prompt textual: {}", ex.getMessage());
            } catch (RuntimeException ex) {
                log.warn("Falha ao derivar prompt de imagem na OpenAI: {}", ex.getMessage());
            }
        }
        try {
            final GeneratedImageResult generated = openAiImagesClient.generate(generationPrompt);
            return new GeneratedImageResult(
                    generated.reference(),
                    generated.mimeType(),
                    generated.provider(),
                    sourcePrompt,
                    generated.revisedPrompt()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao gerar imagem com OpenAI: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Geracao de imagem OpenAI interrompida.", ex);
        }
    }

    private GeneratedImageResult pollinationsResult(final ImageGenRequestDTO request) {
        final String prompt = buildPrompt(request);
        final String url = pollinationsUrl(request, prompt);
        return new GeneratedImageResult(
                url,
                "image/png",
                PROVIDER_POLLINATIONS,
                prompt,
                null
        );
    }

    private String resolveProvider() {
        if (PROVIDER_STUB.equals(provider)) {
            return PROVIDER_STUB;
        }
        if (PROVIDER_POLLINATIONS.equals(provider)) {
            return PROVIDER_POLLINATIONS;
        }
        if (PROVIDER_OPENAI.equals(provider)
                || PROVIDER_AUTO.equals(provider)
                || provider.isBlank()) {
            if (openAiProperties != null && openAiProperties.isConfigured()) {
                return PROVIDER_OPENAI;
            }
            throw new IllegalStateException(
                    "OpenAI nao configurado para gerar imagens. Defina OPENAI_API_KEY."
            );
        }
        if (openAiProperties != null && openAiProperties.isConfigured()) {
            return PROVIDER_OPENAI;
        }
        throw new IllegalStateException("Provider de imagem invalido: " + provider);
    }

    private String pollinationsUrl(final ImageGenRequestDTO request, final String prompt) {
        String url = buildPollinationsUrl(prompt);
        if (url.length() <= this.pollinationsMaxUrlLength) {
            return url;
        }

        final String compactPrompt = buildCompactPrompt(request);
        return shrinkToFit(compactPrompt);
    }

    private String buildPollinationsUrl(final String prompt) {
        final String encodedPrompt = urlEncode(prompt);
        final String model = urlEncode(this.pollinationsModel);
        final StringBuilder url = new StringBuilder(this.pollinationsBaseUrl.replaceAll("/+$", ""));
        url.append('/').append(encodedPrompt)
                .append("?model=").append(model)
                .append("&width=").append(this.pollinationsWidth)
                .append("&height=").append(this.pollinationsHeight);
        if (this.pollinationsNoLogo) {
            url.append("&nologo=true");
        }
        if (this.pollinationsPrivate) {
            url.append("&private=true");
        }
        return url.toString();
    }

    private String shrinkToFit(final String prompt) {
        String workingPrompt = normalizePrompt(prompt);
        String url = buildPollinationsUrl(workingPrompt);

        while (url.length() > this.pollinationsMaxUrlLength
                && workingPrompt.length() > DEFAULT_MIN_PROMPT_CHARS) {
            final int nextLength = Math.max(
                    DEFAULT_MIN_PROMPT_CHARS,
                    workingPrompt.length() - 12
            );
            workingPrompt = workingPrompt.substring(0, nextLength).trim();

            final int lastSpace = workingPrompt.lastIndexOf(' ');
            if (lastSpace >= DEFAULT_MIN_PROMPT_CHARS) {
                workingPrompt = workingPrompt.substring(0, lastSpace).trim();
            }
            url = buildPollinationsUrl(workingPrompt);
        }

        return url;
    }

    private static String buildPrompt(final ImageGenRequestDTO request) {
        final String basePrompt = blankToDefault(
                request.prompt(),
                "Analise profundamente a referencia do produto e gere um packshot fiel para ecommerce em fundo branco puro com sombreamento basico suave de estudio."
        );

        final StringBuilder builder = new StringBuilder(normalizePrompt(basePrompt));
        appendField(builder, "Produto", clip(request.varText("nome", ""), 64));
        appendField(
                builder,
                "Descricao",
                clip(request.varText("descricao", ""), DEFAULT_MAX_PROMPT_FIELD_CHARS)
        );
        appendField(builder, "Categoria", clip(request.varText("categoria", ""), 48));
        appendField(builder, "Fabricante", clip(request.varText("fabricante", ""), 48));
        appendField(builder, "Codigo", clip(request.varText("codigo", ""), 32));
        appendField(builder, "Tipo fisico", clip(request.varText("tipoFisico", ""), DEFAULT_MAX_PROMPT_FIELD_CHARS));
        builder.append(" Antes de gerar, confirme cuidadosamente marca, variante, concentracao, volume, sabor, quantidade e formato da embalagem.");
        builder.append(" Preserve rotulo, tipografia, cores, proporcoes, material e elementos oficiais do item.");
        builder.append(" Reproduza o formato fisico real do produto e nao troque o tipo de objeto por outro parecido.");
        builder.append(" Mostrar somente o produto em fundo branco puro, com sombreamento basico suave logo abaixo e atras do produto, iluminacao de estudio limpa e sem reflexos exagerados.");
        builder.append(" Sem pessoas, sem maos, sem objetos extras, sem mockup 3D, sem cenario, sem texto extra fora da embalagem oficial e sem marca dagua.");
        return normalizePrompt(builder.toString());
    }

    private static String buildCompactPrompt(final ImageGenRequestDTO request) {
        final StringBuilder builder = new StringBuilder(
                "Packshot fiel de produto para ecommerce, fundo branco puro e sombreamento basico suave."
        );
        appendField(builder, "Nome", clip(request.varText("nome", ""), 48));
        appendField(builder, "Descricao", clip(request.varText("descricao", ""), 60));
        appendField(builder, "Categoria", clip(request.varText("categoria", ""), 32));
        appendField(builder, "Fabricante", clip(request.varText("fabricante", ""), 32));
        appendField(builder, "Codigo", clip(request.varText("codigo", ""), 20));
        appendField(builder, "Tipo fisico", clip(request.varText("tipoFisico", ""), 72));
        builder.append(" Preserve embalagem oficial e o formato real do item. Sem pessoas e sem textos extras fora da embalagem.");
        return normalizePrompt(builder.toString());
    }

    private static void appendField(
            final StringBuilder builder,
            final String label,
            final String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        builder.append(' ').append(label).append(": ").append(value.trim()).append('.');
    }

    private static String blankToDefault(final String value, final String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static String clip(final String value, final int maxChars) {
        if (value == null) {
            return "";
        }
        final String normalized = normalizePrompt(value);
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars)).trim();
    }

    private static String normalizePrompt(final String value) {
        if (value == null) {
            return "";
        }
        final String sanitized = value
                .replace('/', ' ')
                .replace('\\', ' ')
                .replace('|', ' ');
        return sanitized.replaceAll("\\s+", " ").trim();
    }

    private static String urlEncode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
