package br.com.lectek.copainsider.application.copa;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static br.com.lectek.copainsider.application.copa.PartidaVM.StatusPartida.*;

// Edições históricas a carregar para o comparador
// Carregadas de forma lazy na primeira comparação

@Service
public class Copa2026DataService {

    private static final Logger log = LoggerFactory.getLogger(Copa2026DataService.class);

    // ── Metadados estáticos das seleções (nome inglês → dados em português) ──

    private record TeamMeta(
            String nome, String slug, String bandeira,
            String confederacao, String melhorResultado, int participacoes) {}

    private static final Map<String, TeamMeta> TEAMS = Map.ofEntries(
        // Grupo A
        Map.entry("Mexico",               new TeamMeta("México",          "mexico",          "🇲🇽", "CONCACAF", "3º lugar (1970, 1986)", 17)),
        Map.entry("South Africa",         new TeamMeta("África do Sul",   "africa-do-sul",   "🇿🇦", "CAF",      "Quartos (2010)", 4)),
        Map.entry("South Korea",          new TeamMeta("Coreia do Sul",   "coreia-do-sul",   "🇰🇷", "AFC",      "4º lugar (2002)", 11)),
        Map.entry("Czech Republic",       new TeamMeta("Rep. Checa",      "rep-checa",       "🇨🇿", "UEFA",     "Vice-campeã (1962)", 10)),
        // Grupo B
        Map.entry("Canada",               new TeamMeta("Canadá",          "canada",          "🇨🇦", "CONCACAF", "Fase de grupos (2022)", 3)),
        Map.entry("Bosnia & Herzegovina", new TeamMeta("Bósnia-Herz.",    "bosnia",          "🇧🇦", "UEFA",     "Fase de grupos (2014)", 2)),
        Map.entry("Qatar",                new TeamMeta("Qatar",           "qatar",           "🇶🇦", "AFC",      "Fase de grupos (2022)", 2)),
        Map.entry("Switzerland",          new TeamMeta("Suíça",           "suica",           "🇨🇭", "UEFA",     "Quartos (1954)", 12)),
        // Grupo C
        Map.entry("Brazil",               new TeamMeta("Brasil",          "brasil",          "🇧🇷", "CONMEBOL", "Campeão (1958,62,70,94,02)", 22)),
        Map.entry("Morocco",              new TeamMeta("Marrocos",        "marrocos",        "🇲🇦", "CAF",      "4º lugar (2022)", 10)),
        Map.entry("Haiti",                new TeamMeta("Haiti",           "haiti",           "🇭🇹", "CONCACAF", "Fase de grupos", 2)),
        Map.entry("Scotland",             new TeamMeta("Escócia",         "escocia",         "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "UEFA", "Fase de grupos", 8)),
        // Grupo D
        Map.entry("USA",                  new TeamMeta("EUA",             "eua",             "🇺🇸", "CONCACAF", "3º lugar (1930)", 11)),
        Map.entry("Paraguay",             new TeamMeta("Paraguai",        "paraguai",        "🇵🇾", "CONMEBOL", "Quartos (2010)", 9)),
        Map.entry("Australia",            new TeamMeta("Austrália",       "australia",       "🇦🇺", "AFC",      "4º lugar (2006)", 6)),
        Map.entry("Turkey",               new TeamMeta("Turquia",         "turquia",         "🇹🇷", "UEFA",     "3º lugar (2002)", 3)),
        // Grupo E
        Map.entry("Germany",              new TeamMeta("Alemanha",        "alemanha",        "🇩🇪", "UEFA",     "Campeã (1954,74,90,2014)", 20)),
        Map.entry("Curaçao",              new TeamMeta("Curaçao",         "curacao",         "🇨🇼", "CONCACAF", "Estreia", 0)),
        Map.entry("Ivory Coast",          new TeamMeta("Costa do Marfim", "costa-do-marfim", "🇨🇮", "CAF",      "Fase de grupos", 3)),
        Map.entry("Ecuador",              new TeamMeta("Equador",         "equador",         "🇪🇨", "CONMEBOL", "Quartos (2006)", 4)),
        // Grupo F
        Map.entry("Netherlands",          new TeamMeta("Países Baixos",   "paises-baixos",   "🇳🇱", "UEFA",     "Vice (1974,78,2010)", 11)),
        Map.entry("Japan",                new TeamMeta("Japão",           "japao",           "🇯🇵", "AFC",      "Oitavos (2022)", 7)),
        Map.entry("Sweden",               new TeamMeta("Suécia",          "suecia",          "🇸🇪", "UEFA",     "3º lugar (1950,1994)", 12)),
        Map.entry("Tunisia",              new TeamMeta("Tunísia",         "tunisia",         "🇹🇳", "CAF",      "Fase de grupos", 6)),
        // Grupo G
        Map.entry("Belgium",              new TeamMeta("Bélgica",         "belgica",         "🇧🇪", "UEFA",     "3º lugar (2018)", 14)),
        Map.entry("Egypt",                new TeamMeta("Egito",           "egito",           "🇪🇬", "CAF",      "Fase de grupos", 3)),
        Map.entry("Iran",                 new TeamMeta("Irão",            "irao",            "🇮🇷", "AFC",      "Fase de grupos", 6)),
        Map.entry("New Zealand",          new TeamMeta("Nova Zelândia",   "nova-zelandia",   "🇳🇿", "OFC",      "Fase de grupos", 3)),
        // Grupo H
        Map.entry("Spain",                new TeamMeta("Espanha",         "espanha",         "🇪🇸", "UEFA",     "Campeã (2010)", 16)),
        Map.entry("Cape Verde",           new TeamMeta("Cabo Verde",      "cabo-verde",      "🇨🇻", "CAF",      "Estreia", 0)),
        Map.entry("Saudi Arabia",         new TeamMeta("Arábia Saudita",  "arabia-saudita",  "🇸🇦", "AFC",      "Fase de grupos", 6)),
        Map.entry("Uruguay",              new TeamMeta("Uruguai",         "uruguai",         "🇺🇾", "CONMEBOL", "Campeão (1930,1950)", 14)),
        // Grupo I
        Map.entry("France",               new TeamMeta("França",          "franca",          "🇫🇷", "UEFA",     "Campeã (1998,2018)", 16)),
        Map.entry("Senegal",              new TeamMeta("Senegal",         "senegal",         "🇸🇳", "CAF",      "Quartos (2002)", 4)),
        Map.entry("Iraq",                 new TeamMeta("Iraque",          "iraque",          "🇮🇶", "AFC",      "Fase de grupos", 2)),
        Map.entry("Norway",               new TeamMeta("Noruega",         "noruega",         "🇳🇴", "UEFA",     "Quartos (1938,1994)", 4)),
        // Grupo J
        Map.entry("Argentina",            new TeamMeta("Argentina",       "argentina",       "🇦🇷", "CONMEBOL", "Campeão (1978,86,2022)", 18)),
        Map.entry("Algeria",              new TeamMeta("Argélia",         "argelia",         "🇩🇿", "CAF",      "Oitavos (2014)", 4)),
        Map.entry("Austria",              new TeamMeta("Áustria",         "austria",         "🇦🇹", "UEFA",     "3º lugar (1954)", 7)),
        Map.entry("Jordan",               new TeamMeta("Jordânia",        "jordania",        "🇯🇴", "AFC",      "Estreia", 0)),
        // Grupo K
        Map.entry("Portugal",             new TeamMeta("Portugal",        "portugal",        "🇵🇹", "UEFA",     "3º lugar (1966,2006)", 9)),
        Map.entry("DR Congo",             new TeamMeta("RD Congo",        "rd-congo",        "🇨🇩", "CAF",      "Fase de grupos (1974)", 2)),
        Map.entry("Uzbekistan",           new TeamMeta("Uzbequistão",     "uzbequistao",     "🇺🇿", "AFC",      "Estreia", 0)),
        Map.entry("Colombia",             new TeamMeta("Colômbia",        "colombia",        "🇨🇴", "CONMEBOL", "Quartos (2014)", 7)),
        // Grupo L
        Map.entry("England",              new TeamMeta("Inglaterra",      "inglaterra",      "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "UEFA", "Campeã (1966)", 17)),
        Map.entry("Croatia",              new TeamMeta("Croácia",         "croacia",         "🇭🇷", "UEFA",     "Vice-campeã (2018)", 8)),
        Map.entry("Ghana",                new TeamMeta("Gana",            "gana",            "🇬🇭", "CAF",      "Quartos (2010)", 4)),
        Map.entry("Panama",               new TeamMeta("Panamá",          "panama",          "🇵🇦", "CONCACAF", "Fase de grupos", 2))
    );

    // ── Mapeamento inverso: nome inglês → slug ───────────────────────────────

    private static final Map<String, String> ENGLISH_TO_SLUG;
    static {
        Map<String, String> m = new HashMap<>();
        TEAMS.forEach((english, meta) -> m.put(english.toLowerCase(Locale.ROOT), meta.slug()));
        // Aliases históricos usados em edições anteriores do openfootball
        m.put("ivory coast",        "costa-do-marfim");
        m.put("côte d'ivoire",      "costa-do-marfim");
        m.put("cote d'ivoire",      "costa-do-marfim");
        m.put("korea republic",     "coreia-do-sul");
        m.put("united states",      "eua");
        m.put("west germany",       "alemanha");
        m.put("bosnia-herzegovina", "bosnia");
        ENGLISH_TO_SLUG = Collections.unmodifiableMap(m);
    }

    // ── Histórico de Copas (lazy-loaded) ─────────────────────────────────────

    private record YearMatch(int ano, OpenFootballClient.OFMatch match) {}

    private static final List<Integer> HISTORICAL_YEARS = List.of(2022, 2018, 2014, 2010, 2006);
    private volatile List<YearMatch> historicalMatches = null;

    // ── Estado dinâmico ──────────────────────────────────────────────────────

    private final OpenFootballClient client;
    private volatile List<PartidaVM>  partidas  = List.of();
    private volatile List<SelecaoVM>  selecoes  = List.of();
    private final    List<JogadorVM>  jogadores;
    private final    List<RivalidadeVM> rivalidades;

    public Copa2026DataService(OpenFootballClient client) {
        this.client = client;
        this.jogadores   = buildJogadores();
        this.rivalidades = buildRivalidades();
    }

    @PostConstruct
    public void init() { refresh(); }

    @Scheduled(fixedDelay = 1_800_000) // 30 min
    public void refresh() {
        try {
            List<OpenFootballClient.OFMatch> matches = client.fetchMatches();
            if (!matches.isEmpty()) {
                partidas = buildPartidas(matches);
                selecoes = buildSelecoes(matches);
                log.info("Copa 2026: {} partidas carregadas", partidas.size());
            }
        } catch (Exception e) {
            log.error("Erro ao atualizar dados Copa 2026: {}", e.getMessage());
        }
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public List<SelecaoVM> listSelecoes() { return selecoes; }

    public Optional<SelecaoVM> findSelecao(String slug) {
        return selecoes.stream().filter(s -> s.slug().equals(slug)).findFirst();
    }

    public Map<String, List<SelecaoVM>> selecoesPorGrupo() {
        return selecoes.stream().collect(
                Collectors.groupingBy(SelecaoVM::grupo, TreeMap::new, Collectors.toList()));
    }

    public List<PartidaVM> listPartidas() { return partidas; }

    public Optional<PartidaVM> findPartida(Long id) {
        return partidas.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    public Map<String, List<PartidaVM>> partidasPorGrupo() {
        return partidas.stream()
                .filter(p -> p.grupo() != null && !p.grupo().isBlank())
                .collect(Collectors.groupingBy(PartidaVM::grupo, TreeMap::new, Collectors.toList()));
    }

    public List<PartidaVM> proximasPartidas(int limite) {
        return partidas.stream()
                .filter(p -> p.status() == AGENDADA)
                .sorted(Comparator.comparing(PartidaVM::dataHora))
                .limit(limite)
                .toList();
    }

    public List<PartidaVM> partidasAoVivo() {
        return partidas.stream().filter(PartidaVM::aoVivo).toList();
    }

    public List<PartidaVM> partidasDaSelecao(String slug) {
        return partidas.stream()
                .filter(p -> p.slugCasa().equals(slug) || p.slugVisitante().equals(slug))
                .sorted(Comparator.comparing(PartidaVM::dataHora))
                .toList();
    }

    public List<JogadorVM> listJogadores() { return jogadores; }

    public List<JogadorVM> topGoleadores(int limite) {
        return jogadores.stream()
                .sorted(Comparator.comparingInt(JogadorVM::gols).reversed())
                .limit(limite).toList();
    }

    public List<JogadorVM> topAssistentes(int limite) {
        return jogadores.stream()
                .sorted(Comparator.comparingInt(JogadorVM::assistencias).reversed())
                .limit(limite).toList();
    }

    public List<JogadorVM> topNota(int limite) {
        return jogadores.stream()
                .sorted(Comparator.comparingDouble(JogadorVM::nota).reversed())
                .limit(limite).toList();
    }

    public List<RivalidadeVM> listRivalidades() { return rivalidades; }

    public List<OpenFootballClient.OFMatch> listHistoricalMatches() {
        ensureHistoricalLoaded();
        return historicalMatches.stream().map(YearMatch::match).toList();
    }

    // ── Comparador ───────────────────────────────────────────────────────────

    public Optional<ComparadorVM> comparar(String slug1, String slug2) {
        Optional<SelecaoVM> opt1 = findSelecao(slug1);
        Optional<SelecaoVM> opt2 = findSelecao(slug2);
        if (opt1.isEmpty() || opt2.isEmpty()) return Optional.empty();

        SelecaoVM s1 = opt1.get();
        SelecaoVM s2 = opt2.get();
        String en1   = englishNameForSlug(slug1);
        String en2   = englishNameForSlug(slug2);

        ensureHistoricalLoaded();

        List<EncuentroVM> encontros = new ArrayList<>();
        int v1 = 0, emp = 0, v2 = 0, g1 = 0, g2 = 0;

        for (YearMatch ym : historicalMatches) {
            OpenFootballClient.OFMatch m = ym.match();
            if (!m.hasScore()) continue;

            boolean m1IsS1 = en1 != null && en1.equalsIgnoreCase(m.team1());
            boolean m1IsS2 = en2 != null && en2.equalsIgnoreCase(m.team1());
            boolean m2IsS1 = en1 != null && en1.equalsIgnoreCase(m.team2());
            boolean m2IsS2 = en2 != null && en2.equalsIgnoreCase(m.team2());

            if (!((m1IsS1 && m2IsS2) || (m1IsS2 && m2IsS1))) continue;

            int gs1 = m1IsS1 ? m.scoreHome() : m.scoreAway();
            int gs2 = m1IsS1 ? m.scoreAway() : m.scoreHome();

            g1 += gs1; g2 += gs2;
            if (gs1 > gs2) v1++; else if (gs1 < gs2) v2++; else emp++;

            encontros.add(new EncuentroVM(ym.ano(), extractFase(m.round()),
                    s1.bandeira(), s1.nome(), gs1, gs2, s2.nome(), s2.bandeira()));
        }

        encontros.sort(Comparator.comparingInt(EncuentroVM::ano).reversed());

        PartidaVM enc2026 = partidas.stream()
                .filter(p -> (p.slugCasa().equals(slug1) && p.slugVisitante().equals(slug2))
                          || (p.slugCasa().equals(slug2) && p.slugVisitante().equals(slug1)))
                .findFirst().orElse(null);

        return Optional.of(new ComparadorVM(s1, s2, v1, emp, v2, g1, g2, encontros, enc2026));
    }

    private synchronized void ensureHistoricalLoaded() {
        if (historicalMatches != null) return;
        List<YearMatch> all = new ArrayList<>();
        for (int year : HISTORICAL_YEARS) {
            for (var m : client.fetchYear(year)) {
                if (m.hasScore()) all.add(new YearMatch(year, m));
            }
        }
        historicalMatches = Collections.unmodifiableList(all);
        log.info("Histórico Copa: {} partidas carregadas ({})", historicalMatches.size(), HISTORICAL_YEARS);
    }

    private String englishNameForSlug(String slug) {
        return TEAMS.entrySet().stream()
                .filter(e -> e.getValue().slug().equals(slug))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    // ── Construção a partir dos dados da API ─────────────────────────────────

    private List<PartidaVM> buildPartidas(List<OpenFootballClient.OFMatch> matches) {
        AtomicLong idGen = new AtomicLong(1);
        return matches.stream()
                .filter(m -> m.team1() != null && m.team2() != null)
                .map(m -> {
                    TeamMeta casa      = resolve(m.team1());
                    TeamMeta visitante = resolve(m.team2());
                    String grupo    = extractGroup(m.group());
                    String fase     = extractFase(m.round());
                    LocalDateTime dt = parseDateTime(m.date(), m.time());
                    PartidaVM.StatusPartida status = m.hasScore() ? ENCERRADA : AGENDADA;
                    String[] ground = splitGround(m.ground());
                    return new PartidaVM(
                            idGen.getAndIncrement(),
                            casa.nome(), casa.slug(), casa.bandeira(),
                            visitante.nome(), visitante.slug(), visitante.bandeira(),
                            dt, fase, grupo,
                            m.scoreHome(), m.scoreAway(),
                            status, ground[0], ground[1]);
                })
                .toList();
    }

    private List<SelecaoVM> buildSelecoes(List<OpenFootballClient.OFMatch> matches) {
        // Extrai pares (nome inglês → grupo) sem repetições
        Map<String, String> teamToGroup = new LinkedHashMap<>();
        for (var m : matches) {
            String grupo = extractGroup(m.group());
            if (grupo == null) continue;
            if (m.team1() != null) teamToGroup.putIfAbsent(m.team1(), grupo);
            if (m.team2() != null) teamToGroup.putIfAbsent(m.team2(), grupo);
        }
        return teamToGroup.entrySet().stream()
                .map(e -> {
                    TeamMeta meta = resolve(e.getKey());
                    return new SelecaoVM(meta.nome(), meta.slug(), meta.bandeira(),
                            e.getValue(), meta.confederacao(), meta.melhorResultado(),
                            meta.participacoes());
                })
                .sorted(Comparator.comparing(SelecaoVM::grupo).thenComparing(SelecaoVM::nome))
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TeamMeta resolve(String englishName) {
        return TEAMS.getOrDefault(englishName,
                new TeamMeta(englishName,
                        englishName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-"),
                        "🏳", "—", "—", 0));
    }

    private String extractGroup(String group) {
        if (group == null) return null;
        if (group.startsWith("Group ")) return group.substring(6).trim();
        return null;
    }

    private String extractFase(String round) {
        if (round == null) return "Fase de grupos";
        if (round.startsWith("Matchday")) return "Fase de grupos";
        return switch (round) {
            case "Round of 32"       -> "32 avos";
            case "Round of 16"       -> "Oitavas";
            case "Quarter-finals"    -> "Quartas";
            case "Semi-finals"       -> "Semifinais";
            case "Third place match" -> "3º Lugar";
            case "Final"             -> "Final";
            default                  -> round;
        };
    }

    private LocalDateTime parseDateTime(String date, String time) {
        try {
            LocalDate d = LocalDate.parse(date);
            String timeStr = (time != null) ? time.split(" ")[0] : "00:00";
            LocalTime t = LocalTime.parse(timeStr);
            return LocalDateTime.of(d, t);
        } catch (Exception e) {
            return LocalDateTime.of(2026, 6, 11, 0, 0);
        }
    }

    private String[] splitGround(String ground) {
        if (ground == null) return new String[]{"", ""};
        // "Estadio Azteca, Mexico City" → ["Estadio Azteca", "Mexico City"]
        // "Mexico City" → ["", "Mexico City"]
        int comma = ground.lastIndexOf(',');
        if (comma > 0) {
            return new String[]{ground.substring(0, comma).trim(), ground.substring(comma + 1).trim()};
        }
        return new String[]{"", ground.trim()};
    }

    // ── Dados estáticos complementares ───────────────────────────────────────

    private List<JogadorVM> buildJogadores() {
        return List.of(
            new JogadorVM(1L,  "Vinicius Jr.",    "Brasil",    "brasil",    "🇧🇷", "Avançado", "Real Madrid",  24, 4, 3, 4, 9.1, ""),
            new JogadorVM(2L,  "Kylian Mbappé",   "França",    "franca",    "🇫🇷", "Avançado", "Real Madrid",  27, 3, 2, 3, 8.8, ""),
            new JogadorVM(3L,  "Erling Haaland",  "Noruega",   "noruega",   "🇳🇴", "Avançado", "Man City",     25, 5, 1, 3, 9.0, ""),
            new JogadorVM(4L,  "Lionel Messi",    "Argentina", "argentina", "🇦🇷", "Médio",    "Inter Miami",  38, 2, 4, 4, 8.7, ""),
            new JogadorVM(5L,  "Cristiano Ronaldo","Portugal", "portugal",  "🇵🇹", "Avançado", "Al Nassr",     41, 2, 1, 4, 7.9, ""),
            new JogadorVM(6L,  "Jude Bellingham", "Inglaterra","inglaterra","🏴󠁧󠁢󠁥󠁮󠁧󠁿","Médio","Real Madrid", 22, 2, 3, 4, 8.6, ""),
            new JogadorVM(7L,  "Rodri",           "Espanha",   "espanha",   "🇪🇸", "Médio",    "Man City",     29, 1, 4, 4, 8.9, ""),
            new JogadorVM(8L,  "Lamine Yamal",    "Espanha",   "espanha",   "🇪🇸", "Avançado", "Barcelona",    18, 3, 3, 4, 8.7, ""),
            new JogadorVM(9L,  "Pedri",           "Espanha",   "espanha",   "🇪🇸", "Médio",    "Barcelona",    23, 1, 3, 3, 8.4, ""),
            new JogadorVM(10L, "Raphinha",        "Brasil",    "brasil",    "🇧🇷", "Avançado", "Barcelona",    28, 2, 2, 4, 8.3, "")
        );
    }

    private List<RivalidadeVM> buildRivalidades() {
        return List.of(
            new RivalidadeVM("brasil-argentina", "Brasil", "brasil", "🇧🇷", "Argentina", "argentina", "🇦🇷",
                    40, 25, 35, "Copa 1990 — Argentina 1-0 Brasil", "O clássico sul-americano mais intenso do mundo."),
            new RivalidadeVM("franca-argentina", "França", "franca", "🇫🇷", "Argentina", "argentina", "🇦🇷",
                    7, 3, 10, "Final 2022 — Argentina 3(4)-3(2) França", "A final de Qatar 2022 foi considerada a melhor de todos os tempos."),
            new RivalidadeVM("brasil-franca", "Brasil", "brasil", "🇧🇷", "França", "franca", "🇫🇷",
                    10, 6, 12, "Semi 2006 — França 1-0 Brasil", "Dois dos países mais vitoriosos da Copa do Mundo."),
            new RivalidadeVM("alemanha-brasil", "Alemanha", "alemanha", "🇩🇪", "Brasil", "brasil", "🇧🇷",
                    14, 4, 7, "Semi 2014 — Alemanha 7-1 Brasil", "O Mineirazo. A maior goleada em semi-finais da história."),
            new RivalidadeVM("espanha-portugal", "Espanha", "espanha", "🇪🇸", "Portugal", "portugal", "🇵🇹",
                    16, 4, 5, "Grupo 2018 — Espanha 3-3 Portugal", "Os vizinhos ibéricos que raramente se encontram nas Copas."),
            new RivalidadeVM("brasil-alemanha2", "Brasil", "brasil", "🇧🇷", "Alemanha", "alemanha", "🇩🇪",
                    7, 4, 14, "Semi 2014 — 7-1", "A cicatriz que ainda dói. Brasil vs Alemanha tem história."),
            new RivalidadeVM("inglaterra-alemanha", "Inglaterra", "inglaterra", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Alemanha", "alemanha", "🇩🇪",
                    12, 5, 8, "Final 1966 — Inglaterra 4-2 Alemanha", "A única Copa da Inglaterra. Um clássico europeu histórico.")
        );
    }
}
