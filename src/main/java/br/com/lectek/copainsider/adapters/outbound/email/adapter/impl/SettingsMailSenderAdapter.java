package br.com.lectek.copainsider.adapters.outbound.email.adapter.impl;

import br.com.lectek.copainsider.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SettingsMailSenderAdapter implements MailSenderAdapter {
    private static final Logger log = LoggerFactory.getLogger(SettingsMailSenderAdapter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PROVIDER_BREVO = "brevo";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private static final String KEY_ENABLED = "email.enabled";
    private static final String KEY_SMTP_HOST = "email.smtp_host";
    private static final String KEY_SMTP_PORT = "email.smtp_port";
    private static final String KEY_SMTP_USER = "email.smtp_user";
    private static final String KEY_SMTP_PASS = "email.smtp_pass";
    private static final String KEY_SMTP_TLS = "email.smtp_tls";
    private static final String KEY_SMTP_SSL = "email.smtp_ssl";
    private static final String KEY_FROM_EMAIL = "email.from_email";
    private static final String KEY_FROM_NAME = "email.from_name";
    private static final String KEY_REPLY_TO = "email.reply_to";
    private static final String KEY_API_PROVIDER = "email.api_provider";
    private static final String KEY_API_KEY = "email.api_key";
    private static final String KEY_API_BASE_URL = "email.api_base_url";
    private static final String[] KEYS_ENABLED = new String[]{KEY_ENABLED, "email.ativo"};
    private static final String[] KEYS_SMTP_HOST = new String[]{KEY_SMTP_HOST, "email.smtp.host"};
    private static final String[] KEYS_SMTP_PORT = new String[]{KEY_SMTP_PORT, "email.smtp.port", "email.smtp.porta"};
    private static final String[] KEYS_SMTP_USER = new String[]{KEY_SMTP_USER, "email.smtp.user", "email.smtp.usuario"};
    private static final String[] KEYS_SMTP_PASS = new String[]{KEY_SMTP_PASS, "email.smtp.password", "email.smtp.senha"};
    private static final String[] KEYS_SMTP_TLS = new String[]{KEY_SMTP_TLS, "email.smtp.tls", "email.smtp.starttls"};
    private static final String[] KEYS_SMTP_SSL = new String[]{KEY_SMTP_SSL, "email.smtp.ssl"};
    private static final String[] KEYS_FROM_EMAIL = new String[]{KEY_FROM_EMAIL, "email.from_email", "email.remetente"};
    private static final String[] KEYS_FROM_NAME = new String[]{KEY_FROM_NAME, "email.from_name", "email.nome_remetente"};
    private static final String[] KEYS_REPLY_TO = new String[]{KEY_REPLY_TO, "email.reply.to", "email.reply_to"};
    private static final String[] KEYS_API_PROVIDER =
            new String[]{KEY_API_PROVIDER, "email.api.provider", "email.provider"};
    private static final String[] KEYS_API_KEY =
            new String[]{KEY_API_KEY, "email.api.key", "email.brevo.api_key"};
    private static final String[] KEYS_API_BASE_URL =
            new String[]{KEY_API_BASE_URL, "email.api.base_url", "email.brevo.base_url"};
    private static final String[] ENV_MAIL_ENABLED = new String[]{"APP_MAIL_ENABLED", "MAIL_ENABLED"};
    private static final String[] ENV_SMTP_HOST = new String[]{"SPRING_MAIL_HOST", "MAIL_HOST"};
    private static final String[] ENV_SMTP_PORT = new String[]{"SPRING_MAIL_PORT", "MAIL_PORT"};
    private static final String[] ENV_SMTP_USER = new String[]{"SPRING_MAIL_USERNAME", "MAIL_USERNAME"};
    private static final String[] ENV_SMTP_PASS = new String[]{"SPRING_MAIL_PASSWORD", "MAIL_PASSWORD"};
    private static final String[] ENV_SMTP_TLS = new String[]{"SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE", "MAIL_SMTP_STARTTLS_ENABLE"};
    private static final String[] ENV_SMTP_SSL = new String[]{"SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE", "MAIL_SMTP_SSL_ENABLE"};
    private static final String[] ENV_FROM = new String[]{"APP_MAIL_FROM", "MAIL_FROM", "SPRING_MAIL_USERNAME"};
    private static final String[] ENV_FROM_NAME = new String[]{"APP_MAIL_FROM_NAME", "MAIL_FROM_NAME"};
    private static final String[] ENV_REPLY_TO = new String[]{"APP_MAIL_REPLY_TO", "MAIL_REPLY_TO"};
    private static final String[] ENV_API_PROVIDER =
            new String[]{"APP_MAIL_API_PROVIDER", "MAIL_API_PROVIDER", "EMAIL_API_PROVIDER"};
    private static final String[] ENV_API_KEY =
            new String[]{"APP_MAIL_API_KEY", "MAIL_API_KEY", "BREVO_API_KEY", "APP_BREVO_API_KEY"};
    private static final String[] ENV_API_BASE_URL =
            new String[]{"APP_MAIL_API_BASE_URL", "MAIL_API_BASE_URL", "BREVO_API_BASE_URL"};

    private final AppSettingService settings;
    private final Environment env;

    public SettingsMailSenderAdapter(AppSettingService settings, Environment env) {
        this.settings = settings;
        this.env = env;
    }

    @Override
    public String send(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        if (!isEnabled()) {
            log.info("[mail] envio DESLIGADO (email.enabled=false). '{}' -> {}", subject, to);
            return "noop-disabled";
        }

        MailIdentity identity = resolveIdentity();
        MailApiConfig apiConfig = resolveApiConfig();
        String host = getOrDefault(KEYS_SMTP_HOST, ENV_SMTP_HOST, "").trim();
        if (host.isBlank()) {
            if (apiConfig.configured()) {
                return sendViaApi(apiConfig, to, subject, htmlBody, bcc, identity);
            }
            throw new IllegalStateException("SMTP host nao configurado.");
        }

        try {
            return sendViaSmtp(host, to, subject, htmlBody, bcc, identity);
        } catch (Exception ex) {
            if (apiConfig.configured() && isConnectivityFailure(ex)) {
                log.warn("[mail] SMTP indisponivel; tentando fallback API. '{}' -> {}", subject, to);
                return sendViaApi(apiConfig, to, subject, htmlBody, bcc, identity);
            }
            log.error("[mail] falha ao enviar '{}'", subject, ex);
            throw new RuntimeException("Falha ao enviar e-mail", ex);
        }
    }

    private String sendViaSmtp(
            String host,
            String to,
            String subject,
            String htmlBody,
            @Nullable List<String> bcc,
            MailIdentity identity
    ) throws Exception {
        JavaMailSenderImpl sender = buildSender(host);
        MimeMessage msg = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
        helper.setValidateAddresses(true);
        helper.setTo(to);
        setFrom(helper, identity.fromEmail(), identity.fromName());
        if (!identity.replyTo().isBlank()) {
            helper.setReplyTo(identity.replyTo());
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        msg.setSentDate(new Date());
        if (bcc != null && !bcc.isEmpty()) {
            helper.setBcc(bcc.toArray(new String[0]));
        }
        sender.send(msg);
        return msg.getMessageID() != null ? msg.getMessageID() : "<none>";
    }

    private String sendViaApi(
            MailApiConfig apiConfig,
            String to,
            String subject,
            String htmlBody,
            @Nullable List<String> bcc,
            MailIdentity identity
    ) {
        if (!PROVIDER_BREVO.equals(apiConfig.provider())) {
            throw new IllegalStateException("Provedor de email API nao suportado: " + apiConfig.provider());
        }
        try {
            String body = buildBrevoPayload(to, subject, htmlBody, bcc, identity);
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiConfig.baseUrl()))
                    .timeout(Duration.ofSeconds(15))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", apiConfig.apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                String messageId = extractMessageId(response.body());
                if (!messageId.isBlank()) {
                    return messageId;
                }
                return "mail-api-" + status;
            }
            throw new IllegalStateException(
                    "Falha no provedor de email. status="
                            + status
                            + ", body="
                            + truncate(response.body(), 600)
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Envio de email interrompido.", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Falha na comunicacao com API de email.", ex);
        } catch (RuntimeException ex) {
            log.error("[mail] falha no fallback API '{}'", subject, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("[mail] falha no fallback API '{}'", subject, ex);
            throw new RuntimeException("Falha ao enviar e-mail via API.", ex);
        }
    }

    private String buildBrevoPayload(
            String to,
            String subject,
            String htmlBody,
            @Nullable List<String> bcc,
            MailIdentity identity
    ) throws IOException {
        JsonNode root = OBJECT_MAPPER.createObjectNode();
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("subject", subject);
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("htmlContent", htmlBody);

        com.fasterxml.jackson.databind.node.ObjectNode senderNode =
                ((com.fasterxml.jackson.databind.node.ObjectNode) root).putObject("sender");
        senderNode.put("email", identity.fromEmail());
        if (!identity.fromName().isBlank()) {
            senderNode.put("name", identity.fromName());
        }

        com.fasterxml.jackson.databind.node.ArrayNode toNode =
                ((com.fasterxml.jackson.databind.node.ObjectNode) root).putArray("to");
        toNode.addObject().put("email", to);

        List<String> cleanBcc = sanitizeRecipients(bcc);
        if (!cleanBcc.isEmpty()) {
            com.fasterxml.jackson.databind.node.ArrayNode bccNode =
                    ((com.fasterxml.jackson.databind.node.ObjectNode) root).putArray("bcc");
            for (String email : cleanBcc) {
                bccNode.addObject().put("email", email);
            }
        }
        if (!identity.replyTo().isBlank()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) root)
                    .putObject("replyTo")
                    .put("email", identity.replyTo());
        }

        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private List<String> sanitizeRecipients(@Nullable List<String> rawEmails) {
        if (rawEmails == null || rawEmails.isEmpty()) {
            return List.of();
        }
        List<String> emails = new ArrayList<>();
        for (String rawEmail : rawEmails) {
            String email = safeTrim(rawEmail);
            if (!email.isBlank()) {
                emails.add(email);
            }
        }
        return emails;
    }

    private String extractMessageId(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(body);
            String messageId = safeTrim(node.path("messageId").asText(""));
            if (!messageId.isBlank()) {
                return messageId;
            }
            return safeTrim(node.path("id").asText(""));
        } catch (Exception ex) {
            return "";
        }
    }

    private static String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit);
    }

    private boolean isConnectivityFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof UnknownHostException) {
                return true;
            }
            String message = safeTrim(current.getMessage()).toLowerCase(Locale.ROOT);
            if (message.contains("couldn't connect to host")
                    || message.contains("connection timed out")
                    || message.contains("connect timed out")
                    || message.contains("connection refused")
                    || message.contains("read timed out")
                    || message.contains("timeout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private MailIdentity resolveIdentity() {
        String smtpUser = getOrDefault(KEYS_SMTP_USER, ENV_SMTP_USER, "").trim();
        String fromRaw = getOrDefault(KEYS_FROM_EMAIL, ENV_FROM, "").trim();
        ParsedFrom parsedFrom = parseFrom(fromRaw);
        String fromEmail = parsedFrom.email();
        if (fromEmail.isBlank() && smtpUser.contains("@")) {
            fromEmail = smtpUser;
        }
        if (fromEmail.isBlank()) {
            throw new IllegalStateException("Email remetente nao configurado.");
        }

        String fromName = getFirstConfigured(KEYS_FROM_NAME);
        if (fromName.isBlank()) {
            fromName = parsedFrom.name();
        }
        if (fromName.isBlank()) {
            fromName = getFirstEnv(ENV_FROM_NAME);
        }

        String replyTo = getFirstConfigured(KEYS_REPLY_TO);
        if (replyTo.isBlank()) {
            replyTo = getFirstEnv(ENV_REPLY_TO);
        }
        return new MailIdentity(fromEmail, safeTrim(fromName), safeTrim(replyTo));
    }

    private MailApiConfig resolveApiConfig() {
        String providerRaw = getOrDefault(KEYS_API_PROVIDER, ENV_API_PROVIDER, "").toLowerCase(Locale.ROOT);
        String apiKey = getOrDefault(KEYS_API_KEY, ENV_API_KEY, "");
        String baseUrl = getOrDefault(KEYS_API_BASE_URL, ENV_API_BASE_URL, BREVO_API_URL);

        String provider = safeTrim(providerRaw);
        if (provider.isBlank() && !safeTrim(apiKey).isBlank()) {
            provider = PROVIDER_BREVO;
        }
        if (provider.isBlank()) {
            return new MailApiConfig("", "", "");
        }
        return new MailApiConfig(provider, safeTrim(apiKey), safeTrim(baseUrl));
    }

    private JavaMailSenderImpl buildSender(String host) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(readPort());
        String smtpUser = getOrDefault(KEYS_SMTP_USER, ENV_SMTP_USER, "").trim();
        String smtpPass = getOrDefault(KEYS_SMTP_PASS, ENV_SMTP_PASS, "").trim();
        sender.setUsername(smtpUser);
        sender.setPassword(smtpPass);
        sender.setJavaMailProperties(buildProps(smtpUser, smtpPass));
        return sender;
    }

    private int readPort() {
        String raw = getOrDefault(KEYS_SMTP_PORT, ENV_SMTP_PORT, "587").trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    private Properties buildProps(String smtpUser, String smtpPass) {
        Properties props = new Properties();
        boolean tls = getBooleanOrEnv(KEYS_SMTP_TLS, ENV_SMTP_TLS, true);
        boolean ssl = getBooleanOrEnv(KEYS_SMTP_SSL, ENV_SMTP_SSL, false);
        boolean auth = !smtpUser.isBlank() || !smtpPass.isBlank();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }

    private void setFrom(MimeMessageHelper helper, String email, String name) throws Exception {
        if (name != null && !name.isBlank()) {
            helper.setFrom(email, name);
        } else {
            helper.setFrom(email);
        }
    }

    private boolean isEnabled() {
        String settingValue = getFirstConfigured(KEYS_ENABLED);
        if (!settingValue.isBlank()) {
            return parseBoolean(settingValue, false);
        }

        String envValue = getFirstEnv(ENV_MAIL_ENABLED);
        if (!envValue.isBlank()) {
            return parseBoolean(envValue, false);
        }

        String smtpHost = getOrDefault(KEYS_SMTP_HOST, ENV_SMTP_HOST, "");
        return !smtpHost.isBlank();
    }

    private boolean getBooleanOrEnv(String[] keys, String[] envKeys, boolean defaultValue) {
        String raw = getFirstConfigured(keys);
        if (!raw.isBlank()) {
            return parseBoolean(raw, defaultValue);
        }
        String envValue = getFirstEnv(envKeys);
        if (envValue.isBlank()) {
            return defaultValue;
        }
        return parseBoolean(envValue, defaultValue);
    }

    private static String safeTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private boolean parseBoolean(String raw, boolean defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return defaultValue;
        }
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes") || normalized.equals("on") || normalized.equals("verdade") || normalized.equals("sim");
    }

    private String getOrDefault(String[] keys, String[] envKeys, String defaultValue) {
        String value = getFirstConfigured(keys);
        if (!value.isBlank()) return value;

        String envValue = getFirstEnv(envKeys);
        if (!envValue.isBlank()) {
            return envValue;
        }
        return defaultValue;
    }

    private String getFirstConfigured(String[] keys) {
        for (String key : keys) {
            try {
                String value = safeTrim(settings.getOrDefault(key, ""));
                if (!value.isBlank()) {
                    return value;
                }
            } catch (Exception ex) {
                log.debug("[mail] falha ao ler app_setting key={}", key, ex);
            }
        }
        return "";
    }

    private String getFirstEnv(String[] keys) {
        for (String key : keys) {
            String value = env.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private ParsedFrom parseFrom(String rawFrom) {
        String raw = safeTrim(rawFrom);
        if (raw.isBlank()) {
            return new ParsedFrom("", "");
        }
        try {
            InternetAddress[] parsed = InternetAddress.parse(raw, true);
            if (parsed.length > 0) {
                InternetAddress addr = parsed[0];
                String email = safeTrim(addr.getAddress());
                String name = safeTrim(addr.getPersonal());
                if (!email.isBlank()) {
                    return new ParsedFrom(email, name);
                }
            }
        } catch (Exception ignored) {
            // fallback para valor simples no formato "usuario@dominio"
        }
        return new ParsedFrom(raw, "");
    }

    private record ParsedFrom(String email, String name) {}

    private record MailIdentity(String fromEmail, String fromName, String replyTo) {}

    private record MailApiConfig(String provider, String apiKey, String baseUrl) {
        private boolean configured() {
            return !provider.isBlank() && !apiKey.isBlank() && !baseUrl.isBlank();
        }
    }
}
