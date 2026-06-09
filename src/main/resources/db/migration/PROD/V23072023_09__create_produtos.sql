-- V23072023_09__create_produtos.sql
-- Ajustada para não criar índices (que já foram criados manualmente) e evitar erro de sintaxe no MySQL.

CREATE TABLE IF NOT EXISTS produtos (
  produto_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  produto VARCHAR(255) NOT NULL,
  categoria_id BIGINT NULL,
  fornecedor_id BIGINT NULL,
  cod_barras VARCHAR(64) NULL,
  preco_anterior DECIMAL(10,2) NULL,
  prod_prvenda DECIMAL(10,2) NOT NULL,
  prod_prpromocao DECIMAL(10,2) NULL,
  prod_saldo INT DEFAULT 0,
  bonus VARCHAR(255) NULL,
  apresentacao VARCHAR(255) NULL,
  inicio_promocao DATE NULL,
  termino_promocao DATE NULL,
  margem_lucro DECIMAL(5,2) NULL,
  prod_estminimo INT DEFAULT 0,
  padrao_comissao_id BIGINT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Índices removidos desta migration para evitar IF NOT EXISTS inválido no MySQL.
