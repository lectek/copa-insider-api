# Maquineta por Webhook

## O que ja foi entregue

- Configuracao de maquineta no admin (`Pagamentos`):
  - `Integracao ativa`
  - `Modo`: `mock` ou `webhook`
  - `Provedor`, `Endpoint webhook`, `Terminal ID`, `Merchant ID`, `Timeout`, `Token`
- Botao `Testar maquineta` na tela de pagamentos.
- API admin para configuracao e teste:
  - `GET /api/admin/pagamentos/terminal/config`
  - `PUT /api/admin/pagamentos/terminal/config`
  - `POST /api/admin/pagamentos/terminal/test`
- Fluxo de venda rapida no caixa:
  - Cartao credito/debito passa pela autorizacao da maquineta.
  - Se recusar/falhar, a venda nao e confirmada.
  - Se aprovar, grava transacao no metodo de pagamento (`[tx:...]`).

## Modos de operacao

- `mock`: aprova localmente, sem dependencia externa.
- `webhook`: envia requisicao HTTP para um endpoint externo (ex.: n8n).

## Payload enviado no modo webhook

Exemplo de corpo enviado para o endpoint configurado:

```json
{
  "amount": 129.90,
  "amountCents": 12990,
  "paymentType": "CARTAO_CREDITO",
  "reference": "PDV-12345678901",
  "source": "PDV_VENDA_RAPIDA",
  "provider": "stone",
  "terminalId": "TERMINAL-01",
  "merchantId": "LOJA-01",
  "requestedAt": "2026-03-08T07:00:00Z",
  "metadata": {
    "notaOpcao": "IMPRESSAO"
  }
}
```

Headers adicionados quando preenchidos:

- `Authorization: Bearer <token>`
- `X-Terminal-Id: <terminalId>`
- `X-Merchant-Id: <merchantId>`

## Resposta esperada do webhook

A API aceita os campos abaixo (minimo recomendado: `approved`):

```json
{
  "approved": true,
  "status": "approved",
  "transactionId": "ABC123456",
  "message": "Pagamento aprovado"
}
```

Campos alternativos aceitos para identificador:

- `transaction_id`
- `nsu`
- `authorizationCode`
- `authorization_code`
- `id`

## Como usar com n8n (gratuito)

Fluxo sugerido no n8n:

1. `Webhook` (recebe payload do sistema).
2. `HTTP Request` (chama API da maquineta/provedor contratado).
3. `Set` (normaliza retorno para `approved`, `status`, `transactionId`, `message`).
4. `Respond to Webhook` (retorna JSON no contrato acima).

Com isso, o backend nao fica acoplado a um provedor unico e voce pode trocar a integracao sem alterar o fluxo do caixa.
