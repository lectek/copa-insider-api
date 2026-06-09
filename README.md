# RedeMaisFarma API

Documentacao tecnica atual do projeto `redemaisfarma-api` (API + frontend web server-side + integracoes).

## Sumario
1. Visao geral
2. Stack e arquitetura
3. Estrutura do repositorio
4. Como executar
5. Perfis e configuracoes
6. API atual (mapa funcional)
7. Banco de dados atual
8. Migracoes Flyway
9. Frontend atual (Thymeleaf + assets)
10. Testes e qualidade
11. Midia e persistencia de imagens
12. Observabilidade e operacao
13. Troubleshooting rapido

## 1. Visao geral
- Aplicacao monolitica em Spring Boot, empacotada como `jar`.
- Projeto com API REST, paginas MVC (Thymeleaf), jobs agendados, autenticacao, envio de email e integracoes legadas.
- Arquitetura por camadas com abordagem hexagonal:
  - `domain`
  - `application`
  - `adapters` (inbound/outbound)
  - `config`

## 2. Stack e arquitetura
### Stack principal
- Java: `21`
- Spring Boot: `3.5.11`
- Spring MVC + WebFlux (WebClient)
- Spring Security
- Spring Data JPA (MySQL)
- Flyway
- Thymeleaf
- Kafka (dependencia presente, uso por perfil/config)
- Redis (dependencia presente, normalmente desabilitado em dev)
- MapStruct + Lombok
- JUnit 5 + Mockito + Testcontainers

### Pontos tecnicos importantes
- Classe principal: `br.com.redemaisfarma.RedeMaisFarmaApiApplication`
- `@EnableScheduling` ativo (jobs/schedulers em runtime)
- Deploy em Railway tem comportamento explicito:
  - detecta ambiente Railway
  - pode forcar `prod` se nenhum perfil ativo estiver definido

## 3. Estrutura do repositorio
- `src/main/java/br/com/redemaisfarma`
  - `adapters/inbound`: controllers web/API, filtros, seguranca
  - `adapters/outbound`: persistencia, email, cache, integracoes
  - `application`: servicos, DTOs, mapeadores, view models
  - `domain`: regras e modelos de dominio
  - `config`: configuracoes de datasources, flyway, security, etc.
- `src/main/resources`
  - `application*.yml` (perfis)
  - `db/migration` e `db/migration-mysql` (Flyway)
  - `templates` (Thymeleaf)
  - `static` (css/js/imagens)
- `src/test/java` e `src/test/resources`
- `docker-compose*.yml`, `Dockerfile`, `.env`

## 4. Como executar
### Requisitos
- Java 21
- Maven Wrapper (`./mvnw`)
- Docker Desktop (para ambiente containerizado)

### Execucao local (sem container do app)
1. Suba o MySQL de desenvolvimento:
```bash
docker compose -f docker-compose.dev.yml up -d mysql
```
2. Rode a aplicacao:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Execucao completa via Docker Compose
```bash
docker compose -f docker-compose.dev.yml up --build -d
```

### URLs uteis em dev (compose)
- App: `http://localhost:18090`
- Swagger UI: `http://localhost:18090/docs`
- OpenAPI JSON: `http://localhost:18090/v3/api-docs`
- Health: `http://localhost:18090/actuator/health`
- Mailpit UI: `http://localhost:8026`
- Mock externo (WireMock): `http://localhost:8081`
- MySQL host: `localhost:3310`

## 5. Perfis e configuracoes
### Perfis principais
- `dev`
  - datasource local (por padrao `localhost:3306` no yml)
  - Flyway habilitado
  - thymeleaf sem cache
- `docker`
  - datasource para container `mysql`
  - configuracoes de runtime para compose
- `prod`
  - configuracao de producao
  - mail/datasource via variaveis de ambiente
- `legacy`
  - foco em conexao Firebird legado
  - Flyway desabilitado nesse perfil
- `test`
  - MySQL via Testcontainers (`jdbc:tc:mysql`)

### Observacao sobre datasource MySQL
`MySqlDataSourceConfig` resolve URL de banco por prioridade:
1. `RAILWAY_MYSQL_URL`
2. `DATABASE_URL`
3. `MYSQL_PRIVATE_URL`
4. (nao-prod) `SPRING_DATASOURCE_URL`
5. (nao-prod) `MYSQL_URL`

Em producao, se URL de banco nao estiver definida, a aplicacao falha no startup.

### Variaveis de ambiente mais relevantes
- Banco:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `MYSQL_URL` (formato `mysql://user:pass@host:port/db`)
- JWT:
  - `JWT_ENABLED`
  - `JWT_SECRET`
  - `JWT_ISSUER`
- Mail:
  - `APP_MAIL_ENABLED`
  - `APP_MAIL_FROM`
  - `SPRING_MAIL_HOST`
  - `SPRING_MAIL_PORT`
  - `APP_MAIL_API_PROVIDER` (opcional, ex: `brevo`)
  - `APP_MAIL_API_KEY` (opcional, fallback HTTPS quando SMTP falhar)
  - `APP_MAIL_API_BASE_URL` (opcional, padrao Brevo)
- SMS:
  - `APP_SMS_ENABLED`
  - `APP_SMS_PROVIDER` (`twilio` ou `brevo`)
  - `APP_SMS_FROM` (remetente/sender)
  - `APP_SMS_DEFAULT_COUNTRY_CODE` (padrao `55`)
  - Twilio:
    - `APP_SMS_TWILIO_ACCOUNT_SID`
    - `APP_SMS_TWILIO_AUTH_TOKEN`
    - `APP_SMS_TWILIO_MESSAGING_SERVICE_SID`
  - Brevo:
    - `APP_SMS_BREVO_API_KEY` (ou `APP_MAIL_API_KEY`)
    - `APP_SMS_BREVO_BASE_URL` (opcional, padrao `https://api.brevo.com`)
    - `APP_SMS_BREVO_TYPE` (opcional: `transactional` ou `marketing`)
- Legado Firebird:
  - `LEGACY_SYNC_ENABLED=true`
  - `spring.datasource.firebird.jdbc-url`
  - `spring.datasource.firebird.username`
  - `spring.datasource.firebird.password`
- Storage de imagens:
  - `APP_MEDIA_PROVIDER` (`local` ou `s3`)
  - `APP_MEDIA_DIR`
  - `APP_MEDIA_PUBLIC_BASE`
  - `APP_MEDIA_USER_DIR`
  - `APP_MEDIA_USER_PUBLIC_BASE`
  - `APP_MEDIA_S3_BUCKET`
  - `APP_MEDIA_S3_REGION`
  - `APP_MEDIA_S3_ENDPOINT`
  - `APP_MEDIA_S3_ACCESS_KEY`
  - `APP_MEDIA_S3_SECRET_KEY`
  - `APP_MEDIA_S3_PATH_STYLE_ACCESS`
  - `APP_MEDIA_S3_PRODUCT_PREFIX`
  - `APP_MEDIA_S3_USER_PREFIX`

### Regra atual para persistencia de imagens
- `local` continua valido para desenvolvimento.
- Em producao, `local` sob `java.io.tmpdir` e considerado volatil: restart/deploy pode apagar avatar de usuario e fotos de produto.
- Para garantir persistencia em Railway, usar volume persistente ou `APP_MEDIA_PROVIDER=s3` com `APP_MEDIA_PUBLIC_BASE` e `APP_MEDIA_USER_PUBLIC_BASE` absolutos.
- O backend hoje salva:
  - avatar de usuario
  - foto principal e galeria de produto
  - imagem gerada por IA
  - rollback de arquivo se a gravacao no banco falhar

## 6. API atual (mapa funcional)
A API e dividida em grupos de prefixos. Para contrato completo, usar `/v3/api-docs`.

### Prefixos principais
- Publico:
  - Canonico: `/api/public/produtos`
  - Legados (deprecated): `/api/public/vitrine`, `/api/public/carrossel`
  - `/api/public/produtos/{produtoId}/stock/subscribe`
- Auth:
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/auth/otp/*`
  - `/api/auth/email-claim/*`
  - `/api/auth/esqueci-senha`
  - `/api/auth/resetar-senha`
- Cliente autenticado (self-service):
  - `/api/cliente/me`
  - `/api/cliente/me/pedidos`
  - `/api/cliente/me/carrinho`
  - `/api/cliente/me/checkout/*`
  - `/api/cliente/me/favoritos`
  - `/api/cliente/me/notificacoes`
- Admin API:
  - `/api/admin/produtos`
  - `/api/admin/marketing/emails/campanhas`
  - `/api/admin/estoque-fisico`
  - `/api/admin/imagens/*`
- Outros prefixos ativos:
  - `/api/v2/*` (legado para compatibilidade)
  - `/api/app/*` (legado para compatibilidade)
  - `/api/relatorios/*`
  - `/api/suporte/*`
  - `/api/email/*`
  - `/api/firebird/*` (legado)

### Regra atual de visibilidade de produtos para usuario final
As consultas publicas de catalogo usam filtro de disponibilidade no repositiorio:
- `disponivel = true`
- `estoque > 0`
- `preco_venda > 0`

Isso afeta:
- `/api/public/produtos`
- `/api/public/produtos/destaques`
- paginas de vitrine que reutilizam `searchPublicPage`/`findCarrossel`/`findVitrineFallback`

### Swagger/OpenAPI
- UI: `/docs`
- JSON: `/v3/api-docs`
- Grupos configurados:
  - `public`
  - `admin`
  - `export`
  - `actuator`

### Seguranca (estado atual)
Ha varias `SecurityFilterChain` com segmentacao por matcher/ordem:
- `SecurityActuatorConfig` (endpoints actuator)
- `SecurityApiConfig` (`/api/**`, JWT stateless quando habilitado)
- `SecurityMvcConfig` (rotas web MVC, login form)
- `SecurityExportConfig` (`/admin/export/**`)

Regras relevantes:
- `/api/public/**` permitido sem auth
- `/api/admin/**` exige `ROLE_ADMIN`
- `/admin/**` exige `ROLE_ADMIN` (com excecoes em fluxos especificos)
- login web em `/auth/login`

Observacao: `MethodSecurityConfig` esta com `@EnableMethodSecurity` e todos os flags em `false`.

## 7. Banco de dados atual
## 7.1 Banco principal: MySQL
- JPA principal configurado em `MySqlDataSourceConfig`.
- Entidades no pacote `br.com.redemaisfarma`.
- Tabelas de dominio principais (mapeadas por entidades atuais):
  - Autenticacao/usuarios:
    - `usuario`, `roles`, `usuario_roles`, `refresh_tokens`, `password_reset_token`, `customers`, `otp_code`
  - Clientes:
    - `cliente`, `cliente_favorito`, `cliente_notificacao`
  - Catalogo/produtos:
    - `produto`, `produto_categoria`, `product_image_job`, `product_stock_subscription`, `movimento_estoque`, `sync_checkpoint`
  - Pedidos:
    - `pedido`, `item_pedido`
  - Marketing/email:
    - `email_campaign`, `email_campaign_queue`, `email_campaign_log`, `email_template`, `email_delivery`
  - Configuracoes/financeiro/fiscal:
    - `app_settings`, `gateway_config`, `nota_fiscal_confirmacao`

## 7.2 Banco legado: Firebird
- Configurado por `FirebirdDataSourceConfig`.
- Nao sobe por padrao: exige `legacy.sync.enabled=true` e propriedades de conexao firebird.
- Uso principal:
  - leitura/sincronizacao de catalogo legado
  - endpoints de apoio legado (`/api/firebird/*`)

## 7.3 Redis e Kafka
- Redis e Kafka estao no `pom.xml` e em configuracoes.
- Em dev, e comum manter desabilitado (`kafka.enabled=false`, listeners off).
- Compose principal traz Kafka e Firebird no arquivo `docker-compose.yml`; compose de dev simplificado em `docker-compose.dev.yml` foca no fluxo de desenvolvimento do app.

## 8. Migracoes Flyway
## 8.1 Como esta configurado hoje
Flyway e configurado programaticamente em `MySqlDataSourceConfig`:
- locations:
  - `classpath:db/migration`
  - `classpath:db/migration-mysql`
- `baselineOnMigrate(true)`
- `outOfOrder` controlado por `FLYWAY_OUT_OF_ORDER` (default `true`)
- `migrate()` executado no startup

### Estrutura atual dos scripts
Pastas em `src/main/resources/db/migration`:
- `ADM`, `AUTH`, `CFG`, `CLI`, `FIN`, `FISC`, `MKT`, `mysql`, `PED`, `PROD`, `USER`

Snapshot atual do repositorio:
- total aproximado de scripts Flyway: `87`

### Convencao recomendada de nomes
- Padrao recomendado no projeto:
  - `VYYYYMMDD_NN__descricao.sql`
- Exemplo real:
  - `V20260227_01__produto_add_metodo_leitura_codigo_barras.sql`

### Comandos uteis Flyway (via Maven)
```bash
./mvnw flyway:info
./mvnw flyway:validate
./mvnw flyway:migrate
./mvnw flyway:repair
```

Com override de host/porta local:
```bash
./mvnw -Dmysql.host=localhost -Dmysql.port=3310 -Dmysql.database=redemaisfarma -Dmysql.user=app_user -Dmysql.password=*** flyway:info
```

### Troubleshooting de migracao
Erro comum:
- `Detected applied migration not resolved locally`

Causas comuns:
- script foi removido/renomeado apos ter sido aplicado no banco

Opcoes de tratamento:
1. restaurar o arquivo de migracao ausente
2. executar `flyway:repair` apenas se a remocao foi intencional
3. em ambiente local descartavel, recriar volume do banco

## 9. Frontend atual (Thymeleaf + assets)
### Tecnologia
- Renderizacao server-side com Thymeleaf.
- Assets estaticos em `src/main/resources/static`.
- Resource handlers mapeiam:
  - `/css/**` -> `classpath:/static/css/`
  - `/js/**` -> `classpath:/static/js/`
  - `/images/**` -> `classpath:/static/images/`
  - `/animations/**` -> `classpath:/static/animations/`
  - `/media/**` -> `file:media/` (arquivos gerados em runtime)

Observacao:
- URLs absolutas de bucket/CDN tambem sao suportadas para imagens persistidas. Esse e o modo recomendado em producao.

### Estrutura de templates
- `templates/pages/admin/*`
  - dashboard, produtos, pedidos, clientes, marketing, financeiro, configuracoes, vendas
- `templates/pages/cliente/*`
  - home, produtos, carrinho, checkout, conta, pedidos, sobre
- `templates/pages/auth/*`
  - login/cadastro/reset
- `templates/fragments/*`
  - layout, header, footer, sidebar, componentes reutilizaveis

### Assets
- CSS por pagina em `static/css/pages/*`
- JS por pagina em:
  - `static/js/pages/admin/*`
  - `static/js/pages/cliente/*`

### Rotas web (MVC) mais usadas
- Publicas:
  - `/`, `/cliente`, `/cliente/index`
  - `/produtos`, `/produtos?q=...`, `/produto/{id}`
  - `/sobre`
  - `/auth/login`, `/auth/cliente/cadastro`
- Cliente autenticado:
  - `/carrinho`
  - `/checkout`
  - `/checkout/confirmacao`
  - `/cliente/conta`, `/cliente/dados`, `/cliente/senha`, `/cliente/pedidos`
- Admin:
  - `/admin/dashboard`
  - `/admin/produtos/*`
  - `/admin/pedidos/*`
  - `/admin/clientes/*`
  - `/admin/marketing/*`
  - `/admin/configuracoes/*`

## 10. Testes e qualidade
### Executar testes
```bash
./mvnw test -Dspring.profiles.active=test
```

### Build completo
```bash
./mvnw clean verify
```

### Perfil de teste
- `src/test/resources/application-test.yml`
- usa Testcontainers (`jdbc:tc:mysql:8.0.43`)
- desabilita componentes que nao sao alvo do teste (kafka/redis etc)

### Cobertura atual de testes (exemplos)
- controllers admin de produto e marketing
- self-service do cliente
- jobs de automacao (carrinho abandonado, reengajamento, recompra, back-in-stock)
- worker de email
- normalizacao de codigo de barras

## 11. Midia e persistencia de imagens
### Fluxo atual
- Uploads de produto e avatar convergem para `ImageStorageService`.
- Imagem gerada por IA e persistida por `AiGeneratedImageStorageService`, que tambem usa `ImageStorageService`.
- Exclusao faz rollback seguro por URL para evitar arquivo orfao quando o banco falha.

### Provedor local
- Usa `APP_MEDIA_DIR` e `APP_MEDIA_USER_DIR`.
- Bom para dev e smoke tests locais.
- Nao e suficiente para producao quando estiver apontando para `/tmp` ou outro filesystem efemero.

### Provedor S3/R2
- Ative com `APP_MEDIA_PROVIDER=s3`.
- Informe pelo menos:
  - `APP_MEDIA_S3_BUCKET`
  - `APP_MEDIA_PUBLIC_BASE`
  - `APP_MEDIA_USER_PUBLIC_BASE`
- Para Cloudflare R2 ou endpoint compativel, informe tambem:
  - `APP_MEDIA_S3_ENDPOINT`
  - `APP_MEDIA_S3_ACCESS_KEY`
  - `APP_MEDIA_S3_SECRET_KEY`
- URLs publicas devem ser absolutas (`https://...`), porque a API passa a gravar esse endereco diretamente no banco.

### Checklist antes de deploy em producao
1. Confirmar se o storage sera volume persistente ou `s3`.
2. Se for `s3`, validar bucket, endpoint, credenciais e URLs publicas.
3. Fazer upload manual de um avatar e de uma imagem de produto.
4. Reiniciar a aplicacao.
5. Confirmar que as URLs continuam abrindo apos o restart.

## 12. Observabilidade e operacao
- Actuator:
  - `/actuator/health`
  - `/actuator/info`
  - exposicoes adicionais por perfil
- OpenAPI:
  - `/v3/api-docs`
  - `/docs`
- Logs:
  - configuracao em `logback-spring.xml`
  - no compose, logs do app em volume `app_logs_dev`

## 13. Troubleshooting rapido
### App sobe no Docker mas fica reiniciando
- Verifique logs do app:
```bash
docker compose -f docker-compose.dev.yml logs --tail 200 app
```
- Se o erro envolver Flyway, valide migracoes e schema history.

### Sem produtos na vitrine publica
- Verifique dados em `produto`:
  - `disponivel = 1`
  - `estoque > 0`
  - `preco_venda > 0`
- Se esses criterios nao forem atendidos, os endpoints publicos retornam vazio por regra de negocio.

### Firebird nao conecta
- Confirme `LEGACY_SYNC_ENABLED=true` e propriedades `spring.datasource.firebird.*`.
- Sem isso, o datasource legado nao e inicializado.

### Avatar ou foto some apos deploy
- Verifique se `APP_MEDIA_PROVIDER` ainda esta em `local`.
- Em Railway, nao deixe `APP_MEDIA_DIR` ou `APP_MEDIA_USER_DIR` apontando para `${java.io.tmpdir}` se a expectativa for persistencia.
- Se estiver usando `s3`, confirme:
  - `APP_MEDIA_S3_BUCKET`
  - `APP_MEDIA_PUBLIC_BASE`
  - `APP_MEDIA_USER_PUBLIC_BASE`
  - endpoint/credenciais quando aplicavel
- Confirme tambem se o banco esta gravando URL absoluta do bucket/CDN, e nao apenas `/media/...`.

---

## Documentacao complementar no repositorio
- `README_DEV.md`
- `README_CONFIG.md`
- `docs/*`
- `AGENTS.md`
