param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# who — 현재 로그인 세션 (점검 시점 활성 사용자).
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $users = New-Object System.Collections.ArrayList
    foreach ($line in ($stdout -split "`r?`n")) {
        $t = $line.Trim()
        if (-not $t) { continue }
        $cols = $t -split '\s+'
        if ($cols.Count -ge 1) { [void]$users.Add($cols[0]) }
    }
    $h['active_users']  = @($users | Select-Object -Unique)
    $h['session_count'] = $users.Count
    return $h
}

Invoke-DbOsCheck -Id 'db.os.users' -Label 'DB OS 활성 사용자' `
    -CmdKey 'users' -Config $Config -Parser $parser
