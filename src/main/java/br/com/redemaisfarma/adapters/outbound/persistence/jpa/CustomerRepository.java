/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.CustomerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository
extends JpaRepository<CustomerEntity, Long> {
    public Optional<CustomerEntity> findByEmail(String var1);

    public Optional<CustomerEntity> findByProviderAndProviderUserId(String var1, String var2);
}

