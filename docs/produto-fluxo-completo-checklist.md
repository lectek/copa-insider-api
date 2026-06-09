# Fluxo Completo de Produto - Analise e Checklist

## 1) Escopo do fluxo analisado
- Entrada e sincronizacao de catalogo (legado + admin)
- Validacao/publicacao/despublicacao
- Exibicao publica (API + paginas)
- Detalhe de produto
- Carrinho, checkout e pedido
- Assinatura de volta ao estoque
- Operacao, testes e observabilidade

## 2) Estado atual (resumo objetivo)
- O filtro publico esta aplicado em vitrine/listagem/detalhe (status PUBLICADO + janela de publicacao + disponibilidade de venda).
- `/catalogo` foi alinhado para redirecionar para `/produtos`.
- Fluxo de notificacao de estoque foi corrigido para reativar inscricao existente por e-mail.
- Destaques por `home.featured.ids` agora respeitam filtro publico.
- Validacao automatizada esta verde (`verify` com 144 testes passando).

## 3) O que precisamos (priorizado)

## P0 - Fechar regras de negocio no ponto de conversao
- [x] Aplicar a mesma regra de "produto vendavel/publico" no carrinho e checkout (hoje a validacao usa `findAllByIdIn` + checks basicos de estoque/preco/disponivel).
  - Referencias: `CartService`, `CartValidationService`, `ClienteSelfApiController`.
  - Entrega esperada: impedir adicionar/finalizar pedido com item nao publicado ou fora da janela de publicacao.

- [x] Definir regra formal para "produto assinavel" em volta ao estoque.
  - Hoje `subscribe` usa `findById` (aceita qualquer produto).
  - Precisamos decidir: permitir apenas produto publicado (mesmo sem estoque) vs permitir qualquer item interno.
  - Entrega esperada: query dedicada de elegibilidade para assinatura + teste.

- [x] Padronizar caminho oficial para APIs de produto.
  - Existem multiplos pontos: `/api/public/produtos`, `/api/public/vitrine`, `/api/public/carrossel`, `/api/v2/produtos`, `/api/app/produtos`.
  - Entrega esperada: declarar endpoint canonico por caso de uso e marcar/deprecar os legados para evitar divergencia.

### Endpoint canonico definido
- Publico (leitura/listagem/detalhe/destaques): `/api/public/produtos/**`
- Publico (assinatura de estoque): `/api/public/produtos/{produtoId}/stock/subscribe`
- Admin (gestao de produto): `/api/admin/produtos/**`
- Legados marcados como deprecated:
  - `/api/public/carrossel/**` (use `/api/public/produtos/destaques`)
  - `/api/public/vitrine/**` (use `/api/public/produtos/destaques`)
  - `/api/v2/produtos/**` (admin legado)
  - `/api/app/produtos/**` (admin legado)

## P1 - Cobertura de testes do fluxo fim-a-fim
- [ ] Criar testes para `ClienteSelfApiController` (carrinho/favoritos/checkout) com casos de produto nao publicavel.
- [ ] Criar teste para `ProdutoDetalheController` com 404 para item fora das regras publicas.
- [ ] Criar teste para `PublicProdutoController` e `PublicCarrosselController` (consistencia com `ProdutoQueryServiceImpl`).
- [ ] Adicionar teste de regressao para regra de assinatura de estoque definida no P0.

## P1 - Operacao e governanca de dados
- [ ] Revisar configuracoes de home:
  - `home.featured.ids`
  - `HOME.main_product_id`
  - `LAYOUT.layout.exibir_indisponiveis`
  - Entrega esperada: IDs invalidos/removidos nao quebram vitrine nem exibem item indevido.

- [ ] Formalizar checklist de publicacao no admin:
  - imagem, preco > 0, estoque, categoria, status VALIDADO -> PUBLICADO, janela de publicacao.
  - Entrega esperada: runbook simples para operacao.

## P2 - Qualidade tecnica e manutencao
- [ ] Reduzir sobreposicao entre `ProdutoRepository` e `ProdutoJpaRepository` para diminuir risco de regra duplicada.
- [ ] Corrigir lock recorrente de arquivos no Windows para voltar a usar `clean verify` sem workaround.
- [ ] Revisar textos/encoding de mensagens/docs para manter padrao legivel.

## 4) Matriz do fluxo (visao por etapa)
| Etapa | Situacao atual | Risco | Acao necessaria |
|---|---|---|---|
| Ingestao/sync | Funciona e ja evita dupla pipeline principal | Medio | Monitorar staleness e falha de sync |
| Curadoria admin | Fluxo de validacao/publicacao existe | Medio | Checklist operacional + testes de transicao de status |
| Listagem publica | Filtragem publica aplicada | Baixo | Manter endpoint canonico e deprecacoes claras |
| Detalhe publico | Busca por `findPublicById` | Baixo | Teste dedicado de 404/regra publica |
| Carrinho/checkout | Valida disponivel/estoque/preco | Alto | Aplicar regra completa de produto publicavel |
| Favoritos | Busca por `findById` | Medio | Definir regra: favorito so para publico ou nao |
| Volta ao estoque | Reativacao corrigida | Medio | Definir elegibilidade de produto assinavel |

## 5) Ordem sugerida de execucao (curta)
1. Fechar regra P0 de carrinho/checkout e assinatura de estoque.
2. Cobrir com testes de controller/servico (P1).
3. Consolidar endpoint canonico e deprecacoes.
4. Publicar runbook operacional e checklist de dados da home.
5. Tratar manutencao tecnica (repos duplicados + lock clean verify).
