SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'gateway_provider'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_provider varchar(40) NULL AFTER pagamento_recebido_em',
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
    AND COLUMN_NAME = 'gateway_owner_reference'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_owner_reference varchar(120) NULL AFTER gateway_provider',
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
    AND COLUMN_NAME = 'gateway_preference_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_preference_id varchar(80) NULL AFTER gateway_owner_reference',
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
    AND COLUMN_NAME = 'gateway_external_reference'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_external_reference varchar(120) NULL AFTER gateway_preference_id',
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
    AND COLUMN_NAME = 'gateway_checkout_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_checkout_url varchar(1024) NULL AFTER gateway_external_reference',
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
    AND COLUMN_NAME = 'gateway_payment_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_payment_id varchar(80) NULL AFTER gateway_checkout_url',
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
    AND COLUMN_NAME = 'gateway_payment_status'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_payment_status varchar(40) NULL AFTER gateway_payment_id',
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
    AND COLUMN_NAME = 'gateway_payment_status_detail'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_payment_status_detail varchar(255) NULL AFTER gateway_payment_status',
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
    AND COLUMN_NAME = 'gateway_payment_updated_at'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_payment_updated_at datetime NULL AFTER gateway_payment_status_detail',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND INDEX_NAME = 'idx_pedido_gateway_external_reference'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_pedido_gateway_external_reference ON pedido (gateway_external_reference)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND INDEX_NAME = 'idx_pedido_gateway_payment_id'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_pedido_gateway_payment_id ON pedido (gateway_payment_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
