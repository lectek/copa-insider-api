package br.com.lectek.copainsider.application.copa;

public record SelecaoVM(
        String nome,
        String slug,
        String bandeira,
        String grupo,
        String confederacao,
        String melhorResultado,
        int participacoes
) {}
