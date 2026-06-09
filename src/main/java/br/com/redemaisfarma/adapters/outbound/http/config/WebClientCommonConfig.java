package br.com.redemaisfarma.adapters.outbound.http.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientCommonConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientCommonConfig.class);

    @Bean
    @ConditionalOnMissingBean(WebClient.Builder.class)
    public WebClient.Builder webClientBuilder(@Value("${app.http.base-url:}") String baseUrl) {
        WebClient.Builder builder = WebClient.builder()
            .filter(logRequest())
            .filter(logResponse())
            .filter(mapErrors());

        if (baseUrl != null && !baseUrl.isBlank()) {
            builder.baseUrl(baseUrl);
        }

        return builder;
    }

    /** Log básico da requisição (método, URL e cabeçalhos em nível DEBUG). */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            try {
                log.info("HTTP --> {} {}", request.method(), request.url());
                if (log.isDebugEnabled()) {
                    request.headers().forEach((name, values) ->
                        values.forEach(value -> log.debug("HTTP --> {}: {}", name, value))
                    );
                }
            } catch (Exception e) {
                log.warn("Falha ao logar requisição WebClient: {}", e.getMessage());
            }
            return Mono.just(request);
        });
    }

    /** Log básico da resposta (status e cabeçalhos em nível DEBUG). */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            try {
                log.info("HTTP <-- {}", response.statusCode());
                if (log.isDebugEnabled()) {
                    response.headers().asHttpHeaders().forEach((name, values) ->
                        values.forEach(value -> log.debug("HTTP <-- {}: {}", name, value))
                    );
                }
            } catch (Exception e) {
                log.warn("Falha ao logar resposta WebClient: {}", e.getMessage());
            }
            return Mono.just(response);
        });
    }

    /**
     * Converte respostas com status de erro em exceções do WebClient.
     * Usa o utilitário nativo {@code ClientResponse.createException()}.
     */
    private ExchangeFilterFunction mapErrors() {
        return (request, next) ->
            next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().isError()) {
                        // Cria WebClientResponseException com corpo (quando houver)
                        return response.createException().flatMap(Mono::error);
                    }
                    return Mono.just(response);
                });
    }
}
