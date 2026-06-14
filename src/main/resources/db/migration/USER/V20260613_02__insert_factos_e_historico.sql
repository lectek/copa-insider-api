-- copa-em-20-factos e historico-confronto nao existem na DB de producao.
-- V20260613_01 fez UPDATE (noop). Este script insere se nao existirem.
INSERT IGNORE INTO copa_produto
    (slug, tipo, preco, preco_eur,
     nome_pt_br, nome_pt_pt, nome_en,
     desc_pt_br, desc_pt_pt, desc_en,
     hotmart_url, hotmart_product_id,
     ativo, ordem)
VALUES
(
    'copa-em-20-factos', 'ANALISE_PREMIUM', 9.90, 3.90,
    'Copa em 20 Factos', 'Copa em 20 Factos', 'World Cup in 20 Facts',
    'Os 20 factos mais surpreendentes da história da Copa do Mundo.',
    'Os 20 factos mais surpreendentes da história do Campeonato do Mundo.',
    'The 20 most surprising facts in World Cup history.',
    'https://pay.hotmart.com/T106266827K', 7915301,
    1, 1
),
(
    'historico-confronto', 'DUELO_HISTORICO', 6.90, 4.90,
    'Histórico do Confronto', 'Histórico do Confronto', 'Match History',
    'Análise completa do histórico entre as selecções ao longo dos anos.',
    'Análise completa do histórico entre as selecções ao longo dos anos.',
    'Full historical analysis of head-to-head records.',
    'https://pay.hotmart.com/L106252926D', 7910328,
    1, 4
);
