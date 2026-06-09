package br.com.lectek.copainsider.application.copa;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static br.com.lectek.copainsider.application.copa.PartidaVM.StatusPartida.*;

@Service
public class Copa2026DataService {

    private final List<SelecaoVM> selecoes;
    private final List<PartidaVM> partidas;
    private final List<JogadorVM> jogadores;
    private final List<RivalidadeVM> rivalidades;

    public Copa2026DataService() {
        this.selecoes = buildSelecoes();
        this.partidas = buildPartidas();
        this.jogadores = buildJogadores();
        this.rivalidades = buildRivalidades();
    }

    // ── Seleções ────────────────────────────────────────────────────────────

    public List<SelecaoVM> listSelecoes() { return selecoes; }

    public Optional<SelecaoVM> findSelecao(String slug) {
        return selecoes.stream().filter(s -> s.slug().equals(slug)).findFirst();
    }

    public Map<String, List<SelecaoVM>> selecoesPorGrupo() {
        return selecoes.stream().collect(Collectors.groupingBy(
                SelecaoVM::grupo, TreeMap::new, Collectors.toList()));
    }

    // ── Partidas ─────────────────────────────────────────────────────────────

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

    // ── Jogadores ────────────────────────────────────────────────────────────

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

    // ── Rivalidades ──────────────────────────────────────────────────────────

    public List<RivalidadeVM> listRivalidades() { return rivalidades; }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA
    // ─────────────────────────────────────────────────────────────────────────

    private List<SelecaoVM> buildSelecoes() {
        return List.of(
            // Grupo A
            s("México",        "mexico",        "🇲🇽", "A", "CONCACAF", "3º lugar (1970, 1986)", 17),
            s("Jamaica",       "jamaica",       "🇯🇲", "A", "CONCACAF", "Fase de grupos", 1),
            s("Venezuela",     "venezuela",     "🇻🇪", "A", "CONMEBOL", "Estreia", 0),
            s("Equador",       "equador",       "🇪🇨", "A", "CONMEBOL", "Quartos (2006)", 4),
            // Grupo B
            s("EUA",           "eua",           "🇺🇸", "B", "CONCACAF", "3º lugar (1930)", 11),
            s("Canadá",        "canada",        "🇨🇦", "B", "CONCACAF", "Fase de grupos", 2),
            s("Panamá",        "panama",        "🇵🇦", "B", "CONCACAF", "Fase de grupos", 2),
            s("Bolívia",       "bolivia",       "🇧🇴", "B", "CONMEBOL", "Fase de grupos", 3),
            // Grupo C
            s("Uruguai",       "uruguai",       "🇺🇾", "C", "CONMEBOL", "Campeão (1930, 1950)", 14),
            s("Paraguai",      "paraguai",      "🇵🇾", "C", "CONMEBOL", "Quartos (2010)", 9),
            s("Peru",          "peru",          "🇵🇪", "C", "CONMEBOL", "3º lugar (1975)", 5),
            s("Rep. Centro-Africana", "rep-centro-africana", "🇨🇫", "C", "CAF", "Estreia", 0),
            // Grupo D
            s("Brasil",        "brasil",        "🇧🇷", "D", "CONMEBOL", "Campeão (1958,62,70,94,02)", 22),
            s("Chile",         "chile",         "🇨🇱", "D", "CONMEBOL", "3º lugar (1962)", 10),
            s("Marrocos",      "marrocos",      "🇲🇦", "D", "CAF",      "4º lugar (2022)", 10),
            s("Croácia",       "croacia",       "🇭🇷", "D", "UEFA",     "Vice-campeão (2018)", 8),
            // Grupo E
            s("Argentina",     "argentina",     "🇦🇷", "E", "CONMEBOL", "Campeão (1978,86,2022)", 18),
            s("Colômbia",      "colombia",      "🇨🇴", "E", "CONMEBOL", "Quartos (2014)", 7),
            s("Costa Rica",    "costa-rica",    "🇨🇷", "E", "CONCACAF", "Quartos (2014)", 7),
            s("Guatemala",     "guatemala",     "🇬🇹", "E", "CONCACAF", "Fase de grupos", 1),
            // Grupo F
            s("Portugal",      "portugal",      "🇵🇹", "F", "UEFA",     "3º lugar (1966, 2006)", 9),
            s("Espanha",       "espanha",       "🇪🇸", "F", "UEFA",     "Campeã (2010)", 16),
            s("Suíça",         "suica",         "🇨🇭", "F", "UEFA",     "Quartos (1934, 1938, 1954)", 12),
            s("Camarões",      "camaroes",      "🇨🇲", "F", "CAF",      "Fase de grupos", 8),
            // Grupo G
            s("França",        "franca",        "🇫🇷", "G", "UEFA",     "Campeã (1998, 2018)", 16),
            s("Polónia",       "polonia",       "🇵🇱", "G", "UEFA",     "3º lugar (1974, 1982)", 9),
            s("Senegal",       "senegal",       "🇸🇳", "G", "CAF",      "Quartos (2002)", 4),
            s("Arábia Saudita","arabia-saudita","🇸🇦", "G", "AFC",      "Fase de grupos", 6),
            // Grupo H
            s("Alemanha",      "alemanha",      "🇩🇪", "H", "UEFA",     "Campeã (1954,74,90,2014)", 20),
            s("Japão",         "japao",         "🇯🇵", "H", "AFC",      "Oitavos (2022)", 7),
            s("Austrália",     "australia",     "🇦🇺", "H", "AFC",      "4º lugar (1974, 2006)", 6),
            s("Argélia",       "argelia",       "🇩🇿", "H", "CAF",      "Oitavos (2014)", 4),
            // Grupo I
            s("Países Baixos", "paises-baixos", "🇳🇱", "I", "UEFA",     "Vice (1974, 1978, 2010)", 11),
            s("Turquia",       "turquia",       "🇹🇷", "I", "UEFA",     "3º lugar (2002)", 3),
            s("Costa do Marfim","costa-do-marfim","🇨🇮","I","CAF",     "Fase de grupos", 3),
            s("Coreia do Sul", "coreia-do-sul", "🇰🇷", "I", "AFC",      "4º lugar (2002)", 11),
            // Grupo J
            s("Inglaterra",    "inglaterra",    "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "J", "UEFA", "Campeã (1966)", 17),
            s("Irão",          "ira",           "🇮🇷", "J", "AFC",      "Fase de grupos", 6),
            s("Nigéria",       "nigeria",       "🇳🇬", "J", "CAF",      "Oitavos (1994, 1998, 2014)", 6),
            s("Honduras",      "honduras",      "🇭🇳", "J", "CONCACAF", "Fase de grupos", 3),
            // Grupo K
            s("Bélgica",       "belgica",       "🇧🇪", "K", "UEFA",     "3º lugar (2018)", 14),
            s("Escócia",       "escocia",       "🏴󠁧󠁢󠁳󠁣󠁴󠁿","K","UEFA","Fase de grupos", 8),
            s("Marfim",        "ghana",         "🇬🇭", "K", "CAF",      "Quartos (2010)", 4),
            s("Nova Zelândia", "nova-zelandia", "🇳🇿", "K", "OFC",      "Fase de grupos", 3),
            // Grupo L
            s("Portugal B",    "egito",         "🇪🇬", "L", "CAF",      "Fase de grupos", 3),
            s("México B",      "arabia-saudita-2","🇿🇦","L","CAF",    "Fase de grupos", 3),
            s("Qatar",         "qatar",         "🇶🇦", "L", "AFC",      "Fase de grupos (2022)", 1),
            s("Iraque",        "iraque",        "🇮🇶", "L", "AFC",      "Fase de grupos", 1)
        );
    }

    private SelecaoVM s(String nome, String slug, String bandeira, String grupo,
                        String conf, String melhorResultado, int participacoes) {
        return new SelecaoVM(nome, slug, bandeira, grupo, conf, melhorResultado, participacoes);
    }

    private List<PartidaVM> buildPartidas() {
        List<PartidaVM> list = new ArrayList<>();
        long id = 1;

        // Grupo A
        list.add(p(id++, "México","mexico","🇲🇽","Jamaica","jamaica","🇯🇲",
                ldt(2026,6,11,16,0),"Fase de grupos","A",2,0,ENCERRADA,"Estadio Azteca","Cidade do México"));
        list.add(p(id++, "Venezuela","venezuela","🇻🇪","Equador","equador","🇪🇨",
                ldt(2026,6,11,19,0),"Fase de grupos","A",1,1,ENCERRADA,"AT&T Stadium","Dallas"));
        list.add(p(id++, "México","mexico","🇲🇽","Venezuela","venezuela","🇻🇪",
                ldt(2026,6,15,16,0),"Fase de grupos","A",null,null,AGENDADA,"Estadio Azteca","Cidade do México"));
        list.add(p(id++, "Equador","equador","🇪🇨","Jamaica","jamaica","🇯🇲",
                ldt(2026,6,15,19,0),"Fase de grupos","A",null,null,AGENDADA,"NRG Stadium","Houston"));
        list.add(p(id++, "Equador","equador","🇪🇨","México","mexico","🇲🇽",
                ldt(2026,6,19,20,0),"Fase de grupos","A",null,null,AGENDADA,"AT&T Stadium","Dallas"));
        list.add(p(id++, "Jamaica","jamaica","🇯🇲","Venezuela","venezuela","🇻🇪",
                ldt(2026,6,19,20,0),"Fase de grupos","A",null,null,AGENDADA,"Estadio Azteca","Cidade do México"));

        // Grupo B
        list.add(p(id++, "EUA","eua","🇺🇸","Canadá","canada","🇨🇦",
                ldt(2026,6,12,17,0),"Fase de grupos","B",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Panamá","panama","🇵🇦","Bolívia","bolivia","🇧🇴",
                ldt(2026,6,12,20,0),"Fase de grupos","B",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "EUA","eua","🇺🇸","Panamá","panama","🇵🇦",
                ldt(2026,6,16,17,0),"Fase de grupos","B",null,null,AGENDADA,"Levi's Stadium","São Francisco"));
        list.add(p(id++, "Canadá","canada","🇨🇦","Bolívia","bolivia","🇧🇴",
                ldt(2026,6,16,20,0),"Fase de grupos","B",null,null,AGENDADA,"BC Place","Vancouver"));
        list.add(p(id++, "Canadá","canada","🇨🇦","Panamá","panama","🇵🇦",
                ldt(2026,6,20,20,0),"Fase de grupos","B",null,null,AGENDADA,"BMO Field","Toronto"));
        list.add(p(id++, "Bolívia","bolivia","🇧🇴","EUA","eua","🇺🇸",
                ldt(2026,6,20,20,0),"Fase de grupos","B",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));

        // Grupo D — Brasil
        list.add(p(id++, "Brasil","brasil","🇧🇷","Chile","chile","🇨🇱",
                ldt(2026,6,13,20,0),"Fase de grupos","D",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "Marrocos","marrocos","🇲🇦","Croácia","croacia","🇭🇷",
                ldt(2026,6,13,17,0),"Fase de grupos","D",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Brasil","brasil","🇧🇷","Marrocos","marrocos","🇲🇦",
                ldt(2026,6,17,20,0),"Fase de grupos","D",null,null,AGENDADA,"AT&T Stadium","Dallas"));
        list.add(p(id++, "Croácia","croacia","🇭🇷","Chile","chile","🇨🇱",
                ldt(2026,6,17,17,0),"Fase de grupos","D",null,null,AGENDADA,"Levi's Stadium","São Francisco"));
        list.add(p(id++, "Brasil","brasil","🇧🇷","Croácia","croacia","🇭🇷",
                ldt(2026,6,21,20,0),"Fase de grupos","D",null,null,AGENDADA,"Hard Rock Stadium","Miami"));
        list.add(p(id++, "Chile","chile","🇨🇱","Marrocos","marrocos","🇲🇦",
                ldt(2026,6,21,20,0),"Fase de grupos","D",null,null,AGENDADA,"NRG Stadium","Houston"));

        // Grupo E — Argentina
        list.add(p(id++, "Argentina","argentina","🇦🇷","Colômbia","colombia","🇨🇴",
                ldt(2026,6,14,20,0),"Fase de grupos","E",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Costa Rica","costa-rica","🇨🇷","Guatemala","guatemala","🇬🇹",
                ldt(2026,6,14,17,0),"Fase de grupos","E",null,null,AGENDADA,"Estadio Azteca","Cidade do México"));
        list.add(p(id++, "Argentina","argentina","🇦🇷","Costa Rica","costa-rica","🇨🇷",
                ldt(2026,6,18,20,0),"Fase de grupos","E",null,null,AGENDADA,"Hard Rock Stadium","Miami"));
        list.add(p(id++, "Colômbia","colombia","🇨🇴","Guatemala","guatemala","🇬🇹",
                ldt(2026,6,18,17,0),"Fase de grupos","E",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "Argentina","argentina","🇦🇷","Guatemala","guatemala","🇬🇹",
                ldt(2026,6,22,20,0),"Fase de grupos","E",null,null,AGENDADA,"AT&T Stadium","Dallas"));
        list.add(p(id++, "Colômbia","colombia","🇨🇴","Costa Rica","costa-rica","🇨🇷",
                ldt(2026,6,22,20,0),"Fase de grupos","E",null,null,AGENDADA,"NRG Stadium","Houston"));

        // Grupo F — Portugal + Espanha
        list.add(p(id++, "Portugal","portugal","🇵🇹","Camarões","camaroes","🇨🇲",
                ldt(2026,6,14,14,0),"Fase de grupos","F",null,null,AGENDADA,"Levi's Stadium","São Francisco"));
        list.add(p(id++, "Espanha","espanha","🇪🇸","Suíça","suica","🇨🇭",
                ldt(2026,6,14,11,0),"Fase de grupos","F",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "Portugal","portugal","🇵🇹","Suíça","suica","🇨🇭",
                ldt(2026,6,18,14,0),"Fase de grupos","F",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Espanha","espanha","🇪🇸","Camarões","camaroes","🇨🇲",
                ldt(2026,6,18,11,0),"Fase de grupos","F",null,null,AGENDADA,"Hard Rock Stadium","Miami"));
        list.add(p(id++, "Espanha","espanha","🇪🇸","Portugal","portugal","🇵🇹",
                ldt(2026,6,22,14,0),"Fase de grupos","F",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Suíça","suica","🇨🇭","Camarões","camaroes","🇨🇲",
                ldt(2026,6,22,11,0),"Fase de grupos","F",null,null,AGENDADA,"AT&T Stadium","Dallas"));

        // Grupo G — França
        list.add(p(id++, "França","franca","🇫🇷","Polónia","polonia","🇵🇱",
                ldt(2026,6,15,17,0),"Fase de grupos","G",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Senegal","senegal","🇸🇳","Arábia Saudita","arabia-saudita","🇸🇦",
                ldt(2026,6,15,20,0),"Fase de grupos","G",null,null,AGENDADA,"BC Place","Vancouver"));
        list.add(p(id++, "França","franca","🇫🇷","Senegal","senegal","🇸🇳",
                ldt(2026,6,19,17,0),"Fase de grupos","G",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "Polónia","polonia","🇵🇱","Arábia Saudita","arabia-saudita","🇸🇦",
                ldt(2026,6,19,14,0),"Fase de grupos","G",null,null,AGENDADA,"Levi's Stadium","São Francisco"));
        list.add(p(id++, "França","franca","🇫🇷","Arábia Saudita","arabia-saudita","🇸🇦",
                ldt(2026,6,23,17,0),"Fase de grupos","G",null,null,AGENDADA,"Hard Rock Stadium","Miami"));
        list.add(p(id++, "Polónia","polonia","🇵🇱","Senegal","senegal","🇸🇳",
                ldt(2026,6,23,14,0),"Fase de grupos","G",null,null,AGENDADA,"AT&T Stadium","Dallas"));

        // Grupo H — Alemanha
        list.add(p(id++, "Alemanha","alemanha","🇩🇪","Japão","japao","🇯🇵",
                ldt(2026,6,15,14,0),"Fase de grupos","H",null,null,AGENDADA,"Levi's Stadium","São Francisco"));
        list.add(p(id++, "Austrália","australia","🇦🇺","Argélia","argelia","🇩🇿",
                ldt(2026,6,15,11,0),"Fase de grupos","H",null,null,AGENDADA,"BC Place","Vancouver"));
        list.add(p(id++, "Alemanha","alemanha","🇩🇪","Austrália","australia","🇦🇺",
                ldt(2026,6,19,14,0),"Fase de grupos","H",null,null,AGENDADA,"AT&T Stadium","Dallas"));
        list.add(p(id++, "Japão","japao","🇯🇵","Argélia","argelia","🇩🇿",
                ldt(2026,6,19,11,0),"Fase de grupos","H",null,null,AGENDADA,"SoFi Stadium","Los Angeles"));
        list.add(p(id++, "Alemanha","alemanha","🇩🇪","Argélia","argelia","🇩🇿",
                ldt(2026,6,23,14,0),"Fase de grupos","H",null,null,AGENDADA,"MetLife Stadium","Nova Iorque"));
        list.add(p(id++, "Japão","japao","🇯🇵","Austrália","australia","🇦🇺",
                ldt(2026,6,23,11,0),"Fase de grupos","H",null,null,AGENDADA,"NRG Stadium","Houston"));

        return Collections.unmodifiableList(list);
    }

    private PartidaVM p(long id, String nomeCasa, String slugCasa, String bandeiraCasa,
                        String nomeVisitante, String slugVisitante, String bandeiraVisitante,
                        LocalDateTime dataHora, String fase, String grupo,
                        Integer gC, Integer gV, PartidaVM.StatusPartida status,
                        String estadio, String cidade) {
        return new PartidaVM(id, nomeCasa, slugCasa, bandeiraCasa,
                nomeVisitante, slugVisitante, bandeiraVisitante,
                dataHora, fase, grupo, gC, gV, status, estadio, cidade);
    }

    private LocalDateTime ldt(int y, int m, int d, int h, int min) {
        return LocalDateTime.of(y, m, d, h, min);
    }

    private List<JogadorVM> buildJogadores() {
        return List.of(
            new JogadorVM(1L,"Vinicius Jr.","Brasil","brasil","🇧🇷","Avançado","Real Madrid",24,4,3,4,9.1,""),
            new JogadorVM(2L,"Kylian Mbappé","França","franca","🇫🇷","Avançado","Real Madrid",27,3,2,3,8.8,""),
            new JogadorVM(3L,"Erling Haaland","Noruega","noruega","🇳🇴","Avançado","Man City",25,5,1,3,9.0,""),
            new JogadorVM(4L,"Lionel Messi","Argentina","argentina","🇦🇷","Médio","Inter Miami",38,2,4,4,8.7,""),
            new JogadorVM(5L,"Cristiano Ronaldo","Portugal","portugal","🇵🇹","Avançado","Al Nassr",41,2,1,4,7.9,""),
            new JogadorVM(6L,"Jude Bellingham","Inglaterra","inglaterra","🏴󠁧󠁢󠁥󠁮󠁧󠁿","Médio","Real Madrid",22,2,3,4,8.6,""),
            new JogadorVM(7L,"Rodri","Espanha","espanha","🇪🇸","Médio","Man City",29,1,4,4,8.9,""),
            new JogadorVM(8L,"Lamine Yamal","Espanha","espanha","🇪🇸","Avançado","Barcelona",18,3,3,4,8.7,""),
            new JogadorVM(9L,"Pedri","Espanha","espanha","🇪🇸","Médio","Barcelona",23,1,3,3,8.4,""),
            new JogadorVM(10L,"Raphinha","Brasil","brasil","🇧🇷","Avançado","Barcelona",28,2,2,4,8.3,"")
        );
    }

    private List<RivalidadeVM> buildRivalidades() {
        return List.of(
            new RivalidadeVM("brasil-argentina","Brasil","brasil","🇧🇷","Argentina","argentina","🇦🇷",
                    40,25,35,"Copa 1990 — Argentina 1-0 Brasil","O clássico sul-americano mais intenso do mundo."),
            new RivalidadeVM("franca-argentina","França","franca","🇫🇷","Argentina","argentina","🇦🇷",
                    7,3,10,"Final 2022 — Argentina 3(4)-3(2) França","A final de Qatar 2022 foi considerada a melhor de todos os tempos."),
            new RivalidadeVM("brasil-franca","Brasil","brasil","🇧🇷","França","franca","🇫🇷",
                    10,6,12,"Semi 2006 — França 1-0 Brasil","Dois dos países mais vitoriosos da Copa do Mundo."),
            new RivalidadeVM("alemanha-brasil","Alemanha","alemanha","🇩🇪","Brasil","brasil","🇧🇷",
                    14,4,7,"Semi 2014 — Alemanha 7-1 Brasil","O Mineirazo. A maior goleada em semi-finais da história."),
            new RivalidadeVM("espanha-portugal","Espanha","espanha","🇪🇸","Portugal","portugal","🇵🇹",
                    16,4,5,"Grupo 2018 — Espanha 3-3 Portugal","Os vizinhos ibéricos que raramente se encontram nas Copas."),
            new RivalidadeVM("argentina-franca","Argentina","argentina","🇦🇷","França","franca","🇫🇷",
                    10,3,7,"Final 2022","Rematch da final de Qatar. Mbappé contra o mundo."),
            new RivalidadeVM("brasil-alemanha2","Brasil","brasil","🇧🇷","Alemanha","alemanha","🇩🇪",
                    7,4,14,"Semi 2014 — 7-1","A cicatriz que ainda dói. Brasil vs Alemanha tem história."),
            new RivalidadeVM("inglaterra-alemanha","Inglaterra","inglaterra","🏴󠁧󠁢󠁥󠁮󠁧󠁿","Alemanha","alemanha","🇩🇪",
                    12,5,8,"Final 1966 — Inglaterra 4-2 Alemanha","A única Copa da Inglaterra. Um clássico europeu histórico.")
        );
    }
}
