SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE produto ADD COLUMN categoria_id BIGINT NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'produto'
    AND column_name = 'categoria_id'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

UPDATE produto
SET categoria = 'Sem Categoria'
WHERE categoria IS NULL OR TRIM(categoria) = '';

INSERT INTO produto_categoria (nome)
SELECT DISTINCT LEFT(TRIM(categoria), 100)
FROM produto
WHERE categoria IS NOT NULL
  AND TRIM(categoria) <> ''
ON DUPLICATE KEY UPDATE nome = VALUES(nome);

UPDATE produto p
JOIN produto_categoria c
  ON LOWER(c.nome) = LOWER(LEFT(TRIM(p.categoria), 100))
SET p.categoria_id = c.id
WHERE p.categoria IS NOT NULL
  AND TRIM(p.categoria) <> '';

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'CREATE INDEX idx_produto_categoria_id ON produto (categoria_id)',
    'SELECT 1'
  )
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'produto'
    AND index_name = 'idx_produto_categoria_id'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE produto ADD CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id) REFERENCES produto_categoria(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT 1'
  )
  FROM information_schema.referential_constraints
  WHERE constraint_schema = DATABASE()
    AND table_name = 'produto'
    AND constraint_name = 'fk_produto_categoria'
);
PREPARE s FROM @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

DROP TRIGGER IF EXISTS trg_produto_categoria_bi;
DROP TRIGGER IF EXISTS trg_produto_categoria_bu;

DELIMITER $$
CREATE TRIGGER trg_produto_categoria_bi
BEFORE INSERT ON produto
FOR EACH ROW
BEGIN
  SET NEW.categoria = COALESCE(
    NULLIF(TRIM(NEW.categoria), ''),
    (SELECT c.nome FROM produto_categoria c WHERE c.id = NEW.categoria_id LIMIT 1),
    'Sem Categoria'
  );
  INSERT INTO produto_categoria (nome)
  VALUES (LEFT(NEW.categoria, 100))
  ON DUPLICATE KEY UPDATE nome = VALUES(nome);
  SET NEW.categoria_id = (
    SELECT c.id
    FROM produto_categoria c
    WHERE LOWER(c.nome) = LOWER(LEFT(NEW.categoria, 100))
    LIMIT 1
  );
END$$

CREATE TRIGGER trg_produto_categoria_bu
BEFORE UPDATE ON produto
FOR EACH ROW
BEGIN
  SET NEW.categoria = COALESCE(
    NULLIF(TRIM(NEW.categoria), ''),
    (SELECT c.nome FROM produto_categoria c WHERE c.id = NEW.categoria_id LIMIT 1),
    'Sem Categoria'
  );
  INSERT INTO produto_categoria (nome)
  VALUES (LEFT(NEW.categoria, 100))
  ON DUPLICATE KEY UPDATE nome = VALUES(nome);
  SET NEW.categoria_id = (
    SELECT c.id
    FROM produto_categoria c
    WHERE LOWER(c.nome) = LOWER(LEFT(NEW.categoria, 100))
    LIMIT 1
  );
END$$
DELIMITER ;
