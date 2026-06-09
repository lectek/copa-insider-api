-- Copia valores antigos para as novas colunas, se estiverem nulas
UPDATE cliente
SET
  created_at = COALESCE(created_at, CAST(criado_em AS DATETIME)),
  updated_at = COALESCE(updated_at, CAST(atualizado_em AS DATETIME));

-- (Opcional) Se quiser forçar NOT NULL em updated_at:
-- ALTER TABLE cliente MODIFY updated_at DATETIME NOT NULL
--   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Quando o código já estiver usando apenas created_at/updated_at,
-- remova as colunas antigas:
ALTER TABLE cliente
  DROP COLUMN criado_em,
  DROP COLUMN atualizado_em;
