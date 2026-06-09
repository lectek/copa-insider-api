SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pedido'
    AND COLUMN_NAME = 'gateway_payment_ticket_url'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_payment_ticket_url varchar(1024) NULL AFTER gateway_payment_updated_at',
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
    AND COLUMN_NAME = 'gateway_pix_qr_code'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_pix_qr_code varchar(4096) NULL AFTER gateway_payment_ticket_url',
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
    AND COLUMN_NAME = 'gateway_pix_qr_code_base64'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE pedido ADD COLUMN gateway_pix_qr_code_base64 MEDIUMTEXT NULL AFTER gateway_pix_qr_code',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
