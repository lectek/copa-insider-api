-- Garante que Copa em 20 Factos e Histórico do Confronto estão activos
-- (V20260611_08 pode não ter aplicado em todas as instâncias)
UPDATE copa_produto
SET ativo = 1,
    hotmart_url        = 'https://pay.hotmart.com/T106266827K',
    hotmart_product_id = 7915301,
    preco_eur          = 3.90,
    ordem              = 1
WHERE slug = 'copa-em-20-factos';

UPDATE copa_produto
SET ativo = 1,
    hotmart_url        = 'https://pay.hotmart.com/L106252926D',
    hotmart_product_id = 7910328,
    preco_eur          = 1.99,
    ordem              = 4
WHERE slug = 'historico-confronto';
