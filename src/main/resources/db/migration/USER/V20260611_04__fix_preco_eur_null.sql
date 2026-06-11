-- Garante que todos os produtos têm preco_eur definido
-- Produtos sem preco_eur explícito ficam com conversão BRL/6.20 arredondada a 2 casas
UPDATE copa_produto
SET preco_eur = ROUND(preco / 6.20, 2)
WHERE preco_eur IS NULL;
