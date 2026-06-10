package br.com.lectek.copainsider.application.copa;

public record FatoCurioso(
        String categoria,
        String icone,
        String titulo,
        String descricao,
        String slugTime
) {
    public FatoCurioso(String categoria, String icone, String titulo, String descricao) {
        this(categoria, icone, titulo, descricao, null);
    }
}
