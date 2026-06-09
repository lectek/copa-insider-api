-- V22__align_produto_schema.sql
-- Alinha colunas/PK/AI/índice de produto sem usar "ADD COLUMN IF NOT EXISTS"

-- === Helper: adiciona coluna se não existir ===
-- Uso: SET @sql := (SELECT IF(COUNT(*)=0,'ALTER TABLE ... ADD COLUMN ...','SELECT 1') ...); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- id
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN id BIGINT NOT NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='id'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- categoria
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN categoria VARCHAR(100) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='categoria'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- codigo_barras
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN codigo_barras VARCHAR(64) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='codigo_barras'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- codigo_original
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN codigo_original VARCHAR(64) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='codigo_original'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- created_at
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN created_at DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='created_at'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- data_cadastro
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN data_cadastro DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='data_cadastro'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- data_importacao
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN data_importacao DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='data_importacao'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- descricao
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN descricao TEXT NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='descricao'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- despublicado_em
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN despublicado_em DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='despublicado_em'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- destaque_carrossel
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN destaque_carrossel TINYINT(1) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='destaque_carrossel'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- disponivel
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN disponivel TINYINT(1) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='disponivel'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- estoque
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN estoque INT NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='estoque'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- fabricante
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN fabricante VARCHAR(100) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='fabricante'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- hash_legado
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN hash_legado VARCHAR(255) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='hash_legado'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- id_produto_externo
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN id_produto_externo VARCHAR(64) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='id_produto_externo'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- imagem
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN imagem VARCHAR(255) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='imagem'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- legacy_id
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN legacy_id VARCHAR(64) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='legacy_id'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- nome
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN nome VARCHAR(255) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='nome'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ordem_carrossel
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN ordem_carrossel INT NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='ordem_carrossel'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- preco_custo
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN preco_custo DECIMAL(15,2) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='preco_custo'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- preco_venda
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN preco_venda DECIMAL(15,2) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='preco_venda'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- publicado_em
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN publicado_em DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='publicado_em'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- status
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN status VARCHAR(32) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='status'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- status_sync
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN status_sync VARCHAR(32) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='status_sync'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- unidade
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN unidade VARCHAR(32) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='unidade'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updated_at
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN updated_at DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='updated_at'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- validador
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN validador VARCHAR(255) NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='validador'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- version
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD COLUMN version INT NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='version'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- === Garantir AUTO_INCREMENT em id ===
SET @sql := (
  SELECT IF(LOWER(COALESCE(EXTRA,'')) NOT LIKE '%auto_increment%',
    'ALTER TABLE produto MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name='produto' AND column_name='id'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- === Garantir PRIMARY KEY em (id) ===
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE produto ADD PRIMARY KEY (id)',
    'SELECT 1')
  FROM information_schema.table_constraints
  WHERE table_schema = DATABASE() AND table_name='produto' AND constraint_type='PRIMARY KEY'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- === Garantir índice em estoque ===
SET @sql := (
  SELECT IF(COUNT(*)=0,
    'CREATE INDEX idx_produto_estoque ON produto (estoque)',
    'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name='produto' AND index_name='idx_produto_estoque'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
