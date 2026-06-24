-- Tabela de tokens persistentes para o remember-me do Spring Security
CREATE TABLE IF NOT EXISTS persistent_logins (
    username  VARCHAR(191) NOT NULL,
    series    VARCHAR(64)  NOT NULL,
    token     VARCHAR(64)  NOT NULL,
    last_used TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (series)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
