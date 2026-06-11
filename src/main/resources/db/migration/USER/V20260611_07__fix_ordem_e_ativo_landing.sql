-- Garante que os 4 produtos principais estão ativos e na ordem certa para a landing
-- Copa Pass: destaque (index 0 da landing = MAIS PROCURADO)
UPDATE copa_produto SET ativo = 1, ordem = 0
WHERE slug = 'copa-pass' AND hotmart_url IS NOT NULL AND hotmart_url != '#';

-- Copa em 20 Factos
UPDATE copa_produto SET ativo = 1, ordem = 1
WHERE slug = 'copa-em-20-factos' AND hotmart_url IS NOT NULL AND hotmart_url != '#';

-- Guia Seleção Portugal (representante do grupo GUIA_SELECAO)
UPDATE copa_produto SET ativo = 1, ordem = 2
WHERE slug = 'guia-selecao-portugal' AND hotmart_url IS NOT NULL AND hotmart_url != '#';

-- Guia Seleção Brasil (segundo do grupo GUIA_SELECAO, não aparece como card separado)
UPDATE copa_produto SET ativo = 1, ordem = 3
WHERE slug = 'guia-selecao-brasil' AND hotmart_url IS NOT NULL AND hotmart_url != '#';

-- Histórico Confronto
UPDATE copa_produto SET ativo = 1, ordem = 4
WHERE slug = 'historico-confronto' AND hotmart_url IS NOT NULL AND hotmart_url != '#';
