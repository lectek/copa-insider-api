-- ix_produto_vitrine
SET @exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME  = 'produto'
    AND INDEX_NAME  = 'ix_produto_vitrine'
);
SET @sql := IF(@exists = 0,
  'CREATE INDEX ix_produto_vitrine ON produto (disponivel, estoque, preco_venda, data_cadastro, id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ix_produto_carrossel
SET @exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME  = 'produto'
    AND INDEX_NAME  = 'ix_produto_carrossel'
);
SET @sql := IF(@exists = 0,
  'CREATE INDEX ix_produto_carrossel ON produto (destaque_carrossel, publicado_em, despublicado_em)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
