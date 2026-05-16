param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# 1) PS 5.1+: Get-LocalUser
# 2) PS 4.0 / 2012 R2: Win32_UserAccount WMI (net user 파싱보다 안정적)
# 3) 최후 폴백: net user 파싱 — 구분선('---...') / 한글 헤더가 사용자명으로 잡히지 않도록 필터.
$users = @()
$method = 'unknown'

try {
    $users = Get-LocalUser -ErrorAction Stop |
        Where-Object Enabled |
        Select-Object Name, LastLogon, PasswordLastSet
    $method = 'Get-LocalUser'
} catch {
    try {
        $users = @(Get-CimInstance -ClassName Win32_UserAccount `
            -Filter "LocalAccount=True AND Disabled=False" -ErrorAction Stop |
            Select-Object Name, SID)
        $method = 'Win32_UserAccount'
    } catch {
        $rawUsers = (& net user) -join "`n"
        $found = [regex]::Matches($rawUsers, '(?m)^\s*([A-Za-z0-9_\-\$]+)')
        foreach ($m in $found) {
            $n = $m.Groups[1].Value
            if ($n -match '^-+$')                          { continue }   # 구분선
            if ($n -in 'User','accounts','for','The')      { continue }   # 영문 헤더 단어
            $users += @{ Name = $n }
        }
        $method = 'net user'
    }
}

$count = @($users).Count
New-CheckResult `
    -Id 'ap.security.users' `
    -Label '사용자 계정 (활성)' `
    -Category 'security' `
    -Cmd 'Get-LocalUser / Win32_UserAccount / net user' `
    -Value @{
        enabled = $count
        users   = @($users | Select-Object -First 30)
        method  = $method
    } `
    -Status 'ok'
