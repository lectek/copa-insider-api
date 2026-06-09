package br.com.redemaisfarma.adapters.inbound.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
@Order(3)
public class SecurityMvcConfig {

    private final Environment env;
    private final SessionRegistry sessionRegistry;

    public SecurityMvcConfig(Environment env, SessionRegistry sessionRegistry) {
        this.env = env;
        this.sessionRegistry = sessionRegistry;
    }

    private boolean isDevLike() {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev")
                        || p.equalsIgnoreCase("local")
                        || p.equalsIgnoreCase("docker"));
    }

    private boolean isProd() {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod")
                        || p.equalsIgnoreCase("production"));
    }

    @Bean
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        boolean dev = isDevLike();

        http.securityMatcher("/**")
            .cors(AbstractHttpConfigurer::disable) // use seu CorsFilter global; mude para withDefaults() se quiser habilitar aqui
            .csrf(csrf -> {
                PathPatternRequestMatcher.Builder pathMatcherBuilder = PathPatternRequestMatcher.withDefaults();
                CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                csrf.csrfTokenRepository(repo);
                csrf.ignoringRequestMatchers(
                        pathMatcherBuilder.matcher("/admin/export/**"),
                        pathMatcherBuilder.matcher(HttpMethod.POST, "/login"),
                        pathMatcherBuilder.matcher(HttpMethod.POST, "/auth/login"),
                        pathMatcherBuilder.matcher("/webhooks/**")
                );
                if (dev) {
                    csrf.ignoringRequestMatchers(
                            pathMatcherBuilder.matcher("/admin/catalogo/sincronizar"),
                            pathMatcherBuilder.matcher("/actuator/**")
                    );
                }
            })
            .authorizeHttpRequests(auth -> {
                // recursos estáticos
                auth.requestMatchers(
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/animations/**", "/media/**",
                        "/vendor/**",
                        "/webjars/**", "/static/**", "/favicon.ico",
                        "/index.html", "/robots.txt"
                ).permitAll();

                // pré-flight CORS
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                // páginas públicas
                auth.requestMatchers(
                        "/api/public/**",
                        "/webhooks/**",
                        "/", "/cliente", "/cliente/index",
                        "/mercadopago/guia",
                        "/suporte", "/alysson",
                        "/catalogo", "/produtos", "/carrinho", "/checkout",
                        "/sobre", "/login", "/auth/login", "/logout", "/cadastro", "/cadastro-cliente",
                        "/auth/cliente/cadastro", "/clientes/cadastro", "/error"
                ).permitAll();

                // (REMOVIDO /pos-login)
                // se quiser manter /entrar-com e /limpar-escolha protegidas:
                auth.requestMatchers("/entrar-com", "/limpar-escolha")
                        .authenticated();

                // Swagger: liberado em dev, restrito em prod
                if (isProd()) {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                            .hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                            .permitAll();
                }

                // Actuator: em prod só admin (fora health/info), em dev tudo liberado
                if (isProd()) {
                    auth.requestMatchers("/actuator/health/**", "/actuator/info").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/actuator/**").permitAll();
                }

                // fluxo de esqueci/resetar senha
                auth.requestMatchers(
                        "/auth/esqueci-senha", "/auth/esqueci-senha/**",
                        "/auth/resetar-senha", "/auth/resetar-senha/**",
                        "/cliente/auth/esqueci-senha", "/cliente/auth/esqueci-senha/**",
                        "/cliente/auth/resetar-senha", "/cliente/auth/resetar-senha/**"
                ).permitAll();

                auth.requestMatchers("/auth/mudar-senha", "/auth/mudar-senha/**")
                        .authenticated();

                // criação de cliente via POST
                auth.requestMatchers(
                                HttpMethod.POST,
                                "/auth/cliente/cadastro",
                                "/clientes"
                        )
                        .permitAll();
                auth.requestMatchers(HttpMethod.POST, "/login", "/auth/login").permitAll();

                // área autenticada do cliente (exceto home pública /cliente e /cliente/index)
                auth.requestMatchers(
                                "/cliente/conta",
                                "/cliente/dados",
                                "/cliente/senha",
                                "/cliente/avatar",
                                "/cliente/pedidos",
                                "/cliente/pedidos/**"
                        )
                        .hasAnyRole("CLIENTE", "DEVELOPER", "DEV", "ADMIN", "USER");

                // qualquer outra rota exige autenticação
                // TEMP (2026-03-05): libera acesso sem login para o admin de produtos.
                auth.requestMatchers("/admin/produtos", "/admin/produtos/**").permitAll();
                auth.requestMatchers("/admin/vendas/rapida", "/admin/vendas/rapida/**")
                        .hasAnyRole("ADMIN", "CAIXA", "DEV", "DEVELOPER");
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/admin/**").hasRole("ADMIN");

                auth.anyRequest().authenticated();
            })
            .formLogin(form -> form
                    .loginPage("/auth/login")
                    .loginProcessingUrl("/auth/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .successHandler(successHandler())
                    .failureUrl("/auth/login?error")
                    .permitAll()
            )
            .logout(l -> l
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/auth/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .rememberMe(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(sf -> sf.migrateSession())
                    .maximumSessions(-1)
                    .sessionRegistry(this.sessionRegistry)
            )
            .headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; " +
                            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://fonts.gstatic.com; " +
                            "script-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data: blob: https: http:; " +
                            "font-src 'self' data: https://fonts.gstatic.com; " +
                            "connect-src 'self'; " +
                            "frame-ancestors 'self'"
                    ))
                    .frameOptions(frame -> frame.sameOrigin())
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .preload(true)
                            .maxAgeInSeconds(31_536_000))
                    .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );

        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String target = resolvePostLoginTarget(request, response, authentication);
            response.sendRedirect(target);
        };
    }

    private String resolvePostLoginTarget(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isCaixa = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CAIXA".equals(a.getAuthority()));
        String roleDefault = isAdmin
                ? "/admin/dashboard"
                : (isCaixa ? "/admin/vendas/rapida" : "/cliente/conta");

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

    static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                // força criação do cookie XSRF-TOKEN
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
