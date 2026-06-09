-- Adiciona classificacao de tarja e regra de receita em produto.
SET @schema := DATABASE();

SET @x := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'tarja_medicacao'
);
SET @sql := IF(
  @x = 0,
  'ALTER TABLE produto ADD COLUMN tarja_medicacao VARCHAR(32) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @x := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'exige_receita'
);
SET @sql := IF(
  @x = 0,
  'ALTER TABLE produto ADD COLUMN exige_receita TINYINT(1) NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE produto
   SET exige_receita = 0
 WHERE exige_receita IS NULL;

SET @has_categoria_table := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @schema
    AND TABLE_NAME = 'produto_categoria'
);
SET @sql := IF(
  @has_categoria_table = 1,
  'INSERT INTO produto_categoria (nome) VALUES (''Medicacoes'')
     ON DUPLICATE KEY UPDATE nome = nome',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
