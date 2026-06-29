CREATE TABLE IF NOT EXISTS ebook_pedido (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    transacao         VARCHAR(100) NOT NULL UNIQUE,
    comprador_email   VARCHAR(200) NOT NULL,
    comprador_nome    VARCHAR(200) NOT NULL,
    selecao_code      VARCHAR(10)  NULL,          -- 'BRA', 'ARG', 'FRA', 'POR'... (null = aguardando seleção)
    selecao_nome      VARCHAR(100) NULL,           -- 'Brasil', 'Argentina'...
    idioma            VARCHAR(10)  NULL,           -- 'pt-BR', 'es', 'fr', 'en'...
    pdf_caminho       VARCHAR(500) NULL,           -- caminho do ficheiro gerado no servidor
    download_token    VARCHAR(100) NULL UNIQUE,    -- token seguro para download
    status            VARCHAR(30)  NOT NULL DEFAULT 'AGUARDANDO_SELECAO',
    criado_em         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gerado_em         DATETIME     NULL,
    INDEX idx_ebook_email  (comprador_email),
    INDEX idx_ebook_status (status),
    INDEX idx_ebook_token  (download_token)
);
