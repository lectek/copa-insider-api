package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.application.view.ProductCategorySectionVM;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductCategorySectionService {

    public static final String SETTING_KEY = "cliente.catalogo.category_sections";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProductCategorySectionService.class);
    private static final String SETTING_DESCRIPTION =
            "Secoes publicas de produtos agrupadas por categoria";
    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = 24;
    private static final Sort SECTION_SORT =
            Sort.by(Sort.Direction.DESC, "dataCadastro")
                    .and(Sort.by(Sort.Direction.DESC, "id"));

    private final ProdutoRepository produtoRepository;
    private final ProdutoCategoriaRepository produtoCategoriaRepository;
    private final AppSettingService settings;
    private final ObjectMapper objectMapper;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public ProductCategorySectionService(
            final ProdutoRepository produtoRepositoryValue,
            final ProdutoCategoriaRepository produtoCategoriaRepositoryValue,
            final AppSettingService appSettingService,
            final ObjectMapper objectMapperValue
    ) {
        this.produtoRepository = produtoRepositoryValue;
        this.produtoCategoriaRepository = produtoCategoriaRepositoryValue;
        this.settings = appSettingService;
        this.objectMapper = objectMapperValue;
    }

    public List<ProductCategorySectionVM> loadPublicSections(
            final Integer requestedLimit
    ) {
        final int defaultLimit = sanitizeLimit(requestedLimit);
        final Long tenantId = resolveTenantId();
        final List<CategorySectionConfig> configuredSections =
                parseConfiguredSections(
                        settings.getOrDefault(SETTING_KEY, "")
                );

        if (configuredSections.isEmpty()) {
            return buildAutomaticSections(tenantId, defaultLimit);
        }

        return configuredSections.stream()
                .map(section -> buildSection(tenantId, section, defaultLimit))
                .filter(section -> !section.produtos().isEmpty())
                .toList();
    }

    public List<CategorySectionConfig> loadEditorConfigs() {
        final String raw = settings.getOrDefault(SETTING_KEY, "");
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            return parseAndSanitize(raw);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Configuracao de secoes por categoria invalida. Ignorando editor estruturado.",
                    ex
            );
            return List.of();
        }
    }

    public List<String> loadAvailableCategories() {
        return produtoCategoriaRepository.findAllNomes().stream()
                .map(this::normalizeValue)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    public String loadEditorJson() {
        final String raw = settings.getOrDefault(SETTING_KEY, "");
        if (raw == null || raw.isBlank()) {
            return "";
        }

        try {
            return writePretty(parseAndSanitize(raw));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Configuracao de secoes por categoria invalida. Exibindo valor bruto para correcao.",
                    ex
            );
            return raw.trim();
        }
    }

    public void saveEditorJson(final String rawJson) {
        settings.upsert(
                SETTING_KEY,
                normalizeEditorJson(rawJson),
                SETTING_DESCRIPTION
        );
    }

    public void saveEditorConfigs(final List<CategorySectionConfig> requestedSections) {
        final List<CategorySectionConfig> sanitized =
                sanitizeConfiguredSections(requestedSections);
        settings.upsert(
                SETTING_KEY,
                sanitized.isEmpty() ? "" : writePretty(sanitized),
                SETTING_DESCRIPTION
        );
    }

    public String normalizeEditorJson(final String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return "";
        }
        final List<CategorySectionConfig> parsed = parseAndSanitize(rawJson);
        return parsed.isEmpty() ? "" : writePretty(parsed);
    }

    private List<ProductCategorySectionVM> buildAutomaticSections(
            final Long tenantId,
            final int defaultLimit
    ) {
        return produtoCategoriaRepository.findAllNomes().stream()
                .map(this::normalizeValue)
                .filter(value -> !value.isBlank())
                .map(categoria -> new CategorySectionConfig(
                        slugify(categoria, "categoria"),
                        categoria,
                        List.of(categoria),
                        defaultLimit
                ))
                .map(section -> buildSection(tenantId, section, defaultLimit))
                .filter(section -> !section.produtos().isEmpty())
                .toList();
    }

    private ProductCategorySectionVM buildSection(
            final Long tenantId,
            final CategorySectionConfig section,
            final int defaultLimit
    ) {
        final int limit = sanitizeLimit(
                section.limite() == null ? defaultLimit : section.limite()
        );
        return new ProductCategorySectionVM(
                section.chave(),
                section.titulo(),
                section.categorias(),
                limit,
                loadProducts(tenantId, section.categorias(), limit)
        );
    }

    private List<ProductCardVM> loadProducts(
            final Long tenantId,
            final List<String> categorias,
            final int limit
    ) {
        final LinkedHashMap<Long, ProductCardVM> grouped = new LinkedHashMap<>();
        for (String categoria : categorias) {
            if (grouped.size() >= limit) {
                break;
            }
            final int remaining = limit - grouped.size();
            final List<ProdutoEntity> products = (tenantId == null
                    ? produtoRepository.searchPublicPageByCategoria(
                        null,
                        categoria,
                        PageRequest.of(0, remaining, SECTION_SORT))
                    : produtoRepository.searchPublicPageByCategoria(
                        tenantId,
                        null,
                        categoria,
                        PageRequest.of(0, remaining, SECTION_SORT)))
                    .getContent();
            for (ProdutoEntity product : products) {
                grouped.putIfAbsent(product.getId(), ProductCardVM.of(product));
                if (grouped.size() >= limit) {
                    break;
                }
            }
        }
        return List.copyOf(grouped.values());
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    private List<CategorySectionConfig> parseConfiguredSections(
            final String rawJson
    ) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }

        try {
            return parseAndSanitize(rawJson);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Configuracao de secoes por categoria ignorada por JSON invalido.",
                    ex
            );
            return List.of();
        }
    }

    private List<CategorySectionConfig> parseAndSanitize(final String rawJson) {
        try {
            final List<CategorySectionConfig> requested = objectMapper.readValue(
                    rawJson,
                    new TypeReference<List<CategorySectionConfig>>() { }
            );
            return sanitizeConfiguredSections(requested);
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "JSON invalido para secoes por categoria.",
                    ex
            );
        }
    }

    private List<CategorySectionConfig> sanitizeConfiguredSections(
            final List<CategorySectionConfig> requested
    ) {
        if (requested == null || requested.isEmpty()) {
            return List.of();
        }

        final List<CategorySectionConfig> sanitized = new ArrayList<>();
        int position = 1;
        for (CategorySectionConfig section : requested) {
            final CategorySectionConfig clean =
                    sanitizeSection(section, position);
            if (clean != null) {
                sanitized.add(clean);
            }
            position++;
        }
        return List.copyOf(sanitized);
    }

    private CategorySectionConfig sanitizeSection(
            final CategorySectionConfig section,
            final int position
    ) {
        if (section == null) {
            return null;
        }

        final List<String> categorias = normalizeCategories(section.categorias());
        if (categorias.isEmpty()) {
            return null;
        }

        final String titulo = resolveTitle(section.titulo(), categorias);
        final String fallbackKey = "categoria-" + position;
        final String chave = slugify(
                normalizeValue(section.chave()).isBlank()
                        ? titulo
                        : section.chave(),
                fallbackKey
        );
        final Integer limite = sanitizeOptionalLimit(section.limite());

        return new CategorySectionConfig(
                chave,
                titulo,
                categorias,
                limite
        );
    }

    private List<String> normalizeCategories(final List<String> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }

        final LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        for (String categoria : categorias) {
            final String normalized = normalizeValue(categoria);
            if (!normalized.isBlank()) {
                unique.putIfAbsent(normalized.toLowerCase(Locale.ROOT), normalized);
            }
        }
        return List.copyOf(unique.values());
    }

    private String resolveTitle(
            final String requestedTitle,
            final List<String> categorias
    ) {
        final String normalized = normalizeValue(requestedTitle);
        if (!normalized.isBlank()) {
            return normalized;
        }
        if (categorias.size() == 1) {
            return categorias.getFirst();
        }
        return String.join(" / ", categorias);
    }

    private Integer sanitizeOptionalLimit(final Integer limit) {
        if (limit == null) {
            return null;
        }
        if (limit <= 0) {
            return null;
        }
        return sanitizeLimit(limit);
    }

    private int sanitizeLimit(final Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private String writePretty(final List<CategorySectionConfig> sections) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sections);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Falha ao serializar configuracao de secoes por categoria.",
                    ex
            );
        }
    }

    private String normalizeValue(final String value) {
        return value == null ? "" : value.trim();
    }

    private static String slugify(
            final String value,
            final String fallback
    ) {
        final String normalized = Normalizer.normalize(
                value == null ? "" : value.trim(),
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}+", "");
        final String slug = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? fallback : slug;
    }

    public record CategorySectionConfig(
            String chave,
            String titulo,
            List<String> categorias,
            Integer limite
    ) { }
}
