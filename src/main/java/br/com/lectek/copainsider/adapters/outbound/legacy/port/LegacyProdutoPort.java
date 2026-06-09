/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.legacy.port;

import br.com.lectek.copainsider.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface LegacyProdutoPort {
    public List<LegacyProdutoDTO> fetchChangedSince(LocalDateTime var1, int var2, int var3);
}

