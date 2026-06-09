/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.http.MediaType
 *  org.springframework.web.reactive.function.client.ExchangeStrategies
 *  org.springframework.web.reactive.function.client.WebClient
 *  org.springframework.web.reactive.function.client.WebClient$Builder
 */
package br.com.lectek.copainsider.adapters.outbound.http.cliente.config;

import br.com.lectek.copainsider.adapters.outbound.http.cliente.client.ClienteClient;
import br.com.lectek.copainsider.adapters.outbound.http.cliente.client.ClienteHttpClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(value={ClienteClientProperties.class})
@ConditionalOnProperty(prefix="integrations.cliente", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class ClienteClientConfig {
    @Bean(value={"clienteWebClient"})
    public WebClient clienteWebClient(WebClient.Builder webClientBuilder, ClienteClientProperties props) {
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(props.getMaxInMemorySize())).build();
        return webClientBuilder.clone().baseUrl(props.getBaseUrl()).defaultHeaders(h -> {
            h.setAccept(List.of(MediaType.APPLICATION_JSON));
            h.setContentType(MediaType.APPLICATION_JSON);
        }).exchangeStrategies(strategies).build();
    }

    @Bean
    public ClienteClient clienteClient(@Qualifier(value="clienteWebClient") WebClient clienteWebClient, ClienteClientProperties props) {
        return new ClienteHttpClient(clienteWebClient, props);
    }
}

