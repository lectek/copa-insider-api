-- Remove produto genérico e adiciona guias por seleção
DELETE FROM copa_produto WHERE slug = 'guia-selecao';

INSERT IGNORE INTO copa_produto
    (slug, tipo, preco, nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     hotmart_url, slug_time1, ativo, ordem)
VALUES
(
    'guia-selecao-portugal', 'GUIA_SELECAO', 24.90,
    '🇵🇹 Guia da Seleção de Portugal',
    '🇵🇹 Guia da Seleção de Portugal',
    '🇵🇹 Portugal Team Guide',
    'A história completa de Portugal nas Copas do Mundo: todas as participações, melhores campanhas, jogos inesquecíveis, artilheiros, recordes e curiosidades.',
    'A história completa de Portugal nos Mundiais: todas as participações, melhores campanhas, jogos inesquecíveis, goleadores, recordes e curiosidades.',
    'The complete World Cup history of Portugal: every tournament, best campaigns, unforgettable games, top scorers, records and trivia.',
    '#', 'portugal', 1, 0
),
(
    'guia-selecao-brasil', 'GUIA_SELECAO', 24.90,
    '🇧🇷 Guia da Seleção do Brasil',
    '🇧🇷 Guia da Seleção do Brasil',
    '🇧🇷 Brazil Team Guide',
    'A história completa do Brasil nas Copas do Mundo: 5 títulos, campanhas épicas, Pelé, Ronaldo, Neymar, Vinicius e todos os momentos que marcaram a história.',
    'A história completa do Brasil nos Mundiais: 5 títulos, campanhas épicas, Pelé, Ronaldo, Neymar, Vinicius e todos os momentos que marcaram a história.',
    'The complete World Cup history of Brazil: 5 titles, epic campaigns, Pelé, Ronaldo, Neymar, Vinicius and all the moments that shaped history.',
    '#', 'brasil', 0, 1
);
