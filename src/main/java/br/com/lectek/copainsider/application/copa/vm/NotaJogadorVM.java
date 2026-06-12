package br.com.lectek.copainsider.application.copa.vm;

public record NotaJogadorVM(
    String slug,
    String nome,
    String posicao,
    String posicaoLabel,
    String numero,
    String foto,
    String selecaoSlug,
    double media,
    int totalVotos,
    boolean votoMinimo,
    Integer meuVoto
) {}
