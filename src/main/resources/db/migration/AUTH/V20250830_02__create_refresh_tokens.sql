-- Tabela de refresh tokens (MySQL 8)
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id            BINARY(16)   NOT NULL,
  user_id       BIGINT       NOT NULL,
  tenant_id     VARCHAR(100) NOT NULL,
  token         VARCHAR(512) NOT NULL,
  issued_at     DATETIME(6)  NOT NULL,
  expires_at    DATETIME(6)  NOT NULL,
  revoked_at    DATETIME(6)  NULL,
  ip_address    VARCHAR(64)  NULL,
  user_agent    VARCHAR(512) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Índices (sem IF EXISTS/NOT EXISTS)
CREATE INDEX ix_refresh_token_user_tenant ON refresh_tokens (user_id, tenant_id);
CREATE UNIQUE INDEX uk_refresh_token_token ON refresh_tokens (token);
CREATE INDEX ix_refresh_token_expires_at ON refresh_tokens (expires_at);
