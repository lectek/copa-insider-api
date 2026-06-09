-- V21__customers_social_login.sql  (MySQL 8.0)
-- Cria/ajusta a estrutura sem quebrar se já existir

-- 1) cria a tabela se não existir
CREATE TABLE IF NOT EXISTS `customers` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT,
  `nome`             VARCHAR(120)  NOT NULL,
  `email`            VARCHAR(180)  NULL,
  `avatar_url`       VARCHAR(512)  NULL,
  `provider`         VARCHAR(32)   NULL,         -- "google" | "facebook"
  `provider_user_id` VARCHAR(128)  NULL,         -- sub/id do provedor
  `email_verificado` TINYINT(1)    NOT NULL DEFAULT 0,
  `ativo`            TINYINT(1)    NOT NULL DEFAULT 1,
  `created_at`       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) garante as colunas de forma compatível (sem usar ADD COLUMN IF NOT EXISTS)

-- nome (NOT NULL)
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='nome'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `nome` VARCHAR(120) NOT NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- email
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='email'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `email` VARCHAR(180) NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- avatar_url
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='avatar_url'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `avatar_url` VARCHAR(512) NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- provider
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='provider'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `provider` VARCHAR(32) NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- provider_user_id
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='provider_user_id'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `provider_user_id` VARCHAR(128) NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- email_verificado
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='email_verificado'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `email_verificado` TINYINT(1) NOT NULL DEFAULT 0'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ativo
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='ativo'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `ativo` TINYINT(1) NOT NULL DEFAULT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- created_at
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='created_at'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- updated_at
SET @sql := IF (
  EXISTS (SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name='customers' AND column_name='updated_at'),
  'SELECT 1',
  'ALTER TABLE `customers` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) UNIQUE(email) se ainda não existir (permite múltiplos NULL)
SET @sql := IF (
  EXISTS (
    SELECT 1
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name   = 'customers'
       AND non_unique   = 0
     GROUP BY index_name
    HAVING SUM(column_name='email')=1 AND COUNT(*)=1
  ),
  'SELECT 1',
  'ALTER TABLE `customers` ADD CONSTRAINT `uk_customers_email` UNIQUE (`email`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) UNIQUE(provider, provider_user_id) se ainda não existir
SET @sql := IF (
  EXISTS (
    SELECT 1
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name   = 'customers'
       AND index_name   = 'uk_customers_provider_user'
  ),
  'SELECT 1',
  'ALTER TABLE `customers` ADD CONSTRAINT `uk_customers_provider_user` UNIQUE (`provider`,`provider_user_id`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
