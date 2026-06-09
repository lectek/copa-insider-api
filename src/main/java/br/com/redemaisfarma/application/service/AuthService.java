/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.dto.request.LoginRequest
 *  br.com.redemaisfarma.application.dto.response.AuthResponse
 *  br.com.redemaisfarma.application.dto.response.LoginResponseDTO
 *  br.com.redemaisfarma.domain.user.Usuario
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import br.com.redemaisfarma.domain.user.Usuario;

public interface AuthService {
    public boolean isBlocked(String var1);

    public void registerFailedAttempt(String var1);

    public AuthResponse authenticate(String var1, String var2);

    public boolean isClienteVip(String var1);

    public String getIdentificador(Usuario var1);

    public String getPassword(Usuario var1);

    public String getEmail(Usuario var1);

    public LoginResponseDTO login(LoginRequest var1);
}

