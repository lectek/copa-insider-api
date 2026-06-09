package br.com.redemaisfarma.adapters.outbound.persistence.entity;

public enum TarjaMedicacao {
    SEM_TARJA(
            "Sem Tarja",
            "Venda livre. Nao exige receita medica."
    ),
    TARJA_AMARELA(
            "Tarja Amarela",
            "Medicamentos genericos que podem ou nao exigir receita medica."
    ),
    TARJA_VERMELHA(
            "Tarja Vermelha",
            "Exige prescricao medica, com ou sem retencao da receita."
    ),
    TARJA_PRETA(
            "Tarja Preta",
            "Medicamento controlado. Exige receita especial com retencao."
    );

    private final String rotulo;
    private final String descricaoRegulatoria;

    TarjaMedicacao(String rotulo, String descricaoRegulatoria) {
        this.rotulo = rotulo;
        this.descricaoRegulatoria = descricaoRegulatoria;
    }

    public String getRotulo() {
        return this.rotulo;
    }

    public String getDescricaoRegulatoria() {
        return this.descricaoRegulatoria;
    }

    public boolean exigeReceitaPorRegra() {
        return this == TARJA_VERMELHA || this == TARJA_PRETA;
    }
}
