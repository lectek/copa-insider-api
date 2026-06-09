-- adiciona coluna se não existir (preco_promocional)
SET @exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'preco_promocional'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE produto ADD COLUMN preco_promocional DECIMAL(10,2) NULL',
  'DO 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- imagem_webp
SET @exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'imagem_webp'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE produto ADD COLUMN imagem_webp VARCHAR(255) NULL',
  'DO 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- desconto_percentual
SET @exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'desconto_percentual'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE produto ADD COLUMN desconto_percentual INT NULL',
  'DO 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- destaque_carrossel
SET @exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'produto'
    AND COLUMN_NAME = 'destaque_carrossel'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE produto ADD COLUMN destaque_carrossel TINYINT(1) NULL DEFAULT 0',
  'DO 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
