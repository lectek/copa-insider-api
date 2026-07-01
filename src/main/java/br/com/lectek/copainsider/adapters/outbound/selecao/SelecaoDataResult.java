package br.com.lectek.copainsider.adapters.outbound.selecao;

import java.util.List;

public class SelecaoDataResult {

    private String selecaoCode;
    private String selecaoNome;
    private String apelido;
    private String idioma;
    private String tomEmocional;
    private String corPrimaria;
    private String corSecundaria;
    private String logoUrl;

    private String historiaFundacao;
    private String significadoCultural;
    private int totalCopas;
    private int titulos;

    private List<Lenda> lendas;
    private List<Partida> partidasHistoricas;
    private List<ParticipacaoCopa> participacoesCopa;
    private List<JogadorAtual> jogadoresAtuais;
    private String contexto2026;

    public SelecaoDataResult() {}

    public record Lenda(
            String nome,
            String apelido,
            String posicao,
            String periodo,
            int gols,
            int jogos,
            int copas,
            String momentoMarcante,
            String legado,
            String biografia,
            String fotoUrl
    ) {}

    public record Partida(
            String adversario,
            String placar,
            String data,
            String local,
            String fase,
            String contextHistorico,
            String momentoDecisivo
    ) {}

    public record ParticipacaoCopa(
            int ano,
            String sede,
            String fase,
            boolean campeao
    ) {}

    public record JogadorAtual(
            String nome,
            String clube,
            String posicao,
            int idade,
            String descricao,
            String fotoUrl
    ) {}

    public String getSelecaoCode()              { return selecaoCode; }
    public void setSelecaoCode(String v)        { this.selecaoCode = v; }

    public String getSelecaoNome()              { return selecaoNome; }
    public void setSelecaoNome(String v)        { this.selecaoNome = v; }

    public String getApelido()                  { return apelido; }
    public void setApelido(String v)            { this.apelido = v; }

    public String getIdioma()                   { return idioma; }
    public void setIdioma(String v)             { this.idioma = v; }

    public String getTomEmocional()             { return tomEmocional; }
    public void setTomEmocional(String v)       { this.tomEmocional = v; }

    public String getCorPrimaria()              { return corPrimaria; }
    public void setCorPrimaria(String v)        { this.corPrimaria = v; }

    public String getCorSecundaria()            { return corSecundaria; }
    public void setCorSecundaria(String v)      { this.corSecundaria = v; }

    public String getLogoUrl()                  { return logoUrl; }
    public void setLogoUrl(String v)            { this.logoUrl = v; }

    public String getHistoriaFundacao()         { return historiaFundacao; }
    public void setHistoriaFundacao(String v)   { this.historiaFundacao = v; }

    public String getSignificadoCultural()      { return significadoCultural; }
    public void setSignificadoCultural(String v){ this.significadoCultural = v; }

    public int getTotalCopas()                  { return totalCopas; }
    public void setTotalCopas(int v)            { this.totalCopas = v; }

    public int getTitulos()                     { return titulos; }
    public void setTitulos(int v)               { this.titulos = v; }

    public List<Lenda> getLendas()              { return lendas; }
    public void setLendas(List<Lenda> v)        { this.lendas = v; }

    public List<Partida> getPartidasHistoricas()        { return partidasHistoricas; }
    public void setPartidasHistoricas(List<Partida> v)  { this.partidasHistoricas = v; }

    public List<ParticipacaoCopa> getParticipacoesCopa()        { return participacoesCopa; }
    public void setParticipacoesCopa(List<ParticipacaoCopa> v)  { this.participacoesCopa = v; }

    public List<JogadorAtual> getJogadoresAtuais()          { return jogadoresAtuais; }
    public void setJogadoresAtuais(List<JogadorAtual> v)    { this.jogadoresAtuais = v; }

    public String getContexto2026()             { return contexto2026; }
    public void setContexto2026(String v)       { this.contexto2026 = v; }
}
