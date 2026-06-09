-- Se o campo @Version na entidade for Long/long:
ALTER TABLE item_pedido
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- (alternativa) Se for Integer/int, use isto em vez de BIGINT:
-- ALTER TABLE item_pedido
--   ADD COLUMN version INT NOT NULL DEFAULT 0;
