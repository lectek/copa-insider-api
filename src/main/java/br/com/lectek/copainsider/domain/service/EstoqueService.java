/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain.service;

public interface EstoqueService {
    public boolean temDisponivel(Long var1, int var2);

    public void baixar(Long var1, int var2, String var3);
}

