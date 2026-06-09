// src/main/java/br/com/redemaisfarma/application/dto/user/UsuarioFormDTO.java
package br.com.redemaisfarma.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioFormDTO(
        Long id,
        @NotBlank String nome,
        @Email String email,
        String cpf,
        String papel,   // ADMIN, FARMACEUTICO, CAIXA, CLIENTE_VIP
        boolean ativo
) {}
