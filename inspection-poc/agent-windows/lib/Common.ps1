# ── 공통 라이브러리 ──────────────────────────────────────────────
# 점검 에이전트 전역 유틸. 모든 체크/콜렉터/렌더러에서 dot-source.

$ErrorActionPreference = 'Stop'
$script:IsoFmt = 'yyyy-MM-ddTHH:mm:sszzz'

function Get-RepoRoot {
    Split-Path -Parent $PSScriptRoot
}

function Read-SiteConfig {
    param([string]$Path)
    if (-not (Test-Path $Path)) { throw "config not found: $Path" }
    $raw = Get-Content -Path $Path -Raw -Encoding UTF8
    return $raw | ConvertFrom-Json
}

function Write-Json {
    param(
        [Parameter(Mandatory)] $Object,
        [Parameter(Mandatory)][string]$Path,
        [switch]$Compact
    )
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    $depth = 10
    $json = if ($Compact) { $Object | ConvertTo-Json -Depth $depth -Compress }
            else          { $Object | ConvertTo-Json -Depth $depth }
    Set-Content -Path $Path -Value $json -Encoding UTF8 -NoNewline
}

function Read-Json {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return $null }
    Get-Content -Path $Path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Resolve-Status {
    param(
        [Parameter(Mandatory)] [double] $Value,
        [Parameter(Mandatory)] $Threshold,   # @{warn=..;crit=..}
        [switch] $Inverted                   # 큰 값이 좋은 경우
    )
    if (-not $Threshold) { return 'ok' }
    $w = $Threshold.warn; $c = $Threshold.crit
    if ($Inverted) {
        if ($Value -le $c) { return 'crit' }
        if ($Value -le $w) { return 'warn' }
        return 'ok'
    } else {
        if ($Value -ge $c) { return 'crit' }
        if ($Value -ge $w) { return 'warn' }
        return 'ok'
    }
}

function New-CheckResult {
    param(
        [Parameter(Mandatory)][string] $Id,
        [Parameter(Mandatory)][string] $Label,
        [Parameter(Mandatory)][string] $Category,
        [ValidateSet('auto','manual','semi')] [string] $Method = 'auto',
        [string] $Cmd,
        $Value,
        [ValidateSet('ok','warn','crit','error','pending_manual','n/a')] [string] $Status = 'ok',
        $Threshold,
        [string] $Raw,
        [int] $TookMs = 0,
        [string] $Note
    )
    $obj = [ordered]@{
        id = $Id
        label = $Label
        category = $Category
        method = $Method
        cmd = $Cmd
        value = $Value
        status = $Status
        took_ms = $TookMs
    }
    if ($Threshold) { $obj.threshold = $Threshold }
    if ($Raw)       { $obj.raw = $Raw }
    if ($Note)      { $obj.note = $Note }
    return [pscustomobject]$obj
}

function Invoke-Timed {
    param([Parameter(Mandatory)][scriptblock]$Block)
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $result = & $Block
    $sw.Stop()
    return @{ Result = $result; Ms = [int]$sw.ElapsedMilliseconds }
}

function Get-IsoNow { (Get-Date).ToString($script:IsoFmt) }

function Write-Log {
    param([string]$Level = 'INFO', [string]$Msg)
    $ts = Get-Date -Format 'HH:mm:ss.fff'
    Write-Host ("[{0}] [{1,-5}] {2}" -f $ts, $Level, $Msg)
}
