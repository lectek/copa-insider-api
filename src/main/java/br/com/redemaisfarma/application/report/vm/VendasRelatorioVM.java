// src/main/java/br/com/redemaisfarma/application/report/vm/VendasRelatorioVM.java
package br.com.redemaisfarma.application.report.vm;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendasRelatorioVM(LocalDate data, Long qtd, BigDecimal total) {}
