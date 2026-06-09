# Template de Board - GetShitDone

## Colunas recomendadas
1. Backlog
2. Ready (DoR OK)
3. Em progresso
4. Em revisao
5. Em QA
6. Pronto para deploy
7. Concluido

## Epicos iniciais
1. Arquitetura e Monorepo
2. Multiempresa (Tenant)
3. Produto e Variacoes
4. Checkout e Pagamentos
5. Entregas e Operacao
6. Marketing e Automacoes
7. Seguranca
8. Observabilidade e Estabilidade

## Campos obrigatorios do card
- Titulo
- Prioridade (`P0`, `P1`, `P2`)
- Tipo (`backend`, `frontend`, `infra`, `qa`, `docs`)
- Dependencias
- Dono
- Estimativa (1-3 dias recomendado)

## Template de card
```text
Titulo: [P1][BACKEND] Nome da tarefa

Objetivo:
- Resultado concreto esperado.

Escopo:
1. ...
2. ...

Fora de escopo:
1. ...

Dependencias:
1. ...

Criterios de aceite:
1. ...
2. ...
3. ...

Testes:
1. unitario
2. integracao
3. regressao (se aplicavel)

Riscos:
1. ...

Rollback:
1. ...
```

## Regra de priorizacao
- `P0`: bloqueia receita, seguranca, deploy, dados ou fluxo critico.
- `P1`: alto impacto de produto/operacao.
- `P2`: evolutivo, ganho incremental.

## Definicao de WIP
- Maximo 2 cards "Em progresso" por pessoa.
- Nao iniciar item novo sem liberar bloqueio do item atual P0/P1.
