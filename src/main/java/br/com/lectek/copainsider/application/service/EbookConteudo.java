package br.com.lectek.copainsider.application.service;

import java.util.List;

public record EbookConteudo(
        String selecaoCode,
        String selecaoNome,
        String apelido,
        String corPrimaria,
        String corSecundaria,
        String fraseCapa,
        String logoUrl,
        String almaSeleção,
        List<Lenda> lendas,
        List<PartidaHistorica> partidas,
        String historiaCopas,
        List<Marco> linhaDoTempo,
        List<Guerreiro> guerreirosHoje,
        String equipaAtual,
        String copa2026,
        String emNumeros,
        String sabiaque,
        String manifestoTorcedor,
        String pressaoNarrativa
) {
    public record Lenda(String nome, String apelido, String bioNarrativa, String legado, String fotoUrl) {}
    public record PartidaHistorica(String titulo, String adversario, String placar, String data, String narrativa) {}
    public record Guerreiro(String nome, String clube, String posicao, String descricao, String fotoUrl) {}
    public record Marco(int ano, String titulo, String descricao) {}
}
