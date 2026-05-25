param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# inspect_template GIS sort 10 'GWS catalina ERROR 카운트' — P10 카드용.
# GWS 는 Windows 로컬 (agent-windows 가 같은 서버). 원격 분기 불필요.
$dir  = $Config.paths.gws_log_dir
$days = 30

if (-not $dir -or -not (Test-Path $dir)) {
    return (New-CheckResult -Id 'gis.gws.catalina_err' -Label 'GWS catalina ERROR 카운트' `
        -Category 'gis' -Method 'auto' -Status 'n/a' `
        -Value @{ path=$dir; exists=$false } `
        -Note ("GWS 로그 경로 미존재: {0}" -f $dir))
}

$cutoff = (Get-Date).AddDays(-$days)
$files = Get-ChildItem -Path $dir -Filter 'catalina*' -ErrorAction SilentlyContinue |
    Where-Object LastWriteTime -GE $cutoff

$count = 0
$sample = @()
foreach ($f in $files) {
    $matches = Select-String -Path $f.FullName -Pattern 'ERROR' -ErrorAction SilentlyContinue
    $count += $matches.Count
    if ($sample.Count -lt 5 -and $matches.Count -gt 0) {
        $sample += @($matches | Select-Object -First (5 - $sample.Count) | ForEach-Object { $_.Line.Trim() })
    }
}

$status = if     ($count -ge 100) { 'crit' }
          elseif ($count -ge 10)  { 'warn' }
          else                     { 'ok'   }

New-CheckResult `
    -Id 'gis.gws.catalina_err' `
    -Label ("GWS catalina ERROR 카운트 ({0}일)" -f $days) `
    -Category 'gis' `
    -Cmd ("Select-String ERROR catalina* in {0} (last {1}d)" -f $dir, $days) `
    -Value @{
        count          = $count
        path           = $dir
        files_scanned  = $files.Count
        sample         = $sample
        window_days    = $days
    } `
    -Status $status
