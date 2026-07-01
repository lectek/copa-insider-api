package br.com.lectek.copainsider.adapters.outbound.selecao;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class SelecaoDataAggregator {

    private static final Logger log = LoggerFactory.getLogger(SelecaoDataAggregator.class);

    // Metadados estáticos por seleção (tom emocional, apelido, cores)
    private static final Map<String, String[]> META = Map.ofEntries(
            Map.entry("BRA", new String[]{"A Canarinha", "Alegria, ginga e jogo bonito", "#009C3B", "#FFDF00"}),
            Map.entry("ARG", new String[]{"La Albiceleste", "Garra, sofrimento épico e glória", "#74ACDF", "#FFFFFF"}),
            Map.entry("FRA", new String[]{"Les Bleus", "Elegância, multiculturalidade e frieza clínica", "#002395", "#FFFFFF"}),
            Map.entry("POR", new String[]{"A Seleção das Quinas", "Saudade, luta e orgulho ibérico", "#006600", "#FF0000"}),
            Map.entry("ESP", new String[]{"La Roja", "Toque, posse e beleza coletiva", "#AA151B", "#F1BF00"}),
            Map.entry("ALE", new String[]{"Die Mannschaft", "Eficiência, mentalidade vencedora e organização", "#000000", "#FFFFFF"}),
            Map.entry("ENG", new String[]{"The Three Lions", "Berço do futebol e expectativa eterna", "#FFFFFF", "#CF081F"}),
            Map.entry("ITA", new String[]{"Gli Azzurri", "Arte tática, defesa sólida e ressurgimento", "#0066CC", "#FFFFFF"}),
            Map.entry("URU", new String[]{"La Celeste", "Tradição, garra e futebol raiz", "#75AADB", "#FFFFFF"}),
            Map.entry("MEX", new String[]{"El Tri", "Festa, paixão e o quinto jogo", "#006847", "#FFFFFF"}),
            Map.entry("USA", new String[]{"The Stars and Stripes", "Crescimento, ambição e sede local", "#B22234", "#FFFFFF"}),
            Map.entry("CAN", new String[]{"Les Rouges", "Estreia histórica e nova geração", "#FF0000", "#FFFFFF"}),
            Map.entry("MAR", new String[]{"Les Lions de l'Atlas", "Orgulho africano e árabe", "#C1272D", "#006233"})
    );

    private final TheSportsDBClient sportsDBClient;
    private final WikipediaClient wikipediaClient;
    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(8,
            r -> new Thread(r, "selecao-data-io"));

    public SelecaoDataAggregator(TheSportsDBClient sportsDBClient, WikipediaClient wikipediaClient) {
        this.sportsDBClient = sportsDBClient;
        this.wikipediaClient = wikipediaClient;
    }

    @PreDestroy
    void shutdown() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ioExecutor.shutdownNow();
        }
    }

    public SelecaoDataResult agregar(String selecaoCode, String idioma) {
        log.info("[aggregator] agregando dados — selecao={} idioma={}", selecaoCode, idioma);

        String code = selecaoCode.toUpperCase();
        String[] meta = META.getOrDefault(code, new String[]{code, "Futebol, paixão e identidade nacional", "#FFFFFF", "#000000"});

        SelecaoDataResult result = new SelecaoDataResult();
        result.setSelecaoCode(code);
        result.setIdioma(idioma);
        result.setApelido(meta[0]);
        result.setTomEmocional(meta[1]);
        result.setCorPrimaria(meta[2]);
        result.setCorSecundaria(meta[3]);

        // As três chamadas iniciais são independentes entre si — correm em paralelo
        CompletableFuture<TheSportsDBClient.TeamInfo> teamInfoF =
                CompletableFuture.supplyAsync(() -> sportsDBClient.buscarEquipa(code), ioExecutor);
        CompletableFuture<String> wikiResumoF =
                CompletableFuture.supplyAsync(() -> wikipediaClient.buscarResumo(code, idioma), ioExecutor);
        CompletableFuture<List<TheSportsDBClient.Player>> jogadoresF =
                CompletableFuture.supplyAsync(() -> sportsDBClient.buscarJogadores(code), ioExecutor);

        TheSportsDBClient.TeamInfo teamInfo = teamInfoF.join();
        result.setSelecaoNome(teamInfo.nome() != null ? teamInfo.nome() : code);
        result.setLogoUrl(teamInfo.logoUrl());
        if (teamInfo.descricao() != null && !teamInfo.descricao().isBlank()) {
            result.setHistoriaFundacao(teamInfo.descricao());
        }

        String wikiResumo = wikiResumoF.join();
        if (!wikiResumo.isBlank()) {
            if (result.getHistoriaFundacao() == null) {
                result.setHistoriaFundacao(wikiResumo);
            }
            result.setSignificadoCultural(wikiResumo);
        }

        // Jogadores — filtra lendas (sem clube atual = aposentado) e ativos
        List<TheSportsDBClient.Player> jogadores = jogadoresF.join();

        List<SelecaoDataResult.JogadorAtual> atuais = jogadores.stream()
                .filter(p -> p.strPlayer() != null)
                .limit(6)
                .map(p -> new SelecaoDataResult.JogadorAtual(
                        p.strPlayer(),
                        "",
                        p.strPosition() != null ? p.strPosition() : "",
                        0,
                        p.strDescriptionPT() != null ? p.strDescriptionPT()
                                : (p.strDescriptionEN() != null ? p.strDescriptionEN() : ""),
                        p.strThumb()
                ))
                .toList();
        result.setJogadoresAtuais(atuais);

        // Lendas — biografias da Wikipedia buscadas em paralelo (até 8 chamadas concorrentes)
        List<TheSportsDBClient.Player> candidatosLenda = jogadores.stream()
                .filter(p -> p.strPlayer() != null)
                .filter(p -> p.strDescriptionEN() != null || p.strDescriptionPT() != null)
                .limit(8)
                .toList();

        List<CompletableFuture<SelecaoDataResult.Lenda>> lendaFutures = candidatosLenda.stream()
                .map(p -> CompletableFuture.supplyAsync(() -> {
                    String bio = wikipediaClient.buscarResumoJogador(p.strPlayer(), idioma);
                    String desc = p.strDescriptionPT() != null ? p.strDescriptionPT()
                            : (p.strDescriptionEN() != null ? p.strDescriptionEN() : "");
                    return new SelecaoDataResult.Lenda(
                            p.strPlayer(), "", p.strPosition() != null ? p.strPosition() : "",
                            "", 0, 0, 0, "", bio.isBlank() ? desc : bio, bio.isBlank() ? desc : bio,
                            p.strThumb()
                    );
                }, ioExecutor))
                .toList();
        List<SelecaoDataResult.Lenda> lendas = lendaFutures.stream().map(CompletableFuture::join).toList();
        result.setLendas(lendas);

        // Partidas e Copas — ficam vazias aqui; Claude enriquece com o seu conhecimento
        result.setPartidasHistoricas(List.of());
        result.setParticipacoesCopa(List.of());
        result.setContexto2026("Copa do Mundo 2026 — sede: EUA, Canadá e México");

        log.info("[aggregator] dados prontos — selecao={} jogadores={}", code, jogadores.size());
        return result;
    }
}
