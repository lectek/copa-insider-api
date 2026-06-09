# Auditoria de Deploy Railway - 2026-03-26

## Resumo
- Repositório enviado para GitHub em `origin/main`.
- Commit base de preparação: `224a88835a44bf05c8d6fac1fb449f17866b76f3`.
- Commit de correção do build Docker: `837540a`.
- Projeto Railway vinculado: `intuitive-fulfillment`.
- Serviço Railway validado: `redemaisfarma-api`.
- Deploy final com sucesso: `58e1f83d-202c-4807-9c42-f10419c19d23`.

## Problema encontrado
- O deploy inicial na Railway falhou no build Docker.
- Erro identificado:

```text
failed to solve: failed to compute cache key: "/Estoque Fisico.csv": not found
```

- Causa:
  - o `Dockerfile` fazia `COPY ["Estoque Fisico.csv", "/app/Estoque Fisico.csv"]`
  - o arquivo `Estoque Fisico.csv` nao existia no contexto enviado para a Railway

## Correcao aplicada
- Remocao da linha de `COPY` do arquivo inexistente no `Dockerfile`.
- A aplicacao permaneceu compativel porque o servico `EstoqueFisicoCsvService` ja suporta configuracao por path e trata ausencia do arquivo sem impedir a subida da aplicacao.

## Validacao do deploy
- Build Railway concluido com `BUILD SUCCESS`.
- Runtime inicializado com sucesso.
- Evidencias dos logs:
  - perfil ativo: `prod`
  - datasource resolvido via `RAILWAY_MYSQL_URL`
  - Tomcat iniciado na porta `8080`
  - aplicacao iniciada com sucesso

## Dominios
- Dominio Railway funcional:
  - `https://redemaisfarma-api-production-78a1.up.railway.app`
- Dominio customizado configurado na Railway:
  - `https://redemaisfarma.far.br`
- Estado na validacao desta data:
  - o dominio Railway respondeu `200`
  - o dominio customizado ainda nao resolvia externamente no teste executado daqui

## Variaveis e saneamento
- Foram removidas duas variaveis invalidas na Railway com espaco no inicio da chave:
  - ` ESTOQUE_FISICO_IMPORT_RUN`
  - ` email.enabled`
- `APP_WEB_BASE_URL` ainda aponta para o dominio Railway e deve ser trocado para o dominio customizado depois da correcao do DNS.

## Riscos remanescentes
- Storage de imagens continua em `/tmp/redemaisfarma/...` com provider local.
- Reinicios e novos deploys podem apagar imagens se `APP_MEDIA_PROVIDER` continuar `local`.
- Flyway emitiu aviso de compatibilidade porque o banco reportado e MySQL `9.4`, mais novo que a faixa explicitamente testada pela versao atual da biblioteca.

## Proximos passos recomendados
1. Corrigir o DNS de `redemaisfarma.far.br`.
2. Atualizar `APP_WEB_BASE_URL` para `https://redemaisfarma.far.br`.
3. Migrar imagens para storage persistente, preferencialmente S3.
4. Revisar as variaveis restantes de producao para remover duplicidades e padronizar mail/configuracoes.
