-- V15__ajustar_produto_para_jpa.sql
-- Compatível com MySQL 8.x e Flyway. Não usa "ADD COLUMN IF NOT EXISTS".

/* 1) Cria a tabela completa (para ambientes vazios) */
CREATE TABLE IF NOT EXISTS produto (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_produto_externo  BIGINT NULL,
  nome                VARCHAR(150)  NOT NULL,
  descricao           VARCHAR(255)  NULL,
  preco_venda         DECIMAL(10,2) NOT NULL,
  imagem              VARCHAR(255)  NULL,
  categoria           VARCHAR(100)  NULL,
  codigo_barras       VARCHAR(50)   NULL,
  preco_custo         DECIMAL(10,2) NULL,
  estoque             INT           NOT NULL DEFAULT 0,
  disponivel          TINYINT(1)    NOT NULL DEFAULT 1,
  fabricante          VARCHAR(100)  NULL,
  codigo_original     BIGINT        NULL,
  unidade             VARCHAR(20)   NULL,
  data_cadastro       DATE          NOT NULL,
  created_at          TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  version             BIGINT        NOT NULL DEFAULT 0,
  UNIQUE KEY uk_produto_codigo_barras (codigo_barras)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/* 2) Ajustes de tipos/tamanhos (idempotente) */
ALTER TABLE produto
  MODIFY COLUMN nome            VARCHAR(150)  NOT NULL,
  MODIFY COLUMN descricao       VARCHAR(255)  NULL,
  MODIFY COLUMN preco_venda     DECIMAL(10,2) NOT NULL,
  MODIFY COLUMN codigo_barras   VARCHAR(50)   NULL,
  MODIFY COLUMN preco_custo     DECIMAL(10,2) NULL,
  MODIFY COLUMN unidade         VARCHAR(20)   NULL,
  MODIFY COLUMN codigo_original BIGINT        NULL,
  MODIFY COLUMN data_cadastro   DATE          NOT NULL;

/* 3) Adiciona colunas que podem faltar (sem IF NOT EXISTS) */
-- id_produto_externo
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'id_produto_externo');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN id_produto_externo BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- created_at
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'created_at');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updated_at
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'updated_at');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- version
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND COLUMN_NAME = 'version');
SET @sql := IF(@x=0, 'ALTER TABLE produto ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* 4) Índice único de código de barras (se faltar) */
SET @x := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produto' AND INDEX_NAME = 'uk_produto_codigo_barras');
SET @sql := IF(@x=0, 'CREATE UNIQUE INDEX uk_produto_codigo_barras ON produto(codigo_barras)', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
