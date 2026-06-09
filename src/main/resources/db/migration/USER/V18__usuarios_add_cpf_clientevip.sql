-- Adiciona CPF e CLIENTE_VIP de forma idempotente (sem IF NOT EXISTS)
-- Tabela correta: 'usuario' (singular)

-- CPF -----------------------------------------------------------------------
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'usuario'
    AND COLUMN_NAME  = 'cpf'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE usuario ADD COLUMN cpf VARCHAR(14) NULL AFTER email;',
  'DO 0'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- CLIENTE_VIP ---------------------------------------------------------------
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'usuario'
    AND COLUMN_NAME  = 'cliente_vip'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE usuario ADD COLUMN cliente_vip TINYINT(1) NOT NULL DEFAULT 0 AFTER ultimo_acesso;',
  'DO 0'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- UNIQUE para CPF (checa se já existe QUALQUER índice UNIQUE em cpf) --------
SET @idx_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'usuario'
    AND COLUMN_NAME  = 'cpf'
    AND NON_UNIQUE   = 0
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE usuario ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf);',
  'DO 0'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Normaliza valores nulos (se houver registros antigos) ---------------------
UPDATE usuario SET cliente_vip = 0 WHERE cliente_vip IS NULL;
