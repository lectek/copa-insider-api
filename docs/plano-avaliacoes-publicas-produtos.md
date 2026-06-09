# Plano de Avaliacoes Publicas de Produtos

## Objetivo

Criar um sistema de avaliacao de produtos visivel a todos, com:

- nota de 1 a 5 estrelas
- comentarios publicos
- media consolidada por produto
- selo de compra verificada
- moderacao administrativa

O objetivo nao e apenas exibir estrelas. O objetivo e criar um fluxo confiavel, controlado e util para conversao no catalogo e na pagina de detalhe do produto.

## Encaixe no sistema atual

Hoje o projeto ja tem pontos naturais para esse subsistema:

- detalhe do produto em `ProdutoDetalheController`
- listagem publica em `ProdutosController`
- envio autenticado do cliente em `ClienteSelfApiController`
- elegibilidade de compra baseada em pedidos e itens do pedido
- card de produto encapsulado em `ProductCardVM`

Isso significa que o sistema de avaliacao pode entrar sem criar uma arquitetura paralela.

## Estado atual confirmado

Hoje o sistema possui:

- autenticacao de cliente
- historico de pedidos do cliente
- detalhe de pedido e itens comprados
- detalhe publico do produto
- listagem publica e cards de produto

Hoje o sistema nao possui:

- tabela de avaliacoes de produto
- nota media publica
- comentarios publicos
- moderacao de avaliacoes
- regra de compra verificada

## Decisoes de produto

### 1. Comentario anonimo nao deve existir

Para farmacia, comentario anonimo vira risco alto de spam, fraude e ataque reputacional.

Recomendacao:

- somente cliente autenticado pode avaliar
- exibicao publica com nome mascarado

Exemplo:

- `Julia A.`
- `Carlos M.`

### 2. So cliente que comprou pode avaliar

A regra mais defensavel e:

- cliente precisa ter comprado o produto
- o pedido precisa estar com status `ENTREGUE`

Isso permite um selo de `Compra verificada`.

### 3. Avaliacao nao deve publicar direto no MVP

Como o comentario sera visivel a todos, o ideal e:

- cliente envia
- avaliacao entra como `PENDENTE`
- admin aprova, rejeita ou oculta

Depois, se a operacao estiver estavel, pode-se avaliar autoaprovacao para cliente com compra verificada.

### 4. Uma avaliacao ativa por cliente e produto

Recomendacao:

- manter uma avaliacao ativa por `usuario + produto`
- se o cliente quiser atualizar a opiniao, ele edita a avaliacao existente

Isso evita inflar a nota artificialmente e simplifica moderacao.

## Arquitetura recomendada

### Tabela principal: `produto_avaliacao`

Campos sugeridos:

- `id`
- `produto_id`
- `usuario_id`
- `pedido_id`
- `nota`
- `titulo` opcional
- `comentario`
- `status`
- `compra_verificada`
- `relevancia` opcional para futuro
- `created_at`
- `updated_at`
- `publicada_em`
- `moderada_em`
- `moderada_por_usuario_id`
- `motivo_moderacao`
- `version`

Status sugeridos:

- `PENDENTE`
- `APROVADA`
- `REJEITADA`
- `OCULTA`

### Tabela de resumo: `produto_avaliacao_resumo`

Recomendacao:

Guardar agregados fora de `produto` para nao misturar sync de catalogo com feedback publico.

Campos sugeridos:

- `produto_id`
- `media_geral`
- `total_avaliacoes`
- `total_comentarios`
- `total_1_estrela`
- `total_2_estrelas`
- `total_3_estrelas`
- `total_4_estrelas`
- `total_5_estrelas`
- `updated_at`

Motivo:

- leitura rapida na listagem
- evita recalcular media a cada request
- desacopla feedback do fluxo de importacao do produto

## Regras de negocio

### Elegibilidade para avaliar

Cliente pode avaliar se:

- estiver autenticado
- existir pelo menos um `ItemPedido` do produto em pedido do cliente
- o pedido estiver `ENTREGUE`

### Publicacao

Avaliacao so entra no frontend publico se:

- `status = APROVADA`

### Edicao

Se uma avaliacao ja aprovada for editada:

- volta para `PENDENTE`
- sai temporariamente do agregado publico

Isso evita que o cliente troque o texto depois da moderacao sem nova revisao.

### Conteudo minimo

Recomendacao para MVP:

- `nota` obrigatoria
- `comentario` opcional mas incentivado

Recomendacao para fase 2:

- exigir comentario para notas 1 e 2

### Exibicao publica

Itens exibidos para todos:

- media do produto
- total de avaliacoes
- distribuicao por estrelas
- comentarios aprovados
- nome mascarado
- data da publicacao
- selo `Compra verificada` quando houver

## Experiencia no frontend

### 1. Pagina de detalhe do produto

Ponto principal de exibicao.

Blocos recomendados:

- resumo de estrelas logo abaixo do titulo ou acima da descricao
- media geral e total de avaliacoes
- barra de distribuicao por estrelas
- lista de comentarios aprovados
- formulario de avaliacao para cliente elegivel

Mensagem de elegibilidade:

- `Voce pode avaliar este produto apos receber um pedido com ele.`

Mensagem de compra verificada:

- `Compra verificada`

### 2. Listagem publica de produtos

Fase 2.

No card de produto:

- media em estrelas
- total de avaliacoes ao lado

Exemplo:

- `4,8 (37)`

### 3. Area do cliente

Fase 2.

Em pedidos entregues:

- CTA `Avaliar produto`
- link direto para o item no detalhe do pedido

## APIs recomendadas

### Publicas

- `GET /api/public/produtos/{id}/avaliacoes/resumo`
- `GET /api/public/produtos/{id}/avaliacoes`

Resposta esperada:

- media
- contagem total
- distribuicao por estrelas
- pagina de comentarios aprovados

### Cliente autenticado

Aproveitar o padrao atual de self-service:

- `GET /api/cliente/me/produtos/{produtoId}/avaliacao/elegibilidade`
- `GET /api/cliente/me/produtos/{produtoId}/avaliacao/minha`
- `POST /api/cliente/me/produtos/{produtoId}/avaliacao`
- `PUT /api/cliente/me/produtos/{produtoId}/avaliacao`

### Admin

- `GET /api/admin/avaliacoes`
- `GET /api/admin/avaliacoes/{id}`
- `POST /api/admin/avaliacoes/{id}/aprovar`
- `POST /api/admin/avaliacoes/{id}/rejeitar`
- `POST /api/admin/avaliacoes/{id}/ocultar`

## Backlog por fase

### Fase 1 - Nucleo do sistema

Objetivo:

Fazer o sistema existir de ponta a ponta no detalhe do produto.

Entrega:

- migrations Flyway
- `ProdutoAvaliacaoEntity`
- `ProdutoAvaliacaoResumoEntity`
- repositories
- service de elegibilidade e moderacao
- APIs publicas
- APIs do cliente autenticado
- bloco visual no detalhe do produto

Valor:

- feedback publico real no produto
- base para prova social e conversao

### Fase 2 - Escala de uso

Objetivo:

Levar avaliacao para mais pontos da jornada.

Entrega:

- estrelas nos cards da listagem
- CTA em pedidos entregues
- selo de compra verificada
- filtro por nota no detalhe do produto

Valor:

- melhora descoberta
- aumenta volume de avaliacao legitima

### Fase 3 - Controle e qualidade

Objetivo:

Melhorar governanca e relevancia.

Entrega:

- fila de moderacao no admin
- busca por palavra-chave
- denuncia de comentario
- ordenacao por mais recentes, maior nota e compra verificada

Valor:

- reduz abuso
- melhora qualidade do conteudo publico

## Ordem tecnica de implementacao

### Sprint 1

- criar migration de `produto_avaliacao`
- criar migration de `produto_avaliacao_resumo`
- criar entities e enums
- criar query de elegibilidade por item entregue

### Sprint 2

- criar service de avaliacao
- criar API cliente para criar/editar
- criar API publica para resumo e comentarios
- recalculo do resumo agregado

### Sprint 3

- integrar no `ProdutoDetalheController`
- renderizar bloco de estrelas e comentarios em `detalhe.html`
- renderizar formulario quando elegivel

### Sprint 4

- criar tela admin de moderacao
- levar estrelas para `ProductCardVM` e listagem
- expor CTA em pedidos entregues

## Regras tecnicas importantes

### 1. Resumo agregado deve ser persistido

Nao e bom calcular media de estrelas em tempo real para toda listagem publica.

Recomendacao:

- atualizar `produto_avaliacao_resumo` a cada aprovacao, ocultacao, rejeicao ou edicao relevante

### 2. Comentario publico nao deve depender de login

A parte publica precisa ser cacheavel e simples:

- comentarios aprovados
- media
- distribuicao

### 3. Nome exibido deve ser anonimizado

Nunca mostrar nome completo do cliente no frontend publico.

### 4. Moderacao deve preservar historico

Nao apagar comentario ao rejeitar ou ocultar. Apenas mudar status.

## Riscos e atencoes

### 1. Spam e abuso

Sem compra verificada e moderacao, a funcionalidade perde credibilidade rapidamente.

### 2. Produto com poucas avaliacoes

Nao usar linguagem enganosa. Se houver 1 avaliacao apenas, mostrar isso com clareza.

### 3. Impacto visual no detalhe

O bloco de avaliacao precisa entrar sem competir com compra, estoque e entrega. Ele deve reforcar conversao, nao poluir a pagina.

### 4. Sincronizacao com catalogo

Por isso a recomendacao de resumo separado. Feedback publico nao deve conflitar com rotinas de sync de produto.

## Proximos passos imediatos

1. Validar o modelo de moderacao: manual no MVP.
2. Implementar Fase 1 no detalhe do produto.
3. So depois levar estrelas para os cards da listagem.

## Recomendacao final

O melhor MVP nao e `comentario livre para qualquer visitante`.

O melhor MVP e:

1. cliente autenticado
2. compra entregue
3. nota de 1 a 5
4. comentario moderado
5. exibicao publica aprovada

Isso cria um sistema confiavel, util comercialmente e com risco operacional controlado.
