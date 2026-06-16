package br.com.lectek.copainsider.application.copa;

public record ClassificacaoVM(
        String selecao,
        String bandeira,
        String slug,
        int jogos,
        int vitorias,
        int empates,
        int derrotas,
        int gm,
        int gs,
        int pts
) {
    public int saldo() { return gm - gs; }
}
