/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.ConstraintValidator
 *  jakarta.validation.ConstraintValidatorContext
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.application.validation;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.validation.annotation.EmailUnico;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class EmailUnicoValidator
implements ConstraintValidator<EmailUnico, String> {
    private final ClienteRepository clienteRepository;

    public EmailUnicoValidator(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String email = value.trim();
        return !this.clienteRepository.existsByEmailIgnoreCase(email);
    }
}

