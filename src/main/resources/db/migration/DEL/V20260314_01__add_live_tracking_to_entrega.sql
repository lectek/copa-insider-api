ALTER TABLE entrega_rota
    ADD COLUMN motorista_latitude DECIMAL(10,6) NULL AFTER finalizada_em,
    ADD COLUMN motorista_longitude DECIMAL(10,6) NULL AFTER motorista_latitude,
    ADD COLUMN motorista_localizacao_em DATETIME NULL AFTER motorista_longitude;

ALTER TABLE entrega_parada
    ADD COLUMN aproximando_notificado_em DATETIME NULL AFTER ocorrencias;
