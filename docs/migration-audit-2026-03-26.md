# Auditoria de Migrations

Data: 2026-03-26

## Situação

A trilha de migrations está funcional para o banco atual, mas não está limpa o suficiente para ser tratada como fonte única de verdade. Há sinais de drift entre ambientes e de evolução paralela do schema.

## Principais conflitos

### 1. Tabelas recriadas com contratos concorrentes

- `otp_code`
  - `AUTH/V24__otp_code_create.sql`
  - `AUTH/V20250913__create_otp_code.sql`
- `email_delivery`
  - `AUTH/V26__create_email_delivery_outbox.sql`
  - `PROD/V20260111_01__create_email_delivery_table.sql`
- `sync_checkpoint`
  - `PROD/V23072023_20__create_sync_checkpoint.sql`
  - `PROD/V20250918__sync_checkpoint.sql`

### 2. Normalização tardia e repetida de `produto`

- `PROD/V15__ajustar_produto_para_jpa.sql`
- `PROD/V22__align_produto_schema.sql`
- `PROD/V23072023_25__produto_normalize_unico_index.sql`

Esses arquivos indicam ausência de um schema canônico claro para `produto`.

### 3. Configuração duplicada

- `CFG/V20250829_01__app_settings.sql` cria `app_setting`
- `CFG/V20250915_01__create_app_settings.sql` cria `app_settings`

## Riscos

- Ambientes novos podem convergir para schemas diferentes dependendo do ponto de entrada.
- A manutenção fica perigosa porque o histórico mistura bootstrap, correção, compatibilização e redefinição estrutural.
- O custo de entender o banco cresce a cada mudança de produto, pedido e integrações.

## Correção imediata já aplicada

As migrations novas criadas na reestruturação do banco foram renumeradas para evitar conflito de versão global do Flyway:

- `PED/V20260325_11__item_pedido_produto_snapshot.sql`
- `PROD/V20260325_12__produto_categoria_fk.sql`

Também foram adicionadas migrations de consolidação estrutural:

- `V20260326_01__consolidate_sync_checkpoint.sql`
- `V20260326_02__consolidate_otp_code.sql`
- `V20260326_03__consolidate_email_delivery.sql`
- `V20260326_04__consolidate_app_settings.sql`

## Próximas etapas recomendadas

1. Criar uma convenção única de versionamento Flyway e parar de misturar estilos.
2. Separar migrations de bootstrap histórico das migrations canônicas ativas.
3. Aplicar e validar as migrations de consolidação no banco Railway antes de novas mudanças estruturais.
