-- Garante que a tabela exista antes (opcional, só pra segurança extra)
-- Se tiver certeza que ela existe, pode remover esse bloco.
CREATE TABLE IF NOT EXISTS sync_checkpoint (
    id BIGINT NOT NULL PRIMARY KEY,
    source VARCHAR(100) NOT NULL
    -- não repita colunas que já existem aqui, isso é só um exemplo
);

-- Adiciona a coluna só se ainda não existir
SET @sync_checkpoint_has_last_since := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sync_checkpoint'
    AND COLUMN_NAME = 'last_since'
);
SET @sync_checkpoint_add_last_since := IF(
  @sync_checkpoint_has_last_since = 0,
  'ALTER TABLE sync_checkpoint ADD COLUMN last_since datetime(6) NULL',
  'SELECT 1'
);
PREPARE sync_checkpoint_last_since_stmt FROM @sync_checkpoint_add_last_since;
EXECUTE sync_checkpoint_last_since_stmt;
DEALLOCATE PREPARE sync_checkpoint_last_since_stmt;
