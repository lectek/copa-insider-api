-- Desativa produtos sem URL de checkout real (duplicados e "em breve" sem plano)
-- Os 5 produtos principais já tiveram hotmart_url definido nas migrations anteriores
UPDATE copa_produto
SET ativo = 0
WHERE hotmart_url IS NULL
   OR hotmart_url = '#';
