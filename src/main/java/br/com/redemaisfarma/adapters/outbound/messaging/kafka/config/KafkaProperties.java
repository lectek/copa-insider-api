package br.com.redemaisfarma.adapters.outbound.messaging.kafka.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    /** Se o módulo Kafka deve ser ativado */
    private boolean enabled = true;

    /** Endereços do cluster Kafka, ex: "kafka:9092" */
    private String bootstrapServers;

    /** Identificador de cliente padrão */
    private String clientId = "redemaisfarma-api";

    /** Quantidade de tentativas de reenvio */
    @NotNull
    @Min(0)
    private Integer retries = 5;

    /** Tamanho máximo do lote em bytes */
    @NotNull
    @Min(16_384)
    private Integer batchSize = 32_768;

    /** Atraso máximo (ms) antes do envio do lote */
    @NotNull
    @Min(0)
    private Integer lingerMs = 5;

    /** Memória do buffer (bytes) — padrão 64 MB */
    @NotNull
    @Min(0x2000000) // 33 MB
    private Integer bufferMemory = 0x4000000; // 67 MB

    /** Garante que mensagens sejam produzidas uma única vez */
    private boolean idempotence = true;

    /** Nível de confirmação de mensagens: "all", "1", "0" */
    private String acks = "all";

    /** Parâmetros extras (ex.: interceptors, configs personalizadas) */
    private Map<String, Object> extra;

    // ========= VALIDAÇÃO =========
    @AssertTrue(message = "kafka.bootstrapServers deve ser informado quando kafka.enabled=true")
    public boolean isBootstrapServersValid() {
        return !enabled || (bootstrapServers != null && !bootstrapServers.isBlank());
    }

    // ========= GETTERS / SETTERS =========
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public Integer getRetries() { return retries; }
    public void setRetries(Integer retries) { this.retries = retries; }

    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }

    public Integer getLingerMs() { return lingerMs; }
    public void setLingerMs(Integer lingerMs) { this.lingerMs = lingerMs; }

    public Integer getBufferMemory() { return bufferMemory; }
    public void setBufferMemory(Integer bufferMemory) { this.bufferMemory = bufferMemory; }

    public boolean isIdempotence() { return idempotence; }
    public void setIdempotence(boolean idempotence) { this.idempotence = idempotence; }

    public String getAcks() { return acks; }
    public void setAcks(String acks) { this.acks = acks; }

    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }

    @Override
    public String toString() {
        return "KafkaProperties{" +
                "enabled=" + enabled +
                ", bootstrapServers='" + bootstrapServers + '\'' +
                ", clientId='" + clientId + '\'' +
                ", retries=" + retries +
                ", batchSize=" + batchSize +
                ", lingerMs=" + lingerMs +
                ", bufferMemory=" + bufferMemory +
                ", idempotence=" + idempotence +
                ", acks='" + acks + '\'' +
                ", extra=" + extra +
                '}';
    }
}
