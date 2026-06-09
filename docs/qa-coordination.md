# Coordenação QA / Produto

## Objetivo
Formalizar o alinhamento entre Docs/backlog, QA briefing e o time de produto para validar os milestones M1‑M4 antes de liberar campanhas ou automações.

## Tarefas
1. Compartilhar `docs/qa-briefing.md` e o bloco atualizado de `docs/backlog.md` com QA/produto; reforçar que eles cobrem preparação de perfis, métricas (`email_campaign.worker.*`, `app.estoque.alerta.*`) e dependências (fila `email_delivery`, SMTP, Firebird, storage).
2. Registrar os alertas observados no `./mvnw clean verify`:
   - `EmailDeliveryWorker` avisa que a tabela `email_delivery` está ausente enquanto espera pelas migrations.
   - `EmailCampaignQueueWorker` loga falhas esperadas (`SMTP indisponível`) ao testar envios.
   - Há avisos repetidos sobre múltiplos `junit-platform.properties` e um alerta sobre `HttpEntityMethodProcessor`/Paginação.
3. Solicitar confirmação de QA/produto para cada milestone (M1 → M2 → M3 → M4) antes de avançar; manter um checklist semanal com status (Pendente/Validado/Bloqueado).

## Comunicação
- Use o canal principal (Slack/Teams/issue tracker) para compartilhar os artefatos e registrar feedbacks.
- Atualize `docs/backlog.md` e `README.md` assim que QA sinalizar ajustes ou dependências novas.
- Se QA encontrar regressões, documente no checklist sob o título “QA risks” indicando o milestone afetado e reinicie a validação após correções.
