CREATE TABLE IF NOT EXISTS password_reset_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(120) NOT NULL UNIQUE,
  usuario_id BIGINT NOT NULL,
  expira_em DATETIME(6) NOT NULL,
  usado BIT NOT NULL DEFAULT 0,
  usado_em DATETIME(6) NULL,
  INDEX ix_prt_usuario (usuario_id)
);
