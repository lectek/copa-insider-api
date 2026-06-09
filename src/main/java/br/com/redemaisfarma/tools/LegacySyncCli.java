/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.ApplicationArguments
 *  org.springframework.boot.ApplicationRunner
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value={"legacy"})
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true", matchIfMissing = false)
public class LegacySyncCli
implements ApplicationRunner {
    private final SincronizacaoCatalogoService sync;
    @Value(value="${legacy.sync.run:false}")
    private boolean run;

    public LegacySyncCli(SincronizacaoCatalogoService sync) {
        this.sync = sync;
    }

    public void run(ApplicationArguments args) {
        if (!this.run) {
            return;
        }
        System.out.println("[legacy-sync] Iniciando sincroniza\u00e7\u00e3o Firebird -> MySQL (estoque=0) ...");
        SincronizacaoCatalogoService.ResumoSync resumo = this.sync.sincronizarTudo();
        System.out.printf("[legacy-sync] OK | lidos=%d inseridos=%d atualizados=%d ignorados=%d erros=%d%n", resumo.lidos(), resumo.inseridos(), resumo.atualizados(), resumo.ignorados(), resumo.erros());
        System.exit(0);
    }
}
