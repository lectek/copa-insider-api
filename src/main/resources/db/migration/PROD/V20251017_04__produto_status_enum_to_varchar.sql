-- Converte somente produto.status de ENUM -> VARCHAR(32), sem tocar no resto

SET @schema_name = DATABASE();

-- Verifica se 'status' é ENUM
SET @is_enum := (
  SELECT CASE WHEN DATA_TYPE='enum' THEN 1 ELSE 0 END
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name
    AND TABLE_NAME='produto'
    AND COLUMN_NAME='status'
);

-- Executa conversão apenas se for ENUM
SET @sql := IF(@is_enum=1, 'ALTER TABLE produto ADD COLUMN status_varchar VARCHAR(32) NULL', 'SELECT 1');
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @sql := IF(@is_enum=1, 'UPDATE produto SET status_varchar = CAST(status AS CHAR(32))', 'SELECT 1');
PREPARE s2 FROM @sql; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @sql := IF(@is_enum=1, 'ALTER TABLE produto DROP COLUMN status', 'SELECT 1');
PREPARE s3 FROM @sql; EXECUTE s3; DEALLOCATE PREPARE s3;

SET @sql := IF(
  @is_enum=1,
  'ALTER TABLE produto CHANGE COLUMN status_varchar status VARCHAR(32) NOT NULL',
  'SELECT 1'
);
PREPARE s4 FROM @sql; EXECUTE s4; DEALLOCATE PREPARE s4;

-- (Opcional, se quiser garantir domínio por CHECK no MySQL 8.0+; remova se 5.7/MariaDB)
-- ALTER TABLE produto
--   ADD CONSTRAINT ck_status_valid
--   CHECK (status IN ('IMPORTADO','ATIVO','INATIVO','RASCUNHO'));
