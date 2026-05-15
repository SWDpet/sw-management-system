param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')
. (Join-Path $PSScriptRoot '..\lib\Ssh.ps1')

# 1) 로컬 윈도우에서 우선 탐지
$proc = Get-Process -ErrorAction SilentlyContinue |
    Where-Object {
        try {
            $_.Path -like '*GeoNURIS_Spatial_Server*' -or
            $_.MainWindowTitle -like '*GSS*' -or
            $_.ProcessName -match 'GSS'
        } catch { $false }
    }
$procCount = @($proc).Count

if ($procCount -gt 0) {
    return (New-CheckResult `
        -Id 'gis.gss.running' `
        -Label 'GSS 구동 상태' `
        -Category 'gis' `
        -Cmd 'Get-Process | ?{ $_.Path -like *GeoNURIS_Spatial_Server* }' `
        -Value @{
            running = $true
            pids = @($proc | ForEach-Object { $_.Id })
            process_count = $procCount
            source = 'local'
        } `
        -Status 'ok')
}

# 2) 로컬에 없으면 → 원격 Unix(SSH)로 ps -ef | grep GSS
$remote = $null
if ($Config.PSObject.Properties['remotes'] -and $Config.remotes.PSObject.Properties['gis_unix']) {
    $remote = $Config.remotes.gis_unix
}
if (-not $remote -or -not $remote.enabled) {
    return (New-CheckResult `
        -Id 'gis.gss.running' `
        -Label 'GSS 구동 상태' `
        -Category 'gis' `
        -Cmd 'Get-Process (local) | ps -ef|grep GSS (remote, disabled)' `
        -Value @{
            running = $false
            source  = 'local'
            note    = 'no GSS process on Windows; remotes.gis_unix not enabled'
        } `
        -Status 'crit' `
        -Note 'GSS not detected locally and remote SSH probe disabled')
}

try {
    $info = Find-GssPathOnRemote -Remote $remote
} catch {
    return (New-CheckResult `
        -Id 'gis.gss.running' `
        -Label 'GSS 구동 상태' `
        -Category 'gis' `
        -Cmd ("ssh {0}@{1} 'ps -ef|grep GSS'" -f $remote.user, $remote.host) `
        -Value @{ running=$false; source='remote-ssh'; error=("$_") } `
        -Status 'error' `
        -Note "SSH probe failed: $_")
}

$status = if ($info.running) { 'ok' } else { 'crit' }
$value = @{
    running       = $info.running
    pids          = $info.pids
    process_count = $info.pids.Count
    source        = 'remote-ssh'
    remote_host   = $remote.host
    remote_user   = $remote.user
    discovered_paths = $info.paths
}
return (New-CheckResult `
    -Id 'gis.gss.running' `
    -Label 'GSS 구동 상태 (원격)' `
    -Category 'gis' `
    -Cmd ("ssh {0}@{1} 'ps -ef|grep GSS'" -f $remote.user, $remote.host) `
    -Value $value `
    -Status $status `
    -Raw $info.raw)
