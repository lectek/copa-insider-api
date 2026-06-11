package br.com.lectek.copainsider.application.copa;

public record EncuentroVM(
        int ano,
        String fase,
        String bandeira1,
        String selecao1,
        int gols1,
        int gols2,
        String selecao2,
        String bandeira2,
        String competicao,
        String iconeCompeticao
) {
    public boolean isFinal()    { return "Final".equals(fase); }
    public boolean isSemi()     { return "Semifinais".equals(fase); }
    public boolean isKnockout() { return !fase.contains("grupos"); }
}
