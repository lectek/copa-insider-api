# Arquitetura de Sincronizacao e Bancos (Multiempresa)

## 1) Objetivo

Estruturar o sistema para:

- suportar varias empresas com isolamento seguro;
- manter cadastro/estoque sincronizados sem duplicidade;
- permitir importacao por CSV/PDF com confirmacao humana;
- equilibrar **integridade** e **disponibilidade** sem perder rastreabilidade.

---

## 2) Modelo de tenancy

### Opcao recomendada agora: `tenant_id` no mesmo banco

Vantagens:

- implementacao mais rapida;
- menor custo operacional;
- facilita relatorios cross-tenant internos.

Regra obrigatoria:

- toda tabela de negocio deve ter `tenant_id`;
- todo indice/unique deve incluir `tenant_id`.

Evolucao futura:

- migrar tenant grande para banco dedicado sem quebrar contrato de aplicacao.

---

## 3) Tabelas base (DDL de referencia)

```sql
create table tenant (
  id bigint primary key auto_increment,
  codigo varchar(64) not null unique,
  nome varchar(255) not null,
  ativo boolean not null default true,
  created_at datetime not null,
  updated_at datetime not null
);

create table produto (
  id bigint primary key auto_increment,
  tenant_id bigint not null,
  legacy_id bigint null,
  codigo_barras varchar(64) null,
  nome varchar(255) not null,
  descricao varchar(1000) null,
  categoria varchar(255) not null,
  fabricante varchar(128) null,
  estoque int not null default 0,
  preco_venda decimal(15,2) not null default 0,
  preco_custo decimal(15,2) not null default 0,
  status varchar(32) not null,
  metodo_leitura_codigo_barras varchar(32) not null,
  version bigint not null default 0,
  created_at datetime not null,
  updated_at datetime not null,
  constraint fk_produto_tenant foreign key (tenant_id) references tenant(id),
  unique key uk_produto_tenant_codigo_barras (tenant_id, codigo_barras),
  key idx_produto_tenant_legacy_id (tenant_id, legacy_id),
  key idx_produto_tenant_status (tenant_id, status)
);
```

### Outbox para sincronizacao confiavel

```sql
create table outbox_event (
  id bigint primary key auto_increment,
  tenant_id bigint not null,
  aggregate_type varchar(64) not null,
  aggregate_id varchar(128) not null,
  event_type varchar(64) not null,
  payload_json json not null,
  dedupe_key varchar(128) not null,
  status varchar(16) not null default 'PENDING',
  attempts int not null default 0,
  next_attempt_at datetime null,
  created_at datetime not null,
  processed_at datetime null,
  unique key uk_outbox_dedupe (tenant_id, dedupe_key),
  key idx_outbox_pending (status, next_attempt_at)
);
```

### Checkpoint e auditoria

```sql
create table sync_checkpoint (
  id bigint primary key auto_increment,
  tenant_id bigint not null,
  origem varchar(64) not null,
  cursor_value varchar(255) not null,
  updated_at datetime not null,
  unique key uk_checkpoint_tenant_origem (tenant_id, origem)
);

create table import_history (
  id bigint primary key auto_increment,
  tenant_id bigint not null,
  origem varchar(32) not null, -- CSV_ESTOQUE, PDF_CATALOGO, LEGADO
  arquivo_hash varchar(128) null,
  arquivo_nome varchar(255) null,
  lidos int not null default 0,
  inseridos int not null default 0,
  atualizados int not null default 0,
  ignorados int not null default 0,
  erros int not null default 0,
  status varchar(16) not null, -- PREVIEW, CONFIRMED, FAILED
  executado_por varchar(128) null,
  created_at datetime not null
);
```

---

## 4) Regras de conflito (fonte de verdade)

Definir prioridade por campo:

1. `nome/descricao/fabricante/categoria`: `MANUAL_ADMIN` > `PDF_CATALOGO` > `LEGADO`
2. `estoque`: `MOVIMENTO_ESTOQUE` + `IMPORT_CSV/PDF_CONFIRMADO` (somatorio, nao overwrite cego)
3. `preco_venda`: `MANUAL_ADMIN` > `PDF_CATALOGO` > `LEGADO`
4. `codigo_barras`: imutavel quando validado; troca apenas por fluxo admin controlado.

Regra tecnica:

- toda escrita relevante gera `outbox_event`;
- consumidor aplica idempotencia via `dedupe_key`;
- sem `dedupe_key`, nao processa.

---

## 5) Fluxo de importacao PDF/CSV

1. Upload arquivo.
2. Parsing para staging (preview, sem gravar estoque final).
3. Usuario confere e confirma.
4. Aplicacao grava lote transacional:
   - produto novo: cria;
   - produto existente: soma estoque.
5. Registra `import_history` + gera eventos outbox.

Bloqueios:

- impedir confirmacao duplicada do mesmo arquivo (`arquivo_hash` por tenant);
- impedir corrida de duas importacoes simultaneas por tenant (lock logico).

---

## 6) Integridade x disponibilidade

### Operacao critica (pedido, pagamento, baixa de estoque)

- priorizar **integridade** (transacao forte no banco primario).

### Sincronizacao e catalogo

- priorizar **disponibilidade** com reconciliacao async:
  - falha no consumidor nao bloqueia operacao principal;
  - retry com backoff + DLQ/log de erro.

---

## 7) Roadmap de implementacao

## Fase P0 (agora)

- padronizar `tenant_id` nas queries e indices principais;
- criar `outbox_event`, `sync_checkpoint`, `import_history`;
- fechar fluxo preview/confirmacao PDF e CSV.

## Fase P1

- worker de outbox com retry/idempotencia;
- dashboard admin de historico de importacao e erros de sync;
- alertas (falha de sync, backlog alto, DLQ).

## Fase P2

- read model separado para consultas pesadas;
- opcao de tenant em banco dedicado;
- reconciliador automatico noturno por tenant.

---

## 8) Prompts de execucao (para usar com agente)

### Prompt 1: migracoes de banco

> Crie migrations Flyway para adicionar `tenant_id` nas tabelas de negocio prioritarias, incluindo indices compostos e constraints unicas por tenant, sem quebrar dados existentes.

### Prompt 2: camada de repositorio

> Refatore repositorios e servicos para exigir `tenant_id` em todas as operacoes de leitura/escrita de produto, com testes cobrindo isolamento entre duas empresas.

### Prompt 3: outbox

> Implemente pattern outbox transacional com produtor e worker consumidor idempotente usando `dedupe_key`, retries com backoff e auditoria de tentativas.

### Prompt 4: importacao confirmada

> Implemente staging de importacao (preview) para CSV/PDF e confirmacao final que soma estoque em produtos existentes e cria novos produtos quando nao encontrados.

---

## 9) Criterios de pronto

- nenhum `produto` de um tenant visivel para outro tenant;
- importacao confirmada nao duplica processamento do mesmo arquivo;
- sincronizacao reprocessavel sem efeitos colaterais (idempotente);
- logs e historico suficientes para auditoria completa.
