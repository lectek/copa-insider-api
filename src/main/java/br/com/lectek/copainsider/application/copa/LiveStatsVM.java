package br.com.lectek.copainsider.application.copa;

import br.com.lectek.copainsider.application.copa.EspnScoreClient.ESDetail;

import java.util.List;

public record LiveStatsVM(
        String minutoAtual,
        String possessaoCasa,
        String possessaoVisitante,
        String remastesCasa,
        String remastesVisitante,
        String remastesAlvoCasa,
        String remastesAlvoVisitante,
        String faltasCasa,
        String faltasVisitante,
        String cartõesAmarelosCasa,
        String cartõesAmarelosVisitante,
        String cartõesVermelhosCasa,
        String cartõesVermelhosVisitante,
        String cantoCasa,
        String cantoVisitante,
        String offisidesCasa,
        String offisidesVisitante,
        List<ESDetail> eventos
) {
    public boolean temEstatisticas() {
        return !"0".equals(remastesCasa) || !"0".equals(remastesVisitante)
                || !"0".equals(possessaoCasa) || !"0".equals(possessaoVisitante);
    }

    public int possessaoCasaInt() {
        try { return (int) Double.parseDouble(possessaoCasa.replace("%", "")); }
        catch (Exception e) { return 50; }
    }

    public int possessaoVisitanteInt() {
        return 100 - possessaoCasaInt();
    }
}
