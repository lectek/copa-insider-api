-- cria tabela se ainda não existir
CREATE TABLE IF NOT EXISTS app_settings (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  setting_key   VARCHAR(191)  NOT NULL,
  setting_value TEXT          NULL,
  description   VARCHAR(255)  NULL,
  created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)   NULL     DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
