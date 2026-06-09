-- V20251005_01__movimento_estoque_add_index.sql
-- Índice composto (ex.: por tipo + criado_em) só se não existir
SET @tbl_exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movimento_estoque'
);

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'movimento_estoque'
    AND INDEX_NAME = 'idx_movestoque_tipo_data'
);

SET @sql := IF(@tbl_exists = 1 AND @idx_exists = 0,
  'CREATE INDEX idx_movestoque_tipo_data ON movimento_estoque (tipo, criado_em);',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
