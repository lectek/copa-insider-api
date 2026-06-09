/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.models.Components
 *  io.swagger.v3.oas.models.OpenAPI
 *  io.swagger.v3.oas.models.info.Info
 *  io.swagger.v3.oas.models.info.License
 *  io.swagger.v3.oas.models.security.SecurityRequirement
 *  io.swagger.v3.oas.models.security.SecurityScheme
 *  io.swagger.v3.oas.models.security.SecurityScheme$Type
 *  io.swagger.v3.oas.models.servers.Server
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package br.com.redemaisfarma.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI redeMaisFarmaOpenAPI() {
        return new OpenAPI().info(new Info().title("RedeMaisFarma API").version("v1.0.0").description("Plataforma da RedeMaisFarma \u2014 cat\u00e1logos, pedidos, clientes e integra\u00e7\u00f5es.").license(new License().name("Apache 2.0"))).servers(List.of(new Server().url("http://127.0.0.1:18090").description("Local (Docker)"), new Server().url("http://localhost:8080").description("Local (sem Docker)"), new Server().url("https://api.seudominio.com").description("Produ\u00e7\u00e3o"))).components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))).addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

