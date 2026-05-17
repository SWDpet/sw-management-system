param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 15 '네트워크 사용률' — netstat -ni (인터페이스별 패킷 + Err).
# AIX 출력 컬럼: Name Mtu Network Address Ipkts Ierrs Opkts Oerrs Coll
# loopback (lo0) 제외, 인터페이스별 Ipkts/Opkts 합산, Ierrs+Oerrs 합산.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.TrimEnd() } | Where-Object {
        $_ -and $_ -notmatch '^(Name|Iface|Kernel)' -and $_ -notmatch '^lo' -and $_ -notmatch '^\s*$'
    })
    $totalIpkts = [long]0; $totalOpkts = [long]0; $totalErrs = [long]0
    $ifaces = @()
    foreach ($l in $lines) {
        $cols = $l -split '\s+'
        if ($cols.Count -lt 7) { continue }
        # AIX: name mtu net addr ipkts ierrs opkts oerrs coll
        if ($os -eq 'aix' -and $cols.Count -ge 9) {
            $ipkts = [long]$cols[4]; $ierrs = [long]$cols[5]
            $opkts = [long]$cols[6]; $oerrs = [long]$cols[7]
            $ifaces += @{ name=$cols[0]; ipkts=$ipkts; opkts=$opkts; ierrs=$ierrs; oerrs=$oerrs }
            $totalIpkts += $ipkts; $totalOpkts += $opkts; $totalErrs += ($ierrs + $oerrs)
        } else {
            # linux/etc.: name mtu rx-ok rx-err rx-drp rx-ovr tx-ok tx-err tx-drp tx-ovr flg
            $ipkts = [long]$cols[2]; $ierrs = [long]$cols[3]
            $opkts = if ($cols.Count -ge 7) { [long]$cols[6] } else { 0 }
            $oerrs = if ($cols.Count -ge 8) { [long]$cols[7] } else { 0 }
            $ifaces += @{ name=$cols[0]; ipkts=$ipkts; opkts=$opkts; ierrs=$ierrs; oerrs=$oerrs }
            $totalIpkts += $ipkts; $totalOpkts += $opkts; $totalErrs += ($ierrs + $oerrs)
        }
    }
    $h['interfaces'] = $ifaces
    $h['total_ipkts'] = $totalIpkts
    $h['total_opkts'] = $totalOpkts
    $h['total_errs']  = $totalErrs
    $h['__status'] = if ($totalErrs -ge 100) { 'warn' } elseif ($totalErrs -ge 1000) { 'crit' } else { 'ok' }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.netstat_perf' -Label 'DB OS 네트워크 사용률 (netstat -ni)' `
    -CmdKey 'netstat_perf' -Config $Config -Parser $parser
