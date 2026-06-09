-- ============================================================
-- V23072023_25__produto_normalize_unico_index.sql
-- Normalização da tabela 'produto' para evitar duplicidades
-- e garantir compatibilidade com JPA (MySQL 8.x)
-- ============================================================

-- 1) Ajustes de tipos compatíveis e seguros (sem encolher colunas)
ALTER TABLE produto
  MODIFY COLUMN nome            VARCHAR(255)  NOT NULL,
  MODIFY COLUMN descricao       TEXT          NULL,
  MODIFY COLUMN preco_venda     DECIMAL(19,2) NOT NULL,
  MODIFY COLUMN codigo_barras   VARCHAR(64)   NULL,
  MODIFY COLUMN preco_custo     DECIMAL(19,2) NOT NULL,
  MODIFY COLUMN unidade         VARCHAR(20)   NULL,
  MODIFY COLUMN codigo_original BIGINT        NULL,
  MODIFY COLUMN data_cadastro   DATETIME      NOT NULL;

-- 2) Adicionar colunas que podem faltar (idempotente)
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='produto' AND COLUMN_NAME='id_produto_externo');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN id_produto_externo BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='produto' AND COLUMN_NAME='created_at');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='produto' AND COLUMN_NAME='updated_at');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='produto' AND COLUMN_NAME='version');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3) Garantir que só exista UM índice único válido para codigo_barras
-- 3.1 Cria o índice padronizado se não existir
SET @has := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
             WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='produto' AND INDEX_NAME='uk_produto_cod_barras');
SET @sql := IF(@has=0, 'CREATE UNIQUE INDEX uk_produto_cod_barras ON produto(codigo_barras)', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3.2 Remove o índice duplicado (nome antigo) se existir
SET @has_old := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='produto' AND INDEX_NAME='uk_produto_codigo_barras');
SET @sql := IF(@has_old>0, 'DROP INDEX uk_produto_codigo_barras ON produto', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================
-- ✔️ Este script é seguro para reexecuções (idempotente).
--    Nenhuma coluna ou índice será recriado caso já exista.
-- ============================================================
