/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Constraint
 *  jakarta.validation.Payload
 */
package br.com.redemaisfarma.application.validation.annotation;

import br.com.redemaisfarma.application.validation.EmailUnicoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy={EmailUnicoValidator.class})
@Target(value={ElementType.FIELD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EmailUnico {
    public String message() default "E-mail j\u00e1 cadastrado.";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}

