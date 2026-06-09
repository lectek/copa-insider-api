package br.com.lectek.copainsider.adapters.inbound.web.security;

import br.com.lectek.copainsider.application.service.fiscal.FiscalPrintStationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@Order(1)
public class SecurityAdminApiConfig {

    private final ObjectProvider<FiscalPrintAgentApiKeyFilter>
            fiscalPrintAgentApiKeyFilterProvider;

    public SecurityAdminApiConfig(
            final ObjectProvider<FiscalPrintAgentApiKeyFilter>
                    fiscalPrintAgentApiKeyFilterProviderValue
    ) {
        this.fiscalPrintAgentApiKeyFilterProvider =
                fiscalPrintAgentApiKeyFilterProviderValue;
    }

    @Bean
    public FiscalPrintAgentApiKeyFilter adminFiscalPrintAgentApiKeyFilter(
            final ObjectProvider<FiscalPrintStationService>
                    fiscalPrintStationServiceProvider
    ) {
        return new FiscalPrintAgentApiKeyFilter(
                fiscalPrintStationServiceProvider
        );
    }

    @Bean
    public FilterRegistrationBean<FiscalPrintAgentApiKeyFilter>
            adminFiscalPrintAgentApiKeyFilterRegistration(
            final FiscalPrintAgentApiKeyFilter adminFiscalPrintAgentApiKeyFilter
    ) {
        final FilterRegistrationBean<FiscalPrintAgentApiKeyFilter> registration =
                new FilterRegistrationBean<>(adminFiscalPrintAgentApiKeyFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain adminApiChain(final HttpSecurity http)
            throws Exception {
        final PathPatternRequestMatcher.Builder pathMatcherBuilder =
                PathPatternRequestMatcher.withDefaults();

        http.securityMatcher("/api/admin/**")
                .cors(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .ignoringRequestMatchers(
                                pathMatcherBuilder.matcher(
                                        "/api/admin/fiscal/impressao/agente/**"
                                )
                        )
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        SessionCreationPolicy.IF_REQUIRED
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/fiscal/impressao/agente/**")
                        .hasAnyRole("ADMIN", "CAIXA", "PRINT_AGENT")
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                );

        final FiscalPrintAgentApiKeyFilter fiscalPrintAgentApiKeyFilter =
                fiscalPrintAgentApiKeyFilterProvider.getIfAvailable();
        if (fiscalPrintAgentApiKeyFilter != null) {
            http.addFilterBefore(
                    fiscalPrintAgentApiKeyFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }
}
