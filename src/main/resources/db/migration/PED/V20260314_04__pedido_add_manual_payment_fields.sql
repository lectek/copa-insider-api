SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'forma_pagamento_recebida'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN forma_pagamento_recebida varchar(30) NULL AFTER metodo_pagamento',
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
    AND COLUMN_NAME = 'pagamento_divergente'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN pagamento_divergente bit(1) NOT NULL DEFAULT b''0'' AFTER forma_pagamento_recebida',
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
    AND COLUMN_NAME = 'avaliacao_cliente'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN avaliacao_cliente int NULL AFTER pagamento_divergente',
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
    AND COLUMN_NAME = 'pagamento_recebido_em'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN pagamento_recebido_em datetime NULL AFTER avaliacao_cliente',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
