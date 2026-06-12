package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.copa.SofaScoreClient;
import br.com.lectek.copainsider.application.copa.SofaScoreClient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SofaScoreService {

    private static final Logger log           = LoggerFactory.getLogger(SofaScoreService.class);
    private static final String WORLD_CUP_KEY = "world cup";

    private final SofaScoreClient client;

    // partida ID → SofaScore event ID
    private final ConcurrentHashMap<Long, Long>             eventIdCache   = new ConcurrentHashMap<>();
    // SofaScore event ID → incidents
    private final ConcurrentHashMap<Long, List<SSIncident>> incidentCache  = new ConcurrentHashMap<>();
    // SofaScore event ID → stat groups
    private final ConcurrentHashMap<Long, List<SSStatGroup>> statsCache    = new ConcurrentHashMap<>();

    public SofaScoreService(SofaScoreClient client) {
        this.client = client;
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public Optional<MatchData> findMatchData(PartidaVM partida) {
        Long eventId = resolveEventId(partida);
        if (eventId == null) return Optional.empty();

        List<SSIncident>  incidents = incidentCache.computeIfAbsent(eventId, client::fetchIncidents);
        List<SSStatGroup> stats     = statsCache.computeIfAbsent(eventId, client::fetchStatistics);

        return Optional.of(new MatchData(eventId, incidents, stats));
    }

    // ── Match lookup ─────────────────────────────────────────────────────────

    private Long resolveEventId(PartidaVM partida) {
        return eventIdCache.computeIfAbsent(partida.id(), id -> {
            if (partida.dataHora() == null) return null;
            String date = partida.dataHora().toLocalDate().toString();
            List<SSEvent> events = client.fetchEventsByDate(date);
            if (events.isEmpty()) {
                // Tenta o dia seguinte (diferença de fuso)
                String next = partida.dataHora().toLocalDate().plusDays(1).toString();
                events = client.fetchEventsByDate(next);
            }
            return events.stream()
                    .filter(this::isWorldCup)
                    .filter(e -> matchesTeams(e, partida))
                    .map(SSEvent::id)
                    .findFirst()
                    .orElseGet(() -> {
                        log.warn("SofaScore: não encontrou partida {} vs {} em {}",
                                partida.selecaoCasa(), partida.selecaoVisitante(), date);
                        return null;
                    });
        });
    }

    private boolean isWorldCup(SSEvent e) {
        return (e.season() != null && e.season().name() != null
                    && e.season().name().toLowerCase().contains(WORLD_CUP_KEY))
                || (e.tournament() != null && e.tournament().name() != null
                    && e.tournament().name().toLowerCase().contains(WORLD_CUP_KEY))
                || (e.tournament() != null && e.tournament().uniqueTournament() != null
                    && e.tournament().uniqueTournament().name() != null
                    && e.tournament().uniqueTournament().name().toLowerCase().contains(WORLD_CUP_KEY));
    }

    private boolean matchesTeams(SSEvent e, PartidaVM p) {
        if (e.homeTeam() == null || e.awayTeam() == null) return false;
        String ssHome = norm(e.homeTeam().name());
        String ssAway = norm(e.awayTeam().name());
        String casa   = norm(p.selecaoCasa());
        String visit  = norm(p.selecaoVisitante());
        return (similar(ssHome, casa) && similar(ssAway, visit))
                || (similar(ssHome, visit) && similar(ssAway, casa));
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean similar(String a, String b) {
        if (a.equals(b)) return true;
        int len = Math.min(Math.min(a.length(), b.length()), 4);
        return len >= 3 && a.startsWith(b.substring(0, len));
    }

    // ── MatchData ────────────────────────────────────────────────────────────

    public record MatchData(
            long             eventId,
            List<SSIncident> incidents,
            List<SSStatGroup> statGroups
    ) {
        public List<SSIncident> golos() {
            return incidents.stream().filter(SSIncident::isGolo).toList();
        }

        public List<SSIncident> cartoes() {
            return incidents.stream().filter(SSIncident::isCartao).toList();
        }

        public boolean temEventos() {
            return !golos().isEmpty() || !cartoes().isEmpty();
        }

        public boolean temEstatisticas() {
            return statGroups != null && !statGroups.isEmpty();
        }

        /** Valor da coluna Home de uma estatística pelo nome (case-insensitive). */
        public String statHome(String name) {
            return findStat(name).map(SSStat::home).orElse("-");
        }

        /** Valor da coluna Away de uma estatística pelo nome (case-insensitive). */
        public String statAway(String name) {
            return findStat(name).map(SSStat::away).orElse("-");
        }

        private Optional<SSStat> findStat(String name) {
            String n = norm(name);
            if (statGroups == null) return Optional.empty();
            return statGroups.stream()
                    .filter(g -> g.statisticsItems() != null)
                    .flatMap(g -> g.statisticsItems().stream())
                    .filter(s -> {
                        String sn = norm(s.name());
                        return sn.equals(n) || sn.contains(n) || n.contains(sn);
                    })
                    .findFirst();
        }

        private static String norm(String s) {
            return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
    }
}
