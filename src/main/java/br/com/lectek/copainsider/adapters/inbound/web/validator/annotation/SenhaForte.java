/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Constraint
 *  jakarta.validation.Payload
 */
package br.com.lectek.copainsider.adapters.inbound.web.validator.annotation;

import br.com.lectek.copainsider.application.validation.SenhaForteValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy={SenhaForteValidator.class})
@Target(value={ElementType.FIELD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface SenhaForte {
    public String message() default "A senha deve conter: m\u00ednimo 8 caracteres, uma mai\u00fascula, uma min\u00fascula, um n\u00famero e um caractere especial.";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

    public int minLength() default 8;

    public boolean requireUppercase() default true;

    public boolean requireLowercase() default true;

    public boolean requireDigit() default true;

    public boolean requireSpecial() default true;
}

