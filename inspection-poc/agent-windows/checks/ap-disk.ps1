param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# inspect_template AP sort 9 'Disk 여유공간' 1 행에 매핑 — drive 별 다중 row → 종합 row 1개로 압축.
# 개별 drive 정보는 value.drives 배열로 보존, 점검 결과는 worst pct 기준.

$drives = Get-CimInstance Win32_LogicalDisk -Filter "DriveType=3" -ErrorAction SilentlyContinue
$th = $Config.thresholds.'disk.pct'

$driveInfo = @()
$worstPct = 0.0
foreach ($d in $drives) {
    if (-not $d.Size) { continue }
    $total = [double]$d.Size
    $free  = [double]$d.FreeSpace
    $used  = $total - $free
    $pct   = [math]::Round(($used / $total) * 100, 1)
    if ($pct -gt $worstPct) { $worstPct = $pct }
    $driveInfo += @{
        letter   = $d.DeviceID.TrimEnd(':').ToLower()
        pct      = $pct
        used_gb  = [math]::Round($used / 1GB, 2)
        free_gb  = [math]::Round($free / 1GB, 2)
        total_gb = [math]::Round($total / 1GB, 2)
    }
}

$status = if ($driveInfo.Count -eq 0) { 'n/a' } else { Resolve-Status -Value $worstPct -Threshold $th }
$summary = if ($driveInfo.Count -eq 0) { 'no drives' } else {
    ($driveInfo | ForEach-Object { ("{0}: {1}%" -f $_.letter.ToUpper(), $_.pct) }) -join ' / '
}

New-CheckResult `
    -Id 'ap.os.disk_summary' `
    -Label 'Disk 여유공간 (종합)' `
    -Category 'filesystem' `
    -Cmd 'Get-CimInstance Win32_LogicalDisk (DriveType=3)' `
    -Value @{
        worst_pct = $worstPct
        drives    = $driveInfo
        summary   = $summary
    } `
    -Threshold $th `
    -Status $status
