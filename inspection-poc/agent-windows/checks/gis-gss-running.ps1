param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$proc = Get-Process -ErrorAction SilentlyContinue |
    Where-Object {
        try {
            $_.Path -like '*GeoNURIS_Spatial_Server*' -or
            $_.MainWindowTitle -like '*GSS*' -or
            $_.ProcessName -match 'GSS'
        } catch { $false }
    }

$procCount = @($proc).Count
$running = ($procCount -gt 0)
$status = if ($running) { 'ok' } else { 'crit' }
$value = @{
    running = $running
    pids    = if ($running) { @($proc | ForEach-Object { $_.Id }) } else { @() }
    process_count = $procCount
}
New-CheckResult `
    -Id 'gis.gss.running' `
    -Label 'GSS 구동 상태' `
    -Category 'gis' `
    -Cmd 'Get-Process | ?{ $_.Path -like *GeoNURIS_Spatial_Server* }' `
    -Value $value `
    -Status $status
