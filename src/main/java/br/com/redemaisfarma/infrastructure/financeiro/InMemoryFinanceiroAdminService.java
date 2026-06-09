/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.infrastructure.financeiro;

import br.com.redemaisfarma.application.view.AssinaturaView;
import br.com.redemaisfarma.domain.financeiro.FinanceiroAdminService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(value={"default", "docker", "dev", "prod"})
public class InMemoryFinanceiroAdminService
implements FinanceiroAdminService {
    private final Map<Long, AssinaturaView> db = new LinkedHashMap<Long, AssinaturaView>();

    public InMemoryFinanceiroAdminService() {
        this.db.put(1L, new AssinaturaView(1L, "Fulano da Silva", "Plano VIP", new BigDecimal("149.90"), LocalDate.now().plusDays(12L), "ATIVA"));
        this.db.put(2L, new AssinaturaView(2L, "Maria Souza", "Plano Basic", new BigDecimal("49.90"), LocalDate.now().plusDays(3L), "ATIVA"));
        this.db.put(3L, new AssinaturaView(3L, "Com\u00e9rcio XPTO", "Plano Pro", new BigDecimal("89.90"), LocalDate.now().minusDays(1L), "ATRASADA"));
    }

    @Override
    public List<AssinaturaView> listarAssinaturas() {
        return new ArrayList<AssinaturaView>(this.db.values());
    }

    @Override
    public Optional<AssinaturaView> buscarAssinatura(Long id) {
        return Optional.ofNullable(this.db.get(id));
    }
}
