-- Define URLs e ativa os produtos incondicionalmente (idempotente)
-- Necessário caso V20260611_02 e V20260611_03 não tenham corrido no Railway

UPDATE copa_produto SET
    hotmart_url        = 'https://pay.hotmart.com/G106266908X',
    hotmart_product_id = 7915316,
    preco_eur          = 7.99,
    ativo              = 1,
    ordem              = 0
WHERE slug = 'copa-pass';

UPDATE copa_produto SET
    hotmart_url        = 'https://pay.hotmart.com/T106266827K',
    hotmart_product_id = 7915301,
    preco_eur          = 3.90,
    ativo              = 1,
    ordem              = 1
WHERE slug = 'copa-em-20-factos';

UPDATE copa_produto SET
    hotmart_url        = 'https://pay.hotmart.com/J106242736P',
    hotmart_product_id = 7907347,
    preco_eur          = 3.99,
    ativo              = 1,
    ordem              = 2
WHERE slug = 'guia-selecao-portugal';

UPDATE copa_produto SET
    hotmart_url        = 'https://pay.hotmart.com/M106250934Y',
    hotmart_product_id = 7909721,
    preco_eur          = 3.99,
    ativo              = 1,
    ordem              = 3
WHERE slug = 'guia-selecao-brasil';

UPDATE copa_produto SET
    hotmart_url        = 'https://pay.hotmart.com/L106252926D',
    hotmart_product_id = 7910328,
    preco_eur          = 1.99,
    ativo              = 1,
    ordem              = 4
WHERE slug = 'historico-confronto';
