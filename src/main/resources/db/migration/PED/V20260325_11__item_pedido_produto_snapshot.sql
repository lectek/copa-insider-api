SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE item_pedido ADD COLUMN produto_nome_snapshot VARCHAR(255) NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'item_pedido'
    AND column_name = 'produto_nome_snapshot'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE item_pedido ADD COLUMN produto_codigo_barras_snapshot VARCHAR(64) NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'item_pedido'
    AND column_name = 'produto_codigo_barras_snapshot'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE item_pedido ADD COLUMN produto_categoria_snapshot VARCHAR(255) NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'item_pedido'
    AND column_name = 'produto_categoria_snapshot'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE item_pedido ADD COLUMN produto_fabricante_snapshot VARCHAR(128) NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'item_pedido'
    AND column_name = 'produto_fabricante_snapshot'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

UPDATE item_pedido ip
JOIN produto p ON p.id = ip.produto_id
SET ip.produto_nome_snapshot = COALESCE(ip.produto_nome_snapshot, LEFT(TRIM(p.nome), 255)),
    ip.produto_codigo_barras_snapshot = COALESCE(ip.produto_codigo_barras_snapshot, LEFT(TRIM(p.codigo_barras), 64)),
    ip.produto_categoria_snapshot = COALESCE(ip.produto_categoria_snapshot, LEFT(TRIM(p.categoria), 255)),
    ip.produto_fabricante_snapshot = COALESCE(ip.produto_fabricante_snapshot, LEFT(TRIM(p.fabricante), 128))
WHERE ip.produto_id IS NOT NULL;
