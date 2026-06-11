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
        // Aliases históricos — variantes usadas nas edições 2006-2022 do openfootball
        m.put("ivory coast",          "costa-do-marfim");
        m.put("côte d'ivoire",        "costa-do-marfim");
        m.put("cote d'ivoire",        "costa-do-marfim");
        m.put("korea republic",       "coreia-do-sul");
        m.put("republic of korea",    "coreia-do-sul");
        m.put("united states",        "eua");
        m.put("u.s.a.",               "eua");
        // — 2006-2022 aliases —
        m.put("west germany",         "alemanha");
        m.put("bosnia-herzegovina",   "bosnia");
        m.put("ir iran",              "irao");
        m.put("england",              "inglaterra");
        m.put("czechia",              "rep-checa");
        m.put("czech rep.",           "rep-checa");
        m.put("slovak republic",      "eslovaquia");
        m.put("north ireland",        "irlanda-do-norte");
        m.put("northern ireland",     "irlanda-do-norte");
        m.put("trinidad & tobago",    "trinidad-tobago");
        m.put("trinidad and tobago",  "trinidad-tobago");
        m.put("republic of ireland",  "irlanda");
        m.put("dr congo",             "rd-congo");
        m.put("democratic republic of congo", "rd-congo");
        m.put("cape verde",           "cabo-verde");
        m.put("new zealand",          "nova-zelandia");
        m.put("saudi arabia",         "arabia-saudita");
        m.put("south africa",         "africa-do-sul");
        m.put("south korea",          "coreia-do-sul");
        m.put("costa rica",           "costa-rica");
        // — Nomes em falta nos logs (2006-2022) —
        m.put("russia",               "russia");
        m.put("serbia",               "servia");
        m.put("serbia and montenegro","servia-e-montenegro");
        m.put("slovakia",             "eslovaquia");
        m.put("slovenia",             "eslovenia");
        m.put("togo",                 "togo");
        m.put("ukraine",              "ucrania");
        m.put("wales",                "gales");
        // — Nações históricas extintas (pré-2006) —
        m.put("soviet union",         "uniao-sovietica");
        m.put("ussr",                 "uniao-sovietica");
        m.put("yugoslavia",           "iugoslavia");
        m.put("czechoslovakia",       "tchecoslovaquia");
        m.put("east germany",         "alemanha-oriental");
        m.put("german dr",            "alemanha-oriental");
        m.put("zaire",                "rd-congo");
        m.put("dutch east indies",    "indonesia");
        // — Nomes comuns pré-2006 —
        m.put("nigeria",              "nigeria");
        m.put("cameroon",             "camaroes");
        m.put("peru",                 "peru");
        m.put("chile",                "chile");
        m.put("poland",               "polonia");
        m.put("hungary",              "hungria");
        m.put("italy",                "italia");
        m.put("romania",              "romenia");
        m.put("bulgaria",             "bulgaria");
        m.put("cuba",                 "cuba");
        m.put("china pr",             "china");
        m.put("north korea",          "coreia-do-norte");
        m.put("dpr korea",            "coreia-do-norte");
        m.put("republic of ireland",  "irlanda");
        m.put("greece",               "grecia");
        m.put("honduras",             "honduras");
        m.put("denmark",              "dinamarca");
        m.put("nigeria",              "nigeria");
        ENGLISH_TO_SLUG = Collections.unmodifiableMap(m);
    }

    // ── Histórico de Copas (lazy-loaded) ─────────────────────────────────────

    private record YearMatch(int ano, String competicao, String icone, OpenFootballClient.OFMatch match) {}

    private static final List<Integer> HISTORICAL_YEARS = List.of(
        2022, 2018, 2014, 2010, 2006,
        2002, 1998, 1994, 1990, 1986,
        1982, 1978, 1974, 1970, 1966,
        1962, 1958, 1954, 1950, 1938,
        1934, 1930
    );

    private static final List<Integer> COPA_AMERICA_YEARS = List.of(
        2024, 2021, 2019, 2016, 2015, 2011, 2007, 2004, 2001, 1999,
        1997, 1995, 1993, 1991, 1989, 1987, 1983, 1979, 1975
    );

    private static final List<Integer> UEFA_EURO_YEARS = List.of(
        2024, 2020, 2016, 2012, 2008, 2004, 2000, 1996,
        1992, 1988, 1984, 1980, 1976, 1972, 1968, 1964, 1960
    );
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

        ensureHistoricalLoaded();

        List<EncuentroVM> encontros = new ArrayList<>();
        int v1 = 0, emp = 0, v2 = 0, g1 = 0, g2 = 0;

        for (YearMatch ym : historicalMatches) {
            OpenFootballClient.OFMatch m = ym.match();
            if (!m.hasScore()) continue;

            String hSlug1 = resolveSlug(m.team1());
            String hSlug2 = resolveSlug(m.team2());
            if (hSlug1 == null || hSlug2 == null) continue;

            boolean m1IsS1 = hSlug1.equals(slug1);
            boolean m1IsS2 = hSlug1.equals(slug2);
            boolean m2IsS1 = hSlug2.equals(slug1);
            boolean m2IsS2 = hSlug2.equals(slug2);

            if (!((m1IsS1 && m2IsS2) || (m1IsS2 && m2IsS1))) continue;

            int gs1 = m1IsS1 ? m.scoreHome() : m.scoreAway();
            int gs2 = m1IsS1 ? m.scoreAway() : m.scoreHome();

            g1 += gs1; g2 += gs2;
            if (gs1 > gs2) v1++; else if (gs1 < gs2) v2++; else emp++;

            encontros.add(new EncuentroVM(ym.ano(), extractFase(m.round()),
                    s1.bandeira(), s1.nome(), gs1, gs2, s2.nome(), s2.bandeira(),
                    ym.competicao(), ym.icone()));
        }

        encontros.sort(Comparator.comparingInt(EncuentroVM::ano).reversed());

        PartidaVM enc2026 = partidas.stream()
                .filter(p -> (p.slugCasa().equals(slug1) && p.slugVisitante().equals(slug2))
                          || (p.slugCasa().equals(slug2) && p.slugVisitante().equals(slug1)))
                .findFirst().orElse(null);

        List<LendaVM> lendas = buildLendas(slug1, slug2);

        return Optional.of(new ComparadorVM(s1, s2, v1, emp, v2, g1, g2, encontros, enc2026, lendas));
    }

    private synchronized void ensureHistoricalLoaded() {
        if (historicalMatches != null) return;
        List<YearMatch> all = new ArrayList<>();

        for (int year : HISTORICAL_YEARS) {
            for (var m : client.fetchYear(year)) {
                if (m.hasScore()) all.add(new YearMatch(year, "Copa do Mundo", "🌍", m));
            }
        }
        for (int year : COPA_AMERICA_YEARS) {
            for (var m : client.fetchCopaAmerica(year)) {
                if (m.hasScore()) all.add(new YearMatch(year, "Copa América", "🏆", m));
            }
        }
        for (int year : UEFA_EURO_YEARS) {
            for (var m : client.fetchEuro(year)) {
                if (m.hasScore()) all.add(new YearMatch(year, "UEFA Euro", "🇪🇺", m));
            }
        }

        historicalMatches = Collections.unmodifiableList(all);
        log.info("Histórico multi-competição: {} partidas carregadas", historicalMatches.size());

        all.stream()
            .flatMap(ym -> java.util.stream.Stream.of(ym.match().team1(), ym.match().team2()))
            .filter(name -> name != null && ENGLISH_TO_SLUG.get(name.toLowerCase(Locale.ROOT)) == null)
            .distinct().sorted()
            .forEach(name -> log.warn("Histórico: sem mapeamento para '{}'", name));
    }

    // ── Lendas / Joias por rivalidade ────────────────────────────────────────

    private static final Map<String, List<LendaVM>> LENDAS_POR_RIVALIDADE = buildLegendsMap();

    private static Map<String, List<LendaVM>> buildLegendsMap() {
        Map<String, List<LendaVM>> m = new HashMap<>();

        // Brasil × Argentina
        List<LendaVM> braArg = List.of(
            new LendaVM("Pelé", "brasil", "🇧🇷", "Avançado", "1958–1977",
                "3× Campeão Mundial (1958, 1962, 1970)",
                "77 golos pela Seleção · Único jogador a vencer 3 Copas",
                "«O futebol é a mais bela das artes.»"),
            new LendaVM("Maradona", "argentina", "🇦🇷", "Meia-atacante", "1977–1994",
                "Campeão Mundial (1986) · Copa América (1987)",
                "34 golos · Gol do Século (1986) · Mão de Deus (1986)",
                "«O futebol não se joga com as mãos, exceto para mim.»"),
            new LendaVM("Ronaldo R9", "brasil", "🇧🇷", "Avançado", "1994–2006",
                "2× Campeão Mundial (1994, 2002) · Copa América (1997, 1999)",
                "15 golos em Copas · Artilheiro da Copa 2002 (8 golos)",
                "«Eu não jogo para enganar os adeptos.»"),
            new LendaVM("Batistuta", "argentina", "🇦🇷", "Avançado", "1991–2002",
                "Copa América (1991, 1993)",
                "10 golos em Copas · 56 golos pela Seleção · Maior artilheiro histórico até Messi",
                "«Nasci para marcar golos.»"),
            new LendaVM("Neymar Jr", "brasil", "🇧🇷", "Avançado", "2010–",
                "Campeão Olímpico (2016) · Copa das Confederações (2013)",
                "79 golos pela Seleção · Maior artilheiro da história do Brasil",
                "«O meu sonho é ganhar uma Copa do Mundo.»"),
            new LendaVM("Lionel Messi", "argentina", "🇦🇷", "Meia-atacante", "2005–",
                "Campeão Mundial (2022) · 3× Copa América (2021, 2015 sub, 1993)",
                "109 golos pela Seleção · 8× Bola de Ouro · Melhor de todos os tempos",
                "«O sucesso é resultado de trabalho duro, nunca de sorte.»")
        );
        putRivalidade(m, "brasil", "argentina", braArg);

        // Portugal × Espanha
        List<LendaVM> porEsp = List.of(
            new LendaVM("Cristiano Ronaldo", "portugal", "🇵🇹", "Avançado", "2003–",
                "UEFA Euro (2016) · Nations League (2019)",
                "130 golos pela Seleção · Maior marcador internacional de sempre",
                "«Talento sem trabalho não é nada.»"),
            new LendaVM("Luís Figo", "portugal", "🇵🇹", "Extremo", "1991–2006",
                "3º lugar Copa do Mundo (2006)",
                "Bola de Ouro 2000 · 127 jogos pela Seleção · 32 golos",
                "«Jogar pela Seleção é a maior honra.»"),
            new LendaVM("Iker Casillas", "espanha", "🇪🇸", "Guarda-redes", "2000–2016",
                "2× UEFA Euro (2008, 2012) · Copa do Mundo (2010)",
                "167 jogos · Melhor guarda-redes do mundo por 3 anos",
                "«Nunca perdes enquanto não desistes.»"),
            new LendaVM("Xavi Hernández", "espanha", "🇪🇸", "Médio", "2000–2014",
                "2× UEFA Euro (2008, 2012) · Copa do Mundo (2010)",
                "133 jogos · Arquitecto do jogo espanhol · 13 golos e 26 assistências",
                "«O futebol é posse de bola.»"),
            new LendaVM("Andrés Iniesta", "espanha", "🇪🇸", "Médio", "2006–2018",
                "2× UEFA Euro (2008, 2012) · Copa do Mundo (2010)",
                "Golo da Final 2010 · Melhor jogador do Euro 2012",
                "«Os momentos difíceis fazem-nos mais fortes.»")
        );
        putRivalidade(m, "portugal", "espanha", porEsp);

        // Brasil × Alemanha
        List<LendaVM> braAle = List.of(
            new LendaVM("Ronaldo R9", "brasil", "🇧🇷", "Avançado", "1994–2006",
                "2× Campeão Mundial (1994, 2002)",
                "Golo na Final de 2002 contra a Alemanha · 15 golos em Copas",
                "«Quando marco um golo, sinto que estou a voar.»"),
            new LendaVM("Ronaldinho", "brasil", "🇧🇷", "Meia-atacante", "1999–2013",
                "Campeão Mundial (2002)",
                "2× Bola de Ouro (2004, 2005) · 33 golos pela Seleção",
                "«O futebol é alegria, é magia.»"),
            new LendaVM("Miroslav Klose", "alemanha", "🇩🇪", "Avançado", "2001–2014",
                "Campeão Mundial (2014)",
                "16 golos em Copas · Recorde histórico mundial · 71 golos pela Seleção",
                "«Cada golo é um presente.»"),
            new LendaVM("Franz Beckenbauer", "alemanha", "🇩🇪", "Defesa/Médio", "1965–1977",
                "Campeão Mundial (1974) como jogador e treinador",
                "O Kaiser · 103 jogos · 14 golos · Bola de Ouro 1972 e 1976",
                "«Liderança é saber quando falar e quando calar.»")
        );
        putRivalidade(m, "brasil", "alemanha", braAle);

        // Portugal × França
        List<LendaVM> porFra = List.of(
            new LendaVM("Cristiano Ronaldo", "portugal", "🇵🇹", "Avançado", "2003–",
                "UEFA Euro (2016) · Nations League (2019)",
                "130 golos · Golo no Euro 2016 SF vs Gales · Lesionado na Final vs França",
                "«A derrota não existe para quem nunca desiste.»"),
            new LendaVM("Zinedine Zidane", "franca", "🇫🇷", "Meia-atacante", "1994–2006",
                "Campeão Mundial (1998) · UEFA Euro (2000)",
                "2× Bola de Ouro · 31 golos · Melhor jogador do Copa 1998",
                "«O talento sem trabalho é como um pássaro sem asas.»"),
            new LendaVM("Thierry Henry", "franca", "🇫🇷", "Avançado", "1997–2010",
                "Campeão Mundial (1998) · UEFA Euro (2000)",
                "51 golos pela Seleção · Artilheiro histórico de França até Giroud",
                "«Marcar pela França é sempre especial.»")
        );
        putRivalidade(m, "portugal", "franca", porFra);

        // França × Argentina
        List<LendaVM> fraArg = List.of(
            new LendaVM("Kylian Mbappé", "franca", "🇫🇷", "Avançado", "2017–",
                "Campeão Mundial (2018) · Finalista (2022)",
                "Melhor jovem Copa 2018 · Hat-trick na Final 2022 · 45 golos pela Seleção",
                "«Estou aqui para ganhar títulos.»"),
            new LendaVM("Lionel Messi", "argentina", "🇦🇷", "Meia-atacante", "2005–",
                "Campeão Mundial (2022)",
                "Bola de Ouro Copa 2022 · Melhor da Final 2022 · 109 golos",
                "«A final de 2022 foi o jogo da minha vida.»"),
            new LendaVM("Zinedine Zidane", "franca", "🇫🇷", "Meia-atacante", "1994–2006",
                "Campeão Mundial (1998) · UEFA Euro (2000)",
                "Eliminou Argentina em 2006 com exibição magistral",
                "«Quem nunca falhou nunca tentou nada grandioso.»")
        );
        putRivalidade(m, "franca", "argentina", fraArg);

        return Collections.unmodifiableMap(m);
    }

    private static void putRivalidade(Map<String, List<LendaVM>> m,
                                       String s1, String s2, List<LendaVM> lendas) {
        m.put(s1 + "×" + s2, lendas);
        m.put(s2 + "×" + s1, lendas);
    }

    private List<LendaVM> buildLendas(String slug1, String slug2) {
        String key = slug1 + "×" + slug2;
        return LENDAS_POR_RIVALIDADE.getOrDefault(key, List.of());
    }

    private String resolveSlug(String englishName) {
        if (englishName == null) return null;
        return ENGLISH_TO_SLUG.get(englishName.toLowerCase(Locale.ROOT));
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
        // Lookup direto
        TeamMeta meta = TEAMS.get(englishName);
        if (meta != null) return meta;
        // Lookup via alias (ex: "Korea Republic" → slug "coreia-do-sul" → TEAMS entry)
        String slug = ENGLISH_TO_SLUG.get(englishName.toLowerCase(Locale.ROOT));
        if (slug != null) {
            for (TeamMeta candidate : TEAMS.values()) {
                if (candidate.slug().equals(slug)) return candidate;
            }
        }
        // Fallback com slug gerado a partir do nome
        return new TeamMeta(englishName,
                englishName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-"),
                "🏳", "—", "—", 0);
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
