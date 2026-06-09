-- Saneamento seguro da tabela produto
-- Revise antes de executar em producao.
-- Recomendado: executar apos revisar produto_auditoria_dados.sql

START TRANSACTION;

-- 1) Backup logico dos produtos potencialmente afetados por saneamento.
CREATE TABLE IF NOT EXISTS produto_backup_saneamento_manual LIKE produto;

INSERT IGNORE INTO produto_backup_saneamento_manual
SELECT *
FROM produto
WHERE
    nome IS NULL
    OR TRIM(nome) = ''
    OR LOWER(TRIM(nome)) IN (
        'produto',
        'sem nome',
        'novo produto',
        'produto do estoque fisico',
        'teste',
        'item'
    )
    OR categoria IS NULL
    OR TRIM(categoria) = ''
    OR fabricante IS NULL
    OR TRIM(fabricante) = ''
    OR estoque >= 30000
    OR codigo_barras IS NULL
    OR TRIM(codigo_barras) = '';

-- 2) Normalizacao segura de texto e preenchimento minimo.
UPDATE produto
SET
    nome = LEFT(
        CASE
            WHEN LOWER(TRIM(REGEXP_REPLACE(COALESCE(nome, ''), '[[:space:]]+', ' '))) IN (
                'produto',
                'sem nome',
                'novo produto',
                'produto do estoque fisico'
            )
             AND NULLIF(TRIM(REGEXP_REPLACE(COALESCE(descricao, ''), '[[:space:]]+', ' ')), '') IS NOT NULL
                THEN TRIM(REGEXP_REPLACE(COALESCE(descricao, ''), '[[:space:]]+', ' '))
            ELSE COALESCE(
                NULLIF(TRIM(REGEXP_REPLACE(COALESCE(nome, ''), '[[:space:]]+', ' ')), ''),
                NULLIF(TRIM(REGEXP_REPLACE(COALESCE(descricao, ''), '[[:space:]]+', ' ')), ''),
                'Produto'
            )
        END,
        255
    ),
    descricao = LEFT(
        COALESCE(
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(descricao, ''), '[[:space:]]+', ' ')), ''),
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(nome, ''), '[[:space:]]+', ' ')), ''),
            'Produto'
        ),
        1000
    ),
    categoria = LEFT(
        COALESCE(
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(categoria, ''), '[[:space:]]+', ' ')), ''),
            'Sem Categoria'
        ),
        255
    ),
    fabricante = LEFT(
        NULLIF(
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(fabricante, ''), '[[:space:]]+', ' ')), ''),
            'null'
        ),
        128
    ),
    unidade = NULLIF(TRIM(REGEXP_REPLACE(COALESCE(unidade, ''), '[[:space:]]+', ' ')), ''),
    status_sync = NULLIF(TRIM(REGEXP_REPLACE(COALESCE(status_sync, ''), '[[:space:]]+', ' ')), ''),
    validador = NULLIF(TRIM(REGEXP_REPLACE(COALESCE(validador, ''), '[[:space:]]+', ' ')), ''),
    codigo_barras = NULLIF(TRIM(codigo_barras), '');

-- 3) Correcao provisoria de estoque inflado por importacao CSV no padrao x1000.
UPDATE produto
SET estoque = estoque / 1000
WHERE estoque >= 30000
  AND MOD(estoque, 1000) = 0
  AND metodo_leitura_codigo_barras = 'CSV_ESTOQUE'
  AND categoria = 'Estoque fisico';

-- 4) View canonica para pesquisa/manipulacao por GPT.
CREATE OR REPLACE VIEW vw_produto_gpt_catalogo AS
SELECT
    p.id,
    p.legacy_id,
    p.codigo_original,
    p.codigo_barras,
    LEFT(
        CASE
            WHEN LOWER(TRIM(REGEXP_REPLACE(COALESCE(p.nome, ''), '[[:space:]]+', ' '))) IN (
                'produto',
                'sem nome',
                'novo produto',
                'produto do estoque fisico'
            )
             AND NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' ')), '') IS NOT NULL
                THEN TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' '))
            ELSE COALESCE(
                NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.nome, ''), '[[:space:]]+', ' ')), ''),
                NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' ')), ''),
                'Produto'
            )
        END,
        255
    ) AS nome_canonico,
    LEFT(
        COALESCE(
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' ')), ''),
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.nome, ''), '[[:space:]]+', ' ')), ''),
            'Produto'
        ),
        1000
    ) AS descricao_canonica,
    COALESCE(
        NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.categoria, ''), '[[:space:]]+', ' ')), ''),
        'Sem Categoria'
    ) AS categoria_canonica,
    NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.fabricante, ''), '[[:space:]]+', ' ')), '') AS fabricante_canonico,
    p.estoque,
    p.preco_venda,
    p.preco_promocional,
    p.disponivel,
    p.status,
    p.metodo_leitura_codigo_barras,
    p.data_importacao,
    p.publicado_em,
    CONCAT_WS(
        ' ',
        COALESCE(
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.nome, ''), '[[:space:]]+', ' ')), ''),
            NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' ')), ''),
            'Produto'
        ),
        COALESCE(NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.descricao, ''), '[[:space:]]+', ' ')), ''), ''),
        COALESCE(NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.categoria, ''), '[[:space:]]+', ' ')), ''), ''),
        COALESCE(NULLIF(TRIM(REGEXP_REPLACE(COALESCE(p.fabricante, ''), '[[:space:]]+', ' ')), ''), ''),
        COALESCE(NULLIF(TRIM(p.codigo_barras), ''), ''),
        COALESCE(CAST(p.legacy_id AS CHAR), ''),
        COALESCE(CAST(p.codigo_original AS CHAR), '')
    ) AS gpt_search_text
FROM produto p;

COMMIT;

-- Consultas uteis apos o saneamento:
-- SELECT * FROM vw_produto_gpt_catalogo WHERE gpt_search_text LIKE '%dipirona%' LIMIT 50;
-- SELECT * FROM vw_produto_gpt_catalogo WHERE codigo_barras = '7891234567890';
-- SELECT * FROM vw_produto_gpt_catalogo WHERE legacy_id = 12345;
