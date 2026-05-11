param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

$results = @()
$drives = Get-CimInstance Win32_LogicalDisk -Filter "DriveType=3" -ErrorAction SilentlyContinue
foreach ($d in $drives) {
    if (-not $d.Size) { continue }
    $total = [double]$d.Size
    $free  = [double]$d.FreeSpace
    $used  = $total - $free
    $pct   = [math]::Round(($used / $total) * 100, 1)
    $th    = $Config.thresholds.'disk.pct'
    $status = Resolve-Status -Value $pct -Threshold $th
    $letter = $d.DeviceID.TrimEnd(':').ToLower()
    $results += New-CheckResult `
        -Id ("ap.disk.{0}" -f $letter) `
        -Label ("Disk {0} 사용량" -f $d.DeviceID) `
        -Category 'filesystem' `
        -Cmd 'Get-CimInstance Win32_LogicalDisk' `
        -Value @{
            pct      = $pct
            used_gb  = [math]::Round($used / 1GB, 2)
            free_gb  = [math]::Round($free / 1GB, 2)
            total_gb = [math]::Round($total / 1GB, 2)
        } `
        -Threshold $th `
        -Status $status
}
$results
