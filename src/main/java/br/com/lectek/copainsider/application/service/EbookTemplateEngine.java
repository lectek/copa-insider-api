package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDadosEstaticos;
import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDataResult;
import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoEstatica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EbookTemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(EbookTemplateEngine.class);

    public EbookConteudo gerar(SelecaoDataResult dados, String idioma) {
        String code = dados.getSelecaoCode();
        SelecaoEstatica est = SelecaoDadosEstaticos.get(code);
        log.info("[template] gerando conteudo — selecao={} idioma={} campeao={}", code, idioma, est.eCampeao());

        // O nome da seleção nunca deve vir cru da TheSportsDB (devolve sempre em
        // inglês, ex: "Brazil"). É sempre substituído pelo nome localizado no
        // idioma do ebook, para nunca haver mistura de idiomas no texto gerado.
        dados.setSelecaoNome(SelecaoDadosEstaticos.nomeLocalizado(code, idioma, dados.getSelecaoNome()));

        String melhor = est.eCampeao()
                ? (est.numTitulos() + "x Campeao Mundial (" + est.resumoAnos() + ")")
                : est.melhorResultado();

        return new EbookConteudo(
            code,
            dados.getSelecaoNome(),
            est.alcunha(),
            dados.getCorPrimaria(),
            dados.getCorSecundaria(),
            EbookTextos.fraseCapa(est.eCampeao(), idioma),
            dados.getLogoUrl(),
            gerarAlmaSeleção(est, dados, idioma),
            gerarLendas(est, dados),
            gerarPartidas(est),
            EbookTextos.historiaCopas(dados.getSelecaoNome(), est.participacoesCopa(),
                    est.eCampeao(), est.numTitulos(), est.resumoAnos(), melhor, idioma),
            gerarLinhaDoTempo(est),
            gerarGuerreiros(dados, idioma),
            EbookTextos.equipaAtual(dados.getSelecaoNome(), est.treinadorAtual(),
                    est.estiloTaticoAtual(), est.historiaRecente(), idioma),
            EbookTextos.copa2026(dados.getSelecaoNome(), est.eCampeao(), est.anfitriao2026(), idioma),
            EbookTextos.emNumeros(est.participacoesCopa(), est.eCampeao(), est.numTitulos(),
                    melhor, est.estiloJogo(), est.rivalHistorico(), idioma),
            est.curiosidade(),
            EbookTextos.manifesto(dados.getSelecaoNome(), est.eCampeao(),
                    est.citacao(), est.autorCitacao(), idioma),
            est.pressaoNarrativa()
        );
    }

    // ── Alma da Seleção ───────────────────────────────────────────────────────

    private String gerarAlmaSeleção(SelecaoEstatica est, SelecaoDataResult dados, String idioma) {
        String intro = EbookTextos.introAlma(
                dados.getSelecaoNome(), est.alcunha(),
                est.eCampeao(), est.numTitulos(), est.resumoAnos(), idioma);

        // Wikipedia intro — já vem no idioma correcto via WikipediaClient
        String wiki = dados.getSignificadoCultural();
        if (wiki != null && !wiki.isBlank()) {
            String resumoWiki = wiki.length() > 800 ? wiki.substring(0, 800) + "..." : wiki;
            return intro + "\n\n" + resumoWiki;
        }
        return intro + "\n\n" + est.curiosidade();
    }

    // ── Lendas ────────────────────────────────────────────────────────────────

    private List<EbookConteudo.Lenda> gerarLendas(SelecaoEstatica est, SelecaoDataResult dados) {
        List<EbookConteudo.Lenda> lendas = new ArrayList<>();

        // Lendas dinâmicas (TheSportsDB/Wikipedia) — usadas só como reforço/fallback
        // quando não existe biografia curada à mão para o nome em questão.
        List<SelecaoDataResult.Lenda> lendasApi = dados.getLendas() != null ? dados.getLendas() : List.of();

        for (SelecaoEstatica.LendaCurada curada : est.legendas()) {
            String nomeLenda = curada.nome();

            // Prioridade 1: biografia curada à mão (rica, única, sem depender de APIs externas)
            if (curada.bio() != null && !curada.bio().isBlank()) {
                String fotoUrl = lendasApi.stream()
                        .filter(l -> nomeLenda.equalsIgnoreCase(l.nome()))
                        .findFirst()
                        .map(SelecaoDataResult.Lenda::fotoUrl)
                        .orElse(null);
                String apelido = curada.periodo() != null ? curada.periodo() : "";
                lendas.add(new EbookConteudo.Lenda(nomeLenda, apelido, curada.bio(),
                        curada.legado() != null ? curada.legado() : "", fotoUrl));
                if (lendas.size() >= 8) break;
                continue;
            }

            // Prioridade 2 (fallback): biografia dinâmica da Wikipedia/TheSportsDB
            SelecaoDataResult.Lenda match = lendasApi.stream()
                    .filter(l -> nomeLenda.equalsIgnoreCase(l.nome()))
                    .findFirst()
                    .orElse(null);

            String bio = match != null
                    ? (match.biografia().isBlank() ? match.legado() : match.biografia())
                    : "";

            if (bio.isBlank()) {
                bio = dados.getTomEmocional() != null
                        ? nomeLenda + " é uma das figuras mais marcantes da história desta seleção, símbolo do "
                          + dados.getTomEmocional() + " que define o seu futebol."
                        : nomeLenda + " é uma das lendas que marcaram a história desta seleção no futebol mundial.";
            }

            String fotoUrl = match != null ? match.fotoUrl() : null;
            lendas.add(new EbookConteudo.Lenda(nomeLenda, "", bio, "", fotoUrl));
            if (lendas.size() >= 8) break; // até 8 lendas por ebook
        }
        return lendas;
    }

    // ── Linha do tempo ────────────────────────────────────────────────────────

    private List<EbookConteudo.Marco> gerarLinhaDoTempo(SelecaoEstatica est) {
        return est.linhaDoTempo().stream()
                .map(m -> new EbookConteudo.Marco(m.ano(), m.titulo(), m.descricao()))
                .toList();
    }

    // ── Partidas históricas ───────────────────────────────────────────────────

    private List<EbookConteudo.PartidaHistorica> gerarPartidas(SelecaoEstatica est) {
        return est.partidas().stream()
                .map(p -> new EbookConteudo.PartidaHistorica(
                        p.titulo(), p.adversario(), p.placar(), p.ano(), p.narrativa()))
                .toList();
    }

    // ── Guerreiros de hoje ────────────────────────────────────────────────────

    private List<EbookConteudo.Guerreiro> gerarGuerreiros(SelecaoDataResult dados, String idioma) {
        List<SelecaoDataResult.JogadorAtual> jogadores =
                dados.getJogadoresAtuais() != null ? dados.getJogadoresAtuais() : List.of();

        return jogadores.stream()
                .limit(5)
                .map(j -> {
                    String posicao = EbookTextos.traduzirPosicao(j.posicao(), idioma);
                    return new EbookConteudo.Guerreiro(
                            j.nome(),
                            j.clube(),
                            posicao,
                            EbookTextos.descricaoGuerreiro(j.nome(), posicao, j.clube(), idioma),
                            j.fotoUrl());
                })
                .toList();
    }
}
