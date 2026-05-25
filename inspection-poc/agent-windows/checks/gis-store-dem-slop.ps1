param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# inspect_template GIS sort 12 'UWES DEM/SLOP 보존 확인' — P10 카드용.
# GWS store 안 DEM / SLOP 디렉토리가 GWS 로그 정리 시 삭제 대상에서 제외돼야 함.
# 본 점검: 두 디렉토리 존재 + 크기(GB) + 최근 수정일 확인.
$storeDir = $Config.paths.gws_store_dir

if (-not $storeDir -or -not (Test-Path $storeDir)) {
    return (New-CheckResult -Id 'gis.uwes.dem_slop_preserved' -Label 'UWES DEM/SLOP 보존 확인' `
        -Category 'gis' -Method 'auto' -Status 'n/a' `
        -Value @{ path=$storeDir; exists=$false } `
        -Note ("UWES Store 경로 미존재: {0}" -f $storeDir))
}

function Measure-StoreSubdir {
    param([string]$Path, [string]$Name)
    $h = [ordered]@{
        name      = $Name
        path      = $Path
        exists    = (Test-Path $Path)
        size_gb   = 0
        file_count = 0
        last_modified = $null
    }
    if (-not $h['exists']) { return $h }
    try {
        $files = Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue
        $bytes = ($files | Measure-Object -Sum Length).Sum
        $h['size_gb']   = if ($bytes) { [math]::Round($bytes / 1GB, 2) } else { 0 }
        $h['file_count'] = $files.Count
        $latest = $files | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($latest) { $h['last_modified'] = $latest.LastWriteTime.ToString('yyyy-MM-ddTHH:mm:ss') }
    } catch {
        $h['error'] = $_.Exception.Message
    }
    return $h
}

$dem  = Measure-StoreSubdir -Path (Join-Path $storeDir 'DEM')  -Name 'DEM'
$slop = Measure-StoreSubdir -Path (Join-Path $storeDir 'SLOP') -Name 'SLOP'

$bothExist = $dem['exists'] -and $slop['exists']
$status = if ($bothExist) { 'ok' } elseif ($dem['exists'] -or $slop['exists']) { 'warn' } else { 'crit' }
$note = if ($bothExist) {
    'DEM/SLOP 모두 보존됨'
} elseif ($status -eq 'warn') {
    '한쪽 디렉토리 누락 — 로그 정리 시 삭제됐는지 확인 필요'
} else {
    'DEM/SLOP 둘 다 없음 — 데이터 손실 가능성, 즉시 확인'
}

New-CheckResult `
    -Id 'gis.uwes.dem_slop_preserved' `
    -Label 'UWES DEM/SLOP 보존 확인' `
    -Category 'gis' `
    -Cmd ("Test-Path {0}\DEM, {0}\SLOP" -f $storeDir) `
    -Value @{
        store_dir   = $storeDir
        dem         = $dem
        slop        = $slop
        both_exist  = $bothExist
    } `
    -Status $status `
    -Note $note
