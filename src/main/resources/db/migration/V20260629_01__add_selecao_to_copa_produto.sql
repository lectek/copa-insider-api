ALTER TABLE copa_produto
    ADD COLUMN selecao_code   VARCHAR(10)  NULL AFTER slug_time2,
    ADD COLUMN idioma_default VARCHAR(10)  NULL AFTER selecao_code;
