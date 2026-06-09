-- V14__ajustar_tipo_data_cadastro.sql

-- Garante que não existam NULLs antes de tornar NOT NULL
UPDATE `produto`
SET `data_cadastro` = CURDATE()
WHERE `data_cadastro` IS NULL;

-- Altera para DATE apenas se ainda não for DATE
SET @precisa_alter := (
  SELECT CASE WHEN DATA_TYPE <> 'date' THEN 1 ELSE 0 END
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'data_cadastro'
);

SET @sql := IF(@precisa_alter = 1,
  'ALTER TABLE `produto` MODIFY COLUMN `data_cadastro` DATE NOT NULL',
  'SELECT 1'
);
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;
