package br.com.lectek.copainsider.tools;

import br.com.lectek.copainsider.application.service.CatalogoVendaDisponivelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "catalogo-venda.local.bootstrap-enabled", havingValue = "true", matchIfMissing = false)
public class CatalogoVendaDisponivelBootstrap implements ApplicationRunner {

    private final CatalogoVendaDisponivelService catalogoVendaDisponivelService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            CatalogoVendaDisponivelService.ImportacaoResumo resumo =
                    this.catalogoVendaDisponivelService.sincronizarCatalogoDisponivel();
            log.info("[catalogo-venda-bootstrap] finalizado | lidos={} inseridos={} atualizados={} inalterados={} desativados={}",
                    resumo.lidos(),
                    resumo.inseridos(),
                    resumo.atualizados(),
                    resumo.inalterados(),
                    resumo.desativados());
        } catch (Exception ex) {
            log.error("[catalogo-venda-bootstrap] falha ao sincronizar catalogo local do PDF", ex);
        }
    }
}
