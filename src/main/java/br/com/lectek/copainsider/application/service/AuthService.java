/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.dto.request.LoginRequest
 *  br.com.lectek.copainsider.application.dto.response.AuthResponse
 *  br.com.lectek.copainsider.application.dto.response.LoginResponseDTO
 *  br.com.lectek.copainsider.domain.user.Usuario
 */
package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.dto.request.LoginRequest;
import br.com.lectek.copainsider.application.dto.response.AuthResponse;
import br.com.lectek.copainsider.application.dto.response.LoginResponseDTO;
import br.com.lectek.copainsider.domain.user.Usuario;

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

