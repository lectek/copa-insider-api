-- Adiciona a coluna 'ativo' apenas se ela ainda não existir (MySQL)
SET @exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'cliente'
    AND COLUMN_NAME = 'ativo'
);

SET @sql := IF(@exists = 0,
  'ALTER TABLE cliente ADD COLUMN ativo TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
