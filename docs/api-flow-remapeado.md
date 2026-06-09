# Remapeamento de Fluxo da API

## Objetivo
Este documento consolida o fluxo Front -> API -> Banco apos os ajustes de seguranca e persistencia.

## Ajustes aplicados
1. `POST /api/public/produtos/{produtoId}/stock/subscribe` agora e publico no `SecurityApiConfig`.
2. `DevProvisioningService.verifyByToken(...)` passou a usar `RoleRepository` para anexar role gerenciada do banco (sem criar role transiente em memoria).

## Fluxo 1: Catalogo publico e aviso de volta ao estoque
1. Frontend lista produtos via:
   - `GET /api/public/produtos/destaques`
   - `GET /api/public/produtos`
2. Usuario solicita aviso de reposicao:
   - `POST /api/public/produtos/{produtoId}/stock/subscribe`
3. API valida payload (`email`, `nome`) e grava em `product_stock_subscription`.
4. API retorna `201 Created` com `Location` da inscricao.

### Contrato esperado
- Sem token JWT para subscribe.
- `201` quando cria inscricao.
- `400/422` para payload invalido.
- `404` para produto inexistente (dependendo da validacao do service).

## Fluxo 2: Provisionamento DEV por token
1. Frontend/operador dispara:
   - `POST /api/dev/provision/start?email={email}`
2. API cria OTP (`otp_code`) e trilha de envio (`email_delivery`).
3. Usuario confirma token:
   - `POST /api/dev/provision/verify?token={token}&cpfIfNew={cpf}&nomeIfNew={nome}`
4. API carrega usuario por email/cpf:
   - cria usuario se nao existir;
   - define senha (informada ou gerada).
5. API resolve role de DEV na tabela `roles` com fallback:
   - `ROLE_DEVELOPER`, `DEVELOPER`, `ROLE_DEV`, `DEV`
6. API adiciona role gerenciada ao usuario e persiste em `usuario` + `usuario_roles`.
7. API retorna `200` com status `dev-enabled`.

### Contrato esperado
- `start`: publico em ambiente `dev/local/docker`.
- `verify`: publico.
- `200` no fluxo valido.
- Erro explicito se nenhuma role de DEV existir em `roles`.

## Fluxo 3: Adicao de produto no admin (sem auto-publicacao)
1. Backoffice cria produto via pagina:
   - `POST /admin/produtos` (form-data com `imagemFile` obrigatoria)
2. Controller persiste o registro em `ProdutoStatus.IMPORTADO` e redireciona para:
   - `GET /admin/produtos/{id}/editar`
3. Front de edicao salva ajustes gerais:
   - `PUT /api/admin/produtos/{id}`
4. Front pode atualizar imagem:
   - `POST /api/admin/produtos/{id}/imagem`
5. Publicacao passou a ser explicita em duas etapas:
   - `POST /api/admin/produtos/{id}/validar?validador={usuario}`
   - `POST /api/admin/produtos/{id}/publicar?validador={usuario}`
6. API grava transicoes na tabela `produto`:
   - `status`: `IMPORTADO` -> `VALIDADO` -> `PUBLICADO`
   - `validador` atualizado em validar/publicar
   - `publicadoEm` preenchido ao publicar

### Contrato esperado
- Criacao no admin nunca publica automaticamente.
- Produto so aparece na listagem publica quando estiver `PUBLICADO`.
- Falhas de upload na criacao fazem rollback do cadastro recem-criado.

## Mapa rapido Front -> API -> Banco
1. Home/Carrossel -> `GET /api/public/produtos/destaques` -> `produto`.
2. Catalogo -> `GET /api/public/produtos` -> `produto`.
3. Avise-me -> `POST /api/public/produtos/{id}/stock/subscribe` -> `product_stock_subscription`.
4. Provision start -> `POST /api/dev/provision/start` -> `otp_code`, `email_delivery`.
5. Provision verify -> `POST /api/dev/provision/verify` -> `usuario`, `usuario_roles`.
6. Admin novo produto -> `POST /admin/produtos` -> `produto(status=IMPORTADO)`.
7. Admin validar/publicar -> `/api/admin/produtos/{id}/validar|publicar` -> `produto(status, validador, publicadoEm)`.
