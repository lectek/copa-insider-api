/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.port;

import br.com.lectek.copainsider.domain.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientePort {
    public Optional<Cliente> buscarPorId(UUID var1);

    public Optional<Cliente> buscarPorCpf(String var1);

    public List<Cliente> buscarPorNome(String var1, int var2, int var3);

    public Cliente salvarOuAtualizar(Cliente var1);
}

