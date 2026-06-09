param(
    [string]$ConfigPath = ".\agent-config.psd1",

    [string]$TaskName = "RedemaisFarma Fiscal Print Agent",

    [string]$PowerShellPath,

    [switch]$RunAsCurrentUser
)

$ErrorActionPreference = "Stop"

function Resolve-AbsolutePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return Join-Path $PSScriptRoot $Path
}

function Resolve-PowerShellExecutable {
    if ($PowerShellPath) {
        return $PowerShellPath
    }

    $pwsh = Get-Command "pwsh.exe" -ErrorAction SilentlyContinue
    if ($pwsh) {
        return $pwsh.Source
    }

    $powershell = Get-Command "powershell.exe" -ErrorAction SilentlyContinue
    if ($powershell) {
        return $powershell.Source
    }

    throw "Nao foi possivel localizar pwsh.exe nem powershell.exe."
}

$resolvedConfigPath = Resolve-AbsolutePath -Path $ConfigPath
if (-not (Test-Path -LiteralPath $resolvedConfigPath)) {
    throw "Arquivo de configuracao nao encontrado: $resolvedConfigPath"
}

$agentScriptPath = Join-Path $PSScriptRoot "print-agent.ps1"
if (-not (Test-Path -LiteralPath $agentScriptPath)) {
    throw "Script do agente nao encontrado: $agentScriptPath"
}

$resolvedPowerShellPath = Resolve-PowerShellExecutable
$argumentList = @(
    "-NoProfile",
    "-ExecutionPolicy", "Bypass",
    "-WindowStyle", "Hidden",
    "-File", ('"' + $agentScriptPath + '"'),
    "-ConfigPath", ('"' + $resolvedConfigPath + '"')
) -join " "

$action = New-ScheduledTaskAction `
    -Execute $resolvedPowerShellPath `
    -Argument $argumentList `
    -WorkingDirectory $PSScriptRoot

$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -RestartCount 999 `
    -RestartInterval (New-TimeSpan -Minutes 1) `
    -MultipleInstances IgnoreNew `
    -StartWhenAvailable

if ($RunAsCurrentUser) {
    $currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $principal = New-ScheduledTaskPrincipal `
        -UserId $currentUser `
        -LogonType Interactive `
        -RunLevel Highest
    $trigger = New-ScheduledTaskTrigger -AtLogOn -User $currentUser
} else {
    $principal = New-ScheduledTaskPrincipal `
        -UserId "SYSTEM" `
        -LogonType ServiceAccount `
        -RunLevel Highest
    $trigger = New-ScheduledTaskTrigger -AtStartup
}

Register-ScheduledTask `
    -TaskName $TaskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Principal $principal `
    -Force | Out-Null

Write-Host "Tarefa '$TaskName' registrada com sucesso."
Write-Host "PowerShell: $resolvedPowerShellPath"
Write-Host "Config: $resolvedConfigPath"
if ($RunAsCurrentUser) {
    Write-Host "Modo: usuario atual no logon."
} else {
    Write-Host "Modo: SYSTEM na inicializacao do Windows."
}
