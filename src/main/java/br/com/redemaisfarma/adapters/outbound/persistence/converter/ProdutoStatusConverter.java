/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.AttributeConverter
 *  jakarta.persistence.Converter
 */
package br.com.redemaisfarma.adapters.outbound.persistence.converter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply=true)
public class ProdutoStatusConverter
implements AttributeConverter<ProdutoStatus, String> {
    private static final String DB_IMPORTADO = "DRAFT";
    private static final String DB_VALIDADO = "APPROVED";
    private static final String DB_PUBLICADO = "PUBLISHED";

    public String convertToDatabaseColumn(ProdutoStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            default -> throw new MatchException(null, null);
            case ProdutoStatus.IMPORTADO -> DB_IMPORTADO;
            case ProdutoStatus.VALIDADO -> DB_VALIDADO;
            case ProdutoStatus.PUBLICADO -> DB_PUBLICADO;
        };
    }

    public ProdutoStatus convertToEntityAttribute(String dbValue) {
        String v;
        if (dbValue == null) {
            return null;
        }
        String string = v = dbValue.trim().toUpperCase();
        int n = -1;
        switch (v.hashCode()) {
            case -1716898105: {
                if (string.equals("IMPORTADO")) {
                    n = 1;
                }
                break;
            }
            case -638646480: {
                if (string.equals("VALIDADO")) {
                    n = 2;
                }
                break;
            }
            case -61451901: {
                if (string.equals("PUBLICADO")) {
                    n = 3;
                }
                break;
            }
            case -60968498: {
                if (string.equals(DB_PUBLICADO)) {
                    n = 3;
                }
                break;
            }
            case 65307009: {
                if (string.equals(DB_IMPORTADO)) {
                    n = 1;
                }
                break;
            }
            case 1967871671: {
                if (string.equals(DB_VALIDADO)) {
                    n = 2;
                }
                break;
            }
        }
        return switch (n) {
            case 1 -> ProdutoStatus.IMPORTADO;
            case 2 -> ProdutoStatus.VALIDADO;
            case 3 -> ProdutoStatus.PUBLICADO;
            default -> throw new IllegalArgumentException("Status inv\u00e1lido no banco: " + dbValue);
        };
    }
}

