-- Seed idempotente: utilizador DEV Lektec
-- Email : lektecjava@gmail.com
-- Senha : @Lektec_25  (BCrypt $2b$10, 10 rounds)
-- Role  : DEVELOPER

-- 1) Cria o utilizador se ainda não existir
INSERT INTO usuario (nome, email, cpf, senha, cliente_vip, tentativas_falhas)
SELECT
  'Dev Lektec',
  'lektecjava@gmail.com',
  '11111111111',
  '$2b$10$5YYLf8z1qV1S1OZGRfal7uY81aVxhuWvl.kbeXtgXRWp21Z1FAvDi',
  0,
  0
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE lower(email) = 'lektecjava@gmail.com'
);

-- 2) Garante que a senha está actualizada (caso o utilizador já exista)
UPDATE usuario
   SET nome            = 'Dev Lektec',
       senha           = '$2b$10$5YYLf8z1qV1S1OZGRfal7uY81aVxhuWvl.kbeXtgXRWp21Z1FAvDi',
       tentativas_falhas = 0
 WHERE lower(email) = 'lektecjava@gmail.com';

-- 3) Garante CPF preenchido (coluna NOT NULL UNIQUE)
UPDATE usuario
   SET cpf = '11111111111'
 WHERE lower(email) = 'lektecjava@gmail.com'
   AND (cpf IS NULL OR trim(cpf) = '');

-- 4) Concede a role DEVELOPER (idempotente via INSERT IGNORE)
INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
  FROM usuario u
  JOIN roles   r ON upper(r.nome) = 'DEVELOPER'
 WHERE lower(u.email) = 'lektecjava@gmail.com';
