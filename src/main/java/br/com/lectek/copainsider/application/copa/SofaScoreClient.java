package br.com.lectek.copainsider.application.copa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class SofaScoreClient {

    private static final Logger log = LoggerFactory.getLogger(SofaScoreClient.class);
    private static final String BASE = "https://api.sofascore.com/api/v1";

    private final RestTemplate rest;
    private final ObjectMapper  mapper;

    public SofaScoreClient(RestTemplateBuilder builder, ObjectMapper mapper) {
        this.rest   = builder.build();
        this.mapper = mapper;
    }

    // ── Endpoints ────────────────────────────────────────────────────────────

    public List<SSEvent> fetchEventsByDate(String date) {
        SSEventsResponse resp = get(BASE + "/sport/football/scheduled-events/" + date,
                SSEventsResponse.class);
        return resp != null && resp.events() != null ? resp.events() : List.of();
    }

    public List<SSIncident> fetchIncidents(long eventId) {
        SSIncidentsResponse resp = get(BASE + "/event/" + eventId + "/incidents",
                SSIncidentsResponse.class);
        return resp != null && resp.incidents() != null ? resp.incidents() : List.of();
    }

    public List<SSStatGroup> fetchStatistics(long eventId) {
        SSStatisticsResponse resp = get(BASE + "/event/" + eventId + "/statistics",
                SSStatisticsResponse.class);
        return resp != null && resp.statistics() != null ? resp.statistics() : List.of();
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    private <T> T get(String url, Class<T> type) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.set("Accept-Encoding", "gzip, deflate, br");
            headers.set("Referer", "https://www.sofascore.com/");
            headers.set("Origin", "https://www.sofascore.com");
            headers.set("Cache-Control", "no-cache");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Site", "same-site");
            ResponseEntity<String> resp = rest.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (resp.getBody() == null) return null;
            return mapper.readValue(resp.getBody(), type);
        } catch (Exception e) {
            log.warn("SofaScore GET {} falhou: {}", url, e.getMessage());
            return null;
        }
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSEventsResponse(List<SSEvent> events) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSEvent(
            long         id,
            SSTeam       homeTeam,
            SSTeam       awayTeam,
            SSScore      homeScore,
            SSScore      awayScore,
            SSStatus     status,
            SSTournament tournament,
            SSSeason     season,
            long         startTimestamp
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSTeam(long id, String name, String slug) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSScore(
            Integer current,
            Integer display,
            Integer period1,
            Integer period2,
            Integer normaltime
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSStatus(int code, String description, String type) {
        public boolean finished() {
            return "finished".equalsIgnoreCase(type) || "ended".equalsIgnoreCase(description);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSTournament(
            String name,
            @JsonProperty("uniqueTournament") SSUniqueTournament uniqueTournament
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSUniqueTournament(long id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSSeason(long id, String name, String year) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSIncidentsResponse(List<SSIncident> incidents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSIncident(
            String   incidentType,
            Integer  time,
            Integer  addedTime,
            Boolean  isHome,
            SSPlayer player,
            SSPlayer assist1,
            Integer  homeScore,
            Integer  awayScore,
            String   goalType,
            String   bodyPart,
            String   cardType,
            String   reason
    ) {
        public String minuto() {
            if (time == null) return "";
            return (addedTime != null && addedTime > 0)
                    ? time + "+" + addedTime + "'"
                    : time + "'";
        }

        public boolean isGolo() {
            return "goal".equalsIgnoreCase(incidentType);
        }

        public boolean isCartao() {
            return "card".equalsIgnoreCase(incidentType);
        }

        public boolean isAmarelo() {
            return isCartao() && "yellow".equalsIgnoreCase(cardType);
        }

        public boolean isVermelho() {
            return isCartao() && ("red".equalsIgnoreCase(cardType)
                    || "yellowRed".equalsIgnoreCase(cardType));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSPlayer(long id, String name, String slug, String position) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSStatisticsResponse(List<SSStatGroup> statistics) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSStatGroup(
            String        groupName,
            List<SSStat>  statisticsItems
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SSStat(
            String name,
            String home,
            String away,
            Number homeValue,
            Number awayValue,
            String key
    ) {}
}
