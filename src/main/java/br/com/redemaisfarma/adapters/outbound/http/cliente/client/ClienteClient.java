/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.cliente.client;

import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteClient {
    public Optional<ClienteExternal> getById(UUID var1);

    public Optional<ClienteExternal> getByCpf(String var1);

    public List<ClienteExternal> searchByName(String var1, int var2, int var3);

    public ClienteExternal upsert(ClienteExternal var1);
}

