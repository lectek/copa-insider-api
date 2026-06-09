-- Cria índices únicos somente se não existirem (MySQL 8 não tem IF NOT EXISTS em CREATE INDEX)

-- email
SET @ix := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'cliente'
    AND INDEX_NAME   = 'uk_cliente_email'
);
SET @ddl := IF(@ix = 0,
  'CREATE UNIQUE INDEX uk_cliente_email ON cliente (email)',
  'DO 0'
);
PREPARE x FROM @ddl; EXECUTE x; DEALLOCATE PREPARE x;

-- cpf
SET @ix := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'cliente'
    AND INDEX_NAME   = 'uk_cliente_cpf'
);
SET @ddl := IF(@ix = 0,
  'CREATE UNIQUE INDEX uk_cliente_cpf ON cliente (cpf)',
  'DO 0'
);
PREPARE x FROM @ddl; EXECUTE x; DEALLOCATE PREPARE x;
