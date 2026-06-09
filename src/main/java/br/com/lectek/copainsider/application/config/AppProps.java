package br.com.lectek.copainsider.application.config;

import br.com.lectek.copainsider.adapters.inbound.web.support.TenantScopedSettingsService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AppProps {
    private static final String DEFAULT_WHATSAPP_NUMBER = "5583988853265";
    private static final String DEFAULT_WHATSAPP_DISPLAY = "(83) 98885-3265";
    private static final String DEFAULT_INSTAGRAM_HANDLE = "@embalandosolucoes";
    private static final String LEGACY_INSTAGRAM_USERNAME = "saudemaisfarma";
    private static final String DEFAULT_FACEBOOK_URL = "https://facebook.com/embalandosolucoes";
    private static final String DEFAULT_ADDRESS_STREET = "Centro";
    private static final String DEFAULT_ADDRESS_BAIRRO = "Centro";
    private static final String DEFAULT_ADDRESS_CITY = "Missao Velha";
    private static final String DEFAULT_ADDRESS_STATE = "CE";
    private static final String DEFAULT_ADDRESS_CEP = "";
    private static final String DEFAULT_STORE_NAME = "Embalando Soluções em Enbalagens e Bomboniere";

    private final AppSettingService settings;
    private final Environment environment;
    @Autowired(required = false)
    private TenantScopedSettingsService tenantScopedSettings;

    public AppProps(AppSettingService settings, Environment environment) {
        this.settings = settings;
        this.environment = environment;
    }

    public String getWhatsapp() {
        String raw = resolveWhatsappRaw();
        return (raw == null || raw.isBlank()) ? DEFAULT_WHATSAPP_DISPLAY : raw;
    }

    private static String nonBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstSetting(String... keys) {
        if (keys == null) {
            return "";
        }
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = settingForCurrentTenant(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String firstEnv(String... keys) {
        if (keys == null) {
            return "";
        }
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = environment.getProperty(key, "");
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String sanitizeDigits(String value) {
        if (value == null) return "";
        return value.replaceAll("\\D+", "");
    }

    private String settingForCurrentTenant(final String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        if (tenantScopedSettings == null) {
            return settings.get(key).orElse("");
        }
        final String tenantContextId = resolveTenantContextId();
        return tenantScopedSettings.getOrDefault(tenantContextId, key, "");
    }

    private String resolveTenantContextId() {
        final ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "";
        }
        final HttpServletRequest request = attrs.getRequest();
        if (request == null) {
            return "";
        }
        return tenantScopedSettings == null
                ? ""
                : tenantScopedSettings.resolveTenantContextId(request);
    }

    private String resolveWhatsappRaw() {
        String envValue = environment.getProperty("app.whatsapp", environment.getProperty("APP_WHATSAPP", ""));
        String fallback = envValue == null ? "" : envValue;
        return settings.get("contato.whatsapp")
                .orElseGet(() -> settings.getOrDefault("whatsapp", fallback))
                .trim();
    }

    public String getWhatsappLink() {
        String digits = sanitizeDigits(resolveWhatsappRaw());
        if (digits.isBlank()) digits = DEFAULT_WHATSAPP_NUMBER;
        return "https://wa.me/" + digits;
    }

    public String getWhatsappDigits() {
        String digits = sanitizeDigits(resolveWhatsappRaw());
        return digits.isBlank() ? DEFAULT_WHATSAPP_NUMBER : digits;
    }

    public String getWhatsappDisplay() {
        String raw = resolveWhatsappRaw();
        return (raw == null || raw.isBlank()) ? DEFAULT_WHATSAPP_DISPLAY : raw;
    }

    public String getWhatsappTelephone() {
        String digits = getWhatsappDigits();
        if (!digits.startsWith("55")) digits = "55" + digits;
        return "+" + digits;
    }

    public String getPhoneDisplay() {
        String configured = firstNonBlank(
                firstSetting("contato.telefone", "GERAL.telefone"),
                firstSetting("contato.whatsapp", "GERAL.whatsapp"),
                firstEnv("APP_TELEPHONE", "APP_PHONE"),
                resolveWhatsappRaw()
        );
        return configured.isBlank() ? DEFAULT_WHATSAPP_DISPLAY : configured;
    }

    public String getPhoneLink() {
        String digits = sanitizeDigits(getPhoneDisplay());
        if (digits.isBlank()) {
            digits = getWhatsappDigits();
        }
        if (!digits.startsWith("55")) {
            digits = "55" + digits;
        }
        return "tel:+" + digits;
    }

    private String resolveInstagramRaw() {
        return firstNonBlank(
                firstSetting("contato.instagram", "GERAL.instagram"),
                firstEnv("APP_INSTAGRAM", "INSTAGRAM_URL", "INSTAGRAM_HANDLE")
        );
    }

    private static String extractHandleFromUrl(String url) {
        String clean = nonBlank(url);
        if (clean.isBlank()) {
            return "";
        }
        clean = clean.replaceFirst("https?://", "");
        int slash = clean.indexOf('/');
        if (slash >= 0) {
            clean = clean.substring(slash + 1);
        }
        int query = clean.indexOf('?');
        if (query >= 0) {
            clean = clean.substring(0, query);
        }
        int hash = clean.indexOf('#');
        if (hash >= 0) {
            clean = clean.substring(0, hash);
        }
        clean = clean.replaceAll("^/+", "").replaceAll("/+$", "");
        if (clean.isBlank()) {
            return "";
        }
        String[] chunks = clean.split("/");
        String last = chunks[chunks.length - 1].trim();
        if (last.isBlank()) {
            return "";
        }
        return last.startsWith("@") ? last.substring(1) : last;
    }

    private static boolean isLegacyInstagramValue(String value) {
        String normalized = nonBlank(value).toLowerCase();
        if (normalized.isBlank()) {
            return false;
        }
        normalized = normalized.replaceFirst("^https?://", "");
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        if (normalized.startsWith("instagram.com/")) {
            normalized = normalized.substring("instagram.com/".length());
        }
        normalized = normalized.replaceAll("^@+", "");
        int query = normalized.indexOf('?');
        if (query >= 0) {
            normalized = normalized.substring(0, query);
        }
        int hash = normalized.indexOf('#');
        if (hash >= 0) {
            normalized = normalized.substring(0, hash);
        }
        int slash = normalized.indexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(0, slash);
        }
        return LEGACY_INSTAGRAM_USERNAME.equals(normalized);
    }

    public String getInstagramDisplay() {
        String raw = resolveInstagramRaw();
        if (raw.isBlank()) {
            return DEFAULT_INSTAGRAM_HANDLE;
        }

        String value = raw.trim();
        if (isLegacyInstagramValue(value)) {
            return DEFAULT_INSTAGRAM_HANDLE;
        }
        if (value.startsWith("@")) {
            return value;
        }
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("instagram.com/")) {
            String url = value.startsWith("instagram.com/") ? "https://" + value : value;
            String handle = extractHandleFromUrl(url);
            return handle.isBlank() ? DEFAULT_INSTAGRAM_HANDLE : "@" + handle;
        }
        if (value.matches("[A-Za-z0-9._]+")) {
            return "@" + value;
        }
        return DEFAULT_INSTAGRAM_HANDLE;
    }

    public String getInstagramUrl() {
        String raw = resolveInstagramRaw();
        if (raw.isBlank()) {
            return "https://instagram.com/" + DEFAULT_INSTAGRAM_HANDLE.substring(1);
        }

        String value = raw.trim();
        if (isLegacyInstagramValue(value)) {
            return "https://instagram.com/" + DEFAULT_INSTAGRAM_HANDLE.substring(1);
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("instagram.com/")) {
            return "https://" + value;
        }
        if (value.startsWith("@")) {
            return "https://instagram.com/" + value.substring(1);
        }
        if (value.matches("[A-Za-z0-9._]+")) {
            return "https://instagram.com/" + value;
        }
        return "https://instagram.com/" + DEFAULT_INSTAGRAM_HANDLE.substring(1);
    }

    public String getFacebookUrl() {
        String raw = firstNonBlank(
                firstSetting("contato.facebook", "GERAL.facebook"),
                firstEnv("APP_FACEBOOK_URL", "FACEBOOK_URL", "FACEBOOK_PAGE"),
                DEFAULT_FACEBOOK_URL
        );
        if (raw.isBlank()) {
            return DEFAULT_FACEBOOK_URL;
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        if (raw.startsWith("facebook.com/")) {
            return "https://" + raw;
        }
        if (raw.startsWith("@")) {
            return "https://facebook.com/" + raw.substring(1);
        }
        return "https://facebook.com/" + raw;
    }

    public String getAddressDisplay() {
        String street = firstNonBlank(firstSetting("endereco.logradouro", "GERAL.endereco"), DEFAULT_ADDRESS_STREET);
        String bairro = firstNonBlank(firstSetting("endereco.bairro", "GERAL.bairro"), DEFAULT_ADDRESS_BAIRRO);
        String city = firstNonBlank(firstSetting("endereco.cidade", "GERAL.cidade"), DEFAULT_ADDRESS_CITY);
        String state = firstNonBlank(firstSetting("endereco.estado", "GERAL.estado"), DEFAULT_ADDRESS_STATE);

        String cityState = city;
        if (!state.isBlank()) {
            cityState = city.isBlank() ? state : city + "/" + state;
        }

        List<String> parts = new ArrayList<>();
        if (!street.isBlank()) {
            parts.add(street);
        }
        if (!bairro.isBlank()) {
            parts.add(bairro);
        }
        if (!cityState.isBlank()) {
            parts.add(cityState);
        }
        return String.join(" - ", parts);
    }

    public String getAddressQuery() {
        String street = firstNonBlank(firstSetting("endereco.logradouro", "GERAL.endereco"), DEFAULT_ADDRESS_STREET);
        String bairro = firstNonBlank(firstSetting("endereco.bairro", "GERAL.bairro"), DEFAULT_ADDRESS_BAIRRO);
        String city = firstNonBlank(firstSetting("endereco.cidade", "GERAL.cidade"), DEFAULT_ADDRESS_CITY);
        String state = firstNonBlank(firstSetting("endereco.estado", "GERAL.estado"), DEFAULT_ADDRESS_STATE);
        String cep = firstNonBlank(firstSetting("endereco.cep", "GERAL.cep"), DEFAULT_ADDRESS_CEP);

        List<String> parts = new ArrayList<>();
        if (!street.isBlank()) {
            parts.add(street);
        }
        if (!bairro.isBlank()) {
            parts.add(bairro);
        }
        if (!city.isBlank()) {
            parts.add(city);
        }
        if (!state.isBlank()) {
            parts.add(state);
        }
        if (!cep.isBlank()) {
            parts.add(cep);
        }
        return String.join(", ", parts);
    }

    public String getAddressMapsUrl() {
        String query = getAddressQuery();
        if (query.isBlank()) {
            query = DEFAULT_ADDRESS_STREET + ", " + DEFAULT_ADDRESS_CITY + ", " + DEFAULT_ADDRESS_STATE;
        }
        return "https://maps.google.com/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    public String getStoreDisplayName() {
        return firstNonBlank(
                firstSetting("loja.nome_exibicao", "loja.nome_fantasia", "GERAL.nome_loja_site", "GERAL.nome_fantasia"),
                DEFAULT_STORE_NAME
        );
    }
}
