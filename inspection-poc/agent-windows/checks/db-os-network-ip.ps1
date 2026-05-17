param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# IP/인터페이스 정보 — 정규화 우선순위 낮음. raw 결과의 inet 라인만 추출해서 보여줌.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $ips = New-Object System.Collections.ArrayList
    foreach ($line in ($stdout -split "`r?`n")) {
        $t = $line.Trim()
        if (-not $t) { continue }
        # IPv4: 'inet 10.1.2.3 ...' (loopback 127 제외)
        if ($t -match 'inet\s+([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})') {
            $ip = $matches[1]
            if ($ip -notmatch '^127\.') { [void]$ips.Add($ip) }
        }
    }
    $h['ipv4'] = @($ips | Select-Object -Unique)
    return $h
}

Invoke-DbOsCheck -Id 'db.os.network_ip' -Label 'DB OS IP/인터페이스' `
    -CmdKey 'network_ip' -Config $Config -Parser $parser
