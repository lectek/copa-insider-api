-- V25__otp_verification_token_backfill_and_enforce.sql

-- 0) cria a tabela se não existir (modelo mínimo; ajuste colunas conforme seu domínio)
CREATE TABLE IF NOT EXISTS otp_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  code VARCHAR(12) NOT NULL,
  verification_token VARCHAR(191) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NULL,
  INDEX idx_otp_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1) Ajusta o tipo/tamanho do token (só se a coluna já existir)
SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'otp_code'
       AND column_name = 'verification_token') > 0,
  'ALTER TABLE otp_code MODIFY COLUMN verification_token VARCHAR(191) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Unique index no token (idempotente)
SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'otp_code'
       AND index_name = 'uk_otp_verification_token') = 0,
  'CREATE UNIQUE INDEX uk_otp_verification_token ON otp_code (verification_token)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) (opcional) backfill do verification_token se você precisa popular dado legado
-- UPDATE otp_code SET verification_token = ... WHERE verification_token IS NULL;
