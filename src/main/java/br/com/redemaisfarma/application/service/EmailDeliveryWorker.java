package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.DependsOn;

@Service
@DependsOn("flyway")
public class EmailDeliveryWorker {
    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryWorker.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SENDING = "SENDING";
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";
    private static final String PROVIDER_SMTP = "SMTP";

    private final EmailDeliveryRepository repository;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean missingTableLogged = new AtomicBoolean(false);

    public EmailDeliveryWorker(
            EmailDeliveryRepository repository,
            MailService mailService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.mailService = mailService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${email.delivery.worker.fixed-delay-ms:15000}")
    @Transactional
    public void processBatch() {
        int batchSize = Math.max(1, Integer.getInteger("email.delivery.worker.batch-size", 50));
        List<EmailDelivery> batch;
        try {
            batch = repository.findByStatusOrderByCreatedAtAsc(
                    STATUS_PENDING,
                    PageRequest.of(0, batchSize)
            );
            missingTableLogged.set(false);
        } catch (DataAccessException ex) {
            handleMissingTable(ex);
            return;
        }
        if (batch.isEmpty()) {
            return;
        }
        for (EmailDelivery item : batch) {
            processItem(item);
            throttle();
        }
    }

    private void processItem(EmailDelivery item) {
        item.setStatus(STATUS_SENDING);
        if (item.getProvider() == null || item.getProvider().isBlank()) {
            item.setProvider(PROVIDER_SMTP);
        }
        repository.save(item);

        try {
            EmailPayload payload = parsePayload(item.getPayloadJson());
            if (payload.subject == null || payload.subject.isBlank()) {
                throw new IllegalArgumentException("Assunto do email nao informado.");
            }

            if (payload.template != null && !payload.template.isBlank()) {
                mailService.sendTemplate(
                        item.getDestination(),
                        payload.subject,
                        payload.template,
                        payload.model,
                        payload.bcc
                );
            } else if (payload.html != null && !payload.html.isBlank()) {
                mailService.sendHtml(item.getDestination(), payload.subject, payload.html, payload.bcc);
            } else if (payload.text != null && !payload.text.isBlank()) {
                mailService.sendText(item.getDestination(), payload.subject, payload.text, payload.bcc);
            } else {
                throw new IllegalArgumentException("Payload nao possui html, text ou template.");
            }

            item.setStatus(STATUS_SENT);
            item.setLastError(null);
            item.setUpdatedAt(Instant.now());
            item.setAttempts(nextAttempts(item));
            repository.save(item);
        } catch (Exception ex) {
            String error = ex.getMessage() == null ? "Falha ao enviar email." : ex.getMessage();
            markFailed(item, error);
            log.warn("[email-delivery] falha ao enviar para {}: {}", maskDestination(item.getDestination()), error);
        }
    }

    private void markFailed(EmailDelivery item, String error) {
        int attempts = nextAttempts(item);
        int maxAttempts = Math.max(1, Integer.getInteger("email.delivery.worker.max-attempts", 5));
        item.setAttempts(attempts);
        item.setLastError(error);
        item.setUpdatedAt(Instant.now());
        if (attempts >= maxAttempts) {
            item.setStatus(STATUS_FAILED);
        } else {
            item.setStatus(STATUS_PENDING);
        }
        repository.save(item);
    }

    private int nextAttempts(EmailDelivery item) {
        return (item.getAttempts() == null ? 0 : item.getAttempts()) + 1;
    }

    private EmailPayload parsePayload(String payloadJson) {
        Map<String, Object> map = readPayload(payloadJson);
        String subject = readString(map, "subject", "assunto");
        String template = readString(map, "template", "templateKey");
        String html = readString(map, "html", "htmlBody");
        String text = readString(map, "text", "textBody");
        List<String> bcc = readStringList(map.get("bcc"));
        Map<String, Object> model = readModel(map);
        return new EmailPayload(subject, template, html, text, bcc, model);
    }

    private Map<String, Object> readPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            return map == null ? new HashMap<>() : map;
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private String readString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String str && !str.isBlank()) {
                return str;
            }
        }
        return null;
    }

    private Map<String, Object> readModel(Map<String, Object> map) {
        Object modelValue = map.get("model");
        if (modelValue instanceof Map<?, ?> modelMap) {
            Map<String, Object> out = new HashMap<>();
            for (Map.Entry<?, ?> entry : modelMap.entrySet()) {
                if (entry.getKey() != null) {
                    out.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return out;
        }
        return new HashMap<>();
    }

    private List<String> readStringList(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object entry : list) {
                if (entry != null) {
                    String value = entry.toString().trim();
                    if (!value.isBlank()) {
                        out.add(value);
                    }
                }
            }
            return out.isEmpty() ? null : out;
        }
        String value = raw.toString().trim();
        return value.isBlank() ? null : List.of(value);
    }

    private void throttle() {
        long delayMs = Long.getLong("email.delivery.worker.throttle-ms", 0L);
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleMissingTable(DataAccessException ex) {
        if (isTableMissing(ex)) {
            if (missingTableLogged.compareAndSet(false, true)) {
                log.error("[email-delivery] tabela 'email_delivery' ausente; worker ficará aguardando a aplicação das migrations. Cause: {}", rootCauseMessage(ex));
            } else {
                log.debug("[email-delivery] ainda aguardando tabela 'email_delivery'; próxima tentativa será na próxima execução.");
            }
            return;
        }
        throw ex;
    }

    String maskDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            return "<desconhecido>";
        }
        int at = destination.indexOf('@');
        if (at <= 0) {
            return "<destino oculto>";
        }
        String domain = destination.substring(at + 1);
        return "***@" + domain;
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private boolean isTableMissing(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SQLException sql && "42S02".equals(sql.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static final class EmailPayload {
        private final String subject;
        private final String template;
        private final String html;
        private final String text;
        private final List<String> bcc;
        private final Map<String, Object> model;

        private EmailPayload(String subject,
                             String template,
                             String html,
                             String text,
                             List<String> bcc,
                             Map<String, Object> model) {
            this.subject = subject;
            this.template = template;
            this.html = html;
            this.text = text;
            this.bcc = bcc;
            this.model = model;
        }
    }
}
