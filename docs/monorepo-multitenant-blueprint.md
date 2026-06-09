# Blueprint Monorepo + Multitenant

## Objetivo
Organizar o repositorio em monorepo com deploy independente por servico, mantendo:
- `SaudeMaisFarma` isolada (single-tenant)
- `Lekteck` + `Embalando` no mesmo core multitenant

## Estrutura alvo
- `apps/lekteck-api`
- `apps/saudemaisfarma-api`
- `libs/shared-kernel`

## Estado atual
- Agregador Maven adicionado: `pom.monorepo.xml`
- Modulo de transicao Saude: `apps/saudemaisfarma-api`
- Modulo executavel Lekteck: `apps/lekteck-api`
- Tenant resolver implementado (host -> header -> default)
- Exemplo de persistencia isolada por tenant implementado no modulo Lekteck
- Guardrail de repositorio tenant-aware implementado com teste automatizado
- CI por caminho: `.github/workflows/monorepo-ci.yml`
- Build executavel da Saude ainda permanece no `pom.xml` da raiz

## Contrato multitenant
1. Identificacao de tenant:
- Primario: host do request (`tenant-a.seudominio.com`)
- Secundario: `X-Tenant-Id` (ambientes internos/teste)

2. Contexto por request:
- Resolver tenant no inicio da request
- Propagar `tenantId` para camada de aplicacao e repositorio
- Limpar contexto ao final da request

3. Persistencia:
- Tabelas multitenant com coluna `tenant_id`
- Indices compostos iniciando por `tenant_id`
- Unicidade sempre escopada por tenant

4. Seguranca:
- Nunca confiar em `tenantId` vindo do payload
- Tenant efetivo deve vir do resolvedor de contexto
- Logs com `tenantId` + correlation id

5. Deploy Railway:
- Um servico por app/tenant de borda
- `rootDirectory` dedicado por servico
- Variaveis isoladas por servico

## Fases recomendadas
1. Fase 1 (agora): bootstrap de estrutura e CI seletivo.
2. Fase 2: criar app Lekteck Spring Boot e tenancy resolver por host/header.
3. Fase 3: mover codigo da Saude da raiz para `apps/saudemaisfarma-api/src`.
4. Fase 4: extrair componentes neutros para `libs/shared-kernel`.
5. Fase 5: hardening (tests tenancy, guardrails de dependencia, observabilidade por tenant).

## Regras de acoplamento
- `apps/saudemaisfarma-api` nao depende de classes internas de `apps/lekteck-api`.
- Compartilhamento somente por `libs/shared-kernel`.
- Regras de farmacia/controlados ficam apenas na Saude.

## Comandos
- Build Saude no monorepo:
  - `./mvnw -DskipITs clean verify`
- Testar modulo Lekteck:
  - `./mvnw -f pom.monorepo.xml -pl apps/lekteck-api -am test`
