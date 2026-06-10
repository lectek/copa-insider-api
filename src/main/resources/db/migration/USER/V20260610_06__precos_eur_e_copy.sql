-- Preços EUR psicológicos e copy persuasivo nos produtos

-- Adiciona coluna preco_eur (compatível com MySQL 5.7)
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE() AND table_name = 'copa_produto' AND column_name = 'preco_eur');
SET @add_col = IF(@col_exists = 0,
    'ALTER TABLE copa_produto ADD COLUMN preco_eur DECIMAL(8,2) NULL',
    'SELECT 1');
PREPARE stmt FROM @add_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Preços EUR psicológicos (sem conversão automática de BRL)
UPDATE copa_produto SET preco_eur = 3.99 WHERE slug = 'guia-selecao-portugal';
UPDATE copa_produto SET preco_eur = 3.99 WHERE slug = 'guia-selecao-brasil';
UPDATE copa_produto SET preco_eur = 1.99 WHERE slug = 'historico-confronto';
UPDATE copa_produto SET preco_eur = 2.49 WHERE slug = 'copa-em-20-factos';
UPDATE copa_produto SET preco_eur = 7.99 WHERE slug = 'copa-pass';

-- Copy persuasivo (pt-PT)
UPDATE copa_produto SET
    nome_pt_pt = '🇵🇹 Guia da Seleção de Portugal',
    desc_pt_pt = 'Portugal já chegou às meias em 2006. CR7 está na última. Histórico completo, jogadores a seguir e o que esperar em 2026. Lês em 5 minutos. Ficas preparado para 90.'
WHERE slug = 'guia-selecao-portugal';

UPDATE copa_produto SET
    nome_pt_pt = '🇧🇷 Guia da Seleção do Brasil',
    desc_pt_pt = '5 títulos. Em 2026 joga em casa pela primeira vez. Histórico completo, artilheiros históricos, jogadores-chave e o que esperar desta geração de Vinicius e Endrick.'
WHERE slug = 'guia-selecao-brasil';

UPDATE copa_produto SET
    nome_pt_pt = 'Histórico do Confronto',
    desc_pt_pt = 'Antes do apito inicial, sabe tudo. Quem leva a melhor historicamente, os jogos mais decisivos, curiosidades que ninguém conta. Lês em 3 minutos. Ficas preparado para 90.'
WHERE slug = 'historico-confronto';

UPDATE copa_produto SET
    nome_pt_pt = 'Copa em 20 Factos',
    desc_pt_pt = 'O golo mais rápido: 11 segundos. A maior goleada: 10-1. O guardião que ganhou a Bola de Ouro. Os 20 factos que fazem qualquer conversa sobre a Copa ficar melhor — hoje.'
WHERE slug = 'copa-em-20-factos';

UPDATE copa_produto SET
    nome_pt_pt = 'Copa Pass — Acesso Total',
    desc_pt_pt = 'Um passe. Toda a Copa. Todos os guias, todos os duelos históricos, todas as curiosidades. Acesso vitalício. O melhor negócio para quem não quer perder nada.'
WHERE slug = 'copa-pass';
