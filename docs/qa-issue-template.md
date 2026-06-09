# QA Validation Issue (para milestone M1)

## Context
- Artefatos enviados: `docs/qa-briefing.md`, `docs/backlog.md`, `docs/qa-coordination.md`, `docs/qa-followup-plan.md`.
- Objetivo: validar M1 (fila + templates + envio transacional) antes de liberar campanhas e automações.
- Build mais recente: `./mvnw clean verify` (passou). Logs reportam SMS/queue warnings esperados (`EmailDeliveryWorker` table missing, `EmailCampaignQueueWorker` SMTP warnings, `junit-platform.properties` duplicados).

## Checklist
- [ ] Perfis `dev/docker/test/legacy` declarados no README possuem `APP_MAIL_*`, `EMAIL_DELIVERY_QUEUE`, `DEV_DATASOURCE_*`, `FIREBIRD_*`, storage (S3/local) e filas `email_delivery`/`email_campaign_queue` configurados.
- [ ] `EmailCampaignQueue` reporta status `PENDING/SENDING/SENT/FAILED`; `EmailCampaignLog` armazena envios/cancelamentos.
- [ ] Dashboard `/admin/marketing/emails/campanhas/fila` mostra cards de status, os 20 itens mais recentes da fila e os 20 logs últimos.
- [ ] Métricas `email_campaign.worker.*` e Actuator `/metrics`/`/health` já acionadas para `EmailDeliveryWorker` e `EmailCampaignQueueWorker`.
- [ ] Dependências confirmadas: migrations de tabela `email_delivery`, SMTP ativo (Mailpit/disparo real), Firebird/Storage no `legacy` se necessário.
- [ ] QA anota riscos (ex: tabela ausente, retry count, alertas `email_campaign.worker.failed > 0`) e registra no issue como “QA risk” antes de liberar M2.

## Resultado esperado
- Comentário final do QA confirmando que o milestone M1 está validado e pronto para M2.
- Se houver bloqueios, detalhar qual parte do checklist falhou e ação requerida.
