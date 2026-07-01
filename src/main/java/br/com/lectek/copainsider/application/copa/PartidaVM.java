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
        Integer golsCasaHT,
        Integer golsVisitanteHT,
        StatusPartida status,
        String estadio,
        String cidade,
        String minuto
) {
    public boolean aoVivo() { return status == StatusPartida.AO_VIVO; }
    public boolean encerrada() { return status == StatusPartida.ENCERRADA; }
    public boolean agendada() { return status == StatusPartida.AGENDADA; }
    public boolean temResultado() { return golsCasa != null && golsVisitante != null; }
    public boolean temHalfTime() { return golsCasaHT != null && golsVisitanteHT != null; }
    public String placarHT() {
        if (!temHalfTime()) return null;
        return golsCasaHT + " : " + golsVisitanteHT;
    }
    public String placar() {
        if (!temResultado()) return "- : -";
        return golsCasa + " : " + golsVisitante;
    }

    // Millisegundos UTC absolutos — para conversão de fuso no browser
    public long epochMs() {
        return dataHora.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }

    public enum StatusPartida { AGENDADA, AO_VIVO, ENCERRADA }
}
