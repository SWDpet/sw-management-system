param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$svcName = 'GeoWebService64'
$svc = Get-Service -Name $svcName -ErrorAction SilentlyContinue
$status = 'crit'
$value = @{ service = $svcName; status = 'NotInstalled' }
if ($svc) {
    $value.status = $svc.Status.ToString()
    $value.start_type = $svc.StartType.ToString()
    $status = if ($svc.Status -eq 'Running') { 'ok' } else { 'crit' }
}

New-CheckResult `
    -Id 'gis.gws.running' `
    -Label 'GWS 서비스 구동' `
    -Category 'gis' `
    -Cmd "Get-Service $svcName" `
    -Value $value `
    -Status $status
