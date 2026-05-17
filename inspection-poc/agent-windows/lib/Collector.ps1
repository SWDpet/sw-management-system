# ── 콜렉터 ──────────────────────────────────────────────
# checks/ 디렉토리의 *.ps1 을 manifest 순서대로 실행하여 스냅샷 구축.
# manifest.json 이 있으면 manifest 의 sort 순서 사용 (inspect_template.sort_order 와 정합 강제).
# 없으면 알파벳 정렬 fallback.

. (Join-Path $PSScriptRoot 'Common.ps1')

function Invoke-Checks {
    param(
        [Parameter(Mandatory)] [string] $ChecksDir,
        [Parameter(Mandatory)] $Config,
        [string[]] $Include = @()   # 빈 배열이면 전체
    )

    $tier = if ($Config.tier) { $Config.tier.ToString().ToUpper() } else { '' }
    $manifestPath = Join-Path (Split-Path -Parent $ChecksDir) 'manifest.json'
    $manifestModules = $null

    if ($tier -and (Test-Path $manifestPath)) {
        try {
            $manifest = Get-Content $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
            if ($manifest.sections.PSObject.Properties[$tier]) {
                $manifestModules = $manifest.sections.$tier
                Write-Log -Level INFO -Msg ("manifest dispatch: tier={0}, rows={1}" -f $tier, $manifestModules.Count)
            } else {
                Write-Log -Level WARN -Msg ("manifest 에 tier '{0}' 섹션 없음 — alphabetical fallback" -f $tier)
            }
        } catch {
            Write-Log -Level WARN -Msg ("manifest 로드 실패: {0} — alphabetical fallback" -f $_.Exception.Message)
        }
    }

    if ($manifestModules) {
        return Invoke-ChecksManifest -ChecksDir $ChecksDir -Config $Config -Modules $manifestModules -Include $Include
    }
    return Invoke-ChecksAlphabetical -ChecksDir $ChecksDir -Config $Config -Include $Include
}

function Invoke-ChecksManifest {
    param(
        [string] $ChecksDir,
        $Config,
        [array] $Modules,
        [string[]] $Include = @()
    )

    # unique module 추출 — 같은 ps1 이 여러 sort 에 등장하면 한 번만 실행
    $uniqueModules = $Modules | Select-Object -ExpandProperty module -Unique
    if ($Include.Count -gt 0) {
        $uniqueModules = $uniqueModules | Where-Object {
            $stem = [System.IO.Path]::GetFileNameWithoutExtension($_)
            $Include -contains $stem
        }
    }

    $rowsById = @{}
    $errors = 0
    foreach ($modName in $uniqueModules) {
        $f = Join-Path $ChecksDir $modName
        if (-not (Test-Path $f)) {
            Write-Log -Level WARN -Msg ("manifest module 누락: {0}" -f $modName)
            continue
        }
        Write-Log -Level INFO -Msg ("check: {0}" -f $modName)
        try {
            $timed = Invoke-Timed { & $f -Config $Config }
            $result = $timed.Result
            if ($null -eq $result) {
                Write-Log -Level WARN -Msg ("  no result from {0}" -f $modName)
                continue
            }
            foreach ($item in @($result)) {
                if (-not $item.PSObject.Properties['took_ms'] -or $item.took_ms -eq 0) {
                    $item | Add-Member -NotePropertyName 'took_ms' -NotePropertyValue $timed.Ms -Force
                }
                $id = if ($item.PSObject.Properties['id']) { $item.id } else { $null }
                if ($id) { $rowsById[$id] = $item }
            }
        } catch {
            $errors++
            Write-Log -Level ERROR -Msg ("  {0}: {1}" -f $modName, $_.Exception.Message)
        }
    }

    # manifest 순서대로 결과 배열 구성 — 누락 row 는 placeholder
    $items = New-Object System.Collections.Generic.List[object]
    foreach ($m in $Modules) {
        if ($rowsById.ContainsKey($m.id)) {
            $items.Add($rowsById[$m.id]) | Out-Null
        } else {
            $status = if ($m.method -eq 'manual') { 'pending_manual' } else { 'n/a' }
            $note   = "manifest sort={0} module={1} (no row from agent — placeholder)" -f $m.sort, $m.module
            $items.Add((New-CheckResult `
                -Id $m.id `
                -Label $m.label `
                -Category 'manifest' `
                -Method $m.method `
                -Status $status `
                -Note $note)) | Out-Null
            if ($m.method -ne 'manual') {
                Write-Log -Level WARN -Msg ("manifest miss: sort={0} id={1} module={2}" -f $m.sort, $m.id, $m.module)
            }
        }
    }

    return @{
        Items = $items.ToArray()
        Errors = $errors
    }
}

function Invoke-ChecksAlphabetical {
    param(
        [string] $ChecksDir,
        $Config,
        [string[]] $Include = @()
    )
    $files = Get-ChildItem -Path $ChecksDir -Filter '*.ps1' -File | Sort-Object Name
    if ($Include.Count -gt 0) {
        $files = $files | Where-Object {
            $stem = [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
            $Include -contains $stem
        }
    }

    $items = New-Object System.Collections.Generic.List[object]
    $errors = 0
    foreach ($f in $files) {
        Write-Log -Level INFO -Msg ("check: {0}" -f $f.Name)
        try {
            $timed = Invoke-Timed { & $f.FullName -Config $Config }
            $result = $timed.Result
            if ($null -eq $result) {
                Write-Log -Level WARN -Msg ("  no result from {0}" -f $f.Name)
                continue
            }
            foreach ($item in @($result)) {
                if (-not $item.PSObject.Properties['took_ms'] -or $item.took_ms -eq 0) {
                    $item | Add-Member -NotePropertyName 'took_ms' -NotePropertyValue $timed.Ms -Force
                }
                $items.Add($item) | Out-Null
            }
        } catch {
            $errors++
            Write-Log -Level ERROR -Msg ("  {0}: {1}" -f $f.Name, $_.Exception.Message)
            $items.Add((New-CheckResult `
                -Id ("error.{0}" -f [System.IO.Path]::GetFileNameWithoutExtension($f.Name)) `
                -Label ("Check failed: {0}" -f $f.Name) `
                -Category 'error' `
                -Method 'auto' `
                -Status 'error' `
                -Note $_.Exception.Message)) | Out-Null
        }
    }

    return @{
        Items = $items.ToArray()
        Errors = $errors
    }
}

function Build-Snapshot {
    param(
        [Parameter(Mandatory)] $Config,
        [Parameter(Mandatory)] [array] $Items,
        [Parameter(Mandatory)] [int] $TotalMs
    )
    $okCount    = @($Items | Where-Object status -EQ 'ok').Count
    $warnCount  = @($Items | Where-Object status -EQ 'warn').Count
    $critCount  = @($Items | Where-Object status -EQ 'crit').Count
    $errorCount = @($Items | Where-Object status -EQ 'error').Count
    $manualCount= @($Items | Where-Object status -EQ 'pending_manual').Count

    $round = (Get-Date).ToString('yyyy-MM')
    $hostInfo = [ordered]@{
        hostname  = [System.Net.Dns]::GetHostName()
        os        = (Get-CimInstance Win32_OperatingSystem).Caption
        os_detail = (Get-CimInstance Win32_OperatingSystem).Version
        model     = (Get-CimInstance Win32_ComputerSystem).Model
        ip        = ((Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
                       Where-Object { $_.IPAddress -notmatch '^(127|169\.254)' }).IPAddress | Select-Object -First 1)
    }

    return [ordered]@{
        schema       = 'snapshot/v1'
        site         = $Config.site
        site_name    = $Config.site_name
        round        = $round
        round_date   = (Get-Date).ToString('yyyy-MM-dd')
        tier         = $Config.tier
        host         = $hostInfo
        taken_at     = Get-IsoNow
        took_ms      = $TotalMs
        agent_version= '0.1.0'
        inspector    = $Config.inspector
        items        = $Items
        summary      = [ordered]@{
            total_items    = $Items.Count
            ok             = $okCount
            warn           = $warnCount
            crit           = $critCount
            errors         = $errorCount
            pending_manual = $manualCount
        }
    }
}
