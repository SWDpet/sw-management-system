param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$since = (Get-Date).AddDays(-30)

# 운영 신호가 아닌 잡음 이벤트는 카운트에서 제외.
# Schannel 36887/36888: 원격 끝점과의 TLS 경고 alert — 클라이언트측 인증서/프로토콜 협상 문제로 자주 발생, AP 자체 장애 아님.
$defaultExcludes = @(
    @{ provider = 'Schannel'; id = 36887 },
    @{ provider = 'Schannel'; id = 36888 }
)
$excludes = $defaultExcludes
if ($Config.event_excludes) {
    $excludes = @($Config.event_excludes | ForEach-Object {
        @{ provider = [string]$_.provider; id = [int]$_.id }
    })
}

$entries = @()
try {
    # MaxEvents 를 1000 까지 끌어올림 — 노이즈 필터 후에도 실제 에러가 누락되지 않도록.
    $entries = Get-WinEvent -FilterHashtable @{
        LogName   = 'System'
        Level     = 1,2,3
        StartTime = $since
    } -MaxEvents 1000 -ErrorAction Stop
} catch {
    # 로그 비어있거나 권한 부족
}

function _IsExcluded($e, $list) {
    foreach ($p in $list) {
        if ($e.ProviderName -eq $p.provider -and [int]$e.Id -eq $p.id) { return $true }
    }
    return $false
}

$allErrs       = @($entries | Where-Object { $_.Level -le 2 })
$signalErrs    = @($allErrs | Where-Object { -not (_IsExcluded $_ $excludes) })
$excludedErrs  = @($allErrs | Where-Object { _IsExcluded $_ $excludes })

$errs = $signalErrs.Count
$th = $Config.thresholds.'event.error_count'
$status = Resolve-Status -Value $errs -Threshold $th

$top = $signalErrs | Select-Object -First 5 | ForEach-Object {
    @{
        time = $_.TimeCreated.ToString('s')
        id   = $_.Id
        provider = $_.ProviderName
        message = ($_.Message -split "`n")[0] -replace '\s+',' '
    }
}

New-CheckResult `
    -Id 'ap.log.system_err' `
    -Label 'Event Log (System) 에러' `
    -Category 'log' `
    -Cmd 'Get-WinEvent System Level 1,2 (지난30일, noise 제외)' `
    -Value @{
        since_days     = 30
        error_count    = $errs
        excluded_count = $excludedErrs.Count
        excluded_rules = $excludes
        recent         = $top
    } `
    -Threshold $th `
    -Status $status
