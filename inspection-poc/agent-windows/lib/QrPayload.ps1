# ── QR 페이로드 생성 ──────────────────────────────────────────────
# 풀 스냅샷 → 축약 qr-payload → gzip → base45 → 다중 프레임

. (Join-Path $PSScriptRoot 'Common.ps1')

# ─ base45 (RFC 9285) — pure PowerShell 구현 ────────────────────
$script:B45 = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:'
function ConvertTo-Base45 {
    param([Parameter(Mandatory)][byte[]]$Bytes)
    # PS 5.1: byte -shl 8은 결과가 byte로 잘림. int로 캐스트 필수.
    # PS 4.0 (Windows Server 2012 R2) 호환 — ::new() 대신 New-Object 사용.
    $sb = New-Object System.Text.StringBuilder
    for ($i = 0; $i -lt $Bytes.Count; $i += 2) {
        if ($i + 1 -lt $Bytes.Count) {
            $hi = [int]$Bytes[$i]
            $lo = [int]$Bytes[$i + 1]
            $n  = ($hi -shl 8) -bor $lo
            $e  = $n % 45; $n = [math]::Floor($n / 45)
            $d  = $n % 45; $n = [math]::Floor($n / 45)
            $c  = $n % 45
            [void]$sb.Append($script:B45[$e]).Append($script:B45[$d]).Append($script:B45[$c])
        } else {
            $n = [int]$Bytes[$i]
            $b = $n % 45; $a = [math]::Floor($n / 45)
            [void]$sb.Append($script:B45[$b]).Append($script:B45[$a])
        }
    }
    return $sb.ToString()
}

function Compress-Gzip {
    param([Parameter(Mandatory)][byte[]]$Bytes)
    # PS 4.0 호환 — System.IO.Compression assembly 명시 로드 + New-Object 사용.
    Add-Type -AssemblyName System.IO.Compression -ErrorAction SilentlyContinue
    $ms = New-Object System.IO.MemoryStream
    $gz = New-Object System.IO.Compression.GZipStream($ms, [System.IO.Compression.CompressionLevel]::Optimal)
    $gz.Write($Bytes, 0, $Bytes.Count)
    $gz.Close()
    return $ms.ToArray()
}

function Get-Sha1Hex {
    param([Parameter(Mandatory)][byte[]]$Bytes)
    $sha = [System.Security.Cryptography.SHA1]::Create()
    $h = $sha.ComputeHash($Bytes)
    ($h | ForEach-Object { $_.ToString('x2') }) -join ''
}

function Get-Crc32 {
    param([Parameter(Mandatory)][byte[]]$Bytes)
    # PS 5.1: 0xFFFFFFFF / 0xEDB88320 같은 32비트 hex 리터럴은 Int32로 파싱되어 음수가 됨.
    # Int64로 캐스트하면 부호확장으로 0xFFFFFFFFEDB88320 이 되어버리므로 양수 십진수로 명시.
    [int64]$poly  = 3988292384      # 0xEDB88320
    [int64]$mask  = 4294967295      # 0xFFFFFFFF (양수 Int64)
    [int64]$crc   = 4294967295
    foreach ($b in $Bytes) {
        $crc = $crc -bxor ([int64]$b)
        for ($j = 0; $j -lt 8; $j++) {
            if (($crc -band 1) -ne 0) {
                $crc = ($crc -shr 1) -bxor $poly
            } else {
                $crc = $crc -shr 1
            }
        }
    }
    return ($crc -bxor $mask) -band $mask
}

function ConvertTo-QrPayload {
    param([Parameter(Mandatory)] $Snapshot)
    # 풀 스냅샷 → 축약 페이로드 (raw/cmd/label 제거, [id,status,value] 튜플)
    $i = foreach ($it in $Snapshot.items) {
        $v = $it.value
        # value가 객체면 대표 숫자 추출 (PS 4.0 호환: try/catch로 키 접근)
        $extracted = $false
        if ($v -ne $null) {
            try { if ($v.total_gb -ne $null -and $v.free_gb -ne $null) {
                $v = [ordered]@{ t = [math]::Round($v.total_gb, 1); f = [math]::Round($v.free_gb, 1); p = $v.pct }; $extracted = $true
            }} catch {}
            if (-not $extracted) { try { if ($v.mounts -ne $null) {
                $v = $v.mounts; $extracted = $true
            }} catch {} }
            if (-not $extracted) { try { if ($v.pct -ne $null) { $v = $v.pct; $extracted = $true }} catch {} }
            if (-not $extracted) { try { if ($v.used_pct -ne $null) { $v = $v.used_pct; $extracted = $true }} catch {} }
            if (-not $extracted) { try { if ($v.count -ne $null) { $v = $v.count; $extracted = $true }} catch {} }
        }
        ,@($it.id, $it.status, $v)
    }
    return [ordered]@{
        s         = 'snapshot/qr1'
        id        = '{0}-{1}-{2}' -f $Snapshot.site, $Snapshot.round, $Snapshot.tier
        site      = $Snapshot.site
        round     = $Snapshot.round
        tier      = $Snapshot.tier
        host      = $Snapshot.host.hostname
        ts        = [int][double]::Parse((Get-Date -UFormat %s))
        inspector = $Snapshot.inspector
        i         = $i
    }
}

function Build-QrFrames {
    param(
        [Parameter(Mandatory)] $QrPayload,
        [int] $MaxChunkChars = 1800
    )
    $json = $QrPayload | ConvertTo-Json -Depth 10 -Compress
    $jsonBytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $gz  = Compress-Gzip -Bytes $jsonBytes
    $b45 = ConvertTo-Base45 -Bytes $gz
    $sha = (Get-Sha1Hex -Bytes $gz).Substring(0,6)

    $chunks = @()
    for ($i = 0; $i -lt $b45.Length; $i += $MaxChunkChars) {
        $end = [math]::Min($i + $MaxChunkChars, $b45.Length)
        $chunks += $b45.Substring($i, $end - $i)
    }

    $frames = @()
    $frames += [ordered]@{
        v = 1
        id = $QrPayload.id
        seq = 0
        total = $chunks.Count
        site = $QrPayload.site
        round = $QrPayload.round
        tier = $QrPayload.tier
        host = $QrPayload.host
        ts = $QrPayload.ts
        hash = $sha
        raw_bytes = $jsonBytes.Count
        gz_bytes = $gz.Count
        b45_chars = $b45.Length
    }
    for ($idx = 0; $idx -lt $chunks.Count; $idx++) {
        $chunk = $chunks[$idx]
        $crc = Get-Crc32 -Bytes ([System.Text.Encoding]::ASCII.GetBytes($chunk))
        $frames += [ordered]@{
            v = 1
            id = $QrPayload.id
            seq = ($idx + 1)
            total = $chunks.Count
            chk = ('{0:x4}' -f ($crc -band 0xFFFF))
            d = $chunk
        }
    }
    return @{
        Frames    = $frames
        RawBytes  = $jsonBytes.Count
        GzBytes   = $gz.Count
        B45Chars  = $b45.Length
        Hash      = $sha
    }
}
