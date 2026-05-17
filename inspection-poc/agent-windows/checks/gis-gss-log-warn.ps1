param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')
. (Join-Path $PSScriptRoot '..\lib\Ssh.ps1')
. (Join-Path $PSScriptRoot '..\lib\Telnet.ps1')
. (Join-Path $PSScriptRoot '..\lib\GssDetect.ps1')

# inspect_template GIS sort 9 'GSS 30일 WARN 카운트' — P10 카드용.
# gis-gss-log-error.ps1 와 동일 구조, 패턴만 'WARN' 으로.
$dir  = $Config.paths.gss_log_dir
$days = 30

function Resolve-WarnStatus {
    param([int]$cnt)
    if     ($cnt -ge 500) { 'warn' }
    elseif ($cnt -ge 100) { 'warn' }
    else                   { 'ok' }
}

if (Test-Path $dir) {
    $cutoff = (Get-Date).AddDays(-$days)
    $files = Get-ChildItem -Path $dir -Filter 'catalina*.log' -ErrorAction SilentlyContinue | Where-Object LastWriteTime -GE $cutoff
    if (-not $files) {
        $files = Get-ChildItem -Path $dir -Filter 'catalina*.out' -ErrorAction SilentlyContinue | Where-Object LastWriteTime -GE $cutoff
    }
    $count = 0
    foreach ($f in $files) {
        $matches = Select-String -Path $f.FullName -Pattern 'WARN' -ErrorAction SilentlyContinue
        $count += $matches.Count
    }
    return (New-CheckResult -Id 'gis.gss.warn_30days' -Label ("GSS 30일 WARN 카운트 ({0}일)" -f $days) `
        -Category 'gis' -Cmd ("Select-String WARN catalina.* (last {0}d local)" -f $days) `
        -Value @{ count=$count; source='local'; files_scanned=$files.Count; window_days=$days } `
        -Status (Resolve-WarnStatus $count))
}

# 원격
$remote = $null
if ($Config.PSObject.Properties['remotes'] -and $Config.remotes) {
    if ($Config.remotes.PSObject.Properties['unix_db'])  { $remote = $Config.remotes.unix_db }
    elseif ($Config.remotes.PSObject.Properties['gis_unix']) { $remote = $Config.remotes.gis_unix }
}
if (-not $remote -or -not $remote.enabled) {
    return (New-CheckResult -Id 'gis.gss.warn_30days' -Label 'GSS 30일 WARN 카운트' -Category 'gis' `
        -Method 'auto' -Status 'n/a' -Note 'local 미존재 + remote 미설정')
}

try { $probe = Find-GssPathOnRemote -Remote $remote }
catch { return (New-CheckResult -Id 'gis.gss.warn_30days' -Label 'GSS 30일 WARN (원격)' -Category 'gis' -Status 'error' -Note "$_") }

$logDir = $null
if ($remote.PSObject.Properties['gss_log_dir_hint'] -and $remote.gss_log_dir_hint) {
    $logDir = $remote.gss_log_dir_hint
} elseif ($probe.paths.Count -gt 0) {
    $logDir = if ($probe.paths[0] -match '/logs$') { $probe.paths[0] } else { ($probe.paths[0] + '/logs') }
}
if (-not $logDir) {
    return (New-CheckResult -Id 'gis.gss.warn_30days' -Label 'GSS 30일 WARN (원격)' -Category 'gis' -Status 'crit' -Note '경로 추정 실패')
}

$cmd = "find '$logDir' -type f -name 'catalina*' -mtime -$days -exec grep -c -i 'WARN' {} + 2>/dev/null"
$r = if (Get-Command Invoke-Remote -ErrorAction SilentlyContinue) { Invoke-Remote -Remote $remote -Command $cmd -TimeoutSec 60 } `
     else { Invoke-RemoteSsh -Remote $remote -Command $cmd -TimeoutSec 60 }

$count = 0
foreach ($line in ($r.stdout -split "`r?`n")) {
    if ($line -match ':(\d+)$') { $count += [int]$matches[1] }
    elseif ($line -match '^\d+$') { $count += [int]$line }
}

return (New-CheckResult -Id 'gis.gss.warn_30days' -Label ("GSS 30일 WARN 카운트 ({0}일, 원격)" -f $days) `
    -Category 'gis' -Cmd $cmd `
    -Value @{ count=$count; source='remote-ssh'; log_dir=$logDir; window_days=$days } `
    -Status (Resolve-WarnStatus $count) -Raw $r.stdout)
