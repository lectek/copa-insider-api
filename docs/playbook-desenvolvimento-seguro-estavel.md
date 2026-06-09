# Playbook de Desenvolvimento Seguro, Funcional e Estável

## Objetivo
Padronizar como planejar, desenvolver, testar e publicar software com segurança, estabilidade e previsibilidade.

## Ferramentas recomendadas
- Gestão de tarefas: **GetShitDone** (ou equivalente)
- Código e PR: **GitHub**
- CI/CD: GitHub Actions (ou equivalente)
- Qualidade: testes automatizados, SAST, dependabot/scan de dependências
- Observabilidade: logs estruturados, métricas e alertas

## Fluxo passo a passo

### 1. Planejamento (GetShitDone)
1. Criar épicos: Produto, Catálogo, Checkout, Entregas, Marketing, Segurança, Observabilidade.
2. Criar tarefas pequenas (1 a 3 dias cada) com DoR/DoD.
3. Definir prioridade: `P0` (risco/receita), `P1` (importante), `P2` (evolutivo).
4. Mapear dependências entre tarefas.
5. Definir critérios de aceite mensuráveis.

### 2. Arquitetura e regras de negócio
1. Registrar regras por domínio (produto, pedido, pagamento, entrega, cliente).
2. Definir fluxos críticos com consistência forte (pedido, pagamento, estoque).
3. Definir fluxos tolerantes a eventual consistency (campanhas, métricas, notificações).
4. Versionar contratos de API (OpenAPI) antes de codar.

### 3. Implementação segura
1. Abrir branch por tarefa: `feat/...`, `fix/...`, `chore/...`.
2. Implementar com validações de entrada, tratamento de erro e logs com correlação.
3. Não expor segredo em código; usar variáveis de ambiente.
4. Evitar quebra de compatibilidade: manter contrato antigo quando necessário.

### 4. Testes e qualidade
1. Unitários para regra de negócio.
2. Integração para fluxo HTTP + persistência + filas.
3. Testes de regressão para checkout/pagamento/estoque.
4. Checkstyle/SpotBugs/Jacoco no pipeline.
5. Critérios mínimos sugeridos:
- build verde
- sem vulnerabilidade crítica
- cobertura mínima do módulo alterado

### 5. Segurança e compliance
1. Revisar autenticação/autorização por endpoint.
2. Validar isolamento de dados por empresa (tenant).
3. Aplicar rate limit em endpoints sensíveis.
4. Proteger uploads (tipo, tamanho, malware scan quando possível).
5. Auditar ações administrativas relevantes.

### 6. Release e operação
1. Deploy em staging com smoke test.
2. Aprovação de release com checklist.
3. Deploy em produção com monitoramento ativo.
4. Plano de rollback documentado.
5. Pós-release: revisar erros, latência e funil de checkout.

## Definições obrigatórias por tarefa
- **DoR (Definition of Ready)**:
  - escopo claro
  - regra de negócio definida
  - contrato de API definido (quando aplicável)
  - risco mapeado
- **DoD (Definition of Done)**:
  - código revisado
  - testes automatizados passando
  - documentação atualizada
  - monitoramento/alerta aplicável

## Template de card no GetShitDone
**Título:** `[P1][BACKEND] Cadastro de produto com variações`  
**Objetivo:** permitir produto simples ou com variações no admin.  
**Escopo:** endpoint, validações, persistência, UI admin.  
**Fora de escopo:** recomendação IA de variações.  
**Critérios de aceite:**
1. checkbox "tem mais de uma versão?"
2. criação de combinações (cor/tamanho)
3. preço/estoque/SKU por variação
4. bloqueio de combinação duplicada  
**Riscos:** migração de dados e impacto no checkout.  
**Testes:** unitário + integração + regressão de carrinho.

## Prompts prontos

### Prompt 1 - Planejamento técnico da sprint
```text
Atue como Tech Lead e quebre o objetivo abaixo em tarefas de 1 a 3 dias.
Objetivo: [descrever]
Contexto: backend Spring Boot + admin web + mobile.
Restrições: sem big-bang, manter compatibilidade.
Saída:
1) backlog priorizado P0/P1/P2
2) dependências entre tarefas
3) riscos e mitigação
4) critérios de aceite por tarefa
5) plano de entrega em 2 semanas
```

### Prompt 2 - Especificação de regra de negócio
```text
Atue como analista de negócios + arquiteto.
Formalize a regra de negócio para: [tema].
Inclua:
1) fluxo principal
2) fluxos de erro
3) validações obrigatórias
4) estados e transições
5) impacto em estoque/pagamento/pedido
6) casos de teste funcionais
```

### Prompt 3 - Implementação backend segura
```text
Atue como engenheiro backend sênior.
Implemente [feature] em Spring Boot seguindo:
- validação de entrada
- tratamento de erro com HTTP correto
- logs com correlation id
- testes unitários e integração
- sem quebrar endpoints existentes
Entregue:
1) lista de arquivos alterados
2) migrations SQL
3) endpoints criados/alterados
4) testes adicionados
5) riscos residuais
```

### Prompt 4 - Implementação frontend/admin
```text
Atue como engenheiro frontend/admin.
Implemente a UX para [feature] com foco em clareza operacional.
Requisitos:
1) estados loading/erro/sucesso
2) validações no formulário
3) feedback de erro acionável
4) acessibilidade básica
5) responsivo desktop/mobile
Entregue:
1) fluxos de tela
2) componentes alterados
3) payloads enviados/recebidos
4) checklist de QA manual
```

### Prompt 5 - Revisão de segurança pré-release
```text
Atue como AppSec.
Faça revisão de segurança da feature [nome].
Checklist:
1) authN/authZ
2) validação e sanitização
3) exposição de dados sensíveis
4) upload/arquivos
5) SQLi/XSS/CSRF
6) segredos e configuração
Saída:
1) achados por severidade
2) correções obrigatórias antes do deploy
3) hardening recomendado
```

### Prompt 6 - Go/No-Go de produção
```text
Atue como Release Manager.
Avalie se a versão [tag] está apta para produção.
Considere:
1) pipeline CI
2) cobertura e testes críticos
3) vulnerabilidades
4) migrações
5) observabilidade e alertas
6) plano de rollback
Retorne:
1) decisão GO/NO-GO
2) pendências bloqueantes
3) checklist final de deploy
```

## Checklist de estabilidade contínua
1. Revisar erros 5xx por endpoint semanalmente.
2. Revisar latência p95/p99 dos fluxos críticos.
3. Revisar falhas de fila (email/integrações).
4. Rodar teste de regressão antes de cada release.
5. Revisar backlog de dívida técnica por impacto.
