param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

$threshold = $null
if ($Config -and $Config.thresholds -and $Config.thresholds.'perf.mem_pct') {
    $threshold = $Config.thresholds.'perf.mem_pct'
}

$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    switch ($os) {
        'linux' {
            # free -m 의 Mem: total used free shared buff/cache available
            $line = ($stdout -split "`r?`n" | Where-Object { $_ -match '^Mem:' } | Select-Object -First 1)
            if ($line) {
                $cols = $line -split '\s+'
                if ($cols.Count -ge 7) {
                    $total = [double]$cols[1]
                    $avail = [double]$cols[6]
                    if ($total -gt 0) {
                        $h['used_pct'] = [math]::Round(($total - $avail) * 100 / $total, 1)
                        $h['total_mb'] = [int]$total
                        $h['avail_mb'] = [int]$avail
                    }
                }
            }
        }
        'aix' {
            # svmon -G -O unit=MB 의 memory 행: size inuse free pin virtual ...
            $line = ($stdout -split "`r?`n" | Where-Object { $_ -match '^memory' } | Select-Object -First 1)
            if ($line) {
                $cols = $line -split '\s+'
                if ($cols.Count -ge 3) {
                    $total = [double]$cols[1]
                    $inuse = [double]$cols[2]
                    if ($total -gt 0) {
                        $h['used_pct'] = [math]::Round($inuse * 100 / $total, 1)
                        $h['total_mb'] = [int]$total
                        $h['inuse_mb'] = [int]$inuse
                    }
                }
            }
        }
        default {
            # solaris/hpux — 다음 iteration 에서 파싱 보강
        }
    }
    return $h
}

$result = Invoke-DbOsCheck -Id 'db.os.perf_mem' -Label 'DB OS Memory 사용률' `
    -CmdKey 'perf_mem' -Config $Config -Parser $parser -Threshold $threshold

if ($threshold -and $result.value.used_pct -ne $null) {
    $result.status = Resolve-Status -Value ([double]$result.value.used_pct) -Threshold $threshold
}
return $result
