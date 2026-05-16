param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')
. (Join-Path $PSScriptRoot '..\lib\DPAPI.ps1')
. (Join-Path $PSScriptRoot '..\lib\Ssh.ps1')
. (Join-Path $PSScriptRoot '..\lib\Telnet.ps1')   # v2 — Invoke-Remote 라우터를 같은 scope 에 import

$dir    = $Config.paths.gss_log_dir
$retain = $Config.purge.log_retain_days
$dryRun = $Config.purge.dry_run

# 1) 로컬 경로 우선
if (Test-Path $dir) {
    $cutoff = (Get-Date).AddDays(-$retain)
    $old = Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object LastWriteTime -LT $cutoff
    $bytes = ($old | Measure-Object -Sum Length).Sum
    $count = $old.Count

    if (-not $dryRun -and $count -gt 0) {
        $old | Remove-Item -Force -ErrorAction SilentlyContinue
    }

    return (New-CheckResult `
        -Id 'gis.gss.log_purge' `
        -Label ("GSS 로그 정리 ({0}일 경과)" -f $retain) `
        -Category 'gis' `
        -Cmd ("Remove-Item {0} LastWriteTime<{1}" -f $dir, $cutoff.ToString('yyyy-MM-dd')) `
        -Value @{
            path = $dir
            source = 'local'
            retain_days = $retain
            dry_run = $dryRun
            purged_count = $count
            purged_mb = [math]::Round(($bytes/1MB), 2)
        } `
        -Status 'ok')
}

# 2) 로컬 미존재 → 원격 Unix(SSH/Telnet) 탐색 (FR-2)
#    v2: unix_db (setup wizard) 우선, v1 호환 gis_unix fallback
$remote = $null
if ($Config.PSObject.Properties['remotes'] -and $Config.remotes) {
    if ($Config.remotes.PSObject.Properties['unix_db']) { $remote = $Config.remotes.unix_db }
    elseif ($Config.remotes.PSObject.Properties['gis_unix']) { $remote = $Config.remotes.gis_unix }
}
if (-not $remote -or -not $remote.enabled) {
    return (New-CheckResult `
        -Id 'gis.gss.log_purge' `
        -Label 'GSS 로그 파일 정리' `
        -Category 'gis' `
        -Method 'auto' `
        -Cmd ("path not found: {0} (remote disabled)" -f $dir) `
        -Value @{ path = $dir; exists = $false; source='local' } `
        -Status 'n/a' `
        -Note ("local path missing and remotes.gis_unix not enabled"))
}

# 2-1) GSS 경로 자동 탐지 → 디렉토리 후보 결정
try {
    $probe = Find-GssPathOnRemote -Remote $remote
} catch {
    return (New-CheckResult `
        -Id 'gis.gss.log_purge' `
        -Label 'GSS 로그 파일 정리 (원격)' `
        -Category 'gis' `
        -Cmd "ssh ps -ef|grep GSS" `
        -Value @{ source='remote-ssh'; error="$_" } `
        -Status 'error')
}

$remoteLogDir = $null
# 우선순위: config hint → 탐지된 경로 + /logs → 탐지된 경로
if ($remote.PSObject.Properties['gss_log_dir_hint'] -and $remote.gss_log_dir_hint) {
    $remoteLogDir = $remote.gss_log_dir_hint
} elseif ($probe.paths.Count -gt 0) {
    $base = $probe.paths[0]
    # 첫 디렉토리 후보의 부모로 한 단계 올라가서 logs/ 추정
    $parent = $base
    if ($parent -notmatch '/logs$') {
        $maybe = "$parent/logs"
        $remoteLogDir = $maybe
    } else {
        $remoteLogDir = $parent
    }
}

if (-not $remoteLogDir) {
    return (New-CheckResult `
        -Id 'gis.gss.log_purge' `
        -Label 'GSS 로그 파일 정리 (원격)' `
        -Category 'gis' `
        -Cmd "ssh ps -ef|grep GSS" `
        -Value @{
            source = 'remote-ssh'
            probed_paths = $probe.paths
            log_dir = $null
        } `
        -Status 'crit' `
        -Note 'GSS 프로세스 경로를 추정할 수 없습니다. config.remotes.gis_unix.gss_log_dir_hint 지정 필요')
}

# 2-2) 원격 디렉토리에서 mtime 기준 파일 개수/크기 산출 (read-only)
#   find -mtime +N -type f → 합산. byte 단위는 du 와 ls 차이 회피 위해 stat 사용.
$cmd = "find '$remoteLogDir' -type f -mtime +$retain -print 2>/dev/null | wc -l; find '$remoteLogDir' -type f -mtime +$retain -exec ls -l {} \; 2>/dev/null | awk '{s+=\$5} END{print s+0}'"
# v2: Invoke-Remote 라우터 우선 (Telnet/SSH 자동). 미로드 시 Invoke-RemoteSsh fallback.
if (Get-Command Invoke-Remote -ErrorAction SilentlyContinue) {
    $r = Invoke-Remote -Remote $remote -Command $cmd -TimeoutSec 60
} else {
    $r = Invoke-RemoteSsh -Remote $remote -Command $cmd -TimeoutSec 60
}
$lines = ($r.stdout -split "`n") | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
# Telnet 출력은 프롬프트/명령에코/sentinel 노이즈 포함 — 숫자만 남긴 줄을 우선 선택
$numeric = @($lines | Where-Object { $_ -match '^\d+$' })

$count = 0; $bytes = 0
if ($numeric.Count -ge 1) { [void][int]::TryParse($numeric[0], [ref]$count) }
if ($numeric.Count -ge 2) { [void][int64]::TryParse($numeric[1], [ref]$bytes) }

return (New-CheckResult `
    -Id 'gis.gss.log_purge' `
    -Label ("GSS 로그 정리 ({0}일 경과, 원격)" -f $retain) `
    -Category 'gis' `
    -Cmd ("ssh {0}@{1} find {2} -mtime +{3}" -f $remote.user, $remote.host, $remoteLogDir, $retain) `
    -Value @{
        path = $remoteLogDir
        source = 'remote-ssh'
        remote_host = $remote.host
        retain_days = $retain
        dry_run = $true
        purged_count = $count
        purged_mb = [math]::Round(($bytes/1MB), 2)
        note = '원격은 dry-run 강제 (PoC). 실제 삭제는 Unix 에이전트에서 수행'
    } `
    -Status 'ok' `
    -Raw $r.stdout)
