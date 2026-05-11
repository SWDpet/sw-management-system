param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# Get-LocalUser는 2012R2에서 PS 5.1 + 일부 패치 필요. 폴백: net user
$users = @()
try {
    $users = Get-LocalUser -ErrorAction Stop |
        Where-Object Enabled |
        Select-Object Name, LastLogon, PasswordLastSet
} catch {
    # 폴백: net user 파싱
    $rawUsers = (& net user) -join "`n"
    $matches = [regex]::Matches($rawUsers, '(?m)^\s*([A-Za-z0-9_\-\$]+)')
    foreach ($m in $matches) {
        $n = $m.Groups[1].Value
        if ($n -in 'User','accounts','for','---','The','명령은') { continue }
        $users += @{ Name = $n }
    }
}

$count = @($users).Count
New-CheckResult `
    -Id 'ap.security.users' `
    -Label '사용자 계정 (활성)' `
    -Category 'security' `
    -Cmd 'Get-LocalUser / net user' `
    -Value @{ enabled = $count; users = @($users | Select-Object -First 30) } `
    -Status 'ok'
