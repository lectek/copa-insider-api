package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.service.SincronizacaoCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ConditionalOnProperty(
        name = "legacy.sync.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
@RequestMapping(
        path = "/admin/catalogo",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Admin - Catalogo",
        description = "Operacoes administrativas de sincronizacao do catalogo"
)
public class AdminCatalogoController {

    /**
     * Service that performs catalog synchronization.
     */
    private final SincronizacaoCatalogoService syncService;

    /**
     * Triggers full catalog synchronization.
     *
     * @return synchronization summary
     */
    @PostMapping(path = "/sincronizar")
    @PreAuthorize(
            "hasRole('ADMIN') or hasAuthority('ADMIN:CATALOGO:SINCRONIZAR')"
    )
    @Operation(
            summary = "Sincronizar catalogo",
            description = "Executa o processo completo e retorna resumo"
    )
    public ResponseEntity<SincronizacaoCatalogoService.ResumoSync>
            sincronizar() {
        final StopWatch stopWatch = new StopWatch("sincronizar-catalogo");
        stopWatch.start();
        try {
            log.info(
                    "Iniciando sincronizacao de catalogo "
                            + "(trigger manual /admin/catalogo/sincronizar)..."
            );
            final SincronizacaoCatalogoService.ResumoSync resumo =
                    syncService.sincronizarTudo();
            stopWatch.stop();
            log.info(
                    "Sincronizacao finalizada em {} ms. Resultado: {}",
                    stopWatch.getTotalTimeMillis(),
                    resumo
            );
            return ResponseEntity.ok(resumo);
        } catch (IllegalArgumentException exception) {
            stopWatch.stop();
            log.warn(
                    "Falha de validacao apos {} ms: {}",
                    stopWatch.getTotalTimeMillis(),
                    exception.getMessage()
            );
            throw exception;
        } catch (Exception exception) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            log.error(
                    "Erro inesperado na sincronizacao apos {} ms",
                    stopWatch.getTotalTimeMillis(),
                    exception
            );
            throw exception;
        }
    }
}
