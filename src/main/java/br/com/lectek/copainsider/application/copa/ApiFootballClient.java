package br.com.lectek.copainsider.application.copa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ApiFootballClient {

    private static final Logger log = LoggerFactory.getLogger(ApiFootballClient.class);
    private static final String BASE = "https://v3.football.api-sports.io";

    private final RestTemplate rest;
    private final ObjectMapper  mapper;
    private final String        apiKey;

    public ApiFootballClient(RestTemplateBuilder builder,
                             ObjectMapper mapper,
                             @Value("${api.football.key:}") String apiKey) {
        this.rest   = builder.build();
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    public boolean configured() {
        return apiKey != null && apiKey.length() > 8;
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    public List<AFFixture> fetchFixtures(int leagueId, int season) {
        String url = BASE + "/fixtures?league=" + leagueId + "&season=" + season;
        AFFixturesResponse resp = get(url, AFFixturesResponse.class);
        return resp != null && resp.response() != null ? resp.response() : List.of();
    }

    // ── Eventos (golos, cartões) ─────────────────────────────────────────────

    public List<AFEvent> fetchEvents(long fixtureId) {
        String url = BASE + "/fixtures/events?fixture=" + fixtureId;
        AFEventsResponse resp = get(url, AFEventsResponse.class);
        return resp != null && resp.response() != null ? resp.response() : List.of();
    }

    // ── Estatísticas ─────────────────────────────────────────────────────────

    public List<AFTeamStats> fetchStatistics(long fixtureId) {
        String url = BASE + "/fixtures/statistics?fixture=" + fixtureId;
        AFStatsResponse resp = get(url, AFStatsResponse.class);
        return resp != null && resp.response() != null ? resp.response() : List.of();
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    private <T> T get(String url, Class<T> type) {
        if (!configured()) return null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apisports-key", apiKey);
            ResponseEntity<String> resp = rest.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (resp.getBody() == null) return null;
            return mapper.readValue(resp.getBody(), type);
        } catch (Exception e) {
            log.warn("API-Football GET {} falhou: {}", url, e.getMessage());
            return null;
        }
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFFixturesResponse(List<AFFixture> response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFFixture(
            AFFixtureInfo fixture,
            AFLeague      league,
            AFTeams       teams,
            AFGoals       goals
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFFixtureInfo(
            long   id,
            String date,
            String status,
            @JsonProperty("venue") AFVenue venue
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFVenue(String name, String city) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFLeague(int id, String name, int season) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFTeams(AFTeamRef home, AFTeamRef away) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFTeamRef(long id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFGoals(Integer home, Integer away) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFEventsResponse(List<AFEvent> response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFEvent(
            AFTime      time,
            AFTeamRef   team,
            AFPlayer    player,
            AFPlayer    assist,
            String      type,
            String      detail
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFTime(Integer elapsed, Integer extra) {
        public String label() {
            if (elapsed == null) return "";
            return extra != null ? elapsed + "+" + extra + "'" : elapsed + "'";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFPlayer(long id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFStatsResponse(List<AFTeamStats> response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFTeamStats(AFTeamRef team, List<AFStat> statistics) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AFStat(String type, Object value) {
        public String valueStr() {
            return value == null ? "-" : value.toString();
        }
    }
}
