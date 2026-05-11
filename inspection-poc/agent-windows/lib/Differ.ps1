# ── 델타 계산 ──────────────────────────────────────────────

. (Join-Path $PSScriptRoot 'Common.ps1')

function Get-NumericFromValue {
    param($Value)
    if ($null -eq $Value) { return $null }
    if ($Value -is [double] -or $Value -is [int] -or $Value -is [long]) { return [double]$Value }
    if ($Value.PSObject.Properties['pct'])      { return [double]$Value.pct }
    if ($Value.PSObject.Properties['used_gb'])  { return [double]$Value.used_gb }
    if ($Value.PSObject.Properties['used_pct']) { return [double]$Value.used_pct }
    if ($Value.PSObject.Properties['count'])    { return [double]$Value.count }
    if ($Value.PSObject.Properties['gb'])       { return [double]$Value.gb }
    return $null
}

function Compute-Delta {
    param(
        [Parameter(Mandatory)] $Current,
        $Previous
    )
    if (-not $Previous) {
        return [ordered]@{
            schema = 'delta/v1'
            site = $Current.site
            round = $Current.round
            tier = $Current.tier
            compared_with = $null
            note = 'first snapshot — no previous to compare'
            changes = @()
            summary = @{ changes = 0; info = 0; watch = 0; warn = 0; crit = 0 }
        }
    }

    $prevById = @{}
    foreach ($it in $Previous.items) { $prevById[$it.id] = $it }

    $changes = New-Object System.Collections.Generic.List[object]
    foreach ($cur in $Current.items) {
        $prev = $prevById[$cur.id]
        if (-not $prev) {
            $changes.Add([ordered]@{
                id = $cur.id
                kind = 'new'
                curr_status = $cur.status
                severity = 'info'
            }) | Out-Null
            continue
        }

        $curVal = Get-NumericFromValue $cur.value
        $prevVal = Get-NumericFromValue $prev.value
        if ($null -ne $curVal -and $null -ne $prevVal) {
            $delta = [math]::Round($curVal - $prevVal, 3)
            if ([math]::Abs($delta) -lt 0.01) { continue }
            $sev = 'info'
            if ($cur.status -eq 'warn' -and $prev.status -eq 'ok')  { $sev = 'warn' }
            if ($cur.status -eq 'crit')                              { $sev = 'crit' }
            if ($delta -gt 5 -and $cur.id -like '*fs.*')             { $sev = 'watch' }
            if ($delta -gt 1 -and $cur.id -like '*store*')           { $sev = 'watch' }
            $changes.Add([ordered]@{
                id = $cur.id
                kind = 'numeric'
                prev = $prevVal
                curr = $curVal
                delta = $delta
                prev_status = $prev.status
                curr_status = $cur.status
                severity = $sev
            }) | Out-Null
            continue
        }

        if ($cur.status -ne $prev.status) {
            $changes.Add([ordered]@{
                id = $cur.id
                kind = 'status'
                prev_status = $prev.status
                curr_status = $cur.status
                severity = if (@('warn','crit','error') -contains $cur.status) { $cur.status } else { 'info' }
            }) | Out-Null
        }
    }

    $info = ($changes | Where-Object severity -EQ 'info').Count
    $watch= ($changes | Where-Object severity -EQ 'watch').Count
    $warn = ($changes | Where-Object severity -EQ 'warn').Count
    $crit = ($changes | Where-Object severity -EQ 'crit').Count

    return [ordered]@{
        schema = 'delta/v1'
        site   = $Current.site
        round  = $Current.round
        tier   = $Current.tier
        compared_with = @{
            round       = $Previous.round
            round_date  = $Previous.round_date
            taken_at    = $Previous.taken_at
        }
        changes = $changes.ToArray()
        summary = @{
            changes = $changes.Count
            info = $info; watch = $watch; warn = $warn; crit = $crit
        }
    }
}
