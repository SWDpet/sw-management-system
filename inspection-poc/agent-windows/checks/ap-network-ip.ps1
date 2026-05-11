param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$ips = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notmatch '^(127|169\.254)' } |
    Select-Object IPAddress, InterfaceAlias, PrefixLength
$dns = (Get-DnsClientServerAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty ServerAddresses -Unique) -join ','

New-CheckResult `
    -Id 'ap.net.ip' `
    -Label 'IP 설정 정보' `
    -Category 'network' `
    -Cmd 'Get-NetIPAddress / Get-DnsClientServerAddress' `
    -Value @{ ips = @($ips); dns = $dns } `
    -Status 'ok'
