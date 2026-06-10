CREATE TABLE IF NOT EXISTS copa_compra (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transacao        VARCHAR(50)  NOT NULL UNIQUE,
    comprador_email  VARCHAR(200) NOT NULL,
    comprador_nome   VARCHAR(200) NOT NULL,
    produto_nome     VARCHAR(200),
    valor            DECIMAL(10,2) NOT NULL,
    moeda            VARCHAR(10),
    status           VARCHAR(30)  NOT NULL,
    criado_em        DATETIME(6)  NOT NULL,
    email_enviado    TINYINT(1)   NOT NULL DEFAULT 0,
    email_enviado_em DATETIME(6)
);
