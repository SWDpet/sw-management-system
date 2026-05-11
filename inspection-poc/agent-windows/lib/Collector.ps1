# ── 콜렉터 ──────────────────────────────────────────────
# checks/ 디렉토리의 모든 *.ps1을 순차 실행하여 스냅샷 구축.

. (Join-Path $PSScriptRoot 'Common.ps1')

function Invoke-Checks {
    param(
        [Parameter(Mandatory)] [string] $ChecksDir,
        [Parameter(Mandatory)] $Config,
        [string[]] $Include = @()   # 빈 배열이면 전체
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
            $timed = Invoke-Timed {
                & $f.FullName -Config $Config
            }
            $result = $timed.Result
            if ($null -eq $result) {
                Write-Log -Level WARN -Msg ("  no result from {0}" -f $f.Name)
                continue
            }
            # 체크가 여러 항목을 반환할 수도 있음 → 배열 정규화
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
