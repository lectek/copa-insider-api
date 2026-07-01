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
        List<String> legendas,
        List<PartidaRef> partidas,
        String treinadorAtual,
        String estiloTaticoAtual,
        String historiaRecente
) {
    public record PartidaRef(String titulo, String adversario, String placar, String ano, String narrativa) {}

    public boolean eCampeao() { return !anosTitulosMundo.isEmpty(); }

    public String resumoAnos() {
        return anosTitulosMundo.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    public int numTitulos() { return anosTitulosMundo.size(); }
}
