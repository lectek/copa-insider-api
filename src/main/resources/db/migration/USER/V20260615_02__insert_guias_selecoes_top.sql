-- Novos guias de seleções: França, Argentina, Inglaterra, Espanha
-- hotmart_url a preencher no admin após criar produtos no Hotmart
INSERT IGNORE INTO copa_produto
    (slug, tipo, preco, preco_eur,
     nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     slug_time1, hotmart_url, hotmart_product_id, ativo, ordem)
VALUES
(
    'guia-selecao-franca', 'GUIA_SELECAO', 14.90, 3.99,
    '🇫🇷 Guia da Seleção da França',
    '🇫🇷 Guia da Seleção da França',
    '🇫🇷 France National Team Guide',
    'Bicampeã (1998, 2018). Mbappé, Griezmann e a geração mais equilibrada do torneio. Histórico completo, análise tática e o que esperar da França no Mundial 2026.',
    'Bicampeã (1998, 2018). Mbappé, Griezmann e a geração mais equilibrada do torneio. Histórico completo, análise tática e o que esperar da França no Mundial 2026.',
    'Two-time champions (1998, 2018). Mbappé, Griezmann. Full history, tactical analysis and what to expect from France at the 2026 World Cup.',
    'franca', NULL, NULL, 1, 5
),
(
    'guia-selecao-argentina', 'GUIA_SELECAO', 14.90, 3.99,
    '🇦🇷 Guia da Seleção da Argentina',
    '🇦🇷 Guia da Seleção da Argentina',
    '🇦🇷 Argentina National Team Guide',
    'Tri-campeã (1978, 1986, 2022). Messi defende o título em 2026. O que faz a Argentina tão especial e o que esperar desta geração histórica em solo norte-americano.',
    'Tri-campeã (1978, 1986, 2022). Messi defende o título em 2026. O que faz a Argentina tão especial e o que esperar desta geração histórica em solo norte-americano.',
    'Three-time champions (1978, 1986, 2022). Messi defends the title in 2026. Full history, key players and what to expect from this historic generation.',
    'argentina', NULL, NULL, 1, 6
),
(
    'guia-selecao-inglaterra', 'GUIA_SELECAO', 14.90, 3.99,
    '🏴󠁧󠁢󠁥󠁮󠁧󠁿 Guia da Seleção da Inglaterra',
    '🏴󠁧󠁢󠁥󠁮󠁧󠁿 Guia da Seleção da Inglaterra',
    '🏴󠁧󠁢󠁥󠁮󠁧󠁿 England National Team Guide',
    'Campeã em 1966. 60 anos de espera. Kane, Bellingham e uma geração que carrega o peso da história. Histórico, momentos decisivos e projeções para 2026.',
    'Campeã em 1966. 60 anos de espera. Kane, Bellingham e uma geração que carrega o peso da história. Histórico, momentos decisivos e projeções para 2026.',
    'Champions in 1966. 60 years of waiting. Kane, Bellingham. Full history, defining moments and what 2026 could mean for England.',
    'inglaterra', NULL, NULL, 1, 7
),
(
    'guia-selecao-espanha', 'GUIA_SELECAO', 14.90, 3.99,
    '🇪🇸 Guia da Seleção da Espanha',
    '🇪🇸 Guia da Seleção da Espanha',
    '🇪🇸 Spain National Team Guide',
    'Campeã em 2010. Yamal, Pedri, Morata. A nova geração espanhola que dominou o Euro 2024 está pronta para reconquistar o mundo em 2026. Histórico e análise completa.',
    'Campeã em 2010. Yamal, Pedri, Morata. A nova geração espanhola que dominou o Euro 2024 está pronta para reconquistar o mundo em 2026. Histórico e análise completa.',
    'Champions in 2010. Yamal, Pedri, Morata. The new Spanish generation that dominated Euro 2024 is ready to conquer the world again in 2026.',
    'espanha', NULL, NULL, 1, 8
);
