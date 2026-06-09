-- cria a tabela se não existir
CREATE TABLE IF NOT EXISTS otp_code (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  destination        VARCHAR(191)  NOT NULL,
  code               VARCHAR(32)   NOT NULL,
  verification_token VARCHAR(191)  NULL,
  created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  expires_at         DATETIME(6)   NOT NULL,
  consumed_at        DATETIME(6)   NULL,
  verified_at        DATETIME(6)   NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- garante o índice (sem usar IF NOT EXISTS)
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'otp_code'
    AND index_name   = 'idx_otp_dest_created'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_otp_dest_created ON otp_code (destination, created_at)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
