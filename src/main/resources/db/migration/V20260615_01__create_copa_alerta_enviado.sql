CREATE TABLE copa_alerta_enviado (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    partida_id BIGINT       NOT NULL,
    email      VARCHAR(255) NOT NULL,
    enviado_em DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alerta_partida_email (partida_id, email)
);
