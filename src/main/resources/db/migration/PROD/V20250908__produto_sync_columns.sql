-- V20250908__produto_sync_columns.sql
-- Adiciona legacy_id, hash_legado e status_sync se ainda não existirem (idempotente em MySQL 5.7+)

-- legacy_id
SET @sql := IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'legacy_id'),
  'SELECT 1',
  'ALTER TABLE produto ADD COLUMN legacy_id BIGINT NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- hash_legado
SET @sql := IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'hash_legado'),
  'SELECT 1',
  'ALTER TABLE produto ADD COLUMN hash_legado VARCHAR(64) NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- status_sync
SET @sql := IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'status_sync'),
  'SELECT 1',
  'ALTER TABLE produto ADD COLUMN status_sync VARCHAR(20) NOT NULL DEFAULT ''PENDENTE'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- (opcionais) índices úteis
SET @sql := IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND INDEX_NAME = 'idx_produto_legacy_id'),
  'SELECT 1',
  'CREATE INDEX idx_produto_legacy_id ON produto(legacy_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND INDEX_NAME = 'idx_produto_codigo_barras'),
  'SELECT 1',
  'CREATE INDEX idx_produto_codigo_barras ON produto(codigo_barras)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
