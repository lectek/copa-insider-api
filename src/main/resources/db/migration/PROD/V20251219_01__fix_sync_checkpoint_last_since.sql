-- Ensure sync_checkpoint has last_since column (idempotent, MySQL 8+)

-- Create table if it does not exist (minimal schema aligned with JPA)
CREATE TABLE IF NOT EXISTS sync_checkpoint (
  id VARCHAR(64) PRIMARY KEY,
  source VARCHAR(100) NOT NULL UNIQUE,
  last_since DATETIME(6) NULL,
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
               ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Add last_since if missing
SET @db := DATABASE();
SELECT COUNT(*) INTO @cnt_last_since
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'sync_checkpoint'
  AND COLUMN_NAME = 'last_since';

SET @sql_add_last_since := IF(
  @cnt_last_since = 0,
  'ALTER TABLE sync_checkpoint ADD COLUMN last_since DATETIME(6) NULL',
  'SELECT 1'
);
PREPARE stmt_add_last_since FROM @sql_add_last_since;
EXECUTE stmt_add_last_since;
DEALLOCATE PREPARE stmt_add_last_since;

-- If last_update exists, backfill last_since when null
SELECT COUNT(*) INTO @cnt_last_update
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'sync_checkpoint'
  AND COLUMN_NAME = 'last_update';

SET @sql_backfill := IF(
  @cnt_last_update > 0,
  'UPDATE sync_checkpoint SET last_since = COALESCE(last_since, last_update)',
  'SELECT 1'
);
PREPARE stmt_backfill FROM @sql_backfill;
EXECUTE stmt_backfill;
DEALLOCATE PREPARE stmt_backfill;
