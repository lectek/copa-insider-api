-- Seed usuario principal do login unificado (web/api/apk)
-- Idempotente: cria/atualiza usuario e vincula role DEVELOPER.

INSERT IGNORE INTO roles (nome, descricao) VALUES
 ('DEVELOPER', 'Acesso total para desenvolvimento/suporte');

INSERT INTO usuario (nome, email, cpf, senha, cliente_vip, tentativas_falhas)
SELECT
  'Alex Morais Silva Junior',
  'lektecjava@gmail.com',
  '09909053497',
  '$2a$10$.O1Yr.S7PTzP44/BoKs03OxJXLVyLyCCpKq5oKIaDmz0KCdF6JSUK',
  0,
  0
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE email = 'lektecjava@gmail.com'
);

UPDATE usuario
   SET nome = 'Alex Morais Silva Junior',
       cpf = '09909053497',
       senha = '$2a$10$.O1Yr.S7PTzP44/BoKs03OxJXLVyLyCCpKq5oKIaDmz0KCdF6JSUK',
       tentativas_falhas = 0
 WHERE email = 'lektecjava@gmail.com';

INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
  FROM usuario u
  JOIN roles r ON r.nome = 'DEVELOPER'
 WHERE u.email = 'lektecjava@gmail.com';
