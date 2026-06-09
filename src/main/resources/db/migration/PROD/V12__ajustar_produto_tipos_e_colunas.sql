-- 1) codigo_original -> BIGINT (só se existir e não for BIGINT)
SET @tem_codigo_original := (
  SELECT COUNT(*) 
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'codigo_original'
    AND DATA_TYPE <> 'bigint'
);
SET @sql_mod := IF(@tem_codigo_original > 0,
  'ALTER TABLE `produto` MODIFY COLUMN `codigo_original` BIGINT NULL',
  'SELECT 1'
);
PREPARE s0 FROM @sql_mod; EXECUTE s0; DEALLOCATE PREPARE s0;

-- 2) cria id_produto_externo se ainda não existir (como você já fez)
SET @falta_externo := (
  SELECT COUNT(*) = 0
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'id_produto_externo'
);
SET @sql_externo := IF(@falta_externo,
  'ALTER TABLE `produto` ADD COLUMN `id_produto_externo` BIGINT NULL',
  'SELECT 1'
);
PREPARE s1 FROM @sql_externo; EXECUTE s1; DEALLOCATE PREPARE s1;
