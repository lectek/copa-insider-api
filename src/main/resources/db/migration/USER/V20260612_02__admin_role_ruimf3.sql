-- Concede papel ADMIN ao administrador da Copa Insider
INSERT IGNORE INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuario u
JOIN roles r ON r.nome = 'ADMIN'
WHERE u.email = 'ruimf3@outlook.com';
