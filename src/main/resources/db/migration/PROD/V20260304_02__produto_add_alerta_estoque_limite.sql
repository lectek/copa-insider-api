-- Adiciona limite de alerta de estoque por produto (override do limite global).

SET @schema_name = DATABASE();

SET @exists_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'alerta_estoque_limite'
);

SET @sql := IF(
  @exists_col = 0,
  'ALTER TABLE produto ADD COLUMN alerta_estoque_limite INT NULL',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
