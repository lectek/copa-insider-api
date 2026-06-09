package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.fiscal.FiscalEnvironment;
import br.com.redemaisfarma.domain.fiscal.FiscalProvider;
import br.com.redemaisfarma.domain.fiscal.FiscalTaxRegime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fiscal_emitter_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fiscal_emitter_provider",
                        columnNames = "provider"
                )
        }
)
public class FiscalEmitterConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FiscalProvider provider;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;

    @Column(name = "company_legal_name", length = 160)
    private String companyLegalName;

    @Column(name = "company_trade_name", length = 160)
    private String companyTradeName;

    @Column(length = 14)
    private String cnpj;

    @Column(name = "state_registration", length = 32)
    private String stateRegistration;

    @Column(name = "municipal_registration", length = 32)
    private String municipalRegistration;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", length = 40)
    private FiscalTaxRegime taxRegime;

    @Column(name = "api_base_url", length = 255)
    private String apiBaseUrl;

    @Column(name = "webhook_url", length = 255)
    private String webhookUrl;

    @Column(name = "api_token", length = 255)
    private String apiToken;

    @Column(name = "csc_id", length = 64)
    private String cscId;

    @Column(name = "csc", length = 255)
    private String csc;

    @Lob
    @Column(name = "certificate_base64")
    private String certificateBase64;

    @Column(name = "certificate_password", length = 255)
    private String certificatePassword;

    @Column(name = "nfe_series", nullable = false)
    private Integer nfeSeries;

    @Column(name = "nfce_series", nullable = false)
    private Integer nfceSeries;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalize();
        if (environment == null) {
            environment = FiscalEnvironment.HOMOLOGACAO;
        }
        if (provider == null) {
            provider = FiscalProvider.FOCUS_NFE;
        }
        if (nfeSeries == null || nfeSeries < 1) {
            nfeSeries = 1;
        }
        if (nfceSeries == null || nfceSeries < 1) {
            nfceSeries = 1;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        companyLegalName = trim(companyLegalName);
        companyTradeName = trim(companyTradeName);
        cnpj = digits(cnpj);
        stateRegistration = trim(stateRegistration);
        municipalRegistration = trim(municipalRegistration);
        apiBaseUrl = trim(apiBaseUrl);
        webhookUrl = trim(webhookUrl);
        apiToken = trim(apiToken);
        cscId = trim(cscId);
        csc = trim(csc);
        certificateBase64 = trim(certificateBase64);
        certificatePassword = trim(certificatePassword);
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private static String digits(final String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long idValue) {
        id = idValue;
    }

    public FiscalProvider getProvider() {
        return provider;
    }

    public void setProvider(final FiscalProvider providerValue) {
        provider = providerValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabledValue) {
        enabled = enabledValue;
    }

    public FiscalEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final FiscalEnvironment environmentValue) {
        environment = environmentValue;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(final String companyLegalNameValue) {
        companyLegalName = companyLegalNameValue;
    }

    public String getCompanyTradeName() {
        return companyTradeName;
    }

    public void setCompanyTradeName(final String companyTradeNameValue) {
        companyTradeName = companyTradeNameValue;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(final String cnpjValue) {
        cnpj = cnpjValue;
    }

    public String getStateRegistration() {
        return stateRegistration;
    }

    public void setStateRegistration(final String stateRegistrationValue) {
        stateRegistration = stateRegistrationValue;
    }

    public String getMunicipalRegistration() {
        return municipalRegistration;
    }

    public void setMunicipalRegistration(final String municipalRegistrationValue) {
        municipalRegistration = municipalRegistrationValue;
    }

    public FiscalTaxRegime getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(final FiscalTaxRegime taxRegimeValue) {
        taxRegime = taxRegimeValue;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(final String apiBaseUrlValue) {
        apiBaseUrl = apiBaseUrlValue;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(final String webhookUrlValue) {
        webhookUrl = webhookUrlValue;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(final String apiTokenValue) {
        apiToken = apiTokenValue;
    }

    public String getCscId() {
        return cscId;
    }

    public void setCscId(final String cscIdValue) {
        cscId = cscIdValue;
    }

    public String getCsc() {
        return csc;
    }

    public void setCsc(final String cscValue) {
        csc = cscValue;
    }

    public String getCertificateBase64() {
        return certificateBase64;
    }

    public void setCertificateBase64(final String certificateBase64Value) {
        certificateBase64 = certificateBase64Value;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(final String certificatePasswordValue) {
        certificatePassword = certificatePasswordValue;
    }

    public Integer getNfeSeries() {
        return nfeSeries;
    }

    public void setNfeSeries(final Integer nfeSeriesValue) {
        nfeSeries = nfeSeriesValue;
    }

    public Integer getNfceSeries() {
        return nfceSeries;
    }

    public void setNfceSeries(final Integer nfceSeriesValue) {
        nfceSeries = nfceSeriesValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
