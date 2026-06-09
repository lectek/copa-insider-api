SET @db := DATABASE();

SET @table_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'otp_code'
);

SET @has_delivery_id := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'delivery_id'
);

SET @has_code_hash := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'code_hash'
);

SET @has_salt := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'salt'
);

SET @has_ttl_seconds := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'ttl_seconds'
);

SET @has_attempts := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'attempts'
);

SET @has_max_attempts := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'max_attempts'
);

SET @has_status := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'status'
);

SET @has_code_plain := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'otp_code'
    AND column_name = 'code'
);

SET @is_canonical := IF(
  @table_exists = 1
  AND @has_delivery_id = 1
  AND @has_code_hash = 1
  AND @has_salt = 1
  AND @has_ttl_seconds = 1
  AND @has_attempts = 1
  AND @has_max_attempts = 1
  AND @has_status = 1,
  1,
  0
);

SET @sql := 'DROP TABLE IF EXISTS otp_code_canonical';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE otp_code_canonical (
  id                 BIGINT        NOT NULL AUTO_INCREMENT,
  delivery_id        VARCHAR(128)  NOT NULL,
  destination        VARCHAR(255)  NOT NULL,
  code_hash          VARCHAR(255)  NOT NULL,
  salt               VARCHAR(64)   NOT NULL,
  ttl_seconds        INT           NOT NULL,
  attempts           INT           NOT NULL DEFAULT 0,
  max_attempts       INT           NOT NULL DEFAULT 5,
  created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  expires_at         DATETIME(6)   NOT NULL,
  status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  verification_token VARCHAR(255)  NULL,
  verified_at        DATETIME(6)   NULL,
  consumed_at        DATETIME(6)   NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_otp_delivery_id (delivery_id),
  KEY idx_otp_destination (destination),
  KEY idx_otp_expires (expires_at),
  KEY idx_otp_verification_token (verification_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @copy_sql := IF(
  @table_exists = 0,
  'SELECT 1',
  IF(
    @is_canonical = 1,
    'INSERT INTO otp_code_canonical (
       id, delivery_id, destination, code_hash, salt, ttl_seconds, attempts, max_attempts,
       created_at, expires_at, status, verification_token, verified_at, consumed_at
     )
     SELECT
       id,
       delivery_id,
       destination,
       code_hash,
       salt,
       ttl_seconds,
       attempts,
       max_attempts,
       created_at,
       expires_at,
       status,
       verification_token,
       verified_at,
       consumed_at
     FROM otp_code
     ON DUPLICATE KEY UPDATE
       destination = VALUES(destination),
       code_hash = VALUES(code_hash),
       salt = VALUES(salt),
       ttl_seconds = VALUES(ttl_seconds),
       attempts = VALUES(attempts),
       max_attempts = VALUES(max_attempts),
       created_at = VALUES(created_at),
       expires_at = VALUES(expires_at),
       status = VALUES(status),
       verification_token = VALUES(verification_token),
       verified_at = VALUES(verified_at),
       consumed_at = VALUES(consumed_at)',
    IF(
      @has_code_plain = 1,
      'INSERT INTO otp_code_canonical (
         id, delivery_id, destination, code_hash, salt, ttl_seconds, attempts, max_attempts,
         created_at, expires_at, status, verification_token, verified_at, consumed_at
       )
       SELECT
         id,
         LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), UUID()), 128),
         destination,
         SHA2(COALESCE(code, ''''), 256),
         '''',
         GREATEST(0, TIMESTAMPDIFF(SECOND, created_at, expires_at)),
         0,
         5,
         created_at,
         expires_at,
         CASE
           WHEN consumed_at IS NOT NULL THEN ''CONSUMED''
           WHEN verified_at IS NOT NULL THEN ''VERIFIED''
           WHEN expires_at < CURRENT_TIMESTAMP(6) THEN ''EXPIRED''
           ELSE ''PENDING''
         END,
         verification_token,
         verified_at,
         consumed_at
       FROM otp_code
       ON DUPLICATE KEY UPDATE
         destination = VALUES(destination),
         code_hash = VALUES(code_hash),
         salt = VALUES(salt),
         ttl_seconds = VALUES(ttl_seconds),
         attempts = VALUES(attempts),
         max_attempts = VALUES(max_attempts),
         created_at = VALUES(created_at),
         expires_at = VALUES(expires_at),
         status = VALUES(status),
         verification_token = VALUES(verification_token),
         verified_at = VALUES(verified_at),
         consumed_at = VALUES(consumed_at)',
      'INSERT INTO otp_code_canonical (
         id, delivery_id, destination, code_hash, salt, ttl_seconds, attempts, max_attempts,
         created_at, expires_at, status, verification_token, verified_at, consumed_at
       )
       SELECT
         id,
         LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), UUID()), 128),
         destination,
         SHA2(COALESCE(verification_token, CONCAT(''legacy-'', id)), 256),
         '''',
         GREATEST(0, TIMESTAMPDIFF(SECOND, created_at, expires_at)),
         0,
         5,
         created_at,
         expires_at,
         CASE
           WHEN consumed_at IS NOT NULL THEN ''CONSUMED''
           WHEN verified_at IS NOT NULL THEN ''VERIFIED''
           WHEN expires_at < CURRENT_TIMESTAMP(6) THEN ''EXPIRED''
           ELSE ''PENDING''
         END,
         verification_token,
         verified_at,
         consumed_at
       FROM otp_code
       ON DUPLICATE KEY UPDATE
         destination = VALUES(destination),
         code_hash = VALUES(code_hash),
         salt = VALUES(salt),
         ttl_seconds = VALUES(ttl_seconds),
         attempts = VALUES(attempts),
         max_attempts = VALUES(max_attempts),
         created_at = VALUES(created_at),
         expires_at = VALUES(expires_at),
         status = VALUES(status),
         verification_token = VALUES(verification_token),
         verified_at = VALUES(verified_at),
         consumed_at = VALUES(consumed_at)'
    )
  )
);
PREPARE stmt FROM @copy_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @backup_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'otp_code_legacy_20260326'
);

SET @rename_sql := IF(
  @is_canonical = 1,
  'DROP TABLE otp_code_canonical',
  IF(
    @table_exists = 1 AND @backup_exists = 0,
    'RENAME TABLE otp_code TO otp_code_legacy_20260326, otp_code_canonical TO otp_code',
    IF(
      @table_exists = 1 AND @backup_exists = 1,
      'DROP TABLE otp_code',
      'SELECT 1'
    )
  )
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @finalize_sql := IF(
  @is_canonical = 1,
  'SELECT 1',
  IF(
    @table_exists = 1 AND @backup_exists = 1,
    'RENAME TABLE otp_code_canonical TO otp_code',
    'SELECT 1'
  )
);
PREPARE stmt FROM @finalize_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
