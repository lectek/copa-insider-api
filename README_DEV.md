# Guia Rápido de Desenvolvimento

## Visão geral
- Stack: Java 21, Spring Boot 3.3, Maven Wrapper, MapStruct, Lombok.
- Módulo principal: `boot-app` (API e templates admin). Hexagonal: `domain`, `application`, `adapters` (web/JPA), `config`.
- Perfis: `dev` (MySQL local), `test` (Testcontainers MySQL), `firebird` (legado). Configs em `src/main/resources/application*.yml`.

## Estado atual da API
- Catálogo admin funcional: páginas `/admin/produtos`, `/admin/produtos/novo`, `/admin/produtos/{id}/editar` consumindo `/api/admin/produtos` (CRUD, validação/publicação, upload de imagem, ações IA).
- Importação legado ativa via perfil `firebird` (conector Firebird Jaybird).
- Observabilidade e qualidade ativas: Actuator, Jacoco, SpotBugs, Checkstyle.
- Testes de controlador (ProdutoAdminRestControllerTest) e do job de alerta (EstoqueBaixoNotificacaoJobTest) cobrem /api/admin/produtos e o alerta de estoque; monitore as flags app.estoque.alerta.*, a fila email_delivery/EmailDeliveryWorker e os endpoints /actuator/health e /actuator/metrics para validar dependencias externas (storage/S3, Firebird).
- Estoque: reservas no checkout/pagamento via `EstoqueService`, baixa em venda rápida, job de alerta de estoque baixo (log/e-mail) configurável em `app.estoque.alerta.*`.

## Fluxos de produto (admin)
- Páginas: `/admin/produtos` (lista), `/admin/produtos/novo` (criar), `/admin/produtos/{id}/editar` (editar). Templates em `templates/pages/admin/produtos/`; JS em `static/js/pages/admin/produto-editar.js`.
- APIs admin: `ProdutoAdminRestController` em `/api/admin/produtos` (listar, obter, criar, atualizar, excluir, validar/publicar, upload de imagem).
- Uso em tela: criação/edição envia `tenantId` padrão (`rede-mais-farma`), gera SKU se não informado e redireciona para edição após criar.

## Comandos úteis
- Build + testes completos: `./mvnw clean verify`
- Somente testes (perfil test): `./mvnw test -Dspring.profiles.active=test`
- Pular integrações (sem Docker): `./mvnw verify -DskipITs`
- Rodar local (dev): `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- MySQL dev via Docker: `docker-compose -f docker-compose.dev.yml up -d mysql`

## Perfis e configuração
- `dev` (padrão): MySQL local `localhost:3306`, Swagger ligado, Mailpit opcional. Ver `application-dev.yml`.
- `docker`: aponta para os serviços do `docker-compose.dev.yml`.
- `test`: Testcontainers (MySQL) em `application-test.yml`.
- `legacy`: conector Jaybird para Firebird; ajuste `FIREBIRD_HOST`, `FIREBIRD_DB`, `FIREBIRD_USER/PASSWORD`.
- Alerta de estoque: habilite com `app.estoque.alerta.enabled=true`; defaults `limite=10`, `percentual=10`, `cron=0 */30 * * * *`, `cooldown-minutos=60`, `email=` opcional.

## Convenções de código
- Injeção por construtor; evitar field injection.
- Indentação 4 espaços, linhas ~120 colunas. Classes: PascalCase; fields/métodos: camelCase.
- Controllers terminam em `Controller`, DTOs em `RequestDTO`/`ResponseDTO`, mappers em `Mapper`.
- Segredos fora do repo; usar perfis/YAML e variáveis de ambiente.

## Testes
- JUnit 5, Mockito, Testcontainers (MySQL). Unitários em `src/test/java`, integração espelha o main.
- JaCoCo roda em `clean verify`; cobrir sucesso e falha.
- Integrações atuais: Auth/Produto; faltam cenários end-to-end checkout/pagamento/estoque.

## Checklist de PR
- Rodar `./mvnw clean verify` (ou `./mvnw test -DskipITs` se sem Docker).
- Descrever motivação, abordagem, endpoints alterados, novos env vars/portas/perfis.
- Atualizar README/Swagger quando mudar contrato de API.
- Seguir também:
  - `CONTRIBUTING.md`
  - `docs/playbook-desenvolvimento-seguro-estavel.md`
  - `docs/getshitdone-board-template.md`
  - `docs/monorepo-estrutura-alvo.md`
  - `docs/estrategia-monorepo-railway-multitenant-e-separado.md`
  - `docs/regras-negocio-cpap-e-invariantes.md`

## Notas rápidas sobre produtos
- Repositórios: `ProdutoJpaRepository` e `ProdutoRepository` (busca/paginação/categorias).
- Entidade: `ProdutoEntity` (status, estoque, preços, timestamps).
- Mapper: `ProdutoMapper` converte domain↔entity/DTO.
- Imagem IA: ações em lista/edição chamam `/api/admin/imagens/{id}/queue|regenerate`.

## Deploy atual GitHub + Railway
- Em `2026-03-26`, o repositório foi preparado para subida ao GitHub e deploy na Railway.
- Push concluído em `origin/main` com o commit `837540a` após saneamento de `.gitignore` e remoção de artefatos locais do fluxo de deploy.
- O deploy Railway bem-sucedido do serviço `redemaisfarma-api` foi o `58e1f83d-202c-4807-9c42-f10419c19d23`, no projeto `intuitive-fulfillment`.
- O domínio Railway respondeu `200`: `https://redemaisfarma-api-production-78a1.up.railway.app`.
- O domínio customizado configurado na Railway é `https://redemaisfarma.far.br`, mas na validação desta data o DNS ainda não resolvia externamente.

## Incidente corrigido no deploy
- O build na Railway falhava no Docker com `COPY [Estoque Fisico.csv, /app/Estoque Fisico.csv]` porque o arquivo não existia no contexto enviado.
- A correção aplicada foi remover essa cópia obrigatória do `Dockerfile`, mantendo a aplicação dependente apenas da configuração de path já suportada pelo serviço `EstoqueFisicoCsvService`.
- Commit da correção: `837540a` (`fix: remove missing csv from docker image build`).

## Estado atual de produção
- Perfil ativo na Railway: `prod`.
- Datasource MySQL subiu via `RAILWAY_MYSQL_URL`.
- Flyway executou com sucesso, com aviso de compatibilidade sobre MySQL `9.4` ser mais novo que a versão testada pela lib atual.
- O serviço iniciou normalmente na porta `8080`.
- O storage de imagens continua local/temporário em `/tmp/redemaisfarma/...`; reinícios/deploys podem apagar imagens se `APP_MEDIA_PROVIDER` continuar `local`.

## Ajustes operacionais pendentes
- Corrigir o DNS público de `redemaisfarma.far.br`.
- Após o DNS resolver corretamente, trocar `APP_WEB_BASE_URL` para `https://redemaisfarma.far.br`.
- Migrar mídia para storage persistente, preferencialmente S3.
- Revisar variáveis de produção e remover entradas quebradas/legadas. Nesta rodada já foram removidas duas variáveis inválidas com espaço no início do nome.

## Blocos de Desenvolvimento (módulos)
- **Cliente (Self-service)**: `/api/cliente/me` (GET/PUT perfil), `/api/cliente/me/enderecos` (CRUD), `/api/cliente/me/pedidos` (paginado). Usa CurrentClienteProvider para extrair cliente autenticado do token.
- **Legado/Importação**: conectores Firebird (perfil `firebird`), serviços de sincronização; monitorar logs em `adapters.outbound.legacy`.
- **Qualidade/Observabilidade**: Actuator, Jacoco, SpotBugs, Checkstyle já integrados; manter `clean verify` no PR.


## Próximos passos para conclusão web
1. Confirmar com o time de produto os critérios de aceitação restantes: checkout completo, pagamentos, notificações e fluxos de erro esperados.
2. Completar o inventário de telas/classes front-end (carrinho, checkout, pagamento, confirmação) e identificar gaps em componentes reutilizáveis.
3. Mapear APIs e backlog necessários para cada tela pendente, incluindo contratos de frete, promoções, cupom e estoque.
4. Priorizar integrações críticas de backend com base no valor percebido pelo comprador e nas dependências técnicas.
5. Checar o estado atual dos mocks e testes existentes para os endpoints de checkout e criar versões atualizadas, se necessário.
6. Gerar ou atualizar componentes UI responsivos para carrinho/checkout com estados de loading, sucesso e erro.
7. Garantir que o catálogo esteja atualizado antes do carrinho: cache, atualização assíncrona ou fallback.
8. Conectar o front ao backend oficial para frete/pagamento, cuidando da autenticação e do token CSRF.
9. Sincronizar estoque/preço ao abrir o carrinho e antes do pagamento, mostrando avisos de indisponibilidade.
10. Implementar logs e métricas para o fluxo de checkout (eventos, tempo médio, falhas) visando monitoramento.
11. Validar o fluxo com testes automatizados (unitários e integrados) e documentar os cenários cobertos.
12. Realizar smoke tests manuais no ambiente dev com versões completas de banco e integrações (MySQL/Testcontainers).
13. Ajustar performance (lazy load, debounce, minificação) e garantir acessibilidade básica (contraste, navegação por teclado).
14. Preparar release notes e atualizar README/OpenAPI com os novos fluxos e endpoints expostos.
15. Coordenar com QA e suporte o plano de testes de regressão, registrando bugs e fechando bloqueios.
16. Fazer deploy em homologação, executar testes finais de ponta a ponta e coletar logs de erro/sucesso.
17. Receber feedback de QA/PO, corrigir falhas críticas e reorganizar prioridades residuais.
18. Atualizar scripts de implantação e configuração (env vars, secrets) caso novos serviços sejam utilizados.
19. Acompanhar métricas pós-deploy, monitorar integrações externas e validar rollback/alertas.
20. Comunicar o time e stakeholders da entrega com checklist, testes executados e próximos passos.
21. Reavaliar o backlog para adicionar ajustes finos de UX/performance detectados durante os testes.
22. Garantir documentação interna atualizada (diagramas, contratos, endpoints) antes do fechamento.
23. Treinar suporte/operations sobre o novo fluxo e preparar runbooks para incidentes.
24. Planejar a iteração seguinte com foco em novos requisitos ou otimizações após estabilizar a entrega.

## Prioridade extrema
- Foto de perfil em "Minha Conta" e "Meus Dados" (upload + exibição).


## Atualização 2026-03-20 - persistência de imagens
- Causa raiz confirmada em produção: `application-prod.yml` ainda usa `${java.io.tmpdir}` para `APP_MEDIA_DIR` e `APP_MEDIA_USER_DIR`.
- Isso significa que restart/deploy pode apagar avatar de usuário, fotos de produto e imagens geradas por IA quando o provider continua `local`.
- O serviço `ImageStorageService` agora suporta dois modos:
  - `local`: desenvolvimento e smoke test
  - `s3`: persistência real para Railway/R2/S3 compatível
- O fluxo centralizado cobre:
  - upload de avatar
  - upload de imagem principal de produto
  - upload de galeria
  - persistência de imagem gerada por IA
  - delete/rollback por URL
- Regra nova para `s3`:
  - `APP_MEDIA_PUBLIC_BASE` e `APP_MEDIA_USER_PUBLIC_BASE` precisam ser URLs absolutas
  - sem isso, a API falha de forma explícita em vez de gravar URL quebrada

### Variáveis obrigatórias para persistência real
- `APP_MEDIA_PROVIDER=s3`
- `APP_MEDIA_S3_BUCKET`
- `APP_MEDIA_PUBLIC_BASE`
- `APP_MEDIA_USER_PUBLIC_BASE`

### Variáveis usuais para R2/S3 compatível
- `APP_MEDIA_S3_ENDPOINT`
- `APP_MEDIA_S3_ACCESS_KEY`
- `APP_MEDIA_S3_SECRET_KEY`
- `APP_MEDIA_S3_REGION`
- `APP_MEDIA_S3_PATH_STYLE_ACCESS`
- `APP_MEDIA_S3_PRODUCT_PREFIX`
- `APP_MEDIA_S3_USER_PREFIX`

### Estado da entrega
- Código preparado para storage persistente.
- Testes locais adicionados em `ImageStorageServiceTest`.
- Profile `docker` agora usa caminhos explícitos em `/opt/app/media/*`.
- `docker-compose.yml` e `docker-compose.dev.yml` agora montam o volume nomeado `app_media_dev` para preservar fotos e avatares entre reinícios do container local.
- Ainda depende de configurar as variáveis do ambiente produtivo antes do próximo deploy se a meta for impedir perda de imagem em Railway.
- Documento de handoff: `docs/media-storage-persistencia.md`.




## Observabilidade das campanhas
- A fila de campanhas gera `email_campaign.worker.processed`, `sent`, `retry` e `failed` e pode ser monitorada via `/actuator/metrics`; configure alertas quando `failed > 0` ou quando os processados pararem.
- O formulário admin agora aceita filtros adicionais (categoria comprada, recência em dias, ticket médio) antes de enfileirar, além de preview/cancelamento/pause para ajuste antes do envio.
