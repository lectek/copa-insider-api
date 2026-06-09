-- V23072023_24__cliente_triggers.sql

-- Normaliza email e CPF antes de INSERT
DROP TRIGGER IF EXISTS cliente_bi;
CREATE TRIGGER cliente_bi
BEFORE INSERT ON cliente
FOR EACH ROW
BEGIN
  SET NEW.email = LOWER(TRIM(NEW.email));
  SET NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf), '.', ''), '-', ''), ' ', '');
END;

-- Normaliza email e CPF antes de UPDATE
DROP TRIGGER IF EXISTS cliente_bu;
CREATE TRIGGER cliente_bu
BEFORE UPDATE ON cliente
FOR EACH ROW
BEGIN
  SET NEW.email = LOWER(TRIM(NEW.email));
  SET NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf), '.', ''), '-', ''), ' ', '');
END;
