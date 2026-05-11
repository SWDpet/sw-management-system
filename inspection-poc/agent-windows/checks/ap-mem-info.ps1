param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$total = (Get-CimInstance Win32_PhysicalMemory | Measure-Object -Sum Capacity).Sum
$gb = [math]::Round($total / 1GB, 1)

New-CheckResult `
    -Id 'ap.hw.memory' `
    -Label 'Memory 구성' `
    -Category 'hw_config' `
    -Cmd 'Get-CimInstance Win32_PhysicalMemory' `
    -Value @{ installed_gb = $gb } `
    -Status 'ok'
