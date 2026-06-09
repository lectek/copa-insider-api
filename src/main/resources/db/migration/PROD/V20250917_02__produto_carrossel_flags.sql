-- Adiciona campos do carrossel (idempotente, mesmo estilo dos teus scripts)

-- destaque_carrossel (TINYINT(1) -> Boolean)
SET @x := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'destaque_carrossel'
);
SET @sql := IF(@x=0,
  'ALTER TABLE produto ADD COLUMN destaque_carrossel TINYINT(1) NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ordem_carrossel
SET @x := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'ordem_carrossel'
);
SET @sql := IF(@x=0,
  'ALTER TABLE produto ADD COLUMN ordem_carrossel INT NULL',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- publicado_em
SET @x := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'publicado_em'
);
SET @sql := IF(@x=0,
  'ALTER TABLE produto ADD COLUMN publicado_em DATETIME NULL',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- despublicado_em
SET @x := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'despublicado_em'
);
SET @sql := IF(@x=0,
  'ALTER TABLE produto ADD COLUMN despublicado_em DATETIME NULL',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- índice composto para vitrine/carrossel
SET @has_idx := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND INDEX_NAME = 'idx_produto_carrossel'
);
SET @sql := IF(@has_idx=0,
  'CREATE INDEX idx_produto_carrossel ON produto (disponivel, destaque_carrossel, ordem_carrossel, data_cadastro)',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
