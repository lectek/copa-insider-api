CREATE TABLE IF NOT EXISTS cliente_favorito (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  produto_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cliente_favorito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT fk_cliente_favorito_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
  CONSTRAINT uk_cliente_favorito UNIQUE (usuario_id, produto_id)
);
