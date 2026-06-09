-- AUTH/V28__otp_code_fix_columns.sql  (versão idempotente e compatível MySQL 8.0)

-- 1) Garante a tabela (se já existir, não recria)
CREATE TABLE IF NOT EXISTS otp_code (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  destination        VARCHAR(255) NOT NULL,
  code_hash          VARBINARY(64) NULL,
  salt               VARBINARY(32) NULL,
  status             VARCHAR(32)  NULL,          -- deixe NULL para não quebrar bases antigas
  attempts           INT          NOT NULL DEFAULT 0,
  max_attempts       INT          NOT NULL DEFAULT 3,
  ttl_seconds        INT          NULL,
  delivery_id        VARCHAR(100) NULL,
  verification_token VARCHAR(100) NULL,
  created_at         DATETIME(6)  NOT NULL,
  expires_at         DATETIME(6)  NOT NULL,
  consumed_at        DATETIME(6)  NULL,
  verified_at        DATETIME(6)  NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1) Assegura colunas sem usar "ADD COLUMN IF NOT EXISTS"
SET @tbl := 'otp_code';

-- destination ---------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'destination'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN destination VARCHAR(255) NOT NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- code_hash -----------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'code_hash'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN code_hash VARBINARY(64) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- salt ----------------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'salt'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN salt VARBINARY(32) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- status --------------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'status'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN status VARCHAR(32) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- attempts ------------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'attempts'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN attempts INT NOT NULL DEFAULT 0;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- max_attempts --------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'max_attempts'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN max_attempts INT NOT NULL DEFAULT 3;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ttl_seconds ---------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'ttl_seconds'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN ttl_seconds INT NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- delivery_id ---------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'delivery_id'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN delivery_id VARCHAR(100) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- verification_token --------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'verification_token'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN verification_token VARCHAR(100) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- created_at ----------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'created_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN created_at DATETIME(6) NOT NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- expires_at ----------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'expires_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN expires_at DATETIME(6) NOT NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- consumed_at ---------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'consumed_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN consumed_at DATETIME(6) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- verified_at ---------------------------------------------------------------
SET @exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = @tbl AND column_name = 'verified_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE otp_code ADD COLUMN verified_at DATETIME(6) NULL;',
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2) Remoção segura de índices legados
--    (uk_otp_delivery_id, uk_otp_verification_token, idx_otp_* antigos, etc.)
-- Helper: dropar índice se existir
-- Uso: set @idx := 'NOME'; (gera ALTER TABLE otp_code DROP INDEX NOME; se existir)
SET @tbl := 'otp_code';

-- uk_otp_delivery_id ---------------------------------------------------------
SET @idx := 'uk_otp_delivery_id';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- uk_otp_verification_token --------------------------------------------------
SET @idx := 'uk_otp_verification_token';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_created_at ---------------------------------------------------------
SET @idx := 'idx_otp_created_at';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_expires_at ---------------------------------------------------------
SET @idx := 'idx_otp_expires_at';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_status -------------------------------------------------------------
SET @idx := 'idx_otp_status';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_destination --------------------------------------------------------
SET @idx := 'idx_otp_destination';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_delivery_id --------------------------------------------------------
SET @idx := 'idx_otp_delivery_id';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_token --------------------------------------------------------------
SET @idx := 'idx_otp_token';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_dest_created (legado composto) ------------------------------------
SET @idx := 'idx_otp_dest_created';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx > 0,
  CONCAT('ALTER TABLE ', @tbl, ' DROP INDEX ', @idx, ';'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3) Recria apenas os índices desejados (não-únicos) de forma idempotente

-- idx_otp_destination (destination)
SET @idx := 'idx_otp_destination';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx = 0,
  CONCAT('ALTER TABLE ', @tbl, ' ADD INDEX ', @idx, ' (destination);'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_delivery_id (delivery_id)
SET @idx := 'idx_otp_delivery_id';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx = 0,
  CONCAT('ALTER TABLE ', @tbl, ' ADD INDEX ', @idx, ' (delivery_id);'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- idx_otp_token (verification_token)
SET @idx := 'idx_otp_token';
SET @exists_idx := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = @tbl AND index_name = @idx
);
SET @sql := IF(@exists_idx = 0,
  CONCAT('ALTER TABLE ', @tbl, ' ADD INDEX ', @idx, ' (verification_token);'),
  'SELECT 1;'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
