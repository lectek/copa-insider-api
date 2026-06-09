/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.domain.support;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class ProdutoHashUtil {
    private ProdutoHashUtil() {
    }

    public static String buildHash(String codigoBarras, String nome, String descricao, BigDecimal precoVenda, Long legacyId) {
        String payload = String.join((CharSequence)"|", ProdutoHashUtil.nullToEmpty(codigoBarras), ProdutoHashUtil.nullToEmpty(nome), ProdutoHashUtil.nullToEmpty(descricao), precoVenda == null ? "" : precoVenda.stripTrailingZeros().toPlainString(), legacyId == null ? "" : legacyId.toString());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        }
        catch (Exception e) {
            return Integer.toHexString(payload.hashCode());
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}

