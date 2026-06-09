package br.com.lectek.copainsider.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.openai")
public class AppAiOpenAiProperties {

    private boolean enabled = true;
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey = "";
    private String chatModel = "gpt-5-mini";
    private String visionModel = "gpt-5-mini";
    private String imageModel = "gpt-image-1.5";
    private String reasoningEffort = "low";
    private int timeoutMs = 30000;
    private boolean storeResponses = true;
    private String systemPrompt =
            "Voce e Alysson, o assistente virtual da CopaInsider. "
                    + "Responda em portugues claro, objetivo e cordial. "
                    + "Ajude com catalogo, disponibilidade, entrega, pagamento e pedidos. "
                    + "Quando perguntarem seu nome, responda que seu nome e Alysson. "
                    + "Nao invente estoque, preco ou promessas operacionais. "
                    + "Se o cliente pedir diagnostico, prescricao ou orientacao medica, "
                    + "deixe claro que nao substitui um profissional de saude.";
    private String productVisionPrompt =
            "Analise profundamente a imagem enviada e pesquise o produto com base nos "
                    + "identificadores disponiveis antes de escrever o prompt final. "
                    + "Confirme marca, nome do produto, variante, concentracao, volume, "
                    + "sabor, quantidade, formato da embalagem, cores, tipografia e selos "
                    + "oficiais. Escreva um prompt curto em portugues para gerar um "
                    + "packshot fiel do produto em fundo branco puro com sombreamento "
                    + "basico suave de estudio para ecommerce. Nao descreva ambiente, "
                    + "pessoas, maos, balcoes, textos inventados, reflexos exagerados "
                    + "nem marcas d'agua. "
                    + "Retorne somente o prompt final.";
    private String imageQuality = "medium";
    private String imageSize = "1024x1024";
    private String imageBackground = "opaque";
    private String imageOutputFormat = "png";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(final String chatModel) {
        this.chatModel = chatModel;
    }

    public String getVisionModel() {
        return visionModel;
    }

    public void setVisionModel(final String visionModel) {
        this.visionModel = visionModel;
    }

    public String getImageModel() {
        return imageModel;
    }

    public void setImageModel(final String imageModel) {
        this.imageModel = imageModel;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(final String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(final int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isStoreResponses() {
        return storeResponses;
    }

    public void setStoreResponses(final boolean storeResponses) {
        this.storeResponses = storeResponses;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(final String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getProductVisionPrompt() {
        return productVisionPrompt;
    }

    public void setProductVisionPrompt(final String productVisionPrompt) {
        this.productVisionPrompt = productVisionPrompt;
    }

    public String getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(final String imageQuality) {
        this.imageQuality = imageQuality;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(final String imageSize) {
        this.imageSize = imageSize;
    }

    public String getImageBackground() {
        return imageBackground;
    }

    public void setImageBackground(final String imageBackground) {
        this.imageBackground = imageBackground;
    }

    public String getImageOutputFormat() {
        return imageOutputFormat;
    }

    public void setImageOutputFormat(final String imageOutputFormat) {
        this.imageOutputFormat = imageOutputFormat;
    }

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
