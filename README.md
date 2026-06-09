# Copa Insider API

Documentação técnica do projeto `copa-insider-api`.

> **Contexto**: O Copa Insider é uma plataforma de conteúdo digital sobre Copa do Mundo — contexto pré-jogo, estatísticas, rivalidades, histórias e guias rápidos para fãs de futebol. Está sendo construído sobre a base legada da SaudeMaisFarma (RedeMaisFarma), com evolução incremental: partes do código legado existem temporariamente e serão substituídas conforme o produto evoluir.

## Sumario
1. Visão geral
2. Stack e arquitetura
3. Estrutura do repositório
4. Como executar
5. Perfis e configurações
6. API atual (mapa funcional)
7. Banco de dados
8. Migrações Flyway
9. Frontend (Thymeleaf)
10. Testes e qualidade
11. Mídia e persistência de imagens
12. Observabilidade
13. Troubleshooting

---

## 1. Visão geral
- Aplicação monolítica Spring Boot, empacotada como `jar`.
- API REST + páginas MVC (Thymeleaf) + jobs agendados + autenticação + envio de email.
- Arquitetura hexagonal por camadas:
  - `domain`
  - `application`
  - `adapters` (inbound/outbound)
  - `config`
- Pacote raiz: `br.com.lectek.copainsider`
- Classe principal: `CopaInsiderApplication`

### Estado atual do projeto
- Fase: MVP inicial
- Estruturas legadas da SaudeMaisFarma ainda presentes em muitos lugares
- Evolução incremental: adaptar e remover partes do legado aos poucos
- Foco: reaproveitamento, velocidade, redução de retrabalho

---

## 2. Stack e arquitetura
### Stack principal
- Java: `21`
- Spring Boot: `3.5.x`
- Spring MVC + Spring Security
- Spring Data JPA (MySQL)
- Flyway
- Thymeleaf
- MapStruct + Lombok
- JUnit 5 + Mockito + Testcontainers

### Dependências presentes mas uso configurável
- Kafka (desabilitado em dev, ativável por env)
- Redis (desabilitado em dev)
- Firebird/Jaybird (legado, exige `LEGACY_SYNC_ENABLED=true`)

---

## 3. Estrutura do repositório
```
src/main/java/br/com/lectek/copainsider/
  adapters/inbound/    → controllers web/API, filtros, segurança
  adapters/outbound/   → persistência, email, cache, integrações
  application/         → serviços, DTOs, mapeadores, view models
  domain/              → regras e modelos de domínio
  config/              → datasources, flyway, security, etc.

src/main/resources/
  application*.yml     → perfis de configuração
  db/migration/        → scripts Flyway
  templates/           → templates Thymeleaf
  static/              → css, js, imagens

src/test/java/         → testes espelham o main
docker-compose*.yml    → ambientes containerizados
Dockerfile
.env                   → variáveis locais (não commitar segredos)
```

---

## 4. Como executar
### Requisitos
- Java 21
- Maven Wrapper (`./mvnw`)
- Docker (para MySQL e serviços de suporte)

### Dev local (MySQL via Docker, app local)
```bash
# Sobe o MySQL
docker compose -f docker-compose.dev.yml up -d mysql

# Roda a aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Dev completo via Docker Compose
```bash
docker compose -f docker-compose.dev.yml up --build -d
```

### URLs em dev
- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/docs`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

---

## 5. Perfis e configurações
### Perfis disponíveis
| Perfil | Descrição |
|--------|-----------|
| `dev`  | MySQL local (`localhost:3306`), Flyway habilitado, sem cache Thymeleaf |
| `docker` | Datasource aponta para container `mysql` do compose |
| `prod` | Produção — conexão via variáveis de ambiente, JWT habilitado |
| `legacy` | Conexão Firebird legado (precisa de `LEGACY_SYNC_ENABLED=true`) |
| `test` | Testcontainers MySQL, desabilita integrações externas |

### Variáveis de ambiente principais

**Banco de dados (produção/Hostinger):**
```
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<porta>/<banco>?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<senha>
```

**JWT:**
```
JWT_ENABLED=true
JWT_SECRET=<segredo-longo-min-32-chars>
JWT_ISSUER=copainsider-api
```

**Mail:**
```
APP_MAIL_ENABLED=true
APP_MAIL_FROM=no-reply@copainsider.com.br
SPRING_MAIL_HOST=smtp.hostinger.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<email>
SPRING_MAIL_PASSWORD=<senha>
```

**Storage de mídia:**
```
APP_MEDIA_PROVIDER=local    # ou s3 para produção persistente
APP_MEDIA_DIR=media/products
APP_MEDIA_USER_DIR=media/users
```

---

## 6. API atual (mapa funcional)
A API foi herdada da SaudeMaisFarma e será adaptada progressivamente para Copa Insider.

### Prefixos ativos
- Público: `/api/public/produtos`, `/api/public/vitrine`
- Auth: `/api/auth/login`, `/api/auth/register`, `/api/auth/otp/*`
- Admin: `/api/admin/produtos`, `/api/admin/estoque-fisico`
- Cliente: `/api/cliente/me`, `/api/cliente/me/pedidos`, `/api/cliente/me/carrinho`

### Swagger/OpenAPI
- UI: `/docs`
- JSON: `/v3/api-docs`

---

## 7. Banco de dados
### Principal: MySQL
- Configurado em `MySqlDataSourceConfig`
- Em produção usa `SPRING_DATASOURCE_URL` (Hostinger ou qualquer MySQL compatível)
- Entidades no pacote `br.com.lectek.copainsider`

### Legado: Firebird
- Configurado por `FirebirdDataSourceConfig`
- Exige `LEGACY_SYNC_ENABLED=true` e propriedades de conexão
- Não sobe por padrão

---

## 8. Migrações Flyway
Configurado em `MySqlDataSourceConfig`:
- `baselineOnMigrate(true)`
- `outOfOrder` controlado por `FLYWAY_OUT_OF_ORDER` (default `true`)
- Locations: `classpath:db/migration`, `classpath:db/migration-mysql`

### Convenção de nomes
```
VYYYYMMDD_NN__descricao.sql
Ex: V20260609_01__copado_add_tabela_conteudo.sql
```

### Comandos Flyway via Maven
```bash
./mvnw flyway:info
./mvnw flyway:validate
./mvnw flyway:repair
```

---

## 9. Frontend (Thymeleaf)
- Server-side rendering com Thymeleaf
- Assets em `src/main/resources/static`
- Templates em `src/main/resources/templates`

### Estrutura de templates
```
templates/pages/admin/       → dashboard, produtos, pedidos, configs
templates/pages/site/        → home pública
templates/pages/auth/        → login, cadastro, reset
templates/pages/cliente/     → área do cliente
templates/fragments/         → layout, navbar, footer, componentes
```

---

## 10. Testes e qualidade
```bash
# Build + todos os testes
./mvnw clean verify

# Só testes (sem Docker)
./mvnw test -Dspring.profiles.active=test

# Pular integrações
./mvnw verify -DskipITs
```

Perfil `test` usa Testcontainers (MySQL) via `application-test.yml`.

---

## 11. Mídia e persistência de imagens
- `local`: desenvolvimento; em produção sob `/tmp` perde imagens no restart
- `s3`: para produção com persistência real

Para usar S3/R2:
```
APP_MEDIA_PROVIDER=s3
APP_MEDIA_S3_BUCKET=<bucket>
APP_MEDIA_PUBLIC_BASE=https://<cdn-ou-bucket-url>/products
APP_MEDIA_USER_PUBLIC_BASE=https://<cdn-ou-bucket-url>/users
APP_MEDIA_S3_ENDPOINT=<endpoint-se-nao-aws>
APP_MEDIA_S3_ACCESS_KEY=<key>
APP_MEDIA_S3_SECRET_KEY=<secret>
```

---

## 12. Observabilidade
- Health: `/actuator/health`
- Info: `/actuator/info`
- OpenAPI: `/v3/api-docs`, `/docs`
- Logs: configurados em `logback-spring.xml`

---

## 13. Troubleshooting
**App não sobe — erro de banco:**
- Confirme `SPRING_DATASOURCE_URL`, `USERNAME` e `PASSWORD` definidos

**Sem produtos na vitrine:**
- `disponivel = 1`, `estoque > 0`, `preco_venda > 0`

**Flyway falha com "migration not resolved":**
- Script removido após aplicado; restaurar ou executar `flyway:repair`

**Imagem some após deploy:**
- `APP_MEDIA_PROVIDER=local` em produção é volátil — migrar para `s3`

---

## Documentação complementar
- `README_DEV.md` — guia rápido de desenvolvimento
- `AGENTS.md` — guia para agentes de IA/automação
- `docs/transicao-legado.md` — decisões sobre transição SaudeMaisFarma → Copa Insider
- `docs/backlog.md` — backlog técnico
- `docs/playbook-desenvolvimento-seguro-estavel.md` — boas práticas
