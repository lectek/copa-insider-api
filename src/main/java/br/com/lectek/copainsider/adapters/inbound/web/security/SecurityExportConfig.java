package br.com.lectek.copainsider.adapters.inbound.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(0)
public class SecurityExportConfig {

    @Bean
    public SecurityFilterChain exportChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/export/**")
            // não usa AbstractHttpConfigurer: deixa o CORS ligado com defaults
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
