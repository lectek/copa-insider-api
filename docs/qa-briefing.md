# QA Briefing: validação dos milestones de marketing e API

## Objetivo
Garantir que QA compreenda o escopo dos milestones M1–M4 (fundação de e-mail, campanhas, automações e engajamento) e valide dependências de infraestrutura, testes automatizados, métricas/alertas e cobertura dos contratos descritos em `README.md` e `docs/backlog.md`.

## Preparação
- Configure os perfis `dev`, `docker`, `test` e `legacy/firebird` com as variáveis necessárias: `APP_MAIL_*`, `APP_WEB_BASE_URL`, `EMAIL_DELIVERY_QUEUE`, `DEV_DATASOURCE_*`, `FIREBIRD_*`, configuração de storage (S3 vs local) e-mail (`Mailpit` em dev) e filas `email_delivery`/`email_campaign_queue`.
- Execute `docker compose -f docker-compose.dev.yml up -d mysql mailpit cliente-mock` sempre que precisar de infra real, e use `./mvnw -pl boot-app test -Dspring.profiles.active=test` para cenários com Testcontainers.
- Confirme que a fila `email_delivery` pode ser monitorada via `/actuator/metrics` (`email_campaign.worker.*`), `/actuator/health`, e que há dashboards/filtros para campanhas no admin (lista/preview/cancelamento).

## Milestones e checkpoints

### M1 – Fundação email
- Valide tabela/entidade `email_campaign`, `email_recipient`, `email_queue`, `email_delivery_log`.
- Verifique estados da fila (`PENDING/SENDING/SENT/FAILED`), retries com backoff e log por campanha do `EmailDeliveryWorker`.
- Confirme que a automação de recompra (`mail/recompra`) registra métricas `recompra.automation.*`, enfileira entradas únicas em `email_campaign_queue` e mostra payloads corretos no painel `/admin/marketing/emails/campanhas/fila`.
- Confirme templates base e transacionais (promoção, pedido, entrega) com pré-visualizações HTML e envios via worker; execute testes com `./mvnw -pl boot-app clean verify` ou `./mvnw -pl boot-app test -Dspring.profiles.active=test`.
- Monitore métricas: `email_campaign.worker.processed/sent/retry/failed` e alerte quando `failed > 0`.
- Documente dependências: `APP_MAIL_*`, `APP_WEB_BASE_URL`, `EMAIL_DELIVERY_QUEUE`, `DEV_DATASOURCE_*`, `email_delivery`.

### M2 – Campanhas
- Exercite CRUD/preview/agendamento/cancelamento da campanha (nome, assunto, template, público, gatilhos).
- Verifique segmentações: categoria, recência (dias), ticket médio, VIP/Inativos/Todos.
- Confirme integração com filas e métricas antes do envio ao público final; use dados fake dos diretórios `mocks/` e `infra/` para simular casos.
- Garanta observabilidade: dashboards em admin, métricas `email_campaign.worker.*`, logs (Mailpit) e alertas (Actuator).

### M3 – Automação
- Teste gatilhos automáticos (carrinho abandonado 1h/24h, reengajamento 30/60/90 dias, recompra contínua).
- Assegure que cada automação enfileire mensagens para `email_campaign_queue`, gere eventos `Micrometer` e log estruturado, e respeite as regras de throttling e retries.
- Caso o tempo seja crítico, controle relógio do teste (`@Testcontainers`/simulação de cron) para checar intervalos e disparos esperados.
- Valide dependências: Firebird (leitura via `ProdutoLegacyService`), MySQL e fila em testes end-to-end antes de ativar automações no ambiente real.

### M4 – Engajamento
- Simule volta ao estoque (subscribe + trigger), conteúdo educativo (agenda publicada) e cashback/benefícios (saldo + validade).
- Verifique o job `ProductBackInStockAutomationService`: a tabela `product_stock_subscription` deve registrar inscrições, `back_in_stock.automation.processed/skipped/enqueued` precisa subir em `/actuator/metrics` e o painel `/admin/marketing/emails/campanhas/fila` deve mostrar o template `mail/back-in-stock` rimando com payloads corretos. Use `POST /api/public/produtos/{produtoId}/stock/subscribe` para criar inscrições e confirme que retornos 201/Location sem reenvio duplicado.
- Confirme compatibilidade com storage (S3/Local) usando `S3StorageAdapter`/`LocalStorageAdapter` e jobs existentes (`ProdutoSyncService`).
- Verifique alertas de estoque configurados via `app.estoque.alerta.*` e disponibilidade no admin (dashboards, logs).

## Testes e QA operacional
- Dê preferência a `./mvnw -pl boot-app clean verify` ou `./mvnw -pl boot-app test -Dspring.profiles.active=test` conforme o milestone avança; execute no perfil apropriado.
- Observe logs de OTP/email claim/reset (`/api/auth/otp/start`, `/api/auth/resetar-senha`, etc.) e execute fluxos descritos em `README.md` para garantir cobertura de autenticação.
- Monitore Actuator (`/actuator/health`, `/actuator/metrics`, `/actuator/loggers`) e métricas Micrometer para filas.

## Comunicação de resultados
- Registre falhas, riscos e tempos de fila em cada milestone (ex: retries no worker, campanhas pausadas, alertas `email_campaign.worker.failed > 0`).
- Atualize `docs/backlog.md` se identificar novos gaps, dependências ou ajustes de prioridade.
- Após validação, confirme com produto/QA se o milestone pode progredir para o próximo passo (M2→M3, etc.).
