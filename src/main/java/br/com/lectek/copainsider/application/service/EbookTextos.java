package br.com.lectek.copainsider.application.service;

public class EbookTextos {

    private static final String PT_BR = "pt-BR";
    private static final String PT_PT = "pt-PT";
    private static final String OFENSIVO  = "ofensivo";
    private static final String DEFENSIVO = "defensivo";
    private static final String BULLET = "  >> ";

    private EbookTextos() {}

    // ── Frase de capa ──────────────────────────────────────────────────────────

    public static String fraseCapa(boolean eCampeao, String idioma) {
        return switch (idioma) {
            case PT_BR, PT_PT -> eCampeao ? "Campeon do Mundo" : "Na Busca da Gloria";
            case "es"         -> eCampeao ? "Campeones del Mundo" : "En Busca de la Gloria";
            case "fr"         -> eCampeao ? "Champions du Monde" : "A la Conquete de la Gloire";
            case "de"         -> eCampeao ? "Weltmeister" : "Auf der Jagd nach Ruhm";
            case "it"         -> eCampeao ? "Campioni del Mondo" : "Alla Conquista della Gloria";
            default           -> eCampeao ? "World Champions" : "Chasing Glory";
        };
    }

    // ── Intro da Alma da Selecao ───────────────────────────────────────────────

    public static String introAlma(String selecaoNome, String alcunha, boolean eCampeao,
                                    int numTitulos, String resumoAnos, String idioma) {
        return eCampeao
                ? introAlmaCampeao(selecaoNome, alcunha, numTitulos, resumoAnos, idioma)
                : introAlmaDesafiante(selecaoNome, alcunha, idioma);
    }

    private static String introAlmaCampeao(String nome, String alcunha,
                                            int numTitulos, String anos, String idioma) {
        String sufixo = numTitulos == 1 ? "" : "s";
        return switch (idioma) {
            case PT_BR, PT_PT ->
                String.format("%s — conhecida mundialmente como %s — e uma das maiores selecoes da historia do futebol. " +
                    "Com %d titulo%s mundial%s (%s), representa a excelencia e a paixao que definem este desporto.",
                    nome, alcunha, numTitulos, sufixo, sufixo, anos);
            case "es" ->
                String.format("%s — conocida mundialmente como %s — es una de las selecciones mas grandes de la historia. " +
                    "Con %d titulo%s mundial%s (%s), representa la excelencia del futbol.",
                    nome, alcunha, numTitulos, sufixo, sufixo, anos);
            case "fr" ->
                String.format("%s — connue sous le nom de %s — est l'une des plus grandes equipes de l'histoire du football. " +
                    "Avec %d titre%s mondial%s (%s), elle represente l'excellence de ce sport.",
                    nome, alcunha, numTitulos, sufixo, sufixo, anos);
            default ->
                String.format("%s — known worldwide as %s — is one of the greatest national teams in football history. " +
                    "With %d World Cup title%s (%s), they represent excellence on the world's biggest stage.",
                    nome, alcunha, numTitulos, sufixo, anos);
        };
    }

    private static String introAlmaDesafiante(String nome, String alcunha, String idioma) {
        return switch (idioma) {
            case PT_BR, PT_PT ->
                String.format("%s — conhecida como %s — e uma das selecoes mais respeitadas do futebol mundial. " +
                    "Cada Copa do Mundo e uma nova oportunidade de escrever a sua historia no maior palco do desporto.",
                    nome, alcunha);
            case "es" ->
                String.format("%s — conocida como %s — es una de las selecciones mas respetadas del futbol mundial. " +
                    "Cada Copa del Mundo es una nueva oportunidad de escribir su historia.",
                    nome, alcunha);
            case "fr" ->
                String.format("%s — connue sous le nom de %s — est l'une des equipes les plus respectees du football mondial. " +
                    "Chaque Coupe du Monde est une nouvelle occasion d'ecrire son histoire.",
                    nome, alcunha);
            default ->
                String.format("%s — known as %s — is one of the most respected national teams in world football. " +
                    "Every World Cup is a new chance to write their story on the biggest stage.",
                    nome, alcunha);
        };
    }

    // ── Historico nas Copas ────────────────────────────────────────────────────

    public static String historiaCopas(String selecaoNome, int participacoes,
                                        boolean eCampeao, int numTitulos, String resumoAnos,
                                        String melhorResultado, String idioma) {
        String sufixo    = numTitulos == 1 ? "" : "s";
        String sufixoVez = numTitulos == 1 ? "" : "es";
        return switch (idioma) {
            case PT_BR, PT_PT -> eCampeao
                ? String.format("%s disputou %d Copas do Mundo, conquistando o titulo %d vez%s — em %s. " +
                    "Um palmares que poucos podem igualar e que faz desta selecao uma referencia no futebol mundial.",
                    selecaoNome, participacoes, numTitulos, sufixoVez, resumoAnos)
                : String.format("%s disputou %d Copas do Mundo, tendo como melhor resultado %s. " +
                    "Ao longo das decadas, construiu uma identidade solida no futebol mundial e continua a lutar pelo topo.",
                    selecaoNome, participacoes, melhorResultado);
            case "es" -> eCampeao
                ? String.format("%s ha disputado %d Copas del Mundo, conquistando el titulo %d vez%s — en %s. " +
                    "Un palmares que pocas selecciones pueden igualar.",
                    selecaoNome, participacoes, numTitulos, sufixoVez, resumoAnos)
                : String.format("%s ha disputado %d Copas del Mundo, con un mejor resultado de %s. " +
                    "A lo largo de las decadas ha construido una identidad solida en el futbol mundial.",
                    selecaoNome, participacoes, melhorResultado);
            case "fr" -> eCampeao
                ? String.format("%s a participe a %d Coupes du Monde, remportant le titre %d fois — en %s. " +
                    "Un palmares que peu d'equipes peuvent egaler.",
                    selecaoNome, participacoes, numTitulos, resumoAnos)
                : String.format("%s a participe a %d Coupes du Monde, avec comme meilleur resultat %s. " +
                    "Au fil des decennies, cette selection a construit une identite forte dans le football mondial.",
                    selecaoNome, participacoes, melhorResultado);
            default -> eCampeao
                ? String.format("%s has participated in %d World Cups, winning the title %d time%s — in %s. " +
                    "A record that few national teams can match.",
                    selecaoNome, participacoes, numTitulos, sufixo, resumoAnos)
                : String.format("%s has participated in %d World Cups, with a best result of %s. " +
                    "Over the decades, they have built a strong identity in world football.",
                    selecaoNome, participacoes, melhorResultado);
        };
    }

    // ── Copa 2026 ──────────────────────────────────────────────────────────────

    public static String copa2026(String selecaoNome, boolean eCampeao, boolean anfitriao, String idioma) {
        String objetivo = objetivoCopa2026(eCampeao, anfitriao, idioma);
        return switch (idioma) {
            case PT_BR, PT_PT ->
                "A Copa do Mundo de 2026 e uma edicao historica, realizada pela primeira vez em tres paises: " +
                "os Estados Unidos, o Canada e o Mexico. Com 48 selecoes participantes — o maior numero da historia — " +
                "o torneio promete ser o maior espetaculo do desporto mundial. " +
                selecaoNome + " chega com um objetivo claro: " + objetivo + ".";
            case "es" ->
                "La Copa del Mundo 2026 es una edicion historica, celebrada en tres paises: " +
                "Estados Unidos, Canada y Mexico. Con 48 selecciones participantes — el mayor numero de la historia — " +
                "el torneo promete ser el mayor espectaculo del deporte mundial. " +
                selecaoNome + " llega con un objetivo claro: " + objetivo + ".";
            case "fr" ->
                "La Coupe du Monde 2026 est une edition historique, organisee dans trois pays: " +
                "les Etats-Unis, le Canada et le Mexique. Avec 48 equipes — le plus grand nombre de l'histoire — " +
                "ce tournoi s'annonce comme le plus grand spectacle sportif du monde. " +
                selecaoNome + " arrive avec un objectif clair: " + objetivo + ".";
            default ->
                "The 2026 FIFA World Cup is a historic edition, held across three countries: " +
                "the United States, Canada and Mexico. With 48 teams — the largest field in history — " +
                "it promises to be the greatest sporting spectacle on earth. " +
                selecaoNome + " arrives with one clear goal: " + objetivo + ".";
        };
    }

    private static String objetivoCopa2026(boolean eCampeao, boolean anfitriao, String idioma) {
        if (anfitriao) {
            return switch (idioma) {
                case PT_BR, PT_PT -> "brilhar em casa e fazer historia diante do seu proprio povo";
                case "es"         -> "brillar en casa y hacer historia ante su propio pueblo";
                case "fr"         -> "briller a domicile et faire l'histoire devant leur propre peuple";
                default           -> "shine on home soil and make history in front of their own fans";
            };
        }
        if (eCampeao) {
            return switch (idioma) {
                case PT_BR, PT_PT -> "defender o estatuto de potencia maxima do futebol mundial";
                case "es"         -> "defender su condicion de potencia maxima del futbol mundial";
                case "fr"         -> "defendre leur statut de puissance maximale du football mondial";
                default           -> "defend their status as the pinnacle of world football";
            };
        }
        return switch (idioma) {
            case PT_BR, PT_PT -> "escrever um novo capitulo glorioso na historia desta selecao";
            case "es"         -> "escribir un nuevo capitulo glorioso en la historia de esta seleccion";
            case "fr"         -> "ecrire un nouveau chapitre glorieux de leur histoire";
            default           -> "write a glorious new chapter in their football history";
        };
    }

    // ── Em Numeros ────────────────────────────────────────────────────────────

    public static String emNumeros(int participacoes, boolean eCampeao, int numTitulos,
                                    String melhorResultado, String estiloJogo,
                                    String rival, String idioma) {
        String header   = headerNumeros(idioma);
        String copas    = linhaParticipacoes(participacoes, idioma);
        String titulos  = linhaTitulos(eCampeao, numTitulos, idioma);
        String melhor   = linhaMelhorResultado(melhorResultado, idioma);
        String estilo   = linhaEstilo(estiloJogo, idioma);

        StringBuilder sb = new StringBuilder(header).append("\n");
        sb.append(BULLET).append(copas).append("\n");
        sb.append(BULLET).append(titulos).append("\n");
        sb.append(BULLET).append(melhor).append("\n");
        sb.append(BULLET).append(estilo);
        if (rival != null) {
            sb.append("\n").append(BULLET).append(linhaRival(rival, idioma));
        }
        return sb.toString();
    }

    private static String headerNumeros(String idioma) {
        return switch (idioma) {
            case "es" -> "EN CIFRAS"; case "fr" -> "EN CHIFFRES";
            case "de" -> "DIE ZAHLEN"; case "it" -> "I NUMERI";
            default   -> "EM NUMEROS";
        };
    }

    private static String linhaParticipacoes(int n, String idioma) {
        return switch (idioma) {
            case "es" -> n + " participaciones en Copas del Mundo";
            case "fr" -> n + " participations en Coupes du Monde";
            case "de" -> n + " Weltmeisterschafts-Teilnahmen";
            case "it" -> n + " partecipazioni ai Mondiali";
            default   -> n + " participacoes em Copas do Mundo";
        };
    }

    private static String linhaTitulos(boolean eCampeao, int numTitulos, String idioma) {
        if (!eCampeao) {
            return switch (idioma) {
                case "es" -> "Aun en busca del 1er titulo mundial";
                case "fr" -> "Toujours en quete du 1er titre mondial";
                case "de" -> "Noch auf der Suche nach dem ersten WM-Titel";
                case "it" -> "Ancora alla ricerca del 1o titolo mondiale";
                default   -> "Ainda na busca do 1.o titulo mundial";
            };
        }
        String sufixo = numTitulos == 1 ? "" : "s";
        return switch (idioma) {
            case "es" -> numTitulos + " titulo" + sufixo + " mundial" + sufixo;
            case "fr" -> numTitulos + " titre" + sufixo + " mondial" + sufixo;
            case "de" -> numTitulos + " Weltmeistertitel";
            case "it" -> numTitulos + " titol" + (numTitulos == 1 ? "o" : "i") + " mondial" + (numTitulos == 1 ? "e" : "i");
            default   -> numTitulos + " titulo" + sufixo + " mundial" + sufixo;
        };
    }

    private static String linhaMelhorResultado(String resultado, String idioma) {
        return switch (idioma) {
            case "es" -> "Mejor resultado: " + resultado;
            case "fr" -> "Meilleur resultat: " + resultado;
            case "de" -> "Bestes Ergebnis: " + resultado;
            case "it" -> "Miglior risultato: " + resultado;
            default   -> "Melhor resultado: " + resultado;
        };
    }

    private static String linhaEstilo(String estilo, String idioma) {
        String trad = traduzirEstilo(estilo, idioma);
        return switch (idioma) {
            case "es" -> "Estilo de juego: " + trad;
            case "fr" -> "Style de jeu: " + trad;
            case "de" -> "Spielstil: " + trad;
            case "it" -> "Stile di gioco: " + trad;
            default   -> "Estilo de jogo: " + trad;
        };
    }

    private static String linhaRival(String rival, String idioma) {
        return switch (idioma) {
            case "es" -> "Gran rival historico: " + rival;
            case "fr" -> "Grand rival historique: " + rival;
            case "de" -> "Historischer Rivale: " + rival;
            case "it" -> "Grande rivale storico: " + rival;
            default   -> "Grande rival historico: " + rival;
        };
    }

    // ── Manifesto do Torcedor ─────────────────────────────────────────────────

    public static String manifesto(String selecaoNome, boolean eCampeao,
                                    String citacao, String autorCitacao, String idioma) {
        String corpo = corpoManifesto(selecaoNome, eCampeao, idioma);
        String frase = "\"" + citacao + "\"\n-- " + autorCitacao;
        return corpo + "\n\n" + frase;
    }

    private static String corpoManifesto(String nome, boolean eCampeao, String idioma) {
        return switch (idioma) {
            case PT_BR, PT_PT -> eCampeao
                ? "Esta selecao nao e apenas uma equipa de futebol. E a identidade de uma nacao, " +
                    "o orgulho de um povo e a esperanca de milhoes. " +
                    nome + " joga com 11 jogadores em campo, mas com milhoes no coracao."
                : "Ha selecoes que ganham titulos, e ha selecoes que ganham coracoes. " +
                    nome + " e das que lutam sem parar, que nunca desistem. " +
                    "O titulo pode nao ter chegado ainda — mas a historia ainda esta a ser escrita. Acredita.";
            case "es" -> eCampeao
                ? "Esta seleccion no es solo un equipo de futbol. Es la identidad de una nacion, " +
                    "el orgullo de un pueblo y la esperanza de millones. " +
                    nome + " juega con 11 jugadores en el campo, pero con millones en el corazon."
                : "Hay selecciones que ganan titulos, y hay selecciones que ganan corazones. " +
                    nome + " es de las que luchan sin parar, que nunca se rinden. " +
                    "El titulo quizas no ha llegado aun, pero la historia todavia se esta escribiendo.";
            case "fr" -> eCampeao
                ? "Cette equipe n'est pas qu'une equipe de football. C'est l'identite d'une nation, " +
                    "la fierte d'un peuple et l'espoir de millions de personnes. " +
                    nome + " joue avec 11 joueurs sur le terrain, mais avec des millions dans le coeur."
                : "Il y a des equipes qui gagnent des titres, et des equipes qui gagnent des coeurs. " +
                    nome + " est de celles qui se battent sans relache, qui ne renoncent jamais. " +
                    "Le titre n'est peut-etre pas encore arrive, mais l'histoire s'ecrit encore.";
            default -> eCampeao
                ? "This team is not just a football squad. It is a nation's identity, " +
                    "a people's pride and the hope of millions. " +
                    nome + " plays with 11 players on the field, but with millions in their hearts."
                : "Some teams win trophies, and some teams win hearts. " +
                    nome + " is one that fights relentlessly, that never gives up. " +
                    "The title may not have arrived yet — but the story is still being written.";
        };
    }

    // ── Descricao de jogador atual ─────────────────────────────────────────────

    public static String descricaoGuerreiro(String nome, String posicao, String clube, String idioma) {
        if (clube == null || clube.isBlank()) {
            return switch (idioma) {
                case "es" -> nome + " (" + posicao + ") -- uno de los pilares de la seleccion actual.";
                case "fr" -> nome + " (" + posicao + ") -- l'un des piliers de l'equipe nationale actuelle.";
                default   -> nome + " (" + posicao + ") -- um dos pilares da selecao atual.";
            };
        }
        return switch (idioma) {
            case "es" -> String.format("%s (%s) -- actualmente en %s, es una de las piezas clave de la seleccion.", nome, posicao, clube);
            case "fr" -> String.format("%s (%s) -- actuellement a %s, est l'une des pieces maitresses de l'equipe.", nome, posicao, clube);
            default   -> String.format("%s (%s) -- atualmente no %s, e uma das pecas-chave da selecao.", nome, posicao, clube);
        };
    }

    // ── Util interno ──────────────────────────────────────────────────────────

    private static String traduzirEstilo(String estilo, String idioma) {
        if (OFENSIVO.equals(estilo)) {
            return switch (idioma) {
                case "es" -> "Ofensivo"; case "fr" -> "Offensif";
                case "de" -> "Offensiv"; case "it" -> "Offensivo";
                default   -> "Ofensivo";
            };
        }
        if (DEFENSIVO.equals(estilo)) {
            return switch (idioma) {
                case "es" -> "Defensivo"; case "fr" -> "Defensif";
                case "de" -> "Defensiv";  case "it" -> "Difensivo";
                default   -> "Defensivo";
            };
        }
        return switch (idioma) {
            case "es" -> "Equilibrado"; case "fr" -> "Equilibre";
            case "de" -> "Ausgewogen";  case "it" -> "Equilibrato";
            default   -> "Equilibrado";
        };
    }
}
