package br.com.lectek.copainsider.adapters.outbound.http.cliente.client;

import br.com.lectek.copainsider.adapters.outbound.http.cliente.config.ClienteClientProperties;
import br.com.lectek.copainsider.adapters.outbound.http.cliente.model.ClienteExternal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

public class ClienteHttpClient implements ClienteClient {

    private final WebClient clienteWebClient;

    @SuppressWarnings("unused") // manter injetado para uso futuro (headers/basePath/flags)
    private final ClienteClientProperties props;

    public ClienteHttpClient(@Qualifier("clienteWebClient") WebClient clienteWebClient,
                             ClienteClientProperties props) {
        this.clienteWebClient = Objects.requireNonNull(clienteWebClient, "clienteWebClient");
        this.props = Objects.requireNonNull(props, "props");
    }

    @Override
    public Optional<ClienteExternal> getById(UUID id) {
        if (id == null) return Optional.empty();
        return exchangeGet("/clientes/{id}", Map.of("id", id.toString()), null, ClienteExternal.class);
    }

    @Override
    public Optional<ClienteExternal> getByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) return Optional.empty();

        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("cpf", cpf);

        return exchangeGet("/clientes/by-cpf", null, q, ClienteExternal.class);
    }

    @Override
    public List<ClienteExternal> searchByName(String nome, int page, int size) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        if (nome != null && !nome.isBlank()) {
            q.add("nome", nome);
        }
        q.add("page", String.valueOf(Math.max(page, 0)));
        q.add("size", String.valueOf(Math.max(size, 1)));

        ClienteExternal[] arr = exchangeGet("/clientes/search", null, q, ClienteExternal[].class)
                .orElse(null);

        if (arr == null || arr.length == 0) return Collections.emptyList();
        return Arrays.asList(arr);
    }

    @Override
    public ClienteExternal upsert(ClienteExternal body) {
        Objects.requireNonNull(body, "body");

        // ajuste o endpoint se na sua API for outro (ex.: PUT /clientes/{id})
        return clienteWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/clientes").build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ClienteExternal.class)
                .block();
    }

    private <T> Optional<T> exchangeGet(String path,
                                        Map<String, String> uriVars,
                                        MultiValueMap<String, String> query,
                                        Class<T> clazz) {
        T result = clienteWebClient.get()
                .uri(builder -> {
                    var b = builder.path(path);
                    if (query != null && !query.isEmpty()) {
                        b.queryParams(query);
                    }
                    return (uriVars == null || uriVars.isEmpty())
                            ? b.build()
                            : b.build(uriVars);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();

        return Optional.ofNullable(result);
    }
}
