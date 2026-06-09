/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.domain.Cliente
 */
package br.com.lectek.copainsider.adapters.outbound.http.mapper;

import br.com.lectek.copainsider.adapters.outbound.http.cliente.model.ClienteExternal;
import br.com.lectek.copainsider.domain.Cliente;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClienteExternalMapper {
    private ClienteExternalMapper() {
    }

    public static Cliente toDomain(ClienteExternal ext) {
        if (ext == null) {
            return null;
        }
        Cliente c = new Cliente();
        c.setNome(ext.getNome());
        c.setCpf(ext.getCpf());
        c.setEmail(ext.getEmail());
        c.setTelefone(ext.getTelefone());
        c.setDataDeNascimento(ext.getDataNascimento());
        c.setAtivo(Boolean.TRUE.equals(ext.getAtivo()));
        return c;
    }

    public static Optional<Cliente> toDomain(Optional<ClienteExternal> extOpt) {
        return extOpt.map(ClienteExternalMapper::toDomain);
    }

    public static List<Cliente> toDomain(List<ClienteExternal> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).map(ClienteExternalMapper::toDomain).collect(Collectors.toList());
    }

    public static ClienteExternal toExternal(Cliente dom) {
        if (dom == null) {
            return null;
        }
        ClienteExternal ext = new ClienteExternal();
        ext.setNome(dom.getNome());
        ext.setCpf(dom.getCpf());
        ext.setEmail(dom.getEmail());
        ext.setTelefone(dom.getTelefone());
        ext.setDataNascimento(dom.getDataDeNascimento());
        ext.setAtivo(dom.isAtivo());
        return ext;
    }

    public static List<ClienteExternal> toExternal(List<Cliente> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).map(ClienteExternalMapper::toExternal).collect(Collectors.toList());
    }
}

