SET @db := DATABASE();

SET @table_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
);

SET @has_purpose := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'purpose'
);

SET @has_destination := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'destination'
);

SET @has_payload_json := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'payload_json'
);

SET @has_provider := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'provider'
);

SET @has_status := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'status'
);

SET @has_attempts := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'email_delivery'
    AND column_name = 'attempts'
);

SET @is_canonical := IF(
  @table_exists = 1
  AND @has_purpose = 1
  AND @has_destination = 1
  AND @has_payload_json = 1
  AND @has_provider = 1
  AND @has_status = 1
  AND @has_attempts = 1,
  1,
  0
);

SET @sql := 'DROP TABLE IF EXISTS email_delivery_canonical';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE email_delivery_canonical (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  purpose      VARCHAR(64)  NOT NULL,
  destination  VARCHAR(254) NOT NULL,
  payload_json JSON         NOT NULL,
  provider     VARCHAR(32)  NOT NULL,
  status       VARCHAR(16)  NOT NULL,
  attempts     INT          NOT NULL DEFAULT 0,
  last_error   TEXT         NULL,
  message_id   VARCHAR(191) NULL,
  created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_email_delivery_status_created (status, created_at),
  KEY idx_email_delivery_destination (destination)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @copy_sql := IF(
  @table_exists = 0,
  'SELECT 1',
  'INSERT INTO email_delivery_canonical (
     id, purpose, destination, payload_json, provider, status, attempts, last_error, message_id, created_at, updated_at
   )
   SELECT
     id,
     COALESCE(NULLIF(TRIM(CAST(purpose AS CHAR(64))), ''''), ''LEGACY''),
     LEFT(COALESCE(NULLIF(TRIM(CAST(destination AS CHAR(254))), ''''), ''unknown@example.invalid''), 254),
     COALESCE(payload_json, JSON_OBJECT()),
     COALESCE(NULLIF(TRIM(CAST(provider AS CHAR(32))), ''''), ''UNKNOWN''),
     COALESCE(NULLIF(TRIM(CAST(status AS CHAR(16))), ''''), ''PENDING''),
     COALESCE(attempts, 0),
     last_error,
     message_id,
     COALESCE(created_at, CURRENT_TIMESTAMP(6)),
     COALESCE(updated_at, CURRENT_TIMESTAMP(6))
   FROM email_delivery
   ON DUPLICATE KEY UPDATE
     purpose = VALUES(purpose),
     destination = VALUES(destination),
     payload_json = VALUES(payload_json),
     provider = VALUES(provider),
     status = VALUES(status),
     attempts = VALUES(attempts),
     last_error = VALUES(last_error),
     message_id = VALUES(message_id),
     created_at = VALUES(created_at),
     updated_at = VALUES(updated_at)'
);
PREPARE stmt FROM @copy_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @backup_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'email_delivery_legacy_20260326'
);

SET @rename_sql := IF(
  @is_canonical = 1,
  'DROP TABLE email_delivery_canonical',
  IF(
    @table_exists = 1 AND @backup_exists = 0,
    'RENAME TABLE email_delivery TO email_delivery_legacy_20260326, email_delivery_canonical TO email_delivery',
    IF(
      @table_exists = 1 AND @backup_exists = 1,
      'DROP TABLE email_delivery',
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
    'RENAME TABLE email_delivery_canonical TO email_delivery',
    'SELECT 1'
  )
);
PREPARE stmt FROM @finalize_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
