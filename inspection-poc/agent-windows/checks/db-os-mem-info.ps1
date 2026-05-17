param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    switch ($os) {
        'aix' {
            # "Memory Size: 16384 MB"
            $mb = _GrepFirst $stdout 'Memory Size:\s*(\d+)'
            if ($mb) { $h['total_mb'] = [int]$mb; $h['total_gb'] = [math]::Round([int]$mb / 1024, 1) }
        }
        'linux' {
            # free -m 의 Mem: 행: total used free shared buff/cache available
            $line = ($stdout -split "`r?`n" | Where-Object { $_ -match '^Mem:' } | Select-Object -First 1)
            if ($line) {
                $cols = $line -split '\s+'
                if ($cols.Count -ge 4) {
                    $h['total_mb'] = [int]$cols[1]
                    $h['used_mb']  = [int]$cols[2]
                    $h['free_mb']  = [int]$cols[3]
                    $h['total_gb'] = [math]::Round([int]$cols[1] / 1024, 1)
                }
            }
        }
        'solaris' {
            $mb = _GrepFirst $stdout 'Memory size:\s*(\d+)'
            if ($mb) { $h['total_mb'] = [int]$mb; $h['total_gb'] = [math]::Round([int]$mb / 1024, 1) }
        }
        default { }
    }
    return $h
}

Invoke-DbOsCheck -Id 'db.os.mem_info' -Label 'DB OS Memory 구성' `
    -CmdKey 'mem_info' -Config $Config -Parser $parser
