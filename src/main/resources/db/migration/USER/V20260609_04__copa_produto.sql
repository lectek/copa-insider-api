-- Catálogo de produtos digitais Copa Insider
CREATE TABLE IF NOT EXISTS copa_produto (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug               VARCHAR(100)   NOT NULL UNIQUE,
    tipo               VARCHAR(50)    NOT NULL,
    preco              DECIMAL(10,2)  NOT NULL,
    nome_pt_br         VARCHAR(200),
    nome_pt_pt         VARCHAR(200),
    nome_en            VARCHAR(200),
    desc_pt_br         TEXT,
    desc_pt_pt         TEXT,
    desc_en            TEXT,
    hotmart_url        VARCHAR(500),
    hotmart_product_id BIGINT         UNIQUE,
    imagem_url         VARCHAR(500),
    slug_time1         VARCHAR(100),
    slug_time2         VARCHAR(100),
    ativo              TINYINT(1) NOT NULL DEFAULT 1,
    ordem              INT        NOT NULL DEFAULT 0
);

INSERT IGNORE INTO copa_produto
    (slug, tipo, preco, nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     hotmart_url, slug_time1, slug_time2, ativo, ordem)
VALUES
-- ─── 1. Guia da Seleção (3,99€ · R$24,90) ────────────────────────────────
(
    'guia-selecao', 'GUIA_SELECAO', 24.90,
    'Guia da Sua Seleção',
    'Guia da Sua Seleção',
    'Your Team Guide',
    'A história completa da tua seleção nas Copas do Mundo: todas as participações, melhores campanhas, jogos inesquecíveis, artilheiros, recordes e curiosidades num guia simples de guardar.',
    'A história completa da tua seleção nos Mundiais: todas as participações, melhores campanhas, jogos inesquecíveis, goleadores, recordes e curiosidades num guia simples de guardar.',
    'The complete World Cup history of your team: every tournament, best campaigns, unforgettable games, top scorers, records and trivia — all in one easy-to-read guide.',
    '#', NULL, NULL, 1, 0
),
-- ─── 2. Histórico do Confronto (1,00€ · R$6,90) ─────────────────────────
(
    'historico-confronto', 'DUELO_HISTORICO', 6.90,
    'Histórico do Confronto',
    'Histórico do Confronto',
    'Head-to-Head History',
    'Antes do apito inicial: saldo do confronto, resultados históricos, jogos decisivos, curiosidades e factos rápidos para leres em 5 minutos.',
    'Antes do pontapé inicial: saldo do confronto, resultados históricos, jogos decisivos, curiosidades e factos rápidos para leres em 5 minutos.',
    'Before kick-off: all-time record, historic results, decisive games, quick facts and trivia — ready in 5 minutes.',
    '#', NULL, NULL, 1, 1
),
-- ─── 3. Copa em 20 Factos (2,49€ · R$15,90) ─────────────────────────────
(
    'copa-em-20-factos', 'ANALISE_PREMIUM', 15.90,
    'Copa em 20 Factos',
    'Copa em 20 Factos',
    'World Cup in 20 Facts',
    'Os 20 factos mais surpreendentes, recordes históricos e curiosidades que fazem qualquer conversa sobre a Copa ficar melhor. Perfeito para partilhar antes dos jogos.',
    'Os 20 factos mais surpreendentes, recordes históricos e curiosidades que fazem qualquer conversa sobre o Mundial ficar melhor. Perfeito para partilhar antes dos jogos.',
    'The 20 most surprising facts, historic records and trivia that make any World Cup conversation better. Perfect to share before the games.',
    '#', NULL, NULL, 1, 2
),
-- ─── 4. Dossier Copa 2026 — Pacote Completo (7,99€ · R$49,90) ───────────
(
    'copa-pass', 'COPA_PASS', 49.90,
    'Dossier Copa 2026 — Pacote Completo',
    'Dossier Copa 2026 — Pacote Completo',
    'Copa 2026 — Complete Package',
    'A melhor escolha para os grandes jogos: guias de seleções, históricos de confrontos, análises táticas, linhas do tempo, curiosidades e jogadores-chave. Acesso vitalício a todo o conteúdo Copa Insider.',
    'A melhor escolha para os grandes jogos: guias de seleções, históricos de confrontos, análises táticas, linhas do tempo, curiosidades e jogadores-chave. Acesso vitalício a todo o conteúdo Copa Insider.',
    'The best choice for big games: team guides, head-to-head history, tactical analysis, timelines, trivia and key players. Lifetime access to all Copa Insider content.',
    '#', NULL, NULL, 1, 3
);
