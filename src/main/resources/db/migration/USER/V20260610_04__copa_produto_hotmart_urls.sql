-- Liga os produtos às páginas de checkout do Hotmart
UPDATE copa_produto
SET hotmart_url        = 'https://pay.hotmart.com/J106242736P',
    hotmart_product_id = 7907347,
    ativo              = 1
WHERE slug = 'guia-selecao-portugal';

UPDATE copa_produto
SET hotmart_url        = 'https://pay.hotmart.com/M106250934Y',
    hotmart_product_id = 7909721,
    ativo              = 1
WHERE slug = 'guia-selecao-brasil';
