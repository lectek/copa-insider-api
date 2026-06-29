-- Actualiza configurações de email para Resend (smtp.resend.com:465 SSL)
INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
VALUES
    ('email.enabled',    'true',                       'Email activo',          NOW(), NOW()),
    ('email.smtp_host',  'smtp.resend.com',            'SMTP host',             NOW(), NOW()),
    ('email.smtp_port',  '465',                        'SMTP port (SSL)',        NOW(), NOW()),
    ('email.smtp_user',  'resend',                     'SMTP username',         NOW(), NOW()),
    ('email.smtp_pass',  're_BMAZYeFW_2J7qEK3gj4Msoe2gzieni6tg', 'SMTP password', NOW(), NOW()),
    ('email.smtp_tls',   'false',                      'STARTTLS desligado',    NOW(), NOW()),
    ('email.smtp_ssl',   'true',                       'SSL activo (porta 465)',NOW(), NOW()),
    ('email.from_email', 'geral@allaboutworldcup2026.com', 'Remetente',         NOW(), NOW()),
    ('email.from_name',  'Copa Insider',               'Nome remetente',        NOW(), NOW())
ON DUPLICATE KEY UPDATE
    setting_value = VALUES(setting_value),
    updated_at    = NOW();
