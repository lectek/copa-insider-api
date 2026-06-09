param(
    [string]$ConfigPath,

    [string]$BaseUrl,

    [string]$StationCode,

    [string]$StationKey,

    [string]$PrinterName,

    [int]$PollSeconds,

    [string]$DownloadDir,

    [string]$LogDir,

    [string]$SumatraPdfPath,

    [int]$HttpTimeoutSeconds,

    [int]$RetryMaxSeconds,

    [int]$PrintTimeoutSeconds,

    [switch]$RetainDownloadedFiles
)

$ErrorActionPreference = "Stop"

$script:DefaultConfig = @{
    PollSeconds = 5
    DownloadDir = ".\.print-agent\downloads"
    LogDir = ".\.print-agent\logs"
    HttpTimeoutSeconds = 30
    RetryMaxSeconds = 60
    PrintTimeoutSeconds = 12
    RetainDownloadedFiles = $false
}

function Get-BaseDirectory {
    if ($ConfigPath) {
        return Split-Path -Path (Resolve-Path -LiteralPath $ConfigPath) -Parent
    }
    if ($PSScriptRoot) {
        return $PSScriptRoot
    }
    return (Get-Location).Path
}

function Resolve-AgentPath {
    param(
        [string]$Value
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $null
    }

    if ([System.IO.Path]::IsPathRooted($Value)) {
        return $Value
    }

    return Join-Path (Get-BaseDirectory) $Value
}

function Get-ConfiguredValue {
    param(
        [string]$Name,
        $CurrentValue,
        $ConfigValue,
        $DefaultValue
    )

    if ($null -ne $CurrentValue -and $CurrentValue -ne "") {
        return $CurrentValue
    }
    if ($null -ne $ConfigValue -and $ConfigValue -ne "") {
        return $ConfigValue
    }
    return $DefaultValue
}

function Load-Configuration {
    $config = @{}
    if ($ConfigPath) {
        $resolvedConfigPath = Resolve-Path -LiteralPath $ConfigPath
        $config = Import-PowerShellDataFile -Path $resolvedConfigPath
    }

    $script:BaseUrl = Get-ConfiguredValue "BaseUrl" $BaseUrl $config.BaseUrl $null
    $script:StationCode = Get-ConfiguredValue "StationCode" $StationCode $config.StationCode $null
    $script:StationKey = Get-ConfiguredValue "StationKey" $StationKey $config.StationKey $null
    $script:PrinterName = Get-ConfiguredValue "PrinterName" $PrinterName $config.PrinterName $null
    $script:PollSeconds = [int](Get-ConfiguredValue "PollSeconds" $PollSeconds $config.PollSeconds $script:DefaultConfig.PollSeconds)
    $script:DownloadDir = Resolve-AgentPath (Get-ConfiguredValue "DownloadDir" $DownloadDir $config.DownloadDir $script:DefaultConfig.DownloadDir)
    $script:LogDir = Resolve-AgentPath (Get-ConfiguredValue "LogDir" $LogDir $config.LogDir $script:DefaultConfig.LogDir)
    $script:SumatraPdfPath = Resolve-AgentPath (Get-ConfiguredValue "SumatraPdfPath" $SumatraPdfPath $config.SumatraPdfPath $null)
    $script:HttpTimeoutSeconds = [int](Get-ConfiguredValue "HttpTimeoutSeconds" $HttpTimeoutSeconds $config.HttpTimeoutSeconds $script:DefaultConfig.HttpTimeoutSeconds)
    $script:RetryMaxSeconds = [int](Get-ConfiguredValue "RetryMaxSeconds" $RetryMaxSeconds $config.RetryMaxSeconds $script:DefaultConfig.RetryMaxSeconds)
    $script:PrintTimeoutSeconds = [int](Get-ConfiguredValue "PrintTimeoutSeconds" $PrintTimeoutSeconds $config.PrintTimeoutSeconds $script:DefaultConfig.PrintTimeoutSeconds)

    if ($PSBoundParameters.ContainsKey("RetainDownloadedFiles")) {
        $script:RetainDownloadedFiles = [bool]$RetainDownloadedFiles
    } elseif ($config.ContainsKey("RetainDownloadedFiles")) {
        $script:RetainDownloadedFiles = [bool]$config.RetainDownloadedFiles
    } else {
        $script:RetainDownloadedFiles = [bool]$script:DefaultConfig.RetainDownloadedFiles
    }
}

function Assert-Configuration {
    if ([string]::IsNullOrWhiteSpace($script:BaseUrl)) {
        throw "BaseUrl obrigatorio."
    }
    if ([string]::IsNullOrWhiteSpace($script:StationCode)) {
        throw "StationCode obrigatorio."
    }
    if ([string]::IsNullOrWhiteSpace($script:StationKey)) {
        throw "StationKey obrigatorio."
    }
    if ($script:PollSeconds -le 0) {
        throw "PollSeconds deve ser maior que zero."
    }
    if ($script:HttpTimeoutSeconds -le 0) {
        throw "HttpTimeoutSeconds deve ser maior que zero."
    }
    if ($script:RetryMaxSeconds -lt $script:PollSeconds) {
        $script:RetryMaxSeconds = $script:PollSeconds
    }
    if ($script:PrintTimeoutSeconds -le 0) {
        throw "PrintTimeoutSeconds deve ser maior que zero."
    }

    New-Item -ItemType Directory -Path $script:DownloadDir -Force | Out-Null
    New-Item -ItemType Directory -Path $script:LogDir -Force | Out-Null
}

function Get-LogFilePath {
    $date = Get-Date -Format "yyyyMMdd"
    $safeStationCode = $script:StationCode -replace "[^A-Za-z0-9_-]", "_"
    return Join-Path $script:LogDir ($safeStationCode + "-" + $date + ".log")
}

function Write-AgentLog {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Level,

        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $line = "$timestamp [$Level] $Message"
    Add-Content -Path (Get-LogFilePath) -Value $line

    switch ($Level) {
        "ERROR" { Write-Error $Message }
        "WARN" { Write-Warning $Message }
        default { Write-Host $line }
    }
}

function Get-AgentHeaders {
    return @{
        "X-Print-Station-Code" = $script:StationCode
        "X-Print-Station-Key" = $script:StationKey
    }
}

function Join-AgentUrl {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $root = $script:BaseUrl.TrimEnd("/")
    if ($Path.StartsWith("http://") -or $Path.StartsWith("https://")) {
        return $Path
    }
    if (-not $Path.StartsWith("/")) {
        $Path = "/" + $Path
    }
    return $root + $Path
}

function Invoke-AgentApi {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Method,

        [Parameter(Mandatory = $true)]
        [string]$Path,

        [object]$Body
    )

    $parameters = @{
        Method = $Method
        Uri = (Join-AgentUrl $Path)
        Headers = (Get-AgentHeaders)
        TimeoutSec = $script:HttpTimeoutSeconds
    }

    if ($PSBoundParameters.ContainsKey("Body")) {
        $parameters.ContentType = "application/json"
        $parameters.Body = $Body | ConvertTo-Json -Depth 5
    }

    return Invoke-RestMethod @parameters
}

function Invoke-AgentHeartbeat {
    Invoke-AgentApi -Method "Post" -Path "/api/admin/fiscal/impressao/agente/estacoes/$($script:StationCode)/heartbeat" | Out-Null
}

function Get-NextJob {
    $response = Invoke-AgentApi -Method "Get" -Path "/api/admin/fiscal/impressao/agente/estacoes/$($script:StationCode)/proximo-job"
    return $response.job
}

function Complete-Job {
    param(
        [Parameter(Mandatory = $true)]
        [long]$JobId
    )

    Invoke-AgentApi -Method "Post" -Path "/api/admin/fiscal/impressao/agente/jobs/$JobId/concluido" | Out-Null
}

function Fail-Job {
    param(
        [Parameter(Mandatory = $true)]
        [long]$JobId,

        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    Invoke-AgentApi -Method "Post" -Path "/api/admin/fiscal/impressao/agente/jobs/$JobId/falhou" -Body @{
        message = $Message
    } | Out-Null
}

function Download-Danfe {
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Job
    )

    $jobDir = Join-Path $script:DownloadDir $script:StationCode
    New-Item -ItemType Directory -Path $jobDir -Force | Out-Null

    $targetFile = Join-Path $jobDir ("job-" + $Job.jobId + ".pdf")
    $uri = Join-AgentUrl $Job.danfeUrl
    Invoke-WebRequest `
        -Uri $uri `
        -OutFile $targetFile `
        -Headers (Get-AgentHeaders) `
        -TimeoutSec $script:HttpTimeoutSeconds
    return $targetFile
}

function Remove-DownloadedFile {
    param(
        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return
    }

    Remove-Item -LiteralPath $Path -Force
}

function Invoke-PdfPrint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PdfPath,

        [int]$Copies = 1
    )

    $normalizedCopies = [Math]::Max($Copies, 1)

    if ($script:SumatraPdfPath -and (Test-Path -LiteralPath $script:SumatraPdfPath)) {
        for ($copyIndex = 1; $copyIndex -le $normalizedCopies; $copyIndex++) {
            $arguments = @("-silent")
            if ($script:PrinterName) {
                $arguments += @("-print-to", $script:PrinterName)
            }
            $arguments += $PdfPath
            $process = Start-Process -FilePath $script:SumatraPdfPath `
                -ArgumentList $arguments `
                -PassThru `
                -Wait
            if ($process.ExitCode -ne 0) {
                throw "SumatraPDF retornou codigo $($process.ExitCode)."
            }
        }
        return
    }

    for ($copyIndex = 1; $copyIndex -le $normalizedCopies; $copyIndex++) {
        $process = Start-Process -FilePath $PdfPath -Verb Print -PassThru
        Start-Sleep -Seconds $script:PrintTimeoutSeconds
        if (-not $process.HasExited) {
            $process.CloseMainWindow() | Out-Null
            Start-Sleep -Seconds 2
            if (-not $process.HasExited) {
                $process | Stop-Process -Force
            }
        }
    }
}

Load-Configuration
Assert-Configuration

$script:RetryDelaySeconds = [Math]::Max($script:PollSeconds, 5)

Write-AgentLog -Level "INFO" -Message "Agente fiscal iniciado para a estacao $($script:StationCode)."

while ($true) {
    $job = $null
    $pdfPath = $null

    try {
        Invoke-AgentHeartbeat
        $job = Get-NextJob

        if ($null -eq $job) {
            Start-Sleep -Seconds $script:PollSeconds
            $script:RetryDelaySeconds = [Math]::Max($script:PollSeconds, 5)
            continue
        }

        Write-AgentLog -Level "INFO" -Message "Job $($job.jobId) recebido para o pedido $($job.pedidoId)."

        if ([string]::IsNullOrWhiteSpace($job.danfeUrl)) {
            Fail-Job -JobId $job.jobId -Message "Job sem DANFE para impressao."
            Write-AgentLog -Level "WARN" -Message "Job $($job.jobId) ignorado por falta de DANFE."
            Start-Sleep -Seconds 1
            continue
        }

        $pdfPath = Download-Danfe -Job $job
        Invoke-PdfPrint -PdfPath $pdfPath -Copies $job.copies
        Complete-Job -JobId $job.jobId
        Write-AgentLog -Level "INFO" -Message "Job $($job.jobId) concluido com sucesso."
        $job = $null

        if (-not $script:RetainDownloadedFiles) {
            try {
                Remove-DownloadedFile -Path $pdfPath
            } catch {
                Write-AgentLog -Level "WARN" -Message "Nao foi possivel remover o PDF local apos a impressao."
            }
        }

        $script:RetryDelaySeconds = [Math]::Max($script:PollSeconds, 5)
    } catch {
        $message = $_.Exception.Message
        Write-AgentLog -Level "WARN" -Message $message

        if ($job -and $job.jobId) {
            try {
                Fail-Job -JobId $job.jobId -Message $message
                Write-AgentLog -Level "WARN" -Message "Falha registrada para o job $($job.jobId)."
            } catch {
                Write-AgentLog -Level "WARN" -Message "Nao foi possivel registrar a falha do job $($job.jobId)."
            }
        }

        Write-AgentLog -Level "INFO" -Message "Aguardando $($script:RetryDelaySeconds)s antes da proxima tentativa."
        Start-Sleep -Seconds $script:RetryDelaySeconds
        $script:RetryDelaySeconds = [Math]::Min(
            $script:RetryDelaySeconds * 2,
            [Math]::Max($script:RetryMaxSeconds, $script:RetryDelaySeconds)
        )
    }
}
