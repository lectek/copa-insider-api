SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'modo_entrega'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN modo_entrega varchar(20) NULL AFTER tipo_pagamento',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE pedido
   SET modo_entrega = CASE
       WHEN endereco_entrega IS NULL OR TRIM(endereco_entrega) = '' THEN 'RETIRADA'
       ELSE 'ENTREGA'
   END
 WHERE modo_entrega IS NULL;
