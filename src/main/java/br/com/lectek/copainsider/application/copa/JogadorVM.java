package br.com.lectek.copainsider.application.copa;

public record JogadorVM(
        Long id,
        String nome,
        String selecao,
        String slugSelecao,
        String bandeira,
        String posicao,
        String clube,
        int idade,
        int gols,
        int assistencias,
        int jogos,
        double nota,
        String foto
) {}
