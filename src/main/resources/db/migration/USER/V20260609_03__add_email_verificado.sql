-- Adiciona coluna de verificação de e-mail
-- DEFAULT 1 = já verificado para contas legadas / criadas antes desta migração
ALTER TABLE usuario
    ADD COLUMN email_verificado TINYINT(1) NOT NULL DEFAULT 1;
