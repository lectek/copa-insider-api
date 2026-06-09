// src/main/java/br/com/redemaisfarma/config/SecurityConfig.java
package br.com.redemaisfarma.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 🔑 Depois de logar:
        // - Se o usuário tentou acessar algo protegido (ex.: /checkout),
        //   ele volta para essa URL.
        // - Se não tiver URL anterior, cai na home "/".
        AuthenticationSuccessHandler successHandler = (request, response, authentication) -> {
            String target = resolvePostLoginTarget(request, response, authentication);
            response.sendRedirect(target);
        };

        http
            // CSRF desabilitado para algumas rotas técnicas / públicas de API
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/actuator/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/api/public/**"
                )
            )

            .authorizeHttpRequests(auth -> auth

                // 🔹 Recursos estáticos SEMPRE liberados
                .requestMatchers(
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/animations/**",
                    "/media/**",
                    "/vendor/**",
                    "/img/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                // 🔹 PÁGINAS PÚBLICAS (área cliente / vitrine)
                // Qualquer pessoa pode navegar na loja:
                // - Home / (raiz)
                // - /cliente      (alias da home cliente)
                // - /cliente/index
                // - /sobre        (página institucional)
                // - /produtos/**  (listar/detalhar produtos)
                // - /buscar       (busca de produtos)
                .requestMatchers(
                    "/",
                    "/cliente",
                    "/cliente/index",
                    "/mercadopago/guia",
                    "/suporte",
                    "/alysson",
                    "/sobre",
                    "/produtos/**",
                    "/buscar"
                ).permitAll()

                // 🔹 Telas de login/cadastro (GET e POST)
                // Liberadas para o usuário poder entrar ou criar conta
                .requestMatchers(HttpMethod.GET,
                    "/auth/login",
                    "/auth/cliente/cadastro"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/auth/login",
                    "/auth/cliente/cadastro"
                ).permitAll()

                // 🔹 APIs públicas e health (para monitoramento, docs, etc.)
                .requestMatchers("/api/public/**", "/api/ia/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // 🔸 ROTAS DE COMPRA — exigem cadastro/login
                // Tudo que mexe com pedido, checkout e carrinho é protegido.
                .requestMatchers(
                    "/carrinho/**",
                    "/checkout/**",
                    "/pedido/**"
                ).authenticated()

                // 🔸 ÁREA ADMIN — só para usuários com ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 🔒 Qualquer outra rota que sobrar exige login
                .anyRequest().authenticated()
            )

            // 🔐 Configuração do login via formulário
            .formLogin(f -> f
                .loginPage("/auth/login")          // GET: tela de login
                .loginProcessingUrl("/auth/login") // POST: envio do formulário
                .successHandler(successHandler)    // redireciona para URL original ou "/"
                .failureUrl("/auth/login?error")
                .permitAll()
            )

            // 🔓 Logout padrão
            .logout(l -> l
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    private String resolvePostLoginTarget(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String roleDefault = isAdmin ? "/admin/dashboard" : "/cliente/conta";

        String requested = request.getParameter("redirect");
        if (StringUtils.hasText(requested) && requested.startsWith("/") && !requested.startsWith("//")) {
            if (!requested.startsWith("/admin") || isAdmin) {
                return requested;
            }
        }

        SavedRequest saved = new HttpSessionRequestCache().getRequest(request, response);
        if (saved != null && StringUtils.hasText(saved.getRedirectUrl())) {
            return saved.getRedirectUrl();
        }

        return roleDefault;
    }
}
