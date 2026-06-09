/*
 * Cria tabela de inscrições para notificações de volta ao estoque.
 */
CREATE TABLE IF NOT EXISTS product_stock_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    recipient_email VARCHAR(254) NOT NULL,
    recipient_name VARCHAR(120),
    status VARCHAR(32) NOT NULL DEFAULT 'SUBSCRIBED',
    product_snapshot JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMP NULL,
    CONSTRAINT fk_product_stock_subscription_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);

CREATE UNIQUE INDEX ux_product_stock_subscription_produto_email ON product_stock_subscription(produto_id, recipient_email);
CREATE INDEX idx_product_stock_subscription_status ON product_stock_subscription(status);
CREATE INDEX idx_product_stock_subscription_produto ON product_stock_subscription(produto_id);
