-- Seeds de papéis comuns
INSERT IGNORE INTO roles (nome, descricao) VALUES
 ('ADMIN', 'Administrador do sistema'),
 ('USER',  'Usuário padrão');

-- Se já existir um usuário admin, vincula o papel ADMIN
-- (ajuste o e-mail conforme o seu seed/realidade)
INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuario u
JOIN roles r ON r.nome = 'ADMIN'
WHERE u.email = 'admin@redemaisfarma.local';
