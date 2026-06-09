package br.com.lectek.copainsider.domain.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BarcodeNormalizerTest {

    @Test
    void normalizeConvertsScientificNotationWithComma() {
        assertThat(BarcodeNormalizer.normalize("7,89671E+12"))
                .isEqualTo("7896710000000");
    }

    @Test
    void normalizeKeepsOnlyDigitsFromPlainText() {
        assertThat(BarcodeNormalizer.normalize(" 789-6000 033445 "))
                .isEqualTo("7896000033445");
    }

    @Test
    void normalizeOrNullReturnsNullWhenBlank() {
        assertThat(BarcodeNormalizer.normalizeOrNull("   "))
                .isNull();
    }
}
