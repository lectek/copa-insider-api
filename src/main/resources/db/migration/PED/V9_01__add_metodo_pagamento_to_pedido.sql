-- Adiciona metodo_pagamento para armazenar escolhas customizadas, se ainda nao existir.
SET @rmf_col_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'pedido'
    AND column_name = 'metodo_pagamento'
);

SET @rmf_sql = IF(
  @rmf_col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN metodo_pagamento VARCHAR(80) NULL AFTER tipo_pagamento',
  'SELECT 1'
);

PREPARE rmf_stmt FROM @rmf_sql;
EXECUTE rmf_stmt;
DEALLOCATE PREPARE rmf_stmt;
