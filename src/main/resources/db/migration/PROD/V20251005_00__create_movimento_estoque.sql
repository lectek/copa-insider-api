-- V20251005_00__create_movimento_estoque.sql
CREATE TABLE IF NOT EXISTS movimento_estoque (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  produto_id   BIGINT NOT NULL,
  tipo         ENUM('ENTRADA','SAIDA') NOT NULL,
  quantidade   INT NOT NULL CHECK (quantidade > 0),
  criado_em    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_movestoque_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
  INDEX idx_mov_produto (produto_id)
) ENGINE=InnoDB;
