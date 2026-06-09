-- Registra a origem da leitura do codigo de barras por produto.

SET @sql := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE produto ADD COLUMN metodo_leitura_codigo_barras VARCHAR(32) NULL',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'produto'
    AND column_name = 'metodo_leitura_codigo_barras'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

UPDATE produto
SET metodo_leitura_codigo_barras = 'DESCONHECIDO'
WHERE metodo_leitura_codigo_barras IS NULL
   OR TRIM(metodo_leitura_codigo_barras) = '';

SET @sql := (
  SELECT IF(COUNT(*) = 0,
            'CREATE INDEX idx_produto_metodo_leitura ON produto (metodo_leitura_codigo_barras)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'produto'
    AND index_name = 'idx_produto_metodo_leitura'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;
