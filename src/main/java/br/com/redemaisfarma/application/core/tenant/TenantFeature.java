package br.com.redemaisfarma.application.core.tenant;

public enum TenantFeature {

    MOD_RECEITA_CONTROLADA("mod.receita_controlada");

    private final String key;

    TenantFeature(final String keyValue) {
        this.key = keyValue;
    }

    public String key() {
        return this.key;
    }
}
