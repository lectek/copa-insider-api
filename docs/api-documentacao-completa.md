# Documentacao Completa da API RedeMaisFarma

Data de referencia: 11/03/2026

## 1. Objetivo do documento

Este documento consolida a visao executiva e tecnica da `redemaisfarma-api` com base no codigo atual do repositorio, perfis Spring, infraestrutura Docker, servicos disponiveis, modulos de IA, roteirizacao, integracoes, variaveis de ambiente e diretrizes de uso.

Escopo deste documento:

- tecnologias usadas
- servicos oferecidos
- estrutura tecnica e organizacional
- variaveis e propriedades principais
- bancos de dados e componentes de infraestrutura
- frameworks e linguagens
- termos de uso e operacao
- analise de urgencia
- proximos passos recomendados

Este documento complementa, mas nao substitui:

- OpenAPI/Swagger em `/docs`
- JSON OpenAPI em `/v3/api-docs`
- demais documentos pontuais em `docs/`

## 2. Resumo executivo

A aplicacao e um monolito Spring Boot com arquitetura em camadas e orientacao hexagonal, responsavel por API REST, paginas MVC com Thymeleaf, autenticacao, catalogo publico, carrinho, checkout, pedidos, backoffice administrativo, marketing por e-mail, notificacoes, integracoes legadas com Firebird, roteirizacao de entregas e recursos de IA.

Hoje, o projeto ja possui duas frentes de IA implementadas:

- assistente conversacional em `/api/ia/ask`, com integracao local via Ollama e fallback textual
- geracao de imagens de produto no backoffice, usando `ProductImageJobService` e `ImageStudioUseCase`

Tambem ja existe servico de roteirizacao de entregas com:

- geocodificacao por Nominatim
- matriz viaria por OSRM quando disponivel
- fallback geometrico quando OSRM nao estiver ativo
- previsao publica de entrega baseada na fila real de pedidos

O maior ponto de atencao imediato nao e a inexistencia de funcionalidade, e sim a necessidade de consolidar governanca, seguranca, observabilidade e qualidade operacional em cima do que ja foi construido.

## 3. Visao geral da solucao

### 3.1 O que o sistema faz

O sistema atende operacao de farmacia e ecommerce com os seguintes blocos:

- vitrine publica de produtos
- autenticacao e gestao de conta do cliente
- carrinho, checkout e pedidos
- gestao administrativa de produtos, clientes, pedidos e configuracoes
- marketing por e-mail e notificacoes
- integracao com catalogo legado
- configuracao de pagamentos e terminal
- configuracao fiscal
- previsao e roteirizacao de entrega
- automacao de imagem de produto por IA

### 3.2 Tipo de aplicacao

- backend principal: monolito Java
- exposicao: REST + MVC server-side
- deploy: JAR Spring Boot
- persistencia principal: MySQL
- integracao legado: Firebird
- infraestrutura local: Docker Compose

## 4. Linguagens, frameworks e stack tecnica

### 4.1 Linguagens usadas

- Java 21
- SQL para migracoes Flyway
- HTML com Thymeleaf
- CSS
- JavaScript
- YAML para configuracao Spring
- Docker Compose YAML para infraestrutura local
- PowerShell e scripts auxiliares para operacao e diagnostico

### 4.2 Frameworks e bibliotecas principais

- Spring Boot 3.5.11
- Spring MVC
- Spring WebFlux e WebClient
- Spring Security
- Spring Data JPA
- Flyway
- Thymeleaf
- Spring Kafka
- Spring Data Redis
- springdoc OpenAPI
- Resilience4j
- MapStruct
- Lombok
- JJWT
- JUnit 5
- Mockito
- Testcontainers
- WireMock

### 4.3 Componentes de infraestrutura usados no projeto

- MySQL 8.0
- Firebird 2.5
- Kafka 3.7
- Redis opcional
- Mailpit
- WireMock
- Nominatim
- OSRM

## 5. Arquitetura e organizacao do codigo

### 5.1 Estrutura macro

O projeto segue uma organizacao proxima de arquitetura hexagonal:

- `domain`: regras de negocio, enums, modelos centrais, servicos de dominio
- `application`: servicos de aplicacao, DTOs, mapeadores, casos de uso e view models
- `adapters/inbound`: controllers REST, controllers MVC, filtros, seguranca e handlers
- `adapters/outbound`: persistencia JPA, legado Firebird, mensageria, cache, IA e integracoes
- `config`: datasources, seguranca, mensageria e bootstrap tecnico

### 5.2 Estrutura relevante do repositorio

- `src/main/java/br/com/redemaisfarma`
- `src/main/resources/application*.yml`
- `src/main/resources/db/migration*`
- `src/main/resources/templates`
- `src/main/resources/static`
- `src/test/java`
- `docs/`
- `infra/`
- `docker-compose.dev.yml`
- `docker-compose.yml`
- `pom.xml`

### 5.3 Arquitetura funcional

O backend combina quatro estilos:

- API REST para integracoes e frontend desacoplado
- MVC com Thymeleaf para paginas renderizadas no servidor
- jobs agendados para automacoes
- adaptadores de integracao para legados, IA, email, mensageria e roteirizacao

## 6. Servicos oferecidos pela API

### 6.1 Catalogo publico e descoberta de produtos

Principais rotas e capacidades:

- `GET /api/public/produtos`
- `GET /api/public/produtos/destaques`
- `GET /api/public/produtos/{id}`
- `GET /api/public/carrossel/destaques`
- `GET /api/public/vitrine/destaques`
- `POST /api/public/produtos/{produtoId}/stock/subscribe`

Regra atual de publicacao para catalogo publico:

- produto disponivel
- estoque maior que zero
- preco de venda maior que zero

### 6.2 Autenticacao, identidade e conta do cliente

Principais grupos:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/otp/*`
- `POST /api/auth/email-claim/*`
- recuperacao de senha em `/auth/*` e `/cliente/auth/*`
- gestao de conta e avatar em `/api/cliente/me/*` e `/cliente/*`

Recursos suportados:

- login com formulario
- JWT para APIs quando habilitado
- OTP
- fluxo de reset de senha
- suporte a OAuth2 em perfis especificos

### 6.3 Carrinho, checkout e pedidos

Capacidades centrais:

- carrinho do cliente
- checkout
- historico de pedidos
- confirmacao de pedido
- consulta por cliente autenticado

Rotas e componentes relevantes:

- `/api/cliente/me/carrinho`
- `/api/cliente/me/checkout/*`
- `/api/cliente/me/pedidos`
- `/pedido/finalizar`
- `/checkout/confirmacao`

### 6.4 Backoffice administrativo

Capacidades principais:

- cadastro, edicao, validacao e publicacao de produtos
- upload e geracao de imagem
- gestao de clientes
- gestao de pedidos
- configuracoes gerais e branding
- configuracoes de email, fiscal, permissoes e integracoes
- relatorios
- estoque fisico
- notificacoes
- venda rapida

Rotas e grupos importantes:

- `/admin/*`
- `/api/admin/produtos`
- `/api/admin/imagens/*`
- `/api/admin/clientes/*`
- `/api/admin/estoque-fisico/*`
- `/api/admin/entregas/*`
- `/api/admin/pagamentos/metodos`
- `/api/admin/pagamentos/terminal`
- `/api/admin/marketing/emails/campanhas`

### 6.5 Marketing e comunicacao

Modulos ja presentes:

- campanhas de email
- templates de email
- fila e log de entrega
- notificacoes para clientes
- alerta de volta ao estoque

### 6.6 Financeiro e pagamentos

Capacidades em codigo:

- configuracao de gateways
- cadastro de metodos de pagamento customizados
- configuracao e teste de terminal de pagamento
- paginas administrativas de financeiro

### 6.7 Fiscal

Capacidades ja presentes em codigo:

- configuracao do emitente fiscal
- suporte a provedor `FOCUS_NFE`
- ambiente de homologacao e producao
- snapshots fiscais de pedido
- historico e documentos fiscais recentes

### 6.8 Integracao legado

Capacidades:

- leitura de catalogo legado em Firebird
- sincronizacao incremental para MySQL
- checkpoint de sincronizacao
- operacao condicional por profile e propriedade

### 6.9 IA e automacao

Capacidades:

- assistente conversacional
- geracao de imagem de produto
- auto-disparo de jobs para produtos sem imagem

### 6.10 Roteirizacao de entrega

Capacidades:

- roteirizacao de ate 12 pedidos por execucao
- geracao de codigo de entrega
- confirmacao de entrega por codigo
- ETA publico baseado em fila real de entregas

## 7. Bancos de dados e persistencia

### 7.1 Banco principal: MySQL

Uso:

- dominio principal da aplicacao
- usuarios, clientes, produtos, pedidos, marketing, configuracoes e modulos administrativos

Grupos de tabelas do dominio:

- autenticacao e usuarios
- clientes e preferidos
- catalogo e estoque
- pedidos e itens
- jobs de imagem
- inscricoes de volta ao estoque
- campanhas e entregas de email
- configuracoes do sistema
- financeiro
- fiscal

Persistencia:

- Spring Data JPA
- Hibernate
- HikariCP

### 7.2 Banco legado: Firebird

Uso:

- leitura de produtos do legado
- sincronizacao controlada para MySQL

Caracteristicas:

- nao e obrigatorio em todos os ambientes
- so inicializa se `legacy.sync.enabled=true`
- configuracao lazy para nao derrubar a aplicacao quando o legado nao estiver acessivel

### 7.3 Redis

Uso atual:

- opcional
- suporte a blacklist de JWT quando a estrategia `jwt.blacklist.strategy=redis` estiver habilitada

### 7.4 Persistencia auxiliar da stack de roteirizacao

Nao e banco do dominio da farmacia, mas faz parte da operacao:

- Nominatim usa base propria para geocodificacao
- OSRM usa dataset preprocessado `.osrm`

## 8. Infraestrutura e servicos auxiliares

### 8.1 Ambiente de desenvolvimento simplificado

`docker-compose.dev.yml` sobe:

- `mysql`
- `mailpit`
- `cliente-mock`
- `app`

E opcionalmente, com profile `routing`:

- `nominatim`
- `osrm`

### 8.2 Ambiente Compose mais completo

`docker-compose.yml` inclui adicionalmente:

- `kafka`
- `firebird`
- servicos de roteirizacao

### 8.3 Portas usuais em dev

- app: `18090`
- mysql: `3310`
- mailpit UI: `8026`
- wiremock: `8081`
- nominatim: `8088`
- osrm: `5000`
- kafka externo: `9094`
- firebird host: `3054`

## 9. Perfis de execucao

Perfis e usos mais relevantes:

- `dev`: desenvolvimento local tradicional
- `docker`: runtime dentro do Compose
- `prod`: producao
- `oauth2`: ativa configuracao de login social
- `test`: testes com Testcontainers
- `legacy`: agrupado com `dev` no profile default para cenarios locais

Observacoes:

- o profile default e `dev`
- o grupo `dev` combina `dev,legacy`
- o grupo `prod-oauth2` combina `prod,docker,oauth2`

## 10. Variaveis e propriedades principais

### 10.1 Banco principal

Variaveis e propriedades mais importantes:

- `RAILWAY_MYSQL_URL`
- `DATABASE_URL`
- `MYSQL_PRIVATE_URL`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `MYSQL_URL`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

Ordem de resolucao do datasource MySQL:

1. `RAILWAY_MYSQL_URL`
2. `DATABASE_URL`
3. `MYSQL_PRIVATE_URL`
4. `SPRING_DATASOURCE_URL`
5. `MYSQL_URL`

### 10.2 Firebird legado

- `LEGACY_SYNC_ENABLED`
- `spring.datasource.firebird.jdbc-url`
- `spring.datasource.firebird.url`
- `spring.datasource.firebird.username`
- `spring.datasource.firebird.password`
- `FIREBIRD_HOST`
- `FIREBIRD_PORT`
- `FIREBIRD_DATABASE`
- `ISC_PASSWORD`

### 10.3 JWT e seguranca

- `JWT_ENABLED`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_ACCESS_TOKEN_TTL_SECONDS`
- `JWT_REFRESH_TOKEN_TTL_SECONDS`
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`
- `JWT_REFRESH_TOKEN_EXPIRATION_MINUTES`
- `JWT_BLACKLIST_STRATEGY`

### 10.4 Web e media

- `APP_WEB_BASE_URL`
- `APP_MEDIA_DIR`
- `APP_MEDIA_PUBLIC_BASE`
- `APP_MEDIA_USER_DIR`
- `APP_MEDIA_USER_PUBLIC_BASE`

### 10.5 Email e SMS

- `APP_MAIL_ENABLED`
- `APP_MAIL_FROM`
- `APP_MAIL_BCC`
- `APP_MAIL_CSS_URL`
- `APP_MAIL_NOOP`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_SMS_ENABLED`
- `APP_SMS_PROVIDER`
- `APP_SMS_FROM`
- `APP_SMS_DEFAULT_COUNTRY_CODE`
- `APP_SMS_TWILIO_ACCOUNT_SID`
- `APP_SMS_TWILIO_AUTH_TOKEN`
- `APP_SMS_TWILIO_MESSAGING_SERVICE_SID`

### 10.6 IA de imagem

- `APP_AI_IMAGE_PROVIDER`
- `APP_AI_IMAGE_POLLINATIONS_BASE_URL`
- `APP_AI_IMAGE_POLLINATIONS_MODEL`
- `APP_AI_IMAGE_POLLINATIONS_WIDTH`
- `APP_AI_IMAGE_POLLINATIONS_HEIGHT`
- `APP_AI_IMAGE_POLLINATIONS_NOLOGO`
- `APP_AI_IMAGE_POLLINATIONS_PRIVATE`

Propriedades tecnicas adicionais aceitas pelo codigo:

- `app.ai.image.pollinations.max-url-length`
- `app.ai.image.autogen.cron-enabled`
- `app.ai.image.autogen.batch-size`
- `app.ai.image.autogen.fixed-delay-ms`

### 10.7 IA conversacional

Propriedades configuradas por `AppAiOllamaProperties`:

- `app.ai.ollama.enabled`
- `app.ai.ollama.base-url`
- `app.ai.ollama.model`
- `app.ai.ollama.temperature`
- `app.ai.ollama.top-p`
- `app.ai.ollama.timeout-ms`
- `app.ai.ollama.system-prompt`

Defaults atuais do codigo:

- modelo: `llama3.1:8b-instruct`
- endpoint default: `http://localhost:11434`

### 10.8 Roteirizacao

- `APP_ROUTE_ENGINE`
- `APP_ROUTE_NOMINATIM_BASE_URL`
- `APP_ROUTE_NOMINATIM_USER_AGENT`
- `APP_ROUTE_NOMINATIM_TIMEOUT_MS`
- `APP_ROUTE_OSRM_BASE_URL`

### 10.9 Kafka e mensageria

- `KAFKA_ENABLED`
- `KAFKA_BOOTSTRAP_SERVERS`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `APP_KAFKA_GROUP_PEDIDOS`
- `app.kafka.topics.product-image-requested`

### 10.10 OAuth2

- `OAUTH_GOOGLE_CLIENT_ID`
- `OAUTH_GOOGLE_CLIENT_SECRET`
- `OAUTH_FACEBOOK_CLIENT_ID`
- `OAUTH_FACEBOOK_CLIENT_SECRET`
- `APP_OAUTH2_ENABLED`
- `APP_SECURITY_OAUTH2_ENABLED`

### 10.11 Integracao externa de cliente

- `INTEGRATIONS_CLIENTE_BASE_URL`
- `INTEGRATIONS_CLIENTE_BASEURL`
- `INTEGRATIONS_CLIENTE_MAX_MEMORY`
- `INTEGRATIONS_CLIENTE_READ_TIMEOUT`
- `INTEGRATIONS_CLIENTE_WRITE_TIMEOUT`

### 10.12 Configuracoes internas em banco (`app_settings`)

O projeto tambem usa configuracoes dinamicas persistidas em banco. Para a parte de entregas, as chaves mais criticas hoje sao:

- `entrega.rota.horario_saida`
- `entrega.rota.velocidade_media_kmh`
- `entrega.rota.minutos_por_parada`
- `entrega.rota.max_paradas`

Tambem existem chaves de contato, endereco, branding e configuracao geral utilizadas por `AppProps` e controllers de configuracao.

## 11. Seguranca, autenticacao e exposicao da API

### 11.1 Grupos de seguranca

O projeto usa cadeias de seguranca separadas:

- `SecurityApiConfig`
- `SecurityMvcConfig`
- `SecurityActuatorConfig`
- `SecurityExportConfig`
- `MethodSecurityConfig`

### 11.2 Regras principais atuais

- `/api/public/**` pode ser acessado sem autenticacao para leitura
- `POST /api/public/produtos/*/stock/subscribe` e publico
- `/api/admin/**` exige `ROLE_ADMIN`
- `/admin/**` exige `ROLE_ADMIN`
- `/api/financeiro/**` aceita `ADMIN` ou `FINANCEIRO`
- `/api/catalogo/**` aceita `ADMIN` ou `CATALOGO`
- `/api/suporte/**` aceita `ADMIN` ou `SUPORTE`

Swagger:

- em nao-producao, liberado
- em producao, restringido a admin no MVC

Actuator:

- em producao, apenas `health` e `info` sao publicos
- demais endpoints de actuator ficam restritos

### 11.3 Observacao importante

Ha uma liberacao temporaria em `SecurityMvcConfig` para `/admin/produtos` e `/admin/produtos/**`. Isso deve ser tratado como item urgente de seguranca antes de exposicao publica definitiva.

## 12. IA atualmente utilizada no projeto

### 12.1 Assistente conversacional

Endpoint:

- `POST /api/ia/ask`

Como funciona:

1. o controller recebe a mensagem e resolve uma `sessionId`
2. o servico `AiAssistantService` responde
3. a implementacao atual `OllamaAiAssistantService` chama `OllamaChatClient`
4. se o Ollama falhar ou estiver desabilitado, o sistema responde com fallback textual

Caracteristicas:

- endpoint publico
- limite atual de mensagem: 2000 caracteres
- prompt de sistema orientado a ecommerce
- evita assumir diagnostico medico como resposta oficial

### 12.2 Geracao de imagem de produto

Area funcional:

- `/admin/imagens`
- `POST /api/admin/imagens/{produtoId}/queue`
- `POST /api/admin/imagens/{produtoId}/regenerate`
- `GET /api/admin/imagens/jobs`

Fluxo atual:

1. o admin identifica um produto sem imagem
2. o sistema cria um `ProductImageRequestedEvent`
3. se Kafka estiver ativo, o evento pode ser publicado
4. mesmo assim, o fluxo atual executa processamento sincrono como fallback ou confirmacao imediata
5. `ProductPromptFactory` monta prompt e fingerprint
6. `ImageStudioUseCase` gera uma URL da imagem
7. `ProductImageJobService` tenta baixar a imagem, converter para PNG e persistir localmente
8. o produto e atualizado com a URL persistida
9. o job fica registrado com status `QUEUED`, `RUNNING`, `DONE`, `ERROR` ou `SKIPPED`

Provedor atual no codigo:

- provider default: `pollinations`
- provider alternativo local: `stub`
- modelo default no adapter atual: `flux`

Ponto forte:

- a funcionalidade de geracao ja existe
- existe historico de jobs
- existe suporte a reprocessamento

Ponto fraco:

- a interface atual retorna URL, o que favorece provedores por URL publica mas nao e o contrato ideal para provedores corporativos que entregam bytes ou base64
- se o download da imagem falha, o sistema pode manter fallback para URL remota, o que fragiliza governanca e durabilidade

### 12.3 Auto-disparo de imagem

Existe automacao para produtos sem imagem:

- servico: `ProductImageAutoTriggerService`
- propriedades:
  - `app.ai.image.autogen.cron-enabled`
  - `app.ai.image.autogen.batch-size`
  - `app.ai.image.autogen.fixed-delay-ms`

## 13. Servico de rota e previsao de entrega

### 13.1 O que o servico faz

O servico de rota atual calcula a melhor ordem de entregas com base em endereco de origem e lista de pedidos.

Componentes envolvidos:

- `DeliveryRouteService`
- `DeliveryRouteOptimizer`
- `PublicDeliveryEstimateService`
- `AdminEntregasRestController`

### 13.2 Como funciona tecnicamente

Fluxo principal da roteirizacao:

1. o admin envia os IDs dos pedidos para `/api/admin/entregas/roteirizar`
2. o sistema valida quantidade minima de 2 e maxima de 12 pedidos
3. cada pedido tem endereco de entrega resolvido e codigo de entrega gerado quando necessario
4. o backend geocodifica origem e destinos via Nominatim
5. se `APP_ROUTE_ENGINE` nao estiver em modo `geo`, tenta obter matriz real via OSRM
6. se OSRM falhar, o sistema cai para distancia geometrica
7. `DeliveryRouteOptimizer` calcula a melhor ordem por custo acumulado
8. a API devolve origem, distancia total, ordem de paradas e URL de mapa

### 13.3 Previsao publica de entrega

`PublicDeliveryEstimateService` usa:

- pedidos ativos em entrega
- endereco do cliente autenticado
- horario de saida da rota
- velocidade media configurada
- minutos por parada

Resultado:

- ETA textual para o cliente
- resumo do tipo "Entrega hoje por volta de HH:mm"

### 13.4 Limitacoes atuais

- a otimização atual foi pensada para ate 12 paradas
- sem OSRM ativo, a previsao e aproximada
- sem cache persistente de geocodificacao, chamadas repetidas podem custar desempenho

## 14. Recomendacao de IA para criacao de imagens

### 14.1 Situacao atual

A funcionalidade ja existe. O que falta e escolher um provedor melhor para testes controlados e, depois, para padronizacao.

### 14.2 Recomendacao pratica por nivel de esforco

#### Opcao A: menor esforco tecnico imediato

Manter `Pollinations` para smoke test e ajuste de prompt.

Quando usar:

- validacao rapida da tela
- testes internos de prompt
- provas de conceito de baixa governanca

Vantagens:

- encaixa no contrato atual baseado em URL
- ja esta implementado
- troca de modelo e configuracao e simples

Limites:

- menor previsibilidade operacional
- dependencia de URL remota
- qualidade e consistencia abaixo do ideal para padrao catalogo-farmacia

Referencia oficial:

- https://github.com/pollinations/pollinations

#### Opcao B: melhor candidata para piloto serio de qualidade

Adotar OpenAI `gpt-image-1.5` como provedor de teste controlado.

Quando usar:

- testes de qualidade visual
- validacao de consistencia de fundo, composicao e packshot
- piloto com governanca melhor que URL publica aberta

Impacto tecnico:

- exige ajuste do adapter atual
- o ideal e o provider devolver bytes/base64, e o backend persistir localmente antes de retornar a URL final da aplicacao

Motivos para recomendacao:

- melhor qualidade percebida para packshot controlado
- ecossistema bem documentado
- melhor caminho para futura governanca empresarial

Referencias oficiais:

- https://developers.openai.com/topics/imagegen/
- https://platform.openai.com/docs/guides/image-generation
- https://openai.com/api/pricing/

Observacao importante:

- a documentacao oficial informa exigencia de verificacao organizacional para acesso a modelos de imagem mais recentes

#### Opcao C: melhor alternativa se a prioridade for stack Google Cloud

Adotar Vertex AI `imagen-4.0-generate-001` ou `imagen-4.0-fast-generate-001`.

Quando usar:

- organizacoes que ja operam no ecossistema Google Cloud
- cenarios que precisam separar qualidade maxima e custo/performance

Impacto tecnico:

- tambem pede adapter orientado a bytes/base64
- boa opcao para pipeline corporativo

Referencias oficiais:

- https://cloud.google.com/vertex-ai/generative-ai/docs/models/imagen/4-0-generate-001
- https://cloud.google.com/vertex-ai/generative-ai/pricing#imagen-model-pricing

### 14.3 Recomendacao final

Se a decisao for "testar agora sem refatorar o contrato", use `Pollinations` apenas como teste operacional.

Se a decisao for "testar qualidade de verdade", a melhor escolha hoje e:

- OpenAI `gpt-image-1.5` para piloto de qualidade

Se a decisao for "padronizar em ambiente Google", a melhor escolha hoje e:

- Vertex AI `imagen-4.0-generate-001`

## 15. Estrategia recomendada para evoluir a IA de imagem

Para sair do estado atual e ir para um estado profissional, a evolucao recomendada e:

1. trocar o contrato de `ImageStudioUseCase` de `String generateSync(...)` para um retorno estruturado
2. retornar bytes/base64 ou stream, nao URL publica de terceiro como contrato principal
3. persistir a imagem em storage proprio via `ImageStorageService`
4. salvar no produto apenas a URL local/publica do sistema
5. manter provedores plugaveis:
   - `pollinations`
   - `openai`
   - `vertex-imagen`
   - `stub`

Contrato sugerido para evolucao:

- `provider`
- `mimeType`
- `bytes`
- `externalReference` opcional
- `metadata` opcional

## 16. Termos de uso e diretrizes operacionais

### 16.1 Uso da API

Esta API deve ser utilizada apenas por:

- aplicacoes oficiais da operacao
- integracoes aprovadas
- administradores autorizados
- ambientes homologados para testes internos

### 16.2 Seguranca e credenciais

- segredos nao devem ser versionados em documentacao publica
- variaveis sensiveis devem ficar em ambiente seguro
- credenciais expostas historicamente devem ser rotacionadas
- privilegios devem seguir minimo acesso necessario

### 16.3 Uso de IA

- respostas do assistente nao substituem orientacao medica, juridica, contabil ou fiscal
- imagens geradas por IA devem passar validacao humana antes de uso comercial
- para produtos farmaceuticos, IA nao deve inventar bula, rotulo, dosagem, faixa etaria ou claims regulados
- qualquer imagem de produto regulado deve respeitar identidade visual, rotulagem e compliance da operacao

### 16.4 Uso da roteirizacao

- a previsao de entrega e estimativa operacional, nao garantia absoluta
- roteirizacao depende da qualidade do endereco informado
- sem OSRM ou sem base geografica local, o ETA perde precisao

### 16.5 Dados pessoais e compliance

- dados de clientes devem ser tratados segundo LGPD
- logs devem evitar exposicao indevida de dados sensiveis
- acessos administrativos devem ser auditaveis

## 17. Build, testes e operacao

### 17.1 Comandos principais

Subir app em dev:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Subir infraestrutura minima:

```bash
docker compose -f docker-compose.dev.yml up -d mysql mailpit cliente-mock
```

Subir stack com roteirizacao:

```bash
docker compose -f docker-compose.dev.yml --profile routing up -d nominatim osrm
```

Rodar testes:

```bash
./mvnw test -Dspring.profiles.active=test
```

Pipeline de verificacao local:

```bash
./mvnw verify
```

### 17.2 O que o `verify` cobre

Com base no `pom.xml` atual:

- Maven Enforcer
- compilacao
- testes unitarios
- testes de integracao por Failsafe quando existirem `*IT`
- JaCoCo report
- analise de dependencias Maven
- empacotamento Spring Boot

## 18. Analise de urgencia

### P0 - critico

- Seguranca administrativa: existe liberacao temporaria de `/admin/produtos` sem auth no MVC. Isso precisa ser removido antes de exposicao definitiva.
- Credenciais e defaults: ha varios defaults sensiveis em configuracoes locais e de compose. E necessario revisar, rotacionar e separar ambientes antes de qualquer distribuicao externa.
- Documentacao central: a base de conhecimento esta fragmentada em README e varios documentos pontuais. Este documento reduz esse risco, mas ainda e recomendavel consolidar indice e ownership.

### P1 - alto

- IA de imagem: a funcionalidade existe, mas o contrato atual baseado em URL nao e o melhor para provedores corporativos. Isso limita qualidade, rastreabilidade e durabilidade.
- Roteirizacao: sem profile `routing` ativo e sem base local do Nominatim/OSRM, o sistema opera com aproximacao. Para prazo real, a stack geografica precisa estar operacional.
- Observabilidade: faltam indicadores consolidados para taxa de erro de jobs de imagem, falhas de geocodificacao e precisao de ETA.

### P2 - medio

- Cache de geocodificacao: ainda nao ha persistencia dedicada para reduzir repeticao de consultas.
- Modulos financeiro e fiscal: a base tecnica ja existe, mas a documentacao operacional e o rollout controlado ainda precisam amadurecer.
- Cobertura automatizada end-to-end: ha testes por modulo, mas vale ampliar testes integrados para IA de imagem, entregas e fiscal.

## 19. Proximos passos recomendados

### 19.1 Proximos passos imediatos

1. remover a liberacao temporaria de `/admin/produtos` sem autenticacao
2. rotacionar segredos e revisar defaults de ambiente
3. escolher o objetivo do piloto de imagem:
   - smoke test rapido: Pollinations
   - piloto de qualidade: OpenAI `gpt-image-1.5`
   - padrao Google Cloud: Vertex Imagen 4

### 19.2 Proximos passos tecnicos de curto prazo

1. refatorar `ImageStudioUseCase` para retorno binario estruturado
2. persistir imagem localmente como padrao obrigatorio
3. adicionar provider strategy para IA de imagem
4. criar metricas para:
   - job de imagem
   - geocodificacao
   - roteirizacao
   - ETA

### 19.3 Proximos passos de produto e operacao

1. ativar e validar stack `routing` com dataset real da regiao atendida
2. adicionar cache de geocodificacao
3. homologar fiscal e terminal de pagamento por ambiente
4. revisar termos internos de uso de IA com o negocio

## 20. Conclusao

O projeto ja possui uma base robusta e abrangente. Os modulos mais estrategicos citados no pedido do negocio ja existem em diferentes niveis de maturidade:

- IA conversacional: existente
- IA para imagem: existente
- servico de rota: existente
- backoffice e ecommerce: existentes
- pagamentos e fiscal: existentes, em consolidacao

O foco correto agora e elevar confiabilidade, seguranca, padronizacao documental e qualidade do provedor de imagem. A recomendacao mais pragmatica e usar `Pollinations` apenas como smoke test e abrir um piloto serio com `OpenAI gpt-image-1.5`, ajustando o contrato do adapter para persistencia local da imagem.
