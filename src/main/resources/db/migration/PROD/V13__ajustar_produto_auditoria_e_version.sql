-- V13__ajustar_produto_auditoria_e_version.sql

-- created_at
SET @precisa_created := (
  SELECT COUNT(*) = 0
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'created_at'
);
SET @sql_created := IF(@precisa_created,
  'ALTER TABLE `produto` ADD COLUMN `created_at` DATETIME(6) NULL',
  'SELECT 1'
);
PREPARE s1 FROM @sql_created; EXECUTE s1; DEALLOCATE PREPARE s1;

-- updated_at
SET @precisa_updated := (
  SELECT COUNT(*) = 0
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'updated_at'
);
SET @sql_updated := IF(@precisa_updated,
  'ALTER TABLE `produto` ADD COLUMN `updated_at` DATETIME(6) NULL',
  'SELECT 1'
);
PREPARE s2 FROM @sql_updated; EXECUTE s2; DEALLOCATE PREPARE s2;

-- version (precisa existir, ser BIGINT e NOT NULL)
SET @existe_version := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'version'
);

SET @precisa_ajuste_version := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'version'
    AND (DATA_TYPE <> 'bigint' OR IS_NULLABLE = 'YES')
);

SET @sql_version := IF(@existe_version = 0,
  'ALTER TABLE `produto` ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0',
  IF(@precisa_ajuste_version > 0,
     'ALTER TABLE `produto` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0',
     'SELECT 1'
  )
);
PREPARE s3 FROM @sql_version; EXECUTE s3; DEALLOCATE PREPARE s3;
