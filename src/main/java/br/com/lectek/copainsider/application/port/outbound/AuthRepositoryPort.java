/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.port.outbound;

import br.com.lectek.copainsider.domain.user.Usuario;
import java.util.Optional;

public interface AuthRepositoryPort {
    public Optional<Usuario> findByIdentifier(String var1);

    public boolean isBlocked(String var1);

    public void registerFailedAttempt(String var1);

    public void resetFailedAttempts(String var1);

    public boolean updateLastAccess(Long var1);

    public void save(Usuario var1);

    public boolean existsByEmail(String var1);

    public Usuario authenticate(String var1, String var2);

    public boolean isClienteVip(String var1);
}

