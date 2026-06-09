-- Creates the app_settings table used by branding/global settings in prod.
CREATE TABLE IF NOT EXISTS app_settings (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  setting_key   VARCHAR(191)  NOT NULL,
  setting_value TEXT          DEFAULT NULL,
  description   VARCHAR(255)  DEFAULT NULL,
  created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Guarantee some defaults so the UI can render without hitting nulls too soon.
INSERT INTO app_settings (setting_key, setting_value, description)
VALUES
  ('loja.nome', 'RedeMaisFarma', 'Nome Público'),
  ('loja.logo', '/images/logo.png', 'URL padrão do logo')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), description = VALUES(description);
