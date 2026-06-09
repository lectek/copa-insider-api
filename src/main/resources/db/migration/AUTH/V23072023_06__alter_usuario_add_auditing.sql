-- Auditoria e controle na tabela `usuario`
-- Migration idempotente compatível com MySQL 8.x
-- Cria (se não existirem) as colunas: version, created_at, updated_at

-- Garanta que o schema atual está setado
SET @db := DATABASE();

-- =============================
-- Coluna: version (para @Version do JPA)
-- =============================
SELECT COUNT(*) INTO @cnt_version
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'usuario'
  AND COLUMN_NAME = 'version';

SET @sql_version := IF(
  @cnt_version = 0,
  'ALTER TABLE `usuario` ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0',
  'DO 0'
);
PREPARE stmt FROM @sql_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================
-- Coluna: created_at (timestamp de criação)
-- =============================
SELECT COUNT(*) INTO @cnt_created
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'usuario'
  AND COLUMN_NAME = 'created_at';

SET @sql_created := IF(
  @cnt_created = 0,
  'ALTER TABLE `usuario` ADD COLUMN `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP',
  'DO 0'
);
PREPARE stmt FROM @sql_created;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================
-- Coluna: updated_at (timestamp de atualização)
-- =============================
SELECT COUNT(*) INTO @cnt_updated
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'usuario'
  AND COLUMN_NAME = 'updated_at';

SET @sql_updated := IF(
  @cnt_updated = 0,
  'ALTER TABLE `usuario` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP',
  'DO 0'
);
PREPARE stmt FROM @sql_updated;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- (Opcional) Remover DEFAULT de version após criada, se preferir sem default:
-- SELECT COUNT(*) INTO @cnt_has_default
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = @db
--   AND TABLE_NAME = 'usuario'
--   AND COLUMN_NAME = 'version'
--   AND COLUMN_DEFAULT IS NOT NULL;
-- SET @sql_drop_default := IF(
--   @cnt_has_default > 0,
--   'ALTER TABLE `usuario` ALTER COLUMN `version` DROP DEFAULT',
--   'DO 0'
-- );
-- PREPARE stmt FROM @sql_drop_default;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;
