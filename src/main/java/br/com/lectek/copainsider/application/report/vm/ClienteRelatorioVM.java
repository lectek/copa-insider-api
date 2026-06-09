// src/main/java/br/com/lectek/copainsider/application/report/vm/ClienteRelatorioVM.java
package br.com.lectek.copainsider.application.report.vm;

import java.math.BigDecimal;

public record ClienteRelatorioVM(String nome, Long qtdPedidos, BigDecimal valorTotal) {}
