-- Cria tabela de outbox para envios de e-mail confiáveis (com retry)
-- Arquivo sugerido: db/migration/AUTH/V26__create_email_delivery_outbox.sql
-- Observação: criamos índices de forma condicional via information_schema + PREPARE,
-- porque MySQL não aceita "CREATE INDEX IF NOT EXISTS".

CREATE TABLE IF NOT EXISTS email_delivery (
  id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  purpose      VARCHAR(64)  NOT NULL,        -- OTP_START, REGISTER_CONFIRM, etc.
  destination  VARCHAR(254) NOT NULL,        -- e-mail do destinatário
  payload_json JSON         NOT NULL,        -- template vars/conteúdo (JSON)
  provider     VARCHAR(32)  NOT NULL,        -- SMTP, SENDGRID, SES...
  status       VARCHAR(16)  NOT NULL,        -- PENDING, SENT, FAILED
  attempts     INT          NOT NULL DEFAULT 0,
  last_error   TEXT         NULL,
  message_id   VARCHAR(191) NULL,            -- ID retornado pelo provedor, se houver
  created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at   DATETIME(6)  NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice composto para varrer pendentes por ordem de criação (status, created_at)
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
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Índice opcional para buscas por destino (destination)
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
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Opcional (idempotência por janela temporal): ajuste conforme estratégia
-- Exemplo: impedir duplicatas do mesmo propósito+destino em curto período.
-- Descomentando abaixo, crie uma UNIQUE condicional via prepared statement também.
-- SET @idx_exists := (
--   SELECT COUNT(*)
--   FROM information_schema.statistics
--   WHERE table_schema = DATABASE()
--     AND table_name   = 'email_delivery'
--     AND index_name   = 'ux_email_delivery_idem'
-- );
-- SET @ddl := IF(
--   @idx_exists = 0,
--   'CREATE UNIQUE INDEX ux_email_delivery_idem ON email_delivery (purpose, destination, created_at)',
--   'DO 0'
-- );
-- PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
