package br.com.lectek.copainsider.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "RelatorioClienteLinhaDTO", description = "Linha do relatório de clientes")
public record RelatorioClienteLinhaDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        Long qtdPedidos,          // placeholder por enquanto
        BigDecimal valorTotal     // placeholder por enquanto
) {}
