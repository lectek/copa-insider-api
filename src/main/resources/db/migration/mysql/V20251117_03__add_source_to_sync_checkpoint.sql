-- V20251117_01__add_source_to_sync_checkpoint.sql
-- Ajusta a tabela de checkpoints de sincronização para suportar 'source'
-- usado no import de produtos (firebird.produtos, etc).

-- 1) Adiciona a coluna 'source'
-- Obs: assumindo MySQL 8+. Se der erro por já existir, é porque o schema
-- já foi ajustado manualmente antes.
ALTER TABLE sync_checkpoint
    ADD COLUMN source VARCHAR(100) NOT NULL;

-- 2) Garante unicidade por 'source'
ALTER TABLE sync_checkpoint
    ADD CONSTRAINT uk_sync_checkpoint_source UNIQUE (source);

-- 3) (Opcional mas recomendado) Cria/ajusta o checkpoint inicial
-- para a fonte 'firebird.produtos', que é exatamente o valor que
-- aparece nos logs como parâmetro do where sc1_0.source = ?.
INSERT INTO sync_checkpoint (id, source, last_since)
VALUES ('firebird.produtos', 'firebird.produtos', '1900-01-01 00:00:00')
ON DUPLICATE KEY UPDATE
    last_since = VALUES(last_since);
