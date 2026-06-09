CREATE TABLE IF NOT EXISTS cliente_notificacao (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  tipo VARCHAR(40) NULL,
  titulo VARCHAR(120) NULL,
  mensagem VARCHAR(500) NULL,
  lida TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cliente_notificacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_cliente_notificacao_usuario ON cliente_notificacao (usuario_id);
