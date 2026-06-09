package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ProdutoBuscaDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarRequestDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarResponseDTO;
import java.util.List;

public interface CaixaVendaRapidaService {
    List<ProdutoBuscaDTO> buscarProdutos(String termo, int limit);

    VendaRapidaFinalizarResponseDTO finalizar(VendaRapidaFinalizarRequestDTO request);
}
