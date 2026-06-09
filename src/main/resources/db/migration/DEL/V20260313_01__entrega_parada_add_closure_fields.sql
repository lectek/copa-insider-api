SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'entrega_parada'
    AND COLUMN_NAME = 'forma_pagamento_recebida'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE entrega_parada ADD COLUMN forma_pagamento_recebida varchar(30) NULL AFTER observacao',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'entrega_parada'
    AND COLUMN_NAME = 'pagamento_divergente'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE entrega_parada ADD COLUMN pagamento_divergente bit(1) NOT NULL DEFAULT b''0'' AFTER forma_pagamento_recebida',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'entrega_parada'
    AND COLUMN_NAME = 'avaliacao_entrega'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE entrega_parada ADD COLUMN avaliacao_entrega int NULL AFTER pagamento_divergente',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'entrega_parada'
    AND COLUMN_NAME = 'ocorrencias'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE entrega_parada ADD COLUMN ocorrencias varchar(255) NULL AFTER avaliacao_entrega',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
