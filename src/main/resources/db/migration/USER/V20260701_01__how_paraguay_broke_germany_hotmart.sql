-- Novo eBook: "How Paraguay Broke Germany"
-- Confronto específico Paraguai x Alemanha (última vitória do Paraguai
-- sobre a Alemanha em Copa do Mundo). Não faz parte do Copa Pass.
INSERT INTO copa_produto
    (slug, tipo, preco, preco_eur,
     nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     hotmart_url, hotmart_product_id, slug_time1, slug_time2, ativo, ordem)
VALUES (
    'how-paraguay-broke-germany', 'DUELO_HISTORICO', 4.99, 4.99,
    'How Paraguay Broke Germany', 'How Paraguay Broke Germany', 'How Paraguay Broke Germany',
    'A história da última vez em que o Paraguai venceu a Alemanha numa Copa do Mundo — uma das maiores zebras do torneio, contada em detalhe.',
    'A história da última vez em que o Paraguai venceu a Alemanha numa Copa do Mundo — uma das maiores zebras do torneio, contada em detalhe.',
    'The story of the last time Paraguay beat Germany at a World Cup — one of the tournament''s greatest upsets, told in full.',
    'https://pay.hotmart.com/C106551895W', 8036387, 'paraguai', 'alemanha', 1, 9
)
ON DUPLICATE KEY UPDATE
    nome_pt_pt = VALUES(nome_pt_pt),
    hotmart_url = VALUES(hotmart_url),
    ativo = VALUES(ativo);
