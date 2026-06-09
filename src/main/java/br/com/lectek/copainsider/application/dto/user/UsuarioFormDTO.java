// src/main/java/br/com/lectek/copainsider/application/dto/user/UsuarioFormDTO.java
package br.com.lectek.copainsider.application.dto.user;

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
