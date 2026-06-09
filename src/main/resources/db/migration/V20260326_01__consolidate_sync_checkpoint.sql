SET @db := DATABASE();

SET @table_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint'
);

SET @has_source := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint'
    AND column_name = 'source'
);

SET @has_last_since := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint'
    AND column_name = 'last_since'
);

SET @has_last_update := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint'
    AND column_name = 'last_update'
);

SET @id_data_type := (
  SELECT COALESCE(data_type, '')
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint'
    AND column_name = 'id'
  LIMIT 1
);

SET @is_canonical := IF(
  @table_exists = 1
  AND @has_source = 1
  AND @has_last_since = 1
  AND @id_data_type IN ('varchar', 'char'),
  1,
  0
);

SET @sql := 'DROP TABLE IF EXISTS sync_checkpoint_canonical';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE sync_checkpoint_canonical (
  id         VARCHAR(64)  NOT NULL,
  source     VARCHAR(100) NOT NULL,
  last_since DATETIME(6)  NOT NULL DEFAULT '1900-01-01 00:00:00',
  updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sync_checkpoint_source (source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @copy_sql := IF(
  @table_exists = 0,
  'SELECT 1',
  IF(
    @has_source = 1 AND @has_last_since = 1,
    'INSERT INTO sync_checkpoint_canonical (id, source, last_since, updated_at)
     SELECT
       LEFT(COALESCE(NULLIF(TRIM(CAST(source AS CHAR(100))), ''''), TRIM(CAST(id AS CHAR(64))), ''legacy''), 64),
       LEFT(COALESCE(NULLIF(TRIM(CAST(source AS CHAR(100))), ''''), TRIM(CAST(id AS CHAR(64))), ''legacy''), 100),
       COALESCE(last_since, ''1900-01-01 00:00:00''),
       COALESCE(updated_at, CURRENT_TIMESTAMP(6))
     FROM sync_checkpoint
     ON DUPLICATE KEY UPDATE
       source = VALUES(source),
       last_since = VALUES(last_since),
       updated_at = VALUES(updated_at)',
    IF(
      @has_source = 1 AND @has_last_update = 1,
      'INSERT INTO sync_checkpoint_canonical (id, source, last_since, updated_at)
       SELECT
         LEFT(COALESCE(NULLIF(TRIM(CAST(source AS CHAR(100))), ''''), TRIM(CAST(id AS CHAR(64))), ''legacy''), 64),
         LEFT(COALESCE(NULLIF(TRIM(CAST(source AS CHAR(100))), ''''), TRIM(CAST(id AS CHAR(64))), ''legacy''), 100),
         COALESCE(last_update, ''1900-01-01 00:00:00''),
         COALESCE(updated_at, CURRENT_TIMESTAMP(6))
       FROM sync_checkpoint
       ON DUPLICATE KEY UPDATE
         source = VALUES(source),
         last_since = VALUES(last_since),
         updated_at = VALUES(updated_at)',
      IF(
        @has_last_since = 1,
        'INSERT INTO sync_checkpoint_canonical (id, source, last_since, updated_at)
         SELECT
           LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), ''legacy''), 64),
           LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), ''legacy''), 100),
           COALESCE(last_since, ''1900-01-01 00:00:00''),
           COALESCE(updated_at, CURRENT_TIMESTAMP(6))
         FROM sync_checkpoint
         ON DUPLICATE KEY UPDATE
           source = VALUES(source),
           last_since = VALUES(last_since),
           updated_at = VALUES(updated_at)',
        'INSERT INTO sync_checkpoint_canonical (id, source, last_since, updated_at)
         SELECT
           LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), ''legacy''), 64),
           LEFT(COALESCE(NULLIF(TRIM(CAST(id AS CHAR(64))), ''''), ''legacy''), 100),
           COALESCE(last_update, ''1900-01-01 00:00:00''),
           COALESCE(updated_at, CURRENT_TIMESTAMP(6))
         FROM sync_checkpoint
         ON DUPLICATE KEY UPDATE
           source = VALUES(source),
           last_since = VALUES(last_since),
           updated_at = VALUES(updated_at)'
      )
    )
  )
);
PREPARE stmt FROM @copy_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := 'INSERT INTO sync_checkpoint_canonical (id, source, last_since)
             VALUES (''firebird_produtos'', ''firebird_produtos'', ''1900-01-01 00:00:00'')
             ON DUPLICATE KEY UPDATE source = VALUES(source)';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @backup_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'sync_checkpoint_legacy_20260326'
);

SET @rename_sql := IF(
  @is_canonical = 1,
  'DROP TABLE sync_checkpoint_canonical',
  IF(
    @table_exists = 1 AND @backup_exists = 0,
    'RENAME TABLE sync_checkpoint TO sync_checkpoint_legacy_20260326, sync_checkpoint_canonical TO sync_checkpoint',
    IF(
      @table_exists = 1 AND @backup_exists = 1,
      'DROP TABLE sync_checkpoint',
      'SELECT 1'
    )
  )
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @finalize_sql := IF(
  @is_canonical = 1,
  'SELECT 1',
  IF(
    @table_exists = 1 AND @backup_exists = 1,
    'RENAME TABLE sync_checkpoint_canonical TO sync_checkpoint',
    'SELECT 1'
  )
);
PREPARE stmt FROM @finalize_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
