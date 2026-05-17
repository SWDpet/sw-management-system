<#
.SYNOPSIS
  DB(Unix) OS 점검 공통 invoker. checks/db.os.*.ps1 가 dot-source 해서 사용.

.DESCRIPTION
  - remote.unix_db 가 비활성/미설정 → status='n/a' 로 정상 종료
  - OS 별 명령 lookup (Get-UnixCmd) → Invoke-Remote (SSH/Telnet/plink 라우터)
  - parser scriptblock 받아 결과 정규화. parser 미지정 시 raw output (첫 20줄) 만 capture
  - 결과는 New-CheckResult 한 건 (multi-row 결과는 value 안에 배열로 담음)
#>

. (Join-Path $PSScriptRoot 'Common.ps1')
. (Join-Path $PSScriptRoot 'DPAPI.ps1')
. (Join-Path $PSScriptRoot 'Ssh.ps1')
. (Join-Path $PSScriptRoot 'Telnet.ps1')
. (Join-Path $PSScriptRoot 'UnixCmd.ps1')

function _GrepFirst {
    <# 텍스트에서 정규식 패턴의 첫 매치 그룹1 값 반환. 매치 없으면 $null. #>
    param([string]$Text, [string]$Pattern)
    if (-not $Text) { return $null }
    foreach ($line in ($Text -split "`r?`n")) {
        if ($line -match $Pattern) { return $matches[1].Trim() }
    }
    return $null
}

function _LastNonEmptyLine {
    param([string]$Text)
    if (-not $Text) { return '' }
    # @(...) 강제 array — PS 4.0 은 pipeline single result 를 scalar 로 반환,
    # 그 경우 [-1] 가 string 의 마지막 char 를 잡음 (2026-05-17 강진 db.os.uptime "5" 버그).
    $lines = @($Text -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    if ($lines.Count -eq 0) { return '' }
    return $lines[-1]
}

function Invoke-DbOsCheck {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)][string]$Id,
        [Parameter(Mandatory)][string]$Label,
        [Parameter(Mandatory)][string]$CmdKey,
        [Parameter(Mandatory)]$Config,
        [int]$TimeoutSec = 30,
        [scriptblock]$Parser,
        $Threshold,
        [string]$Category = 'db_os'
    )

    $remote = $null
    if ($Config -and $Config.remotes -and $Config.remotes.unix_db) {
        $remote = $Config.remotes.unix_db
    }
    if (-not $remote -or -not $remote.enabled) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd '(unix_db disabled)' -Status 'n/a' -Note 'remotes.unix_db 미설정 또는 enabled=false')
    }

    $os = if ($remote.os) { [string]$remote.os } else { 'linux' }
    $cmd = Get-UnixCmd -Key $CmdKey -Os $os
    if (-not $cmd) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd "(no cmd for $os)" -Status 'n/a' -Note "UnixCmdTable 에 $CmdKey/$os 매핑 없음")
    }

    $r = Invoke-Remote -Remote $remote -Command $cmd -TimeoutSec $TimeoutSec
    if (-not $r.ok) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd $cmd -Status 'error' `
            -Value @{ os = $os; stderr = $r.stderr } -Raw $r.stdout)
    }

    $value = [ordered]@{ os = $os }
    $status = 'ok'
    $threshold = $Threshold

    if ($Parser) {
        try {
            $parsed = & $Parser $r.stdout $os $remote
            if ($parsed -is [System.Collections.IDictionary]) {
                foreach ($k in $parsed.Keys) {
                    if ($k -eq '__status')    { $status = [string]$parsed[$k]; continue }
                    if ($k -eq '__threshold') { $threshold = $parsed[$k];      continue }
                    $value[$k] = $parsed[$k]
                }
            }
        } catch {
            $value['parse_error'] = $_.Exception.Message
            $status = 'error'
        }
    }

    if (-not $value.Contains('output')) {
        $out = @($r.stdout -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ }) | Select-Object -First 20
        $value['output'] = @($out)
    }

    $args = @{
        Id = $Id; Label = $Label; Category = $Category; Cmd = $cmd
        Value = $value; Status = $status; Raw = $r.stdout
    }
    if ($threshold) { $args['Threshold'] = $threshold }
    return (New-CheckResult @args)
}
