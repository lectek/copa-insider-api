/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.port.ClientePort
 *  br.com.lectek.copainsider.domain.Cliente
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.adapters.outbound.http.adapter;

import br.com.lectek.copainsider.adapters.outbound.http.cliente.client.ClienteClient;
import br.com.lectek.copainsider.adapters.outbound.http.cliente.model.ClienteExternal;
import br.com.lectek.copainsider.adapters.outbound.http.mapper.ClienteExternalMapper;
import br.com.lectek.copainsider.application.port.ClientePort;
import br.com.lectek.copainsider.domain.Cliente;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="integrations.cliente", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class ClienteHttpAdapter
implements ClientePort {
    private final ClienteClient client;

    public ClienteHttpAdapter(ClienteClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public Optional<Cliente> buscarPorId(UUID id) {
        return this.client.getById(id).map(ClienteExternalMapper::toDomain);
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        return this.client.getByCpf(cpf).map(ClienteExternalMapper::toDomain);
    }

    public List<Cliente> buscarPorNome(String nome, int page, int size) {
        List<ClienteExternal> list = this.client.searchByName(nome, page, size);
        return list.stream().map(ClienteExternalMapper::toDomain).collect(Collectors.toList());
    }

    public Cliente salvarOuAtualizar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("cliente n\u00e3o pode ser nulo");
        }
        ClienteExternal payload = ClienteExternalMapper.toExternal(cliente);
        ClienteExternal saved = this.client.upsert(payload);
        return ClienteExternalMapper.toDomain(saved);
    }
}

