-- Evita warnings
SET @old_sql_notes := @@sql_notes; 
SET sql_notes = 0;

-- Remover se existirem (sem erro)
DROP VIEW IF EXISTS vw_produto;
DROP VIEW IF EXISTS produto_view;
DROP VIEW IF EXISTS vw_produto_detalhe;

-- View canônica (sem DEFINER, rodando com permissões de quem consulta)
CREATE OR REPLACE ALGORITHM=MERGE SQL SECURITY INVOKER VIEW vw_produto AS
SELECT
    p.id,
    p.nome,
    p.descricao,
    -- Alias de compatibilidade: onde o código esperar "preco", entregamos preco_venda
    p.preco_venda          AS preco,
    p.preco_venda,
    p.preco_promocional,
    p.imagem,
    p.categoria,
    p.codigo_barras,
    p.preco_custo,
    p.estoque,
    p.disponivel,
    p.fabricante,
    p.codigo_original,
    p.unidade,
    p.data_cadastro,
    p.data_importacao,
    p.id_produto_externo,
    p.created_at,
    p.updated_at,
    p.version,
    p.despublicado_em,
    p.destaque_carrossel,
    p.hash_legado,
    p.legacy_id,
    p.ordem_carrossel,
    p.publicado_em,
    p.status,        -- VARCHAR(32) (migrado em 20251017.04/20251021)
    p.status_sync,
    p.validador
FROM produto p;

-- Alias histórico, se o código apontar pra esse nome
CREATE OR REPLACE ALGORITHM=MERGE SQL SECURITY INVOKER VIEW produto_view AS
SELECT * FROM vw_produto;

-- Versão "detalhe": inclui campos úteis e derivados comuns (ajuste conforme precisar)
CREATE OR REPLACE ALGORITHM=MERGE SQL SECURITY INVOKER VIEW vw_produto_detalhe AS
SELECT
    p.id,
    p.nome,
    p.descricao,
    p.preco_venda          AS preco,
    p.preco_venda,
    p.preco_promocional,
    -- Derivados práticos
    CASE
        WHEN p.preco_promocional IS NOT NULL AND p.preco_promocional < p.preco_venda THEN 1
        ELSE 0
    END AS em_promocao,
    CASE
        WHEN p.preco_promocional IS NOT NULL AND p.preco_promocional < p.preco_venda THEN p.preco_promocional
        ELSE p.preco_venda
    END AS preco_final,
    p.imagem,
    p.categoria,
    p.codigo_barras,
    p.estoque,
    p.disponivel,
    p.fabricante,
    p.unidade,
    p.data_cadastro,
    p.publicado_em,
    p.despublicado_em,
    p.status,
    p.status_sync,
    p.created_at,
    p.updated_at,
    p.version,
    p.validador,
    p.hash_legado,
    p.legacy_id
FROM produto p;

-- Restaura notes
SET sql_notes = @old_sql_notes;
