package br.com.lectek.copainsider.domain.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

public final class BarcodeNormalizer {

    private BarcodeNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String raw = value.trim()
                .replace('\u00A0', ' ')
                .replace(" ", "");
        if (raw.isBlank()) {
            return "";
        }

        if (raw.contains("E") || raw.contains("e")) {
            try {
                String normalizedScientific = raw.replace(',', '.');
                BigDecimal asNumber = new BigDecimal(normalizedScientific, MathContext.DECIMAL128);
                return asNumber.toPlainString().replaceAll("\\D+", "");
            } catch (Exception ignored) {
                // fallback below
            }
        }

        if (raw.indexOf(',') >= 0 && raw.indexOf('.') >= 0) {
            String localeAware = raw.toUpperCase(Locale.ROOT).replace(".", "").replace(',', '.');
            try {
                BigDecimal asNumber = new BigDecimal(localeAware, MathContext.DECIMAL128);
                return asNumber.toPlainString().replaceAll("\\D+", "");
            } catch (Exception ignored) {
                // fallback below
            }
        }

        return raw.replaceAll("\\D+", "");
    }

    public static String normalizeOrNull(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? null : normalized;
    }
}
