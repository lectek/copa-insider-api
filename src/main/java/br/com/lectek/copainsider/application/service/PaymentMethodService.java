package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.view.PaymentMethodVM;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodService {

    private static final Set<String> CHECKOUT_SUPPORTED_VALUES =
            Set.of("pix", "credito", "debito");

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public PaymentMethodService(AppSettingService settings, ObjectMapper objectMapper) {
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    public List<PaymentMethodVM> listActiveMethods() {
        List<PaymentMethodVM> methods = new ArrayList<>();

        boolean pixAtivo = settings.getBoolean("pg.pix_ativo", true);
        boolean cartaoAtivo = settings.getBoolean("pg.cartao_ativo", true);
        boolean boletoAtivo = settings.getBoolean("pg.boleto_ativo", false);
        boolean dinheiroAtivo = settings.getBoolean("pg.dinheiro_ativo", true);

        if (pixAtivo) {
            methods.add(new PaymentMethodVM("pix", "PIX (recomendado)", "online"));
        }
        if (boletoAtivo) {
            methods.add(new PaymentMethodVM("boleto", "Boleto Bancario", "online"));
        }
        if (cartaoAtivo) {
            methods.add(new PaymentMethodVM("credito", "Cartao de Credito", "online"));
            methods.add(new PaymentMethodVM("debito", "Cartao de Debito", "online"));
        }
        if (dinheiroAtivo) {
            methods.add(new PaymentMethodVM("dinheiro", "Dinheiro", "offline"));
        }

        methods.addAll(loadCustomPaymentMethods());
        return methods;
    }

    public boolean isActiveValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (PaymentMethodVM method : listActiveMethods()) {
            if (value.equals(method.value())) {
                return true;
            }
        }
        return false;
    }

    public List<PaymentMethodVM> listCheckoutMethods() {
        return listActiveMethods().stream()
                .filter(method -> CHECKOUT_SUPPORTED_VALUES.contains(method.value()))
                .map(this::toCheckoutMethod)
                .toList();
    }

    public boolean isCheckoutSupportedValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return listCheckoutMethods().stream()
                .anyMatch(method -> value.equals(method.value()));
    }

    public String resolveLabel(String value) {
        if (value == null) return "";
        for (PaymentMethodVM method : listActiveMethods()) {
            if (value.equals(method.value())) {
                return method.label();
            }
        }
        return value;
    }

    public String resolveCheckoutLabel(String value) {
        if (value == null) {
            return "";
        }
        for (PaymentMethodVM method : listCheckoutMethods()) {
            if (value.equals(method.value())) {
                return method.label();
            }
        }
        return resolveLabel(value);
    }

    public String resolveType(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        for (PaymentMethodVM method : listActiveMethods()) {
            if (value.equals(method.value())) {
                return method.tipo() == null ? "" : method.tipo().trim().toLowerCase(Locale.ROOT);
            }
        }
        return inferTypeFromValue(value);
    }

    public boolean isOfflineValue(final String value) {
        return "offline".equals(resolveType(value));
    }

    private List<PaymentMethodVM> loadCustomPaymentMethods() {
        String raw = settings.getOrDefault("pg.custom_methods", "[]");
        List<PaymentMethodVM> out = new ArrayList<>();
        try {
            List<CustomPaymentMethod> list = objectMapper.readValue(raw, new TypeReference<List<CustomPaymentMethod>>() {});
            if (list == null) {
                return out;
            }
            for (CustomPaymentMethod m : list) {
                if (m == null || !m.isAtivo()) {
                    continue;
                }
                String tipo = normalizeType(m.getTipo());
                if ("pos".equals(tipo)) {
                    // POS/maquineta should not appear in customer checkout.
                    continue;
                }
                String label = m.getNome();
                if (m.getTaxa() != null && m.getTaxa().compareTo(BigDecimal.ZERO) > 0) {
                    label = label + " (" + m.getTaxa().toPlainString() + "%)";
                }
                String value = "custom:" + (m.getId() == null ? m.getNome().toLowerCase() : m.getId());
                out.add(new PaymentMethodVM(value, label, tipo));
            }
        } catch (Exception ex) {
            return out;
        }
        return out;
    }

    private String normalizeType(String raw) {
        String tipo = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return tipo.isBlank() ? "custom" : tipo;
    }

    private String inferTypeFromValue(final String value) {
        final String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "dinheiro" -> "offline";
            case "pix", "credito", "debito", "boleto" -> "online";
            default -> normalized.startsWith("custom:") ? "custom" : "";
        };
    }

    private PaymentMethodVM toCheckoutMethod(PaymentMethodVM method) {
        return switch (method.value()) {
            case "pix" -> new PaymentMethodVM("pix", "PIX", "online");
            case "credito" -> new PaymentMethodVM(
                    "credito",
                    "Cartao de Credito",
                    "online"
            );
            case "debito" -> new PaymentMethodVM(
                    "debito",
                    "Cartao de Debito",
                    "online"
            );
            default -> method;
        };
    }

    private static class CustomPaymentMethod {
        private String id;
        private String nome;
        private String tipo;
        private BigDecimal taxa;
        private boolean ativo;

        public String getId() {
            return id;
        }

        @SuppressWarnings("unused")
        public void setId(String id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        @SuppressWarnings("unused")
        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo;
        }

        @SuppressWarnings("unused")
        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getTaxa() {
            return taxa;
        }

        @SuppressWarnings("unused")
        public void setTaxa(BigDecimal taxa) {
            this.taxa = taxa;
        }

        public boolean isAtivo() {
            return ativo;
        }

        @SuppressWarnings("unused")
        public void setAtivo(boolean ativo) {
            this.ativo = ativo;
        }
    }
}
