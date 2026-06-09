-- há linhas na tabela?
SET @has_rows := (SELECT COUNT(*) FROM nota_fiscal_confirmacao);

-- ===== nome =====
SET @missing_nome := (
  SELECT COUNT(*) = 0 FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nota_fiscal_confirmacao' AND COLUMN_NAME = 'nome'
);
SET @ddl := CASE
  WHEN @missing_nome = 1 AND @has_rows = 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `nome` VARCHAR(150) NOT NULL'
  WHEN @missing_nome = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `nome` VARCHAR(150) NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- se adicionou como NULL, preencher e travar como NOT NULL
SET @fix := CASE
  WHEN @missing_nome = 1 AND @has_rows > 0 THEN 'UPDATE `nota_fiscal_confirmacao` SET `nome` = '''' WHERE `nome` IS NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @fix; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @lock := CASE
  WHEN @missing_nome = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` MODIFY COLUMN `nome` VARCHAR(150) NOT NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @lock; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== preferencia =====
SET @missing_pref := (
  SELECT COUNT(*) = 0 FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nota_fiscal_confirmacao' AND COLUMN_NAME = 'preferencia'
);
SET @ddl := CASE
  WHEN @missing_pref = 1 AND @has_rows = 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `preferencia` VARCHAR(50) NOT NULL'
  WHEN @missing_pref = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `preferencia` VARCHAR(50) NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fix := CASE
  WHEN @missing_pref = 1 AND @has_rows > 0 THEN 'UPDATE `nota_fiscal_confirmacao` SET `preferencia` = '''' WHERE `preferencia` IS NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @fix; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @lock := CASE
  WHEN @missing_pref = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` MODIFY COLUMN `preferencia` VARCHAR(50) NOT NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @lock; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== email =====
SET @missing_email := (
  SELECT COUNT(*) = 0 FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nota_fiscal_confirmacao' AND COLUMN_NAME = 'email'
);
SET @ddl := CASE
  WHEN @missing_email = 1 AND @has_rows = 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `email` VARCHAR(180) NOT NULL'
  WHEN @missing_email = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` ADD COLUMN `email` VARCHAR(180) NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fix := CASE
  WHEN @missing_email = 1 AND @has_rows > 0 THEN 'UPDATE `nota_fiscal_confirmacao` SET `email` = '''' WHERE `email` IS NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @fix; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @lock := CASE
  WHEN @missing_email = 1 AND @has_rows > 0 THEN 'ALTER TABLE `nota_fiscal_confirmacao` MODIFY COLUMN `email` VARCHAR(180) NOT NULL'
  ELSE 'SELECT 1'
END;
PREPARE stmt FROM @lock; EXECUTE stmt; DEALLOCATE PREPARE stmt;
