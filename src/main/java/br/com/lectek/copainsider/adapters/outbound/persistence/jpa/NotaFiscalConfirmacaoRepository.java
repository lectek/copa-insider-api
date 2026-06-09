/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.NotaFiscalConfirmacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaFiscalConfirmacaoRepository
extends JpaRepository<NotaFiscalConfirmacaoEntity, Long> {
}

