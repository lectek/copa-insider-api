package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.copa.ApiFootballClient;
import br.com.lectek.copainsider.application.copa.ApiFootballClient.*;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiFootballService {

    private static final Logger log = LoggerFactory.getLogger(ApiFootballService.class);

    private final ApiFootballClient client;
    private final int leagueId;
    private final int season;

    // fixture ID → eventos e stats (cache permanente — partidas encerradas não mudam)
    private final ConcurrentHashMap<Long, List<AFEvent>>    eventCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<AFTeamStats>> statsCache = new ConcurrentHashMap<>();

    // partida Copa Insider ID → fixture ID da API-Football
    private final ConcurrentHashMap<Long, Long> fixtureIdCache = new ConcurrentHashMap<>();

    // lista de fixtures do torneio, carregada lazy
    private volatile List<AFFixture> allFixtures = null;

    public ApiFootballService(ApiFootballClient client,
                              @Value("${api.football.league-id:1}") int leagueId,
                              @Value("${api.football.season:2026}") int season) {
        this.client   = client;
        this.leagueId = leagueId;
        this.season   = season;
    }

    public boolean disponivel() {
        return client.configured();
    }

    // ── Resultado principal: dados completos de uma partida ──────────────────

    public Optional<MatchData> findMatchData(PartidaVM partida) {
        if (!client.configured()) return Optional.empty();

        Long fixtureId = resolveFixtureId(partida);
        if (fixtureId == null) return Optional.empty();

        List<AFEvent>    events = eventCache.computeIfAbsent(fixtureId,
                id -> client.fetchEvents(id));
        List<AFTeamStats> stats = statsCache.computeIfAbsent(fixtureId,
                id -> client.fetchStatistics(id));

        return Optional.of(new MatchData(fixtureId, events, stats));
    }

    // ── Match de fixture ────────────────────────────────────────────────────

    private Long resolveFixtureId(PartidaVM partida) {
        return fixtureIdCache.computeIfAbsent(partida.id(), id -> {
            List<AFFixture> fixtures = ensureFixtures();
            return fixtures.stream()
                    .filter(f -> matches(f, partida))
                    .map(f -> f.fixture().id())
                    .findFirst().orElse(null);
        });
    }

    private boolean matches(AFFixture f, PartidaVM p) {
        if (f.teams() == null || p.dataHora() == null) return false;
        // Data (tolerância de ±1 dia por diferenças de fuso)
        try {
            LocalDate apiDate   = LocalDate.parse(f.fixture().date().substring(0, 10));
            LocalDate partidaDate = p.dataHora().toLocalDate();
            if (Math.abs(apiDate.toEpochDay() - partidaDate.toEpochDay()) > 1) return false;
        } catch (Exception e) {
            return false;
        }
        // Times por nome normalizado
        String home = normalize(f.teams().home().name());
        String away = normalize(f.teams().away().name());
        String casa      = normalize(p.selecaoCasa());
        String visitante = normalize(p.selecaoVisitante());
        return (similar(home, casa) && similar(away, visitante))
                || (similar(home, visitante) && similar(away, casa));
    }

    private List<AFFixture> ensureFixtures() {
        if (allFixtures == null) {
            synchronized (this) {
                if (allFixtures == null) {
                    allFixtures = client.fetchFixtures(leagueId, season);
                    log.info("API-Football: {} fixtures carregados (league={} season={})",
                            allFixtures.size(), leagueId, season);
                }
            }
        }
        return allFixtures;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private static boolean similar(String a, String b) {
        if (a.equals(b)) return true;
        // Prefixo de 4 chars como fallback (ex: "braz" ~ "brazil")
        int len = Math.min(Math.min(a.length(), b.length()), 4);
        return len >= 3 && a.substring(0, len).equals(b.substring(0, len));
    }

    // ── Record de resultado ──────────────────────────────────────────────────

    public record MatchData(
            long            fixtureId,
            List<AFEvent>   events,
            List<AFTeamStats> stats
    ) {
        public List<AFEvent> golos() {
            return events.stream()
                    .filter(e -> "Goal".equalsIgnoreCase(e.type()))
                    .toList();
        }

        public List<AFEvent> cartoes() {
            return events.stream()
                    .filter(e -> "Card".equalsIgnoreCase(e.type()))
                    .toList();
        }

        public Optional<String> stat(String teamName, String statType) {
            return stats.stream()
                    .filter(ts -> normalize(ts.team().name()).contains(normalize(teamName))
                               || normalize(teamName).contains(normalize(ts.team().name())))
                    .flatMap(ts -> ts.statistics().stream())
                    .filter(s -> statType.equalsIgnoreCase(s.type()))
                    .map(AFStat::valueStr)
                    .findFirst();
        }

        private static String normalize(String s) {
            return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
    }
}
