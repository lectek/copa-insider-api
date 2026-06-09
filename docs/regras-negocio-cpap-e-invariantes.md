# Regras de Negocio Oficiais - Matriz CP/AP e Invariantes

## Objetivo
Definir regras centrais do sistema, separando fluxos com consistencia forte (CP) e fluxos com consistencia eventual (AP), para orientar implementacao segura e estavel.

## Dominios cobertos
1. Produto e estoque
2. Pedido e checkout
3. Pagamento
4. Entrega
5. Marketing e notificacoes

## Invariantes globais
1. Nenhuma operacao deve cruzar dados entre tenants.
2. Pedido confirmado deve manter historico imutavel de itens/precos.
3. Estoque nunca pode ficar negativo apos confirmacao de venda.
4. Pagamento aprovado nao pode ser perdido em reprocessamento.
5. Todo evento de sincronizacao deve ser idempotente.

## Matriz CP/AP por fluxo

| Fluxo | Tipo | Motivo |
|---|---|---|
| Criacao/edicao de produto | CP | Fonte de verdade de catalogo |
| Publicacao/despublicacao de produto | CP | Impacta exibicao e venda |
| Atualizacao de estoque para venda | CP | Evita overselling |
| Fechamento de pedido | CP | Integridade comercial/fiscal |
| Captura/confirmacao de pagamento | CP | Integridade financeira |
| Confirmacao de entrega | CP | Estado final do pedido |
| Campanhas de email | AP | Aceita atraso controlado |
| Automacoes de marketing | AP | Processamento assincrono |
| Dashboards e metricas | AP | Leitura analitica |
| Sincronizacao secundaria entre bancos | AP com reconciliacao | Alta disponibilidade com consistencia eventual |

## Regras por dominio

## 1) Produto e estoque
### Regras
1. Produto pode ser simples ou com variacoes.
2. Em produto com variacoes, preco e estoque sao da variacao.
3. Combinacao de atributos da variacao deve ser unica por produto.
4. SKU deve ser unico por tenant.

### Invariantes
1. Nao permitir variacao duplicada (`cor+tamanho` repetidos).
2. Nao permitir estoque negativo.
3. Nao publicar produto ativo sem dados minimos obrigatorios.

## 2) Pedido e checkout
### Regras
1. Antes de fechar pedido, validar disponibilidade real de estoque.
2. Snapshot do pedido deve congelar preco e itens no momento do fechamento.
3. Pedido nao pode voltar para estado anterior apos confirmacao de pagamento.

### Invariantes
1. Id do pedido e status devem ser auditaveis.
2. Mudanca de status invalida deve ser bloqueada por regra.

## 3) Pagamento
### Regras
1. Toda confirmacao de pagamento deve ser idempotente.
2. Timeout de gateway deve permitir retry seguro sem duplicar cobranca.
3. Divergencia entre metodo solicitado e recebido deve ser registrada.

### Invariantes
1. Transacao externa deve ter chave unica de correlacao.
2. Mesmo callback nao pode gerar duas baixas financeiras.

## 4) Entrega
### Regras
1. Rota so pode ser iniciada se pedidos estiverem elegiveis.
2. Parada deve respeitar transicoes: `pendente -> em_rota -> entregue/insucesso`.
3. Confirmacao de entrega exige evidencias minimas (codigo/status/ocorrencia quando aplicavel).

### Invariantes
1. Nao concluir rota com parada obrigatoria em aberto.
2. Toda ocorrencia deve ficar auditada com timestamp e operador.

## 5) Marketing e notificacoes
### Regras
1. Campanhas devem permitir pausar/resumir/cancelar.
2. Fila de envio deve controlar retry e status.
3. Automacoes nao podem duplicar disparo para mesma janela e chave de negocio.

### Invariantes
1. Processamento de fila deve ser idempotente.
2. Falhas devem ser rastreaveis por campanha/evento.

## Contrato inicial - Produto com variacoes (base)

## Payload conceitual de criacao/edicao
```json
{
  "tenantId": "empresa-a",
  "nome": "Copo termico",
  "tipoProduto": "VARIAVEL",
  "categoriaId": 10,
  "variacoes": [
    {
      "atributos": {"cor": "preto", "tamanho": "500ml"},
      "sku": "COPO-PR-500",
      "preco": 49.90,
      "estoque": 30,
      "ativo": true
    }
  ]
}
```

## Validacoes minimas
1. `tipoProduto=SIMPLES` -> sem grade de variacoes obrigatoria.
2. `tipoProduto=VARIAVEL` -> ao menos 1 variacao valida.
3. SKU unico por tenant.
4. Combinacao de atributos unica por produto.

## Regras de sincronizacao (base)
1. Evento deve ter `eventId` unico e `tenantId`.
2. Consumidor deve registrar `eventId` processado (deduplicacao).
3. Reprocessamento deve ser seguro (sem efeito colateral duplicado).
4. Job de reconciliacao deve apontar divergencias entre origem e destino.

## Checklist de aceite do P0.3
1. Matriz CP/AP aprovada.
2. Invariantes por dominio aprovados.
3. Contrato inicial de produto com variacoes aprovado.
4. Base pronta para iniciar P0.4 (tenant) e P0.5 (modelo + migration).
