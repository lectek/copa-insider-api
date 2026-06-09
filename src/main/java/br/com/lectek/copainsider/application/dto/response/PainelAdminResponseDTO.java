/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.dto.response;

import br.com.lectek.copainsider.domain.enums.StatusPedido;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PainelAdminResponseDTO {
    private String adminNome;
    private long totalPedidos;
    private long clientesAtivos;
    private long qtdProdutos;
    private long qtdPedidosPendentes;
    private long qtdPedidosEntregues;
    private double totalLucro;
    private double ticketMedio;
    private double satisfacaoCliente;
    private LocalDateTime dataUltimoPedido;
    private List<String> categoriasMaisVendidas;
    private List<AlertItemDTO> alertas;
    private Map<StatusPedido, Long> pedidosPorStatus = new EnumMap<StatusPedido, Long>(StatusPedido.class);

    public String getAdminNome() {
        return this.adminNome;
    }

    public void setAdminNome(String adminNome) {
        this.adminNome = adminNome;
    }

    public long getTotalPedidos() {
        return this.totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public long getClientesAtivos() {
        return this.clientesAtivos;
    }

    public void setClientesAtivos(long clientesAtivos) {
        this.clientesAtivos = clientesAtivos;
    }

    public long getQtdProdutos() {
        return this.qtdProdutos;
    }

    public void setQtdProdutos(long qtdProdutos) {
        this.qtdProdutos = qtdProdutos;
    }

    public long getQtdPedidosPendentes() {
        return this.qtdPedidosPendentes;
    }

    public void setQtdPedidosPendentes(long qtdPedidosPendentes) {
        this.qtdPedidosPendentes = qtdPedidosPendentes;
    }

    public long getQtdPedidosEntregues() {
        return this.qtdPedidosEntregues;
    }

    public void setQtdPedidosEntregues(long qtdPedidosEntregues) {
        this.qtdPedidosEntregues = qtdPedidosEntregues;
    }

    public double getTotalLucro() {
        return this.totalLucro;
    }

    public void setTotalLucro(double totalLucro) {
        this.totalLucro = totalLucro;
    }

    public double getTicketMedio() {
        return this.ticketMedio;
    }

    public void setTicketMedio(double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public double getSatisfacaoCliente() {
        return this.satisfacaoCliente;
    }

    public void setSatisfacaoCliente(double satisfacaoCliente) {
        this.satisfacaoCliente = satisfacaoCliente;
    }

    public LocalDateTime getDataUltimoPedido() {
        return this.dataUltimoPedido;
    }

    public void setDataUltimoPedido(LocalDateTime dataUltimoPedido) {
        this.dataUltimoPedido = dataUltimoPedido;
    }

    public List<String> getCategoriasMaisVendidas() {
        return this.categoriasMaisVendidas;
    }

    public void setCategoriasMaisVendidas(List<String> categoriasMaisVendidas) {
        this.categoriasMaisVendidas = categoriasMaisVendidas;
    }

    public List<AlertItemDTO> getAlertas() {
        return this.alertas;
    }

    public void setAlertas(List<AlertItemDTO> alertas) {
        this.alertas = alertas;
    }

    public Map<StatusPedido, Long> getPedidosPorStatus() {
        return this.pedidosPorStatus;
    }

    public void setPedidosPorStatus(Map<StatusPedido, Long> pedidosPorStatus) {
        this.pedidosPorStatus = pedidosPorStatus;
    }
}
