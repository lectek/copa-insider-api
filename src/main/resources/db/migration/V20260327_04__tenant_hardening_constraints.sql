SET @db := DATABASE();

SET @tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'tenant'
);

SET @produto_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'produto'
);

SET @mov_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'movimento_estoque'
);

SET @sub_exists := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = @db
    AND table_name = 'product_stock_subscription'
);

SET @sql := IF(
  @tenant_exists = 1,
  'INSERT INTO tenant (codigo, nome, ativo)
   SELECT ''default'', ''Tenant Padrao'', 1
   WHERE NOT EXISTS (SELECT 1 FROM tenant WHERE codigo = ''default'')',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @produto_exists = 1 AND @tenant_exists = 1,
  'UPDATE produto
      SET tenant_id = (SELECT id FROM tenant WHERE codigo = ''default'' LIMIT 1)
    WHERE tenant_id IS NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @produto_tenant_is_nullable := (
  SELECT IFNULL(is_nullable, 'YES')
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND column_name = 'tenant_id'
  LIMIT 1
);

SET @sql := IF(
  @produto_exists = 1 AND @produto_tenant_is_nullable = 'YES',
  'ALTER TABLE produto MODIFY COLUMN tenant_id BIGINT NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_old_1_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'uk_produto_cod_barras'
);

SET @sql := IF(
  @produto_exists = 1 AND @uk_old_1_exists > 0,
  'DROP INDEX uk_produto_cod_barras ON produto',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_old_2_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'uk_produto_codigo_barras'
);

SET @sql := IF(
  @produto_exists = 1 AND @uk_old_2_exists > 0,
  'DROP INDEX uk_produto_codigo_barras ON produto',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_new_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'produto'
    AND index_name = 'ux_produto_tenant_codigo_barras'
);

SET @sql := IF(
  @produto_exists = 1 AND @uk_new_exists = 0,
  'CREATE UNIQUE INDEX ux_produto_tenant_codigo_barras ON produto(tenant_id, codigo_barras)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @mov_exists = 1 AND @tenant_exists = 1,
  'UPDATE movimento_estoque me
      JOIN produto p ON p.id = me.produto_id
       SET me.tenant_id = p.tenant_id
     WHERE me.tenant_id IS NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @sub_exists = 1 AND @tenant_exists = 1,
  'UPDATE product_stock_subscription ps
      JOIN produto p ON p.id = ps.produto_id
       SET ps.tenant_id = p.tenant_id
     WHERE ps.tenant_id IS NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @mov_tenant_nullable := (
  SELECT IFNULL(is_nullable, 'YES')
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'movimento_estoque'
    AND column_name = 'tenant_id'
  LIMIT 1
);

SET @sql := IF(
  @mov_exists = 1 AND @mov_tenant_nullable = 'YES',
  'ALTER TABLE movimento_estoque MODIFY COLUMN tenant_id BIGINT NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sub_tenant_nullable := (
  SELECT IFNULL(is_nullable, 'YES')
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'product_stock_subscription'
    AND column_name = 'tenant_id'
  LIMIT 1
);

SET @sql := IF(
  @sub_exists = 1 AND @sub_tenant_nullable = 'YES',
  'ALTER TABLE product_stock_subscription MODIFY COLUMN tenant_id BIGINT NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_mov_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.table_constraints
  WHERE table_schema = @db
    AND table_name = 'movimento_estoque'
    AND constraint_type = 'FOREIGN KEY'
    AND constraint_name = 'fk_movestoque_tenant'
);

SET @sql := IF(
  @mov_exists = 1 AND @tenant_exists = 1 AND @fk_mov_tenant_exists = 0,
  'ALTER TABLE movimento_estoque
      ADD CONSTRAINT fk_movestoque_tenant
      FOREIGN KEY (tenant_id) REFERENCES tenant(id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_sub_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.table_constraints
  WHERE table_schema = @db
    AND table_name = 'product_stock_subscription'
    AND constraint_type = 'FOREIGN KEY'
    AND constraint_name = 'fk_product_stock_subscription_tenant'
);

SET @sql := IF(
  @sub_exists = 1 AND @tenant_exists = 1 AND @fk_sub_tenant_exists = 0,
  'ALTER TABLE product_stock_subscription
      ADD CONSTRAINT fk_product_stock_subscription_tenant
      FOREIGN KEY (tenant_id) REFERENCES tenant(id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

