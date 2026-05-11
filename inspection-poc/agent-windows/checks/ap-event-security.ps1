param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$since = (Get-Date).AddDays(-30)
$entries = @()
$noteText = $null
try {
    $entries = Get-WinEvent -FilterHashtable @{
        LogName   = 'Security'
        Level     = 1,2,3
        StartTime = $since
    } -MaxEvents 200 -ErrorAction Stop
} catch {
    $noteText = ("Security 로그 접근 불가: {0}" -f $_.Exception.Message)
}

$errs = @($entries | Where-Object { $_.Level -le 2 }).Count
$status = if ($noteText) { 'n/a' } elseif ($errs -gt 0) { 'warn' } else { 'ok' }

$args = @{
    Id = 'ap.log.security_err'
    Label = 'Event Log (Security) 에러'
    Category = 'log'
    Cmd = 'Get-WinEvent Security Level 1,2'
    Value = @{ since_days = 30; error_count = $errs }
    Status = $status
}
if ($noteText) { $args.Note = $noteText }
New-CheckResult @args
