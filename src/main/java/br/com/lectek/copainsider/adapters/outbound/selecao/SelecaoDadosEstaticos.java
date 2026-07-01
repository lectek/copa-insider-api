package br.com.lectek.copainsider.adapters.outbound.selecao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelecaoDadosEstaticos {

    private static final Map<String, SelecaoEstatica> DADOS = new HashMap<>();

    static {

        // ── BRASIL ─────────────────────────────────────────────────────────────
        DADOS.put("BRA", new SelecaoEstatica(
            "A Canarinha", "CONMEBOL", 22,
            List.of(1958, 1962, 1970, 1994, 2002), null,
            "Argentina", "ofensivo",
            "O Brasil é a única seleção a ter disputado todas as 22 edições da Copa do Mundo. " +
            "Em 5 delas, voltou para casa com a taça — mais do que qualquer outra nação da história.",
            "Não existe pressão maior do que vestir a camisa do Brasil. É responsabilidade, é história, é a nação inteira nas tuas costas.",
            "Ronaldo Fenômeno", false,
            List.of("Pelé", "Garrincha", "Zico", "Ronaldo", "Ronaldinho Gaúcho", "Cafu", "Roberto Carlos"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Final Perfeita — Pelé com 17 Anos", "Suécia", "5-2", "1958",
                    "No Rasunda Estadion, em Solna, o Brasil disputou a sua primeira final de Copa do Mundo. " +
                    "Com Pelé a apenas 17 anos, a Canarinha protagonizou um dos jogos mais belos da história. " +
                    "Pelé marcou dois golos extraordinários na segunda parte, Vavá bisou e Zagalo fechou a contagem. " +
                    "O Brasil tornava-se campeão do mundo pela primeira vez, com um futebol tão alegre que até o público sueco aplaudiu de pé."
                ),
                new SelecaoEstatica.PartidaRef(
                    "O Mineirazo — A Noite Mais Negra", "Alemanha", "1-7", "2014",
                    "No Estádio Mineirão, em Belo Horizonte, a seleção anfitriã sofreu a maior humilhação da sua história. " +
                    "Sem Neymar e sem Thiago Silva, suspensos, o Brasil desmoronou em campo. " +
                    "A Alemanha marcou 5 golos nos primeiros 29 minutos. " +
                    "O silêncio no estádio — interrompido apenas pelos soluços dos adeptos — ficou gravado para sempre na memória do futebol mundial."
                ),
                new SelecaoEstatica.PartidaRef(
                    "O Maracanazo — A Dor que Nunca Passa", "Uruguai", "1-2", "1950",
                    "Com quase 200.000 espetadores — o maior público da história do futebol — o Maracanã aguardava o Brasil campeão. " +
                    "Bastava um empate. O Uruguai tinha outras ideias. " +
                    "Ghiggia marcou o 2-1 aos 79 minutos, e o maior silêncio da história do futebol tombou sobre o Rio de Janeiro. " +
                    "O jornalista Nélson Rodrigues chamaria a este momento 'a nossa Hiroshima'."
                )
            )
            , "Carlo Ancelotti", "O treinador italiano trouxe uma gestao mais flexivel ao elenco estrelado, com uma equipa mais compacta e bem posicionada taticamente.", "Depois de um inicio dificil na Copa do Mundo de 2026, o Brasil reagiu com duas vitorias seguidas e liderou o Grupo C, com Neymar de volta a selecao apos quase tres anos de ausencia."
        ));

        // ── ARGENTINA ──────────────────────────────────────────────────────────
        DADOS.put("ARG", new SelecaoEstatica(
            "La Albiceleste", "CONMEBOL", 18,
            List.of(1978, 1986, 2022), null,
            "Brasil", "equilibrado",
            "Lionel Messi é o único jogador da história a ter sido eleito Melhor Jogador em duas Copas do Mundo diferentes — " +
            "em 2014 e em 2022. No Qatar, conquistou o único título que ainda lhe faltava.",
            "A minha única obsessão na vida foi ganhar o Mundial com a Argentina. Quando aconteceu, percebi que nada mais seria igual.",
            "Lionel Messi, 2022", false,
            List.of("Diego Maradona", "Lionel Messi", "Gabriel Batistuta", "Mario Kempes", "Alfredo Di Stéfano"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Génio e a Mão de Deus", "Inglaterra", "2-1", "1986",
                    "Nos quartos de final do México, em apenas 90 minutos, Diego Maradona condensou toda a complexidade do futebol. " +
                    "Primeiro marcou com a mão — 'a Mão de Deus', disse ele depois. " +
                    "Depois, numa jogada de 11 segundos e 10 adversários driblados, marcou aquele que seria eleito o Golo do Século. " +
                    "A Argentina venceu e seguiu para o título. Maradona tinha 25 anos e era, simplesmente, o melhor do mundo."
                ),
                new SelecaoEstatica.PartidaRef(
                    "A Final Mais Dramática da História", "França", "3-3 (p.g.)", "2022",
                    "A Argentina liderava 2-0 aos 80 minutos, caminhava tranquila para o título. " +
                    "Mbappé marcou dois golos em 97 segundos e empatou. Na prorrogação, Messi voltou a marcar. " +
                    "Mbappé empatou novamente — hat-trick histórico. Nos penáltis, Emi Martínez travou o decisivo. " +
                    "Messi, aos 35 anos, chorava abraçado ao troféu. A Argentina era tricampeã do mundo."
                )
            )
            , "Lionel Scaloni", "Scaloni comanda a Argentina ha sete anos com um 4-3-3 fluido, dominio do meio-campo e rotacoes posicionais constantes na construcao de jogo.", "Campea do Mundo em 2022 e bicampea da Copa America, a Argentina chegou a Copa 2026 como numero um do ranking FIFA e venceu o seu grupo com 100% de aproveitamento, com Messi ainda em acao pela selecao."
        ));

        // ── FRANÇA ─────────────────────────────────────────────────────────────
        DADOS.put("FRA", new SelecaoEstatica(
            "Les Bleus", "UEFA", 16,
            List.of(1998, 2018), null,
            "Alemanha", "equilibrado",
            "A equipa de 1998 que ganhou a Copa em casa era composta por jogadores de 17 nacionalidades de origem diferentes — " +
            "um símbolo da França multicultural que celebra o lema 'Black, Blanc, Beur'.",
            "Ganhar o Mundial em Paris, no Stade de France, diante do povo francês... foi o momento mais bonito da minha vida.",
            "Zinedine Zidane, 1998", false,
            List.of("Zinedine Zidane", "Thierry Henry", "Michel Platini", "Kylian Mbappé", "Just Fontaine"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Zidane Abre o Céu Sobre Paris", "Brasil", "3-0", "1998",
                    "No Stade de France, diante de 80.000 espetadores em êxtase, a França recebeu o Brasil pentacampeão na final. " +
                    "Com o Brasil debilitado — Ronaldo sofrera uma crise convulsiva na véspera — Zidane dominou o jogo. " +
                    "Marcou dois golos de cabeça na primeira parte. Petit fechou o marcador. " +
                    "A França tornava-se campeã do mundo pela primeira vez, com Paris a explodir de alegria."
                ),
                new SelecaoEstatica.PartidaRef(
                    "Mbappé Escreve o Futuro", "Croácia", "4-2", "2018",
                    "Na final de Moscovo, a França confirmou a sua segunda estrela. " +
                    "Mbappé, com apenas 19 anos, marcou o quarto golo e tornou-se o segundo jogador mais jovem da história a marcar numa final de Copa — atrás apenas de Pelé em 1958. " +
                    "Uma geração de talentos extraordinários, liderada por Mbappé e Griezmann, mostrou que o futuro do futebol francês não podia ser mais brilhante."
                )
            )
            , "Didier Deschamps", "Depois de catorze anos no comando — este e o seu ultimo torneio a frente da selecao — Deschamps constroi uma estrutura solida e pragmatica em torno de Mbappe.", "A Franca venceu o seu grupo de apuramento com apenas um golo sofrido em seis jogos, liderada por Kylian Mbappe, agora capitao e melhor marcador da historia francesa."
        ));

        // ── ALEMANHA ───────────────────────────────────────────────────────────
        DADOS.put("ALE", new SelecaoEstatica(
            "Die Mannschaft", "UEFA", 20,
            List.of(1954, 1974, 1990, 2014), null,
            "Brasil", "defensivo",
            "A Alemanha é a única seleção europeia a ter vencido uma Copa do Mundo no continente americano — " +
            "em 2014, no Brasil, após marcar 7-1 ao anfitrião nas meias-finais.",
            "No futebol, a organização e a mentalidade vencedora são tão importantes como o talento. Sem disciplina, o talento não chega.",
            "Franz Beckenbauer", false,
            List.of("Franz Beckenbauer", "Gerd Müller", "Lothar Matthäus", "Miroslav Klose", "Oliver Kahn"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Milagre de Berna", "Hungria", "3-2", "1954",
                    "A Hungria de 1954 era a melhor equipa do mundo e não perdia há quatro anos. " +
                    "Tinha goleado a Alemanha por 8-3 na fase de grupos. Na final, em Berna, liderou por 2-0 ao fim de 8 minutos. " +
                    "O que se seguiu entrou para a história: a Alemanha remontou e venceu por 3-2. " +
                    "O 'Milagre de Berna' é considerado o renascimento da identidade nacional alemã no pós-guerra."
                ),
                new SelecaoEstatica.PartidaRef(
                    "Sete a Um — O Fim de um Mito", "Brasil", "7-1", "2014",
                    "No Mineirão, a Alemanha desmontou o Brasil peça por peça. Cinco golos nos primeiros 29 minutos. " +
                    "Klose marcou o seu 16.º golo em Mundiais, tornando-se o maior artilheiro da história da competição. " +
                    "O estádio, que horas antes cantava o Brasil campeão, caiu num silêncio de incredulidade. " +
                    "A Alemanha venceu a final e conquistou o seu quarto título, com golo de Götze na prorrogação."
                )
            )
            , "Julian Nagelsmann", "Aos 38 anos, o mais jovem selecionador do torneio aposta num 4-2-3-1 com contrapressao agressiva e transicoes rapidas — o gegenpressing tipico do futebol alemao moderno.", "A Alemanha apurou-se como lider do seu grupo, com 16 golos marcados e apenas tres sofridos, contando ja com Jamal Musiala recuperado de uma lesao grave."
        ));

        // ── INGLATERRA ─────────────────────────────────────────────────────────
        DADOS.put("ENG", new SelecaoEstatica(
            "The Three Lions", "UEFA", 16,
            List.of(1966), null,
            "Alemanha", "equilibrado",
            "A Inglaterra inventou as regras modernas do futebol em 1863 e ganhou o Mundial uma única vez — " +
            "em 1966, em Wembley, no seu próprio país. Desde então, a espera dura há mais de seis décadas.",
            "Some people think football is a matter of life and death. I assure you, it is much more serious than that.",
            "Bill Shankly", false,
            List.of("Bobby Moore", "Bobby Charlton", "Wayne Rooney", "Gary Lineker", "Gordon Banks"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "It's Coming Home — A Final de 1966", "Alemanha Ocidental", "4-2", "1966",
                    "Em Wembley, com 96.924 espetadores, a Inglaterra realizou o sonho perante os seus adeptos. " +
                    "Geoff Hurst marcou o único hat-trick da história das finais mundiais — " +
                    "incluindo o controverso terceiro golo que o árbitro de linha confirmou ter cruzado a linha. " +
                    "Bobby Moore levantou a Jules Rimet. A Inglaterra jamais voltaria tão perto do topo."
                )
            )
            , "Thomas Tuchel", "Mais flexivel do que o antecessor, Tuchel alterna entre defesa a tres e a quatro, ajustando o bloco defensivo consoante o adversario.", "A Inglaterra teve um apuramento perfeito — sem perder nem sofrer um unico golo — e goleou a Croacia por 4-2 na estreia da Copa 2026, com Harry Kane a liderar como capitao e maior goleador da historia da selecao."
        ));

        // ── ITÁLIA ─────────────────────────────────────────────────────────────
        DADOS.put("ITA", new SelecaoEstatica(
            "Gli Azzurri", "UEFA", 18,
            List.of(1934, 1938, 1982, 2006), null,
            "Brasil", "defensivo",
            "A Itália é a única seleção a ter vencido o Mundial em dois anos consecutivos — 1934 e 1938. " +
            "Quatro títulos no total fazem dos Azzurri a seleção europeia mais vencedora da história.",
            "Il calcio è il gioco più bello del mondo, e noi gli Azzurri siamo i suoi custodi.",
            "Paolo Maldini", false,
            List.of("Paolo Maldini", "Roberto Baggio", "Gianluigi Buffon", "Giuseppe Meazza", "Paolo Rossi"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Tardelli e o Grito Eterno", "Alemanha Ocidental", "3-1", "1982",
                    "No Santiago Bernabéu, em Madrid, a Itália de Paolo Rossi conquistou o seu terceiro título mundial. " +
                    "Rossi — que voltara de dois anos de suspensão — foi o herói com seis golos no torneio. " +
                    "Tardelli marcou o segundo golo e soltou aquele grito que ficou para sempre: braços abertos, boca aberta, lágrimas nos olhos. " +
                    "Italia campione del mondo."
                ),
                new SelecaoEstatica.PartidaRef(
                    "O Penálti de Baggio — A Imagem de uma Geração", "Brasil", "0-0 (p.g.)", "1994",
                    "Na primeira final da história decidida nos penáltis, Roberto Baggio caminhou para a marca dos 11 metros. " +
                    "O chuto saiu alto, por cima da barra. O Brasil era campeão. " +
                    "Baggio ficou sozinho no campo, de cabeça baixa, numa das imagens mais icónicas da história do desporto. " +
                    "Dez anos depois, admitiu que ainda sonhava com aquele penálti."
                )
            )
            , null, "Sem treinador efetivo desde abril de 2026, a federacao italiana procura ainda o sucessor ideal para reconstruir a selecao a partir do zero.", "Pela terceira vez consecutiva, a Italia falhou o apuramento para a fase final — eliminada nos play-offs pela Bosnia — deixando de fora do Mundial 2026 uma das selecoes mais vencedoras da historia."
        ));

        // ── URUGUAI ────────────────────────────────────────────────────────────
        DADOS.put("URU", new SelecaoEstatica(
            "La Celeste", "CONMEBOL", 14,
            List.of(1930, 1950), null,
            "Brasil", "defensivo",
            "O Uruguai tem a maior proporção de títulos mundiais per capita de qualquer seleção: " +
            "2 títulos para um país de apenas 3,5 milhões de habitantes. Nenhuma outra nação se aproxima deste registo.",
            "Somos campeones del mundo y nadie nos lo va a quitar.",
            "Obdulio Varela, 1950", false,
            List.of("Obdulio Varela", "Alcides Ghiggia", "Luis Suárez", "Diego Forlán", "Enzo Francescoli"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Maracanazo — David Derruba Golias", "Brasil", "2-1", "1950",
                    "Com quase 200.000 espetadores no Maracanã, o Brasil precisava apenas de um empate para ser campeão. " +
                    "O Uruguai entrou como azarão. Friaça abriu o marcador para o Brasil. " +
                    "Schiaffino empatou e Ghiggia, aos 79 minutos, fez o golo que gelou o mundo. " +
                    "O maior silêncio da história do futebol instalou-se no maior estádio do mundo. O Uruguai era bicampeão."
                )
            )
            , "Marcelo Bielsa", "Bielsa mantem a sua assinatura tatica: 4-3-3 com pressao alta, verticalidade extrema e uma preparacao obsessiva em video antes de cada adversario.", "O Uruguai foi eliminado na fase de grupos da Copa 2026, apos empates com Arabia Saudita e Cabo Verde e derrota com Espanha — uma saida precoce que gerou tensao entre Bielsa e o plantel."
        ));

        // ── ESPANHA ────────────────────────────────────────────────────────────
        DADOS.put("ESP", new SelecaoEstatica(
            "La Roja", "UEFA", 16,
            List.of(2010), null,
            "Portugal", "ofensivo",
            "Entre 2008 e 2012, a Espanha ganhou três torneios consecutivos: Euro 2008, Copa do Mundo 2010 e Euro 2012. " +
            "É a única seleção na história do futebol a conseguir esse feito.",
            "El balón tiene que correr más rápido que el hombre.",
            "Xavi Hernández", false,
            List.of("Xavi Hernández", "Andrés Iniesta", "Raúl", "David Villa", "Iker Casillas"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Iniesta Faz Espanha Chorar de Alegria", "Países Baixos", "1-0", "2010",
                    "Numa final disputada em Joanesburgo — 14 cartões amarelos, 1 vermelho — " +
                    "a Espanha e os Países Baixos lutaram durante 120 minutos. " +
                    "A decisão chegou nos últimos segundos da prorrogação: Iniesta recebeu o passe de Cesc e atirou para as redes. " +
                    "Arrancou a camisola e correu em delírio. A Espanha era campeã do mundo pela primeira e única vez."
                )
            )
            , "Luis de la Fuente", "De la Fuente mantem a posse posicional que sempre definiu a Espanha, mas com mais diretismo e amplitude do que as geracoes anteriores, construindo o jogo em 4-3-3.", "Campea da Eurocopa 2024, a Espanha chega a Copa 2026 como uma das grandes favoritas, liderada pela nova geracao de Lamine Yamal e pelo capitao Rodri, Bola de Ouro em titulo."
        ));

        // ── PORTUGAL ───────────────────────────────────────────────────────────
        DADOS.put("POR", new SelecaoEstatica(
            "A Seleção das Quinas", "UEFA", 8,
            List.of(), "3.º Lugar (1966)",
            "Espanha", "equilibrado",
            "Eusébio marcou 9 golos na Copa de 1966 — um recorde que durou 24 anos. " +
            "Numa partida de quartas de final, com Portugal a perder 0-3 para a Coreia do Norte, " +
            "marcou 4 golos e conduziu Portugal à vitória histórica por 5-3.",
            "Ser campeão do mundo é o sonho da minha vida. Nunca desistirei.",
            "Cristiano Ronaldo", false,
            List.of("Eusébio", "Cristiano Ronaldo", "Luís Figo", "Rui Costa", "Fernando Peyroteo"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Eusébio Salva Portugal Sozinho", "Coreia do Norte", "5-3", "1966",
                    "Nos quartos de final de 1966, Portugal encontrou-se a perder 0-3 para a Coreia do Norte ao intervalo. " +
                    "Era a maior surpresa da história do Mundial até então. " +
                    "O que se seguiu foi pura magia de Eusébio: quatro golos na segunda parte. " +
                    "Torres fechou a contagem em 5-3. Portugal chegava às meias-finais do seu melhor Mundial alguma vez."
                )
            )
            , "Roberto Martínez", "O tecnico espanhol mantem a base da equipa mas tem apostado em sangue novo, com foco em qualidade em espacos curtos e controlo da zona central do campo.", "Portugal marcou seis golos e sofreu apenas um na fase de grupos da Copa 2026, mas dois empates deixaram a equipa atras da Colombia — com Cristiano Ronaldo, aos 41 anos, a disputar o seu sexto Mundial."
        ));

        // ── MÉXICO ─────────────────────────────────────────────────────────────
        DADOS.put("MEX", new SelecaoEstatica(
            "El Tri", "CONCACAF", 17,
            List.of(), "Quartas de Final (1970, 1986)",
            "Estados Unidos", "ofensivo",
            "O México alcançou os quartos de final em 7 participações mundiais, mas nunca passou dessa fase — " +
            "fenômeno conhecido pelos adeptos como 'el Quinto Partido': o jogo que nunca chega.",
            "¡Arriba México! El corazón de la selección late con millones de voces.",
            "Hugo Sánchez", true,
            List.of("Hugo Sánchez", "Cuauhtémoc Blanco", "Jorge Campos", "Andrés Guardado", "Javier Hernández"),
            List.of()
            , "Javier Aguirre", "Na sua terceira passagem a frente do Mexico num Mundial, Aguirre aposta num 4-2-3-1 pragmatico, com armadilha de fora de jogo e foco total em nao sofrer golos.", "O Mexico teve a sua melhor fase de grupos de sempre na Copa 2026 — tres vitorias seguidas sem sofrer um unico golo, como anfitriao ao lado dos Estados Unidos e do Canada."
        ));

        // ── ESTADOS UNIDOS ─────────────────────────────────────────────────────
        DADOS.put("USA", new SelecaoEstatica(
            "The Stars and Stripes", "CONCACAF", 11,
            List.of(), "3.º Lugar (1930)",
            "México", "equilibrado",
            "Em 1994, os Estados Unidos organizaram a Copa do Mundo e bateram todos os recordes de público — " +
            "mais de 3,5 milhões de espetadores em 52 jogos. Foi o catalisador do crescimento do futebol no país.",
            "We're not just growing the game here. We're making it our own.",
            "Landon Donovan", true,
            List.of("Landon Donovan", "Clint Dempsey", "Tim Howard", "Christian Pulisic", "Kasey Keller"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Surpresa que Chocou o Mundo", "Portugal", "3-2", "2002",
                    "Nos oitavos de final do Japão/Coreia, os EUA derrotaram Portugal — a seleção de Figo e Rui Costa — por 3-2. " +
                    "O resultado chocou o mundo do futebol. Os americanos seguiram para os quartos de final, " +
                    "onde foram eliminados pela Alemanha. Foi a melhor participação dos EUA desde 1930."
                )
            )
            , "Mauricio Pochettino", "Pochettino promove um futebol de pressao alta e ofensivo, com os laterais avancados a abrir espaco para os extremos jogarem por dentro.", "Anfitria da Copa do Mundo de 2026, a selecao norte-americana entra no torneio com uma mentalidade de candidata, ja nao de azarao, sob o comando do argentino."
        ));

        // ── MARROCOS ───────────────────────────────────────────────────────────
        DADOS.put("MAR", new SelecaoEstatica(
            "Les Lions de l'Atlas", "CAF", 7,
            List.of(), "4.º Lugar (2022)",
            "Argélia", "defensivo",
            "No Qatar 2022, o Marrocos tornou-se a primeira seleção africana e árabe a alcançar as meias-finais " +
            "de uma Copa do Mundo, eliminando no caminho a Espanha e Portugal.",
            "Nous avons fait l'histoire. Pour l'Afrique, pour le monde arabe, pour tous ceux qui ont rêvé.",
            "Achraf Hakimi, 2022", false,
            List.of("Achraf Hakimi", "Hakim Ziyech", "Youssef En-Nesyri", "Ahmed Faras", "Noureddine Naybet"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Geração que Mudou a História de África", "Portugal", "1-0", "2022",
                    "Nos quartos de final do Qatar, o Marrocos enfrentou Portugal e venceu por 1-0 " +
                    "com um golo de En-Nesyri de cabeça. Era a primeira vez que uma seleção africana chegava às meias-finais. " +
                    "O Marrocos tinha eliminado a Espanha nos oitavos e a Bélgica na fase de grupos. " +
                    "Uma geração única mudou a história do futebol africano para sempre."
                )
            )
            , "Mohamed Ouahbi", "Ouahbi herdou de Walid Regragui a identidade defensiva que definiu o Marrocos — bloco medio organizado, 4-3-3, pressao coletiva coordenada.", "Nove sobreviventes do plantel que fez historia nas meias-finais de 2022 continuam no grupo, agora sob um novo treinador nomeado apenas meses antes do Mundial 2026."
        ));

        // ── CANADÁ ─────────────────────────────────────────────────────────────
        DADOS.put("CAN", new SelecaoEstatica(
            "Les Rouges", "CONCACAF", 3,
            List.of(), "Fase de Grupos (2022)",
            "Estados Unidos", "equilibrado",
            "O Canadá qualificou-se para a Copa do Mundo pela primeira vez em 1986 " +
            "e depois só regressou 36 anos mais tarde, em 2022. Em 2026, disputará o Mundial em casa pela primeira vez.",
            "This is our time. This generation has a chance to make history.",
            "Alphonso Davies", true,
            List.of("Alphonso Davies", "Jonathan David", "Atiba Hutchinson", "Milan Borjan"),
            List.of()
            , "Jesse Marsch", "O tecnico norte-americano constroi uma equipa organizada e de pressao coordenada, com contrato renovado ate 2030 depois dos resultados historicos no Mundial.", "Como anfitriao da Copa 2026, o Canada bateu o seu primeiro ponto de sempre (empate com a Bosnia) e a sua primeira vitoria de sempre (6-0 sobre o Qatar), avancando pela primeira vez aos oitavos de final."
        ));

        // ── PAÍSES BAIXOS ──────────────────────────────────────────────────────
        DADOS.put("NED", new SelecaoEstatica(
            "Oranje", "UEFA", 11,
            List.of(), "Vice-Campeão (1974, 1978, 2010)",
            "Alemanha", "ofensivo",
            "A Holanda é a grande seleção que nunca ganhou o Mundial, tendo perdido três finais (1974, 1978, 2010). " +
            "O 'Futebol Total' de Johan Cruyff nos anos 70 revolucionou o jogo e influencia gerações até hoje.",
            "Every disadvantage has its advantage.",
            "Johan Cruyff", false,
            List.of("Johan Cruyff", "Marco van Basten", "Ruud Gullit", "Frank Rijkaard", "Robin van Persie"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Futebol Total — A Final de 1974", "Alemanha Ocidental", "1-2", "1974",
                    "A Holanda de Cruyff chegou à final de 1974 como a melhor equipa do mundo. " +
                    "Johan Neeskens marcou de penálti ao primeiro minuto, antes de qualquer jogador alemão tocar na bola. " +
                    "Mas a Alemanha remontou e venceu por 2-1. " +
                    "A Holanda perdeu a final, mas ganhou o coração do mundo. O 'Futebol Total' ficou para sempre na história do jogo."
                )
            )
            , "Ronald Koeman", "Na sua segunda passagem pela selecao, Koeman mantem o 4-3-3 estruturado e a posse tecnica holandesa, com flexibilidade para mudar para 4-4-2 ou 3-4-3 sem bola.", "Os Paises Baixos entraram na Copa 2026 como candidata de segundo escalao, num grupo com Japao, Suecia e Tunisia, apesar das baixas de Xavi Simons e Jeremy Frimpong."
        ));

        // ── COLÔMBIA ───────────────────────────────────────────────────────────
        DADOS.put("COL", new SelecaoEstatica(
            "Los Cafeteros", "CONMEBOL", 7,
            List.of(), "Quartas de Final (2014)",
            "Argentina", "ofensivo",
            "James Rodríguez foi o artilheiro da Copa de 2014 com 6 golos em 5 jogos, " +
            "tornando-se o mais jovem a ganhar a Bota de Ouro. O seu golo de bicicleta contra o Uruguai foi eleito o Golo do Torneio.",
            "Colombia tiene el fútbol más alegre del mundo.",
            "James Rodríguez", false,
            List.of("Carlos Valderrama", "René Higuita", "Radamel Falcao", "James Rodríguez", "Freddy Rincón"),
            List.of()
            , "Néstor Lorenzo", "O tecnico argentino alterna entre 4-2-3-1 e 4-3-3, com pressao de alta intensidade e transicoes verticais rapidas construidas em torno de James Rodriguez.", "A Colombia esteve 28 jogos invicta em 2024 — batendo Alemanha, Brasil, Uruguai e Espanha — e terminou a frente de Portugal no seu grupo da Copa 2026."
        ));

        // ── BÉLGICA ────────────────────────────────────────────────────────────
        DADOS.put("BEL", new SelecaoEstatica(
            "Les Diables Rouges", "UEFA", 14,
            List.of(), "3.º Lugar (2018)",
            "Países Baixos", "ofensivo",
            "A 'Geração Dourada' da Bélgica (2014-2022) chegou ao topo do ranking FIFA como número 1 do mundo " +
            "durante 3 anos consecutivos, mas nunca conquistou um grande torneio internacional.",
            "Nous avons une génération unique. L'histoire se fera un jour.",
            "Eden Hazard", false,
            List.of("Eden Hazard", "Kevin De Bruyne", "Romelu Lukaku", "Jan Vertonghen", "Thibaut Courtois"),
            List.of()
            , "Rudi Garcia", "No seu primeiro grande torneio a frente da Belgica, o tecnico frances prefere um 4-3-3 ofensivo e tecnico, construido em torno dos avancados de elite do plantel.", "Num grupo com Egito, Irao e Nova Zelandia, a Belgica de Kevin De Bruyne — aos 34 anos, provavelmente no seu ultimo Mundial — chega com expectativa de alcancar confortavelmente os quartos de final."
        ));

        // ── CROÁCIA ────────────────────────────────────────────────────────────
        DADOS.put("CRO", new SelecaoEstatica(
            "Vatreni", "UEFA", 7,
            List.of(), "Vice-Campeão (2018)",
            "Sérvia", "equilibrado",
            "A Croácia, com apenas 4 milhões de habitantes, é a menor nação a chegar à final de uma Copa do Mundo — " +
            "em 2018, perdeu para a França por 4-2.",
            "Uvijek naprijed, nikad natrag. Sempre em frente, nunca para trás.",
            "Luka Modrić", false,
            List.of("Luka Modrić", "Davor Šuker", "Ivan Rakitić", "Robert Kovač", "Zvonimir Boban"),
            List.of()
            , "Zlatko Dalić", "O selecionador mais longevo da historia da Croacia mantem o 4-3-3 fluido e o dominio do meio-campo que levaram a selecao a duas finais consecutivas de podio.", "Vice-campea em 2018 e terceira classificada em 2022, a Croacia teve um apuramento invicto para 2026 — a melhor campanha da sua historia — com Luka Modric, aos 40 anos, a disputar o seu quinto Mundial."
        ));

        // ── JAPÃO ──────────────────────────────────────────────────────────────
        DADOS.put("JPN", new SelecaoEstatica(
            "Samurai Blue", "AFC", 8,
            List.of(), "Oitavos de Final (2002, 2010, 2018, 2022)",
            "Coreia do Sul", "defensivo",
            "O Japão derrotou a Alemanha e a Espanha na fase de grupos do Qatar 2022 — " +
            "tornando-se a primeira seleção asiática a eliminar dois ex-campeões mundiais europeus no mesmo torneio.",
            "Futebol é o nosso caminho para o mundo. Cada jogo é uma oportunidade de crescer.",
            "Shinji Kagawa", false,
            List.of("Hidetoshi Nakata", "Shinji Kagawa", "Keisuke Honda", "Yasuhito Endo", "Shunsuke Nakamura"),
            List.of()
            , "Hajime Moriyasu", "Moriyasu alterna entre 4-2-3-1 e 4-3-3 consoante o adversario, com ajustes taticos ao vivo que ja o tornaram conhecido por comunicar instrucoes atraves de um quadro branco na area tecnica.", "O Japao terminou em segundo no seu grupo da Copa 2026 e foi eliminado nos oitavos de final pelo Brasil, por 2-1, apesar da ausencia de Kaoru Mitoma por lesao grave."
        ));

        // ── SENEGAL ────────────────────────────────────────────────────────────
        DADOS.put("SEN", new SelecaoEstatica(
            "Les Lions de la Teranga", "CAF", 3,
            List.of(), "Quartas de Final (2002)",
            "Camarões", "equilibrado",
            "O Senegal é a primeira seleção africana a ganhar a Copa Africana das Nações (AFCON) " +
            "e qualificar-se para a Copa do Mundo no mesmo ano (2022). Sadio Mané é o símbolo desta geração histórica.",
            "Nous sommes l'Afrique. Nous jouons pour un continent entier.",
            "Sadio Mané", false,
            List.of("Sadio Mané", "El Hadji Diouf", "Kalidou Koulibaly", "Aliou Cissé"),
            List.of()
            , "Pape Thiaw", "O ex-avancado, promovido depois de vencer o CHAN com a selecao local, aposta em poder fisico, maturidade tatica e uma combinacao de urgencia com controlo do jogo.", "O Senegal goleou o Iraque por 5-0 na fase de grupos da Copa 2026 e seguiu para os oitavos de final, com Sadio Mane, aos 34 anos, a disputar o que anunciou ser o seu ultimo Mundial."
        ));

        // ── COREIA DO SUL ──────────────────────────────────────────────────────
        DADOS.put("KOR", new SelecaoEstatica(
            "Taegeuk Warriors", "AFC", 11,
            List.of(), "4.º Lugar (2002)",
            "Japão", "defensivo",
            "Em 2002, como co-anfitriã com o Japão, a Coreia do Sul chegou às meias-finais — " +
            "a melhor participação de qualquer seleção asiática na história da Copa do Mundo.",
            "We represent 50 million hearts. We play for every single one of them.",
            "Park Ji-sung", false,
            List.of("Park Ji-sung", "Cha Bum-kun", "Son Heung-min", "Lee Young-pyo", "Hong Myung-bo"),
            List.of()
            , null, "Sem selecionador confirmado desde a demissao de Hong Myung-bo em junho de 2026, a federacao sul-coreana procura um sucessor depois da pior participacao da historia do pais num Mundial.", "A Coreia do Sul apurou-se invicta mas foi eliminada na fase de grupos da Copa 2026, com apenas uma vitoria em tres jogos — uma queda que custou o cargo ao treinador."
        ));

        // ── AUSTRÁLIA ──────────────────────────────────────────────────────────
        DADOS.put("AUS", new SelecaoEstatica(
            "Socceroos", "AFC", 6,
            List.of(), "Oitavos de Final (2006, 2022)",
            "Nova Zelândia", "equilibrado",
            "Em 2006, a Austrália chegou aos oitavos de final pela primeira vez — e foi eliminada pela eventual campeã Itália, " +
            "num penálti polémico nos últimos minutos. Em 2022, repetiu o feito sob as ordens de Graham Arnold.",
            "Socceroos never give up. That's our DNA and always will be.",
            "Tim Cahill", false,
            List.of("Tim Cahill", "Mark Schwarzer", "Harry Kewell", "Mile Jedinak", "Ange Postecoglou"),
            List.of()
            , "Tony Popovic", "Popovic aposta num bloco defensivo compacto (3-4-2-1 ou 4-2-3-1), disciplina tatica e transicoes rapidas de alta intensidade.", "Sob a nova abordagem de Popovic, a Australia garantiu o apuramento com jogos de antecedencia e entrou na Copa 2026 no mesmo grupo da anfitria Estados Unidos."
        ));
    }

    public static SelecaoEstatica get(String code) {
        return DADOS.getOrDefault(code.toUpperCase(), criarGenerico(code));
    }

    private static SelecaoEstatica criarGenerico(String code) {
        return new SelecaoEstatica(
            code, "FIFA", 1,
            List.of(), "Fase de Grupos",
            null, "equilibrado",
            "Cada seleção traz a sua cultura e identidade para o maior palco do futebol mundial.",
            "O futebol une o mundo.", "FIFA",
            false, List.of(), List.of(),
            null, null, null
        );
    }
}
