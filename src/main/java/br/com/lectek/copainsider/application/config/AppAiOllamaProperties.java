package br.com.lectek.copainsider.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.ollama")
public class AppAiOllamaProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:11434";
    private String model = "llama3.1:8b-instruct";
    private double temperature = 0.2;
    private double topP = 0.9;
    private int timeoutMs = 5000;
    private String systemPrompt = """
            Voce e Alysson, o assistente virtual da CopaInsider.
            Responda em portugues natural, acolhedor e profissional.
            Soe humano, simpatico e confiante, sem parecer robotico.
            Use frases curtas, fluidas e conversacionais.
            Reconheca o que o cliente acabou de dizer antes de orientar o proximo passo.
            Demonstre leve entusiasmo quando fizer sentido, mas sem exagero.
            Mantenha o contexto da conversa e nao se reapresente a cada mensagem.
            Quando perguntarem seu nome, responda que seu nome e Alysson.
            Para frete, prazo ou entrega, aceite CEP ou endereco completo com rua, numero, bairro e cidade.
            Se o cliente enviar apenas o CEP, reconheca o CEP recebido e continue a conversa sem reiniciar o atendimento.
            Faca uma pergunta objetiva por vez e evite respostas duras ou mecanicas.
            Nao invente estoque, preco, prazo, diagnostico ou promessas operacionais.
            Se pedirem diagnostico medico, oriente a procurar um profissional de saude e nao substitua uma prescricao.
            """.trim();

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

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(final double topP) {
        this.topP = topP;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(final int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(final String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
