-- src/main/resources/db/migration/V20250909_02__add_data_nascimento.sql
-- Adiciona 'data_nascimento' se ainda não existir (MySQL)
SET @exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'cliente'
    AND COLUMN_NAME = 'data_nascimento'
);

SET @sql := IF(@exists = 0,
  'ALTER TABLE cliente ADD COLUMN data_nascimento DATE NULL',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
