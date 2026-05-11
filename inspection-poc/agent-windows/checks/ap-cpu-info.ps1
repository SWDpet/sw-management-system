param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$cpus = Get-CimInstance Win32_Processor
$cores = ($cpus | Measure-Object -Sum NumberOfCores).Sum
$clock = [math]::Round((($cpus | Measure-Object -Average MaxClockSpeed).Average) / 1000, 2)
$name  = ($cpus | Select-Object -First 1).Name

New-CheckResult `
    -Id 'ap.hw.cpu' `
    -Label 'CPU 구성' `
    -Category 'hw_config' `
    -Method 'auto' `
    -Cmd 'Get-CimInstance Win32_Processor' `
    -Value @{ cores = $cores; clock_ghz = $clock; name = $name } `
    -Status 'ok'
