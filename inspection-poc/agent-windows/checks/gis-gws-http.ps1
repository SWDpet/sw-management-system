param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$url = $Config.paths.gws_url
$code = 0
$err = $null
try {
    $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    $code = [int]$resp.StatusCode
} catch [System.Net.WebException] {
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
    } else {
        $err = $_.Exception.Message
    }
} catch {
    $err = $_.Exception.Message
}

$status = if ($code -ge 200 -and $code -lt 400) { 'ok' }
          elseif ($code -ge 400)                { 'warn' }
          else                                  { 'crit' }

$value = @{ url = $url; http_status = $code }
if ($err) { $value.error = $err }

New-CheckResult `
    -Id 'gis.gws.http' `
    -Label 'GWS HTTP 응답' `
    -Category 'gis' `
    -Cmd "Invoke-WebRequest $url" `
    -Value $value `
    -Status $status
