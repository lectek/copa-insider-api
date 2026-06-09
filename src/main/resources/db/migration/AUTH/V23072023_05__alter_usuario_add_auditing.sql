-- Auditing columns em `usuario` (compatível com MySQL 8.x sem ADD COLUMN IF NOT EXISTS)

-- created_at
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'created_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE `usuario` ADD COLUMN `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- updated_at
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'updated_at'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE `usuario` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- created_by
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'created_by'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE `usuario` ADD COLUMN `created_by` BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- updated_by
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'updated_by'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE `usuario` ADD COLUMN `updated_by` BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- (Opcional) índices – só crie se precisar:
-- SET @exists := (
--   SELECT COUNT(*) FROM information_schema.STATISTICS
--   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'ix_usuario_created_by'
-- );
-- SET @sql := IF(@exists = 0,
--   'CREATE INDEX `ix_usuario_created_by` ON `usuario`(`created_by`)',
--   'SELECT 1'
-- );
-- PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- SET @exists := (
--   SELECT COUNT(*) FROM information_schema.STATISTICS
--   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'ix_usuario_updated_by'
-- );
-- SET @sql := IF(@exists = 0,
--   'CREATE INDEX `ix_usuario_updated_by` ON `usuario`(`updated_by`)',
--   'SELECT 1'
-- );
-- PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
