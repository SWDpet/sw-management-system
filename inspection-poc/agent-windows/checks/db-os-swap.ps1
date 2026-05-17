param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 13 'SWAP 사용률' — AIX lsps -a.
# lsps 출력 예: "hd6  hdisk0  rootvg  2048MB  -1  yes  yes  lv  0"
#  → 컬럼 4=size, 컬럼 9=%used. AIX 만 깊은 파싱, 다른 OS 는 raw.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ -and $_ -notmatch '^(Page Space|Physical Volume)' })
    if ($os -eq 'aix' -and $lines.Count -gt 0) {
        $totalUsed = 0.0; $totalSize = 0
        $entries = @()
        foreach ($l in $lines) {
            $cols = $l -split '\s+'
            if ($cols.Count -ge 9) {
                $sizeMb  = if ($cols[3] -match '(\d+)') { [int]$matches[1] } else { 0 }
                $usedPct = if ($cols[8] -match '(\d+)') { [int]$matches[1] } else { 0 }
                $entries += @{ name=$cols[0]; size_mb=$sizeMb; used_pct=$usedPct }
                $totalSize += $sizeMb
                $totalUsed += ($sizeMb * $usedPct / 100.0)
            }
        }
        $avgPct = if ($totalSize -gt 0) { [math]::Round($totalUsed * 100.0 / $totalSize, 1) } else { 0 }
        $h['entries']  = $entries
        $h['used_pct'] = $avgPct
        $h['__status'] = if ($avgPct -ge 90) { 'crit' } elseif ($avgPct -ge 80) { 'warn' } else { 'ok' }
    } else {
        $h['raw_lines'] = $lines
        $h['__status']  = 'ok'
    }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.swap_pct' -Label 'DB OS SWAP 사용률 (lsps -a)' `
    -CmdKey 'swap' -Config $Config -Parser $parser `
    -Threshold @{ warn = 80; crit = 90 }
