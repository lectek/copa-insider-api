package br.com.lectek.copainsider.application.copa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Unofficial ESPN scoreboard + summary API — no key needed.
 * Scoreboard: site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard
 * Summary:    site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/summary?event={id}
 */
@Component
public class EspnScoreClient {

    private static final Logger log = LoggerFactory.getLogger(EspnScoreClient.class);
    private static final String SCOREBOARD =
            "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard";
    private static final String SUMMARY =
            "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/summary?event=";

    private final RestTemplate rest;
    private final ObjectMapper  mapper;

    public EspnScoreClient(RestTemplateBuilder builder, ObjectMapper mapper) {
        this.rest   = builder.build();
        this.mapper = mapper;
    }

    // ── Scoreboard (lista de jogos do dia) ──────────────────────────────────

    public List<ESEvent> fetchToday() {
        try {
            String body = rest.getForObject(SCOREBOARD, String.class);
            if (body == null) return List.of();
            ESResponse resp = mapper.readValue(body, ESResponse.class);
            return resp != null && resp.events() != null ? resp.events() : List.of();
        } catch (Exception e) {
            log.warn("ESPN scoreboard falhou: {}", e.getMessage());
            return List.of();
        }
    }

    // ── Summary (detalhes de um evento específico) ───────────────────────────

    public ESSummary fetchSummary(String espnEventId) {
        try {
            String body = rest.getForObject(SUMMARY + espnEventId, String.class);
            if (body == null) return null;
            return mapper.readValue(body, ESSummary.class);
        } catch (Exception e) {
            log.warn("ESPN summary {} falhou: {}", espnEventId, e.getMessage());
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DTOs — Scoreboard
    // ══════════════════════════════════════════════════════════════════════════

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESResponse(List<ESEvent> events) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESEvent(String id, List<ESCompetition> competitions) {
        public ESCompetition comp() {
            return competitions != null && !competitions.isEmpty() ? competitions.get(0) : null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESCompetition(
            List<ESCompetitor> competitors,
            ESStatus status,
            ESSituation situation,
            List<ESDetail> details
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESCompetitor(String homeAway, String score, ESTeam team) {
        public boolean isHome() { return "home".equalsIgnoreCase(homeAway); }
        public int scoreInt()   { try { return Integer.parseInt(score); } catch (Exception e) { return 0; } }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESTeam(String id, String displayName, String abbreviation) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStatus(ESStatusType type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStatusType(String state, String name, String displayClock, Integer period) {
        public boolean isLive()     { return "in".equalsIgnoreCase(state); }
        public boolean isFinished() { return "post".equalsIgnoreCase(state); }
        public String minuto() {
            if (displayClock != null && !displayClock.isBlank()) return displayClock;
            return period != null ? (period == 1 ? "1ª Parte" : "2ª Parte") : "";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESSituation(String displayClock, Integer period) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESDetail(
            ESDetailType type,
            ESClock clock,
            ESTeam team,
            List<ESAthlete> athletesInvolved,
            Boolean scoringPlay,
            Boolean yellowCard,
            Boolean redCard,
            Boolean ownGoal,
            @JsonProperty("penaltyKick") Boolean penalty
    ) {
        public boolean isGolo()          { return Boolean.TRUE.equals(scoringPlay); }
        public boolean isCartaoAmarelo() { return Boolean.TRUE.equals(yellowCard) && !Boolean.TRUE.equals(redCard); }
        public boolean isCartaoVermelho(){ return Boolean.TRUE.equals(redCard); }
        public boolean isOwnGoal()       { return Boolean.TRUE.equals(ownGoal); }
        public boolean isPenalty()       { return Boolean.TRUE.equals(penalty); }
        public String minuto()           { return clock != null ? clock.displayValue() : ""; }
        public String jogadorNome() {
            if (athletesInvolved == null || athletesInvolved.isEmpty()) return "";
            return athletesInvolved.get(0).displayName();
        }
        public String tipoIcone() {
            if (!isGolo()) {
                if (isCartaoVermelho()) return "🟥";
                if (isCartaoAmarelo()) return "🟨";
                return "▶";
            }
            if (isOwnGoal())  return "⚽🔴";
            if (isPenalty())  return "⚽(P)";
            return "⚽";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESDetailType(String id, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESClock(String displayValue) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESAthlete(String displayName, String id) {}

    // ══════════════════════════════════════════════════════════════════════════
    // DTOs — Summary
    // ══════════════════════════════════════════════════════════════════════════

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESSummary(ESBoxscore boxscore, ESHeader header) {
        public ESHeaderCompetition headerComp() {
            if (header == null || header.competitions() == null || header.competitions().isEmpty()) return null;
            return header.competitions().get(0);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESBoxscore(List<ESBoxTeam> teams) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESBoxTeam(String homeAway, ESTeam team, List<ESStat> statistics) {
        public boolean isHome() { return "home".equalsIgnoreCase(homeAway); }
        public String stat(String name) {
            if (statistics == null) return "0";
            return statistics.stream()
                    .filter(s -> name.equals(s.name()))
                    .map(ESStat::displayValue)
                    .findFirst().orElse("0");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStat(String name, String displayValue, String label) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESHeader(List<ESHeaderCompetition> competitions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESHeaderCompetition(
            ESStatus status,
            ESSituation situation,
            List<ESDetail> details,
            List<ESCompetitor> competitors
    ) {}

    // ══════════════════════════════════════════════════════════════════════════
    // Leaders (artilheiros / assistentes) — sem chave de API
    // ══════════════════════════════════════════════════════════════════════════

    private static final String STATS_URL =
            "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/statistics";

    public record LeaderStats(String nome, String selecao, String fotoUrl,
                              int gols, int assists, int jogos) {}

    public record LeadersResult(List<LeaderStats> gols, List<LeaderStats> assists) {
        public boolean isEmpty() { return gols.isEmpty() && assists.isEmpty(); }
    }

    public LeadersResult fetchLeaders() {
        try {
            String body = rest.getForObject(STATS_URL, String.class);
            if (body == null) return new LeadersResult(List.of(), List.of());
            ESLeadersResponse resp = mapper.readValue(body, ESLeadersResponse.class);
            List<LeaderStats> goalLeaders  = List.of();
            List<LeaderStats> assistLeaders = List.of();
            if (resp.stats() != null) {
                for (ESStatCat cat : resp.stats()) {
                    if (cat.leaders() == null) continue;
                    List<LeaderStats> parsed = cat.leaders().stream()
                            .map(this::parseLeaderEntry).filter(Objects::nonNull).toList();
                    if ("Goals".equalsIgnoreCase(cat.displayName()))   goalLeaders   = parsed;
                    else if ("Assists".equalsIgnoreCase(cat.displayName())) assistLeaders = parsed;
                }
            }
            return new LeadersResult(goalLeaders, assistLeaders);
        } catch (Exception e) {
            log.warn("ESPN leaders falhou: {}", e.getMessage());
            return new LeadersResult(List.of(), List.of());
        }
    }

    private LeaderStats parseLeaderEntry(ESLeaderEntry entry) {
        try {
            if (entry.athlete() == null) return null;
            String nome    = entry.athlete().displayName();
            String selecao = entry.athlete().team() != null ? entry.athlete().team().displayName() : "";
            String foto    = entry.athlete().headshot() != null ? entry.athlete().headshot().href() : "";
            String sv      = entry.shortDisplayValue() != null ? entry.shortDisplayValue() : "";
            return new LeaderStats(nome, selecao, foto,
                    parseStatVal(sv, "G"), parseStatVal(sv, "A"), parseStatVal(sv, "M"));
        } catch (Exception e) { return null; }
    }

    private static int parseStatVal(String sv, String key) {
        int idx = sv.indexOf(key + ":");
        if (idx < 0) return 0;
        StringBuilder num = new StringBuilder();
        for (char c : sv.substring(idx + key.length() + 1).trim().toCharArray()) {
            if (Character.isDigit(c)) num.append(c);
            else if (!num.isEmpty()) break;
        }
        try { return num.isEmpty() ? 0 : Integer.parseInt(num.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    // DTOs — statistics leaders endpoint

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESLeadersResponse(List<ESStatCat> stats) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStatCat(String displayName, List<ESLeaderEntry> leaders) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESLeaderEntry(String displayValue, String shortDisplayValue,
                                double value, ESLeaderAthlete athlete) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESLeaderAthlete(String displayName, ESHeadshot headshot, ESLeaderTeam team) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESHeadshot(String href) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESLeaderTeam(String displayName, String abbreviation) {}
}
