SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'endereco_entrega'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN endereco_entrega varchar(255) NULL AFTER metodo_pagamento',
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
    AND COLUMN_NAME = 'codigo_entrega'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN codigo_entrega varchar(6) NULL AFTER endereco_entrega',
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
    AND COLUMN_NAME = 'codigo_entrega_gerado_em'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN codigo_entrega_gerado_em datetime NULL AFTER codigo_entrega',
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
    AND COLUMN_NAME = 'codigo_entrega_confirmado_em'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN codigo_entrega_confirmado_em datetime NULL AFTER codigo_entrega_gerado_em',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
