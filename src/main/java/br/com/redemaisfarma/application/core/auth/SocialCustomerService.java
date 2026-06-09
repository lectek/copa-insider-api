/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.core.auth;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.CustomerEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.CustomerRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialCustomerService {
    private final CustomerRepository repo;

    public SocialCustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void linkOrCreate(String provider, String providerUserId, String email, String nome, String avatarUrl, boolean emailVerificado) {
        CustomerEntity e = null;
        if (email != null && !email.isBlank()) {
            e = this.repo.findByEmail(email).orElse(null);
        }
        if (e == null && provider != null && providerUserId != null) {
            e = this.repo.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
        }
        if (e == null) {
            e = new CustomerEntity();
            e.setNome(Objects.requireNonNullElse(nome, "Cliente"));
            e.setEmail(email);
            e.setAvatarUrl(avatarUrl);
            e.setProvider(provider);
            e.setProviderUserId(providerUserId);
            e.setEmailVerificado(emailVerificado || email != null);
            e.setAtivo(true);
            this.repo.save(e);
            return;
        }
        boolean changed = false;
        if (e.getProvider() == null && provider != null) {
            e.setProvider(provider);
            changed = true;
        }
        if (e.getProviderUserId() == null && providerUserId != null) {
            e.setProviderUserId(providerUserId);
            changed = true;
        }
        if (e.getAvatarUrl() == null && avatarUrl != null) {
            e.setAvatarUrl(avatarUrl);
            changed = true;
        }
        if (!e.isEmailVerificado() && emailVerificado) {
            e.setEmailVerificado(true);
            changed = true;
        }
        if (e.getNome() == null && nome != null) {
            e.setNome(nome);
            changed = true;
        }
        if (changed) {
            this.repo.save(e);
        }
    }
}

