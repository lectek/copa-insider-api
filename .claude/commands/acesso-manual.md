# Conceder ou Revogar Acesso Manualmente

Gera o SQL para conceder ou revogar acesso de um utilizador a um produto Copa Insider,
sem depender do webhook Hotmart.

O utilizador vai indicar: email, slug do produto, e se é para conceder ou revogar.

## Slugs disponíveis

Lê `src/main/resources/db/migration/USER/V20260611_08__force_urls_e_ativo_copa_pass_e_factos.sql`
e lista os slugs activos. Os slugs de acesso são:
- `copa-pass`
- `copa-em-20-factos`
- `guia-selecao-portugal`
- `guia-selecao-brasil`
- `historico-confronto`
- `acesso-calendario-comparador`

## Para CONCEDER acesso

```sql
-- Conceder acesso a <email> ao produto <slug>
INSERT INTO copa_acesso (email, produto_slug, transacao, concedido_em)
VALUES ('<email_lowercase>', '<slug>', 'MANUAL-<YYYYMMDD>', NOW())
ON DUPLICATE KEY UPDATE concedido_em = NOW();
```

**Se for copa-pass**, adicionar também os produtos filhos:
```sql
INSERT INTO copa_acesso (email, produto_slug, transacao, concedido_em) VALUES
('<email>', 'copa-em-20-factos',          'MANUAL-<YYYYMMDD>', NOW()),
('<email>', 'guia-selecao-portugal',       'MANUAL-<YYYYMMDD>', NOW()),
('<email>', 'guia-selecao-brasil',         'MANUAL-<YYYYMMDD>', NOW()),
('<email>', 'historico-confronto',         'MANUAL-<YYYYMMDD>', NOW()),
('<email>', 'acesso-calendario-comparador','MANUAL-<YYYYMMDD>', NOW())
ON DUPLICATE KEY UPDATE concedido_em = NOW();
```

## Para REVOGAR acesso

```sql
-- Revogar acesso de <email> ao produto <slug>
DELETE FROM copa_acesso WHERE email = '<email_lowercase>' AND produto_slug = '<slug>';
```

## Instruções finais

1. Apresenta o SQL gerado formatado e pronto para copiar.
2. Lembra que este SQL deve ser corrido directamente na base de dados Railway
   (Railway dashboard → MySQL → Query).
3. Avisa que o acesso é efectivo imediatamente — o utilizador não precisa de fazer login novamente.
