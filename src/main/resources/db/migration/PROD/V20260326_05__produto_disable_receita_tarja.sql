-- Desativa o controle de receita/tarja para operacao de comercio local.
UPDATE produto
   SET exige_receita = 0,
       tarja_medicacao = NULL;
