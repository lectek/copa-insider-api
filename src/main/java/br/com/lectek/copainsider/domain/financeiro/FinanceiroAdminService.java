/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain.financeiro;

import br.com.lectek.copainsider.application.view.AssinaturaView;
import java.util.List;
import java.util.Optional;

public interface FinanceiroAdminService {
    public List<AssinaturaView> listarAssinaturas();

    public Optional<AssinaturaView> buscarAssinatura(Long var1);
}

