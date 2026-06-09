-- Adiciona coluna last_since para controle de checkpoint do import (idempotente)

SET @db := DATABASE();
SELECT COUNT(*) INTO @cnt_last_since
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'sync_checkpoint'
  AND COLUMN_NAME = 'last_since';

SET @sql_add_last_since := IF(
  @cnt_last_since = 0,
  'ALTER TABLE sync_checkpoint ADD COLUMN last_since DATETIME(6) NULL AFTER updated_at',
  'SELECT 1'
);
PREPARE stmt_add_last_since FROM @sql_add_last_since;
EXECUTE stmt_add_last_since;
DEALLOCATE PREPARE stmt_add_last_since;
