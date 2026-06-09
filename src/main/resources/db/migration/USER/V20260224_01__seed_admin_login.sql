-- Seed idempotente de admin para testes (web/api)
-- Login: admin@redemaisfarma.local
-- Senha em texto (para comunicação com QA): RedeMais@2026

INSERT IGNORE INTO roles (nome, descricao) VALUES
 ('ADMIN', 'Administrador do sistema');

INSERT INTO usuario (nome, email, cpf, senha, cliente_vip, tentativas_falhas)
SELECT
  'Administrador RedeMaisFarma',
  'admin@redemaisfarma.local',
  '99999999999',
  '$2a$10$JAh3ALzz09ZpriY7DqwL6.U0mPXxf9pLOQWvzboVgw53mNZR50QZK',
  1,
  0
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE lower(email) = 'admin@redemaisfarma.local'
);

UPDATE usuario
   SET nome = 'Administrador RedeMaisFarma',
       senha = '$2a$10$JAh3ALzz09ZpriY7DqwL6.U0mPXxf9pLOQWvzboVgw53mNZR50QZK',
       cliente_vip = 1,
       tentativas_falhas = 0
 WHERE lower(email) = 'admin@redemaisfarma.local';

UPDATE usuario
   SET cpf = CASE
       WHEN cpf IS NULL OR trim(cpf) = '' THEN '99999999999'
       ELSE cpf
   END
 WHERE lower(email) = 'admin@redemaisfarma.local';

INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
  FROM usuario u
  JOIN roles r ON upper(r.nome) = 'ADMIN'
 WHERE lower(u.email) = 'admin@redemaisfarma.local';
