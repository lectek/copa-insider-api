package br.com.lectek.copainsider.application.copa.vm;

public record ChatMensagemVM(
    Long id,
    String autorNome,
    String mensagem,
    String tipo,
    String criadoEm
) {}
