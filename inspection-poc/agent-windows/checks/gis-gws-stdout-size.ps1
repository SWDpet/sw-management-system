param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# inspect_template GIS sort 11 'GWS stdout 로그 크기 (MB)' — P10 카드용.
# geowebservice64-stdout*.log 파일 크기 합산. 로그 로테이션 미설정 시 단일 파일이 큼.
$dir = $Config.paths.gws_log_dir
$th  = if ($Config.thresholds.PSObject.Properties['gws.stdout_mb']) { $Config.thresholds.'gws.stdout_mb' } else { $null }

if (-not $dir -or -not (Test-Path $dir)) {
    return (New-CheckResult -Id 'gis.gws.stdout_log_size' -Label 'GWS stdout 로그 크기' `
        -Category 'gis' -Method 'auto' -Status 'n/a' `
        -Value @{ path=$dir; exists=$false } `
        -Note ("GWS 로그 경로 미존재: {0}" -f $dir))
}

$files = Get-ChildItem -Path $dir -Filter 'geowebservice64-stdout*.log' -ErrorAction SilentlyContinue
if (-not $files -or $files.Count -eq 0) {
    # 대체 패턴 — Windows Service 가 'stdout' 또는 'service-stdout' 등으로 떨굴 수 있음
    $files = Get-ChildItem -Path $dir -Filter '*stdout*.log' -ErrorAction SilentlyContinue
}

$totalBytes = ($files | Measure-Object -Sum Length).Sum
$totalMb = if ($totalBytes) { [math]::Round($totalBytes / 1MB, 2) } else { 0 }
$largest = $files | Sort-Object Length -Descending | Select-Object -First 1

$status = 'ok'
if ($th) {
    $status = if     ($totalMb -ge $th.crit) { 'crit' }
              elseif ($totalMb -ge $th.warn) { 'warn' }
              else                            { 'ok' }
} else {
    # 기본 임계: 500MB warn, 2GB crit (로테이션 미설정 의심)
    $status = if     ($totalMb -ge 2048) { 'crit' }
              elseif ($totalMb -ge 500)  { 'warn' }
              else                        { 'ok' }
}

New-CheckResult `
    -Id 'gis.gws.stdout_log_size' `
    -Label 'GWS stdout 로그 크기 (MB)' `
    -Category 'gis' `
    -Cmd ("Get-ChildItem {0} -Filter geowebservice64-stdout*.log | Measure Length -Sum" -f $dir) `
    -Value @{
        total_mb       = $totalMb
        file_count     = $files.Count
        largest_name   = if ($largest) { $largest.Name } else { $null }
        largest_mb     = if ($largest) { [math]::Round($largest.Length / 1MB, 2) } else { 0 }
        path           = $dir
        rotation_hint  = if ($files.Count -le 1 -and $totalMb -gt 100) { '로그 로테이션 미설정 의심' } else { 'ok' }
    } `
    -Status $status `
    -Threshold (@{ warn = 500; crit = 2048 })
