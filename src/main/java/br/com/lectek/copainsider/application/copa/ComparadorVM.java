package br.com.lectek.copainsider.application.copa;

import java.util.List;

public record ComparadorVM(
        SelecaoVM selecao1,
        SelecaoVM selecao2,
        int vitorias1,
        int empates,
        int vitorias2,
        int golsTotal1,
        int golsTotal2,
        List<EncuentroVM> encontros,
        PartidaVM encontroCopa2026
) {
    public int totalJogos()  { return vitorias1 + empates + vitorias2; }
    public boolean temHistorico() { return !encontros.isEmpty(); }

    public int pct1() {
        if (totalJogos() == 0) return 50;
        return (int) Math.round(vitorias1 * 100.0 / totalJogos());
    }
}
