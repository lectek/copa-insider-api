package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class HomeCarouselConfigService {

    public static final String DEFAULT_STYLE_KEY =
            "cliente.home.carousel.default_style";

    private static final String STYLE_KEY_PREFIX = "cliente.home.carousel.";
    private static final String STYLE_KEY_SUFFIX = ".style";
    private static final String FALLBACK_STYLE = "classic";

    private static final List<CarouselDefinition> DEFINITIONS = List.of(
            new CarouselDefinition(
                    "destaque",
                    "Destaques da semana",
                    "Carrossel principal com ofertas e lancamentos."
            ),
            new CarouselDefinition(
                    "paraVoce",
                    "Para voce",
                    "Sugestoes personalizadas logo abaixo do hero."
            ),
            new CarouselDefinition(
                    "novidades",
                    "Novidades",
                    "Produtos recem-chegados com mais espaco para imagem."
            ),
            new CarouselDefinition(
                    "categorySections",
                    "Vitrines por categoria",
                    "Estilo padrao das vitrines de categoria exibidas na home."
            ),
            new CarouselDefinition(
                    "maisVendidos",
                    "Mais vendidos",
                    "Itens com maior giro e prova social na home."
            )
    );

    private static final List<StyleOption> STYLE_OPTIONS = List.of(
            new StyleOption(
                    "classic",
                    "Card classico",
                    "Layout equilibrado com imagem, preco e CTA destacados."
            ),
            new StyleOption(
                    "horizontal",
                    "Card horizontal",
                    "Imagem lateral maior e informacoes lado a lado."
            ),
            new StyleOption(
                    "minimal",
                    "Card minimalista",
                    "Visual limpo com foco em titulo, preco e leitura rapida."
            ),
            new StyleOption(
                    "spotlight",
                    "Card com destaque visual",
                    "Bloco mais chamativo para campanhas e vitrines principais."
            ),
            new StyleOption(
                    "image-xl",
                    "Card com imagem ampliada",
                    "Da prioridade a embalagem e reduz o peso visual do texto."
            ),
            new StyleOption(
                    "compact",
                    "Card compacto",
                    "Versao mais enxuta para areas com menos altura disponivel."
            )
    );

    private final AppSettingService settings;

    public HomeCarouselConfigService(final AppSettingService appSettingService) {
        this.settings = appSettingService;
    }

    public List<CarouselDefinition> definitions() {
        return DEFINITIONS;
    }

    public List<StyleOption> styleOptions() {
        return STYLE_OPTIONS;
    }

    public HomeCarouselConfig load() {
        final Map<String, String> persisted = settings.getAllByKeys(settingKeys());
        final String defaultStyle = sanitizeStyle(
                persisted.get(DEFAULT_STYLE_KEY)
        );

        final Map<String, String> selectedStyles = new LinkedHashMap<>();
        final Map<String, String> resolvedStyles = new LinkedHashMap<>();

        for (CarouselDefinition definition : DEFINITIONS) {
            final String overrideValue = sanitizeOptionalStyle(
                    persisted.get(styleKey(definition.key()))
            );
            selectedStyles.put(
                    definition.key(),
                    overrideValue == null ? "" : overrideValue
            );
            resolvedStyles.put(
                    definition.key(),
                    overrideValue == null ? defaultStyle : overrideValue
            );
        }

        return new HomeCarouselConfig(
                defaultStyle,
                Map.copyOf(selectedStyles),
                Map.copyOf(resolvedStyles)
        );
    }

    public Map<String, String> resolveStyles() {
        return load().resolvedStyles();
    }

    public void save(
            final String defaultStyleValue,
            final Map<String, String> requestedStyles
    ) {
        final String defaultStyle = sanitizeStyle(defaultStyleValue);
        settings.upsert(
                DEFAULT_STYLE_KEY,
                defaultStyle,
                "Estilo padrao dos carrosseis da home"
        );

        final Map<String, String> safeRequested = requestedStyles == null
                ? Map.of()
                : requestedStyles;

        for (CarouselDefinition definition : DEFINITIONS) {
            final String overrideValue = sanitizeOptionalStyle(
                    safeRequested.get(definition.key())
            );
            settings.upsert(
                    styleKey(definition.key()),
                    overrideValue == null ? "" : overrideValue,
                    "Estilo do carrossel " + definition.label()
            );
        }
    }

    public Collection<String> settingKeys() {
        final LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put(DEFAULT_STYLE_KEY, DEFAULT_STYLE_KEY);
        for (CarouselDefinition definition : DEFINITIONS) {
            final String key = styleKey(definition.key());
            keys.put(key, key);
        }
        return Set.copyOf(keys.keySet());
    }

    public String sanitizeStyle(final String value) {
        final String normalized = normalize(value);
        if (normalized.isBlank()) {
            return FALLBACK_STYLE;
        }
        for (StyleOption option : STYLE_OPTIONS) {
            if (option.key().equals(normalized)) {
                return normalized;
            }
        }
        return FALLBACK_STYLE;
    }

    private String sanitizeOptionalStyle(final String value) {
        final String normalized = normalize(value);
        if (normalized.isBlank()) {
            return null;
        }
        for (StyleOption option : STYLE_OPTIONS) {
            if (option.key().equals(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private static String normalize(final String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String styleKey(final String carouselKey) {
        return STYLE_KEY_PREFIX + carouselKey + STYLE_KEY_SUFFIX;
    }

    public record CarouselDefinition(
            String key,
            String label,
            String description
    ) { }

    public record StyleOption(
            String key,
            String label,
            String description
    ) { }

    public record HomeCarouselConfig(
            String defaultStyle,
            Map<String, String> selectedStyles,
            Map<String, String> resolvedStyles
    ) { }
}
