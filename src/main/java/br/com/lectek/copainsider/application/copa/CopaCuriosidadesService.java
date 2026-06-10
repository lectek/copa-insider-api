package br.com.lectek.copainsider.application.copa;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CopaCuriosidadesService {

    private final Copa2026DataService dataService;

    public CopaCuriosidadesService(Copa2026DataService dataService) {
        this.dataService = dataService;
    }

    // ── Pool estático de factos curados ──────────────────────────────────────

    private static final List<FatoCurioso> POOL_ESTATICO = List.of(

        // ── Artilheiros históricos ──────────────────────────────────────────
        new FatoCurioso("Artilheiros", "⚽",
            "Miroslav Klose: o maior goleador de todos os tempos",
            "O alemão marcou 16 golos em 4 Copas (2002, 2006, 2010, 2014). Superou Ronaldo Nazário em 2014 e continua imbatível."),

        new FatoCurioso("Artilheiros", "⚽",
            "Ronaldo Nazário: 15 golos pelo Brasil",
            "O Fenómeno marcou 15 golos em 4 Copas, incluindo 8 em 2002 — Copa que ganhou no Japão/Coreia. O maior artilheiro brasileiro da história."),

        new FatoCurioso("Artilheiros", "⚽",
            "Pelé: único tricampeão mundial",
            "Pelé marcou 12 golos em 4 Copas (1958–1970) e é o único jogador a ganhar 3 títulos mundiais. Estreou-se com 17 anos em 1958."),

        new FatoCurioso("Artilheiros", "⚽",
            "Lionel Messi: 13 golos, recorde argentino",
            "Messi superou Gabriel Batistuta (10 golos) e tornou-se o maior artilheiro da Argentina nas Copas. Levantou a taça em 2022 ao 5.º encontro."),

        new FatoCurioso("Artilheiros", "⚽",
            "Just Fontaine: 13 golos numa ÚNICA Copa",
            "O francês marcou 13 golos em apenas 6 jogos na Copa de 1958 na Suécia. Um recorde que resiste há quase 70 anos e parece inalcançável."),

        new FatoCurioso("Artilheiros", "⚽",
            "Gerd Müller: 14 golos em só 13 jogos",
            "O 'Bombardeiro da Nação' alemã tem a melhor média de sempre: 1,07 golos por jogo. Artilheiro em 1970 (10 golos) e campeão em 1974."),

        new FatoCurioso("Artilheiros", "⚽",
            "Eusébio: 9 golos em 1966, artilheiro da Copa",
            "O 'Pantera Negra' de Moçambique foi o goleador de Inglaterra 1966. Portugal ficou em 3.º lugar — o melhor resultado histórico da seleção até 2006."),

        new FatoCurioso("Artilheiros", "⚽",
            "Gary Lineker: 10 golos, artilheiro em 1986",
            "O inglês marcou 10 golos em 2 Copas (1986 e 1990) e foi o artilheiro do México 1986. Nunca recebeu um cartão amarelo em toda a carreira."),

        new FatoCurioso("Artilheiros", "⚽",
            "David Villa: 9 golos, maior artilheiro espanhol",
            "Villa foi o goleador da Espanha campeã em 2010 com 5 golos e terminou com 9 no total — o máximo de qualquer jogador espanhol nas Copas."),

        new FatoCurioso("Artilheiros", "⚽",
            "Javier Hernández: 10 golos, o melhor do México",
            "O 'Chicharito' é o maior artilheiro mexicano na história das Copas com 10 golos. O México nunca passou dos quartos de final."),

        new FatoCurioso("Artilheiros", "⚽",
            "James Rodríguez: artilheiro em 2014 com 6 golos",
            "O colombiano foi a revelação do Brasil 2014. O seu golo de voleio contra o Uruguai foi eleito o melhor golo do torneio — e da última década."),

        new FatoCurioso("Artilheiros", "⚽",
            "Guillermo Stábile: o 1.º artilheiro da história",
            "O argentino marcou 8 golos na Copa inaugural de 1930 no Uruguai. Foi o goleador da primeira Copa do Mundo da história do futebol."),

        // ── Goleiros lendários ──────────────────────────────────────────────
        new FatoCurioso("Goleiros", "🧤",
            "Taffarel: o herói das grandes penalidades em 1994",
            "O goleiro brasileiro defendeu o penálti de Baggio na final de 1994 nos EUA. O Brasil ganhou a sua 4.ª Copa — 24 anos depois de 1970."),

        new FatoCurioso("Goleiros", "🧤",
            "Oliver Kahn: melhor jogador do Mundial em 2002",
            "Kahn foi o único goleiro da história a ganhar o Bola de Ouro numa Copa. Mesmo com a derrota da Alemanha na final, foi o jogador do torneio."),

        new FatoCurioso("Goleiros", "🧤",
            "Gianluigi Buffon: 2 golos sofridos em 7 jogos em 2006",
            "O italiano foi impenetrável no caminho ao título em Berlim 2006. Ficou sem sofrer golos em 5 jogos consecutivos — o melhor de uma época dourada."),

        new FatoCurioso("Goleiros", "🧤",
            "Emiliano Martínez: o herói da final de 2022",
            "O guardião argentino defendeu o penálti de Coman nas grandes penalidades e fez uma defesa crucial contra Muani nos segundos finais da prorrogação."),

        new FatoCurioso("Goleiros", "🧤",
            "Tim Howard: 16 defesas contra a Bélgica em 2014",
            "O guardião americano fez 16 defesas num único jogo contra a Bélgica nos oitavos de final — o recorde histórico numa partida eliminatória da Copa."),

        new FatoCurioso("Goleiros", "🧤",
            "Sepp Maier: o pilar da Alemanha campeã em 1974",
            "Maier foi o goleiro da Alemanha Ocidental no título de 1974 em casa. Jogo com Cruyff e os Países Baixos na final ficou na história do futebol."),

        // ── Recordes absolutos ──────────────────────────────────────────────
        new FatoCurioso("Recordes", "🏆",
            "Brasil: único país em todas as 23 Copas",
            "O Brasil é a única seleção a participar em todas as edições da Copa do Mundo desde 1930 — um recorde de presença absoluta e inalcançável."),

        new FatoCurioso("Recordes", "🏆",
            "Brasil: 5 títulos mundiais, o máximo da história",
            "1958, 1962, 1970, 1994, 2002. Nenhum país ganhou mais Copas. A Alemanha e a Itália estão em 2.º com 4 títulos cada."),

        new FatoCurioso("Recordes", "🏆",
            "Maior goleada: Hungria 10-1 El Salvador (1982)",
            "No Mundial de Espanha, a Hungria bateu El Salvador por 10-1 — a maior goleada da história das Copas. Lázló Kiss marcou hat-trick em 8 minutos."),

        new FatoCurioso("Recordes", "🏆",
            "Jogo com mais golos: Áustria 7-5 Suíça (1954)",
            "A Copa de 1954 na Suíça foi a mais goleadora da história: 5,4 golos por jogo. A vitória austríaca por 7-5 continua imbatível em número de golos."),

        new FatoCurioso("Recordes", "🏆",
            "Golo mais rápido: Hakan Şükür em 11 segundos",
            "O avançado turco marcou 11 segundos após o apito inicial no jogo do 3.º lugar contra a Coreia do Sul em 2002 — o golo mais rápido da história da Copa."),

        new FatoCurioso("Recordes", "🏆",
            "Pelé: mais jovem goleador — 17 anos em 1958",
            "Pelé marcou na final contra a Suécia com apenas 17 anos e 249 dias. Continua a ser o goleador mais jovem numa final de Copa do Mundo."),

        new FatoCurioso("Recordes", "🏆",
            "Roger Milla: mais velho goleador — 42 anos em 1994",
            "O camaronês Roger Milla entrou como suplente e marcou contra a Rússia aos 42 anos e 39 dias. O marcador mais velho da história das Copas."),

        new FatoCurioso("Recordes", "🏆",
            "Países Baixos: 3 finais perdidas sem nunca ganhar",
            "1974, 1978 e 2010 — os holandeses chegaram à final três vezes e perderam sempre. A maior nação a nunca ter ganho uma Copa do Mundo."),

        new FatoCurioso("Recordes", "🏆",
            "Copa 1998: a mais goleadora da era moderna",
            "Com 171 golos em 64 jogos, a Copa de França 1998 tem a maior soma de golos de qualquer edição com formato de 32 equipas."),

        // ── Copa 2026 ───────────────────────────────────────────────────────
        new FatoCurioso("Copa 2026", "🌎",
            "A maior Copa da história: 48 seleções e 104 jogos",
            "2026 é a primeira Copa com 48 participantes — um aumento de 50% face às 32 de 1998 a 2022. São 104 jogos no total, contra 64 anteriores."),

        new FatoCurioso("Copa 2026", "🌎",
            "3 países anfitriões pela primeira vez na história",
            "EUA, Canadá e México acolhem a Copa 2026 em conjunto — a primeira vez que 3 países partilham a organização de uma Copa do Mundo."),

        new FatoCurioso("Copa 2026", "🌎",
            "México sede pela 3.ª vez — recorde único",
            "O México é o único país a acolher a Copa do Mundo 3 vezes: 1970, 1986 e agora 2026. O Estádio Azteca recebe jogos numa Copa pela 3.ª vez."),

        new FatoCurioso("Copa 2026", "🌎",
            "16 grupos de 3 equipas na fase inicial",
            "O novo formato tem 16 grupos de 3 equipas. Os 2 primeiros de cada grupo e os 8 melhores terceiros classificados avançam para os 32 avos de final."),

        new FatoCurioso("Copa 2026", "🌎",
            "A Copa mais cara de sempre: 11 estádios nos EUA",
            "Os EUA recebem 11 das 16 cidades anfitriãs, incluindo Nova Iorque, Los Angeles, Dallas e Miami. Prevê-se o maior número de espectadores da história."),

        // ── Curiosidades únicas ─────────────────────────────────────────────
        new FatoCurioso("Curiosidades", "🎭",
            "A final de 2022 foi eleita a melhor de sempre",
            "Argentina 3-3 França com golos nos últimos minutos e grandes penalidades. Mbappé marcou hat-trick na derrota. Considerada a final mais dramática da história."),

        new FatoCurioso("Curiosidades", "🎭",
            "Zidane: expulso na sua última partida profissional",
            "Zidane cabeceou Materazzi na final de 2006 em Berlim e foi expulso. A França perdeu nas grandes penalidades. Foi o último jogo da carreira do francês."),

        new FatoCurioso("Curiosidades", "🎭",
            "O Mineirazo: Alemanha 7-1 Brasil em 2014",
            "A maior goleada da história das semifinais. A Alemanha marcou 5 golos em 18 minutos. O Estádio Mineirão ficou em silêncio. A Alemanha foi campeã."),

        new FatoCurioso("Curiosidades", "🎭",
            "Costa Rica 2014: eliminou Itália, Uruguai e Inglaterra",
            "Considerado o maior feito de um 'pequeno' país: a Costa Rica ganhou o grupo de Uruguai, Itália e Inglaterra — três ex-campeões mundiais."),

        new FatoCurioso("Curiosidades", "🎭",
            "Baggio: o penálti que doeu a toda a Itália em 1994",
            "Roberto Baggio falhou o penálti decisivo na final de 1994 contra o Brasil. A imagem do craque italiano de cabeça baixa ficou para a eternidade."),

        new FatoCurioso("Curiosidades", "🎭",
            "Copa 1950: Uruguai campeão sem jogar uma final",
            "Em 1950 não havia final. O Uruguai jogou a última partida do grupo final contra o Brasil (Maracanazo 2-1) e ganhou o título. A Copa sem final."),

        new FatoCurioso("Curiosidades", "🎭",
            "Ernie Brandts: marcou para as duas equipas no mesmo jogo",
            "Em 1978, o holandês marcou um auto-golo e depois um golo contra a Itália no mesmo jogo. A Holanda ganhou 2-1 e Brandts foi o homem do jogo."),

        new FatoCurioso("Curiosidades", "🎭",
            "Maradona: o golo do século e a mão de Deus",
            "Em 1986 contra a Inglaterra, Maradona marcou com a mão (reconhecido depois) e depois fez o 'Golo do Século' — o melhor golo da história votado pela FIFA.")
    );

    // ── Factos dinâmicos computados do histórico ──────────────────────────────

    private List<FatoCurioso> computarFatosHistoricos() {
        List<FatoCurioso> resultado = new ArrayList<>();
        try {
            Map<String, int[]> stats = new HashMap<>(); // slug → [vitorias, golosMarcados, golosSofridos, jogos]

            for (OpenFootballClient.OFMatch m : dataService.listHistoricalMatches()) {
                if (!m.hasScore()) continue;

                String t1 = slugPara(m.team1());
                String t2 = slugPara(m.team2());
                if (t1 == null || t2 == null) continue;

                stats.computeIfAbsent(t1, k -> new int[4]);
                stats.computeIfAbsent(t2, k -> new int[4]);

                int s1 = m.scoreHome(), s2 = m.scoreAway();
                stats.get(t1)[1] += s1; stats.get(t1)[2] += s2; stats.get(t1)[3]++;
                stats.get(t2)[1] += s2; stats.get(t2)[2] += s1; stats.get(t2)[3]++;
                if (s1 > s2) stats.get(t1)[0]++;
                else if (s2 > s1) stats.get(t2)[0]++;
            }

            if (stats.isEmpty()) return resultado;

            // Mais golos marcados (2006-2022)
            stats.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue()[1]))
                .ifPresent(e -> resultado.add(new FatoCurioso("Estatísticas", "📊",
                    "Mais golos nas últimas 5 Copas: " + nomeDisplay(e.getKey()),
                    nomeDisplay(e.getKey()) + " marcou " + e.getValue()[1] +
                    " golos entre 2006 e 2022 — o ataque mais produtivo da última geração.",
                    e.getKey())));

            // Melhor defesa (menos golos sofridos por jogo)
            stats.entrySet().stream()
                .filter(e -> e.getValue()[3] >= 5)
                .min(Comparator.comparingDouble(e -> (double) e.getValue()[2] / e.getValue()[3]))
                .ifPresent(e -> {
                    double media = (double) e.getValue()[2] / e.getValue()[3];
                    resultado.add(new FatoCurioso("Estatísticas", "📊",
                        "Melhor defesa (2006-2022): " + nomeDisplay(e.getKey()),
                        nomeDisplay(e.getKey()) + " sofreu apenas " +
                        String.format("%.1f", media) + " golos por jogo nas últimas 5 Copas.",
                        e.getKey()));
                });

            // Maior número de vitórias
            stats.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue()[0]))
                .ifPresent(e -> resultado.add(new FatoCurioso("Estatísticas", "📊",
                    "Mais vitórias nas últimas 5 Copas: " + nomeDisplay(e.getKey()),
                    nomeDisplay(e.getKey()) + " ganhou " + e.getValue()[0] +
                    " jogos entre 2006 e 2022 — a seleção mais consistente da era recente.",
                    e.getKey())));

            // Total de golos nas 5 Copas
            int totalGolos = stats.values().stream().mapToInt(v -> v[1]).sum() / 2;
            resultado.add(new FatoCurioso("Estatísticas", "📊",
                "Total de golos nas últimas 5 Copas: " + totalGolos,
                "Entre 2006 e 2022 foram marcados " + totalGolos +
                " golos em 5 edições da Copa do Mundo — uma média de " +
                String.format("%.0f", totalGolos / 5.0) + " golos por Copa."));

        } catch (Exception e) {
            // dados históricos ainda a carregar — retorna o que tiver
        }
        return resultado;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public List<FatoCurioso> getFatos(int quantidade) {
        List<FatoCurioso> pool = new ArrayList<>(POOL_ESTATICO);
        pool.addAll(computarFatosHistoricos());

        Collections.shuffle(pool);
        return pool.stream().limit(quantidade).toList();
    }

    public List<FatoCurioso> getFatosPorCategoria(String categoria) {
        List<FatoCurioso> pool = new ArrayList<>(POOL_ESTATICO);
        pool.addAll(computarFatosHistoricos());
        return pool.stream()
                .filter(f -> f.categoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public List<String> getCategorias() {
        return POOL_ESTATICO.stream()
                .map(FatoCurioso::categoria)
                .distinct()
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final Map<String, String> SLUG_TO_NOME = Map.ofEntries(
        Map.entry("brasil",       "Brasil"),
        Map.entry("alemanha",     "Alemanha"),
        Map.entry("franca",       "França"),
        Map.entry("argentina",    "Argentina"),
        Map.entry("espanha",      "Espanha"),
        Map.entry("portugal",     "Portugal"),
        Map.entry("italia",       "Itália"),
        Map.entry("inglaterra",   "Inglaterra"),
        Map.entry("paises-baixos","Países Baixos"),
        Map.entry("eua",          "EUA"),
        Map.entry("uruguai",      "Uruguai"),
        Map.entry("croacia",      "Croácia"),
        Map.entry("belgica",      "Bélgica"),
        Map.entry("marrocos",     "Marrocos")
    );

    private static final Map<String, String> ENGLISH_TO_SLUG = Map.ofEntries(
        Map.entry("brazil",           "brasil"),
        Map.entry("germany",          "alemanha"),
        Map.entry("france",           "franca"),
        Map.entry("argentina",        "argentina"),
        Map.entry("spain",            "espanha"),
        Map.entry("portugal",         "portugal"),
        Map.entry("italy",            "italia"),
        Map.entry("england",          "inglaterra"),
        Map.entry("netherlands",      "paises-baixos"),
        Map.entry("usa",              "eua"),
        Map.entry("united states",    "eua"),
        Map.entry("uruguay",          "uruguai"),
        Map.entry("croatia",          "croacia"),
        Map.entry("belgium",          "belgica"),
        Map.entry("morocco",          "marrocos"),
        Map.entry("west germany",     "alemanha")
    );

    private String slugPara(String englishName) {
        if (englishName == null) return null;
        return ENGLISH_TO_SLUG.get(englishName.toLowerCase(java.util.Locale.ROOT));
    }

    private String nomeDisplay(String slug) {
        return SLUG_TO_NOME.getOrDefault(slug, slug);
    }
}
