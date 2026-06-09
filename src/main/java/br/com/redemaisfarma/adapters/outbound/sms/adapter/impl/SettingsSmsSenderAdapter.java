package br.com.redemaisfarma.adapters.outbound.sms.adapter.impl;

import br.com.redemaisfarma.adapters.outbound.sms.adapter.SmsSenderAdapter;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SettingsSmsSenderAdapter implements SmsSenderAdapter {
    private static final Logger log = LoggerFactory.getLogger(SettingsSmsSenderAdapter.class);
    private static final String PROVIDER_TWILIO = "twilio";
    private static final String PROVIDER_BREVO = "brevo";

    private static final String[] KEYS_ENABLED = {"sms.enabled", "sms.ativo"};
    private static final String[] KEYS_PROVIDER = {"sms.provider"};
    private static final String[] KEYS_FROM = {"sms.from", "sms.from_number", "sms.twilio.from_number"};
    private static final String[] KEYS_DEFAULT_COUNTRY_CODE = {"sms.default_country_code", "sms.default_country"};
    private static final String[] KEYS_TWILIO_ACCOUNT_SID = {"sms.twilio.account_sid", "sms.twilio_account_sid"};
    private static final String[] KEYS_TWILIO_AUTH_TOKEN = {"sms.twilio.auth_token", "sms.twilio_auth_token"};
    private static final String[] KEYS_TWILIO_MESSAGING_SERVICE_SID = {
            "sms.twilio.messaging_service_sid",
            "sms.twilio_messaging_service_sid"
    };
    private static final String[] KEYS_BREVO_API_KEY = {"sms.brevo.api_key", "sms.brevo_api_key", "sms.api_key"};
    private static final String[] KEYS_BREVO_BASE_URL = {"sms.brevo.base_url", "sms.brevo_base_url", "sms.api_base_url"};
    private static final String[] KEYS_BREVO_TYPE = {"sms.brevo.type", "sms.brevo_type"};

    private static final String[] ENV_ENABLED = {"APP_SMS_ENABLED", "SMS_ENABLED"};
    private static final String[] ENV_PROVIDER = {"APP_SMS_PROVIDER", "SMS_PROVIDER"};
    private static final String[] ENV_FROM = {"APP_SMS_FROM", "SMS_FROM", "TWILIO_FROM_NUMBER"};
    private static final String[] ENV_DEFAULT_COUNTRY_CODE = {"APP_SMS_DEFAULT_COUNTRY_CODE", "SMS_DEFAULT_COUNTRY_CODE"};
    private static final String[] ENV_TWILIO_ACCOUNT_SID = {"APP_SMS_TWILIO_ACCOUNT_SID", "TWILIO_ACCOUNT_SID"};
    private static final String[] ENV_TWILIO_AUTH_TOKEN = {"APP_SMS_TWILIO_AUTH_TOKEN", "TWILIO_AUTH_TOKEN"};
    private static final String[] ENV_TWILIO_MESSAGING_SERVICE_SID = {
            "APP_SMS_TWILIO_MESSAGING_SERVICE_SID",
            "TWILIO_MESSAGING_SERVICE_SID"
    };
    private static final String[] ENV_TWILIO_BASE_URL = {"APP_SMS_TWILIO_BASE_URL", "TWILIO_API_BASE_URL"};
    private static final String[] ENV_BREVO_API_KEY = {
            "APP_SMS_BREVO_API_KEY",
            "SMS_BREVO_API_KEY",
            "APP_SMS_API_KEY",
            "SMS_API_KEY",
            "APP_MAIL_API_KEY"
    };
    private static final String[] ENV_BREVO_BASE_URL = {
            "APP_SMS_BREVO_BASE_URL",
            "SMS_BREVO_BASE_URL",
            "APP_SMS_API_BASE_URL",
            "SMS_API_BASE_URL"
    };
    private static final String[] ENV_BREVO_TYPE = {"APP_SMS_BREVO_TYPE", "SMS_BREVO_TYPE"};

    private final AppSettingService settings;
    private final Environment env;
    private final HttpClient httpClient;

    public SettingsSmsSenderAdapter(AppSettingService settings, Environment env) {
        this.settings = settings;
        this.env = env;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String send(String destination, String message) {
        if (!isEnabled()) {
            throw new IllegalStateException("SMS sending is disabled.");
        }

        String provider = resolveProvider();
        if (PROVIDER_TWILIO.equals(provider)) {
            return sendViaTwilio(destination, message);
        }
        if (PROVIDER_BREVO.equals(provider)) {
            return sendViaBrevo(destination, message);
        }
        throw new IllegalStateException("Unsupported SMS provider: " + provider);
    }

    private String sendViaTwilio(String destination, String message) {
        String accountSid = requireNonBlank(
                getString(KEYS_TWILIO_ACCOUNT_SID, ENV_TWILIO_ACCOUNT_SID, ""),
                "Twilio account SID not configured."
        );
        String authToken = requireNonBlank(
                getString(KEYS_TWILIO_AUTH_TOKEN, ENV_TWILIO_AUTH_TOKEN, ""),
                "Twilio auth token not configured."
        );
        String from = getString(KEYS_FROM, ENV_FROM, "").trim();
        String messagingServiceSid = getString(KEYS_TWILIO_MESSAGING_SERVICE_SID, ENV_TWILIO_MESSAGING_SERVICE_SID, "").trim();
        if (from.isBlank() && messagingServiceSid.isBlank()) {
            throw new IllegalStateException("Twilio sender is not configured. Set APP_SMS_FROM or TWILIO_MESSAGING_SERVICE_SID.");
        }

        String defaultCountryCode = getString(KEYS_DEFAULT_COUNTRY_CODE, ENV_DEFAULT_COUNTRY_CODE, "55");
        String to = normalizeToE164(destination, defaultCountryCode);
        String body = normalizeMessage(message);
        String baseUrl = getFirstEnv(ENV_TWILIO_BASE_URL);
        if (baseUrl.isBlank()) {
            baseUrl = "https://api.twilio.com";
        }
        baseUrl = trimTrailingSlash(baseUrl);

        Map<String, String> form = new LinkedHashMap<>();
        form.put("To", to);
        form.put("Body", body);
        if (!messagingServiceSid.isBlank()) {
            form.put("MessagingServiceSid", messagingServiceSid);
        } else {
            form.put("From", from);
        }

        String endpoint = baseUrl + "/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formUrlEncode(form)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 == 2) {
                String providerId = extractJsonScalarField(response.body(), "sid");
                return providerId.isBlank() ? "accepted" : providerId;
            }
            log.warn("[sms] Twilio rejected request. status={} body={}", response.statusCode(), safeSnippet(response.body()));
            throw new IllegalStateException("Twilio rejected SMS request.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS request interrupted.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("SMS provider connection failed.", ex);
        }
    }

    private String sendViaBrevo(String destination, String message) {
        String apiKey = requireNonBlank(
                getString(KEYS_BREVO_API_KEY, ENV_BREVO_API_KEY, ""),
                "Brevo API key not configured."
        );
        String fromRaw = requireNonBlank(
                getString(KEYS_FROM, ENV_FROM, ""),
                "Brevo sender is not configured. Set APP_SMS_FROM."
        );
        String sender = normalizeBrevoSender(fromRaw);
        String defaultCountryCode = getString(KEYS_DEFAULT_COUNTRY_CODE, ENV_DEFAULT_COUNTRY_CODE, "55");
        String recipient = normalizeToBrevoRecipient(destination, defaultCountryCode);
        String content = normalizeMessage(message);
        String type = normalizeBrevoType(getString(KEYS_BREVO_TYPE, ENV_BREVO_TYPE, "transactional"));
        String baseUrl = getString(KEYS_BREVO_BASE_URL, ENV_BREVO_BASE_URL, "").trim();
        if (baseUrl.isBlank()) {
            baseUrl = "https://api.brevo.com";
        }
        baseUrl = trimTrailingSlash(baseUrl);

        String payload = "{"
                + "\"sender\":\"" + escapeJson(sender) + "\","
                + "\"recipient\":\"" + escapeJson(recipient) + "\","
                + "\"content\":\"" + escapeJson(content) + "\","
                + "\"type\":\"" + escapeJson(type) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v3/transactionalSMS/send"))
                .timeout(Duration.ofSeconds(20))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 == 2) {
                String providerId = extractJsonScalarField(response.body(), "messageId");
                return providerId.isBlank() ? "accepted" : providerId;
            }
            log.warn("[sms] Brevo rejected request. status={} body={}", response.statusCode(), safeSnippet(response.body()));
            throw new IllegalStateException("Brevo rejected SMS request.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS request interrupted.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("SMS provider connection failed.", ex);
        }
    }

    private String resolveProvider() {
        String envProvider = getFirstEnv(ENV_PROVIDER);
        if (!envProvider.isBlank()) {
            return envProvider.trim().toLowerCase(Locale.ROOT);
        }
        String settingProvider = getFirstConfigured(KEYS_PROVIDER);
        if (!settingProvider.isBlank()) {
            return settingProvider.trim().toLowerCase(Locale.ROOT);
        }
        return PROVIDER_TWILIO;
    }

    private boolean isEnabled() {
        String settingValue = getFirstConfigured(KEYS_ENABLED);
        if (!settingValue.isBlank()) {
            return parseBoolean(settingValue, false);
        }
        String envValue = getFirstEnv(ENV_ENABLED);
        if (!envValue.isBlank()) {
            return parseBoolean(envValue, false);
        }
        return false;
    }

    private String normalizeToE164(String destination, String defaultCountryCodeRaw) {
        String digits = destination == null ? "" : destination.replaceAll("\\D", "");
        if (digits.startsWith("00")) {
            digits = digits.substring(2);
        }
        if (digits.isBlank()) {
            throw new IllegalArgumentException("SMS destination is empty.");
        }

        String defaultCountryCode = normalizeCountryCode(defaultCountryCodeRaw);
        if (digits.length() <= 11 && !defaultCountryCode.isBlank()) {
            digits = defaultCountryCode + digits;
        }
        if (!digits.matches("\\d{10,15}")) {
            throw new IllegalArgumentException("SMS destination must have between 10 and 15 digits.");
        }
        return "+" + digits;
    }

    private String normalizeCountryCode(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() > 4) {
            return digits.substring(0, 4);
        }
        return digits;
    }

    private String normalizeMessage(String raw) {
        String text = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (text.isBlank()) {
            throw new IllegalArgumentException("SMS message is empty.");
        }
        if (text.length() > 480) {
            return text.substring(0, 480);
        }
        return text;
    }

    private String normalizeBrevoSender(String raw) {
        String sender = raw == null ? "" : raw.trim();
        if (sender.startsWith("+") && sender.substring(1).matches("\\d{1,15}")) {
            return sender.substring(1);
        }
        return sender;
    }

    private String normalizeToBrevoRecipient(String destination, String defaultCountryCodeRaw) {
        String e164 = normalizeToE164(destination, defaultCountryCodeRaw);
        return e164.startsWith("+") ? e164.substring(1) : e164;
    }

    private String normalizeBrevoType(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return "marketing".equals(normalized) ? "marketing" : "transactional";
    }

    private String formUrlEncode(Map<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(urlEncode(entry.getKey()))
              .append('=')
              .append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String getString(String[] settingKeys, String[] envKeys, String defaultValue) {
        String fromSetting = getFirstConfigured(settingKeys);
        if (!fromSetting.isBlank()) {
            return fromSetting;
        }
        String fromEnv = getFirstEnv(envKeys);
        if (!fromEnv.isBlank()) {
            return fromEnv;
        }
        return defaultValue;
    }

    private String getFirstConfigured(String[] keys) {
        for (String key : keys) {
            try {
                String value = settings.getOrDefault(key, "");
                if (value != null && !value.trim().isBlank()) {
                    return value.trim();
                }
            } catch (Exception ex) {
                log.debug("[sms] unable to read app_setting key={}", key, ex);
            }
        }
        return "";
    }

    private String getFirstEnv(String[] keys) {
        for (String key : keys) {
            String value = env.getProperty(key);
            if (value != null && !value.trim().isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean parseBoolean(String raw, boolean defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return defaultValue;
        }
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("on")
                || normalized.equals("sim")
                || normalized.equals("verdade");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value.trim();
    }

    private String extractJsonScalarField(String json, String field) {
        if (json == null || json.isBlank()) {
            return "";
        }
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return "";
        }
        int valueStart = start + pattern.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) {
            return "";
        }
        if (json.charAt(valueStart) == '"') {
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd <= valueStart) {
                return "";
            }
            return json.substring(valueStart + 1, valueEnd).trim();
        }

        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == ',' || c == '}' || Character.isWhitespace(c)) {
                break;
            }
            valueEnd++;
        }
        if (valueEnd <= valueStart) {
            return "";
        }
        return json.substring(valueStart, valueEnd).trim();
    }

    private String safeSnippet(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() > 220 ? compact.substring(0, 220) + "..." : compact;
    }

    private String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
