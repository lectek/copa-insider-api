# Produtos Copa Insider — O que é cada um e o que está a impedir

> Referência rápida para perceber o estado actual de cada produto no Hotmart/loja.
> Última actualização: Junho 2026

---

## Como os produtos funcionam tecnicamente

```
Compra no Hotmart → webhook POST /hotmart/webhook
                 → CopaCompraEntity gravada na DB
                 → e-mail enviado ao comprador
                 → CopaAcessoEntity criada (email + tipo)
                 → utilizador faz login → Spring Security vê o acesso → conteúdo desbloqueado
```

Cada `CopaProdutoEntity` tem:
- `slug` — identificador único
- `tipo` → enum `TipoCopaProduto`
- `hotmartUrl` — URL de pagamento no Hotmart
- `hotmartProductId` — ID numérico do produto no Hotmart (usado para mapear o webhook)
- `ativo` — toggle on/off na loja e na validação de acesso

---

## 1. Copa Pass — Dossier Copa 2026

| Campo | Valor |
|---|---|
| Tipo | `COPA_PASS` |
| Slug | `copa-pass` |
| Preço | €7,99 |
| Hotmart ID | `G106266908X` |
| Estado | **ACTIVO — À venda** |

### O que é
O produto principal e mais completo. Acesso ilimitado a:
- Calendário completo com filtros por grupo/fase
- Comparador de seleções (histórico completo de confrontos)
- Factos & curiosidades premium
- Futuras análises táticas e estatísticas avançadas

### O que está a funcionar
- Webhook do Hotmart a receber compras corretamente
- E-mail de confirmação enviado automaticamente
- Acesso concedido imediatamente após compra
- Paywall no `/calendario` e `/comparar` aponta directo para este produto

### O que ainda falta
- Página `/conta/acessos` para o utilizador ver o que comprou (Phase 3 do PLANO.md)
- E-mail pós-compra podia incluir links directos para o conteúdo (actualmente só texto genérico)

---

## 2. Guia de Seleção

| Campo | Valor |
|---|---|
| Tipo | `GUIA_SELECAO` |
| Slug | `guia-{nome-selecao}` (ex: `guia-brasil`, `guia-portugal`) |
| Preço previsto | €1,99 por seleção |
| Hotmart ID | Não configurado |
| Estado | **INACTIVO — Não à venda** |

### O que é
Um PDF/página digital por seleção com:
- Plantel completo com posições e clubes
- Análise do treinador e sistema táctico
- Historial na Copa do Mundo
- Grupo, adversários e calendário de jogos
- Probabilidade de progressão e estrelas a seguir

### O que está a impedir

**Bloqueio principal: Conteúdo não existe ainda**

- A entidade `CopaProdutoEntity` suporta `slugTime1` para associar a seleção, mas não há geração automática do conteúdo do guia
- Seria necessário criar um template de guia em PDF ou uma página web premium por seleção
- O `ProductPromptFactory.java` existe (sugere que houve intenção de usar AI para gerar conteúdo) mas não está ligado ao fluxo de entrega

**Bloqueio secundário: Volume**
- São 48 seleções × €1,99 = muitos produtos para gerir no Hotmart (um produto por seleção ou um produto que desbloqueia qual seleção?)
- Decisão de arquitectura: produto único com parâmetro de seleção vs. 48 produtos separados no Hotmart

**O que seria necessário para activar:**
1. Definir formato do guia (PDF gerado, página web, ou ambos)
2. Criar template de conteúdo por seleção
3. Criar produto(s) no Hotmart e configurar `hotmartProductId`
4. Mapear `hotmartProductId` → seleção no webhook handler

---

## 3. Duelo Histórico

| Campo | Valor |
|---|---|
| Tipo | `DUELO_HISTORICO` |
| Slug | `duelo-{selecao1}-vs-{selecao2}` (ex: `duelo-brasil-vs-argentina`) |
| Preço previsto | €1,99 a €2,99 por rivalidade |
| Hotmart ID | Não configurado |
| Estado | **INACTIVO — Não à venda** |

### O que é
Um relatório profundo sobre uma rivalidade específica:
- Historial completo de todos os confrontos (já temos os dados no código)
- Análise do confronto específico na Copa 2026 (se houver)
- Lendas de cada seleção (já implementado no `/comparar`)
- Estatísticas avançadas (domínio, gols, fases críticas)

### O que está a impedir

**Bloqueio principal: O conteúdo gratuito já entrega quase tudo**

O `/comparar` já mostra o histórico completo, o encontro na Copa 2026, e as lendas para utilizadores com Copa Pass. O produto "Duelo Histórico" separado seria redundante a não ser que ofereça algo extra significativo (ex: análise táctica em PDF, vídeos, etc.).

**Bloqueio secundário: Indefinição de valor**
- O que tem o "Duelo Histórico" pago que o `/comparar` com Copa Pass não tem?
- Se a resposta for "nada diferente", faz mais sentido folding este produto dentro do Copa Pass (que já foi feito)

**O que seria necessário para activar:**
1. Definir o conteúdo exclusivo que justifica compra separada (ex: relatório PDF com análise táctica)
2. Criar produto no Hotmart e configurar `hotmartProductId`  
3. Ligar `slugTime1` + `slugTime2` ao webhook para saber qual duelo foi comprado

---

## 4. Análise Premium

| Campo | Valor |
|---|---|
| Tipo | `ANALISE_PREMIUM` |
| Slug | `analise-{tema}` |
| Preço previsto | €2,99 a €4,99 |
| Hotmart ID | Não configurado |
| Estado | **INACTIVO — Não à venda** |

### O que é
Relatórios de análise táctica e estatística em profundidade:
- Análise das favoritas à Copa (Brasil, França, Argentina, Portugal, Espanha)
- Análise do Grupo da Morte
- Previsão estatística de artilheiros
- Análise do sistema de jogo de cada seleccionador
- Relatórios pós-fase de grupos

### O que está a impedir

**Bloqueio principal: Nenhum conteúdo criado**

Não existe sistema de escrita, aprovação ou entrega de análises. O `ProductPromptFactory.java` sugere integração com AI para geração de conteúdo mas não está operacional.

**Bloqueio secundário: Janela de tempo**

A Copa já começou (12 Junho 2026). Análises pré-torneio já perderam oportunidade. O foco devia mudar para:
- Análises pós-fase de grupos (publicar à medida que avança)
- Análises de jogos específicos (resumo pós-jogo, já temos `/jogo/{id}/resumo`)

**O que seria necessário para activar:**
1. Escrever pelo menos 2-3 análises de exemplo para ter produto mínimo
2. Criar página de entrega do conteúdo (pode ser `/guia/{slug}` que já existe — ver `CopaLojaController.java`)
3. Configurar produto no Hotmart

---

## 5. Acesso Ferramentas

| Campo | Valor |
|---|---|
| Tipo | `ACESSO_FERRAMENTAS` |
| Slug | `acesso-ferramentas` |
| Preço previsto | €2,99 a €3,99 |
| Hotmart ID | Não configurado |
| Estado | **DESCONTINUADO / INACTIVO** |

### O que era
Acesso avulso apenas ao Calendário + Comparador, sem o conteúdo editorial do Copa Pass.

### Por que foi descontinuado
- A diferença de preço face ao Copa Pass (€7,99) não justificava um produto separado
- Criava confusão para o comprador ("o que é que tenho vs. o que falta?")
- Os paywalls em `/calendario` e `/comparar` apontam agora directamente para o Copa Pass

### Situação actual
- O botão de compra nos paywalls foi actualizado para ir directamente para `pay.hotmart.com/G106266908X`
- Este produto não deve ser reactivado isoladamente
- Se houver necessidade de um "acceso só às ferramentas" mais barato, reconsiderar o preço do Copa Pass

---

## Mapa de prioridade de activação

```
Estado actual (Junho 2026):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

 Copa Pass        ████████████████  ACTIVO ✅
 Guia Seleção     ░░░░░░░░░░░░░░░░  Bloqueado: sem conteúdo
 Duelo Histórico  ░░░░░░░░░░░░░░░░  Bloqueado: sem valor diferenciado claro
 Análise Premium  ░░░░░░░░░░░░░░░░  Bloqueado: sem conteúdo (oportunidade agora)
 Acesso Ferramentas ──────────────  Descontinuado

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Próximo produto a activar recomendado:
→ Análise Premium (ex: "Análise do Grupo da Morte — €2,99")
  Motivo: conteúdo editorial que podemos criar agora, Copa já em jogo,
  janela de 4 semanas até à final, sem dependências técnicas bloqueantes
  além de escrever o conteúdo e criar o produto no Hotmart.
```

---

## O que precisa de acontecer para lançar um produto novo (checklist)

- [ ] Conteúdo criado e aprovado
- [ ] Produto criado no Hotmart com preço correcto
- [ ] `hotmartProductId` configurado na DB via admin (`/admin/copa/produtos`)
- [ ] `hotmartUrl` configurado na DB via admin
- [ ] Webhook mapeado automaticamente: `HotmartWebhookController.java` usa `hotmartProductId` para encontrar o `CopaProdutoEntity` pelo campo `hotmart_product_id` na DB, e concede acesso pelo `slug` do produto — não há código adicional a alterar, só a DB precisa de estar configurada
- [ ] Produto marcado como `ativo = true` no admin
- [ ] Página de entrega do conteúdo existe (rota `/guia/{slug}` com template correspondente)
- [ ] Teste end-to-end: simular webhook → verificar acesso concedido → verificar conteúdo disponível
