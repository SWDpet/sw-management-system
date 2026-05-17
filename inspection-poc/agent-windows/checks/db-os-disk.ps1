param($Config)
. (Join-Path $PSScriptRoot '..\lib\DbOsCheck.ps1')

# df 출력 — 마운트포인트별 사용률. 결과는 단일 New-CheckResult 의 value.filesystems 배열.
# 임계값 초과 마운트포인트가 1개라도 있으면 status = warn/crit (worst 채택).
$threshold = $null
if ($Config -and $Config.thresholds -and $Config.thresholds.'disk.pct') {
    $threshold = $Config.thresholds.'disk.pct'
}

$parser = {
    param($stdout, $os, $remote)
    $h = [ordered]@{}
    $fsList = New-Object System.Collections.ArrayList
    $worstPct = 0.0

    $lines = $stdout -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ }
    foreach ($line in $lines) {
        if ($line -match '^(Filesystem|File system|Total)') { continue }
        $cols = $line -split '\s+'
        $fsName = $null; $mnt = $null; $sizeGb = $null; $usedGb = $null; $pct = $null

        switch ($os) {
            'aix' {
                # df -gH: Filesystem GB-blocks Free %Used Iused %Iused Mounted-on
                if ($cols.Count -ge 7 -and $cols[3] -match '^([\d.]+)%?$') {
                    $fsName = $cols[0]
                    $sizeGb = [double]$cols[1]
                    $freeGb = [double]$cols[2]
                    $usedGb = [math]::Round($sizeGb - $freeGb, 1)
                    $pct    = [double]($cols[3] -replace '%','')
                    $mnt    = $cols[6]
                }
            }
            'linux' {
                # df -PT: Filesystem Type 1024-blocks Used Available Capacity Mounted-on
                if ($cols.Count -ge 7 -and $cols[5] -match '^([\d.]+)%') {
                    $fsType  = $cols[1]
                    if ($fsType -match '^(tmpfs|devtmpfs|proc|sysfs|cgroup|overlay|squashfs|udev|none)$') { continue }
                    $fsName  = $cols[0]
                    $sizeKb  = [double]$cols[2]
                    $usedKb  = [double]$cols[3]
                    $sizeGb  = [math]::Round($sizeKb / 1048576, 1)
                    $usedGb  = [math]::Round($usedKb / 1048576, 1)
                    $pct     = [double]($cols[5] -replace '%','')
                    $mnt     = $cols[6]
                }
            }
            'solaris' {
                if ($cols.Count -ge 6 -and $cols[4] -match '^([\d.]+)%') {
                    $fsName = $cols[0]; $mnt = $cols[5]
                    $pct    = [double]($cols[4] -replace '%','')
                }
            }
            'hpux' {
                # bdf: Filesystem kbytes used avail %used Mounted-on
                if ($cols.Count -ge 6 -and $cols[4] -match '^([\d.]+)%') {
                    $fsName  = $cols[0]
                    $sizeKb  = [double]$cols[1]
                    $usedKb  = [double]$cols[2]
                    $sizeGb  = [math]::Round($sizeKb / 1048576, 1)
                    $usedGb  = [math]::Round($usedKb / 1048576, 1)
                    $pct     = [double]($cols[4] -replace '%','')
                    $mnt     = $cols[5]
                }
            }
        }

        if ($pct -ne $null -and $mnt) {
            $row = [ordered]@{
                mount = $mnt; filesystem = $fsName
                size_gb = $sizeGb; used_gb = $usedGb; used_pct = $pct
            }
            [void]$fsList.Add($row)
            if ($pct -gt $worstPct) { $worstPct = $pct }
        }
    }

    $h['filesystems'] = @($fsList)
    $h['worst_pct'] = $worstPct
    return $h
}

$result = Invoke-DbOsCheck -Id 'db.os.disk' -Label 'DB OS 디스크 사용량' `
    -CmdKey 'disk' -Config $Config -Parser $parser -Threshold $threshold

if ($threshold -and $result.value.worst_pct -ne $null) {
    $result.status = Resolve-Status -Value ([double]$result.value.worst_pct) -Threshold $threshold
}
return $result
