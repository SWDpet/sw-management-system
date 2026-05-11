param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$os = Get-CimInstance Win32_OperatingSystem
$totalKb = [double]$os.TotalVisibleMemorySize
$freeKb  = [double]$os.FreePhysicalMemory
$pct = [math]::Round((1 - $freeKb / $totalKb) * 100, 1)
$usedGb = [math]::Round(($totalKb - $freeKb) / 1MB, 2)
$totalGb = [math]::Round($totalKb / 1MB, 2)

$th = $Config.thresholds.'perf.mem_pct'
$status = Resolve-Status -Value $pct -Threshold $th

New-CheckResult `
    -Id 'ap.perf.mem_pct' `
    -Label 'Memory 사용률' `
    -Category 'perf' `
    -Cmd 'Win32_OperatingSystem.FreePhysicalMemory' `
    -Value @{ pct = $pct; used_gb = $usedGb; total_gb = $totalGb } `
    -Threshold $th `
    -Status $status
