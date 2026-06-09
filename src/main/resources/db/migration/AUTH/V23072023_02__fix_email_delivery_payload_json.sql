ALTER TABLE email_delivery
  MODIFY COLUMN payload_json JSON NOT NULL;

-- opcional: se quiser ampliar erro textual
ALTER TABLE email_delivery
  MODIFY COLUMN last_error TEXT NULL;
