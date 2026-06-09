package br.com.lectek.copainsider.application.copa;

public record RivalidadeVM(
        String id,
        String selecao1,
        String slug1,
        String bandeira1,
        String selecao2,
        String slug2,
        String bandeira2,
        int vitorias1,
        int empates,
        int vitorias2,
        String jogoFamoso,
        String descricao
) {}
