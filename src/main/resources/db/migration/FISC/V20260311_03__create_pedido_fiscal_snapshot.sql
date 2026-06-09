CREATE TABLE IF NOT EXISTS pedido_fiscal_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  pedido_id BIGINT NOT NULL,
  source VARCHAR(40) NOT NULL,
  suggested_document_model VARCHAR(20) NOT NULL,
  recipient_name VARCHAR(160) NULL,
  recipient_document VARCHAR(20) NULL,
  recipient_email VARCHAR(150) NULL,
  recipient_phone VARCHAR(25) NULL,
  recipient_address VARCHAR(255) NULL,
  issuer_cnpj VARCHAR(14) NULL,
  payment_method VARCHAR(120) NULL,
  shipping_amount DECIMAL(19, 2) NULL,
  total_amount DECIMAL(19, 2) NOT NULL,
  payload_json LONGTEXT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pedido_fiscal_snapshot_pedido (pedido_id),
  KEY idx_pedido_fiscal_snapshot_source (source),
  CONSTRAINT fk_pedido_fiscal_snapshot_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedido (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
