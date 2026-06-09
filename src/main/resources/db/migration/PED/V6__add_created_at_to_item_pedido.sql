-- Audit / timestamps para item_pedido
ALTER TABLE item_pedido
  ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- (Opcional, se sua entidade também tiver updatedAt)
-- ALTER TABLE item_pedido
--   ADD COLUMN updated_at TIMESTAMP(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6);
