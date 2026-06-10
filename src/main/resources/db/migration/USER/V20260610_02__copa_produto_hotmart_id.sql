-- Adiciona hotmart_product_id à copa_produto se ainda não existir
-- (necessário quando a tabela foi criada por uma versão anterior do V04 sem essa coluna)

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE()
      AND table_name   = 'copa_produto'
      AND column_name  = 'hotmart_product_id'
);
SET @add_col = IF(@col_exists = 0,
    'ALTER TABLE copa_produto ADD COLUMN hotmart_product_id BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @add_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name   = 'copa_produto'
      AND index_name   = 'hotmart_product_id'
);
SET @add_idx = IF(@idx_exists = 0,
    'ALTER TABLE copa_produto ADD UNIQUE INDEX hotmart_product_id (hotmart_product_id)',
    'SELECT 1');
PREPARE stmt FROM @add_idx; EXECUTE stmt; DEALLOCATE PREPARE stmt;
