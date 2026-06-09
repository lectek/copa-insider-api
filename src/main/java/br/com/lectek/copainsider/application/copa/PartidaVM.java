package br.com.lectek.copainsider.application.copa;

import java.time.LocalDateTime;

public record PartidaVM(
        Long id,
        String selecaoCasa,
        String slugCasa,
        String bandeiraCasa,
        String selecaoVisitante,
        String slugVisitante,
        String bandeiraVisitante,
        LocalDateTime dataHora,
        String fase,
        String grupo,
        Integer golsCasa,
        Integer golsVisitante,
        StatusPartida status,
        String estadio,
        String cidade
) {
    public boolean aoVivo() { return status == StatusPartida.AO_VIVO; }
    public boolean encerrada() { return status == StatusPartida.ENCERRADA; }
    public boolean agendada() { return status == StatusPartida.AGENDADA; }
    public boolean temResultado() { return golsCasa != null && golsVisitante != null; }
    public String placar() {
        if (!temResultado()) return "- : -";
        return golsCasa + " : " + golsVisitante;
    }

    public enum StatusPartida { AGENDADA, AO_VIVO, ENCERRADA }
}
