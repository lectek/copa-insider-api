-- V23072023__03.sql
-- Ajusta tabela usuario e cria usuario_roles compatível com UsuarioEntity

-- Tabela principal -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome               VARCHAR(120)  NOT NULL,
  email              VARCHAR(150)  NOT NULL,
  cpf                VARCHAR(14)   NOT NULL,
  senha              VARCHAR(255)  NOT NULL,

  cliente_vip        BIT(1)        NOT NULL DEFAULT b'0',
  tentativas_falhas  INT           NOT NULL DEFAULT 0,
  ultimo_acesso      DATETIME(6)            NULL,

  created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  version            BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT uk_usuario_email UNIQUE (email),
  CONSTRAINT uk_usuario_cpf   UNIQUE (cpf)
);

-- NADA de "CREATE INDEX IF NOT EXISTS" aqui:
-- os UNIQUE acima já criam índices; se quiser outro índice não-único,
-- crie em uma migration separada específica para MySQL versão X.

-- Tabela de roles (ElementCollection) ---------------------------------------
CREATE TABLE IF NOT EXISTS usuario_roles (
  usuario_id BIGINT      NOT NULL,
  role       VARCHAR(50) NOT NULL,
  CONSTRAINT fk_roles_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON DELETE CASCADE,
  CONSTRAINT uk_usuario_role UNIQUE (usuario_id, role)
);
