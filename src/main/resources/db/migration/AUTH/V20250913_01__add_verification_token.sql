-- cria a coluna apenas se não existir (compatível com todos 8.0)
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'otp_code'
    AND COLUMN_NAME  = 'verification_token'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE otp_code ADD COLUMN verification_token VARCHAR(255)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- cria o índice somente se não existir (compatível com todos 8.0)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'otp_code'
    AND INDEX_NAME   = 'idx_otp_verification_token'
);

SET @ddl := IF(
  @idx_exists = 0,
  'ALTER TABLE otp_code ADD INDEX idx_otp_verification_token (verification_token)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
