package br.com.lectek.copainsider.adapters.outbound.ai;

import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDataResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClaudeContentClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeContentClient.class);
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";
    private static final int MAX_TOKENS = 8192;

    @Value("${anthropic.api-key:}")
    private String apiKey;

    private final WebClient webClient;

    public ClaudeContentClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String gerarConteudo(SelecaoDataResult dados, String idioma) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[claude] ANTHROPIC_API_KEY não configurada — retornando placeholder");
            return gerarPlaceholder();
        }

        String prompt = construirPrompt(dados, idioma);
        log.info("[claude] gerando conteúdo — selecao={} idioma={} tokens_max={}", dados.getSelecaoCode(), idioma, MAX_TOKENS);

        try {
            ClaudeRequest req = new ClaudeRequest(
                    MODEL,
                    MAX_TOKENS,
                    List.of(new Message("user", prompt))
            );

            ClaudeResponse resp = webClient.post()
                    .uri(CLAUDE_API_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(ClaudeResponse.class)
                    .block();

            if (resp == null || resp.content() == null || resp.content().isEmpty()) {
                log.warn("[claude] resposta vazia para {}", dados.getSelecaoCode());
                return gerarPlaceholder();
            }

            String conteudo = resp.content().get(0).text();
            log.info("[claude] conteúdo gerado — {} caracteres", conteudo.length());
            return conteudo;

        } catch (Exception e) {
            log.error("[claude] erro ao gerar conteúdo para {}: {}", dados.getSelecaoCode(), e.getMessage());
            return gerarPlaceholder();
        }
    }

    private String construirPrompt(SelecaoDataResult dados, String idioma) {
        String legendas = dados.getLendas() != null
                ? dados.getLendas().stream()
                    .map(l -> "- " + l.nome() + " (" + l.posicao() + "): " + l.biografia())
                    .collect(Collectors.joining("\n"))
                : "";

        String jogadores = dados.getJogadoresAtuais() != null
                ? dados.getJogadoresAtuais().stream()
                    .map(j -> "- " + j.nome() + " (" + j.posicao() + ", " + j.clube() + ")")
                    .collect(Collectors.joining("\n"))
                : "";

        return """
                Você é o redator-chefe do Copa Insider, especialista em futebol internacional.
                Crie o conteúdo completo de um ebook profissional sobre a seleção indicada abaixo.

                IDIOMA DE SAÍDA: %s
                SELEÇÃO: %s (%s)
                APELIDO: %s
                TOM EMOCIONAL: %s
                HISTÓRIA/CONTEXTO: %s

                LENDAS DISPONÍVEIS:
                %s

                JOGADORES ATUAIS:
                %s

                CONTEXTO COPA 2026: %s

                Gere o conteúdo estruturado em JSON com exatamente estes campos:
                {
                  "frase_capa": "frase de impacto para a capa (max 12 palavras, arrepia)",
                  "alma_selecao": "texto de 400 palavras sobre a história e significado cultural, tom emotivo e patriótico",
                  "lendas": [
                    {
                      "nome": "nome do jogador",
                      "apelido": "apelido ou alcunha",
                      "bio_narrativa": "biografia de 250 palavras, como homenagem, termina com frase que arrepia",
                      "legado": "frase curta sobre o legado (max 20 palavras)"
                    }
                  ],
                  "partidas_historicas": [
                    {
                      "titulo": "nome do jogo (ex: A Noite que Parou o Mundo)",
                      "adversario": "nome do adversário",
                      "placar": "X-Y",
                      "data": "data/ano",
                      "narrativa": "narrativa dramatizada de 300 palavras como um conto"
                    }
                  ],
                  "historia_copas": "texto de 300 palavras sobre a participação histórica em Copas, com emoção",
                  "guerreiros_hoje": [
                    {
                      "nome": "nome do jogador",
                      "descricao": "perfil inspirador de 150 palavras, por que carrega a camisa com honra"
                    }
                  ],
                  "copa_2026": "texto de 350 palavras sobre as expectativas para 2026, tom de esperança e tensão patriótica",
                  "manifesto_torcedor": "manifesto de 100 palavras em tom de hino, segunda pessoa do plural, termina com grito de guerra"
                }

                REGRAS OBRIGATÓRIAS:
                - Escreve SEMPRE em %s
                - Tom: patriótico, emotivo, como se o leitor fosse fã apaixonado da seleção
                - Usa "nós" e "nossa seleção" — o leitor faz parte da história
                - Cada texto deve ter pelo menos uma frase que arrepia
                - Inclui no mínimo 5 lendas e 3 partidas históricas
                - Retorna APENAS o JSON válido, sem markdown, sem explicações
                """.formatted(
                        idioma, dados.getSelecaoNome(), dados.getSelecaoCode(),
                        dados.getApelido(), dados.getTomEmocional(),
                        dados.getHistoriaFundacao() != null ? dados.getHistoriaFundacao() : "seleção histórica",
                        legendas, jogadores,
                        dados.getContexto2026(),
                        idioma
                );
    }

    private static final String PLACEHOLDER = """
            {
              "frase_capa": "A paixão que une uma nação",
              "alma_selecao": "Conteúdo em geração...",
              "lendas": [],
              "partidas_historicas": [],
              "historia_copas": "Conteúdo em geração...",
              "guerreiros_hoje": [],
              "copa_2026": "Conteúdo em geração...",
              "manifesto_torcedor": "Conteúdo em geração..."
            }
            """;

    private String gerarPlaceholder() {
        return PLACEHOLDER;
    }

    // ── DTOs internos ───────────────────────────────────────────────────────

    public record ClaudeRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            List<Message> messages
    ) {}

    public record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClaudeResponse(
            List<ContentBlock> content,
            String id,
            @JsonProperty("stop_reason") String stopReason
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(String type, String text) {}
}
