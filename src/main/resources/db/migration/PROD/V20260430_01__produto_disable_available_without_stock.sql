UPDATE produto
   SET disponivel = FALSE,
       status = 'IMPORTADO',
       despublicado_em = COALESCE(despublicado_em, CURRENT_TIMESTAMP)
 WHERE disponivel = TRUE
   AND (estoque IS NULL OR estoque <= 0);
