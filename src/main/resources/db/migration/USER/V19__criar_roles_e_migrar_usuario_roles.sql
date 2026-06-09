-- 0) (opcional) padroniza DB
-- ALTER DATABASE `redemaisfarma_test` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 1) Cria tabela de papéis com collation explícita
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

-- 1.1) Garante collation em roles.nome
ALTER TABLE roles
  MODIFY COLUMN nome VARCHAR(50)
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  NOT NULL;

-- 1.2) Alinha collation de usuario_roles.role se a coluna existir
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario_roles'
    AND COLUMN_NAME = 'role'
);
SET @sql := IF(@col_exists = 1,
  'ALTER TABLE usuario_roles MODIFY COLUMN role VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Povoa 'roles' a partir de usuario_roles.role
INSERT INTO roles (nome)
SELECT DISTINCT ur.role
FROM usuario_roles ur
LEFT JOIN roles r ON r.nome = ur.role
WHERE r.id IS NULL;

-- 3) Adiciona coluna role_id (guard via information_schema)
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario_roles'
    AND COLUMN_NAME = 'role_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE usuario_roles ADD COLUMN role_id BIGINT NULL AFTER usuario_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) Preenche role_id com base no nome (somente onde estiver NULL)
UPDATE usuario_roles ur
JOIN roles r ON r.nome = ur.role
SET ur.role_id = r.id
WHERE ur.role_id IS NULL;

-- 5) Cria FK se não existir
SET @fk_exists := (
  SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_usuario_roles_role'
);
SET @sql := IF(@fk_exists = 0,
  'ALTER TABLE usuario_roles ADD CONSTRAINT fk_usuario_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5.1) Cria índice em role_id se não existir
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario_roles'
    AND INDEX_NAME = 'idx_usuario_roles_role_id'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_usuario_roles_role_id ON usuario_roles(role_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6) Torna role_id NOT NULL
ALTER TABLE usuario_roles
  MODIFY COLUMN role_id BIGINT NOT NULL;

-- 6.1) Ajusta PK para (usuario_id, role_id) apenas se necessário
SET @need_pk_change := (
  SELECT
    CASE
      WHEN EXISTS (
        SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'usuario_roles'
          AND CONSTRAINT_NAME = 'PRIMARY'
          AND COLUMN_NAME = 'usuario_id'
          AND ORDINAL_POSITION = 1
      )
      AND EXISTS (
        SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'usuario_roles'
          AND CONSTRAINT_NAME = 'PRIMARY'
          AND COLUMN_NAME = 'role_id'
          AND ORDINAL_POSITION = 2
      )
      THEN 0 ELSE 1
    END
);
SET @sql := IF(@need_pk_change = 1,
  'ALTER TABLE usuario_roles DROP PRIMARY KEY, ADD PRIMARY KEY (usuario_id, role_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7) Remove a coluna de texto 'role' (se existir)
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'usuario_roles'
    AND COLUMN_NAME = 'role'
);
SET @sql := IF(@col_exists = 1,
  'ALTER TABLE usuario_roles DROP COLUMN role',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 8) NÃO recrie uk_usuario_email aqui (já existe em versões anteriores).
