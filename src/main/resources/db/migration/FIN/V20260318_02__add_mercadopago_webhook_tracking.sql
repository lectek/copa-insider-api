ALTER TABLE mercadopago_seller_connection
  ADD COLUMN last_webhook_received_at TIMESTAMP NULL AFTER revoked_at,
  ADD COLUMN last_webhook_payment_id VARCHAR(80) NULL AFTER last_webhook_received_at;
