-- Allow Google OAuth users to register without a CPF
-- NULL is allowed multiple times in a UNIQUE column in MySQL
ALTER TABLE usuario MODIFY COLUMN cpf VARCHAR(14) NULL;
