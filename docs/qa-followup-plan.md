# QA Follow-up & Scheduling

## Próximos checkpoints

- **Imediato (hoje)**: enviar os artefatos (`docs/qa-briefing.md`, `docs/backlog.md`, `docs/qa-coordination.md`) ao QA/produto e pedir confirmação de recebimento + alinhamento de dependências (fila `email_delivery`, SMTP, Firebird/storage).
- **Semanal (ou ao final de cada milestone)**: criar um breve relatório com o status dos testes (`./mvnw clean verify` + runs específicos), alertas observados e o checkpoint de métricas/actuator; registrar no canal escolhido (issue, Slack ou board).
- **Antes de avançar para o próximo milestone (M2→M3, etc.)**: exigir aprovação explícita do QA/produto, confirmando que os critérios de `docs/backlog.md` foram testados e o canal de observabilidade (`email_campaign.worker.*`, `app.estoque.alerta.*`) está dentro dos limites.

## Comunicação sugerida

- Criar uma issue ou card com título “QA validation – [milestone]” contendo:
  1. Lista de artefatos (briefing, backlog, coordenação).
  2. Resultados/alertas do último `./mvnw clean verify`.
  3. Status de dependências (filas, SMTP, Firebird, storage).
  4. Ação esperada (ex: “QA confirma que fila `email_delivery` e `email_campaign_queue` foram validadas antes de liberar campanhas”).
- Atualizar o mesmo card com comentários semanais sobre novos testes.
- Se algo falhar, registrar como “QA risk” com milestoning.

## Monitoramento contínuo

- Reexibir a checklist aos membros de QA ao final de cada sprint.
- Quando QA liberar uma milestone, agendar o próximo conjunto de testes (por exemplo, `./mvnw clean verify` + testes focados em campanhas/automações).
