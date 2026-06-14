package br.com.lectek.copainsider.application.copa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Unofficial ESPN scoreboard API — no key needed, works from cloud servers.
 * Endpoint: site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard
 */
@Component
public class EspnScoreClient {

    private static final Logger log = LoggerFactory.getLogger(EspnScoreClient.class);
    private static final String URL =
            "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard";

    private final RestTemplate rest;
    private final ObjectMapper  mapper;

    public EspnScoreClient(RestTemplateBuilder builder, ObjectMapper mapper) {
        this.rest   = builder.build();
        this.mapper = mapper;
    }

    public List<ESEvent> fetchToday() {
        try {
            String body = rest.getForObject(URL, String.class);
            if (body == null) return List.of();
            ESResponse resp = mapper.readValue(body, ESResponse.class);
            return resp != null && resp.events() != null ? resp.events() : List.of();
        } catch (Exception e) {
            log.warn("ESPN scoreboard falhou: {}", e.getMessage());
            return List.of();
        }
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

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
            ESStatus status
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESCompetitor(
            String homeAway,
            String score,
            ESTeam team
    ) {
        public boolean isHome() { return "home".equalsIgnoreCase(homeAway); }
        public int scoreInt()   { try { return Integer.parseInt(score); } catch (Exception e) { return 0; } }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESTeam(String displayName) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStatus(ESStatusType type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ESStatusType(String state, String name) {
        public boolean isLive()     { return "in".equalsIgnoreCase(state); }
        public boolean isFinished() { return "post".equalsIgnoreCase(state); }
    }
}
