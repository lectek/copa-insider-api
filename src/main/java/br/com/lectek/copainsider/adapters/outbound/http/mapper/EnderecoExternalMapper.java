/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.domain.Endereco
 */
package br.com.lectek.copainsider.adapters.outbound.http.mapper;

import br.com.lectek.copainsider.adapters.outbound.http.cliente.model.EnderecoExternal;
import br.com.lectek.copainsider.domain.Endereco;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EnderecoExternalMapper {
    private EnderecoExternalMapper() {
    }

    public static Endereco toDomain(EnderecoExternal ext) {
        if (ext == null) {
            return null;
        }
        Endereco e = new Endereco();
        e.setCep(ext.getCep());
        e.setLogradouro(ext.getLogradouro());
        e.setNumero(ext.getNumero());
        e.setComplemento(ext.getComplemento());
        e.setBairro(ext.getBairro());
        e.setCidade(ext.getCidade());
        e.setUf(ext.getEstado());
        e.setPrincipal(Boolean.TRUE.equals(ext.getPrincipal()));
        return e;
    }

    public static EnderecoExternal toExternal(Endereco dom) {
        if (dom == null) {
            return null;
        }
        EnderecoExternal ext = new EnderecoExternal();
        ext.setCep(dom.getCep());
        ext.setLogradouro(dom.getLogradouro());
        ext.setNumero(dom.getNumero());
        ext.setComplemento(dom.getComplemento());
        ext.setBairro(dom.getBairro());
        ext.setCidade(dom.getCidade());
        ext.setEstado(dom.getUf());
        ext.setPrincipal(dom.isPrincipal());
        return ext;
    }

    public static List<Endereco> toDomain(List<EnderecoExternal> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).map(EnderecoExternalMapper::toDomain).collect(Collectors.toList());
    }

    public static List<EnderecoExternal> toExternal(List<Endereco> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).map(EnderecoExternalMapper::toExternal).collect(Collectors.toList());
    }
}

