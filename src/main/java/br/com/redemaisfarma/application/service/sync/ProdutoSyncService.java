package br.com.redemaisfarma.application.service.sync;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Adaptador fino para o scheduler existente.
 * Mantém compatibilidade com SincronizarProdutosJob.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBean(SincronizacaoCatalogoService.class)
public class ProdutoSyncService {

    private final SincronizacaoCatalogoService sync;

    /** Retorna o total processado (soma das ações) apenas para log. */
    public int sincronizar() {
        var r = sync.sincronizarTudo();
        return (r.inseridos() + r.atualizados() + r.ignorados());
    }
}
