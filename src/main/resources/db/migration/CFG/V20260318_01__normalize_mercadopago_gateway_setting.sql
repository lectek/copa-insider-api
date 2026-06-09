UPDATE app_setting
SET svalue = 'mercadopago'
WHERE skey = 'pg.gateway'
  AND LOWER(
        REPLACE(
            REPLACE(
                REPLACE(TRIM(COALESCE(svalue, '')), '_', ''),
                '-',
                ''
            ),
            ' ',
            ''
        )
    ) = 'mercadopago';
