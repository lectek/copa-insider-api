package br.com.lectek.copainsider.application.copa;

public record JogoConvocado(
    String slug,
    String nome,
    String numero,
    String posicao,   // GL, DF, MC, AT
    String foto,
    String selecaoSlug
) {
    public String posicaoLabel() {
        return switch (posicao) {
            case "GL" -> "Guarda-Redes";
            case "DF" -> "Defesa";
            case "MC" -> "Médio";
            case "AT" -> "Avançado";
            default   -> posicao;
        };
    }
}
