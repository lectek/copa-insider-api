/* V3 — adiciona colunas de auditoria e versionamento em pedido */

ALTER TABLE pedido
  ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN version     BIGINT   NOT NULL DEFAULT 0;

/* Índices úteis */
CREATE INDEX idx_pedido_created_at ON pedido (created_at);
