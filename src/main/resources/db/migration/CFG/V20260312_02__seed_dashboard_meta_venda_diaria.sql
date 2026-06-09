INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'dashboard.meta_venda_diaria.valor',
       '2000.00',
       'Meta diaria de vendas exibida no dashboard administrativo',
       CURRENT_TIMESTAMP(6),
       CURRENT_TIMESTAMP(6)
 WHERE NOT EXISTS (
       SELECT 1
         FROM app_settings
        WHERE setting_key = 'dashboard.meta_venda_diaria.valor'
 );
