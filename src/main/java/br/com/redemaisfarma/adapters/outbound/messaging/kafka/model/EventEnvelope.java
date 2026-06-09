package br.com.redemaisfarma.adapters.outbound.messaging.kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Envelope genérico para eventos publicados/consumidos no Kafka.
 * @param <T> tipo do payload do evento
 */
public class EventEnvelope<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private String source;

    // Serializa como string ISO-8601 (requer JavaTimeModule no ObjectMapper)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant occurredAt;

    private String tenantId;
    private T data;

    public EventEnvelope() {
        // Jackson
    }

    public EventEnvelope(String eventType, String source, String tenantId, T data) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.occurredNow();
        this.tenantId = tenantId;
        this.data = data;
    }

    /** Fábrica conveniente. */
    public static <T> EventEnvelope<T> of(String eventType, String source, String tenantId, T data) {
        return new EventEnvelope<>(eventType, source, tenantId, data);
    }

    /** Define occurredAt = agora (forma correta). */
    public void occurredNow() {
        this.occurredAt = Instant.now();
    }

    /** Alias legado para compatibilidade com código já decompilado. */
    @Deprecated
    public void occuredNow() {
        this.occurredNow();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventEnvelope<?> that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", occurredAt=" + occurredAt +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
