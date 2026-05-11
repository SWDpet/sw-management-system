# ── 스냅샷 입출력 ──────────────────────────────────────────────

. (Join-Path $PSScriptRoot 'Common.ps1')

function Get-SnapshotPath {
    param(
        [Parameter(Mandatory)] [string] $SnapshotDir,
        [Parameter(Mandatory)] $Snapshot
    )
    $stamp = (Get-Date).ToString('yyyy-MM-ddTHHmmss')
    $name = '{0}-{1}-{2}-{3}.json' -f $Snapshot.site, $Snapshot.round, $Snapshot.tier, $stamp
    return Join-Path $SnapshotDir $name
}

function Save-Snapshot {
    param(
        [Parameter(Mandatory)] [string] $SnapshotDir,
        [Parameter(Mandatory)] $Snapshot
    )
    $path = Get-SnapshotPath -SnapshotDir $SnapshotDir -Snapshot $Snapshot
    Write-Json -Object $Snapshot -Path $path
    return $path
}

function Get-LatestPreviousSnapshot {
    param(
        [Parameter(Mandatory)] [string] $SnapshotDir,
        [Parameter(Mandatory)] $Current,    # 현재 스냅샷 (자기 자신은 제외)
        [string] $TierFilter
    )
    if (-not (Test-Path $SnapshotDir)) { return $null }
    $tier = if ($TierFilter) { $TierFilter } else { $Current.tier }
    $pattern = '{0}-*-{1}-*.json' -f $Current.site, $tier
    $candidates = Get-ChildItem -Path $SnapshotDir -Filter $pattern -File |
        Sort-Object Name -Descending
    foreach ($c in $candidates) {
        $snap = Read-Json -Path $c.FullName
        if ($snap.taken_at -eq $Current.taken_at) { continue }
        if ($snap.round -eq $Current.round)       { continue }   # 같은 회차 재실행은 skip
        return $snap
    }
    return $null
}
