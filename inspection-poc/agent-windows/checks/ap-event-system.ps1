param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$since = (Get-Date).AddDays(-30)
$entries = @()
try {
    $entries = Get-WinEvent -FilterHashtable @{
        LogName   = 'System'
        Level     = 1,2,3   # Critical, Error, Warning
        StartTime = $since
    } -MaxEvents 200 -ErrorAction Stop
} catch {
    # 로그 비어있거나 권한 부족
}

$errs = @($entries | Where-Object { $_.Level -le 2 }).Count
$th = $Config.thresholds.'event.error_count'
$status = Resolve-Status -Value $errs -Threshold $th

$top = $entries | Select-Object -First 5 | ForEach-Object {
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
    -Cmd 'Get-WinEvent System Level 1,2 (지난30일)' `
    -Value @{ since_days = 30; error_count = $errs; recent = $top } `
    -Threshold $th `
    -Status $status
