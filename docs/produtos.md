# Produtos Copa Insider — O que é cada um, estado e próximos passos

> Referência rápida para perceber o estado actual de cada produto.
> Última actualização: Junho 2026

---

## Como os produtos funcionam tecnicamente

```
Compra no Hotmart → webhook POST /webhooks/hotmart
                 → CopaCompraEntity gravada na DB
                 → Conta criada (se email novo) com senha temporária
                 → CopaAcessoEntity criada (email + slug do produto)
                 → E-mail enviado ao comprador
                 → Utilizador faz login → Spring Security vê o acesso → conteúdo desbloqueado
```

O mapeamento é automático: `HotmartWebhookController` usa o `hotmart_product_id` do webhook
para encontrar o `CopaProdutoEntity` na DB pelo campo `hotmart_product_id`, depois usa
o `slug` do produto para gravar o acesso. Não há código a alterar para novos produtos —
só a DB precisa de estar configurada.

---

## 1. Copa Pass — Dossier Copa 2026

| Campo | Valor |
|---|---|
| Tipo | `COPA_PASS` |
| Slug | `copa-pass` |
| ID Hotmart | 7915316 |
| Preço | €7,99 |
| Estado | **ACTIVO ✅** |

### O que inclui
O produto "guarda-chuva": dá acesso a TODOS os outros automaticamente.
- Calendário completo (`/calendario`) com filtros por grupo/fase
- Comparador de seleções (`/comparar`) com histórico completo de confrontos
- Copa em 20 Factos, Guia Portugal, Guia Brasil, Histórico do Confronto

### Falta melhorar
- Email pós-compra não menciona que o Copa Pass inclui tudo — o comprador não sabe que tem acesso a `/calendario` e `/comparar`
- Não existe `/conta/acessos` onde o utilizador veja os produtos que comprou

---

## 2. Copa em 20 Factos

| Campo | Valor |
|---|---|
| Tipo | `ANALISE_PREMIUM` |
| Slug | `copa-em-20-factos` |
| ID Hotmart | 7915301 |
| Preço | €3,90 |
| Estado | **ACTIVO ✅** |

### O que é
Acesso premium à página `/factos` com factos exclusivos sobre a Copa.
Actualmente `/factos` é público — a versão paga provavelmente desbloqueia
uma selecção especial ou volume maior de factos.

### A verificar
- O gate de acesso em `/factos` está activo para compradores deste produto?
- O conteúdo exclusivo existe no sistema ou é igual ao gratuito?

---

## 3. Guia de Seleção — Portugal

| Campo | Valor |
|---|---|
| Tipo | `GUIA_SELECAO` |
| Slug | `guia-selecao-portugal` |
| ID Hotmart | 7907347 |
| Preço | €3,99 |
| Estado | **ACTIVO ✅** |

### O que é
Guia digital para a seleção portuguesa na Copa 2026.
Entregue via página `/guia/guia-selecao-portugal` (template `produto.html`).

### A verificar
- O conteúdo do guia está a ser entregue corretamente na página `/guia/{slug}`?
- O template `produto.html` mostra tudo o que prometemos ao comprador?

---

## 4. Guia de Seleção — Brasil

| Campo | Valor |
|---|---|
| Tipo | `GUIA_SELECAO` |
| Slug | `guia-selecao-brasil` |
| ID Hotmart | 7909721 |
| Preço | €3,99 |
| Estado | **ACTIVO ✅** |

### O que é
Guia digital para a seleção brasileira na Copa 2026.
Mesmo mecanismo de entrega que Portugal.

---

## 5. Histórico do Confronto

| Campo | Valor |
|---|---|
| Tipo | `DUELO_HISTORICO` |
| Slug | `historico-confronto` |
| ID Hotmart | 7910328 |
| Preço | €1,99 |
| Estado | **ACTIVO ✅** |

### O que é
Acesso ao comparador `/comparar` com histórico completo de qualquer rivalidade.
Versão individual (sem o resto do Copa Pass) para quem quer só os confrontos.

---

## 6. Calendário & Comparador (acesso isolado)

| Campo | Valor |
|---|---|
| Tipo | `ACESSO_FERRAMENTAS` |
| Slug | `acesso-calendario-comparador` |
| ID Hotmart | — |
| Preço | €3,99 |
| Estado | **DESCONTINUADO ❌** |

### Situação
Substituído pelo Copa Pass e pelo "Histórico do Confronto" separado.
Os paywalls em `/calendario` e `/comparar` apontam directamente para o Copa Pass.

---

## Mapa de estado

```
                    À venda  Conteúdo     Entrega
                    no site  no sistema   verificada
─────────────────────────────────────────────────────
Copa Pass           ✅        ✅            ⚠️ email fraco
Copa em 20 Factos   ✅        ⚠️ verificar  ⚠️ verificar
Guia Portugal       ✅        ⚠️ verificar  ⚠️ verificar
Guia Brasil         ✅        ⚠️ verificar  ⚠️ verificar
Histórico Confronto ✅        ✅            ⚠️ verificar
Cal. & Comparador   ❌        —            —
─────────────────────────────────────────────────────
```

---

## Próximos passos prioritários

### Verificações urgentes (antes de escalar vendas)
1. **Testar fluxo completo** de compra de cada produto → verificar acesso concedido → verificar que o conteúdo entregue corresponde ao que o comprador espera
2. **Verificar `produto.html`** — é esta a página de entrega dos Guias? Tem conteúdo suficiente?
3. **Email pós-compra do Copa Pass** — mencionar explicitamente que inclui `/calendario` e `/comparar`

### Funcionalidades em falta
1. **`/conta/acessos`** — utilizador autenticado vê os seus produtos e links directos
2. **Mais guias de seleção** — Portugal e Brasil estão, faltam os outros 46 (ou os mais populares: Argentina, França, Alemanha, Espanha, Inglaterra)
3. **Email melhorado** — links directos para cada produto comprado
