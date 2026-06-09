RedeMaisFarma API — Guia de Desenvolvimento (DEV)

Stack: Java 21 · Spring Boot 3.3.x · Hexagonal (Ports & Adapters) · JPA/Hibernate · MySQL (primário) · Firebird (legado) · Flyway · MapStruct · Kafka · Docker/Compose · Testcontainers · Swagger/OpenAPI · Thymeleaf (admin)

Sumário

Visão geral

Requisitos

Início rápido (60s)

Perfis de execução

Variáveis de ambiente

Banco de dados

Migrações (Flyway)

Build & Run

Docker/Compose

Logs (Logback)

Swagger / OpenAPI

Estrutura de pastas

Persistência (múltiplos datasources)

Mensageria (Kafka)

Testes

Troubleshooting

Scripts úteis

Convenções de código

Segurança

Roadmap curto

Visão geral

A RedeMaisFarma API é uma aplicação Spring Boot modular seguindo arquitetura Hexagonal: domínio independente, adapters de entrada (web/controllers, schedulers) e saída (JPA, integrações), com MySQL como base principal e integração Firebird ao legado (Digifarma). Possui painel admin (Thymeleaf), Swagger para documentação, Flyway para migrações, Kafka para eventos, e Logback com rotação por data+tamanho.

Requisitos

JDK 21 (Temurin/Adoptium recomendado)

Maven 3.9+

Docker & Docker Compose (para stack completa)

Make (opcional, se usar scripts)

Git

Início rápido (60s)
# 1) Clonar
git clone TODO:URL_DO_REPO redemaisfarma-api
cd redemaisfarma-api

# 2) Subir dependências via Docker (MySQL, Firebird, Kafka, Mailpit...)
docker compose up -d

# 3) Build
mvn clean package -DskipTests

# 4) Rodar em DEV (perfil dev)
java -jar target/redemaisfarma-api-*.jar --spring.profiles.active=dev

# 5) Acessos rápidos
# Health
curl http://localhost:8080/actuator/health
# Swagger
# Abra no browser: http://localhost:8080/swagger-ui/index.html

Perfis de execução

dev: logs detalhados, console colorido, acesso a banco local, features de debug.

docker: pensado para rodar dentro do container (stdout + arquivo assíncrono).

prod: logs enxutos, appenders assíncronos, stdout sem cor.

test: ajustado para Testcontainers e diagnósticos de schema.

legacy: apenas calibra níveis de log do pacote legado (opcional).

Ativar com:

--spring.profiles.active=dev

Variáveis de ambiente

Crie um .env (usado pelo Compose) e/ou exporte antes de rodar:

# MySQL
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=redemaisfarma
MYSQL_USER=root
MYSQL_PASSWORD=TODO_SENHA

# Firebird legado
FB_HOST=localhost
FB_PORT=3050
FB_DB_PATH=/data/legacy.fdb
FB_USER=sysdba
FB_PASSWORD=masterkey

# Kafka
KAFKA_BROKER=localhost:9092

# Logs (opcional)
LOG_PATH=/opt/app/logs
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO
LOG_LEVEL_SPRING=INFO


Dica: no application.yml, aponte o MySQL como primário (vide seção Persistência).

Banco de dados

MySQL (primário): dados do sistema, migrations via Flyway.

Firebird (legado): leitura/escrita conforme integrações; nunca use como default.

Strings (exemplos):

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:redemaisfarma}?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver


Firebird (secundário):

legacy:
  firebird:
    datasource:
      url: jdbc:firebirdsql://${FB_HOST:localhost}:${FB_PORT:3050}/${FB_DB_PATH:/data/legacy.fdb}?encoding=UTF8
      username: ${FB_USER:sysdba}
      password: ${FB_PASSWORD:masterkey}
      driver-class-name: org.firebirdsql.jdbc.FBDriver

Migrações (Flyway)

Migrations em src/main/resources/db/migration (VYYYYMMDD.HH__descricao.sql).

Histórico automático em flyway_schema_history.

Comandos úteis:

# Reparar checksums (quando renomeou/ajustou scripts)
mvn org.flywaydb:flyway-maven-plugin:repair \
  -Dflyway.url=jdbc:mysql://localhost:3306/redemaisfarma \
  -Dflyway.user=root \
  -Dflyway.password=TODO_SENHA

# Limpar e migrar (apenas em DEV!)
mvn -Dflyway.cleanDisabled=false org.flywaydb:flyway-maven-plugin:clean migrate

Build & Run
# Build completo
mvn clean verify

# Rodar com perfil
java -jar target/redemaisfarma-api-*.jar --spring.profiles.active=dev

# Atalhos Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

Docker/Compose

Subir todo o stack:

docker compose up -d
docker compose ps


Parar:

docker compose down


Logs do app:

docker logs -f redemaisfarma-api


Portas padrão: API 8080 (mapeada para 18090 no host se configurado), MySQL 3306, Firebird 3050, Kafka 9092, Mailpit 8025/1025.

Logs (Logback)

Rotação: diária + 50MB por arquivo, gzip, maxHistory=7, totalSizeCap=2GB.

Arquivos: app.log e access.log em ${LOG_PATH:-/opt/app/logs}.

Assíncrono: ativo em docker e prod (lossless: neverBlock=false, filas grandes).

Ajuste em src/main/resources/logback-spring.xml.
MDC cid já suportado. Popule em filtros quando necessário.

Swagger / OpenAPI

UI: http://localhost:8080/swagger-ui/index.html

Docs: http://localhost:8080/v3/api-docs

Ative/desative por perfil via springdoc.* se desejar.
Endpoints de saúde: GET /actuator/health, GET /actuator/info.

Estrutura de pastas
src/main/java/br/com/redemaisfarma/
  adapters/
    inbound/   # controllers, schedulers, filters
    outbound/  # persistence, legacy, clients externos
      persistence/
        mysql/     # Entities/Repos MySQL
        repository # TODO: ajustar
      legacy/     # Entities/Repos Firebird
  application/
    service/      # casos de uso
    dto/          # request/response
    mapper/       # MapStruct
  domain/
    model/        # entidades de domínio
    port/         # portas
  infra/
    logging/      # filtros/log
    config/       # @Configuration (datasource, security, etc.)
resources/
  db/migration/   # Flyway
  templates/      # Thymeleaf (admin)
  static/         # assets


TODO: Ajuste esses caminhos para os seus pacotes reais, se diferirem.

Persistência (múltiplos datasources)

MySQL é o PRIMÁRIO (@Primary em DataSource, EntityManagerFactory e TransactionManager do MySQL).

Firebird é secundário, usado via pacote específico e/ou @Transactional(transactionManager="firebirdTransactionManager").

Dicas:

Repositórios por pacote com @EnableJpaRepositories(entityManagerFactoryRef=..., transactionManagerRef=...).

Em métodos que acessam o legado:

@Transactional(transactionManager = "firebirdTransactionManager", readOnly = true)


Em métodos padrão (MySQL): @Transactional simples usa o primário.

Mensageria (Kafka)

Configurar em application-*.yml:

spring:
  kafka:
    bootstrap-servers: ${KAFKA_BROKER:localhost:9092}
    consumer:
      group-id: redemaisfarma
      auto-offset-reset: latest
    producer:
      acks: all


Tópicos: TODO: listar tópicos usados (ex.: produto-sync, estoque-event, etc.).

Testes

Unitários: JUnit5 + Mockito.

Integração: Testcontainers (MySQL e Kafka sobem isolados em CI/DEV).

Padrões:

*Test.java (unit)

*IT.java (integração)

Cobertura: JaCoCo habilitado.

Comandos:

mvn test
mvn verify

Troubleshooting

API sobe mas endpoints 404

Verifique @RequestMapping base e perfil ativo.

Veja DispatcherServlet e HandlerMappings no log (suba org.springframework.web=DEBUG em dev).

Erro de transação com múltiplos TMs

Falta @Primary no MySQL ou @Transactional(transactionManager=...) no serviço legado.

Flyway falha

Confira credenciais do MySQL; rode flyway:repair.

Veja se há scripts com nomes fora do padrão.

Kafka desconectando

Cheque bootstrap-servers; confirme que o broker está acessível do container/app.

Teste com kafkacat/kafka-console-producer.sh.

Logs não rodam para arquivo

Permissões em ${LOG_PATH}.

Em docker/prod, appenders assíncronos devem estar ativos.

Firebird não conecta

Caminho do .fdb e permissões de volume.

Versão do Jaybird e encoding=UTF8.

Comandos úteis:

# Filtrar SQL
docker logs redemaisfarma-api 2>&1 | grep -E "org.hibernate.SQL|jdbc.bind"

# Health
curl -s http://localhost:8080/actuator/health | jq

# Ver variáveis no container
docker exec -it redemaisfarma-api env | sort

Scripts úteis
# Subir stack local
docker compose up -d

# Reset MySQL (DEV!)
docker exec -it redemaisfarma-mysql mysql -uroot -p$MYSQL_PASSWORD -e "DROP DATABASE IF EXISTS redemaisfarma; CREATE DATABASE redemaisfarma;"

# Flyway repair + migrate
mvn org.flywaydb:flyway-maven-plugin:repair -Dflyway.user=$MYSQL_USER -Dflyway.password=$MYSQL_PASSWORD -Dflyway.url=jdbc:mysql://localhost:3306/redemaisfarma
mvn org.flywaydb:flyway-maven-plugin:migrate

Convenções de código

MapStruct para mapeamentos DTO ↔ entidades.

Pacotes por contexto (domain/application/adapters/infra).

Controllers finos; regras em application.service.

Validações com Bean Validation (Jakarta).

Respostas de erro padronizadas (Problem Details) — RestExceptionTranslator.

Segurança

JWT (TODO: confirmar/ajustar detalhes de geração/validação).

CORS por perfil (abrir em dev, restrito em prod).

Headers: X-Request-Id / MDC cid para rastreabilidade.

Roadmap curto

 Finalizar DTOs e mapeamentos pendentes do legado.

 Cobrir fluxo de sincronização de produtos com testes de integração.

 Painel admin: métricas no AdminMetricsService + gráficos.

 Observabilidade (Micrometer + Prometheus/Grafana) — opcional.

 Pipelines CI (test, build, docker push, deploy).