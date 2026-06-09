SET @db := DATABASE();

SET @canonical_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'app_settings'
);

SET @legacy_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'app_setting'
);

SET @canonical_ok := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'app_settings'
    AND column_name IN ('setting_key', 'setting_value', 'description', 'created_at', 'updated_at')
);

SET @sql := 'DROP TABLE IF EXISTS app_settings_canonical';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE app_settings_canonical (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  setting_key   VARCHAR(191) NOT NULL,
  setting_value LONGTEXT     NULL,
  description   VARCHAR(255) NULL,
  created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @copy_canonical_sql := IF(
  @canonical_exists = 1 AND @canonical_ok >= 5,
  'INSERT INTO app_settings_canonical (id, setting_key, setting_value, description, created_at, updated_at)
   SELECT
     id,
     setting_key,
     setting_value,
     description,
     COALESCE(created_at, CURRENT_TIMESTAMP(6)),
     COALESCE(updated_at, CURRENT_TIMESTAMP(6))
   FROM app_settings
   ON DUPLICATE KEY UPDATE
     setting_value = VALUES(setting_value),
     description = VALUES(description),
     created_at = VALUES(created_at),
     updated_at = VALUES(updated_at)',
  'SELECT 1'
);
PREPARE stmt FROM @copy_canonical_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @copy_legacy_sql := IF(
  @legacy_exists = 1,
  'INSERT INTO app_settings_canonical (setting_key, setting_value, description, created_at, updated_at)
   SELECT
     skey,
     svalue,
     COALESCE(description, CONCAT(''Migrado de app_setting'', IF(category IS NULL OR TRIM(category) = '''', '''', CONCAT('' ['' , category , '']'')))),
     COALESCE(updated_at, CURRENT_TIMESTAMP(6)),
     COALESCE(updated_at, CURRENT_TIMESTAMP(6))
   FROM app_setting
   WHERE skey IS NOT NULL
     AND TRIM(skey) <> ''''
   ON DUPLICATE KEY UPDATE
     setting_value = COALESCE(app_settings_canonical.setting_value, VALUES(setting_value)),
     description = COALESCE(app_settings_canonical.description, VALUES(description)),
     updated_at = GREATEST(app_settings_canonical.updated_at, VALUES(updated_at))',
  'SELECT 1'
);
PREPARE stmt FROM @copy_legacy_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @canonical_backup_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'app_settings_legacy_20260326'
);

SET @legacy_backup_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'app_setting_legacy_20260326'
);

SET @sql := IF(
  @canonical_exists = 1 AND @canonical_backup_exists = 0,
  'RENAME TABLE app_settings TO app_settings_legacy_20260326',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @legacy_exists = 1 AND @legacy_backup_exists = 0,
  'RENAME TABLE app_setting TO app_setting_legacy_20260326',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @app_settings_exists_after := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'app_settings'
);

SET @sql := IF(
  @app_settings_exists_after = 0,
  'RENAME TABLE app_settings_canonical TO app_settings',
  'DROP TABLE app_settings_canonical'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
