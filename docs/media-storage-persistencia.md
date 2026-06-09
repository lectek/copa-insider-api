# Persistencia de Imagens em Producao

Data: `2026-03-20`

## Objetivo
Garantir que avatar de usuario, fotos de produto e imagens geradas por IA nao sejam perdidos em restart ou deploy da API.

## Causa raiz encontrada
- O profile `prod` ainda apontava `APP_MEDIA_DIR` e `APP_MEDIA_USER_DIR` para `${java.io.tmpdir}`.
- Em Railway, esse filesystem e efemero.
- Resultado: o banco continuava com a URL, mas o arquivo podia sumir depois de reinicio ou nova imagem do container.

## O que foi fechado no codigo
- `ImageStorageService` agora suporta:
  - `local`
  - `s3`
- O mesmo servico atende:
  - upload de avatar
  - upload de produto
  - galeria do produto
  - persistencia de imagem gerada por IA
  - delete por URL para rollback
- Em modo `s3`, a API exige base publica absoluta para nao salvar URL quebrada.

## Arquivos principais
- `src/main/java/br/com/redemaisfarma/application/core/media/ImageStorageService.java`
- `src/main/java/br/com/redemaisfarma/application/core/media/ImageStorageProperties.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`
- `src/test/java/br/com/redemaisfarma/application/core/media/ImageStorageServiceTest.java`

## Variaveis de ambiente
### Minimo obrigatorio para persistencia real
- `APP_MEDIA_PROVIDER=s3`
- `APP_MEDIA_S3_BUCKET`
- `APP_MEDIA_PUBLIC_BASE`
- `APP_MEDIA_USER_PUBLIC_BASE`

### Normalmente necessario em R2/S3 compativel
- `APP_MEDIA_S3_ENDPOINT`
- `APP_MEDIA_S3_ACCESS_KEY`
- `APP_MEDIA_S3_SECRET_KEY`
- `APP_MEDIA_S3_REGION`
- `APP_MEDIA_S3_PATH_STYLE_ACCESS`
- `APP_MEDIA_S3_PRODUCT_PREFIX`
- `APP_MEDIA_S3_USER_PREFIX`

## Exemplo de configuracao
```env
APP_MEDIA_PROVIDER=s3
APP_MEDIA_S3_BUCKET=redemais-media
APP_MEDIA_S3_REGION=us-east-1
APP_MEDIA_S3_ENDPOINT=https://<account>.r2.cloudflarestorage.com
APP_MEDIA_S3_ACCESS_KEY=<access-key>
APP_MEDIA_S3_SECRET_KEY=<secret-key>
APP_MEDIA_S3_PATH_STYLE_ACCESS=true
APP_MEDIA_S3_PRODUCT_PREFIX=products
APP_MEDIA_S3_USER_PREFIX=users
APP_MEDIA_PUBLIC_BASE=https://cdn.redemaisfarma.com/products
APP_MEDIA_USER_PUBLIC_BASE=https://cdn.redemaisfarma.com/users
```

## Regras operacionais
- `local` continua valido para desenvolvimento.
- Em producao, `local` so e aceitavel se existir volume persistente de verdade.
- Sem bucket/volume persistente, qualquer deploy continua em risco.
- URLs gravadas no banco precisam ser finais e publicas.

## Validacao recomendada apos configurar o ambiente
1. Subir a API com as variaveis de storage persistente.
2. Fazer upload de avatar.
3. Fazer upload de imagem de produto.
4. Gerar uma imagem via IA e salvar no produto.
5. Reiniciar a aplicacao.
6. Confirmar que os tres arquivos continuam abrindo.
7. Conferir no banco que as URLs gravadas sao absolutas.

## Pendencia fora do codigo
- Configurar o ambiente produtivo no Railway com bucket ou volume persistente antes do proximo deploy desta entrega.
