param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# vmstat 1 2 의 마지막 라인에서 idle 컬럼 추출 → 사용률 = 100 - idle.
# OS 별 idle 컬럼 위치 (끝에서 N번째) 는 UnixCmd.ps1 의 Get-VmstatIdleOffset.
$threshold = $null
if ($Config -and $Config.thresholds -and $Config.thresholds.'perf.cpu_pct') {
    $threshold = $Config.thresholds.'perf.cpu_pct'
}

$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $last = _LastNonEmptyLine $stdout
    if (-not $last) { return $h }
    $tokens = @($last -split '\s+' | Where-Object { $_ -ne '' })
    if ($tokens.Count -lt 5) { return $h }

    $offset = Get-VmstatIdleOffset -Os $os
    $idx = $tokens.Count - 1 - $offset
    if ($idx -lt 0 -or $idx -ge $tokens.Count) { return $h }
    $idleStr = $tokens[$idx]
    if ($idleStr -notmatch '^\d+(\.\d+)?$') { return $h }

    $idle = [double]$idleStr
    $used = [math]::Round(100 - $idle, 1)
    $h['used_pct']   = $used
    $h['idle_pct']   = $idle

    # threshold 평가 — 코드의 perf.cpu_pct 임계와 동일
    $th = $remote.thresholds   # not used; threshold 는 outer scope
    return $h
}

$result = Invoke-DbOsCheck -Id 'db.os.perf_cpu' -Label 'DB OS CPU 사용률' `
    -CmdKey 'perf_cpu' -Config $Config -Parser $parser -Threshold $threshold

# threshold 가 있고 used_pct 가 파싱됐으면 status 재평가
if ($threshold -and $result.value.used_pct -ne $null) {
    $result.status = Resolve-Status -Value ([double]$result.value.used_pct) -Threshold $threshold
}
return $result
