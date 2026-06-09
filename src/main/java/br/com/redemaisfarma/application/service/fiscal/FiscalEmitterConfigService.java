package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.FiscalEmitterConfigRepository;
import br.com.redemaisfarma.domain.fiscal.FiscalEnvironment;
import br.com.redemaisfarma.domain.fiscal.FiscalProvider;
import br.com.redemaisfarma.domain.fiscal.FiscalTaxRegime;
import java.net.URI;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalEmitterConfigService {

    private static final int DEFAULT_SERIES = 1;
    private static final int MIN_SERIES = 1;
    private static final int MAX_SERIES = 999;

    private final FiscalEmitterConfigRepository repository;

    public FiscalEmitterConfigService(
            final FiscalEmitterConfigRepository fiscalEmitterConfigRepository
    ) {
        this.repository = fiscalEmitterConfigRepository;
    }

    @Transactional(readOnly = true)
    public FiscalEmitterConfig load(final FiscalProvider provider) {
        return repository.findByProvider(requireProvider(provider))
                .map(this::toView)
                .orElseGet(() -> defaultConfig(provider));
    }

    @Transactional(readOnly = true)
    public FiscalEmitterConfigEntity requireEnabledEntity(
            final FiscalProvider provider
    ) {
        final FiscalEmitterConfigEntity entity = repository.findByProvider(
                requireProvider(provider)
        ).orElseThrow(() -> new NoSuchElementException(
                "Configuracao fiscal nao encontrada para o provedor."
        ));
        if (!entity.isEnabled()) {
            throw new IllegalStateException(
                    "Configuracao fiscal desativada para o provedor."
            );
        }
        return entity;
    }

    @Transactional
    public FiscalEmitterConfig save(
            final FiscalProvider provider,
            final FiscalEmitterConfigInput input
    ) {
        if (input == null) {
            throw new IllegalArgumentException(
                    "Configuracao fiscal obrigatoria."
            );
        }
        final FiscalProvider normalizedProvider = requireProvider(provider);
        final FiscalEmitterConfigEntity entity = repository.findByProvider(
                normalizedProvider
        ).orElseGet(() -> {
            final FiscalEmitterConfigEntity created =
                    new FiscalEmitterConfigEntity();
            created.setProvider(normalizedProvider);
            return created;
        });

        merge(entity, input);
        validate(entity);
        return toView(repository.save(entity));
    }

    private void merge(
            final FiscalEmitterConfigEntity entity,
            final FiscalEmitterConfigInput input
    ) {
        entity.setEnabled(Boolean.TRUE.equals(input.enabled()));
        entity.setEnvironment(input.environment() == null
                ? entity.getEnvironment() == null
                ? FiscalEnvironment.HOMOLOGACAO
                : entity.getEnvironment()
                : input.environment());
        entity.setCompanyLegalName(nullIfBlank(input.companyLegalName()));
        entity.setCompanyTradeName(nullIfBlank(input.companyTradeName()));
        entity.setCnpj(digits(input.cnpj()));
        entity.setStateRegistration(nullIfBlank(input.stateRegistration()));
        entity.setMunicipalRegistration(
                nullIfBlank(input.municipalRegistration())
        );
        entity.setTaxRegime(input.taxRegime());
        entity.setApiBaseUrl(
                normalizeApiBaseUrl(input.apiBaseUrl(), entity.getEnvironment())
        );
        entity.setWebhookUrl(nullIfBlank(input.webhookUrl()));
        entity.setNfeSeries(normalizeSeries(
                input.nfeSeries(),
                entity.getNfeSeries()
        ));
        entity.setNfceSeries(normalizeSeries(
                input.nfceSeries(),
                entity.getNfceSeries()
        ));
        entity.setCscId(nullIfBlank(input.cscId()));

        if (Boolean.TRUE.equals(input.clearApiToken())) {
            entity.setApiToken(null);
        } else if (!isBlank(input.apiToken())) {
            entity.setApiToken(input.apiToken().trim());
        }

        if (Boolean.TRUE.equals(input.clearCsc())) {
            entity.setCsc(null);
        } else if (!isBlank(input.csc())) {
            entity.setCsc(input.csc().trim());
        }

        if (Boolean.TRUE.equals(input.clearCertificate())) {
            entity.setCertificateBase64(null);
            entity.setCertificatePassword(null);
        } else {
            if (!isBlank(input.certificateBase64())) {
                entity.setCertificateBase64(input.certificateBase64().trim());
            }
            if (!isBlank(input.certificatePassword())) {
                entity.setCertificatePassword(
                        input.certificatePassword().trim()
                );
            }
        }
    }

    private void validate(final FiscalEmitterConfigEntity entity) {
        if (entity.getEnvironment() == null) {
            throw new IllegalArgumentException("Ambiente fiscal obrigatorio.");
        }
        validateSeries(entity.getNfeSeries(), "Serie NF-e");
        validateSeries(entity.getNfceSeries(), "Serie NFC-e");
        validateOptionalUrl(entity.getApiBaseUrl(), "Base URL da API fiscal");
        validateOptionalUrl(entity.getWebhookUrl(), "Webhook fiscal");
        if (entity.getCnpj() != null && !entity.getCnpj().isBlank()
                && entity.getCnpj().length() != 14) {
            throw new IllegalArgumentException("CNPJ do emitente invalido.");
        }

        if (!entity.isEnabled()) {
            return;
        }

        requireNotBlank(entity.getCompanyLegalName(), "Razao social obrigatoria.");
        requireNotBlank(entity.getCompanyTradeName(), "Nome fantasia obrigatorio.");
        requireNotBlank(entity.getCnpj(), "CNPJ do emitente obrigatorio.");
        requireNotBlank(
                entity.getStateRegistration(),
                "Inscricao estadual obrigatoria."
        );
        if (entity.getTaxRegime() == null) {
            throw new IllegalArgumentException("Regime tributario obrigatorio.");
        }
        requireNotBlank(entity.getApiToken(), "Token da API fiscal obrigatorio.");
        requireNotBlank(entity.getCscId(), "Identificador CSC obrigatorio.");
        requireNotBlank(entity.getCsc(), "CSC obrigatorio.");
    }

    private FiscalEmitterConfig toView(final FiscalEmitterConfigEntity entity) {
        return new FiscalEmitterConfig(
                entity.getProvider(),
                entity.isEnabled(),
                entity.getEnvironment(),
                blankToEmpty(entity.getCompanyLegalName()),
                blankToEmpty(entity.getCompanyTradeName()),
                blankToEmpty(entity.getCnpj()),
                blankToEmpty(entity.getStateRegistration()),
                blankToEmpty(entity.getMunicipalRegistration()),
                entity.getTaxRegime(),
                blankToEmpty(entity.getApiBaseUrl()),
                blankToEmpty(entity.getWebhookUrl()),
                entity.getNfeSeries() == null ? DEFAULT_SERIES : entity.getNfeSeries(),
                entity.getNfceSeries() == null ? DEFAULT_SERIES : entity.getNfceSeries(),
                !isBlank(entity.getApiToken()),
                blankToEmpty(entity.getCscId()),
                !isBlank(entity.getCsc()),
                !isBlank(entity.getCertificateBase64())
        );
    }

    private FiscalEmitterConfig defaultConfig(final FiscalProvider provider) {
        return new FiscalEmitterConfig(
                requireProvider(provider),
                false,
                FiscalEnvironment.HOMOLOGACAO,
                "",
                "",
                "",
                "",
                "",
                null,
                defaultApiBaseUrl(FiscalEnvironment.HOMOLOGACAO),
                "",
                DEFAULT_SERIES,
                DEFAULT_SERIES,
                false,
                "",
                false,
                false
        );
    }

    private FiscalProvider requireProvider(final FiscalProvider provider) {
        if (provider == null) {
            return FiscalProvider.FOCUS_NFE;
        }
        return provider;
    }

    private String normalizeApiBaseUrl(
            final String apiBaseUrl,
            final FiscalEnvironment environment
    ) {
        final String normalized = nullIfBlank(apiBaseUrl);
        if (normalized == null) {
            return defaultApiBaseUrl(environment);
        }
        return normalized;
    }

    private String defaultApiBaseUrl(final FiscalEnvironment environment) {
        if (environment == FiscalEnvironment.PRODUCAO) {
            return "https://api.focusnfe.com.br";
        }
        return "https://homologacao.focusnfe.com.br";
    }

    private Integer normalizeSeries(
            final Integer input,
            final Integer currentValue
    ) {
        if (input != null) {
            return input;
        }
        if (currentValue != null && currentValue >= MIN_SERIES) {
            return currentValue;
        }
        return DEFAULT_SERIES;
    }

    private void validateSeries(final Integer value, final String label) {
        if (value == null || value < MIN_SERIES || value > MAX_SERIES) {
            throw new IllegalArgumentException(
                    label + " deve estar entre 1 e 999."
            );
        }
    }

    private void validateOptionalUrl(
            final String rawUrl,
            final String label
    ) {
        if (isBlank(rawUrl)) {
            return;
        }
        try {
            final URI uri = URI.create(rawUrl);
            final String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException(label + " sem esquema.");
            }
            final String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme)
                    && !"https".equals(normalizedScheme)) {
                throw new IllegalArgumentException(
                        label + " deve usar http ou https."
                );
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(label + " sem host valido.");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " invalido.");
        }
    }

    private void requireNotBlank(final String value, final String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private static String nullIfBlank(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String blankToEmpty(final String value) {
        return value == null ? "" : value;
    }

    private static String digits(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.replaceAll("\\D", "");
        return normalized.isBlank() ? null : normalized;
    }

    public record FiscalEmitterConfig(
            FiscalProvider provider,
            boolean enabled,
            FiscalEnvironment environment,
            String companyLegalName,
            String companyTradeName,
            String cnpj,
            String stateRegistration,
            String municipalRegistration,
            FiscalTaxRegime taxRegime,
            String apiBaseUrl,
            String webhookUrl,
            Integer nfeSeries,
            Integer nfceSeries,
            boolean apiTokenConfigured,
            String cscId,
            boolean cscConfigured,
            boolean certificateConfigured
    ) {
    }

    public record FiscalEmitterConfigInput(
            Boolean enabled,
            FiscalEnvironment environment,
            String companyLegalName,
            String companyTradeName,
            String cnpj,
            String stateRegistration,
            String municipalRegistration,
            FiscalTaxRegime taxRegime,
            String apiBaseUrl,
            String webhookUrl,
            Integer nfeSeries,
            Integer nfceSeries,
            String apiToken,
            Boolean clearApiToken,
            String cscId,
            String csc,
            Boolean clearCsc,
            String certificateBase64,
            String certificatePassword,
            Boolean clearCertificate
    ) {
    }
}
