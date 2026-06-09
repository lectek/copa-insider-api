package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.dto.response.RelatorioClienteLinhaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelatorioClienteService {
    Page<RelatorioClienteLinhaDTO> listar(String q, String cpf, String telefone, Pageable pageable);
}
