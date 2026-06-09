SET @db := DATABASE();

SET @sql := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='fiscal_situacao_relatorio'
    )
    THEN 'SELECT 1'
    WHEN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema=@db AND table_name='produto' AND column_name='fiscal_cofins_cst'
    )
    THEN 'ALTER TABLE produto ADD COLUMN fiscal_situacao_relatorio VARCHAR(16) NULL AFTER fiscal_cofins_cst'
    ELSE 'ALTER TABLE produto ADD COLUMN fiscal_situacao_relatorio VARCHAR(16) NULL'
  END
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
