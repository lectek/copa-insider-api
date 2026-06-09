# Backlog tecnico (email marketing e configuracoes)

## Milestones
1) M1 - Fundacao email: fila + templates + envio transacional
2) M2 - Campanhas: CRUD + agendamento + segmentacao basica
3) M3 - Automacoes: carrinho abandonado + reengajamento + recompra
4) M4 - Engajamento: volta ao estoque + conteudo + cashback

## Epics e tarefas

### M1 - Fundacao email (P0)
- DB: tabelas `email_campaign`, `email_recipient`, `email_queue`, `email_delivery_log`
- Serviço: fila de envio (pending/sending/sent/failed), retries e throttling
- Templates: base + promocional + transacional (pedido/entrega)
- Admin UI: lista de envios + detalhes do log
- Observabilidade: logs por campanha e metricas basicas
- Observabilidade: contadores Micrometer para `processed`, `sent`, `retry` e `failed`, válvulas de alerta e documentação no README/Actuator

## Critérios de aceitação e observabilidade por milestone

### M1 - Fundacao email
- Fila cobrindo status `PENDING/SENDING/SENT/FAILED`, retries com backoff e logs de entrega por campanha.
- Templates base + transacional validados com testes de integração (`./mvnw -pl boot-app clean verify` ou `./mvnw -pl boot-app test -Dspring.profiles.active=test`).
- Workflow do `EmailDeliveryWorker` documentado no README, com métricas visíveis em `/actuator/metrics` e alertas acionados quando `email_campaign_worker.failed > 0`.
- Documentar dependências: env vars `APP_MAIL_*`, `APP_WEB_BASE_URL`, `EMAIL_DELIVERY_QUEUE`, banco MySQL `DEV_DATASOURCE_*` e fila `email_delivery`.

### M2 - Campanhas
- CRUD completo + agendamento em fila com preview/cancelamento, cobertos por testes de contratos de API (controller + service).
- Segmentação básica com filtros categoria/recência/ticket coberta por cenários de integração, incluindo dados fake em `mocks/` e `infra/`. Testes repetidos com `./mvnw -pl boot-app test -Dspring.profiles.active=test`.
- Observabilidade: dashboards de campanha e alertas por métricas `email_campaign.worker.*` para pausar/resumir campanhas.

### M3 - Automacoes
- Regras de marketing (carrinho abandonado, reengajamento, recompra) executadas pelo scheduler; cada automação deve gerar eventos `Micrometer` e logs estruturados.
- Validar entrega de cupons com testes end-to-end sensíveis ao tempo (`@Testcontainers` com controle de relógio se necessário).
- Enfileiramento para `email_campaign_queue` e dependências (Firebird, MySQL) testadas antes de ativar automações.

### M4 - Engajamento
- Volta ao estoque, conteúdo educativo e cashback precisam ter gatilhos identificáveis (`subscribe`, `agenda`, `validação de saldo`) e dashboards de monitoramento claro no admin.
- Confirmar compatibilidade com storage (S3/Local) e Firebird via jobs existentes (`ProdutoSyncService`), garantindo alertas `app.estoque.alerta.*` configurados antes do release.

## Dependências do plano API
- Banco: MySQL local ou Testcontainers (`DEV_DATASOURCE_*` / `spring.datasource.*` no `application-{perfil}.yml`).
- Legado Firebird: habilitar `legacy`/`firebird` com `FIREBIRD_*` e verificar `ProdutoLegacyService` antes de campanhas e conteúdos legados.
- SMTP/queue: `APP_MAIL_ENABLED`, `APP_MAIL_FROM`, `APP_MAIL_REPLY_TO`, `EMAIL_DELIVERY_QUEUE`, `email_delivery` e `email_campaign_queue` precisam estar contemplados em cada perfil.
- Storage: `S3StorageAdapter` ou `LocalStorageAdapter` com fallback documentado no README.

## Backlog detalhado das campanhas e automações (M2‑M4)
Este bloco expande o plano das milestones M2‑M4 alinhando-as aos passos 19‑40 do plano geral e aos endpoints de marketing documentados no README.

### M2 – Campanhas (P0-P1)
- Passos relacionados do plano 50: 19 (Carrinho), 26 (Notificações), 28 (Alertas), 29 (Promoções), 31 (Histórico de buscas) e 33 (Chatbot/Ajuda). A implementação de campanhas cruza esses pontos porque deve integrar carrinho, notificações e alertas visuais no admin.
- Tarefas:
  * Expandir CRUD de campanhas com campos de meta (categoria, recência, ticket médio, VIP/Inativos/Todos) e reuso dos templates base - garante aderência aos passos 29 e 33 (promoções e ajuda contextual).
  * Agendamento com preview, validação de campos, cancelamento/pausa e visualização de logs por campanha (planos 27/28 e 31 - central de avisos, alertas e histórico de envios).
  * Segmentação suportando filtros transversais (categoria comprada, dias desde última compra, faixa de ticket, status VIP/Inativo). Isso prepara para futuros passos (29-31) ao alimentar dados do histórico de buscas e preferências.
  * Integração com dashboards/alertas do admin e métricas `email_campaign.worker.*` para dar visibilidade (passos 27/38).
  * Testes de contratos (controller + service), usamos mock data em `mocks/marketing` e `infra/` helpers para simular públicos e público VIP/inativo.

### M3 – Automação (P1)
- Passos relacionados do plano 50: 19 (Carrinho), 20 (Checkout), 26 (Notificações), 28 (Alertas), 36 (Tracking), 41 (Segurança). As automações ligam o carrinho, notificações e alertas automatizados.
- Tarefas:
  * Carrinho abandonado (1h + 24h) disparando cupom, respeitando throttle/retries e batendo nas métricas `email_campaign_queue`. Analisar tokens e segurança (passo 41) ao construir links.
  * Reengajamento (30/60/90 dias) com base na última compra, com segmentação de saldo e priorização de clientes inativos (passo 26).
  * Recompra automática para medicamentos de uso contínuo; sincroniza com estoque (passo 18) e notifica quando estoque muda (passo 28) ou há alertas de baixa.
  * Template `mail/recompra` e métricas `recompra.automation.processed/skipped/enqueued` documentadas no README e revisadas no QA; audite o `email_campaign_queue` para garantir que duplicados não sejam reenfileirados.
  * Cada automação deve registrar tracking (passo 36) e eventos `Micrometer`, e disponibilizar logs para QA/ops.
  * Criar fim/macros para testes com `@Testcontainers`, relógio controlado, e garantir compatibilidade com Firebird/MySQL antes de ativar.

### M4 – Engajamento (P2)
- Passos relacionados do plano 50: 28 (Alertas), 29 (Promoções), 35 (Políticas), 38 (Observabilidade), 39-40 (Performance/Offline), 44 (Testes E2E).
- Tarefas:
  * Volta ao estoque (passo 28) com subscribe per product e notificação via pipeline `email_campaign_queue`; documentar triggers e validar compatibilidade com Firebird/ProdutoSync.
  * Conteúdo educativo + agenda (passo 29) sincronizado com marketing campaigns, com status e logs para QA.
  * Cashback/benefícios (passo 32) com alertas de saldo/validade, integrando os mesmos dashboards do admin e observabilidade `Actuator`/`metrics`.
  * Garantir performance e resiliência (passos 38-40) ao enviar emails (fila, caches, retries) e registrar falhas >0 para automação offline.
  * E2E/regressão (passos 43-44) ao final de cada ciclo, incluindo testes de OTP/email claim e fluxos de checkout para não quebrar o cliente final.

### Sincronização com o plano de 50 passos
- Sempre que uma campanha/automação tocar o cliente (carrinho, checkout, notificações, promoções), referenciar no backlog o passo correspondente do plano 50 para garantir consistência com o produto (por exemplo, campanhas com preview ligadas aos passos 29/31).
- Atualizar periodicamente a documentação (`README.md`, `README_DEV.md`, `docs/admin-frontend-refactor.md`, `docs/client-flow-wireframes.md`) com os resultados dos testes principais (passos 43-44) e feedbacks do QA para manter o roadmap vivo.
### M2 - Campanhas (P0)
- CRUD campanhas (nome, assunto, template, publico, agendamento)
- Segmentacao basica: categoria comprada, recencia, ticket medio
- Preview antes de enviar
- Cancelamento/pausa de campanha
- Segmentacao apoiada por novos campos (categoria, dias de recencia, ticket medio) para refinar listas VIP/Inativos/Todos

### M3 - Automacoes (P1)
- Carrinho abandonado (1h/24h com cupom)
- Reengajamento inativos (30/60/90 dias)
- Recompra automatica (medicamentos uso continuo)

-### M4 – Engajamento (P2)
- Volta ao estoque (subscribe + trigger)
  * Criar tabela `product_stock_subscription` para armazenar e-mail/nome/estado das inscrições e garantir único-produto+destino.
  * Job cron `ProductBackInStockAutomationService` lê inscrições `SUBSCRIBED`, verifica `produto.estoque > 0` e enfileira `mail/back-in-stock`, marcando `NOTIFIED` após o envio. Métricas `back_in_stock.automation.*` devem alimentar dashboards/alertas. Expor `POST /api/public/produtos/{produtoId}/stock/subscribe` (`email`, `nome`) para receber inscrições no frontend/mobile.
- Conteudo educativo (agenda)
- Cashback/beneficios (alertas e validade)

## Tickets detalhados (inicio)

### T1 - Criar entidades e migrations do email marketing (P0, M1)
- Criar tabelas de campanha, fila e log
- Definir indices por status e created_at
- Adicionar status enum (PENDING/SENDING/SENT/FAILED)

### T2 - Serviço de fila de envio (P0, M1)
- Buscar lote de emails pendentes
- Enviar com throttling
- Persistir resultado em log
- Marcar falhas com retry

### T3 - Templates base + transacional (P0, M1)
- Base HTML com header/footer
- Template de pedido (confirmacao e status)
- Template de promocao simples

### T4 - UI de monitoramento de envios (P0, M1)
- Lista de envios recentes
- Detalhe por campanha
- Filtros por status
