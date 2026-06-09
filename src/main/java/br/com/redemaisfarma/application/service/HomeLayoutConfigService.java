package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HomeLayoutConfigService {

    public static final String SETTING_KEY = "cliente.home.layout.config";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HomeLayoutConfigService.class);

    private static final String SETTING_DESCRIPTION =
            "Configuracao estrutural da home do cliente";

    private static final int DEFAULT_QUICK_CATEGORIES_LIMIT = 8;
    private static final int MIN_QUICK_CATEGORIES_LIMIT = 4;
    private static final int MAX_QUICK_CATEGORIES_LIMIT = 12;

    private static final Set<String> ALLOWED_LAYOUTS = Set.of(
            "carousel",
            "grid"
    );

    private static final List<SectionDefinition> SECTION_DEFINITIONS = List.of(
            new SectionDefinition(
                    "destaque",
                    "Destaques da semana",
                    "Bloco principal com produtos priorizados na home.",
                    "carousel",
                    "Destaques da semana",
                    "Ofertas e lancamentos selecionados",
                    10
            ),
            new SectionDefinition(
                    "paraVoce",
                    "Para voce",
                    "Sugestoes que funcionam como vitrine secundaria.",
                    "carousel",
                    "Para voce",
                    "Recomendacoes com base no seu perfil",
                    20
            ),
            new SectionDefinition(
                    "novidades",
                    "Novidades",
                    "Produtos mais recentes para renovar a vitrine.",
                    "carousel",
                    "Novidades",
                    "Acabaram de chegar",
                    30
            ),
            new SectionDefinition(
                    "categorySections",
                    "Vitrines por categoria",
                    "Agrupa secoes automaticas ou configuradas por categoria dentro da home.",
                    "grid",
                    "Explore por categoria",
                    "Descobertas organizadas por familias de produtos",
                    40
            ),
            new SectionDefinition(
                    "maisVendidos",
                    "Mais vendidos",
                    "Itens com maior recorrencia e prova social.",
                    "carousel",
                    "Mais vendidos",
                    "Os queridinhos da nossa comunidade",
                    50
            )
    );

    private static final List<String> DEFAULT_TRUST_ITEMS = List.of(
            "Entrega local",
            "Retirada na loja",
            "Compra segura",
            "Atendimento farmaceutico"
    );

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public HomeLayoutConfigService(
            final AppSettingService appSettingService,
            final ObjectMapper objectMapperValue
    ) {
        this.settings = appSettingService;
        this.objectMapper = objectMapperValue;
    }

    public List<SectionDefinition> definitions() {
        return SECTION_DEFINITIONS;
    }

    public HomeLayoutConfig load() {
        final String raw = settings.getOrDefault(SETTING_KEY, "");
        if (raw == null || raw.isBlank()) {
            return defaults();
        }

        try {
            final StoredHomeLayoutConfig stored = objectMapper.readValue(
                    raw,
                    StoredHomeLayoutConfig.class
            );
            return mergeWithDefaults(stored);
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Falha ao ler configuracao estrutural da home. Usando padrao.", ex);
            return defaults();
        }
    }

    public void save(final HomeLayoutConfig config) {
        final HomeLayoutConfig sanitized = sanitize(config);
        try {
            settings.upsert(
                    SETTING_KEY,
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(toStored(sanitized)),
                    SETTING_DESCRIPTION
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(
                    "Nao foi possivel serializar a configuracao estrutural da home.",
                    ex
            );
        }
    }

    public HomeLayoutConfig defaults() {
        final List<HomeSectionConfig> sections = new ArrayList<>();
        for (SectionDefinition definition : SECTION_DEFINITIONS) {
            sections.add(new HomeSectionConfig(
                    definition.key(),
                    true,
                    definition.defaultLayout(),
                    definition.defaultOrder(),
                    definition.defaultTitle(),
                    definition.defaultSubtitle(),
                    "Ver todos",
                    "/produtos"
            ));
        }
        return new HomeLayoutConfig(
                true,
                true,
                true,
                List.copyOf(DEFAULT_TRUST_ITEMS),
                true,
                DEFAULT_QUICK_CATEGORIES_LIMIT,
                List.copyOf(sections)
        );
    }

    private HomeLayoutConfig mergeWithDefaults(final StoredHomeLayoutConfig stored) {
        final HomeLayoutConfig base = defaults();
        if (stored == null) {
            return base;
        }

        final Map<String, StoredHomeSectionConfig> storedSections =
                new LinkedHashMap<>();
        if (stored.sections() != null) {
            for (StoredHomeSectionConfig section : stored.sections()) {
                if (section == null || section.key() == null) {
                    continue;
                }
                storedSections.put(normalize(section.key()), section);
            }
        }

        final List<HomeSectionConfig> sections = new ArrayList<>();
        for (SectionDefinition definition : SECTION_DEFINITIONS) {
            final StoredHomeSectionConfig storedSection =
                    storedSections.get(definition.key());
            final HomeSectionConfig fallback = base.sections().stream()
                    .filter(section -> section.key().equals(definition.key()))
                    .findFirst()
                    .orElseThrow();
            sections.add(sanitizeSection(storedSection, definition, fallback));
        }

        return new HomeLayoutConfig(
                stored.heroSearchEnabled() == null
                        ? base.heroSearchEnabled()
                        : stored.heroSearchEnabled(),
                stored.heroPrincipalEnabled() == null
                        ? base.heroPrincipalEnabled()
                        : stored.heroPrincipalEnabled(),
                stored.trustStripEnabled() == null
                        ? base.trustStripEnabled()
                        : stored.trustStripEnabled(),
                sanitizeTrustItems(stored.trustItems()),
                stored.quickCategoriesEnabled() == null
                        ? base.quickCategoriesEnabled()
                        : stored.quickCategoriesEnabled(),
                sanitizeQuickCategoriesLimit(stored.quickCategoriesLimit()),
                List.copyOf(sections)
        );
    }

    private HomeLayoutConfig sanitize(final HomeLayoutConfig config) {
        final HomeLayoutConfig fallback = defaults();
        if (config == null) {
            return fallback;
        }

        final List<HomeSectionConfig> sanitizedSections = new ArrayList<>();
        final Map<String, HomeSectionConfig> provided = new LinkedHashMap<>();
        if (config.sections() != null) {
            for (HomeSectionConfig section : config.sections()) {
                if (section == null || section.key() == null) {
                    continue;
                }
                provided.put(normalize(section.key()), section);
            }
        }

        for (SectionDefinition definition : SECTION_DEFINITIONS) {
            final HomeSectionConfig section = provided.get(definition.key());
            final HomeSectionConfig fallbackSection = fallback.sections().stream()
                    .filter(item -> item.key().equals(definition.key()))
                    .findFirst()
                    .orElseThrow();
            sanitizedSections.add(sanitizeSection(section, definition, fallbackSection));
        }

        return new HomeLayoutConfig(
                config.heroSearchEnabled(),
                config.heroPrincipalEnabled(),
                config.trustStripEnabled(),
                sanitizeTrustItems(config.trustItems()),
                config.quickCategoriesEnabled(),
                sanitizeQuickCategoriesLimit(config.quickCategoriesLimit()),
                List.copyOf(sanitizedSections)
        );
    }

    private HomeSectionConfig sanitizeSection(
            final StoredHomeSectionConfig stored,
            final SectionDefinition definition,
            final HomeSectionConfig fallback
    ) {
        if (stored == null) {
            return fallback;
        }
        return sanitizeSection(
                new HomeSectionConfig(
                        definition.key(),
                        stored.enabled() == null
                                ? fallback.enabled()
                                : stored.enabled(),
                        stored.layout(),
                        stored.order(),
                        stored.title(),
                        stored.subtitle(),
                        stored.ctaLabel(),
                        stored.ctaHref()
                ),
                definition,
                fallback
        );
    }

    private HomeSectionConfig sanitizeSection(
            final HomeSectionConfig section,
            final SectionDefinition definition,
            final HomeSectionConfig fallback
    ) {
        if (section == null) {
            return fallback;
        }
        return new HomeSectionConfig(
                definition.key(),
                section.enabled(),
                sanitizeLayout(section.layout(), fallback.layout()),
                sanitizeOrder(section.order(), fallback.order()),
                sanitizeText(section.title(), definition.defaultTitle()),
                sanitizeText(section.subtitle(), definition.defaultSubtitle()),
                sanitizeText(section.ctaLabel(), fallback.ctaLabel()),
                sanitizeHref(section.ctaHref(), fallback.ctaHref())
        );
    }

    private StoredHomeLayoutConfig toStored(final HomeLayoutConfig config) {
        final List<StoredHomeSectionConfig> sections = config.sections().stream()
                .sorted(Comparator.comparingInt(HomeSectionConfig::order))
                .map(section -> new StoredHomeSectionConfig(
                        section.key(),
                        section.enabled(),
                        section.layout(),
                        section.order(),
                        section.title(),
                        section.subtitle(),
                        section.ctaLabel(),
                        section.ctaHref()
                ))
                .toList();

        return new StoredHomeLayoutConfig(
                config.heroSearchEnabled(),
                config.heroPrincipalEnabled(),
                config.trustStripEnabled(),
                config.trustItems(),
                config.quickCategoriesEnabled(),
                config.quickCategoriesLimit(),
                sections
        );
    }

    private List<String> sanitizeTrustItems(final List<String> items) {
        final List<String> source = items == null || items.isEmpty()
                ? DEFAULT_TRUST_ITEMS
                : items;
        final List<String> sanitized = source.stream()
                .map(this::normalizeText)
                .filter(value -> !value.isBlank())
                .limit(4)
                .toList();
        return sanitized.isEmpty() ? List.copyOf(DEFAULT_TRUST_ITEMS) : List.copyOf(sanitized);
    }

    private int sanitizeQuickCategoriesLimit(final Integer value) {
        if (value == null) {
            return DEFAULT_QUICK_CATEGORIES_LIMIT;
        }
        return Math.max(
                MIN_QUICK_CATEGORIES_LIMIT,
                Math.min(MAX_QUICK_CATEGORIES_LIMIT, value)
        );
    }

    private int sanitizeOrder(final Integer value, final int fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.max(1, Math.min(999, value));
    }

    private String sanitizeLayout(final String value, final String fallback) {
        final String normalized = normalize(value);
        return ALLOWED_LAYOUTS.contains(normalized) ? normalized : fallback;
    }

    private String sanitizeHref(final String value, final String fallback) {
        final String normalized = normalizeText(value);
        if (normalized.isBlank()) {
            return fallback;
        }
        return normalized.startsWith("/") ? normalized : fallback;
    }

    private String sanitizeText(final String value, final String fallback) {
        final String normalized = normalizeText(value);
        if (normalized.isBlank()) {
            return fallback;
        }
        return normalized.length() > 120 ? fallback : normalized;
    }

    private String normalizeText(final String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record SectionDefinition(
            String key,
            String label,
            String description,
            String defaultLayout,
            String defaultTitle,
            String defaultSubtitle,
            int defaultOrder
    ) { }

    public record HomeLayoutConfig(
            boolean heroSearchEnabled,
            boolean heroPrincipalEnabled,
            boolean trustStripEnabled,
            List<String> trustItems,
            boolean quickCategoriesEnabled,
            int quickCategoriesLimit,
            List<HomeSectionConfig> sections
    ) { }

    public record HomeSectionConfig(
            String key,
            boolean enabled,
            String layout,
            Integer order,
            String title,
            String subtitle,
            String ctaLabel,
            String ctaHref
    ) { }

    private record StoredHomeLayoutConfig(
            Boolean heroSearchEnabled,
            Boolean heroPrincipalEnabled,
            Boolean trustStripEnabled,
            List<String> trustItems,
            Boolean quickCategoriesEnabled,
            Integer quickCategoriesLimit,
            List<StoredHomeSectionConfig> sections
    ) { }

    private record StoredHomeSectionConfig(
            String key,
            Boolean enabled,
            String layout,
            Integer order,
            String title,
            String subtitle,
            String ctaLabel,
            String ctaHref
    ) { }
}
