-- Idempotente: derruba e recria os gatilhos de normalização
DROP TRIGGER IF EXISTS cliente_bi;
DROP TRIGGER IF EXISTS cliente_bu;

CREATE TRIGGER cliente_bi
BEFORE INSERT ON cliente
FOR EACH ROW
  SET NEW.email = LOWER(TRIM(COALESCE(NEW.email, ''))),
      NEW.cpf   = REGEXP_REPLACE(TRIM(COALESCE(NEW.cpf, '')), '[^0-9]', '');

CREATE TRIGGER cliente_bu
BEFORE UPDATE ON cliente
FOR EACH ROW
  SET NEW.email = LOWER(TRIM(COALESCE(NEW.email, ''))),
      NEW.cpf   = REGEXP_REPLACE(TRIM(COALESCE(NEW.cpf, '')), '[^0-9]', '');
