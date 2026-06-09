-- MySQL 8+ • Schema inicial (tabelas + PKs/FKs + unicidades mínimas)

CREATE TABLE IF NOT EXISTS usuario (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome              VARCHAR(255)       NOT NULL,
  email             VARCHAR(255)       NOT NULL,
  cpf               VARCHAR(14)        NOT NULL,
  senha             VARCHAR(255)       NOT NULL,
  tentativas_falhas INT                NOT NULL DEFAULT 0,
  cliente_vip       TINYINT(1)         NOT NULL DEFAULT 0,
  ultimo_acesso     DATETIME           NULL,
  criado_em         TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em     TIMESTAMP          NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_usuario_email UNIQUE (email),
  CONSTRAINT uk_usuario_cpf   UNIQUE (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS usuario_roles (
  usuario_id BIGINT      NOT NULL,
  role       VARCHAR(50) NOT NULL,
  CONSTRAINT pk_usuario_roles PRIMARY KEY (usuario_id, role),
  CONSTRAINT fk_usuario_roles_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cliente (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome      VARCHAR(255) NOT NULL,
  email     VARCHAR(255) NOT NULL,
  telefone  VARCHAR(30)  NULL,
  cpf       VARCHAR(14)  NOT NULL,
  senha     VARCHAR(255) NOT NULL,
  criado_em TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_cliente_email UNIQUE (email),
  CONSTRAINT uk_cliente_cpf   UNIQUE (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS produto (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(255)  NOT NULL,
  descricao      TEXT          NULL,
  preco_venda    DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  imagem         VARCHAR(255)  NULL,
  categoria      VARCHAR(100)  NULL,
  codigo_barras  VARCHAR(64)   NULL,
  preco_custo    DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  estoque        INT           NOT NULL DEFAULT 0,
  disponivel     TINYINT(1)    NOT NULL DEFAULT 1,
  fabricante     VARCHAR(100)  NULL,
  codigo_original VARCHAR(64)  NULL,
  unidade        VARCHAR(10)   NULL,
  data_cadastro  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_produto_cod_barras UNIQUE (codigo_barras)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pedido (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_id     BIGINT       NOT NULL,
  data           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total          DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  status         VARCHAR(20)  NOT NULL,
  tipo_pagamento VARCHAR(20)  NOT NULL,
  metodo_pagamento VARCHAR(80) NULL,
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
    REFERENCES cliente(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS item_pedido (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  pedido_id     BIGINT       NOT NULL,
  produto_id    BIGINT       NOT NULL,
  quantidade    INT          NOT NULL,
  preco_unitario DECIMAL(19,2) NOT NULL,
  subtotal      DECIMAL(19,2) NOT NULL,
  CONSTRAINT fk_item_pedido__pedido FOREIGN KEY (pedido_id)
    REFERENCES pedido(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_item_pedido__produto FOREIGN KEY (produto_id)
    REFERENCES produto(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT uk_item_pedido__pedido_produto UNIQUE (pedido_id, produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
