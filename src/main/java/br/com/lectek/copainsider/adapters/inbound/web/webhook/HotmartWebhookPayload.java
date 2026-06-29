package br.com.lectek.copainsider.adapters.inbound.web.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
            Price price,
            @JsonProperty("custom_fields") List<CustomField> customFields
    ) {
        public String customField(String fieldName) {
            if (customFields == null) return null;
            return customFields.stream()
                    .filter(f -> fieldName.equalsIgnoreCase(f.fieldName()))
                    .map(CustomField::value)
                    .findFirst()
                    .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CustomField(
            @JsonProperty("field_name") String fieldName,
            String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Buyer(
            String name,
            String email,
            String document,
            Phone phone
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Phone(
            @JsonProperty("phone") String number,
            @JsonProperty("country_code") String countryCode
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
