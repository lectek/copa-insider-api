# Monorepo - Estrutura Alvo (Fase inicial)

## Objetivo
Unificar o desenvolvimento em um repositorio unico, com padrao de build e governanca comum, sem migracao "big bang".

## Estado atual
- O repositorio ainda opera no formato atual (backend + artefatos web/mobile no mesmo workspace).
- A estrutura abaixo e **alvo**; a migracao sera faseada para nao interromper entregas.

## Estrutura alvo
```text
/
|-- backend/                 # Spring Boot API e templates atuais
|-- admin-web/               # Frontend/admin quando separado
|-- mobile/                  # App mobile
|-- shared-contracts/        # OpenAPI, DTOs, enums de contrato
|-- docs/
|-- .github/workflows/
```

## Estrategia de migracao incremental
1. **Fase 1 (agora):** padronizacao de CI, PR, governanca e backlog.
2. **Fase 2:** criar `shared-contracts/` com contrato de API versionado.
3. **Fase 3:** mover backend atual para `backend/` sem alterar funcionalidade.
4. **Fase 4:** separar `admin-web/` quando frontend deixar de depender de templates mistos.
5. **Fase 5:** alinhar versionamento e release por modulo.

## Regra de execucao
1. Nenhuma fase inicia sem checklist da fase anterior concluido.
2. Toda mudanca estrutural deve ter PR dedicado (sem misturar com feature de negocio).
3. Cada fase precisa de smoke test em ambiente de homologacao.

## Regras de compatibilidade
1. Nao quebrar rotas atuais durante migracao.
2. Toda mudanca estrutural deve manter build verde.
3. Mudanca de path deve vir com ajuste de docs e pipeline.

## CI minimo por modulo (alvo)
1. `backend`: build + testes + checks estaticos.
2. `admin-web`: lint + testes.
3. `mobile`: build + testes.
4. `shared-contracts`: validacao OpenAPI + breaking-change check.

## Criterio de aceite da fase atual
1. Workflow CI ativo para `main` e PR.
2. Template de PR e guia de contribuicao ativos.
3. Documento de estrutura alvo aprovado.
