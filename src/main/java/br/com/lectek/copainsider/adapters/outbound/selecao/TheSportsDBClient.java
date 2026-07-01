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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TheSportsDBClient {

    private static final Logger log = LoggerFactory.getLogger(TheSportsDBClient.class);
    private static final String BASE_URL = "https://www.thesportsdb.com/api/v1/json/3";

    // códigos ISO → ID da equipa nacional no TheSportsDB
    private static final Map<String, String> TEAM_IDS = Map.ofEntries(
            Map.entry("BRA", "133597"),
            Map.entry("ARG", "133599"),
            Map.entry("FRA", "133610"),
            Map.entry("POR", "133631"),
            Map.entry("ESP", "133601"),
            Map.entry("ALE", "133608"),
            Map.entry("ENG", "133604"),
            Map.entry("ITA", "133606"),
            Map.entry("URU", "133638"),
            Map.entry("NED", "133615"),
            Map.entry("BEL", "133602"),
            Map.entry("CRO", "133607"),
            Map.entry("SUI", "133635"),
            Map.entry("DEN", "133609"),
            Map.entry("MEX", "133623"),
            Map.entry("USA", "133641"),
            Map.entry("CAN", "133603"),
            Map.entry("MAR", "133622")
    );

    private final WebClient webClient;

    public TheSportsDBClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                .responseTimeout(Duration.ofSeconds(6))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(6, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(6, TimeUnit.SECONDS)));
        this.webClient = builder.clone().baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public TeamInfo buscarEquipa(String selecaoCode) {
        String teamId = TEAM_IDS.getOrDefault(selecaoCode.toUpperCase(), null);
        if (teamId == null) {
            log.warn("[thesportsdb] sem mapeamento para selecao={}", selecaoCode);
            return new TeamInfo(selecaoCode, selecaoCode, null, null, null, null, 0, 0);
        }
        try {
            TeamResponse resp = webClient.get()
                    .uri("/lookupteam.php?id=" + teamId)
                    .retrieve()
                    .bodyToMono(TeamResponse.class)
                    .block();

            if (resp == null || resp.teams() == null || resp.teams().isEmpty()) {
                log.warn("[thesportsdb] sem dados para teamId={}", teamId);
                return new TeamInfo(selecaoCode, selecaoCode, null, null, null, null, 0, 0);
            }

            Team t = resp.teams().get(0);
            return new TeamInfo(
                    selecaoCode,
                    t.strTeam(),
                    t.strDescriptionPT() != null ? t.strDescriptionPT() : t.strDescriptionEN(),
                    t.strTeamBadge(),
                    t.strTeamJersey(),
                    t.strCountry(),
                    safeParse(t.intFormedYear()),
                    0
            );
        } catch (Exception e) {
            log.error("[thesportsdb] erro ao buscar equipa {}: {}", selecaoCode, e.getMessage());
            return new TeamInfo(selecaoCode, selecaoCode, null, null, null, null, 0, 0);
        }
    }

    public List<Player> buscarJogadores(String selecaoCode) {
        String teamId = TEAM_IDS.getOrDefault(selecaoCode.toUpperCase(), null);
        if (teamId == null) return Collections.emptyList();
        try {
            PlayerResponse resp = webClient.get()
                    .uri("/lookup_all_players.php?id=" + teamId)
                    .retrieve()
                    .bodyToMono(PlayerResponse.class)
                    .block();

            return (resp != null && resp.player() != null) ? resp.player() : Collections.emptyList();
        } catch (Exception e) {
            log.error("[thesportsdb] erro ao buscar jogadores {}: {}", selecaoCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    private int safeParse(String v) {
        try { return v != null ? Integer.parseInt(v) : 0; } catch (Exception e) { return 0; }
    }

    // ── DTOs internos ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamResponse(List<Team> teams) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(
            @JsonProperty("strTeam")           String strTeam,
            @JsonProperty("strDescriptionEN")  String strDescriptionEN,
            @JsonProperty("strDescriptionPT")  String strDescriptionPT,
            @JsonProperty("strTeamBadge")      String strTeamBadge,
            @JsonProperty("strTeamJersey")     String strTeamJersey,
            @JsonProperty("strCountry")        String strCountry,
            @JsonProperty("intFormedYear")     String intFormedYear
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerResponse(List<Player> player) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Player(
            @JsonProperty("strPlayer")         String strPlayer,
            @JsonProperty("strPosition")       String strPosition,
            @JsonProperty("strNationality")    String strNationality,
            @JsonProperty("strDescriptionEN")  String strDescriptionEN,
            @JsonProperty("strDescriptionPT")  String strDescriptionPT,
            @JsonProperty("dateBorn")          String dateBorn,
            @JsonProperty("strThumb")          String strThumb
    ) {}

    public record TeamInfo(
            String code,
            String nome,
            String descricao,
            String logoUrl,
            String jerseyUrl,
            String pais,
            int anoCriacao,
            int titulos
    ) {}
}
