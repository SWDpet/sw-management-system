param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$gw = $null
try {
    $gw = (Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction Stop |
           Sort-Object -Property RouteMetric | Select-Object -First 1).NextHop
} catch { }

$routeCount = 0
try { $routeCount = (Get-NetRoute -ErrorAction Stop).Count } catch { }

$adapterErrors = 0
try {
    $stats = Get-NetAdapterStatistics -ErrorAction Stop
    $adapterErrors = ($stats | Measure-Object -Sum OutboundDiscardedPackets,InboundDiscardedPackets).Sum
} catch { }

$status = if ($gw) { 'ok' } else { 'warn' }
New-CheckResult `
    -Id 'ap.net.routes' `
    -Label 'Network 라우팅' `
    -Category 'network' `
    -Cmd 'Get-NetRoute / Get-NetAdapterStatistics' `
    -Value @{ default_gw = $gw; routes = $routeCount; discarded = $adapterErrors } `
    -Status $status
