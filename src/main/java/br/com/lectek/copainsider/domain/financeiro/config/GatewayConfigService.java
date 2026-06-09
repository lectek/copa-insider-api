/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain.financeiro.config;
import java.util.List;
import java.util.Optional;

public interface GatewayConfigService {
    public List<GatewayConfig> listar(Boolean var1, String var2);

    public Optional<GatewayConfig> buscarPorId(Long var1);

    public Optional<GatewayConfig> buscarAtivaPorProvedor(String var1);

    public GatewayConfig criar(GatewayConfig var1);

    public GatewayConfig atualizar(Long var1, GatewayConfig var2);

    public void remover(Long var1);

    public GatewayConfig ativar(Long var1, boolean var2);
}

