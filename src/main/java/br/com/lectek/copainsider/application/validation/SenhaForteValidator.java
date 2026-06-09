/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.ConstraintValidator
 *  jakarta.validation.ConstraintValidatorContext
 */
package br.com.lectek.copainsider.application.validation;

import br.com.lectek.copainsider.adapters.inbound.web.validator.annotation.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {
    private static final String DIGIT_PATTERN = ".*\\d.*";
    private static final String UPPER_PATTERN = ".*[A-Z].*";
    private static final String LOWER_PATTERN = ".*[a-z].*";
    private static final String SPECIAL_PATTERN = ".*[^A-Za-z0-9].*";

    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null) {
            return false;
        }
        if (senha.length() < 8) {
            return false;
        }
        return senha.matches(DIGIT_PATTERN)
                && senha.matches(UPPER_PATTERN)
                && senha.matches(LOWER_PATTERN)
                && senha.matches(SPECIAL_PATTERN);
    }
}
