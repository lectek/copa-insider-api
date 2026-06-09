/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.redemaisfarma.domain.user;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository
extends JpaRepository<Role, Long> {
    public Optional<Role> findByNome(String var1);
}

