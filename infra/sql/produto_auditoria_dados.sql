-- Auditoria de qualidade de dados da tabela produto
-- Execute em blocos, revisando os resultados antes de qualquer correcao.

-- 1) Resumo geral
SELECT
    COUNT(*) AS total_produtos,
    SUM(CASE WHEN nome IS NULL OR TRIM(nome) = '' THEN 1 ELSE 0 END) AS sem_nome,
    SUM(CASE WHEN categoria IS NULL OR TRIM(categoria) = '' THEN 1 ELSE 0 END) AS sem_categoria,
    SUM(CASE WHEN codigo_barras IS NULL OR TRIM(codigo_barras) = '' THEN 1 ELSE 0 END) AS sem_codigo_barras,
    SUM(CASE WHEN preco_venda IS NULL OR preco_venda <= 0 THEN 1 ELSE 0 END) AS sem_preco_venda,
    SUM(CASE WHEN estoque IS NULL THEN 1 ELSE 0 END) AS estoque_nulo,
    SUM(CASE WHEN estoque < 0 THEN 1 ELSE 0 END) AS estoque_negativo,
    SUM(CASE WHEN estoque >= 30000 THEN 1 ELSE 0 END) AS estoque_suspeito_30k,
    SUM(CASE WHEN imagem IS NULL OR TRIM(imagem) = '' THEN 1 ELSE 0 END) AS sem_imagem
FROM produto;

-- 2) Duplicidade por legacy_id
SELECT
    legacy_id,
    COUNT(*) AS qtd,
    GROUP_CONCAT(id ORDER BY id ASC) AS ids,
    GROUP_CONCAT(nome ORDER BY id ASC SEPARATOR ' | ') AS nomes
FROM produto
WHERE legacy_id IS NOT NULL
GROUP BY legacy_id
HAVING COUNT(*) > 1
ORDER BY qtd DESC, legacy_id ASC;

-- 3) Duplicidade por codigo_original
SELECT
    codigo_original,
    COUNT(*) AS qtd,
    GROUP_CONCAT(id ORDER BY id ASC) AS ids,
    GROUP_CONCAT(nome ORDER BY id ASC SEPARATOR ' | ') AS nomes
FROM produto
WHERE codigo_original IS NOT NULL
GROUP BY codigo_original
HAVING COUNT(*) > 1
ORDER BY qtd DESC, codigo_original ASC;

-- 4) Possiveis duplicados por nome normalizado
WITH nomes AS (
    SELECT
        id,
        nome,
        codigo_barras,
        legacy_id,
        categoria,
        fabricante,
        LOWER(TRIM(REGEXP_REPLACE(COALESCE(nome, ''), '[[:space:]]+', ' '))) AS nome_norm
    FROM produto
    WHERE nome IS NOT NULL
      AND TRIM(nome) <> ''
)
SELECT
    nome_norm,
    COUNT(*) AS qtd,
    GROUP_CONCAT(id ORDER BY id ASC) AS ids,
    GROUP_CONCAT(
        CONCAT(
            '#', id,
            ' [cb=', COALESCE(codigo_barras, '-'),
            ', legacy=', COALESCE(CAST(legacy_id AS CHAR), '-'),
            ', cat=', COALESCE(categoria, '-'),
            ', fab=', COALESCE(fabricante, '-'),
            '] ', nome
        )
        ORDER BY id ASC SEPARATOR ' | '
    ) AS amostras
FROM nomes
WHERE nome_norm NOT IN ('produto', 'sem nome', 'novo produto', 'produto do estoque fisico')
GROUP BY nome_norm
HAVING COUNT(*) > 1
ORDER BY qtd DESC, nome_norm ASC;

-- 5) Nomes suspeitos para revisao humana
SELECT
    id,
    legacy_id,
    codigo_barras,
    nome,
    descricao,
    categoria,
    fabricante,
    metodo_leitura_codigo_barras,
    estoque,
    preco_venda
FROM produto
WHERE nome IS NULL
   OR TRIM(nome) = ''
   OR LOWER(TRIM(nome)) IN (
        'produto',
        'sem nome',
        'novo produto',
        'produto do estoque fisico',
        'teste',
        'item'
   )
   OR CHAR_LENGTH(TRIM(nome)) < 4
   OR TRIM(nome) NOT REGEXP '[[:alpha:]]'
   OR nome REGEXP '<[^>]+>'
   OR nome REGEXP '\\.(jpg|jpeg|png|webp|pdf)$'
   OR nome REGEXP '^[0-9 ./,+-]+$'
ORDER BY id DESC;

-- 6) Produtos com baixa pesquisabilidade para GPT
SELECT
    id,
    legacy_id,
    codigo_barras,
    nome,
    descricao,
    categoria,
    fabricante,
    metodo_leitura_codigo_barras
FROM produto
WHERE (codigo_barras IS NULL OR TRIM(codigo_barras) = '')
  AND legacy_id IS NULL
  AND (
        nome IS NULL
        OR TRIM(nome) = ''
        OR LOWER(TRIM(nome)) IN (
            'produto',
            'sem nome',
            'novo produto',
            'produto do estoque fisico'
        )
      )
ORDER BY id DESC;

-- 7) Variacoes desnecessarias de categoria
SELECT
    LOWER(TRIM(REGEXP_REPLACE(COALESCE(categoria, ''), '[[:space:]]+', ' '))) AS categoria_norm,
    COUNT(*) AS qtd_registros,
    COUNT(DISTINCT categoria) AS qtd_variantes,
    GROUP_CONCAT(DISTINCT categoria ORDER BY categoria ASC SEPARATOR ' | ') AS variantes
FROM produto
WHERE categoria IS NOT NULL
  AND TRIM(categoria) <> ''
GROUP BY LOWER(TRIM(REGEXP_REPLACE(COALESCE(categoria, ''), '[[:space:]]+', ' ')))
HAVING COUNT(DISTINCT categoria) > 1
ORDER BY qtd_variantes DESC, qtd_registros DESC;

-- 8) Variacoes desnecessarias de fabricante
SELECT
    LOWER(TRIM(REGEXP_REPLACE(COALESCE(fabricante, ''), '[[:space:]]+', ' '))) AS fabricante_norm,
    COUNT(*) AS qtd_registros,
    COUNT(DISTINCT fabricante) AS qtd_variantes,
    GROUP_CONCAT(DISTINCT fabricante ORDER BY fabricante ASC SEPARATOR ' | ') AS variantes
FROM produto
WHERE fabricante IS NOT NULL
  AND TRIM(fabricante) <> ''
GROUP BY LOWER(TRIM(REGEXP_REPLACE(COALESCE(fabricante, ''), '[[:space:]]+', ' ')))
HAVING COUNT(DISTINCT fabricante) > 1
ORDER BY qtd_variantes DESC, qtd_registros DESC;

-- 9) Produtos publicados em estado inconsistente
SELECT
    id,
    nome,
    status,
    disponivel,
    estoque,
    preco_venda,
    imagem,
    metodo_leitura_codigo_barras,
    categoria
FROM produto
WHERE status = 'PUBLICADO'
  AND (
        disponivel IS NULL
        OR disponivel = 0
        OR estoque IS NULL
        OR estoque <= 0
        OR preco_venda IS NULL
        OR preco_venda <= 0
        OR imagem IS NULL
        OR TRIM(imagem) = ''
      )
ORDER BY id DESC;

-- 10) Estoque fisico ainda suspeito
SELECT
    id,
    legacy_id,
    codigo_barras,
    nome,
    estoque,
    metodo_leitura_codigo_barras,
    categoria
FROM produto
WHERE estoque >= 30000
  AND metodo_leitura_codigo_barras = 'CSV_ESTOQUE'
  AND categoria = 'Estoque fisico'
ORDER BY estoque DESC, id DESC;
