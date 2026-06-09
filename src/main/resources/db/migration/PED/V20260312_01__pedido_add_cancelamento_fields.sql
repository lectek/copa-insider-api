SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'cancelamento_motivo'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN cancelamento_motivo varchar(40) NULL AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'cancelado_em'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN cancelado_em datetime NULL AFTER cancelamento_motivo',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
