package br.com.redemaisfarma.application.service.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryRouteServiceTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldEstimateDistanceUsingStreetFallbackWhenHouseNumberDoesNotResolve()
            throws Exception {
        final List<String> requestedQueries = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/search", exchange -> {
            final String query = extractQuery(exchange.getRequestURI());
            requestedQueries.add(query);

            final String response = switch (query) {
                case "Rua Base, 310 - Loja 102, Mangabeira, Joao Pessoa, PB" ->
                        """
                        [{"lat":"-7.172200","lon":"-34.840100"}]
                        """;
                case "Rua Huerta Ferreira de Melo n263" -> "[]";
                case "Rua Huerta Ferreira de Melo" ->
                        """
                        [{"lat":"-7.181500","lon":"-34.852900"}]
                        """;
                default -> "[]";
            };
            writeJson(exchange, response);
        });
        server.start();

        final DeliveryRouteService service = new DeliveryRouteService(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort() + "/search",
                "http://localhost:0",
                "geo",
                "RedeMaisFarma-Test/1.0",
                3000L
        );

        final List<DeliveryRouteService.LegDistanceEstimate> estimates =
                service.estimateSequentialDistances(
                        "Rua Base, 310 - Loja 102, Mangabeira, Joao Pessoa, PB",
                        List.of("Rua Huerta Ferreira de Melo n263")
                );

        assertThat(estimates).hasSize(1);
        assertThat(estimates.getFirst().referenciaAnterior()).isEqualTo("Base");
        assertThat(estimates.getFirst().distanciaKm()).isNotNull();
        assertThat(requestedQueries).contains(
                "Rua Huerta Ferreira de Melo n263",
                "Rua Huerta Ferreira de Melo"
        );
    }

    @Test
    void shouldPlanRouteUsingStreetFallbackWhenExactStopAddressFails()
            throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/search", exchange -> {
            final String query = extractQuery(exchange.getRequestURI());
            final String response = switch (query) {
                case "Base da loja" ->
                        """
                        [{"lat":"-7.172200","lon":"-34.840100"}]
                        """;
                case "Rua Maria de Lourdes Gomes da Silva, 157" -> "[]";
                case "Rua Maria de Lourdes Gomes da Silva" ->
                        """
                        [{"lat":"-7.170000","lon":"-34.845000"}]
                        """;
                case "Rua Beatriz Maria de Oliveira numero 158" -> "[]";
                case "Rua Beatriz Maria de Oliveira" ->
                        """
                        [{"lat":"-7.168500","lon":"-34.850000"}]
                        """;
                default -> "[]";
            };
            writeJson(exchange, response);
        });
        server.start();

        final DeliveryRouteService service = new DeliveryRouteService(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort() + "/search",
                "http://localhost:0",
                "geo",
                "RedeMaisFarma-Test/1.0",
                3000L
        );

        final DeliveryRouteService.PlannedRoute plannedRoute = service.plan(
                "Base da loja",
                List.of(
                        new DeliveryRouteService.DeliveryStopInput(
                                8L,
                                "Cliente 1",
                                "Rua Maria de Lourdes Gomes da Silva, 157",
                                "123456",
                                "PRONTO_PARA_ENTREGA"
                        ),
                        new DeliveryRouteService.DeliveryStopInput(
                                9L,
                                "Cliente 2",
                                "Rua Beatriz Maria de Oliveira numero 158",
                                "654321",
                                "PRONTO_PARA_ENTREGA"
                        )
                )
        );

        assertThat(plannedRoute.paradas()).hasSize(2);
        assertThat(plannedRoute.distanciaTotalKm()).isGreaterThan(BigDecimal.ZERO);
        assertThat(plannedRoute.paradas())
                .extracting(DeliveryRouteService.DeliveryStopPlan::distanciaAnteriorKm)
                .allSatisfy(distance -> assertThat(distance).isNotNull());
    }

    @Test
    void shouldPlanRouteUsingOriginFallbackWhenOriginContainsComplementAndNeighborhood()
            throws Exception {
        final List<String> requestedQueries = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/search", exchange -> {
            final String query = extractQuery(exchange.getRequestURI());
            requestedQueries.add(query);

            final String response = switch (query) {
                case "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Mangabeira, Joao Pessoa, PB" -> "[]";
                case "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Joao Pessoa, PB" -> "[]";
                case "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Mangabeira, Joao Pessoa, PB, Brasil" -> "[]";
                case "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Joao Pessoa, PB, Brasil" -> "[]";
                case "Rua Prefeito Luiz A. M. Coutinho, 310, Mangabeira, Joao Pessoa, PB" -> "[]";
                case "Rua Prefeito Luiz A. M. Coutinho, 310, Mangabeira, Joao Pessoa, PB, Brasil" -> "[]";
                case "Rua Prefeito Luiz Alberto M Coutinho, 310, Joao Pessoa, PB" ->
                        """
                        [{"lat":"-7.165100","lon":"-34.839700"}]
                        """;
                case "Rua Maria de Lourdes Gomes da Silva, 157" ->
                        """
                        [{"lat":"-7.170000","lon":"-34.845000"}]
                        """;
                default -> "[]";
            };
            writeJson(exchange, response);
        });
        server.start();

        final DeliveryRouteService service = new DeliveryRouteService(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort() + "/search",
                "http://localhost:0",
                "geo",
                "RedeMaisFarma-Test/1.0",
                3000L
        );

        final DeliveryRouteService.PlannedRoute plannedRoute = service.plan(
                "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Mangabeira, Joao Pessoa, PB",
                List.of(
                        new DeliveryRouteService.DeliveryStopInput(
                                8L,
                                "Cliente 1",
                                "Rua Maria de Lourdes Gomes da Silva, 157",
                                "123456",
                                "PRONTO_PARA_ENTREGA"
                        )
                )
        );

        assertThat(plannedRoute.paradas()).hasSize(1);
        assertThat(plannedRoute.distanciaTotalKm()).isGreaterThan(BigDecimal.ZERO);
        assertThat(requestedQueries).contains(
                "Rua Prefeito Luiz A. M. Coutinho, 310 - Loja 102, Mangabeira, Joao Pessoa, PB",
                "Rua Prefeito Luiz Alberto M Coutinho, 310, Joao Pessoa, PB"
        );
    }

    @Test
    void shouldPlanRouteFromGpsCoordinatesWithoutGeocodingOrigin()
            throws Exception {
        final List<String> requestedQueries = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/search", exchange -> {
            final String query = extractQuery(exchange.getRequestURI());
            requestedQueries.add(query);

            final String response = switch (query) {
                case "Rua Maria de Lourdes Gomes da Silva, 157" ->
                        """
                        [{"lat":"-7.170000","lon":"-34.845000"}]
                        """;
                default -> "[]";
            };
            writeJson(exchange, response);
        });
        server.start();

        final DeliveryRouteService service = new DeliveryRouteService(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort() + "/search",
                "http://localhost:0",
                "geo",
                "RedeMaisFarma-Test/1.0",
                3000L
        );

        final DeliveryRouteService.PlannedRoute plannedRoute = service.planFromOrigin(
                new DeliveryRouteService.RouteOriginInput(
                        "Localizacao atual do dispositivo",
                        null,
                        -7.1695d,
                        -34.8393d
                ),
                List.of(
                        new DeliveryRouteService.DeliveryStopInput(
                                8L,
                                "Cliente 1",
                                "Rua Maria de Lourdes Gomes da Silva, 157",
                                "123456",
                                "PRONTO_PARA_ENTREGA"
                        )
                )
        );

        assertThat(plannedRoute.origem()).isEqualTo("Localizacao atual do dispositivo");
        assertThat(plannedRoute.paradas()).hasSize(1);
        assertThat(requestedQueries).containsExactly("Rua Maria de Lourdes Gomes da Silva, 157");
    }

    @Test
    void shouldFallbackToInputOrderWhenGeocoderReturnsRateLimit()
            throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/search", exchange -> {
            writeJson(exchange, 429, """
                    {"message":"rate limited"}
                    """);
        });
        server.start();

        final DeliveryRouteService service = new DeliveryRouteService(
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort() + "/search",
                "http://localhost:0",
                "geo",
                "RedeMaisFarma-Test/1.0",
                3000L
        );

        final DeliveryRouteService.PlannedRoute plannedRoute = service.plan(
                "Base da loja, Joao Pessoa/PB",
                List.of(
                        new DeliveryRouteService.DeliveryStopInput(
                                8L,
                                "Cliente 1",
                                "Rua Maria de Lourdes Gomes da Silva, 157",
                                "123456",
                                "PRONTO_PARA_ENTREGA"
                        ),
                        new DeliveryRouteService.DeliveryStopInput(
                                9L,
                                "Cliente 2",
                                "Rua Huerta Ferreira de Melo n263",
                                "654321",
                                "PRONTO_PARA_ENTREGA"
                        )
                )
        );

        assertThat(plannedRoute.origem()).isEqualTo("Base da loja, Joao Pessoa/PB");
        assertThat(plannedRoute.distanciaTotalKm()).isNull();
        assertThat(plannedRoute.paradas())
                .extracting(DeliveryRouteService.DeliveryStopPlan::pedidoId)
                .containsExactly(8L, 9L);
        assertThat(plannedRoute.paradas().getFirst().latitude()).isNull();
        assertThat(plannedRoute.paradas().getFirst().longitude()).isNull();
        assertThat(plannedRoute.mapaUrl())
                .contains("google.com/maps/dir")
                .contains("Rua+Maria+de+Lourdes+Gomes+da+Silva");
    }

    private static void writeJson(
            final HttpExchange exchange,
            final String body
    ) throws IOException {
        writeJson(exchange, 200, body);
    }

    private static void writeJson(
            final HttpExchange exchange,
            final int status,
            final String body
    ) throws IOException {
        final byte[] content = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(status, content.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(content);
        } finally {
            exchange.close();
        }
    }

    private static String extractQuery(final URI uri) {
        final List<String> values = new ArrayList<>();
        final String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        for (String entry : rawQuery.split("&")) {
            final int separator = entry.indexOf('=');
            final String key = separator >= 0 ? entry.substring(0, separator) : entry;
            final String value = separator >= 0 ? entry.substring(separator + 1) : "";
            if ("q".equals(key)) {
                values.add(URLDecoder.decode(value, StandardCharsets.UTF_8));
            }
        }
        return values.isEmpty() ? "" : values.getFirst();
    }
}
