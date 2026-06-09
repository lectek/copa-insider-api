CREATE TABLE IF NOT EXISTS produto_categoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_produto_categoria_nome (nome)
) ENGINE=InnoDB;

INSERT INTO produto_categoria (nome) VALUES ('Sem Categoria')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Analgesicos e antitermicos')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Antigripais')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Alergia e antialergicos')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Digestivo e antiacidos')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Vitaminas e suplementos')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Cuidados com a pele')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Higiene pessoal')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Bebes e mamas')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Primeiros socorros')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Saude intima')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Ortopedia')
  ON DUPLICATE KEY UPDATE nome = nome;
INSERT INTO produto_categoria (nome) VALUES ('Fitness e bem-estar')
  ON DUPLICATE KEY UPDATE nome = nome;
