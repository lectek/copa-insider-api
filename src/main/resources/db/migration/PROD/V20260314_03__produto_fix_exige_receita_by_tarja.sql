-- Garante consistencia entre tarja e exige_receita para produtos de medicacao.

UPDATE produto
   SET exige_receita = 1
 WHERE tarja_medicacao IN ('TARJA_VERMELHA', 'TARJA_PRETA')
   AND (exige_receita IS NULL OR exige_receita <> 1);

UPDATE produto
   SET exige_receita = 0
 WHERE tarja_medicacao = 'SEM_TARJA'
   AND (exige_receita IS NULL OR exige_receita <> 0);
