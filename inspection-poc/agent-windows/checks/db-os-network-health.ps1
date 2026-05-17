param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 19~21 — 1 module 3 row (Link / Ping / Collisions).
# manifest.json 의 DB sort 19/20/21 가 모두 db-os-network-health.ps1 로 매핑됨.

# ── sort 19: Link 상태 (netstat -na | head -20)
$linkParser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    $established = @($lines | Where-Object { $_ -match 'ESTABLISHED' }).Count
    $listen      = @($lines | Where-Object { $_ -match 'LISTEN' }).Count
    $timeWait    = @($lines | Where-Object { $_ -match 'TIME_WAIT' }).Count
    $h['established'] = $established
    $h['listen']      = $listen
    $h['time_wait']   = $timeWait
    $h['__status']    = if ($listen -lt 1) { 'warn' } else { 'ok' }
    return $h
}
$linkResult = Invoke-DbOsCheck -Id 'db.os.net_link' -Label 'DB OS Link 상태 (netstat -na)' `
    -CmdKey 'net_link' -Config $Config -Parser $linkParser

# ── sort 20: Ping 상태 (gateway ping 1회)
$pingParser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    if ($stdout -match 'no gateway') {
        $h['__status'] = 'warn'
        $h['note']     = 'default gateway not found'
        return $h
    }
    # AIX/linux ping 출력: "64 bytes from X.X.X.X: icmp_seq=0 ttl=64 time=1.234 ms"
    $rttLine = ($stdout -split "`r?`n" | Where-Object { $_ -match 'time=([\d.]+)' } | Select-Object -First 1)
    if ($rttLine -and $rttLine -match 'time=([\d.]+)\s*ms') {
        $rtt = [double]$matches[1]
        $h['rtt_ms']   = $rtt
        $h['__status'] = if ($rtt -ge 100) { 'warn' } else { 'ok' }
    } else {
        # 'packet loss' / '0% loss' 패턴 — 0% loss = ok
        $lossLine = ($stdout -split "`r?`n" | Where-Object { $_ -match 'packet loss' } | Select-Object -First 1)
        if ($lossLine -match '(\d+)% (packet )?loss') {
            $loss = [int]$matches[1]
            $h['loss_pct'] = $loss
            $h['__status'] = if ($loss -ge 50) { 'crit' } elseif ($loss -gt 0) { 'warn' } else { 'ok' }
        } else {
            $h['__status'] = 'n/a'
            $h['note']     = 'ping output 미인식'
        }
    }
    return $h
}
$pingResult = Invoke-DbOsCheck -Id 'db.os.net_ping' -Label 'DB OS Ping 상태 (gateway)' `
    -CmdKey 'net_ping' -Config $Config -Parser $pingParser -TimeoutSec 10

# ── sort 21: Collisions (netstat -ni Coll 컬럼)
$collParser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.TrimEnd() } | Where-Object {
        $_ -and $_ -notmatch '^(Name|Iface|Kernel)' -and $_ -notmatch '^lo' -and $_ -notmatch '^\s*$'
    })
    $totalColl = [long]0
    $ifaces = @()
    foreach ($l in $lines) {
        $cols = $l -split '\s+'
        # AIX netstat -ni: name mtu net addr ipkts ierrs opkts oerrs coll → Coll = cols[8]
        if ($os -eq 'aix' -and $cols.Count -ge 9) {
            $coll = [long]$cols[8]
            $ifaces += @{ name=$cols[0]; coll=$coll }
            $totalColl += $coll
        }
    }
    $h['interfaces']  = $ifaces
    $h['total_coll']  = $totalColl
    $h['__status']    = if ($totalColl -ge 1000) { 'warn' } elseif ($totalColl -ge 10000) { 'crit' } else { 'ok' }
    return $h
}
$collResult = Invoke-DbOsCheck -Id 'db.os.net_collisions' -Label 'DB OS Collisions (netstat -ni Coll)' `
    -CmdKey 'net_collisions' -Config $Config -Parser $collParser

# 3 row 배열로 반환 — Collector 가 array 정규화
@($linkResult, $pingResult, $collResult)
