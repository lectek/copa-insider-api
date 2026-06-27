package br.com.lectek.copainsider.adapters.inbound.web.security;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class WebSecurityBeans {

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        return (Collection<? extends GrantedAuthority> in) -> {
            // copia mantendo o tipo mais amplo
            Set<GrantedAuthority> out = new HashSet<>(in);

            boolean isDev   = in.stream().anyMatch(a ->
                    "ROLE_DEVELOPER".equals(a.getAuthority()) || "ROLE_DEV".equals(a.getAuthority()));
            boolean isAdmin = in.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            if (isDev) {
                out.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                out.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            } else if (isAdmin) {
                out.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            }
            return out;
        };
    }

    @Bean
    public AuthenticationSuccessHandler webSuccessHandler(
            @Value("${app.security.oauth2.post-login-redirect:/pos-login}") String redirect
    ) {
        return (request, response, authentication) -> {
            // sendRedirect já define 302
            response.sendRedirect(redirect);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:}") String allowedOriginsProp
    ) {
        List<String> allowedOrigins = StringUtils.hasText(allowedOriginsProp)
                ? Arrays.stream(allowedOriginsProp.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList())
                : List.of(
                        "http://localhost:18090",
                        "http://localhost:18080",
                        "http://localhost:*",
                        "https://*.copainsider.com",
                        "https://*.lek-tec.com"
                );

        CorsConfiguration cfg = new CorsConfiguration();

        boolean hasWildcard = allowedOrigins.stream().anyMatch(o -> o.contains("*") || o.contains(":*"));
        if (hasWildcard) {
            cfg.setAllowedOriginPatterns(allowedOrigins);
        } else {
            cfg.setAllowedOrigins(allowedOrigins);
        }

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With",
                "X-OTP-TOKEN", "X-CSRF-TOKEN", "X-XSRF-TOKEN"
        ));
        cfg.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
