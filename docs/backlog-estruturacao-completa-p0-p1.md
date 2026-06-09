# Backlog Inicial P0/P1 - Estruturacao Completa do Sistema

## Objetivo
Executar a estruturacao completa da plataforma com foco em: repositorio unico, multiempresa, sincronizacao confiavel, produto com variacoes, seguranca e estabilidade.

## Resultado esperado
- Arquitetura unificada e governada.
- Regras de negocio formalizadas.
- Fluxos criticos protegidos por testes e monitoramento.
- Evolucao de produto (variacoes) pronta para uso no admin.

## Sequencia macro (30/60/90 dias)
- **30 dias (Fundacao):** governanca, arquitetura, contratos e seguranca base.
- **60 dias (Core):** modelo de dados multiempresa + produto com variacoes + APIs.
- **90 dias (Operacao):** sincronizacao avancada, observabilidade, hardening e release estavel.

## P0 - Critico (executar primeiro)

### P0.1 - Governanca unica de execucao
- Escopo:
1. Adotar o playbook oficial.
2. Padronizar card template no GetShitDone (DoR/DoD/aceite/risco).
3. Criar board com epicos: Arquitetura, Produto, Checkout, Entregas, Marketing, Seguranca, Observabilidade.
- Dependencias: nenhuma.
- Criterios de aceite:
1. Board criado com prioridades `P0/P1/P2`.
2. Todo item novo segue template unico.

### P0.2 - Repositorio unico (monorepo)
- Escopo:
1. Definir estrutura de pastas (`backend`, `admin-web`, `mobile`, `shared-contracts`).
2. Configurar CI minimo (build + testes + checks estaticos).
3. Definir convencao de branch e PR.
- Dependencias: P0.1.
- Criterios de aceite:
1. Pipeline roda em PR e branch principal.
2. Padrao de commit/PR documentado.

### P0.3 - Regras de negocio oficiais
- Escopo:
1. Especificar regras para produto, estoque, pedido, pagamento, entrega.
2. Definir fluxos CP (consistencia forte) e AP (eventual).
3. Registrar contratos de API e versionamento.
- Dependencias: P0.1.
- Criterios de aceite:
1. Documento de regra por dominio aprovado.
2. Fluxos criticos identificados com invariantes.

### P0.4 - Base multiempresa (tenant)
- Escopo:
1. Definir estrategia de tenancy (inicialmente `tenant_id` com isolamento logico).
2. Aplicar filtros de tenant no acesso aos dados.
3. Ajustar autenticacao para carregar contexto de tenant.
- Dependencias: P0.3.
- Criterios de aceite:
1. Endpoint admin/cliente nao cruza dados entre tenants.
2. Teste de isolamento cobrindo leitura e escrita.

### P0.5 - Produto com variacoes (backend)
- Escopo:
1. Modelar produto pai + variacoes (cor, tamanho etc.).
2. Migrations e constraints (SKU unico por tenant, combinacao unica de atributos).
3. API para criar/editar produto simples e com variacoes.
- Dependencias: P0.3, P0.4.
- Criterios de aceite:
1. Produto simples continua funcionando.
2. Produto com variacoes salva e consulta corretamente.
3. Estoque/preco por variacao.

### P0.6 - Produto com variacoes (admin UX)
- Escopo:
1. Checkbox "Tem mais de uma versao?" no cadastro.
2. Se marcado: secao de atributos e grade de combinacoes.
3. Edicao por combinacao: SKU, preco, estoque, imagem, ativo.
- Dependencias: P0.5.
- Criterios de aceite:
1. Fluxo simples e fluxo com variacoes operacionais.
2. Validacoes visuais e de backend consistentes.

### P0.7 - Seguranca minima obrigatoria
- Escopo:
1. Revisao de autorizacao em endpoints admin e self-service.
2. Protecao de upload (tipo/tamanho) e logs de auditoria.
3. Varredura de vulnerabilidades no CI.
- Dependencias: P0.2.
- Criterios de aceite:
1. Sem vulnerabilidade critica aberta para release.
2. Endpoints sensiveis com autorizacao validada por testes.

### P0.8 - Gate de qualidade para release
- Escopo:
1. Definir suite minima: unitario + integracao + regressao checkout/estoque/pagamento.
2. Criar checklist Go/No-Go.
3. Definir rollback padrao.
- Dependencias: P0.2, P0.3.
- Criterios de aceite:
1. Nenhum deploy sem gate completo.
2. Checklist de release versionado no repositorio.

## P1 - Importante (sequencia imediata apos P0)

### P1.1 - Sincronizacao confiavel entre bancos/modulos
- Escopo:
1. Definir estrategia (outbox/consumidor idempotente).
2. Chave de idempotencia e deduplicacao.
3. Rotina de reconciliacao.
- Dependencias: P0.3, P0.4.
- Criterios de aceite:
1. Reprocessamento nao duplica efeito.
2. Painel de lag/erros de sincronizacao disponivel.

### P1.2 - Observabilidade operacional
- Escopo:
1. Correlation ID ponta a ponta.
2. Dashboards de erros, latencia e filas.
3. Alertas para 5xx, falha de fila e aumento de latencia.
- Dependencias: P0.8.
- Criterios de aceite:
1. Alertas ativos em staging/producao.
2. Tempo medio de deteccao reduzido.

### P1.3 - Hardening do checkout e pagamentos
- Escopo:
1. Garantir consistencia de estoque antes do fechamento.
2. Tratar timeout/retry de gateway.
3. Auditar divergencia de forma de pagamento.
- Dependencias: P0.3, P0.8.
- Criterios de aceite:
1. Fluxo de erro coberto por testes.
2. Sem regressao no caminho feliz.

### P1.4 - Entregas e rastreio operacional
- Escopo:
1. Consolidar roteirizacao e estados de parada.
2. Garantir rastreabilidade de ocorrencias e confirmacoes.
3. Relatorios operacionais por janela.
- Dependencias: P0.8.
- Criterios de aceite:
1. Estados de entrega sem transicao invalida.
2. Auditoria por rota/parada disponivel.

### P1.5 - Marketing e automacoes com controle
- Escopo:
1. Governar campanhas/automacoes com pausa/resumo e logs.
2. Reforcar limites de envio e retries.
3. Segmentar com consistencia de dados.
- Dependencias: P1.1, P1.2.
- Criterios de aceite:
1. Filas monitoradas e recuperaveis.
2. Sem duplicacao de disparo.

## Dependencias criticas
1. P0.3 e P0.4 bloqueiam quase todo o restante.
2. P0.5 bloqueia P0.6.
3. P0.8 bloqueia releases seguros de P1.

## Riscos principais e mitigacao
1. **Risco:** quebrar fluxo atual de produto.
- Mitigacao: compatibilidade com produto simples + testes de regressao.
2. **Risco:** vazamento entre tenants.
- Mitigacao: filtro de tenant central + testes de isolamento automatizados.
3. **Risco:** sincronizacao gerar duplicidade.
- Mitigacao: idempotencia e deduplicacao obrigatorias.
4. **Risco:** release com regressao operacional.
- Mitigacao: gate Go/No-Go + rollback ensaiado.

## Definicao de pronto para iniciar implementacao
1. P0.1 e P0.2 concluidos.
2. P0.3 aprovado por produto + tecnico.
3. Escopo de P0.4/P0.5 fechado com migrations planejadas.

## Proxima execucao imediata (ordem)
1. Abrir cards do P0.3 por dominio: produto, estoque, pedido, pagamento, entrega.
2. Consolidar matriz CP/AP por fluxo critico.
3. Aprovar contrato base de produto com variacoes.
4. Iniciar modelagem de dados e migrations (P0.5).
