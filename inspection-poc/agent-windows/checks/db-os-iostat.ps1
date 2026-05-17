param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 14 'I/O 사용률' — iostat 1 2.
# AIX iostat: Disks 별로 % tm_act 컬럼. linux: %util 컬럼. 두 OS 모두 마지막 샘플의 worst disk 추출.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.TrimEnd() })
    $diskLines = @($lines | Where-Object { $_ -match '^(hdisk|sd[a-z]+|nvme|c\d+t\d+)' })
    if ($diskLines.Count -eq 0) {
        $h['raw_tail']  = @($lines | Select-Object -Last 10)
        $h['__status']  = 'n/a'
        $h['note']      = 'disk row not detected — raw 확인'
        return $h
    }

    $worst = 0.0
    $entries = @()
    foreach ($l in $diskLines) {
        $cols = $l -split '\s+'
        # AIX: hdisk0 % tm_act bps tps bread bwrtn → cols[1] = tm_act
        # linux: sda rrqm/s wrqm/s r/s w/s ... %util → 마지막 컬럼 = util
        $pct = 0.0
        if ($os -eq 'aix' -and $cols.Count -ge 2) { $pct = [double]$cols[1] }
        elseif ($cols.Count -ge 2)                { $pct = [double]$cols[-1] }
        if ($pct -gt $worst) { $worst = $pct }
        $entries += @{ disk = $cols[0]; pct = $pct }
    }
    $h['disks']     = $entries
    $h['worst_pct'] = [math]::Round($worst, 1)
    $h['__status']  = if ($worst -ge 80) { 'crit' } elseif ($worst -ge 60) { 'warn' } else { 'ok' }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.iostat' -Label 'DB OS I/O 사용률 (iostat 1 2)' `
    -CmdKey 'iostat' -Config $Config -Parser $parser -TimeoutSec 15 `
    -Threshold @{ warn = 60; crit = 80 }
