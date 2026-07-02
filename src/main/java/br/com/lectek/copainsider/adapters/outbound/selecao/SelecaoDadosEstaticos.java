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
            List.of(
                new SelecaoEstatica.LendaCurada(
                    "Pelé", "1958–1970",
                    "Edson Arantes do Nascimento tinha apenas 17 anos quando marcou dois gols na final de 1958 e chorou " +
                    "abraçado ao goleiro sueco, ainda sem entender o tamanho do que tinha acabado de fazer. Doze anos depois, " +
                    "em 1970, encerrou a carreira em Copas como tricampeão mundial e autor do gol que abriu a final contra a Itália — " +
                    "um cabeceio tão certeiro que o goleiro Albertosi disse depois que só ouviu a bola entrar, não a viu. " +
                    "É o único jogador da história com três títulos mundiais e, para muitos, o maior futebolista de todos os tempos.",
                    "O Rei. Três Copas, um símbolo que transcendeu o esporte e se tornou patrimônio do Brasil."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Garrincha", "1958–1962",
                    "Manoel Francisco dos Santos nasceu com uma deformação nas pernas que os médicos garantiam que o impediria " +
                    "de jogar futebol. Tornou-se o driblador mais genial que o jogo já viu. Enquanto Pelé esteve lesionado em 1962, " +
                    "foi Garrincha quem carregou o Brasil ao bicampeonato sozinho, driblando times inteiros como quem brinca na rua. " +
                    "O Brasil nunca perdeu uma partida oficial em que Garrincha tenha entrado em campo pela seleção — um recorde " +
                    "impossível de replicar.",
                    "A Alegria do Povo. O único jogador que fez do drible uma forma de poesia."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Sócrates", "1979–1986",
                    "Médico formado, capitão do Corinthians, ícone da Democracia Corinthiana — Sócrates jogava futebol como quem " +
                    "discute filosofia: com a mesma calma e a mesma profundidade. Na Copa de 1982, liderou uma das seleções mais " +
                    "artisticamente perfeitas da história, ao lado de Zico, Falcão e Éder, mas caiu diante da Itália de Paolo Rossi " +
                    "num dos jogos mais lembrados de sempre. Nunca foi campeão do mundo, mas nenhuma lista de maiores jogadores " +
                    "brasileiros está completa sem o seu nome.",
                    "Prova de que grandeza no futebol não se mede só em taças."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Zico", "1978–1986",
                    "Chamavam-lhe 'o Pelé Branco', um fardo injusto para um jogador com identidade própria. Artilheiro nato, " +
                    "de batida de bola parada quase perfeita, Zico foi o coração das seleções de 1982 e 1986 — talvez as duas " +
                    "equipas mais talentosas a nunca conquistar o título. É, até hoje, o maior artilheiro da história do Flamengo " +
                    "e um símbolo de que o Brasil também sabe perder com grandeza, sem deixar de ser lembrado com carinho.",
                    "O maior jogador brasileiro que nunca ergueu a taça — e ainda assim, uma lenda absoluta."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Romário", "1987–2005",
                    "Baixinho, egocêntrico dentro da área e genial como poucos, Romário viveu a Copa de 1994 como se fosse dele " +
                    "sozinho. Cinco gols, indicado o melhor jogador do torneio, peça central do tetracampeonato que encerrou um " +
                    "jejum de 24 anos sem título. Marcou mais de mil gols na carreira, segundo a sua própria contagem — um número " +
                    "que virou lenda em si mesmo.",
                    "O artilheiro que devolveu ao Brasil o gosto pelo título."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Bebeto", "1985–1998",
                    "Parceiro de Romário no ataque de 1994, Bebeto trouxe inteligência de posicionamento e frieza na finalização " +
                    "a uma dupla que se tornou lendária. O seu gol contra a Holanda nas quartas de final, celebrado a embalar um " +
                    "bebê imaginário — homenagem ao nascimento do filho — é uma das imagens mais repetidas da história das Copas.",
                    "Metade de uma das duplas de ataque mais eficientes que o Brasil já teve."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Ronaldo Fenômeno", "1994–2011",
                    "Nenhuma lesão foi capaz de apagar o que Ronaldo fez dentro de campo. Depois de perder a final de 1998 em " +
                    "circunstâncias nunca totalmente esclarecidas, voltou em 2002 — recém-saído de duas cirurgias graves no joelho — " +
                    "e marcou os dois gols da final contra a Alemanha, sendo o artilheiro do torneio com 8 gols. É, até hoje, um dos " +
                    "três maiores artilheiros da história das Copas do Mundo.",
                    "A maior história de superação do futebol brasileiro moderno."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Ronaldinho Gaúcho", "1999–2013",
                    "Sorriso fácil, drible impossível de defender. Ronaldinho foi a alma do pentacampeonato em 2002, ao lado de " +
                    "Ronaldo e Rivaldo — o trio ficou conhecido como 'os três Rs'. O seu gol de falta contra a Inglaterra nas quartas, " +
                    "de fora da área e sem querer surpreender o goleiro, é lembrado como um dos mais bonitos e injustos da história " +
                    "das Copas. Poucos jogadores fizeram o futebol parecer tão divertido.",
                    "O jogador que fez o mundo sorrir jogando futebol."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Neymar", "2010–presente",
                    "Herdeiro simbólico da camisa 10 desde muito jovem, Neymar carregou o peso de ser 'o próximo Pelé' antes " +
                    "mesmo de estrear numa Copa do Mundo. Viveu o trauma do Mineirazo por fora — lesionado na véspera — e desde " +
                    "então tem perseguido o título que falta na sua carreira. É o maior artilheiro da história da seleção brasileira, " +
                    "à frente do próprio Pelé, e regressa para 2026 depois de quase três anos afastado por lesões graves, em busca " +
                    "de um capítulo final à altura do seu talento.",
                    "O maior artilheiro da história da seleção, ainda em busca do seu momento definitivo."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Vinícius Júnior", "2019–presente",
                    "Do Flamengo ao Real Madrid, Vinícius Júnior tornou-se o rosto mais reconhecível do futebol brasileiro da " +
                    "sua geração antes dos 25 anos. Velocidade, drible e uma capacidade de decidir jogos grandes que já lhe valeu " +
                    "títulos de Champions League. Chega à Copa 2026 como a principal esperança ofensiva do Brasil, carregando a " +
                    "expectativa de uma nação que há mais de duas décadas não ergue a taça.",
                    "O rosto da nova geração canarinha — e o principal candidato a herdar o peso da camisa 10."
                ),
                new SelecaoEstatica.LendaCurada(
                    "Endrick", "2023–presente",
                    "Ainda adolescente quando assinou com o Real Madrid, Endrick representa a mais recente geração de " +
                    "centroavantes brasileiros criados para o gol — instinto de finalização, sangue-frio e uma ambição que já o " +
                    "levou à seleção principal antes dos 18 anos. Para muitos torcedores, é o símbolo de que a fábrica de craques " +
                    "brasileira continua a funcionar, mesmo depois de duas décadas sem título mundial.",
                    "A promessa mais jovem da nova geração, carregando o peso de um país que espera pelo hexacampeonato."
                )
            ),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Final Perfeita — Pelé com 17 Anos", "Suécia", "5-2", "1958",
                    "No Rasunda Estadion, em Solna, o Brasil disputou a sua primeira final de Copa do Mundo. " +
                    "Com Pelé a apenas 17 anos, a Canarinha protagonizou um dos jogos mais belos da história. " +
                    "Pelé marcou dois gols extraordinários na segunda parte, Vavá fez dois e Zagalo fechou a contagem. " +
                    "O Brasil tornava-se campeão do mundo pela primeira vez, com um futebol tão alegre que até o público sueco aplaudiu de pé."
                ),
                new SelecaoEstatica.PartidaRef(
                    "O Mineirazo — A Noite Mais Negra", "Alemanha", "1-7", "2014",
                    "No Estádio Mineirão, em Belo Horizonte, a seleção anfitriã sofreu a maior humilhação da sua história. " +
                    "Sem Neymar e sem Thiago Silva, suspensos, o Brasil desmoronou em campo. " +
                    "A Alemanha marcou 5 gols nos primeiros 29 minutos. " +
                    "O silêncio no estádio — interrompido apenas pelos soluços dos torcedores — ficou gravado para sempre na memória do futebol mundial."
                ),
                new SelecaoEstatica.PartidaRef(
                    "O Maracanazo — A Dor que Nunca Passa", "Uruguai", "1-2", "1950",
                    "Com quase 200.000 espectadores — o maior público da história do futebol — o Maracanã aguardava o Brasil campeão. " +
                    "Bastava um empate. O Uruguai tinha outras ideias. " +
                    "Ghiggia marcou o 2-1 aos 79 minutos, e o maior silêncio da história do futebol tombou sobre o Rio de Janeiro. " +
                    "O jornalista Nélson Rodrigues chamaria a este momento 'a nossa Hiroshima'."
                )
            )
            , "Carlo Ancelotti", "O treinador italiano trouxe uma gestão mais flexível ao elenco estrelado, com uma equipe mais compacta e bem posicionada taticamente.", "Depois de um início difícil na Copa do Mundo de 2026, o Brasil reagiu com duas vitórias seguidas e liderou o Grupo C, com Neymar de volta à seleção após quase três anos de ausência."
            , List.of(
                new SelecaoEstatica.Marco(1914, "As Primeiras Partidas",
                    "O futebol organizado no Brasil dá os primeiros passos com jogos internacionais ainda amadores, " +
                    "lançando as bases do que se tornaria a seleção mais vitoriosa da história das Copas do Mundo."),
                new SelecaoEstatica.Marco(1930, "A Primeira Copa do Mundo",
                    "No Uruguai, o Brasil disputa a sua primeira Copa do Mundo. A eliminação ainda na fase inicial é o " +
                    "primeiro de muitos capítulos de uma jornada que duraria mais de um século."),
                new SelecaoEstatica.Marco(1950, "O Quase-Título",
                    "Como anfitrião, o Brasil chega à última rodada só precisando de um empate contra o Uruguai no Maracanã " +
                    "lotado. A derrota por 2-1 entra para a história como o Maracanazo — a maior dor do futebol brasileiro."),
                new SelecaoEstatica.Marco(1958, "O Primeiro Título",
                    "Na Suécia, um garoto de 17 anos chamado Pelé e um ponta genial chamado Garrincha lideram o Brasil ao " +
                    "primeiro título mundial da sua história, com um futebol tão bonito que conquistou o mundo."),
                new SelecaoEstatica.Marco(1962, "O Segundo Título",
                    "No Chile, com Pelé lesionado logo na fase de grupos, Garrincha assume o protagonismo e carrega o Brasil " +
                    "ao bicampeonato sozinho, driblando adversários como ninguém antes ou depois."),
                new SelecaoEstatica.Marco(1970, "O Terceiro Título — Tricampeão para Sempre",
                    "No México, a seleção mais lembrada da história do futebol — Pelé, Jairzinho, Tostão, Rivellino, Carlos " +
                    "Alberto — conquista o tricampeonato e fica definitivamente com a taça Jules Rimet."),
                new SelecaoEstatica.Marco(1982, "A Melhor Seleção que Não Foi Campeã",
                    "Zico, Sócrates, Falcão e Éder formam uma das equipes mais artisticamente completas da história — mas " +
                    "caem diante da Itália de Paolo Rossi num jogo lembrado até hoje como uma das maiores tragédias do futebol brasileiro."),
                new SelecaoEstatica.Marco(1994, "O Quarto Título — Fim do Jejum",
                    "Nos Estados Unidos, Romário e Bebeto encerram 24 anos sem título mundial, vencendo a Itália nos pênaltis " +
                    "numa final sem gols que devolveu ao Brasil o posto de nação mais vitoriosa das Copas."),
                new SelecaoEstatica.Marco(2002, "O Quinto Título — Pentacampeão",
                    "Na Coreia do Sul e no Japão, o trio Ronaldo, Rivaldo e Ronaldinho — os 'três Rs' — conduz o Brasil ao " +
                    "quinto título mundial, com Ronaldo a superar as duas cirurgias no joelho para ser artilheiro do torneio."),
                new SelecaoEstatica.Marco(2014, "O Trauma do Mineirazo",
                    "Como anfitrião novamente, o Brasil sofre a pior derrota da sua história — 1-7 diante da Alemanha nas " +
                    "semifinais — um resultado que ainda hoje define uma geração de torcedores."),
                new SelecaoEstatica.Marco(2026, "A Nova Geração, Rumo ao Hexa",
                    "Com Neymar de volta e uma nova geração liderada por Vinícius Júnior e Endrick, o Brasil busca o sexto " +
                    "título mundial e o fim de mais de duas décadas de espera.")
            )
            , "Poucas coisas no desporto mundial equivalem à pressão de vestir a camisa amarela da seleção brasileira. " +
              "Não é apenas um jogo — é a expectativa de mais de 200 milhões de pessoas, o peso de cinco estrelas bordadas " +
              "no peito e a certeza de que qualquer resultado abaixo do título será sempre insuficiente. Jogadores brasileiros " +
              "aprendem cedo que o talento, por si só, nunca é desculpa: o Brasil não perdoa gerações inteiras — como a de " +
              "1982 — apenas por terem jogado bonito sem vencer. É essa mesma pressão, no entanto, que forjou os maiores " +
              "nomes da história do futebol. Carregar a camisa 10 do Brasil é, ao mesmo tempo, o maior sonho e o maior fardo " +
              "que um futebolista pode assumir."
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
            SelecaoEstatica.nomesSimples("Diego Maradona", "Lionel Messi", "Gabriel Batistuta", "Mario Kempes", "Alfredo Di Stéfano"),
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
            , "Lionel Scaloni", "Scaloni comanda a Argentina ha sete anos com um 4-3-3 fluido, dominio do meio-campo e rotacoes posicionais constantes na construcao de jogo.", "Campea do Mundo em 2022 e bicampea da Copa America, a Argentina chegou a Copa 2026 como numero um do ranking FIFA e venceu o seu grupo com 100% de aproveitamento, com Messi ainda em acao pela selecao.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Zinedine Zidane", "Thierry Henry", "Michel Platini", "Kylian Mbappé", "Just Fontaine"),
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
            , "Didier Deschamps", "Depois de catorze anos no comando — este e o seu ultimo torneio a frente da selecao — Deschamps constroi uma estrutura solida e pragmatica em torno de Mbappe.", "A Franca venceu o seu grupo de apuramento com apenas um golo sofrido em seis jogos, liderada por Kylian Mbappe, agora capitao e melhor marcador da historia francesa.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Franz Beckenbauer", "Gerd Müller", "Lothar Matthäus", "Miroslav Klose", "Oliver Kahn"),
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
            , "Julian Nagelsmann", "Aos 38 anos, o mais jovem selecionador do torneio aposta num 4-2-3-1 com contrapressao agressiva e transicoes rapidas — o gegenpressing tipico do futebol alemao moderno.", "A Alemanha apurou-se como lider do seu grupo, com 16 golos marcados e apenas tres sofridos, contando ja com Jamal Musiala recuperado de uma lesao grave.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Bobby Moore", "Bobby Charlton", "Wayne Rooney", "Gary Lineker", "Gordon Banks"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "It's Coming Home — A Final de 1966", "Alemanha Ocidental", "4-2", "1966",
                    "Em Wembley, com 96.924 espetadores, a Inglaterra realizou o sonho perante os seus adeptos. " +
                    "Geoff Hurst marcou o único hat-trick da história das finais mundiais — " +
                    "incluindo o controverso terceiro golo que o árbitro de linha confirmou ter cruzado a linha. " +
                    "Bobby Moore levantou a Jules Rimet. A Inglaterra jamais voltaria tão perto do topo."
                )
            )
            , "Thomas Tuchel", "Mais flexivel do que o antecessor, Tuchel alterna entre defesa a tres e a quatro, ajustando o bloco defensivo consoante o adversario.", "A Inglaterra teve um apuramento perfeito — sem perder nem sofrer um unico golo — e goleou a Croacia por 4-2 na estreia da Copa 2026, com Harry Kane a liderar como capitao e maior goleador da historia da selecao.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Paolo Maldini", "Roberto Baggio", "Gianluigi Buffon", "Giuseppe Meazza", "Paolo Rossi"),
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
            , null, "Sem treinador efetivo desde abril de 2026, a federacao italiana procura ainda o sucessor ideal para reconstruir a selecao a partir do zero.", "Pela terceira vez consecutiva, a Italia falhou o apuramento para a fase final — eliminada nos play-offs pela Bosnia — deixando de fora do Mundial 2026 uma das selecoes mais vencedoras da historia.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Obdulio Varela", "Alcides Ghiggia", "Luis Suárez", "Diego Forlán", "Enzo Francescoli"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Maracanazo — David Derruba Golias", "Brasil", "2-1", "1950",
                    "Com quase 200.000 espetadores no Maracanã, o Brasil precisava apenas de um empate para ser campeão. " +
                    "O Uruguai entrou como azarão. Friaça abriu o marcador para o Brasil. " +
                    "Schiaffino empatou e Ghiggia, aos 79 minutos, fez o golo que gelou o mundo. " +
                    "O maior silêncio da história do futebol instalou-se no maior estádio do mundo. O Uruguai era bicampeão."
                )
            )
            , "Marcelo Bielsa", "Bielsa mantem a sua assinatura tatica: 4-3-3 com pressao alta, verticalidade extrema e uma preparacao obsessiva em video antes de cada adversario.", "O Uruguai foi eliminado na fase de grupos da Copa 2026, apos empates com Arabia Saudita e Cabo Verde e derrota com Espanha — uma saida precoce que gerou tensao entre Bielsa e o plantel.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Xavi Hernández", "Andrés Iniesta", "Raúl", "David Villa", "Iker Casillas"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Iniesta Faz Espanha Chorar de Alegria", "Países Baixos", "1-0", "2010",
                    "Numa final disputada em Joanesburgo — 14 cartões amarelos, 1 vermelho — " +
                    "a Espanha e os Países Baixos lutaram durante 120 minutos. " +
                    "A decisão chegou nos últimos segundos da prorrogação: Iniesta recebeu o passe de Cesc e atirou para as redes. " +
                    "Arrancou a camisola e correu em delírio. A Espanha era campeã do mundo pela primeira e única vez."
                )
            )
            , "Luis de la Fuente", "De la Fuente mantem a posse posicional que sempre definiu a Espanha, mas com mais diretismo e amplitude do que as geracoes anteriores, construindo o jogo em 4-3-3.", "Campea da Eurocopa 2024, a Espanha chega a Copa 2026 como uma das grandes favoritas, liderada pela nova geracao de Lamine Yamal e pelo capitao Rodri, Bola de Ouro em titulo.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Eusébio", "Cristiano Ronaldo", "Luís Figo", "Rui Costa", "Fernando Peyroteo"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "Eusébio Salva Portugal Sozinho", "Coreia do Norte", "5-3", "1966",
                    "Nos quartos de final de 1966, Portugal encontrou-se a perder 0-3 para a Coreia do Norte ao intervalo. " +
                    "Era a maior surpresa da história do Mundial até então. " +
                    "O que se seguiu foi pura magia de Eusébio: quatro golos na segunda parte. " +
                    "Torres fechou a contagem em 5-3. Portugal chegava às meias-finais do seu melhor Mundial alguma vez."
                )
            )
            , "Roberto Martínez", "O tecnico espanhol mantem a base da equipa mas tem apostado em sangue novo, com foco em qualidade em espacos curtos e controlo da zona central do campo.", "Portugal marcou seis golos e sofreu apenas um na fase de grupos da Copa 2026, mas dois empates deixaram a equipa atras da Colombia — com Cristiano Ronaldo, aos 41 anos, a disputar o seu sexto Mundial.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Hugo Sánchez", "Cuauhtémoc Blanco", "Jorge Campos", "Andrés Guardado", "Javier Hernández"),
            List.of()
            , "Javier Aguirre", "Na sua terceira passagem a frente do Mexico num Mundial, Aguirre aposta num 4-2-3-1 pragmatico, com armadilha de fora de jogo e foco total em nao sofrer golos.", "O Mexico teve a sua melhor fase de grupos de sempre na Copa 2026 — tres vitorias seguidas sem sofrer um unico golo, como anfitriao ao lado dos Estados Unidos e do Canada.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Landon Donovan", "Clint Dempsey", "Tim Howard", "Christian Pulisic", "Kasey Keller"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Surpresa que Chocou o Mundo", "Portugal", "3-2", "2002",
                    "Nos oitavos de final do Japão/Coreia, os EUA derrotaram Portugal — a seleção de Figo e Rui Costa — por 3-2. " +
                    "O resultado chocou o mundo do futebol. Os americanos seguiram para os quartos de final, " +
                    "onde foram eliminados pela Alemanha. Foi a melhor participação dos EUA desde 1930."
                )
            )
            , "Mauricio Pochettino", "Pochettino promove um futebol de pressao alta e ofensivo, com os laterais avancados a abrir espaco para os extremos jogarem por dentro.", "Anfitria da Copa do Mundo de 2026, a selecao norte-americana entra no torneio com uma mentalidade de candidata, ja nao de azarao, sob o comando do argentino.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Achraf Hakimi", "Hakim Ziyech", "Youssef En-Nesyri", "Ahmed Faras", "Noureddine Naybet"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "A Geração que Mudou a História de África", "Portugal", "1-0", "2022",
                    "Nos quartos de final do Qatar, o Marrocos enfrentou Portugal e venceu por 1-0 " +
                    "com um golo de En-Nesyri de cabeça. Era a primeira vez que uma seleção africana chegava às meias-finais. " +
                    "O Marrocos tinha eliminado a Espanha nos oitavos e a Bélgica na fase de grupos. " +
                    "Uma geração única mudou a história do futebol africano para sempre."
                )
            )
            , "Mohamed Ouahbi", "Ouahbi herdou de Walid Regragui a identidade defensiva que definiu o Marrocos — bloco medio organizado, 4-3-3, pressao coletiva coordenada.", "Nove sobreviventes do plantel que fez historia nas meias-finais de 2022 continuam no grupo, agora sob um novo treinador nomeado apenas meses antes do Mundial 2026.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Alphonso Davies", "Jonathan David", "Atiba Hutchinson", "Milan Borjan"),
            List.of()
            , "Jesse Marsch", "O tecnico norte-americano constroi uma equipa organizada e de pressao coordenada, com contrato renovado ate 2030 depois dos resultados historicos no Mundial.", "Como anfitriao da Copa 2026, o Canada bateu o seu primeiro ponto de sempre (empate com a Bosnia) e a sua primeira vitoria de sempre (6-0 sobre o Qatar), avancando pela primeira vez aos oitavos de final.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Johan Cruyff", "Marco van Basten", "Ruud Gullit", "Frank Rijkaard", "Robin van Persie"),
            List.of(
                new SelecaoEstatica.PartidaRef(
                    "O Futebol Total — A Final de 1974", "Alemanha Ocidental", "1-2", "1974",
                    "A Holanda de Cruyff chegou à final de 1974 como a melhor equipa do mundo. " +
                    "Johan Neeskens marcou de penálti ao primeiro minuto, antes de qualquer jogador alemão tocar na bola. " +
                    "Mas a Alemanha remontou e venceu por 2-1. " +
                    "A Holanda perdeu a final, mas ganhou o coração do mundo. O 'Futebol Total' ficou para sempre na história do jogo."
                )
            )
            , "Ronald Koeman", "Na sua segunda passagem pela selecao, Koeman mantem o 4-3-3 estruturado e a posse tecnica holandesa, com flexibilidade para mudar para 4-4-2 ou 3-4-3 sem bola.", "Os Paises Baixos entraram na Copa 2026 como candidata de segundo escalao, num grupo com Japao, Suecia e Tunisia, apesar das baixas de Xavi Simons e Jeremy Frimpong.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Carlos Valderrama", "René Higuita", "Radamel Falcao", "James Rodríguez", "Freddy Rincón"),
            List.of()
            , "Néstor Lorenzo", "O tecnico argentino alterna entre 4-2-3-1 e 4-3-3, com pressao de alta intensidade e transicoes verticais rapidas construidas em torno de James Rodriguez.", "A Colombia esteve 28 jogos invicta em 2024 — batendo Alemanha, Brasil, Uruguai e Espanha — e terminou a frente de Portugal no seu grupo da Copa 2026.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Eden Hazard", "Kevin De Bruyne", "Romelu Lukaku", "Jan Vertonghen", "Thibaut Courtois"),
            List.of()
            , "Rudi Garcia", "No seu primeiro grande torneio a frente da Belgica, o tecnico frances prefere um 4-3-3 ofensivo e tecnico, construido em torno dos avancados de elite do plantel.", "Num grupo com Egito, Irao e Nova Zelandia, a Belgica de Kevin De Bruyne — aos 34 anos, provavelmente no seu ultimo Mundial — chega com expectativa de alcancar confortavelmente os quartos de final.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Luka Modrić", "Davor Šuker", "Ivan Rakitić", "Robert Kovač", "Zvonimir Boban"),
            List.of()
            , "Zlatko Dalić", "O selecionador mais longevo da historia da Croacia mantem o 4-3-3 fluido e o dominio do meio-campo que levaram a selecao a duas finais consecutivas de podio.", "Vice-campea em 2018 e terceira classificada em 2022, a Croacia teve um apuramento invicto para 2026 — a melhor campanha da sua historia — com Luka Modric, aos 40 anos, a disputar o seu quinto Mundial.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Hidetoshi Nakata", "Shinji Kagawa", "Keisuke Honda", "Yasuhito Endo", "Shunsuke Nakamura"),
            List.of()
            , "Hajime Moriyasu", "Moriyasu alterna entre 4-2-3-1 e 4-3-3 consoante o adversario, com ajustes taticos ao vivo que ja o tornaram conhecido por comunicar instrucoes atraves de um quadro branco na area tecnica.", "O Japao terminou em segundo no seu grupo da Copa 2026 e foi eliminado nos oitavos de final pelo Brasil, por 2-1, apesar da ausencia de Kaoru Mitoma por lesao grave.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Sadio Mané", "El Hadji Diouf", "Kalidou Koulibaly", "Aliou Cissé"),
            List.of()
            , "Pape Thiaw", "O ex-avancado, promovido depois de vencer o CHAN com a selecao local, aposta em poder fisico, maturidade tatica e uma combinacao de urgencia com controlo do jogo.", "O Senegal goleou o Iraque por 5-0 na fase de grupos da Copa 2026 e seguiu para os oitavos de final, com Sadio Mane, aos 34 anos, a disputar o que anunciou ser o seu ultimo Mundial.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Park Ji-sung", "Cha Bum-kun", "Son Heung-min", "Lee Young-pyo", "Hong Myung-bo"),
            List.of()
            , null, "Sem selecionador confirmado desde a demissao de Hong Myung-bo em junho de 2026, a federacao sul-coreana procura um sucessor depois da pior participacao da historia do pais num Mundial.", "A Coreia do Sul apurou-se invicta mas foi eliminada na fase de grupos da Copa 2026, com apenas uma vitoria em tres jogos — uma queda que custou o cargo ao treinador.", List.of(), null
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
            SelecaoEstatica.nomesSimples("Tim Cahill", "Mark Schwarzer", "Harry Kewell", "Mile Jedinak", "Ange Postecoglou"),
            List.of()
            , "Tony Popovic", "Popovic aposta num bloco defensivo compacto (3-4-2-1 ou 4-2-3-1), disciplina tatica e transicoes rapidas de alta intensidade.", "Sob a nova abordagem de Popovic, a Australia garantiu o apuramento com jogos de antecedencia e entrou na Copa 2026 no mesmo grupo da anfitria Estados Unidos.", List.of(), null
        ));
    }

    public static SelecaoEstatica get(String code) {
        return DADOS.getOrDefault(code.toUpperCase(), criarGenerico(code));
    }

    // código → {pt, es, fr, en} — nome da seleção sempre localizado, nunca vindo
    // cru da TheSportsDB (que devolve tudo em inglês, ex: "Brazil" em vez de "Brasil").
    private static final Map<String, String[]> NOMES = new HashMap<>();
    static {
        NOMES.put("BRA", new String[]{"Brasil", "Brasil", "Brésil", "Brazil"});
        NOMES.put("ARG", new String[]{"Argentina", "Argentina", "Argentine", "Argentina"});
        NOMES.put("FRA", new String[]{"França", "Francia", "France", "France"});
        NOMES.put("ALE", new String[]{"Alemanha", "Alemania", "Allemagne", "Germany"});
        NOMES.put("ENG", new String[]{"Inglaterra", "Inglaterra", "Angleterre", "England"});
        NOMES.put("ITA", new String[]{"Itália", "Italia", "Italie", "Italy"});
        NOMES.put("URU", new String[]{"Uruguai", "Uruguay", "Uruguay", "Uruguay"});
        NOMES.put("ESP", new String[]{"Espanha", "España", "Espagne", "Spain"});
        NOMES.put("POR", new String[]{"Portugal", "Portugal", "Portugal", "Portugal"});
        NOMES.put("MEX", new String[]{"México", "México", "Mexique", "Mexico"});
        NOMES.put("USA", new String[]{"Estados Unidos", "Estados Unidos", "États-Unis", "United States"});
        NOMES.put("MAR", new String[]{"Marrocos", "Marruecos", "Maroc", "Morocco"});
        NOMES.put("CAN", new String[]{"Canadá", "Canadá", "Canada", "Canada"});
        NOMES.put("NED", new String[]{"Países Baixos", "Países Bajos", "Pays-Bas", "Netherlands"});
        NOMES.put("COL", new String[]{"Colômbia", "Colombia", "Colombie", "Colombia"});
        NOMES.put("BEL", new String[]{"Bélgica", "Bélgica", "Belgique", "Belgium"});
        NOMES.put("CRO", new String[]{"Croácia", "Croacia", "Croatie", "Croatia"});
        NOMES.put("JPN", new String[]{"Japão", "Japón", "Japon", "Japan"});
        NOMES.put("SEN", new String[]{"Senegal", "Senegal", "Sénégal", "Senegal"});
        NOMES.put("KOR", new String[]{"Coreia do Sul", "Corea del Sur", "Corée du Sud", "South Korea"});
        NOMES.put("AUS", new String[]{"Austrália", "Australia", "Australie", "Australia"});
    }

    /** Nome da seleção sempre no idioma do ebook — nunca depende do que a API externa devolver. */
    public static String nomeLocalizado(String code, String idioma, String fallback) {
        String[] variantes = NOMES.get(code.toUpperCase());
        if (variantes == null) return fallback;
        int i = switch (idioma) {
            case "pt-BR", "pt-PT" -> 0;
            case "es" -> 1;
            case "fr" -> 2;
            default   -> 3;
        };
        return variantes[i];
    }

    private static SelecaoEstatica criarGenerico(String code) {
        return new SelecaoEstatica(
            code, "FIFA", 1,
            List.of(), "Fase de Grupos",
            null, "equilibrado",
            "Cada seleção traz a sua cultura e identidade para o maior palco do futebol mundial.",
            "O futebol une o mundo.", "FIFA",
            false, List.of(), List.of(),
            null, null, null,
            List.of(), null
        );
    }
}
