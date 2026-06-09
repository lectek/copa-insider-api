-- 1) Derruba FKs antigas (se existirem)
SET @has_fk1 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_item_pedido__produto'
);
SET @sql := IF(@has_fk1=1,
  'ALTER TABLE item_pedido DROP FOREIGN KEY fk_item_pedido__produto',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_fk2 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_movestoque_produto'
);
SET @sql := IF(@has_fk2=1,
  'ALTER TABLE movimento_estoque DROP FOREIGN KEY fk_movestoque_produto',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2) Cria FKs novas para produtos(produto_id) (se não existirem)
SET @has_new1 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_item_pedido__produtos'
);
SET @sql := IF(@has_new1=0,
  'ALTER TABLE item_pedido
     ADD CONSTRAINT fk_item_pedido__produtos
     FOREIGN KEY (produto_id) REFERENCES produtos(produto_id)',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_new2 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_movestoque_produtos'
);
SET @sql := IF(@has_new2=0,
  'ALTER TABLE movimento_estoque
-- === PROD/V20251101_01__fix_produto_view.sql ===
-- 1) DROP FKs antigas (se existirem), que apontavam para 'produtos'
SET @has_fk1 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_item_pedido__produto'
);
SET @sql := IF(@has_fk1=1,
  'ALTER TABLE item_pedido DROP FOREIGN KEY fk_item_pedido__produto',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_fk2 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_movestoque_produto'
);
SET @sql := IF(@has_fk2=1,
  'ALTER TABLE movimento_estoque DROP FOREIGN KEY fk_movestoque_produto',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2) Adiciona FKs novas para 'produto(id)' (se não existirem)
SET @has_new1 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_item_pedido__produto_id'
);
SET @sql := IF(@has_new1=0,
  'ALTER TABLE item_pedido
     ADD CONSTRAINT fk_item_pedido__produto_id
     FOREIGN KEY (produto_id) REFERENCES produto(id)',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_new2 := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_movestoque__produto_id'
);
SET @sql := IF(@has_new2=0,
  'ALTER TABLE movimento_estoque
     ADD CONSTRAINT fk_movestoque__produto_id
     FOREIGN KEY (produto_id) REFERENCES produto(id)',
  'SELECT 1'
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3) NÃO derrube a tabela 'produto'. Ela é sua tabela física com dados.

-- 4) (Re)cria uma VIEW compatível, SEM DEFINER, usando INVOKER
--    Mapeia colunas existentes de 'produto' e deixa NULL onde não há equivalente
DROP VIEW IF EXISTS produto_view;

CREATE OR REPLACE
SQL SECURITY INVOKER
VIEW produto_view AS
SELECT
  p.id                AS id,
  p.nome              AS nome,
  NULL                AS categoria_id,        -- 'produto' tem 'categoria' (varchar); ajuste aqui se quiser manter string
  NULL                AS fornecedor_id,       -- não existe no schema atual
  p.codigo_barras     AS codigo_barras,
  p.preco_custo       AS preco_anterior,      -- melhor aproximação
  p.preco_venda       AS preco_venda,
  p.preco_promocional AS preco_promocao,
  p.estoque           AS estoque,
  NULL                AS bonus,               -- não existe
  p.unidade           AS apresentacao,        -- aproximação
  NULL                AS inicio_promocao,     -- não existe
  NULL                AS termino_promocao,    -- não existe
  NULL                AS margem_lucro,        -- calcule depois se desejar
  NULL                AS estoque_minimo,      -- não existe
  NULL                AS padrao_comissao_id,  -- não existe
  p.created_at        AS created_at,
  p.updated_at        AS updated_at
FROM produto p;
