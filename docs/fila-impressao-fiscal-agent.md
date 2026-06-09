# Fila de Impressao Fiscal - Agente da Loja

## Objetivo

Permitir que um computador da loja, ligado a uma impressora local, consuma a fila fiscal sem depender do navegador do admin.

O agente deve rodar com uma credencial propria da estacao, gerada no painel admin da fila fiscal.

## Fluxo esperado

1. A estacao envia `heartbeat`.
2. A estacao consulta o proximo job pronto.
3. Se houver job com `danfeUrl`, a estacao baixa o arquivo e imprime localmente.
4. Ao terminar, a estacao marca o job como `concluido`.
5. Se falhar, a estacao marca o job como `falhou`.

## Endpoints

Base: `/api/admin/fiscal/impressao/agente`

Headers obrigatorios em todas as chamadas do agente:

- `X-Print-Station-Code`
- `X-Print-Station-Key`

### Heartbeat da estacao

`POST /estacoes/{code}/heartbeat`

Resposta:

```json
{
  "id": 1,
  "code": "CAIXA-1",
  "displayName": "Caixa principal",
  "active": true,
  "lastHeartbeatAt": "2026-03-11T15:00:00"
}
```

### Capturar proximo job

`GET /estacoes/{code}/proximo-job`

Sem job:

```json
{
  "job": null
}
```

Com job:

```json
{
  "job": {
    "jobId": 77,
    "pedidoId": 14,
    "fiscalDocumentId": 99,
    "jobType": "DANFE_IMMEDIATE",
    "printChannel": "IMMEDIATE",
    "stationCode": "CAIXA-1",
    "stationName": "Caixa principal",
    "copies": 1,
    "accessKey": "123",
    "danfeUrl": "https://...",
    "documentStatus": "AUTHORIZED",
    "updatedAt": "2026-03-11T15:02:00"
  }
}
```

Observacao:
- esse endpoint ja reserva o job para a estacao e move o status para `PRINTING`

### Concluir impressao

`POST /jobs/{jobId}/concluido`

### Registrar falha

`POST /jobs/{jobId}/falhou`

Payload:

```json
{
  "message": "sem papel"
}
```

## Regras operacionais

- Jobs de balcao e retirada entram com prioridade maior.
- Jobs de entrega podem ser segurados/liberados pelo admin.
- O agente deve ignorar job sem `danfeUrl`.
- O agente so pode concluir ou falhar jobs atribuidos a propria estacao.
- O admin continua podendo cancelar, reimprimir ou mover a fila manualmente.

## Auditoria no admin

O painel `/admin/fiscal/impressao` exibe:

- jobs recentes da fila
- estacoes cadastradas
- historico recente de eventos da fila

Esse historico ajuda a rastrear criacao, segurada, liberacao, inicio, falha, impressao e reimpressao sem depender de logs de infraestrutura.

## Autenticacao

- Em operacao normal, o agente deve usar a credencial da propria estacao.
- O admin pode gerar ou regenerar a chave no painel `/admin/fiscal/impressao`.
- A chave aparece uma unica vez apos a geracao e depois fica armazenada apenas como hash.
- Para suporte manual, os endpoints ainda aceitam usuarios com papel `ADMIN` ou `CAIXA`.

## Script inicial

Existe um agente PowerShell inicial em:

- `infra/print-agent/print-agent.ps1`
- `infra/print-agent/agent-config.example.psd1`
- `infra/print-agent/install-print-agent-task.ps1`
- `infra/print-agent/uninstall-print-agent-task.ps1`

Uso manual recomendado:

```powershell
.\infra\print-agent\print-agent.ps1 `
  -ConfigPath ".\infra\print-agent\agent-config.psd1"
```

Observacoes:

- Se `SumatraPDF` estiver disponivel, ele e o modo recomendado para impressao silenciosa.
- Sem ele, o script tenta o verbo `Print` do Windows para o visualizador PDF padrao.
- O agente grava logs diarios e aplica backoff progressivo em falhas.

## Instalacao no Windows

Para subir com o Windows:

```powershell
.\infra\print-agent\install-print-agent-task.ps1 `
  -ConfigPath ".\infra\print-agent\agent-config.psd1"
```

Modo alternativo, quando a impressora existe apenas no usuario logado:

```powershell
.\infra\print-agent\install-print-agent-task.ps1 `
  -ConfigPath ".\infra\print-agent\agent-config.psd1" `
  -RunAsCurrentUser
```

Para remover:

```powershell
.\infra\print-agent\uninstall-print-agent-task.ps1
```
