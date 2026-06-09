/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.lectek.copainsider.domain.financeiro.config;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayConfigRepository
extends JpaRepository<GatewayConfig, Long> {
    public Optional<GatewayConfig> findFirstByProvedorIgnoreCaseAndAtivoTrue(String var1);

    public Optional<GatewayConfig> findTopByProvedorIgnoreCaseOrderByAtualizadoEmDesc(String var1);

    public List<GatewayConfig> findByProvedorIgnoreCaseOrderByAtualizadoEmDesc(String var1);

    public List<GatewayConfig> findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(boolean var1, String var2);

    public List<GatewayConfig> findByAtivoOrderByAtualizadoEmDesc(boolean var1);

    public List<GatewayConfig> findAllByOrderByAtualizadoEmDesc();

    public boolean existsByProvedorIgnoreCaseAndNomeIgnoreCase(String var1, String var2);

    public boolean existsByProvedorIgnoreCaseAndNomeIgnoreCaseAndIdNot(String var1, String var2, Long var3);
}

