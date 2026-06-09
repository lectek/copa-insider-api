SET @exists_pdf := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nota_fiscal_confirmacao' AND COLUMN_NAME = 'pdf'
);
SET @has_rows := (SELECT COUNT(*) FROM nota_fiscal_confirmacao);

-- cria coluna
SET @ddl := CASE
  WHEN @exists_pdf = 0 AND @has_rows = 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `pdf` LONGBLOB NOT NULL'
  WHEN @exists_pdf = 0 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `pdf` LONGBLOB NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- se adicionou como NULL, preencher com blob vazio e travar NOT NULL
SET @fix := CASE
  WHEN @exists_pdf = 0 AND @has_rows > 0 THEN 'UPDATE `nota_fiscal_confirmacao` SET `pdf` = x'''' WHERE `pdf` IS NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @fix; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @lock := CASE
  WHEN @exists_pdf = 0 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` MODIFY COLUMN `pdf` LONGBLOB NOT NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @lock; EXECUTE stmt; DEALLOCATE PREPARE stmt;
