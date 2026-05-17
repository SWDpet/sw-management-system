param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# uptime — "10:23am up 31 days, 4:12, 2 users, load average: 0.12, 0.08, 0.05"
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $line = _LastNonEmptyLine $stdout
    if (-not $line) { return $h }
    $h['raw'] = $line
    # up duration (days)
    $m = [regex]::Match($line, 'up\s+(?:(\d+)\s+day[s]?)?')
    if ($m.Success -and $m.Groups[1].Value) {
        $h['uptime_days'] = [int]$m.Groups[1].Value
    }
    # load average
    $m = [regex]::Match($line, 'load\s+average[s]?:\s+([\d.]+),?\s+([\d.]+),?\s+([\d.]+)')
    if ($m.Success) {
        $h['load_1m']  = [double]$m.Groups[1].Value
        $h['load_5m']  = [double]$m.Groups[2].Value
        $h['load_15m'] = [double]$m.Groups[3].Value
    }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.uptime' -Label 'DB OS Uptime / Load' `
    -CmdKey 'uptime' -Config $Config -Parser $parser
