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

@Component
public class OpenFootballClient {

    private static final Logger log = LoggerFactory.getLogger(OpenFootballClient.class);
    private static final String URL =
            "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json";

    private final RestTemplate rest;
    private final ObjectMapper mapper;

    public OpenFootballClient(RestTemplateBuilder builder, ObjectMapper mapper) {
        this.rest   = builder.build();
        this.mapper = mapper;
    }

    private static final String BASE = "https://raw.githubusercontent.com/openfootball/";

    public List<OFMatch> fetchMatches() {
        return fetchYear(2026);
    }

    public List<OFMatch> fetchYear(int year) {
        String url = (year == 2026) ? URL
                : BASE + "worldcup.json/master/" + year + "/worldcup.json";
        return fetch(url, "Copa do Mundo " + year);
    }

    public List<OFMatch> fetchCopaAmerica(int year) {
        String url = BASE + "copa-america.json/master/" + year + "/copa-america.json";
        return fetch(url, "Copa América " + year);
    }

    public List<OFMatch> fetchEuro(int year) {
        String url = BASE + "euro.json/master/" + year + "/euro.json";
        return fetch(url, "UEFA Euro " + year);
    }

    private List<OFMatch> fetch(String url, String label) {
        try {
            String json = rest.getForObject(url, String.class);
            if (json == null) return List.of();
            OFResponse resp = mapper.readValue(json, OFResponse.class);
            return (resp != null && resp.matches() != null) ? resp.matches() : List.of();
        } catch (Exception e) {
            log.warn("Falha ao buscar {} openfootball: {}", label, e.getMessage());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OFResponse(String name, List<OFMatch> matches) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OFMatch(
            String round,
            String date,
            String time,
            String team1,
            String team2,
            OFScore score,
            String group,
            String ground
    ) {
        public boolean hasScore() {
            return score != null && score.ft() != null && score.ft().size() == 2;
        }
        public Integer scoreHome() { return hasScore() ? score.ft().get(0) : null; }
        public Integer scoreAway() { return hasScore() ? score.ft().get(1) : null; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OFScore(
            @JsonProperty("ft") List<Integer> ft,
            @JsonProperty("ht") List<Integer> ht
    ) {}
}
