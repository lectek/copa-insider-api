package br.com.lectek.copainsider.adapters.outbound.selecao;

import java.util.List;
import java.util.stream.Collectors;

public record SelecaoEstatica(
        String alcunha,
        String confederacao,
        int participacoesCopa,
        List<Integer> anosTitulosMundo,
        String melhorResultado,
        String rivalHistorico,
        String estiloJogo,
        String curiosidade,
        String citacao,
        String autorCitacao,
        boolean anfitriao2026,
        List<LendaCurada> legendas,
        List<PartidaRef> partidas,
        String treinadorAtual,
        String estiloTaticoAtual,
        String historiaRecente,
        List<Marco> linhaDoTempo,
        String pressaoNarrativa
) {
    public record PartidaRef(String titulo, String adversario, String placar, String ano, String narrativa) {}

    /** Lenda com biografia curada à mão. Quando bio/legado forem null, o gerador
     *  recorre à biografia dinâmica (TheSportsDB/Wikipedia) como antes. */
    public record LendaCurada(String nome, String periodo, String bio, String legado) {}

    /** Um marco da linha do tempo da seleção (fundação, primeira Copa, cada título, etc.). */
    public record Marco(int ano, String titulo, String descricao) {}

    /** Envolve nomes simples (sem biografia curada ainda) no novo formato,
     *  mantendo o comportamento antigo (biografia dinâmica com fallback genérico). */
    public static List<LendaCurada> nomesSimples(String... nomes) {
        return java.util.Arrays.stream(nomes)
                .map(n -> new LendaCurada(n, null, null, null))
                .toList();
    }

    public boolean eCampeao() { return !anosTitulosMundo.isEmpty(); }

    public String resumoAnos() {
        return anosTitulosMundo.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    public int numTitulos() { return anosTitulosMundo.size(); }
}
