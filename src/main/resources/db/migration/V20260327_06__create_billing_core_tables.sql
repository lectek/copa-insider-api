CREATE TABLE IF NOT EXISTS subscription (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  plan_id BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL,
  monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  current_period_start DATETIME(6) NULL,
  current_period_end DATETIME(6) NULL,
  cancel_at_period_end TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_subscription_tenant (tenant_id),
  KEY idx_subscription_plan (plan_id),
  KEY idx_subscription_status (status),
  CONSTRAINT fk_subscription_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id),
  CONSTRAINT fk_subscription_plan
    FOREIGN KEY (plan_id) REFERENCES plan_catalog(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  subscription_id BIGINT NULL,
  period_start DATETIME(6) NULL,
  period_end DATETIME(6) NULL,
  status VARCHAR(24) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  due_at DATETIME(6) NULL,
  paid_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_invoice_tenant_created (tenant_id, created_at),
  KEY idx_invoice_status (status),
  KEY idx_invoice_subscription (subscription_id),
  CONSTRAINT fk_invoice_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id),
  CONSTRAINT fk_invoice_subscription
    FOREIGN KEY (subscription_id) REFERENCES subscription(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  invoice_id BIGINT NOT NULL,
  source_type VARCHAR(24) NOT NULL,
  source_ref VARCHAR(64) NULL,
  description VARCHAR(255) NULL,
  amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_invoice_item_invoice (invoice_id),
  KEY idx_invoice_item_source_type (source_type),
  CONSTRAINT fk_invoice_item_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoice(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS commission_ledger (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  reference_type VARCHAR(32) NOT NULL,
  reference_id VARCHAR(64) NOT NULL,
  gross_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  rate_percent DECIMAL(5,2) NOT NULL DEFAULT 1.00,
  commission_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  occurred_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_commission_ref (tenant_id, reference_type, reference_id),
  KEY idx_commission_tenant_occurred (tenant_id, occurred_at),
  KEY idx_commission_occurred (occurred_at),
  CONSTRAINT fk_commission_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ad_revenue_ledger (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  reference_type VARCHAR(32) NOT NULL,
  reference_id VARCHAR(64) NOT NULL,
  impressions BIGINT NULL,
  clicks BIGINT NULL,
  revenue_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  occurred_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ad_revenue_ref (tenant_id, reference_type, reference_id),
  KEY idx_ad_revenue_tenant_occurred (tenant_id, occurred_at),
  KEY idx_ad_revenue_occurred (occurred_at),
  CONSTRAINT fk_ad_revenue_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
