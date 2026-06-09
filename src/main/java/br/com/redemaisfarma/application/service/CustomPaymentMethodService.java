package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomPaymentMethodService {

    private static final String KEY_CUSTOM_METHODS = "pg.custom_methods";
    private static final String DESC_CUSTOM_METHODS =
            "Metodos de pagamento personalizados";
    private static final String DEFAULT_TYPE = "custom";
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "offline",
            "online",
            "custom",
            "pos"
    );

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public CustomPaymentMethodService(
            final AppSettingService appSettingService,
            final ObjectMapper objectMapperValue
    ) {
        this.settings = appSettingService;
        this.objectMapper = objectMapperValue;
    }

    @Transactional(readOnly = true)
    public List<CustomPaymentMethod> list() {
        return loadMethods().stream()
                .sorted(
                        Comparator.comparing(
                                CustomPaymentMethod::getNome,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    @Transactional
    public CustomPaymentMethod create(final CustomPaymentMethodInput input) {
        final String nome = normalizeName(input.nome());
        final List<CustomPaymentMethod> methods = loadMethods();
        ensureUniqueName(methods, nome, null);

        final CustomPaymentMethod method = new CustomPaymentMethod();
        method.setId(UUID.randomUUID().toString());
        method.setNome(nome);
        method.setTipo(normalizeType(input.tipo()));
        method.setTaxa(sanitizeTaxa(input.taxa()));
        method.setAtivo(input.ativo() == null || input.ativo());

        methods.add(method);
        saveMethods(methods);
        return method;
    }

    @Transactional
    public CustomPaymentMethod update(
            final String id,
            final CustomPaymentMethodInput input
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id do metodo obrigatorio.");
        }

        final String nome = normalizeName(input.nome());
        final List<CustomPaymentMethod> methods = loadMethods();
        final CustomPaymentMethod current = methods.stream()
                .filter(method -> id.equals(method.getId()))
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Metodo de pagamento nao encontrado."
                        )
                );

        ensureUniqueName(methods, nome, id);
        current.setNome(nome);
        current.setTipo(normalizeType(input.tipo()));
        current.setTaxa(sanitizeTaxa(input.taxa()));
        current.setAtivo(input.ativo() == null || input.ativo());

        saveMethods(methods);
        return current;
    }

    @Transactional
    public void delete(final String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id do metodo obrigatorio.");
        }
        final List<CustomPaymentMethod> methods = loadMethods();
        final boolean removed = methods.removeIf(method -> id.equals(method.getId()));
        if (!removed) {
            throw new NoSuchElementException("Metodo de pagamento nao encontrado.");
        }
        saveMethods(methods);
    }

    private List<CustomPaymentMethod> loadMethods() {
        final String raw = settings.getOrDefault(KEY_CUSTOM_METHODS, "[]");
        try {
            final List<CustomPaymentMethod> list = objectMapper.readValue(
                    raw,
                    new TypeReference<List<CustomPaymentMethod>>() {
                    }
            );
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private void saveMethods(final List<CustomPaymentMethod> methods) {
        try {
            final String json = objectMapper.writeValueAsString(
                    methods == null ? List.of() : methods
            );
            settings.upsert(KEY_CUSTOM_METHODS, json, DESC_CUSTOM_METHODS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao salvar metodos de pagamento.",
                    ex
            );
        }
    }

    private void ensureUniqueName(
            final List<CustomPaymentMethod> methods,
            final String nome,
            final String exceptId
    ) {
        final String normalized = nome.toLowerCase(Locale.ROOT);
        final boolean exists = methods.stream()
                .anyMatch(method -> {
                    if (exceptId != null && exceptId.equals(method.getId())) {
                        return false;
                    }
                    final String current = method.getNome() == null
                            ? ""
                            : method.getNome().trim().toLowerCase(Locale.ROOT);
                    return current.equals(normalized);
                });
        if (exists) {
            throw new IllegalStateException("Metodo de pagamento ja existe.");
        }
    }

    private String normalizeName(final String value) {
        final String nome = value == null ? "" : value.trim();
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Nome do metodo obrigatorio.");
        }
        return nome;
    }

    private String normalizeType(final String value) {
        final String type = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (type.isBlank()) {
            return DEFAULT_TYPE;
        }
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "Tipo invalido. Use offline, online, custom ou pos."
            );
        }
        return type;
    }

    private BigDecimal sanitizeTaxa(final BigDecimal taxa) {
        if (taxa == null) {
            return null;
        }
        if (taxa.signum() < 0) {
            throw new IllegalArgumentException(
                    "Taxa nao pode ser negativa."
            );
        }
        return taxa;
    }

    public record CustomPaymentMethodInput(
            String nome,
            String tipo,
            BigDecimal taxa,
            Boolean ativo
    ) {
    }

    public static class CustomPaymentMethod {

        private String id;
        private String nome;
        private String tipo;
        private BigDecimal taxa;
        private boolean ativo;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(final String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(final String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getTaxa() {
            return taxa;
        }

        public void setTaxa(final BigDecimal taxa) {
            this.taxa = taxa;
        }

        public boolean isAtivo() {
            return ativo;
        }

        public void setAtivo(final boolean ativo) {
            this.ativo = ativo;
        }
    }
}
