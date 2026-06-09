# Copa Insider — Guia de Desenvolvimento

## Contexto do projeto
O Copa Insider está sendo construído sobre a base legada da SaudeMaisFarma (RedeMaisFarma).
O objetivo é evoluir essa base para uma plataforma de conteúdo sobre Copa do Mundo: contexto pré-jogo, estatísticas, rivalidades, histórias e guias para fãs.

**Fase atual: MVP**
- Ainda existem muitas estruturas herdadas do legado
- Código legado convive com código novo temporariamente
- Foco: reaproveitamento, velocidade, evolução incremental

---

## Stack
- Java 21, Spring Boot 3.5.x, Maven Wrapper
- MapStruct, Lombok
- Arquitetura hexagonal: `domain`, `application`, `adapters` (web/JPA), `config`
- Pacote raiz: `br.com.lectek.copainsider`
- Perfis: `dev` (MySQL local), `test` (Testcontainers MySQL), `prod` (Hostinger)

---

## Setup inicial (dev local)

### Pré-requisitos
- Java 21
- Docker Desktop
- MySQL local ou via Docker

### Subir banco local via Docker
```bash
docker compose -f docker-compose.dev.yml up -d mysql
```

### Rodar aplicação
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Build + testes completos
```bash
./mvnw clean verify
```

### Somente testes (sem Docker)
```bash
./mvnw test -Dspring.profiles.active=test
```

---

## Configuração local (application-dev.yml)
Banco padrão em dev:
```
localhost:3306/copainsider
usuário: root
senha: root
```

Para sobrescrever, defina no ambiente:
```
DEV_DATASOURCE_URL=jdbc:mysql://...
DEV_DATASOURCE_USERNAME=...
DEV_DATASOURCE_PASSWORD=...
```

---

## Estrutura de código relevante
```
config/MySqlDataSourceConfig.java     → configuração datasource (produção)
config/SecurityApiConfig.java         → segurança da API
CopaInsiderApplication.java           → entrypoint
adapters/inbound/web/                 → controllers
adapters/outbound/persistence/        → repositórios JPA
application/service/                  → serviços de aplicação
domain/                               → regras de negócio
```

---

## Código legado — o que ainda é herança da SaudeMaisFarma
As seguintes partes são originárias do legado e serão adaptadas/removidas progressivamente:
- Templates de email marketing (estrutura e nomenclatura)
- Endpoints `/api/v2/*` e `/api/app/*` (compatibilidade)
- Lógica de Firebird (banco legado; desabilitado por padrão)
- Nomes de variáveis com "redemaisfarma" no `.env`
- Estrutura de campanhas de email — será repensada para Copa Insider

> **Regra**: não remover legado abruptamente. Adaptar gradualmente e documentar as decisões em `docs/transicao-legado.md`.

---

## Infraestrutura de banco
### Desenvolvimento
- MySQL local ou container Docker

### Produção (Hostinger)
Conexão via variáveis de ambiente:
```
SPRING_DATASOURCE_URL=jdbc:mysql://<host-hostinger>:3306/<banco>?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<senha>
```

> Não há mais detecção de ambiente Railway. A conexão é direta via `SPRING_DATASOURCE_URL`.

---

## Convenções de código
- Injeção por construtor (sem `@Autowired` em campo)
- Indentação: 4 espaços, linhas até ~120 colunas
- Controllers: sufixo `Controller`
- DTOs: sufixo `RequestDTO` / `ResponseDTO`
- Mappers: sufixo `Mapper`
- Segredos fora do repo — usar variáveis de ambiente ou perfis YAML

---

## Testes
- JUnit 5 + Mockito + Testcontainers (MySQL)
- Unitários em `src/test/java`; integração espelha o main
- Cobrir tanto cenário de sucesso quanto de falha

---

## Flyway
- Scripts em `src/main/resources/db/migration/`
- Convenção: `VYYYYMMDD_NN__descricao.sql`
- `outOfOrder=true` por padrão (controlável via `FLYWAY_OUT_OF_ORDER`)

---

## Checklist de PR
- [ ] `./mvnw clean verify` passou
- [ ] Motivação e abordagem descritas
- [ ] Endpoints alterados documentados no Swagger/README
- [ ] Novos env vars listados
- [ ] `docs/transicao-legado.md` atualizado se houver mudança arquitetural

---

## Links úteis
- API docs (local): `http://localhost:8080/docs`
- Health (local): `http://localhost:8080/actuator/health`
- `docs/transicao-legado.md` — decisões e histórico de transição
- `docs/backlog.md` — backlog técnico
