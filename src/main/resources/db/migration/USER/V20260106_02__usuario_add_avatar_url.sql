-- Adiciona avatar_url em usuario (imagem de perfil)
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'usuario'
    AND column_name = 'avatar_url'
);
SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE usuario ADD COLUMN avatar_url VARCHAR(255) NULL',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
