param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$dir = $Config.paths.gss_log_dir
$retain = $Config.purge.log_retain_days
$dryRun = $Config.purge.dry_run

if (-not (Test-Path $dir)) {
    return (New-CheckResult `
        -Id 'gis.gss.log_purge' `
        -Label 'GSS 로그 파일 정리' `
        -Category 'gis' `
        -Method 'auto' `
        -Cmd ("dir not found: {0}" -f $dir) `
        -Value @{ path = $dir; exists = $false } `
        -Status 'n/a' `
        -Note ("path missing: {0}" -f $dir))
}

$cutoff = (Get-Date).AddDays(-$retain)
$old = Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object LastWriteTime -LT $cutoff
$bytes = ($old | Measure-Object -Sum Length).Sum
$count = $old.Count

if (-not $dryRun -and $count -gt 0) {
    $old | Remove-Item -Force -ErrorAction SilentlyContinue
}

New-CheckResult `
    -Id 'gis.gss.log_purge' `
    -Label ("GSS 로그 정리 ({0}일 경과)" -f $retain) `
    -Category 'gis' `
    -Cmd ("Remove-Item {0} LastWriteTime<{1}" -f $dir, $cutoff.ToString('yyyy-MM-dd')) `
    -Value @{
        path = $dir
        retain_days = $retain
        dry_run = $dryRun
        purged_count = $count
        purged_mb = [math]::Round(($bytes/1MB), 2)
    } `
    -Status 'ok'
