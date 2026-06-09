// br/com/lectek/copainsider/application/support/RelatorioAssembler.java
package br.com.lectek.copainsider.application.support;

import br.com.lectek.copainsider.application.dto.response.FiltroRelatorioResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public final class RelatorioAssembler {
    private RelatorioAssembler(){}

    public static FiltroRelatorioResponseDTO novoBasico(String usuario, String tenant, Map<String,String> filtros) {
        FiltroRelatorioResponseDTO dto = new FiltroRelatorioResponseDTO();
        dto.setRelatorioId(UUID.randomUUID());
        dto.setDataGeracao(LocalDateTime.now());
        dto.setUsuarioSolicitante(usuario);
        dto.setTenantId(tenant);
        dto.setFiltrosAplicados(filtros != null ? filtros : Collections.emptyMap());
        dto.setMetricasAgregadas(new LinkedHashMap<>());
        dto.setOrdenacao(new ArrayList<>());
        return dto;
    }

    public static void preencherPaginacao(FiltroRelatorioResponseDTO alvo, org.springframework.data.domain.Page<?> page) {
        alvo.setPaginacao(FiltroRelatorioResponseDTO.PaginacaoDTO.fromPage(page));
    }

    public static void addMetrica(FiltroRelatorioResponseDTO alvo, String chave, BigDecimal valor) {
        if (alvo.getMetricasAgregadas() == null) alvo.setMetricasAgregadas(new LinkedHashMap<>());
        alvo.getMetricasAgregadas().put(chave, valor != null ? valor : BigDecimal.ZERO);
    }
}
