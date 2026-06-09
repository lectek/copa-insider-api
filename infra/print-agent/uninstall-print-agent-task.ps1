param(
    [string]$TaskName = "RedemaisFarma Fiscal Print Agent"
)

$ErrorActionPreference = "Stop"

$task = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if (-not $task) {
    Write-Host "Tarefa '$TaskName' nao encontrada."
    exit 0
}

Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
Write-Host "Tarefa '$TaskName' removida com sucesso."
