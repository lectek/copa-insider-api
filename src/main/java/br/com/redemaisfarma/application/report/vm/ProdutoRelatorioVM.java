package br.com.redemaisfarma.application.report.vm;

import java.math.BigDecimal;

/**
 * ViewModel usada na tela "Relatórios • Produtos".
 * Representa um resumo de vendas por produto.
 */
public record ProdutoRelatorioVM(
        String nome,
        Long qtd,
        BigDecimal total
) {}
