CREATE TABLE IF NOT EXISTS gateway_config (
  id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  provedor          VARCHAR(50)  NOT NULL,         -- ex: pagarme, cielo, stone
  api_key           VARCHAR(255) NULL,
  api_secret        VARCHAR(255) NULL,
  webhook_secret    VARCHAR(255) NULL,
  ativo             TINYINT(1)   NOT NULL DEFAULT 1,
  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_gateway_provedor_ativo (provedor, ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
