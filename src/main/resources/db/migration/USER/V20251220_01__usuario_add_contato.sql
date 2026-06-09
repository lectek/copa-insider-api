-- add contact fields to usuario (MySQL < 8.0.29 does not support IF NOT EXISTS)
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario'
    AND COLUMN_NAME = 'telefone'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE usuario ADD COLUMN telefone varchar(25) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario'
    AND COLUMN_NAME = 'endereco'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE usuario ADD COLUMN endereco varchar(200) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
