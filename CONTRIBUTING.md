# Contributing

## Fluxo de branch
- `main`: branch estavel.
- `feat/<tema-curto>`: novas funcionalidades.
- `fix/<tema-curto>`: correcao de bug.
- `chore/<tema-curto>`: tarefas tecnicas/infra/docs.

## Convencao de commits
- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `refactor: ...`

## Regras de PR
1. PR pequeno e focado.
2. Usar o template de PR em `.github/pull_request_template.md`.
3. Nao misturar refactor grande com feature.
4. Em mudanca de contrato, atualizar docs/README/OpenAPI.
5. Em mudanca estrutural (monorepo/migracao), abrir PR dedicado.

## DoR (Definition of Ready)
1. Objetivo claro.
2. Regra de negocio definida.
3. Escopo e fora de escopo definidos.
4. Criticos de risco mapeados.
5. Criterios de aceite testaveis.

## DoD (Definition of Done)
1. Build e verificacoes no CI aprovados.
2. Testes relevantes adicionados/atualizados.
3. Documentacao atualizada.
4. Checklist de risco/rollback preenchido no PR.

## Qualidade local (antes de abrir PR)
```bash
./mvnw -B -DskipITs clean verify
```

## Regra de estabilidade
1. Nao fazer deploy sem CI verde.
2. Nao aprovar PR sem plano de rollback em mudanca P0/P1.

## Seguranca
1. Nao commitar segredo/token/senha.
2. Validar autorizacao em endpoint novo.
3. Evitar logs com dados sensiveis.
