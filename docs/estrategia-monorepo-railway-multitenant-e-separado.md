# Estrategia Monorepo + Railway (Lekteck/Embalando + Saude Separada)

## Decisao oficial
1. `Lekteck` e `Embalando` ficam no mesmo produto multitenant.
2. `SaudeMaisFarma` continua separada em regras de negocio (nao entra no multitenant), mas permanece sincronizada no mesmo repositorio Git.
3. Deploy continua automatico para os tres servicos a partir do mesmo repositorio.
4. Regras comerciais, onboarding e owner dashboard estao centralizados em `docs/lekteck-comercial-onboarding-owner-dashboard.md`.

---

## Arquitetura alvo
1. Repositorio unico (monorepo).
2. Tres servicos Railway independentes:
   - `lekteck-api` (multitenant)
   - `embalando-solucoes-api` (tenant dentro da Lekteck)
   - `redemaisfarma-api` (produto separado)
3. Cada servico com:
   - dominio proprio
   - variaveis de ambiente proprias
   - pipeline de deploy proprio

---

## Estrutura sugerida de pastas
```text
/
|-- apps/
|   |-- lekteck-api/          # core multitenant
|   |-- saudemaisfarma-api/   # isolado (vendido), sincronizado via mesmo repo
|-- libs/
|   |-- shared-kernel/        # utilitarios realmente compartilhados
|-- docs/
|-- .github/workflows/
```

Regra: `saudemaisfarma-api` nao pode depender de modulos internos de `lekteck-api`; no maximo usar `libs/shared-kernel`.

---

## Configuracao Railway (deploy automatico)
## 1) Servico `lekteck-api`
1. Conectar ao mesmo repositorio GitHub.
2. Definir `rootDirectory=apps/lekteck-api`.
3. Ativar auto-deploy na branch principal.
4. Variaveis:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `APP_MODE=multitenant`
   - `TENANT_RESOLUTION_MODE=host`

## 2) Servico `embalando-solucoes-api`
1. Conectar ao mesmo repositorio GitHub.
2. Apontar para `rootDirectory=apps/lekteck-api` (mesmo codigo da Lekteck).
3. Ativar auto-deploy.
4. Variaveis:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `APP_MODE=multitenant`
   - `TENANT_DEFAULT=embalando`
   - `APP_PUBLIC_NAME=Embalando`

## 3) Servico `redemaisfarma-api`
1. Conectar ao mesmo repositorio GitHub.
2. Definir `rootDirectory=apps/saudemaisfarma-api`.
3. Ativar auto-deploy.
4. Variaveis:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `APP_MODE=singletenant`
   - `APP_PUBLIC_NAME=SaudeMaisFarma`

---

## Regras de isolamento de negocio
1. Tudo que for de farmacia/controlados fica somente em `saudemaisfarma-api`.
2. `lekteck-api` nao recebe regras de receita controlada.
3. Tema/branding por tenant no multitenant:
   - header/footer/catalogo/nome IA/config login.
4. Nomes de IA definidos:
   - SaudeMaisFarma: `Alysson`
   - Embalando: `Livoninho`

---

## Sincronizacao de atualizacao (mesmo repositorio)
1. Um push no repositorio pode disparar os 3 deploys automaticamente.
2. Cada servico builda apenas sua pasta (`rootDirectory`) ou modo (via env var).
3. Mudanca em Saude nao precisa alterar Lekteck, e vice-versa.
4. Se quiser reduzir rebuild desnecessario, usar workflow com filtro de caminhos por app.

---

## Plano de desacoplamento futuro da Saude (sem trauma)
1. Manter API da Saude dentro de `apps/saudemaisfarma-api`.
2. Evitar acoplamento com classes internas da Lekteck.
3. Padronizar contratos HTTP independentes.
4. Quando decidir separar:
   - mover `apps/saudemaisfarma-api` para repo novo
   - reapontar Railway do servico `redemaisfarma-api`
   - manter dominios e variaveis sem alterar clientes.

---

## Checklist de execucao
1. Criar/ajustar pastas `apps/lekteck-api` e `apps/saudemaisfarma-api`.
2. Ajustar `Dockerfile`/`railway.json` para funcionar por app (ou por variavel de modo).
3. Vincular `embalando-solucoes-api` ao mesmo repo (hoje ele precisa desse ajuste).
4. Confirmar auto-deploy ligado nos 3 servicos.
5. Validar healthcheck:
   - `/actuator/health` dos 3 dominios
6. Validar tenant correto em Embalando (`embalando`) e IA (`Livoninho`).
7. Validar Saude isolada com IA `Alysson`.

---

## Riscos e mitigacao
1. Risco: deploy cruzado quebrar app nao relacionado.
   - Mitigacao: `rootDirectory` separado + variaveis por servico.
2. Risco: acoplamento silencioso entre Saude e Lekteck.
   - Mitigacao: regra de dependencia por modulo e revisao de PR.
3. Risco: divergencia de dominio/DNS.
   - Mitigacao: checklist de DNS e healthcheck apos cada deploy.
