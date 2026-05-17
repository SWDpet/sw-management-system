param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    switch ($os) {
        'aix' {
            $cores = _GrepFirst $stdout 'Number Of Processors:\s*(\d+)'
            $model = _GrepFirst $stdout 'Processor Type:\s*(.+)$'
            $clk   = _GrepFirst $stdout 'Processor Clock Speed:\s*(\d+)'
            if ($cores) { $h['cores'] = [int]$cores }
            if ($model) { $h['model'] = $model }
            if ($clk)   { $h['clock_ghz'] = [math]::Round([double]$clk / 1000, 2) }
        }
        'linux' {
            $cores = _GrepFirst $stdout '^CPU\(s\):\s+(\d+)'
            $model = _GrepFirst $stdout '^Model name:\s+(.+)$'
            $mhz   = _GrepFirst $stdout '^CPU(?:\s+max)?\s+MHz:\s+([\d.]+)'
            if (-not $cores) { $cores = (($stdout -split "`r?`n" | Where-Object { $_ -match '^processor\s*:' }).Count) }
            if (-not $model) { $model = _GrepFirst $stdout '^model name\s*:\s*(.+)$' }
            if ($cores) { $h['cores'] = [int]$cores }
            if ($model) { $h['model'] = $model }
            if ($mhz)   { $h['clock_ghz'] = [math]::Round([double]$mhz / 1000, 2) }
        }
        default {
            # solaris/hpux — raw output 만 (정규화 다음 iteration)
        }
    }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.cpu_info' -Label 'DB OS CPU 구성' `
    -CmdKey 'cpu_info' -Config $Config -Parser $parser
