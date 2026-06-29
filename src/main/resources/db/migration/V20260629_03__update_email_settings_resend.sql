-- Configura envio de email via Resend HTTP API (Railway bloqueia SMTP)
INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
VALUES
    ('email.enabled',      'true',                                   'Email activo',     NOW(), NOW()),
    ('email.smtp_host',    '',                                        'SMTP desactivado', NOW(), NOW()),
    ('email.api_provider', 'resend',                                  'Provedor API',     NOW(), NOW()),
    ('email.api_key',      're_BMAZYeFW_2J7qEK3gj4Msoe2gzieni6tg',  'Resend API key',   NOW(), NOW()),
    ('email.from_email',   'geral@allaboutworldcup2026.com',          'Remetente',        NOW(), NOW()),
    ('email.from_name',    'Copa Insider',                            'Nome remetente',   NOW(), NOW())
ON DUPLICATE KEY UPDATE
    setting_value = VALUES(setting_value),
    updated_at    = NOW();
