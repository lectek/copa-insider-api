package br.com.lectek.copainsider.adapters.inbound.web.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HotmartWebhookPayload(
        String hottok,
        Data data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String event,
            Purchase purchase
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Purchase(
            String transaction,
            String status,
            Buyer buyer,
            Product product,
            Price price
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Buyer(
            String name,
            String email
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Product(
            @JsonProperty("id") Long id,
            String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Price(
            Double value,
            @JsonProperty("currency_value") String currencyValue
    ) {}
}
