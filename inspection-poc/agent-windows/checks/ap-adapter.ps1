param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$adapters = Get-NetAdapter -ErrorAction SilentlyContinue |
    Select-Object Name, Status, LinkSpeed, MacAddress
$up = @($adapters | Where-Object Status -EQ 'Up').Count
$total = @($adapters).Count

$status = if ($up -ge 1) { 'ok' } else { 'warn' }
New-CheckResult `
    -Id 'ap.hw.adapter' `
    -Label 'Adapter (연결상태)' `
    -Category 'hw_config' `
    -Cmd 'Get-NetAdapter' `
    -Value @{ total = $total; up = $up; adapters = @($adapters) } `
    -Status $status
