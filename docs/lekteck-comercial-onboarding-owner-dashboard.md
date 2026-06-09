# Lekteck - Plano Comercial, Onboarding e Owner Dashboard (Canonico)

## Objetivo
Consolidar em um unico arquivo as decisoes de:
1. planos e monetizacao;
2. onboarding pos-pagamento;
3. dashboard do dono da plataforma (owner);
4. rota de execucao com prompts prontos.

Este arquivo evita duplicacao com os demais docs.

---

## Escopo de plataforma
1. `Lekteck` + `Embalando` no modelo multitenant.
2. `SaudeMaisFarma` separada em regras de negocio, mas sincronizada no mesmo repositorio.
3. Referencia de arquitetura/deploy: `docs/estrategia-monorepo-railway-multitenant-e-separado.md`.

---

## Planos oficiais
## Free
1. Preco: `R$ 0,00`.
2. Comissao: `1%` sobre vendas.
3. Anuncios: habilitados.
4. Sem: rota, IA operacional, catalogo nacional, modulo farmacia (quando nao contratado via add-on).

## Start
1. Preco: `R$ 99,90`.
2. Comissao: `0%`.
3. Anuncios: habilitados.
4. Sem: rota, IA operacional, catalogo nacional, modulo farmacia, atendente IA (salvo add-on).

## Plus
1. Preco: `R$ 199,90`.
2. Comissao: `0%`.
3. Anuncios: habilitados.
4. Com: rota, IA operacional, catalogo nacional, modulo farmacia.
5. Sem: atendente IA (salvo add-on).

## Premium
1. Preco: `R$ 399,90`.
2. Comissao: `0%`.
3. Anuncios: desabilitados.
4. Com: todas as funcionalidades, incluindo atendente IA.

---

## Add-ons oficiais
1. Add-on avulso por ferramenta: `R$ 49,90/mes`.
2. Atendente IA completo: `R$ 129,90/mes`.

Add-ons avulsos sugeridos:
1. `delivery_route`
2. `ai_ops`
3. `national_catalog`
4. `pharmacy_module`
5. `no_ads`

Regra de entitlement:
1. Feature ativa se `feature_do_plano OR addon_ativo`.

---

## Regras de cobranca
1. Upgrade no meio do ciclo: pro-rata imediato.
2. Downgrade/cancelamento: aplica no proximo ciclo.
3. Comissao de 1%: somente Free (ate nova regra explicita).
4. Anuncios: Free/Start/Plus ativos por padrao; Premium sem anuncios.
5. Tudo deve gerar lancamento de auditoria financeira.

---

## Onboarding pos-pagamento (wizard)
## Passo 1 - Identidade
1. Upload de logo.
2. Definir 3 cores principais.
3. Nome da loja.
4. Nome da IA da loja.

## Passo 2 - Tipo de comercio
1. `LOCAL`
2. `PHARMACY`

## Passo 3 - Dados publicos
1. telefone/whatsapp;
2. endereco;
3. email comercial;
4. horario de funcionamento;
5. proposito;
6. texto "sobre nos";
7. redes sociais (opcional).

## Passo 4 - Importacao inicial de produtos
1. Upload CSV/PDF.
2. Leitura automatica.
3. Preview para confirmacao humana.
4. Confirmar:
   - produto novo -> cria;
   - produto existente -> soma estoque.

## Passo 5 - Revisao
1. Plano e add-ons ativos.
2. Features liberadas/bloqueadas.
3. Resumo de importacao (lidos/inseridos/atualizados/erros).
4. Publicar loja.

---

## Owner Dashboard (plataforma)
Visao exclusiva de `ROLE_PLATFORM_OWNER`.

## KPIs principais
1. Receita total.
2. MRR.
3. Receita por fonte: assinatura, comissao, anuncios, add-ons.
4. GMV total e GMV por plano.
5. Churn de tenants.
6. Top modulos vendidos/ativados.

## Blocos de tela
1. Resumo financeiro.
2. Planos e base de tenants.
3. Comissao Free (1%).
4. Receita de anuncios.
5. Modulos/APIs mais ativados.
6. Risco de inadimplencia.

## APIs internas (owner)
1. `GET /owner/dashboard/summary`
2. `GET /owner/dashboard/revenue-by-source`
3. `GET /owner/dashboard/plans`
4. `GET /owner/dashboard/commissions`
5. `GET /owner/dashboard/ads`
6. `GET /owner/dashboard/modules`
7. `GET /owner/dashboard/risk`

---

## Modelo de dados minimo
1. `tenant`
2. `subscription`
3. `invoice`
4. `invoice_item`
5. `commission_ledger`
6. `ad_revenue_ledger`
7. `addon_catalog`
8. `tenant_addon_subscription`
9. `feature_usage_daily`
10. `tenant_feature_entitlement` (cache/materializado)

---

## Caminho de execucao (ordem)
1. Fechar modelo comercial (planos + add-ons + regras de cobranca).
2. Implementar tabelas billing/ledger.
3. Implementar motor de entitlement por plano + add-on.
4. Implementar onboarding wizard pos-pagamento.
5. Integrar leitor CSV/PDF com preview+confirmacao.
6. Implementar Owner Dashboard.
7. Instrumentar metricas e auditoria.
8. Liberar deploy gradual por feature flag.
9. Executar cards no board: `docs/lekteck-board-cards-p0-p1.md`.

---

## Prompt pack (execucao direta)
## Prompt A - Billing e planos
> Implemente o modulo de billing multitenant com planos Free/Start/Plus/Premium, comissao de 1% no Free, anuncios por plano e ciclo mensal com pro-rata para upgrade. Crie migrations Flyway, entidades, servicos e testes.

## Prompt B - Add-ons e entitlement
> Implemente add-ons (R$ 49,90 por ferramenta e atendente IA R$ 129,90), com regra final de acesso `plano OR addon`. Exponha endpoint para consulta de features ativas por tenant e cubra com testes de regressao.

## Prompt C - Onboarding wizard
> Implemente onboarding pos-pagamento em 5 passos (identidade, tipo de comercio, dados publicos, importacao inicial CSV/PDF com preview, revisao final). Persistir tudo em configuracoes de tenant e permitir continuar depois.

## Prompt D - Importacao inicial
> Reutilize o fluxo existente de importacao CSV/PDF para operar em modo staging + confirmacao. Ao confirmar: criar produtos novos e somar estoque nos existentes. Registrar auditoria completa de importacao.

## Prompt E - Owner Dashboard
> Crie o painel `ROLE_PLATFORM_OWNER` com KPIs de receita por fonte (assinatura/comissao/anuncios/add-ons), MRR, churn, top modulos, risco de inadimplencia e APIs internas `/owner/dashboard/*`.

## Prompt F - Frontend de planos
> Crie pagina de planos com comparativo claro, CTA de upgrade/add-ons e regras visuais por plano (anuncios, comissao, features bloqueadas). Incluir textos de transparencia de cobranca.

---

## Referencias canonicas (nao duplicar)
1. Estrategia de repositorio/deploy: `docs/estrategia-monorepo-railway-multitenant-e-separado.md`
2. Tenant e sincronizacao de banco: `docs/arquitetura-sincronizacao-bancos-tenant.md`
3. Regras invariantes de dominio: `docs/regras-negocio-cpap-e-invariantes.md`
4. Board de execucao: `docs/getshitdone-board-template.md`
5. Backlog estruturado: `docs/backlog-estruturacao-completa-p0-p1.md`

Regra:
1. Novas decisoes de plano/onboarding/owner entram primeiro neste arquivo.
2. Os demais docs devem apenas referenciar este arquivo quando o tema for comercial.
