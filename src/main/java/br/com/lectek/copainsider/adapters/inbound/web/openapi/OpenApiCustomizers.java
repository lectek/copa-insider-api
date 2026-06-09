/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.models.OpenAPI
 *  io.swagger.v3.oas.models.info.Contact
 *  io.swagger.v3.oas.models.info.Info
 *  io.swagger.v3.oas.models.info.License
 *  io.swagger.v3.oas.models.tags.Tag
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package br.com.lectek.copainsider.adapters.inbound.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiCustomizers {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("CopaInsider API").version("v1.0.0").description("Plataforma da CopaInsider para gest\u00e3o de clientes, pedidos e produtos.").contact(new Contact().name("Time CopaInsider").email("suporte@copainsider.com.br").url("https://copainsider.com.br")).license(new License().name("Apache 2.0").url("http://springdoc.org"))).tags(List.of(new Tag().name("Clientes").description("Opera\u00e7\u00f5es de cadastro e consulta de clientes"), new Tag().name("Pedidos").description("Opera\u00e7\u00f5es de cria\u00e7\u00e3o e gest\u00e3o de pedidos"), new Tag().name("Produtos").description("Consulta e gerenciamento de produtos"), new Tag().name("Auth").description("Autentica\u00e7\u00e3o e gerenciamento de usu\u00e1rios")));
    }
}

