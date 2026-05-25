param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 18 '미러 상태' — AIX lsvg -l rootvg.
# 출력 컬럼: LV NAME TYPE LPs PPs PVs LV STATE MOUNT POINT
# LV STATE = 'open/syncd' 정상, 'closed/stale' 비정상.
# stale 1개라도 = warn, closed 1개라도 = crit.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.TrimEnd() } | Where-Object {
        $_ -and $_ -notmatch '^(LV NAME|rootvg:)' -and $_ -notmatch '^\s*$'
    })
    $lvs = @()
    $stale = 0; $closed = 0
    foreach ($l in $lines) {
        $cols = $l -split '\s+'
        if ($cols.Count -lt 6) { continue }
        $state = if ($cols.Count -ge 6) { $cols[5] } else { '' }
        if ($cols.Count -ge 7) { $state = "$($cols[5])/$($cols[6])" }
        $lvs += @{ name=$cols[0]; type=$cols[1]; lps=$cols[2]; state=$state }
        if ($state -match 'stale')  { $stale++ }
        if ($state -match 'closed') { $closed++ }
    }
    $h['lvs']         = $lvs
    $h['lv_count']    = $lvs.Count
    $h['stale_count']  = $stale
    $h['closed_count'] = $closed
    $h['__status'] = if ($closed -gt 0) { 'crit' } elseif ($stale -gt 0) { 'warn' } else { 'ok' }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.lsvg_rootvg' -Label 'DB OS 미러 상태 (lsvg -l rootvg)' `
    -CmdKey 'lsvg' -Config $Config -Parser $parser
