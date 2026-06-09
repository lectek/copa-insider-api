-- Histórico de movimentos de estoque
CREATE TABLE IF NOT EXISTS movimento_estoque (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  produto_id   BIGINT NOT NULL,
  quantidade   INT NOT NULL,
  tipo         ENUM('ENTRADA','SAIDA') NOT NULL,
  motivo       VARCHAR(255) NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_movestoque_prod
    FOREIGN KEY (produto_id) REFERENCES produtos(produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- MySQL 8 não suporta CREATE INDEX IF NOT EXISTS.
-- Checa se o índice existe; se não, cria.
SET @idx_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'movimento_estoque'
    AND INDEX_NAME = 'idx_movestoque_prod'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_movestoque_prod ON movimento_estoque (produto_id);',
  'DO 0'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
