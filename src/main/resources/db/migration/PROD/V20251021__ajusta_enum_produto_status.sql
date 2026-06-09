-- V20251021__ajusta_enum_produto_status.sql
UPDATE produto SET status = 'PUBLICADO' WHERE status = 'ATIVO';
UPDATE produto SET status = 'VALIDADO'  WHERE status IN ('INATIVO','RASCUNHO');

ALTER TABLE produto DROP CHECK ck_status_valid;
ALTER TABLE produto
  ADD CONSTRAINT ck_status_valid
  CHECK (status IN ('IMPORTADO','VALIDADO','PUBLICADO'));

ALTER TABLE produto MODIFY status VARCHAR(32) NOT NULL DEFAULT 'IMPORTADO';
