/* ============================================================================
   V27__auth_roles_bootstrap.sql
   - Normaliza roles (Esquema B)
   - Seeds: ADMIN, USER, DEVELOPER
   - Concede DEVELOPER a lektecjava@gmail.com
   - Cria tabela de auditoria role_audit
   Idempotente — seguro para reexecução.
============================================================================ */

-- 1) Tabela roles (se não existir)
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(50) NOT NULL,
  descricao VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_nome (nome)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE roles
  MODIFY COLUMN nome VARCHAR(50)
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  NOT NULL;

-- 2) usuario_roles → adiciona role_id e migra valores de texto (se houver)
SET @has_role_id := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='usuario_roles' AND COLUMN_NAME='role_id'
);
SET @sql := IF(@has_role_id = 0,
  'ALTER TABLE usuario_roles ADD COLUMN role_id BIGINT NULL AFTER usuario_id',
  'SELECT 1'
);
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @has_role_text := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='usuario_roles' AND COLUMN_NAME='role'
);

-- Copia valores distintos da coluna texto para roles
SET @sql := IF(@has_role_text = 1,
  'INSERT INTO roles (nome)
     SELECT DISTINCT ur.role
     FROM usuario_roles ur
     LEFT JOIN roles r ON r.nome = ur.role
     WHERE ur.role IS NOT NULL AND r.id IS NULL',
  'SELECT 1'
);
PREPARE s2 FROM @sql; EXECUTE s2; DEALLOCATE PREPARE s2;

-- Backfill role_id
SET @sql := IF(@has_role_text = 1,
  'UPDATE usuario_roles ur
     JOIN roles r ON r.nome = ur.role
   SET ur.role_id = r.id
   WHERE ur.role_id IS NULL AND ur.role IS NOT NULL',
  'SELECT 1'
);
PREPARE s3 FROM @sql; EXECUTE s3; DEALLOCATE PREPARE s3;

-- FK e índice
SET @fk_exists := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'fk_usuario_roles_role'
);
SET @sql := IF(@fk_exists = 0,
  'ALTER TABLE usuario_roles
     ADD CONSTRAINT fk_usuario_roles_role
     FOREIGN KEY (role_id) REFERENCES roles(id)
     ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE s4 FROM @sql; EXECUTE s4; DEALLOCATE PREPARE s4;

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='usuario_roles' AND INDEX_NAME='idx_usuario_roles_role_id'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_usuario_roles_role_id ON usuario_roles(role_id)',
  'SELECT 1'
);
PREPARE s5 FROM @sql; EXECUTE s5; DEALLOCATE PREPARE s5;

-- Ajusta PK (usuario_id, role_id)
SET @need_pk_change := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuario_roles'
        AND CONSTRAINT_NAME='PRIMARY' AND COLUMN_NAME='usuario_id' AND ORDINAL_POSITION=1
    )
    AND EXISTS (
      SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuario_roles'
        AND CONSTRAINT_NAME='PRIMARY' AND COLUMN_NAME='role_id' AND ORDINAL_POSITION=2
    )
    THEN 0 ELSE 1 END
);
SET @sql := IF(@need_pk_change = 1,
  'ALTER TABLE usuario_roles DROP PRIMARY KEY, ADD PRIMARY KEY (usuario_id, role_id)',
  'SELECT 1'
);
PREPARE s6 FROM @sql; EXECUTE s6; DEALLOCATE PREPARE s6;

-- Remove coluna texto 'role' (se existir)
SET @sql := IF(@has_role_text = 1,
  'ALTER TABLE usuario_roles DROP COLUMN role',
  'SELECT 1'
);
PREPARE s7 FROM @sql; EXECUTE s7; DEALLOCATE PREPARE s7;

ALTER TABLE usuario_roles
  MODIFY COLUMN role_id BIGINT NOT NULL;

-- 3) Seeds de papéis
INSERT IGNORE INTO roles (nome, descricao) VALUES
 ('ADMIN', 'Administrador do sistema'),
 ('USER',  'Usuário padrão/cliente'),
 ('DEVELOPER', 'Acesso total para desenvolvimento/suporte');

-- 4) Concede DEVELOPER ao e-mail solicitado (se existir)
INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuario u
JOIN roles r ON r.nome = 'DEVELOPER'
WHERE u.email = 'lektecjava@gmail.com';

-- 5) Auditoria de mudanças de papel
CREATE TABLE IF NOT EXISTS role_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  action VARCHAR(20) NOT NULL,           -- GRANT | REVOKE
  target_user_id BIGINT NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  actor_email VARCHAR(150) NULL,
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- compat MySQL 8: cria índice apenas se não existir
SET @has_idx_target := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'role_audit'
    AND index_name = 'idx_role_audit_target'
);
SET @sql := IF(@has_idx_target = 0,
  'CREATE INDEX idx_role_audit_target ON role_audit (target_user_id, created_at)',
  'SELECT 1'
);
PREPARE s8 FROM @sql; EXECUTE s8; DEALLOCATE PREPARE s8;

SET @has_idx_actor := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'role_audit'
    AND index_name = 'idx_role_audit_actor'
);
SET @sql := IF(@has_idx_actor = 0,
  'CREATE INDEX idx_role_audit_actor  ON role_audit (actor_email, created_at)',
  'SELECT 1'
);
PREPARE s9 FROM @sql; EXECUTE s9; DEALLOCATE PREPARE s9;

-- 6) Política: não concedemos USER ao admin nesta migration (separação admin/cliente).
