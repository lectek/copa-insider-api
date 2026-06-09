-- V23__produto_add_status_validador.sql
-- Idempotente p/ MySQL 8 (sem ADD COLUMN IF NOT EXISTS)

SET @db := DATABASE();

-- ------- status --------
SET @sql := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='status'
    )
    THEN 'SELECT 1'  -- já existe
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='publicado_em'
    )
    THEN 'ALTER TABLE produto ADD COLUMN status VARCHAR(32) NULL AFTER publicado_em'
    ELSE 'ALTER TABLE produto ADD COLUMN status VARCHAR(32) NULL'
  END
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ------- validador --------
SET @sql := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='validador'
    )
    THEN 'SELECT 1'  -- já existe
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='status'
    )
    THEN 'ALTER TABLE produto ADD COLUMN validador VARCHAR(255) NULL AFTER status'
    ELSE 'ALTER TABLE produto ADD COLUMN validador VARCHAR(255) NULL'
  END
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
