-- ============================================================================
-- V20251017_03__produto_normalize_nullable_defaults_indexes.sql
-- Normalização tolerante a estado (idempotente) da tabela produto
-- - Ajusta nulabilidade e defaults
-- - Converte ENUM -> VARCHAR (status, status_sync) se necessário
-- - Cria índice único em hash_legado (se não existir)
-- - Aplica CHECKs básicos (MySQL 8.0+)
-- - Backfill de dados críticos
-- ============================================================================

SET @schema_name = DATABASE();

-- 0) Garante que a tabela 'produto' existe
SELECT 1 FROM INFORMATION_SCHEMA.TABLES
 WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'produto'
 INTO @has_produto;

-- Se a tabela não existir, encerra sem erro
SET @sql := IF(@has_produto IS NULL, 'SELECT 1', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1) Normalização de colunas: tipos, nulabilidade e defaults (apenas se necessário)
DROP PROCEDURE IF EXISTS sp_produto_normalize_columns;
DELIMITER $$
CREATE PROCEDURE sp_produto_normalize_columns()
BEGIN
  -- Helper: altera coluna se tipo/nulabilidade/default divergir
  -- Observação: Compara COLUMN_TYPE, IS_NULLABLE e COLUMN_DEFAULT

  -- codigo_original BIGINT NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='codigo_original'
      AND (DATA_TYPE <> 'bigint' OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY codigo_original BIGINT NULL;
  END IF;

  -- imagem / imagem_webp VARCHAR(512) NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='imagem'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 512 OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY imagem VARCHAR(512) NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='imagem_webp'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 512 OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY imagem_webp VARCHAR(512) NULL;
  END IF;

  -- unidade VARCHAR(32) NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='unidade'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 32 OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY unidade VARCHAR(32) NULL;
  END IF;

  -- ordem_carrossel INT NULL DEFAULT 0
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='ordem_carrossel'
      AND (DATA_TYPE <> 'int' OR IS_NULLABLE <> 'YES' OR (COLUMN_DEFAULT IS NULL OR COLUMN_DEFAULT <> '0'))
  ) THEN
    ALTER TABLE produto MODIFY ordem_carrossel INT NULL DEFAULT 0;
  END IF;

  -- estoque INT NOT NULL DEFAULT 0
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='estoque'
      AND (DATA_TYPE <> 'int' OR IS_NULLABLE <> 'NO' OR (COLUMN_DEFAULT IS NULL OR COLUMN_DEFAULT <> '0'))
  ) THEN
    ALTER TABLE produto MODIFY estoque INT NOT NULL DEFAULT 0;
  END IF;

  -- preco_custo DECIMAL(10,2) NOT NULL DEFAULT 0.00
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='preco_custo'
      AND (DATA_TYPE <> 'decimal' OR NUMERIC_PRECISION <> 10 OR NUMERIC_SCALE <> 2 OR IS_NULLABLE <> 'NO' OR (COLUMN_DEFAULT IS NULL OR COLUMN_DEFAULT NOT IN ('0.00','0')))
  ) THEN
    ALTER TABLE produto MODIFY preco_custo DECIMAL(10,2) NOT NULL DEFAULT 0.00;
  END IF;

  -- preco_promocional DECIMAL(10,2) NULL (ADD se não existir, senão MODIFY se divergente)
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto' AND COLUMN_NAME='preco_promocional'
  ) THEN
    ALTER TABLE produto ADD COLUMN preco_promocional DECIMAL(10,2) NULL;
  ELSE
    IF EXISTS (
      SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
        AND COLUMN_NAME='preco_promocional'
        AND (DATA_TYPE <> 'decimal' OR NUMERIC_PRECISION <> 10 OR NUMERIC_SCALE <> 2 OR IS_NULLABLE <> 'YES')
    ) THEN
      ALTER TABLE produto MODIFY preco_promocional DECIMAL(10,2) NULL;
    END IF;
  END IF;

  -- preco_venda DECIMAL(10,2) NOT NULL DEFAULT 0.00
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='preco_venda'
      AND (DATA_TYPE <> 'decimal' OR NUMERIC_PRECISION <> 10 OR NUMERIC_SCALE <> 2 OR IS_NULLABLE <> 'NO' OR (COLUMN_DEFAULT IS NULL OR COLUMN_DEFAULT NOT IN ('0.00','0')))
  ) THEN
    ALTER TABLE produto MODIFY preco_venda DECIMAL(10,2) NOT NULL DEFAULT 0.00;
  END IF;

  -- publicado_em DATETIME NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='publicado_em'
      AND (DATA_TYPE NOT IN ('datetime','timestamp') OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY publicado_em DATETIME NULL;
  END IF;

  -- updated_at / created_at DATETIME NOT NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='updated_at'
      AND (DATA_TYPE NOT IN ('datetime','timestamp') OR IS_NULLABLE <> 'NO')
  ) THEN
    ALTER TABLE produto MODIFY updated_at DATETIME NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='created_at'
      AND (DATA_TYPE NOT IN ('datetime','timestamp') OR IS_NULLABLE <> 'NO')
  ) THEN
    ALTER TABLE produto MODIFY created_at DATETIME NOT NULL;
  END IF;

  -- fabricante VARCHAR(128) NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='fabricante'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 128 OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY fabricante VARCHAR(128) NULL;
  END IF;

  -- validador VARCHAR(256) NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='validador'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 256 OR IS_NULLABLE <> 'YES')
  ) THEN
    ALTER TABLE produto MODIFY validador VARCHAR(256) NULL;
  END IF;

  -- hash_legado VARCHAR(128) NOT NULL
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
      AND COLUMN_NAME='hash_legado'
      AND (DATA_TYPE <> 'varchar' OR CHARACTER_MAXIMUM_LENGTH < 128 OR IS_NULLABLE <> 'NO')
  ) THEN
    ALTER TABLE produto MODIFY hash_legado VARCHAR(128) NOT NULL;
  END IF;
END$$
DELIMITER ;
CALL sp_produto_normalize_columns();
DROP PROCEDURE sp_produto_normalize_columns;

-- 2) Converter status/status_sync de ENUM -> VARCHAR(32) (apenas se ainda forem ENUM)
DROP PROCEDURE IF EXISTS sp_produto_convert_enums;
DELIMITER $$
CREATE PROCEDURE sp_produto_convert_enums()
BEGIN
  DECLARE status_is_enum INT DEFAULT 0;
  DECLARE sync_is_enum   INT DEFAULT 0;

  SELECT CASE WHEN DATA_TYPE='enum' THEN 1 ELSE 0 END
    INTO status_is_enum
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto' AND COLUMN_NAME='status'
  LIMIT 1;

  SELECT CASE WHEN DATA_TYPE='enum' THEN 1 ELSE 0 END
    INTO sync_is_enum
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto' AND COLUMN_NAME='status_sync'
  LIMIT 1;

  -- status
  IF status_is_enum = 1 THEN
    ALTER TABLE produto ADD COLUMN status_varchar VARCHAR(32) NULL;
    UPDATE produto SET status_varchar = CAST(status AS CHAR(32));
    ALTER TABLE produto DROP COLUMN status;
    ALTER TABLE produto CHANGE COLUMN status_varchar status VARCHAR(32) NOT NULL;
  END IF;

  -- status_sync
  IF sync_is_enum = 1 THEN
    ALTER TABLE produto ADD COLUMN status_sync_varchar VARCHAR(32) NULL;
    UPDATE produto SET status_sync_varchar = CAST(status_sync AS CHAR(32));
    ALTER TABLE produto DROP COLUMN status_sync;
    ALTER TABLE produto CHANGE COLUMN status_sync_varchar status_sync VARCHAR(32) NOT NULL;
  END IF;
END$$
DELIMITER ;
CALL sp_produto_convert_enums();
DROP PROCEDURE sp_produto_convert_enums;

-- 3) CHECK constraints (somente se ainda não existirem) - MySQL 8.0+

-- Preço/estoque não-negativos
SET @exists_ck1 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_preco_custo_nonneg');
SET @sql := IF(@exists_ck1=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_preco_custo_nonneg CHECK (preco_custo >= 0)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_ck2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_preco_venda_nonneg');
SET @sql := IF(@exists_ck2=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_preco_venda_nonneg CHECK (preco_venda >= 0)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- preco_promocional só se a coluna existir
SET @col_preco_prom := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto' AND COLUMN_NAME='preco_promocional'
);
SET @exists_ck3 := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_preco_promocional_nonneg'
);
SET @sql := IF(@col_preco_prom=1 AND @exists_ck3=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_preco_promocional_nonneg CHECK (preco_promocional IS NULL OR preco_promocional >= 0)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_ck4 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_estoque_nonneg');
SET @sql := IF(@exists_ck4=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_estoque_nonneg CHECK (estoque >= 0)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) Índice único em hash_legado
SET @exists_idx := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='produto'
    AND INDEX_NAME='ux_produto_hash_legado'
);
SET @sql := IF(@exists_idx=0,
  'CREATE UNIQUE INDEX ux_produto_hash_legado ON produto(hash_legado)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5) Backfill de dados
UPDATE produto
SET preco_promocional = preco_venda
WHERE preco_promocional IS NULL AND preco_venda IS NOT NULL;

UPDATE produto
SET validador = COALESCE(validador, fabricante),
    fabricante = NULL
WHERE fabricante LIKE 'Lucro:%';

UPDATE produto
SET unidade = 'UN'
WHERE unidade IS NULL OR unidade = '';

UPDATE produto
SET ordem_carrossel = 0
WHERE ordem_carrossel IS NULL;

UPDATE produto
SET estoque = 0
WHERE estoque IS NULL;

UPDATE produto SET status = 'IMPORTADO'
WHERE status IS NULL OR status NOT IN ('IMPORTADO','ATIVO','INATIVO','RASCUNHO');

UPDATE produto SET status_sync = 'SINCRONIZADO'
WHERE status_sync IS NULL OR status_sync NOT IN ('SINCRONIZADO','PENDENTE','ERRO');

-- 6) (Opcional) CHECKs de domínios de status
SET @exists_ck5 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_status_valid');
SET @sql := IF(@exists_ck5=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_status_valid CHECK (status IN (''IMPORTADO'',''ATIVO'',''INATIVO'',''RASCUNHO''))',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_ck6 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA=@schema_name AND TABLE_NAME='produto' AND CONSTRAINT_NAME='ck_status_sync_valid');
SET @sql := IF(@exists_ck6=0,
  'ALTER TABLE produto ADD CONSTRAINT ck_status_sync_valid CHECK (status_sync IN (''SINCRONIZADO'',''PENDENTE'',''ERRO''))',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Fim

