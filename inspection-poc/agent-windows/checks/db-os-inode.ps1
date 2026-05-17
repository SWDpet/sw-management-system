param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 17 'I-node 사용량' — df -i.
# 출력 컬럼: Filesystem Inodes IUsed IFree IUse% Mounted on
# worst IUse% 추출 + 임계 80/90.
$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.TrimEnd() } | Where-Object {
        $_ -and $_ -notmatch '^(Filesystem|Node)'
    })
    $worst = 0.0
    $entries = @()
    foreach ($l in $lines) {
        $cols = $l -split '\s+'
        if ($cols.Count -lt 5) { continue }
        # 다양한 OS 가 IUse% 컬럼 위치 다름 — % 가 포함된 첫 토큰 찾기
        $pctTok = $cols | Where-Object { $_ -match '\d+%' } | Select-Object -First 1
        if (-not $pctTok) { continue }
        $pct = [int]($pctTok -replace '[^\d]','')
        $mount = $cols[-1]
        $entries += @{ filesystem=$cols[0]; mount=$mount; iuse_pct=$pct }
        if ($pct -gt $worst) { $worst = $pct }
    }
    $h['filesystems'] = $entries
    $h['worst_pct']   = $worst
    $h['__status']    = if ($worst -ge 90) { 'crit' } elseif ($worst -ge 80) { 'warn' } else { 'ok' }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.inode' -Label 'DB OS I-node 사용량 (df -i)' `
    -CmdKey 'inode' -Config $Config -Parser $parser `
    -Threshold @{ warn = 80; crit = 90 }
