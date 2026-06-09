/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.core.userdetails.User
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.security.core.userdetails.UserDetailsService
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.security.provisioning.InMemoryUserDetailsManager
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile(value={"dev-inmemory"})
public class DevUsersConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService inMemoryUsers(PasswordEncoder enc) {
        String senha = "@Lektec!larissa25";
        return new InMemoryUserDetailsManager(new UserDetails[]{User.withUsername((String)"dev").password(enc.encode((CharSequence)senha)).roles(new String[]{"DEV"}).build(), User.withUsername((String)"admin").password(enc.encode((CharSequence)senha)).roles(new String[]{"ADMIN"}).build(), User.withUsername((String)"lektecjava@gmail.com").password(enc.encode((CharSequence)senha)).roles(new String[]{"DEV"}).build()});
    }
}
