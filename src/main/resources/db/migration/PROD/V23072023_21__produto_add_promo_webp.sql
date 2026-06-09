-- V23072023_21__produto_add_promo_webp.sql
-- Torna a migration idempotente para ambientes que já aplicaram manualmente a coluna/índice

-- 1) Adiciona coluna se não existir
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name   = 'produto'
    AND column_name  = 'promo_webp'
);

SET @sql := IF(@col_exists = 0,
  'ALTER TABLE produto ADD COLUMN promo_webp TINYINT(1) NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Garante DEFAULT/NOT NULL mesmo se a coluna já existia sem o default correto
-- (só executa se a coluna existir e o default não for 0)
SET @needs_default := (
  SELECT CASE
           WHEN COUNT(*) = 0 THEN 0
           WHEN (SELECT COLUMN_DEFAULT FROM information_schema.columns
                 WHERE table_schema = DATABASE()
                   AND table_name   = 'produto'
                   AND column_name  = 'promo_webp') <> '0' THEN 1
           ELSE 0
         END
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name   = 'produto'
    AND column_name  = 'promo_webp'
);

SET @sql := IF(@needs_default = 1,
  'ALTER TABLE produto ALTER COLUMN promo_webp SET DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) Cria índice se não existir (usa INFORMATION_SCHEMA para compatibilidade)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'produto'
    AND index_name   = 'idx_produto_promo_webp'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_produto_promo_webp ON produto (promo_webp)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
