param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')
. (Join-Path $PSScriptRoot '..\lib\Ssh.ps1')
. (Join-Path $PSScriptRoot '..\lib\Telnet.ps1')
. (Join-Path $PSScriptRoot '..\lib\GssDetect.ps1')

# inspect_template GIS sort 8 'GSS 30일 ERROR 카운트' — P10 카드용.
# GSS catalina.out 30일 누적 ERROR 카운트. 로컬 우선, 미존재 시 원격 SSH/Telnet.
$dir   = $Config.paths.gss_log_dir
$days  = 30

function Resolve-ErrorStatus {
    param([int]$cnt)
    if     ($cnt -ge 100) { 'crit' }
    elseif ($cnt -ge 10)  { 'warn' }
    else                   { 'ok' }
}

# 로컬 catalina.out 검사
if (Test-Path $dir) {
    $cutoff = (Get-Date).AddDays(-$days)
    $catalinaFiles = Get-ChildItem -Path $dir -Filter 'catalina*.log' -ErrorAction SilentlyContinue |
        Where-Object LastWriteTime -GE $cutoff
    if (-not $catalinaFiles) {
        $catalinaFiles = Get-ChildItem -Path $dir -Filter 'catalina*.out' -ErrorAction SilentlyContinue |
            Where-Object LastWriteTime -GE $cutoff
    }
    $count = 0
    $sample = @()
    foreach ($f in $catalinaFiles) {
        $matches = Select-String -Path $f.FullName -Pattern 'ERROR' -ErrorAction SilentlyContinue
        $count += $matches.Count
        if ($sample.Count -lt 5 -and $matches.Count -gt 0) {
            $sample += @($matches | Select-Object -First (5 - $sample.Count) | ForEach-Object { $_.Line.Trim() })
        }
    }
    return (New-CheckResult `
        -Id 'gis.gss.err_30days' `
        -Label ("GSS 30일 ERROR 카운트 ({0}일)" -f $days) `
        -Category 'gis' `
        -Cmd ("Select-String ERROR catalina.* (last {0}d local)" -f $days) `
        -Value @{
            count      = $count
            source     = 'local'
            files_scanned = $catalinaFiles.Count
            sample     = $sample
            window_days = $days
        } `
        -Status (Resolve-ErrorStatus $count))
}

# 원격 (Unix GSS) — find + grep
$remote = $null
if ($Config.PSObject.Properties['remotes'] -and $Config.remotes) {
    if ($Config.remotes.PSObject.Properties['unix_db'])  { $remote = $Config.remotes.unix_db }
    elseif ($Config.remotes.PSObject.Properties['gis_unix']) { $remote = $Config.remotes.gis_unix }
}
if (-not $remote -or -not $remote.enabled) {
    return (New-CheckResult `
        -Id 'gis.gss.err_30days' -Label 'GSS 30일 ERROR 카운트' -Category 'gis' `
        -Method 'auto' -Cmd ("path not found: {0} (remote disabled)" -f $dir) `
        -Value @{ path=$dir; exists=$false } `
        -Status 'n/a' -Note 'local 미존재 + remote 미설정')
}

try {
    $probe = Find-GssPathOnRemote -Remote $remote
} catch {
    return (New-CheckResult -Id 'gis.gss.err_30days' -Label 'GSS 30일 ERROR (원격)' `
        -Category 'gis' -Status 'error' -Note "$_")
}
$logDir = $null
if ($remote.PSObject.Properties['gss_log_dir_hint'] -and $remote.gss_log_dir_hint) {
    $logDir = $remote.gss_log_dir_hint
} elseif ($probe.paths.Count -gt 0) {
    $logDir = if ($probe.paths[0] -match '/logs$') { $probe.paths[0] } else { ($probe.paths[0] + '/logs') }
}
if (-not $logDir) {
    return (New-CheckResult -Id 'gis.gss.err_30days' -Label 'GSS 30일 ERROR (원격)' `
        -Category 'gis' -Status 'crit' `
        -Value @{ probed_paths=$probe.paths } `
        -Note 'GSS 로그 경로 추정 실패')
}

$cmd = "find '$logDir' -type f -name 'catalina*' -mtime -$days -exec grep -c -i 'ERROR' {} + 2>/dev/null"
$r = if (Get-Command Invoke-Remote -ErrorAction SilentlyContinue) { Invoke-Remote -Remote $remote -Command $cmd -TimeoutSec 60 } `
     else { Invoke-RemoteSsh -Remote $remote -Command $cmd -TimeoutSec 60 }

# grep -c 출력: "path:count" per line — 합산
$count = 0
foreach ($line in ($r.stdout -split "`r?`n")) {
    if ($line -match ':(\d+)$') { $count += [int]$matches[1] }
    elseif ($line -match '^\d+$') { $count += [int]$line }
}

return (New-CheckResult -Id 'gis.gss.err_30days' -Label ("GSS 30일 ERROR 카운트 ({0}일, 원격)" -f $days) `
    -Category 'gis' -Cmd $cmd `
    -Value @{ count=$count; source='remote-ssh'; remote_host=$remote.host; log_dir=$logDir; window_days=$days } `
    -Status (Resolve-ErrorStatus $count) -Raw $r.stdout)
