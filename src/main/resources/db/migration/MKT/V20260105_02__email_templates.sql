-- Templates de email customizados

CREATE TABLE IF NOT EXISTS email_template (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nome        VARCHAR(120) NOT NULL,
  assunto     VARCHAR(180) NOT NULL,
  html        LONGTEXT     NOT NULL,
  created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at  DATETIME(6)  NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name   = 'email_template'
    AND index_name   = 'idx_email_template_nome'
);
SET @ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_email_template_nome ON email_template (nome)',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
