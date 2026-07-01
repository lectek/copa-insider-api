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
    // ATENÇÃO: confirma sempre um ID novo com GET /lookupteam.php?id=X antes de o
    // adicionar aqui — os valores antigos desta tabela apontavam todos para clubes
    // ingleses/escoceses (ex: POR=133631 era o Peterborough United), não seleções.
    private static final Map<String, String> TEAM_IDS = Map.ofEntries(
            Map.entry("BRA", "134496"),
            Map.entry("ARG", "134509"),
            Map.entry("FRA", "133913"),
            Map.entry("POR", "133908"),
            Map.entry("ESP", "133909"),
            Map.entry("ALE", "133907"),
            Map.entry("ENG", "133914"),
            Map.entry("ITA", "133910"),
            Map.entry("URU", "134504"),
            Map.entry("NED", "133905"),
            Map.entry("BEL", "134515"),
            Map.entry("CRO", "133912"),
            Map.entry("SUI", "134506"),
            Map.entry("DEN", "133906"),
            Map.entry("MEX", "134497"),
            Map.entry("USA", "134514"),
            Map.entry("CAN", "140073"),
            Map.entry("MAR", "136139")
    );

    // strCountry esperado por seleção (conforme devolvido pela própria TheSportsDB) —
    // usado só para detetar um ID mal mapeado (ex: apontar para um clube em vez da
    // seleção) antes que o nome/dados errados cheguem a um ebook.
    private static final Map<String, String> PAIS_ESPERADO = Map.ofEntries(
            Map.entry("BRA", "Brazil"),
            Map.entry("ARG", "Argentina"),
            Map.entry("FRA", "France"),
            Map.entry("POR", "Portugal"),
            Map.entry("ESP", "Spain"),
            Map.entry("ALE", "Germany"),
            Map.entry("ENG", "England"),
            Map.entry("ITA", "Italy"),
            Map.entry("URU", "Uruguay"),
            Map.entry("NED", "The Netherlands"),
            Map.entry("BEL", "Belgium"),
            Map.entry("CRO", "Croatia"),
            Map.entry("SUI", "Switzerland"),
            Map.entry("DEN", "Denmark"),
            Map.entry("MEX", "Mexico"),
            Map.entry("USA", "United States"),
            Map.entry("CAN", "Canada"),
            Map.entry("MAR", "Morocco")
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

            String paisEsperado = PAIS_ESPERADO.get(selecaoCode.toUpperCase());
            if (paisEsperado != null && t.strCountry() != null && !paisEsperado.equalsIgnoreCase(t.strCountry())) {
                log.error("[thesportsdb] MISMATCH DE SELECAO — selecao={} esperava pais={} mas o teamId={} devolveu equipa={} pais={}. "
                                + "Ignorando dados da API para esta selecao (fallback seguro).",
                        selecaoCode, paisEsperado, teamId, t.strTeam(), t.strCountry());
                return new TeamInfo(selecaoCode, selecaoCode, null, null, null, null, 0, 0);
            }

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
