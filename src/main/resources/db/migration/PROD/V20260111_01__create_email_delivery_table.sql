-- Creates email_delivery outbox table so the scheduled worker can load pending emails.
CREATE TABLE IF NOT EXISTS email_delivery (
  id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  purpose      VARCHAR(64)  NOT NULL,
  destination  VARCHAR(254) NOT NULL,
  payload_json JSON         NOT NULL,
  provider     VARCHAR(32)  NOT NULL,
  status       VARCHAR(16)  NOT NULL,
  attempts     INT          NOT NULL DEFAULT 0,
  last_error   TEXT         DEFAULT NULL,
  message_id   VARCHAR(191) DEFAULT NULL,
  created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Composite index to fetch pending deliveries ordered by creation.
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_delivery'
    AND index_name   = 'idx_email_delivery_status_created'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_delivery_status_created ON email_delivery (status, created_at)',
  'DO 0'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_delivery'
    AND index_name   = 'idx_email_delivery_destination'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_delivery_destination ON email_delivery (destination)',
  'DO 0'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
