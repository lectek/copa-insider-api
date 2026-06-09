/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AuthMapper {
    private AuthMapper() {
    }

    public static LoginResponseDTO toLoginResponseDTO(AuthResponse auth) {
        LoginResponseDTO.UserType userType = LoginResponseDTO.UserType.CLIENTE;
        if (auth.getRoles() != null) {
            if (auth.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ADMIN"))) {
                userType = LoginResponseDTO.UserType.ADMIN;
            } else if (auth.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ATENDENTE"))) {
                userType = LoginResponseDTO.UserType.ATENDENTE;
            }
        }
        return new LoginResponseDTO(auth.getAccessToken(), auth.getRefreshToken(), userType, auth.getUserId(), auth.getUsername(), auth.getEmail(), auth.getRoles(), auth.getExpiresAt(), LocalDateTime.now(), LoginResponseDTO.AccountStatus.ACTIVE, "Bem-vindo!", auth.getTenantId() != null ? auth.getTenantId() : "rede-mais-farma", auth.getTraceId() != null ? auth.getTraceId() : UUID.randomUUID());
    }
}

