SET @db := DATABASE();

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

SET @mov_tenant_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'movimento_estoque'
    AND column_name = 'tenant_id'
);

SET @sql := IF(
  @mov_exists = 1 AND @mov_tenant_col_exists = 0,
  'ALTER TABLE movimento_estoque ADD COLUMN tenant_id BIGINT NULL AFTER produto_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sub_tenant_col_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'product_stock_subscription'
    AND column_name = 'tenant_id'
);

SET @sql := IF(
  @sub_exists = 1 AND @sub_tenant_col_exists = 0,
  'ALTER TABLE product_stock_subscription ADD COLUMN tenant_id BIGINT NULL AFTER produto_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @mov_exists = 1 AND @produto_exists = 1,
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
  @sub_exists = 1 AND @produto_exists = 1,
  'UPDATE product_stock_subscription ps
      JOIN produto p ON p.id = ps.produto_id
       SET ps.tenant_id = p.tenant_id
     WHERE ps.tenant_id IS NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_mov_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'movimento_estoque'
    AND index_name = 'idx_movestoque_tenant_produto'
);

SET @sql := IF(
  @mov_exists = 1 AND @idx_mov_tenant_exists = 0,
  'CREATE INDEX idx_movestoque_tenant_produto ON movimento_estoque (tenant_id, produto_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_sub_tenant_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name = 'product_stock_subscription'
    AND index_name = 'idx_product_stock_subscription_tenant'
);

SET @sql := IF(
  @sub_exists = 1 AND @idx_sub_tenant_exists = 0,
  'CREATE INDEX idx_product_stock_subscription_tenant ON product_stock_subscription (tenant_id, produto_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TRIGGER IF EXISTS trg_movestoque_set_tenant_before_insert;
CREATE TRIGGER trg_movestoque_set_tenant_before_insert
BEFORE INSERT ON movimento_estoque
FOR EACH ROW
SET NEW.tenant_id = COALESCE(NEW.tenant_id, (SELECT p.tenant_id FROM produto p WHERE p.id = NEW.produto_id LIMIT 1));

DROP TRIGGER IF EXISTS trg_product_stock_subscription_set_tenant_before_insert;
CREATE TRIGGER trg_product_stock_subscription_set_tenant_before_insert
BEFORE INSERT ON product_stock_subscription
FOR EACH ROW
SET NEW.tenant_id = COALESCE(NEW.tenant_id, (SELECT p.tenant_id FROM produto p WHERE p.id = NEW.produto_id LIMIT 1));
