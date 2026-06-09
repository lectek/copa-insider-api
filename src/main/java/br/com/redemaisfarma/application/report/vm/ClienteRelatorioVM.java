// src/main/java/br/com/redemaisfarma/application/report/vm/ClienteRelatorioVM.java
package br.com.redemaisfarma.application.report.vm;

import java.math.BigDecimal;

public record ClienteRelatorioVM(String nome, Long qtdPedidos, BigDecimal valorTotal) {}
