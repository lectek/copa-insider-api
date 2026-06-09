-- Usuários locais (login/senha) + coleção de roles (ElementCollection)

CREATE TABLE IF NOT EXISTS usuario (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  nome              VARCHAR(120)  NOT NULL,
  email             VARCHAR(150)  NOT NULL,
  cpf               VARCHAR(14)   NULL,
  senha             VARCHAR(255)  NOT NULL,
  ultimo_acesso     DATETIME      NULL,
  cliente_vip       TINYINT(1)    NOT NULL DEFAULT 0,
  tentativas_falhas INT           NOT NULL DEFAULT 0,
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version           BIGINT        NOT NULL DEFAULT 0,
  CONSTRAINT uk_usuario_email UNIQUE (email),
  CONSTRAINT uk_usuario_cpf   UNIQUE (cpf)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario_roles (
  usuario_id BIGINT      NOT NULL,
  role       VARCHAR(50) NOT NULL,
  PRIMARY KEY (usuario_id, role),
  CONSTRAINT fk_usuario_roles_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- ✅ Garantir que cpf aceite NULL (caso alguma versão anterior tenha criado como NOT NULL)
-- (Se já for NULL, esse MODIFY é no-op)
ALTER TABLE usuario
  MODIFY COLUMN cpf VARCHAR(14) NULL;

-- 👤 Usuário ADMIN inicial (senha: admin) -> troque depois!
-- Observação: explicitamos cpf=NULL para não quebrar quando cpf for NOT NULL em versões antigas
INSERT INTO usuario (nome, email, senha, cliente_vip, tentativas_falhas, cpf)
SELECT 'Administrador', 'admin@redemaisfarma.com',
       '$2a$10$1Vd.8m3tQbI2yCwYw4vTFeH5o5Jv7gN1U3z8uVqzQF7xXq8gq7v9G',
       1, 0, NULL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email='admin@redemaisfarma.com');

-- 🔒 Vincular ROLE_ADMIN ao admin criado (idempotente)
INSERT IGNORE INTO usuario_roles (usuario_id, role)
SELECT u.id, 'ROLE_ADMIN'
FROM usuario u
WHERE u.email='admin@redemaisfarma.com';
