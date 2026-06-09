package br.com.lectek.copainsider.adapters.inbound.web.security;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(2)
public class SecurityApiConfig {

    private final ObjectProvider<JwtOncePerRequestFilter> jwtFilterProvider;
    private final ObjectProvider<JwtAuthEntryPoint> jwtEntryPointProvider;
    private final Environment env;

    public SecurityApiConfig(
            ObjectProvider<JwtOncePerRequestFilter> jwtFilterProvider,
            ObjectProvider<JwtAuthEntryPoint> jwtEntryPointProvider,
            Environment env
    ) {
        this.jwtFilterProvider = jwtFilterProvider;
        this.jwtEntryPointProvider = jwtEntryPointProvider;
        this.env = env;
    }

    private boolean isDevLike() {
        for (String p : env.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(p) || "local".equalsIgnoreCase(p) || "docker".equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean isJwtEnabled() {
        // security.jwt.enabled tem prioridade; fallback para jwt.enabled; default=false
        Boolean primary = env.getProperty("security.jwt.enabled", Boolean.class);
        if (primary != null) return primary;
        return env.getProperty("jwt.enabled", Boolean.class, false);
    }

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(cors -> {}) // usa config global de CORS, se houver
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                        "/api/ping/**",
                        "/api/status",
                        "/api/ia/**",
                        "/api/auth/otp/**",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/auth/email-claim/**",
                        "/api/auth/password/reset-otp",
                        "/api/auth/register/complete-otp"
                ).permitAll();

                if (isDevLike()) {
                    auth.requestMatchers("/api/dev/provision/start").permitAll();
                } else {
                    auth.requestMatchers("/api/dev/provision/start").hasRole("DEVELOPER");
                }

                auth.requestMatchers("/api/dev/provision/verify").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/api/public/**", "/api/produtos/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/public/produtos/*/stock/subscribe").permitAll();
                auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                auth.requestMatchers("/api/financeiro/**").hasAnyRole("ADMIN", "FINANCEIRO");
                auth.requestMatchers("/api/catalogo/**").hasAnyRole("ADMIN", "CATALOGO");
                auth.requestMatchers("/api/suporte/**").hasAnyRole("ADMIN", "SUPORTE");
                auth.requestMatchers("/api/internal/**").hasAnyRole("ADMIN", "PLATFORM_OWNER");

                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                auth.anyRequest().authenticated();
            });
        if (isJwtEnabled()) {
            AuthenticationEntryPoint entryPoint = jwtEntryPointProvider.getIfAvailable();
            if (entryPoint != null) {
                http.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));
            }
            Filter jwtFilter = jwtFilterProvider.getIfAvailable();
            if (jwtFilter != null) {
                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            }
        }

        return http.build();
    }
}
