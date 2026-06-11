-- Tabela de acessos concedidos após compra Hotmart
CREATE TABLE IF NOT EXISTS copa_acesso (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL,
    produto_slug  VARCHAR(100)  NOT NULL,
    transacao     VARCHAR(100),
    concedido_em  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email_produto (email, produto_slug),
    INDEX idx_email (email)
);

-- Produto: Acesso Calendário + Comparador
INSERT IGNORE INTO copa_produto
    (slug, tipo, preco, preco_eur,
     nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     hotmart_url, ativo, ordem)
VALUES (
    'acesso-calendario-comparador',
    'ACESSO_FERRAMENTAS',
    24.90, 3.99,
    'Calendário & Comparador — Copa 2026',
    'Calendário & Comparador — Copa 2026',
    'Calendar & Comparator — Copa 2026',
    'Acesso completo ao Calendário de jogos da Copa 2026 e ao Comparador histórico de seleções (Copa do Mundo, Copa América e UEFA Euro desde 1930).',
    'Acesso completo ao Calendário de jogos da Copa 2026 e ao Comparador histórico de selecções (Copa do Mundo, Copa América e UEFA Euro desde 1930).',
    'Full access to the Copa 2026 match Calendar and the historical Comparator (World Cup, Copa América and UEFA Euro since 1930).',
    '#', 0, 4
);
