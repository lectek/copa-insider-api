SET @db := DATABASE();

SET @tenant_table_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'tenant'
);

SET @produto_table_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'produto'
);

SET @sql := IF(
  @tenant_table_exists = 1,
  'INSERT INTO tenant (codigo, nome, ativo)
   SELECT ''default'', ''Tenant Padrao'', 1
   WHERE NOT EXISTS (SELECT 1 FROM tenant WHERE codigo = ''default'')',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tenant_id_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND column_name = 'tenant_id'
);

SET @sql := IF(
  @produto_table_exists = 1 AND @tenant_id_col_exists = 0,
  'ALTER TABLE produto ADD COLUMN tenant_id BIGINT NULL AFTER id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @produto_table_exists = 1 AND @tenant_table_exists = 1,
  'UPDATE produto
      SET tenant_id = (SELECT id FROM tenant WHERE codigo = ''default'' LIMIT 1)
    WHERE tenant_id IS NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TRIGGER IF EXISTS trg_produto_set_default_tenant_before_insert;
CREATE TRIGGER trg_produto_set_default_tenant_before_insert
BEFORE INSERT ON produto
FOR EACH ROW
SET NEW.tenant_id = COALESCE(NEW.tenant_id, (SELECT id FROM tenant WHERE codigo = 'default' LIMIT 1));

SET @idx_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'idx_produto_tenant_id'
);

SET @sql := IF(
  @produto_table_exists = 1 AND @idx_tenant_exists = 0,
  'CREATE INDEX idx_produto_tenant_id ON produto (tenant_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_tenant_legacy_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'idx_produto_tenant_legacy_id'
);

SET @sql := IF(
  @produto_table_exists = 1 AND @idx_tenant_legacy_exists = 0,
  'CREATE INDEX idx_produto_tenant_legacy_id ON produto (tenant_id, legacy_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_tenant_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'idx_produto_tenant_status'
);

SET @sql := IF(
  @produto_table_exists = 1 AND @idx_tenant_status_exists = 0,
  'CREATE INDEX idx_produto_tenant_status ON produto (tenant_id, status)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_produto_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.table_constraints
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND constraint_type = 'FOREIGN KEY'
    AND constraint_name = 'fk_produto_tenant'
);

SET @sql := IF(
  @produto_table_exists = 1 AND @tenant_table_exists = 1 AND @fk_produto_tenant_exists = 0,
  'ALTER TABLE produto
      ADD CONSTRAINT fk_produto_tenant
      FOREIGN KEY (tenant_id) REFERENCES tenant(id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
