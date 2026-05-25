param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# inspect_template DB sort 8 'Adapter 점검' — AIX lsdev -Cc adapter.
# 출력 라인 수와 첫 5줄 capture, 정상 = 라인 1 이상.
$parser = {
    param($stdout, $os, $remote)
    $lines = @($stdout -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    $h = [ordered]@{}
    $h['adapter_count'] = $lines.Count
    if ($lines.Count -gt 0) {
        $h['sample'] = @($lines | Select-Object -First 5)
        $h['__status'] = 'ok'
    } else {
        $h['__status'] = 'warn'
    }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.adapter' -Label 'DB OS Adapter (lsdev -Cc adapter)' `
    -CmdKey 'adapter' -Config $Config -Parser $parser
