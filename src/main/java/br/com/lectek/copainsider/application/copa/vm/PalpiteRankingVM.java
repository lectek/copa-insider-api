package br.com.lectek.copainsider.application.copa.vm;

public record PalpiteRankingVM(
        String nomeExibicao,
        int totalPalpites,
        int pontos,
        int acertosExatos,
        int acertosResultado
) {}
