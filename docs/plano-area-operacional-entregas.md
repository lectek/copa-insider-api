# Plano da Area Operacional de Entregas

## Objetivo

Estruturar uma area operacional de entregas que aproveite o backend de roteirizacao ja existente e transforme isso em um fluxo real de operacao para:

- operador interno que monta e despacha a rota
- entregador que executa a sequencia de paradas
- administracao que acompanha progresso, falhas e historico

O objetivo nao e criar um novo modulo isolado do zero. O objetivo e productizar o que ja existe em `AdminEntregasRestController` e `DeliveryRouteService`.

## Estado atual confirmado

Hoje o sistema ja possui:

- endpoint `POST /api/admin/entregas/roteirizar`
- endpoint `POST /api/admin/entregas/{pedidoId}/confirmar`
- endpoint `POST /api/admin/entregas/{pedidoId}/codigo/regenerar`
- calculo de rota via `DeliveryRouteService`
- ETA publico via `PublicDeliveryEstimateService`
- configuracoes operacionais de entrega em `AdminConfiguracoesGeralController`
- codigo de entrega salvo no `PedidoEntity`

Hoje o sistema ainda nao possui:

- tela dedicada `/admin/entregas`
- tela mobile para o entregador
- persistencia de uma rota montada
- associacao de rota a um entregador
- status operacional de paradas
- historico de tentativa de entrega

## Decisao de arquitetura

### 1. Nao sobrecarregar `Pedido.status`

O enum atual `StatusPedido` cobre o fluxo comercial:

- `ABERTO`
- `AGUARDANDO_PAGAMENTO`
- `PAGO`
- `ENVIADO`
- `ENTREGUE`
- `CANCELADO`

Ele nao e suficiente para a operacao de rua. O caminho mais limpo e manter `Pedido.status` como status de negocio e criar status proprios para a logistica.

### 2. Criar entidades proprias de rota

Recomendacao:

- `EntregaRotaEntity`
- `EntregaParadaEntity`
- opcional depois: `EntregaOcorrenciaEntity`

Isso evita gambiarra em `PedidoEntity` e permite:

- salvar a ordem planejada
- guardar quem foi o entregador
- marcar progresso de cada parada
- reprocessar falhas sem perder historico

## Modelo de dados recomendado

### `entrega_rota`

Campos sugeridos:

- `id`
- `data_operacao`
- `origem`
- `distancia_total_km`
- `mapa_url`
- `status`
- `entregador_usuario_id` nullable no MVP
- `criada_por_usuario_id`
- `despachada_em`
- `iniciada_em`
- `finalizada_em`
- `created_at`
- `updated_at`

Status sugeridos:

- `RASCUNHO`
- `PLANEJADA`
- `DESPACHADA`
- `EM_EXECUCAO`
- `CONCLUIDA`
- `CANCELADA`

### `entrega_parada`

Campos sugeridos:

- `id`
- `rota_id`
- `pedido_id`
- `ordem`
- `cliente_nome_snapshot`
- `endereco_snapshot`
- `codigo_entrega_snapshot`
- `status`
- `eta_previsto`
- `confirmado_em`
- `motivo_falha`
- `observacao`
- `maps_url` opcional depois
- `created_at`
- `updated_at`

Status sugeridos:

- `PENDENTE`
- `A_CAMINHO`
- `CHEGOU`
- `ENTREGUE`
- `TENTATIVA_SEM_SUCESSO`
- `REAGENDAR`
- `CANCELADA`

### Regra de integridade

- `Pedido.status` continua sendo atualizado para `ENTREGUE` apenas na confirmacao real.
- `Pedido.status` pode ir para `ENVIADO` quando a rota for despachada.
- a operacao detalhada fica em `entrega_parada.status`.

## Telas recomendadas

### 1. `/admin/entregas`

Tela principal do operador logistico.

Blocos:

- cards de resumo: `prontos para rota`, `em execucao`, `entregues hoje`, `falhas`
- tabela de pedidos elegiveis para entrega
- filtros por data, bairro, status, forma de pagamento, modo de entrega
- selecao multipla
- botao `Roteirizar`
- painel lateral com resultado da rota

Dados por linha:

- numero do pedido
- cliente
- endereco
- bairro
- total
- status comercial
- codigo de entrega
- checkbox para incluir na rota

### 2. `/admin/entregas/rotas/{rotaId}`

Tela de detalhe da rota.

Blocos:

- cabecalho com distancia, quantidade de paradas, entregador, status
- timeline das paradas em ordem
- acao `Abrir mapa`
- acoes por parada

Acoes por parada:

- `Marcar em deslocamento`
- `Confirmar entrega`
- `Registrar falha`
- `Regenerar codigo`
- `Ver pedido`

### 3. `/admin/entregas/execucao/{rotaId}`

Tela mobile-first para uso em rua.

Blocos:

- card da proxima parada
- endereco com CTA `Abrir no Maps`
- telefone do cliente
- codigo de entrega em destaque
- campo para validar codigo
- botao `Entregue`
- botao `Nao entregue`
- lista das proximas paradas

Observacao:

No MVP essa tela pode usar autenticacao admin/usuario interno. Em uma segunda etapa ela vira uma area dedicada ao entregador.

## Endpoints adicionais necessarios

O backend atual resolve roteirizacao e confirmacao. Para a area completa, faltam endpoints de consulta e persistencia.

### Consultas operacionais

- `GET /api/admin/entregas/pedidos-elegiveis`
- `GET /api/admin/entregas/rotas`
- `GET /api/admin/entregas/rotas/{rotaId}`
- `GET /api/admin/entregas/rotas/{rotaId}/execucao`

### Persistencia da rota

- `POST /api/admin/entregas/rotas`
  - recebe lista de `pedidoIds`
  - chama o `DeliveryRouteService`
  - salva `entrega_rota` e `entrega_parada`
- `POST /api/admin/entregas/rotas/{rotaId}/despachar`
- `POST /api/admin/entregas/rotas/{rotaId}/iniciar`
- `POST /api/admin/entregas/rotas/{rotaId}/cancelar`

### Acoes nas paradas

- `POST /api/admin/entregas/rotas/{rotaId}/paradas/{paradaId}/chegada`
- `POST /api/admin/entregas/rotas/{rotaId}/paradas/{paradaId}/confirmar`
- `POST /api/admin/entregas/rotas/{rotaId}/paradas/{paradaId}/falha`
- `POST /api/admin/entregas/rotas/{rotaId}/paradas/{paradaId}/reagendar`

### Aproveitamento do backend atual

Os endpoints existentes podem ser reutilizados internamente:

- `AdminEntregasRestController.roteirizar` continua sendo o nucleo do calculo
- `confirmarEntrega` vira parte do fluxo de parada
- `regenerarCodigo` continua util na operacao

## Regras de negocio recomendadas

Pedido elegivel para rota:

- `modoEntrega = ENTREGA`
- `status` em `PAGO` ou `ENVIADO`
- endereco valido
- nao estar em outra rota ativa

Regras adicionais:

- manter limite de ate 12 pedidos por execucao no MVP
- ao despachar rota, atualizar pedidos para `ENVIADO` quando aplicavel
- ao confirmar parada, atualizar pedido para `ENTREGUE`
- pedido `CANCELADO` deve ser bloqueado da execucao

## Backlog por fase

### Fase 1 - Cockpit do operador

Objetivo:

Colocar em producao uma area `/admin/entregas` funcional com roteirizacao persistida.

Entrega:

- item no menu admin para `Entregas`
- controller web para renderizar paginas Thymeleaf
- API de pedidos elegiveis
- API de criar rota persistida
- tela com selecao de pedidos e geracao da rota
- tela detalhe da rota
- botao para abrir `mapaUrl`

Valor:

- backend existente passa a gerar operacao real
- equipe interna passa a montar rota sem gambiarra manual

### Fase 2 - Execucao da rota

Objetivo:

Dar ao entregador uma tela simples e usavel no celular.

Entrega:

- tela mobile-first de execucao
- destaque da proxima parada
- confirmacao por codigo
- registro de falha de entrega
- avancar para a proxima parada

Valor:

- reduz contato manual e erro operacional
- fecha o ciclo da rota no proprio sistema

### Fase 3 - Historico e controle

Objetivo:

Dar visibilidade gerencial e capacidade de analise.

Entrega:

- relatorio de rotas por dia
- taxa de sucesso por entregador
- motivos de falha mais comuns
- historico de ocorrencias por pedido

Valor:

- melhora operacional continua
- base para SLA e escalabilidade

## Ordem tecnica de implementacao

### Sprint 1

- criar migrations Flyway para `entrega_rota` e `entrega_parada`
- criar entities, repositories e enums de status
- criar caso de uso para salvar a rota planejada
- criar endpoint `GET /pedidos-elegiveis`
- criar endpoint `POST /rotas`

### Sprint 2

- criar pagina `/admin/entregas`
- criar JS da tela para selecao, roteirizacao e exibicao do resultado
- criar pagina de detalhe da rota
- adicionar menu `Entregas` no header e sidebar

### Sprint 3

- criar pagina mobile de execucao
- criar endpoints de progresso de parada
- integrar confirmacao por codigo na parada
- tratar falha e reagendamento

### Sprint 4

- relatorios
- filtros avancados
- associacao a entregador
- hardening de permissao

## Riscos e atencoes

### 1. Persistencia e o principal bloqueador

Sem salvar a rota, a operacao vira apenas uma simulacao. Esse e o primeiro gap que precisa ser resolvido.

### 2. Status do pedido nao deve virar status logistico

Misturar tudo em `Pedido.status` vai gerar regressao em telas, fiscal, checkout e relatorios.

### 3. Endereco ruim quebra o ganho da roteirizacao

Vale normalizar endereco e, depois, considerar cache de geocodificacao.

### 4. Motoboy precisa de UX simples

A area do entregador nao pode parecer uma tela de admin reduzida. Precisa ser mobile-first e orientada a acao rapida.

## Proximos passos imediatos

1. Criar `docs` e backlog oficial desta frente.
2. Implementar persistencia de rota e parada.
3. Criar a pagina `/admin/entregas`.
4. So depois abrir a frente da tela do entregador.

## Recomendacao final

O melhor caminho nao e comecar pelo mapa nem por rastreamento em tempo real.

O melhor caminho e:

1. salvar rota
2. operar rota no admin
3. executar rota no mobile
4. medir historico e falhas

Isso reaproveita o backend existente, reduz risco de retrabalho e coloca a operacao de entrega em uso real mais rapido.
