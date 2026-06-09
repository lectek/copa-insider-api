SET @db := DATABASE();

CREATE TABLE IF NOT EXISTS tenant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(64) NOT NULL,
  nome VARCHAR(255) NOT NULL,
  ativo TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id VARCHAR(128) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  payload_json JSON NOT NULL,
  dedupe_key VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  attempts INT NOT NULL DEFAULT 0,
  next_attempt_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  processed_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_outbox_event_tenant_dedupe (tenant_id, dedupe_key),
  KEY idx_outbox_event_status_next_attempt (status, next_attempt_at),
  KEY idx_outbox_event_tenant_created_at (tenant_id, created_at),
  CONSTRAINT fk_outbox_event_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sync_checkpoint_tenant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  origem VARCHAR(64) NOT NULL,
  cursor_value VARCHAR(255) NOT NULL,
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sync_checkpoint_tenant_origem (tenant_id, origem),
  CONSTRAINT fk_sync_checkpoint_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS import_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  origem VARCHAR(32) NOT NULL,
  arquivo_hash VARCHAR(128) NULL,
  arquivo_nome VARCHAR(255) NULL,
  lidos INT NOT NULL DEFAULT 0,
  inseridos INT NOT NULL DEFAULT 0,
  atualizados INT NOT NULL DEFAULT 0,
  ignorados INT NOT NULL DEFAULT 0,
  erros INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL,
  executado_por VARCHAR(128) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_import_history_tenant_created_at (tenant_id, created_at),
  KEY idx_import_history_tenant_status (tenant_id, status),
  CONSTRAINT fk_import_history_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tenant (codigo, nome, ativo)
SELECT 'default', 'Tenant Padrao', 1
WHERE NOT EXISTS (SELECT 1 FROM tenant WHERE codigo = 'default');

