param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$dir = $Config.paths.gws_store_dir
if (-not (Test-Path $dir)) {
    return (New-CheckResult `
        -Id 'gis.gws.store_size' `
        -Label 'GWS store 폴더 용량' `
        -Category 'gis' `
        -Value @{ path = $dir; exists = $false } `
        -Status 'n/a')
}

$timed = Invoke-Timed {
    $bytes = (Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue |
              Measure-Object -Sum Length).Sum
    $files = (Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue).Count
    @{ bytes = $bytes; files = $files }
}
$gb = [math]::Round($timed.Result.bytes / 1GB, 2)

$th = $Config.thresholds.'gws.store_gb'
$status = Resolve-Status -Value $gb -Threshold $th

New-CheckResult `
    -Id 'gis.gws.store_size' `
    -Label 'GWS store 폴더 용량' `
    -Category 'gis' `
    -Cmd "Get-ChildItem '$dir' -Recurse | Measure Length" `
    -Value @{ path = $dir; total_gb = $gb; files = $timed.Result.files } `
    -Threshold $th `
    -Status $status `
    -TookMs $timed.Ms
