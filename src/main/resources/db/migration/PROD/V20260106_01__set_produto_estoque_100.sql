-- Ajuste de estoque para visibilidade na vitrine
UPDATE produto
SET estoque = 100,
    disponivel = COALESCE(disponivel, TRUE);
