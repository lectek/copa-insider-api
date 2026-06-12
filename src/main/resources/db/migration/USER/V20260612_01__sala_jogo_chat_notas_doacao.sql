CREATE TABLE sala_chat_mensagem (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    jogo_id     BIGINT         NOT NULL,
    session_id  VARCHAR(100)   NOT NULL,
    autor_nome  VARCHAR(80)    NOT NULL,
    mensagem    TEXT           NOT NULL,
    tipo        VARCHAR(20)    NOT NULL DEFAULT 'TEXTO',
    ativo       TINYINT(1)     NOT NULL DEFAULT 1,
    criado_em   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_sala_chat_jogo (jogo_id, criado_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sala_voto_jogador (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    jogo_id       BIGINT        NOT NULL,
    session_id    VARCHAR(100)  NOT NULL,
    jogador_slug  VARCHAR(100)  NOT NULL,
    selecao_slug  VARCHAR(100)  NOT NULL,
    nota          TINYINT       NOT NULL,
    criado_em     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_voto (jogo_id, session_id, jogador_slug),
    INDEX idx_voto_jogo (jogo_id, jogador_slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doacao (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    stripe_session_id  VARCHAR(200)   UNIQUE,
    doador_email       VARCHAR(200),
    doador_nome        VARCHAR(100),
    valor              DECIMAL(10,2)  NOT NULL,
    moeda              VARCHAR(10)    NOT NULL DEFAULT 'EUR',
    status             VARCHAR(30)    NOT NULL DEFAULT 'PENDENTE',
    mensagem           TEXT,
    criado_em          DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_doacao_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
