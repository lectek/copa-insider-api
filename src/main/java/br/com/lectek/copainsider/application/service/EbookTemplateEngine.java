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
            gerarAlmaSeleção(est, dados, idioma),
            gerarLendas(est, dados),
            gerarPartidas(est),
            EbookTextos.historiaCopas(dados.getSelecaoNome(), est.participacoesCopa(),
                    est.eCampeao(), est.numTitulos(), est.resumoAnos(), melhor, idioma),
            gerarGuerreiros(dados, idioma),
            EbookTextos.copa2026(dados.getSelecaoNome(), est.eCampeao(), est.anfitriao2026(), idioma),
            EbookTextos.emNumeros(est.participacoesCopa(), est.eCampeao(), est.numTitulos(),
                    melhor, est.estiloJogo(), est.rivalHistorico(), idioma),
            est.curiosidade(),
            EbookTextos.manifesto(dados.getSelecaoNome(), est.eCampeao(),
                    est.citacao(), est.autorCitacao(), idioma)
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

        // Lendas estáticas com bio da Wikipedia (se disponível nos dados agregados)
        List<SelecaoDataResult.Lenda> lendasApi = dados.getLendas() != null ? dados.getLendas() : List.of();

        for (String nomeLenda : est.legendas()) {
            String bio = lendasApi.stream()
                    .filter(l -> nomeLenda.equalsIgnoreCase(l.nome()))
                    .findFirst()
                    .map(l -> l.biografia().isBlank() ? l.legado() : l.biografia())
                    .orElse("");

            // Fallback: usar tom emocional da seleção se não há bio
            if (bio.isBlank()) {
                bio = dados.getTomEmocional() != null
                        ? nomeLenda + " -- " + dados.getTomEmocional() + "."
                        : nomeLenda + " -- uma lenda desta selecao.";
            }

            lendas.add(new EbookConteudo.Lenda(nomeLenda, "", bio, ""));
            if (lendas.size() >= 4) break; // máximo 4 lendas por ebook
        }
        return lendas;
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
                .map(j -> new EbookConteudo.Guerreiro(
                        j.nome(),
                        j.clube(),
                        j.posicao(),
                        EbookTextos.descricaoGuerreiro(j.nome(), j.posicao(), j.clube(), idioma)))
                .toList();
    }
}
