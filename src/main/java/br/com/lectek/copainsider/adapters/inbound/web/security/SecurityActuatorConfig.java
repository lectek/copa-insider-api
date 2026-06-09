package br.com.lectek.copainsider.adapters.inbound.web.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration(proxyBeanMethods = false)
public class SecurityActuatorConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        RequestMatcher anyEndpoint = EndpointRequest.toAnyEndpoint();

        http.securityMatcher(anyEndpoint);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .requestMatchers(EndpointRequest.to("loggers")).hasRole("DEV")
                .anyRequest().authenticated()
        );

        http.csrf(csrf -> csrf.ignoringRequestMatchers(anyEndpoint));
        http.httpBasic(Customizer.withDefaults());
        // sem AbstractHttpConfigurer aqui para evitar incompatibilidades
        http.formLogin(form -> form.disable());

        return http.build();
    }
}
