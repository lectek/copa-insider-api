-- Liga o produto genérico 'guia-selecao' ao produto Hotmart 8028523
-- Todos os guias de seleção são vendidos pelo mesmo produto Hotmart
-- O código da seleção é capturado por cookie e escolhido após a compra
UPDATE copa_produto
SET hotmart_product_id = '8028523'
WHERE slug = 'guia-selecao';
