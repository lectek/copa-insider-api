# Lekteck - Cards P0/P1 (Execucao)

Baseado em:
1. `docs/lekteck-comercial-onboarding-owner-dashboard.md`
2. `docs/getshitdone-board-template.md`

---

Titulo: [P0][BACKEND] Modelo de planos, add-ons e entitlement

Objetivo:
- Implementar fonte unica de verdade para liberar funcionalidades por plano e add-on.

Escopo:
1. Criar tabelas `plan`, `plan_feature`, `addon_catalog`, `tenant_addon_subscription`.
2. Criar servico `TenantEntitlementService` com regra `plano OR addon`.
3. Expor endpoint interno `GET /internal/tenant/{tenantId}/entitlements`.

Fora de escopo:
1. Cobranca real.

Dependencias:
1. Migrations Flyway.

Criterios de aceite:
1. Tenant Free sem add-on nao acessa rota/ia/catalogo nacional.
2. Tenant Start com add-on de rota acessa rota.
3. Tenant Plus acessa funcionalidades do plano sem add-on.

Testes:
1. unitario entitlement
2. integracao endpoint interno
3. regressao de bloqueio por plano

Riscos:
1. Vazamento de permissao entre tenants.

Rollback:
1. Desativar gate por feature flag global.

---

Titulo: [P0][BACKEND] Billing core (assinatura, comissao, anuncios, add-ons)

Objetivo:
- Registrar receita por fonte e permitir visao financeira confiavel no owner dashboard.

Escopo:
1. Criar `subscription`, `invoice`, `invoice_item`, `commission_ledger`, `ad_revenue_ledger`.
2. Implementar lancamentos de:
   - assinatura mensal
   - comissao 1% no Free
   - receita de anuncios
   - add-ons
3. Criar endpoint consolidado de receita por fonte.

Fora de escopo:
1. Gateway de pagamento externo completo.

Dependencias:
1. Card de planos/entitlement.

Criterios de aceite:
1. Owner consulta receita por periodo e fonte.
2. Comissao de 1% so aparece em tenants Free.
3. Premium nao gera receita de anuncios.

Testes:
1. unitario de regras financeiras
2. integracao com banco
3. regressao de calculo mensal

Riscos:
1. Inconsistencia em pro-rata.

Rollback:
1. Manter somente calculo fechado por ciclo mensal.

---

Titulo: [P0][BACKEND] Onboarding pos-pagamento (wizard 5 passos)

Objetivo:
- Fazer novo cliente configurar loja e publicar rapido apos contratar.

Escopo:
1. Criar estado de onboarding por tenant.
2. Implementar APIs dos 5 passos:
   - identidade
   - tipo de comercio
   - dados publicos
   - importacao inicial
   - revisao/publicacao
3. Persistir dados em configuracoes tenant-scoped.

Fora de escopo:
1. Editor visual avancado de pagina.

Dependencias:
1. Entitlement ativo.

Criterios de aceite:
1. Fluxo pode ser pausado e retomado.
2. Footer/sobre usam telefone/endereco/proposito salvos.
3. Publicacao final marca onboarding concluido.

Testes:
1. unitario por passo
2. integracao fluxo completo
3. regressao de retomada

Riscos:
1. Fluxo quebrar em tenant sem logo.

Rollback:
1. Permitir fallback com placeholders padrao.

---

Titulo: [P0][BACKEND] Importacao inicial CSV/PDF com preview e confirmacao

Objetivo:
- Garantir importacao segura com confirmacao humana antes de alterar estoque.

Escopo:
1. Reusar parser atual CSV/PDF para staging.
2. Exibir preview com status por linha (novo/existente/erro).
3. Confirmacao transacional:
   - novo -> cria
   - existente -> soma estoque
4. Registrar `import_history`.

Fora de escopo:
1. OCR avancado de PDF escaneado.

Dependencias:
1. Onboarding wizard.

Criterios de aceite:
1. Mesmo arquivo nao confirma duas vezes (hash por tenant).
2. Erros nao derrubam todo lote sem feedback.
3. Auditoria mostra lidos/inseridos/atualizados/erros.

Testes:
1. unitario parser
2. integracao confirmacao
3. regressao de idempotencia

Riscos:
1. PDF sem estrutura tabular.

Rollback:
1. Manter CSV como fallback obrigatorio.

---

Titulo: [P1][BACKEND] Owner Dashboard APIs

Objetivo:
- Fornecer dados consolidados da plataforma para decisao comercial.

Escopo:
1. Criar rotas:
   - `/owner/dashboard/summary`
   - `/owner/dashboard/revenue-by-source`
   - `/owner/dashboard/plans`
   - `/owner/dashboard/commissions`
   - `/owner/dashboard/ads`
   - `/owner/dashboard/modules`
   - `/owner/dashboard/risk`
2. Proteger com `ROLE_PLATFORM_OWNER`.

Fora de escopo:
1. BI externo.

Dependencias:
1. Billing core.

Criterios de aceite:
1. APIs retornam dados por periodo.
2. Nenhum admin de tenant acessa essas rotas.
3. KPIs batem com ledger.

Testes:
1. unitario agregacoes
2. integracao seguranca
3. regressao de filtros

Riscos:
1. Query pesada.

Rollback:
1. Cache por periodo para reduzir carga.

---

Titulo: [P1][FRONTEND] Tela de planos e add-ons (Lekteck)

Objetivo:
- Mostrar oferta clara e aumentar conversao/upgrade.

Escopo:
1. Pagina de planos com comparativo de features.
2. Bloco de add-ons com preco e status atual.
3. Textos de transparencia:
   - anuncios por plano
   - comissao 1% no Free
   - pro-rata em upgrade

Fora de escopo:
1. Teste A/B.

Dependencias:
1. Entitlement e billing.

Criterios de aceite:
1. Usuario entende diferenca entre planos em 1 tela.
2. CTA de upgrade/add-on funcionando.
3. Estado atual do plano aparece corretamente.

Testes:
1. unitario de componentes
2. integracao de fluxo de upgrade
3. regressao responsiva

Riscos:
1. Ambiguidade comercial.

Rollback:
1. Versao simplificada sem add-ons ate estabilizar.

---

Titulo: [P1][FRONTEND] Owner Dashboard UI

Objetivo:
- Entregar painel executivo da plataforma com KPIs de negocio.

Escopo:
1. Home com cards de receita total, MRR, churn e receita por fonte.
2. Graficos por periodo (assinatura/comissao/anuncios/add-ons).
3. Tabelas de top tenants e top modulos.

Fora de escopo:
1. Exportacao XLS/PDF.

Dependencias:
1. Owner Dashboard APIs.

Criterios de aceite:
1. Carregamento em tempo aceitavel.
2. Filtros por periodo funcionam em todos os blocos.
3. Sem vazamento de dados para perfis nao-owner.

Testes:
1. unitario de render
2. integracao com APIs
3. regressao de permissao

Riscos:
1. Divergencia entre KPI da API e UI.

Rollback:
1. Mostrar somente cards principais sem graficos.

---

## Ordem recomendada no board
1. P0 Planos/add-ons/entitlement
2. P0 Billing core
3. P0 Onboarding wizard
4. P0 Importacao inicial
5. P1 Owner Dashboard APIs
6. P1 Tela de planos/add-ons
7. P1 Owner Dashboard UI

