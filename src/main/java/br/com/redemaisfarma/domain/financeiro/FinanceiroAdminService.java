/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.domain.financeiro;

import br.com.redemaisfarma.application.view.AssinaturaView;
import java.util.List;
import java.util.Optional;

public interface FinanceiroAdminService {
    public List<AssinaturaView> listarAssinaturas();

    public Optional<AssinaturaView> buscarAssinatura(Long var1);
}

