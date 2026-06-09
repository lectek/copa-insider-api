CREATE TABLE IF NOT EXISTS plan_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(120) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_catalog_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plan_feature (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  feature_key VARCHAR(64) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_feature_plan_key (plan_id, feature_key),
  KEY idx_plan_feature_key (feature_key),
  CONSTRAINT fk_plan_feature_plan
    FOREIGN KEY (plan_id) REFERENCES plan_catalog(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS addon_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(120) NOT NULL,
  feature_key VARCHAR(64) NOT NULL,
  monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_addon_catalog_code (code),
  KEY idx_addon_catalog_feature_key (feature_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_plan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  plan_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_plan_tenant (tenant_id),
  KEY idx_tenant_plan_plan (plan_id),
  CONSTRAINT fk_tenant_plan_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id),
  CONSTRAINT fk_tenant_plan_plan
    FOREIGN KEY (plan_id) REFERENCES plan_catalog(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_addon_subscription (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  addon_id BIGINT NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  started_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ended_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_addon_active (tenant_id, addon_id, active),
  KEY idx_tenant_addon_tenant_active (tenant_id, active),
  CONSTRAINT fk_tenant_addon_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id),
  CONSTRAINT fk_tenant_addon_addon
    FOREIGN KEY (addon_id) REFERENCES addon_catalog(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO plan_catalog (code, name, active)
SELECT 'FREE', 'Plano Free', 1
WHERE NOT EXISTS (SELECT 1 FROM plan_catalog WHERE code = 'FREE');

INSERT INTO plan_catalog (code, name, active)
SELECT 'START', 'Plano Start', 1
WHERE NOT EXISTS (SELECT 1 FROM plan_catalog WHERE code = 'START');

INSERT INTO plan_catalog (code, name, active)
SELECT 'PLUS', 'Plano Plus', 1
WHERE NOT EXISTS (SELECT 1 FROM plan_catalog WHERE code = 'PLUS');

INSERT INTO plan_catalog (code, name, active)
SELECT 'PREMIUM', 'Plano Premium', 1
WHERE NOT EXISTS (SELECT 1 FROM plan_catalog WHERE code = 'PREMIUM');

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'ads_enabled', 1
FROM plan_catalog p
WHERE p.code IN ('FREE', 'START', 'PLUS')
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'ads_enabled'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'commission_enabled', 1
FROM plan_catalog p
WHERE p.code = 'FREE'
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'commission_enabled'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'delivery_route', 1
FROM plan_catalog p
WHERE p.code IN ('PLUS', 'PREMIUM')
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'delivery_route'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'ai_ops', 1
FROM plan_catalog p
WHERE p.code IN ('PLUS', 'PREMIUM')
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'ai_ops'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'national_catalog', 1
FROM plan_catalog p
WHERE p.code IN ('PLUS', 'PREMIUM')
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'national_catalog'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'pharmacy_module', 1
FROM plan_catalog p
WHERE p.code IN ('PLUS', 'PREMIUM')
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'pharmacy_module'
  );

INSERT INTO plan_feature (plan_id, feature_key, enabled)
SELECT p.id, 'ai_attendant', 1
FROM plan_catalog p
WHERE p.code = 'PREMIUM'
  AND NOT EXISTS (
    SELECT 1
    FROM plan_feature pf
    WHERE pf.plan_id = p.id AND pf.feature_key = 'ai_attendant'
  );

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_DELIVERY_ROUTE', 'Add-on Rota de Entrega', 'delivery_route', 49.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_DELIVERY_ROUTE');

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_AI_OPS', 'Add-on IA Operacional', 'ai_ops', 49.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_AI_OPS');

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_NATIONAL_CATALOG', 'Add-on Catalogo Nacional', 'national_catalog', 49.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_NATIONAL_CATALOG');

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_PHARMACY_MODULE', 'Add-on Modulo Farmacia', 'pharmacy_module', 49.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_PHARMACY_MODULE');

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_NO_ADS', 'Add-on Sem Anuncios', 'no_ads', 49.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_NO_ADS');

INSERT INTO addon_catalog (code, name, feature_key, monthly_price, active)
SELECT 'ADDON_AI_ATTENDANT', 'Add-on Atendente IA', 'ai_attendant', 129.90, 1
WHERE NOT EXISTS (SELECT 1 FROM addon_catalog WHERE code = 'ADDON_AI_ATTENDANT');

INSERT INTO tenant_plan (tenant_id, plan_id)
SELECT t.id, p.id
FROM tenant t
JOIN plan_catalog p ON p.code = 'FREE'
LEFT JOIN tenant_plan tp ON tp.tenant_id = t.id
WHERE tp.id IS NULL;
