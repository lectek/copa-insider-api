package br.com.lectek.copainsider.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {

    private static final String LOGIN_URL = "/auth/login";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationSuccessHandler successHandler = (request, response, authentication) -> {
            String target = resolvePostLoginTarget(request, response, authentication);
            response.sendRedirect(target);
        };

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/actuator/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/api/public/**",
                    "/api/admin/**",
                    "/api/jogo/**",
                    "/api/doacao/**",
                    "/webhooks/**"
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**", "/api/admin/**").hasAnyRole("ADMIN", "DEVELOPER")
                .requestMatchers("/conta/**").authenticated()
                .anyRequest().permitAll()
            )

            .formLogin(f -> f
                .loginPage(LOGIN_URL)
                .loginProcessingUrl(LOGIN_URL)
                .successHandler(successHandler)
                .failureHandler(loginFailureHandler())
                .permitAll()
            )

            .logout(l -> l
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl(LOGIN_URL + "?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String suffix = (exception instanceof DisabledException) ? "?disabled" : "?error";
            response.sendRedirect(LOGIN_URL + suffix);
        };
    }

    private String resolvePostLoginTarget(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                            || "ROLE_DEVELOPER".equals(a.getAuthority()));
        String roleDefault = isAdmin ? "/admin/dashboard" : "/";

        String requested = request.getParameter("redirect");
        if (StringUtils.hasText(requested) && requested.startsWith("/")
                && !requested.startsWith("//")
                && (isAdmin || !requested.startsWith("/cliente"))
                && (!requested.startsWith("/admin") || isAdmin)) {
            return requested;
        }

        SavedRequest saved = new HttpSessionRequestCache().getRequest(request, response);
        if (saved != null && StringUtils.hasText(saved.getRedirectUrl())) {
            String url = saved.getRedirectUrl();
            // Clientes normais ficam na área pública; admin acede a tudo
            if ((isAdmin || !url.contains("/cliente")) && (isAdmin || !url.contains("/admin"))) {
                return url;
            }
        }

        return roleDefault;
    }

}
