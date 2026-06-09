# Agente Local de Impressao Fiscal

Este diretorio contem um agente PowerShell inicial para a estacao da loja consumir a fila fiscal e imprimir a DANFE localmente.

## Arquivos

- `print-agent.ps1`: loop de polling da fila, download do PDF e retorno de status para a API.
- `agent-config.example.psd1`: exemplo de configuracao externa por estacao.
- `install-print-agent-task.ps1`: registra o agente como tarefa automatica do Windows.
- `uninstall-print-agent-task.ps1`: remove a tarefa automatica do Windows.

## Requisitos

- Windows com PowerShell 5.1+ ou PowerShell 7+
- Acesso ao endpoint da aplicacao
- Credencial da estacao gerada em `/admin/fiscal/impressao`
- Impressora instalada na maquina

## Recomendacao

Para impressao silenciosa de PDF, use `SumatraPDF`.

## Configuracao

1. Copie `agent-config.example.psd1` para `agent-config.psd1`.
2. Ajuste `BaseUrl`, `StationCode`, `StationKey` e `PrinterName`.
3. Se quiser impressao silenciosa, aponte `SumatraPdfPath`.

Exemplo de execucao manual:

```powershell
.\print-agent.ps1 `
  -ConfigPath ".\agent-config.psd1"
```

## Instalacao automatica

Modo recomendado para loja:

```powershell
.\install-print-agent-task.ps1 -ConfigPath ".\agent-config.psd1"
```

Isso registra o agente como tarefa do Windows em modo `SYSTEM`, iniciando com o Windows.

Se a impressora da loja estiver instalada apenas no usuario logado:

```powershell
.\install-print-agent-task.ps1 `
  -ConfigPath ".\agent-config.psd1" `
  -RunAsCurrentUser
```

Para remover:

```powershell
.\uninstall-print-agent-task.ps1
```

## Logs e comportamento

- logs diarios ficam em `LogDir`
- PDFs baixados ficam em `DownloadDir`
- o agente aplica backoff progressivo em falhas de rede ou impressao
- o agente limpa o PDF apos sucesso, salvo se `RetainDownloadedFiles = $true`

## Proximos passos operacionais

- integrar spool ou impressora termica de forma especifica por modelo
- validar na loja se a impressora responde melhor em `SYSTEM` ou `RunAsCurrentUser`
