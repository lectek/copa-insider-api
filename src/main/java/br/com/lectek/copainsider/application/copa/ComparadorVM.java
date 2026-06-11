package br.com.lectek.copainsider.application.copa;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ComparadorVM(
        SelecaoVM selecao1,
        SelecaoVM selecao2,
        int vitorias1,
        int empates,
        int vitorias2,
        int golsTotal1,
        int golsTotal2,
        List<EncuentroVM> encontros,
        PartidaVM encontroCopa2026,
        List<LendaVM> lendas
) {
    public int totalJogos()  { return vitorias1 + empates + vitorias2; }
    public boolean temHistorico() { return !encontros.isEmpty(); }

    public int pct1() {
        if (totalJogos() == 0) return 50;
        return (int) Math.round(vitorias1 * 100.0 / totalJogos());
    }

    public Map<String, List<EncuentroVM>> encontrosPorCompeticao() {
        return encontros.stream()
                .collect(Collectors.groupingBy(EncuentroVM::competicao,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted((a, b) -> Integer.compare(b.ano(), a.ano()))
                                        .toList()
                        )));
    }

    public List<LendaVM> lendasTime1() {
        return lendas.stream()
                .filter(l -> selecao1.slug().equals(l.slugTime()))
                .toList();
    }

    public List<LendaVM> lendasTime2() {
        return lendas.stream()
                .filter(l -> selecao2.slug().equals(l.slugTime()))
                .toList();
    }
}
