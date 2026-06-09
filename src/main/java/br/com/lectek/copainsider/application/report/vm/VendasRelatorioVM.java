// src/main/java/br/com/lectek/copainsider/application/report/vm/VendasRelatorioVM.java
package br.com.lectek.copainsider.application.report.vm;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendasRelatorioVM(LocalDate data, Long qtd, BigDecimal total) {}
