/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.dto.response;

public class PainelClienteResponseDTO {
    private String nomeCliente;
    private int pedidosRecentes;
    private double totalGasto;
    private String mensagemBoasVindas;
    private String categoriaMaisComprada;
    private boolean statusVip;

    public PainelClienteResponseDTO(String nomeCliente, int pedidosRecentes, double totalGasto, String mensagemBoasVindas, String categoriaMaisComprada, boolean statusVip) {
        this.nomeCliente = nomeCliente;
        this.pedidosRecentes = pedidosRecentes;
        this.totalGasto = totalGasto;
        this.mensagemBoasVindas = mensagemBoasVindas;
        this.categoriaMaisComprada = categoriaMaisComprada;
        this.statusVip = statusVip;
    }

    public String getNomeCliente() {
        return this.nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getPedidosRecentes() {
        return this.pedidosRecentes;
    }

    public void setPedidosRecentes(int pedidosRecentes) {
        this.pedidosRecentes = pedidosRecentes;
    }

    public double getTotalGasto() {
        return this.totalGasto;
    }

    public void setTotalGasto(double totalGasto) {
        this.totalGasto = totalGasto;
    }

    public String getMensagemBoasVindas() {
        return this.mensagemBoasVindas;
    }

    public void setMensagemBoasVindas(String mensagemBoasVindas) {
        this.mensagemBoasVindas = mensagemBoasVindas;
    }

    public String getCategoriaMaisComprada() {
        return this.categoriaMaisComprada;
    }

    public void setCategoriaMaisComprada(String categoriaMaisComprada) {
        this.categoriaMaisComprada = categoriaMaisComprada;
    }

    public boolean isStatusVip() {
        return this.statusVip;
    }

    public void setStatusVip(boolean statusVip) {
        this.statusVip = statusVip;
    }

    public String toString() {
        return "PainelClienteResponseDTO{nomeCliente='" + this.nomeCliente + "', pedidosRecentes=" + this.pedidosRecentes + ", totalGasto=" + this.totalGasto + ", mensagemBoasVindas='" + this.mensagemBoasVindas + "', categoriaMaisComprada='" + this.categoriaMaisComprada + "', statusVip=" + this.statusVip + "}";
    }
}

