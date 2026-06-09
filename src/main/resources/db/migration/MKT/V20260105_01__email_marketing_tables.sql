-- Email marketing: campanhas, fila e logs

CREATE TABLE IF NOT EXISTS email_campaign (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nome          VARCHAR(120) NOT NULL,
  assunto       VARCHAR(180) NOT NULL,
  template_key  VARCHAR(120) NOT NULL,
  status        VARCHAR(20)  NOT NULL,
  scheduled_at  DATETIME(6)  NULL,
  segment_json  JSON         NULL,
  created_by    VARCHAR(120) NULL,
  created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)  NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_campaign_queue (
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  campaign_id      BIGINT       NOT NULL,
  recipient_email  VARCHAR(254) NOT NULL,
  recipient_name   VARCHAR(120) NULL,
  payload_json     JSON         NULL,
  status           VARCHAR(16)  NOT NULL,
  attempts         INT          NOT NULL DEFAULT 0,
  last_error       TEXT         NULL,
  message_id       VARCHAR(191) NULL,
  scheduled_at     DATETIME(6)  NULL,
  created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at       DATETIME(6)  NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_email_campaign_queue_campaign
    FOREIGN KEY (campaign_id) REFERENCES email_campaign(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_campaign_log (
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  campaign_id      BIGINT       NOT NULL,
  recipient_email  VARCHAR(254) NOT NULL,
  status           VARCHAR(16)  NOT NULL,
  provider         VARCHAR(32)  NULL,
  message_id       VARCHAR(191) NULL,
  error_text       TEXT         NULL,
  sent_at          DATETIME(6)  NULL,
  created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_email_campaign_log_campaign
    FOREIGN KEY (campaign_id) REFERENCES email_campaign(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes (MySQL: usar information_schema + PREPARE)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign'
    AND index_name   = 'idx_email_campaign_status_scheduled'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_status_scheduled ON email_campaign (status, scheduled_at)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign_queue'
    AND index_name   = 'idx_email_campaign_queue_status_sched'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_queue_status_sched ON email_campaign_queue (status, scheduled_at)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign_queue'
    AND index_name   = 'idx_email_campaign_queue_campaign'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_queue_campaign ON email_campaign_queue (campaign_id)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign_queue'
    AND index_name   = 'idx_email_campaign_queue_recipient'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_queue_recipient ON email_campaign_queue (recipient_email)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign_log'
    AND index_name   = 'idx_email_campaign_log_campaign_sent'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_log_campaign_sent ON email_campaign_log (campaign_id, sent_at)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_campaign_log'
    AND index_name   = 'idx_email_campaign_log_recipient'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_campaign_log_recipient ON email_campaign_log (recipient_email)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
