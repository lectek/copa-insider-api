ALTER TABLE pedido_fiscal_snapshot
    ADD COLUMN print_channel VARCHAR(30) NOT NULL DEFAULT 'NONE' AFTER total_amount,
    ADD COLUMN email_delivery_requested BIT(1) NOT NULL DEFAULT b'0' AFTER print_channel,
    ADD COLUMN email_delivery_address VARCHAR(180) NULL AFTER email_delivery_requested;

ALTER TABLE fiscal_document
    ADD COLUMN email_delivery_sent_at DATETIME NULL AFTER danfe_storage_path;
