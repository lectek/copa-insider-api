package br.com.lectek.copainsider.adapters.outbound.selecao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WikipediaClient {

    private static final Logger log = LoggerFactory.getLogger(WikipediaClient.class);

    // Títulos dos artigos Wikipedia por seleção e idioma
    private static final Map<String, Map<String, String>> ARTIGOS = Map.ofEntries(
            Map.entry("BRA", Map.of(
                    "pt", "Seleção_Brasileira_de_Futebol",
                    "en", "Brazil_national_football_team",
                    "es", "Selección_de_fútbol_de_Brasil",
                    "fr", "Équipe_du_Brésil_de_football"
            )),
            Map.entry("ARG", Map.of(
                    "pt", "Seleção_Argentina_de_Futebol",
                    "en", "Argentina_national_football_team",
                    "es", "Selección_de_fútbol_de_Argentina",
                    "fr", "Équipe_d'Argentine_de_football"
            )),
            Map.entry("FRA", Map.of(
                    "pt", "Seleção_Francesa_de_Futebol",
                    "en", "France_national_football_team",
                    "es", "Selección_de_fútbol_de_Francia",
                    "fr", "Équipe_de_France_de_football"
            )),
            Map.entry("POR", Map.of(
                    "pt", "Seleção_Portuguesa_de_Futebol",
                    "en", "Portugal_national_football_team",
                    "es", "Selección_de_fútbol_de_Portugal",
                    "fr", "Équipe_du_Portugal_de_football"
            )),
            Map.entry("ESP", Map.of(
                    "pt", "Seleção_Espanhola_de_Futebol",
                    "en", "Spain_national_football_team",
                    "es", "Selección_de_fútbol_de_España",
                    "fr", "Équipe_d'Espagne_de_football"
            )),
            Map.entry("ENG", Map.of(
                    "pt", "Seleção_Inglesa_de_Futebol",
                    "en", "England_national_football_team",
                    "es", "Selección_de_fútbol_de_Inglaterra",
                    "fr", "Équipe_d'Angleterre_de_football"
            )),
            Map.entry("ALE", Map.of(
                    "pt", "Seleção_Alemã_de_Futebol",
                    "en", "Germany_national_football_team",
                    "es", "Selección_de_fútbol_de_Alemania",
                    "fr", "Équipe_d'Allemagne_de_football"
            )),
            Map.entry("ITA", Map.of(
                    "pt", "Seleção_Italiana_de_Futebol",
                    "en", "Italy_national_football_team",
                    "es", "Selección_de_fútbol_de_Italia",
                    "fr", "Équipe_d'Italie_de_football"
            ))
    );

    private final WebClient webClient;

    public WikipediaClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                .responseTimeout(Duration.ofSeconds(6))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(6, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(6, TimeUnit.SECONDS)));
        this.webClient = builder.clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public String buscarResumo(String selecaoCode, String idioma) {
        String lang = resolverLang(idioma);
        Map<String, String> artigos = ARTIGOS.get(selecaoCode.toUpperCase());
        if (artigos == null) return "";

        String titulo = artigos.getOrDefault(lang, artigos.get("en"));
        if (titulo == null) return "";

        try {
            String url = "https://" + lang + ".wikipedia.org/api/rest_v1/page/summary/" + titulo;

            WikiSummary summary = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(WikiSummary.class)
                    .block();

            return summary != null && summary.extract() != null ? summary.extract() : "";
        } catch (Exception e) {
            log.warn("[wikipedia] erro ao buscar '{}' ({}): {}", titulo, lang, e.getMessage());
            return "";
        }
    }

    public String buscarResumoJogador(String nomeJogador, String idioma) {
        String lang = resolverLang(idioma);
        String titulo = nomeJogador.replace(" ", "_");
        try {
            String url = "https://" + lang + ".wikipedia.org/api/rest_v1/page/summary/" + titulo;
            WikiSummary summary = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(WikiSummary.class)
                    .block();

            return summary != null && summary.extract() != null ? summary.extract() : "";
        } catch (Exception e) {
            log.debug("[wikipedia] sem artigo para '{}' ({})", nomeJogador, lang);
            return "";
        }
    }

    private String resolverLang(String idioma) {
        if (idioma == null) return "en";
        return switch (idioma.toLowerCase()) {
            case "pt-br", "pt-pt", "pt" -> "pt";
            case "es"                   -> "es";
            case "fr"                   -> "fr";
            case "de"                   -> "de";
            case "it"                   -> "it";
            default                     -> "en";
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WikiSummary(
            String title,
            String extract,
            @JsonProperty("thumbnail") Thumbnail thumbnail
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(String source) {}
}
