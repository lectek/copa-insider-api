CREATE TABLE copa_palpite (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    partida_id      BIGINT       NOT NULL,
    gols_casa       INT          NOT NULL DEFAULT 0,
    gols_visitante  INT          NOT NULL DEFAULT 0,
    criado_em       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_palpite_email_partida (email, partida_id)
);
