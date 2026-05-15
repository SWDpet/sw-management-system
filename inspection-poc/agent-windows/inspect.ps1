<#
.SYNOPSIS
  UPIS 점검 자동화 — Windows AP 서버 에이전트
.DESCRIPTION
  점검 대상 서버(Windows Server 2012R2 이상)에서 실행:
    1) checks/ 의 모든 *.ps1 순차 실행 → 스냅샷 구축
    2) snapshots/ 에 영구 저장
    3) 직전 회차 스냅샷과 비교하여 delta 산출
    4) QR 반출 페이로드(JSON+gzip+base45+frames) 생성
    5) out/ 에 frames.json + summary.html (모니터로 띄울 QR 뷰어) 저장
.PARAMETER ConfigPath
  사이트별 설정 JSON 경로. (예: config\site.dyg.json)
.PARAMETER OnlyChecks
  특정 체크만 실행 (디버깅용). 파일명(확장자 제외)을 콤마로 나열.
.EXAMPLE
  .\inspect.ps1 -ConfigPath .\config\site.dyg.json
#>
[CmdletBinding()]
param(
    [string] $ConfigPath = (Join-Path $PSScriptRoot 'config\site.dyg.json'),
    [string[]] $OnlyChecks = @()
)

$ErrorActionPreference = 'Stop'
# PS 5.1 호환: 단일 객체 .Count 등을 허용하기 위해 strict mode는 사용하지 않음

# 콘솔 UTF-8 출력 — 한국어 Windows 서버에서 한글 표시 안정화 (best-effort).
try {
    [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
    $OutputEncoding           = [Console]::OutputEncoding
} catch {}

$root = $PSScriptRoot
. (Join-Path $root 'lib\Common.ps1')
. (Join-Path $root 'lib\Collector.ps1')
. (Join-Path $root 'lib\Snapshot.ps1')
. (Join-Path $root 'lib\Differ.ps1')
. (Join-Path $root 'lib\QrPayload.ps1')

Write-Log -Level INFO -Msg "===== UPIS 점검 자동화 v0.1.0 ====="
Write-Log -Level INFO -Msg "config: $ConfigPath"

$cfg = Read-SiteConfig -Path $ConfigPath
Write-Log -Level INFO -Msg ("site={0}  tier={1}  inspector={2}" -f $cfg.site, $cfg.tier, $cfg.inspector)

$checksDir = Join-Path $root 'checks'
$snapshotDir = Join-Path $root 'snapshots'
$outDir = Join-Path $root 'out'

$totalSw = [System.Diagnostics.Stopwatch]::StartNew()

# 1) 체크 실행
$run = Invoke-Checks -ChecksDir $checksDir -Config $cfg -Include $OnlyChecks
$totalSw.Stop()
Write-Log -Level INFO -Msg ("collected {0} items, errors {1} in {2} ms" -f $run.Items.Count, $run.Errors, $totalSw.ElapsedMilliseconds)

# 2) 스냅샷 구축 + 저장
$snapshot = Build-Snapshot -Config $cfg -Items $run.Items -TotalMs ([int]$totalSw.ElapsedMilliseconds)
$snapPath = Save-Snapshot -SnapshotDir $snapshotDir -Snapshot $snapshot
Write-Log -Level INFO -Msg ("snapshot saved: {0}" -f (Split-Path -Leaf $snapPath))

# 3) 델타
$prev = Get-LatestPreviousSnapshot -SnapshotDir $snapshotDir -Current $snapshot
$delta = Compute-Delta -Current $snapshot -Previous $prev
$deltaPath = Join-Path $outDir 'delta.json'
Write-Json -Object $delta -Path $deltaPath
if ($prev) {
    Write-Log -Level INFO -Msg ("delta vs {0} ({1} changes)" -f $prev.round, $delta.summary.changes)
} else {
    Write-Log -Level INFO -Msg "delta: first snapshot, no previous"
}

# 4) QR 페이로드 + frames
$qrPayload = ConvertTo-QrPayload -Snapshot $snapshot
$qr = Build-QrFrames -QrPayload $qrPayload -MaxChunkChars 1800
$framesPath = Join-Path $outDir 'frames.json'
Write-Json -Object $qr.Frames -Path $framesPath
Write-Log -Level INFO -Msg ("frames built: total={0}  raw={1}B  gz={2}B  b45={3}c  sha={4}" -f `
    $qr.Frames.Count, $qr.RawBytes, $qr.GzBytes, $qr.B45Chars, $qr.Hash)

# 5) summary.html (모니터 표시용)
$summaryPath = Join-Path $outDir 'summary.html'
$ok = $snapshot.summary.ok; $warn = $snapshot.summary.warn; $crit = $snapshot.summary.crit
$manual = $snapshot.summary.pending_manual
$rows = ($snapshot.items | ForEach-Object {
    $valStr = if ($null -eq $_.value) { '—' } else {
        if ($_.value -is [string] -or $_.value -is [int] -or $_.value -is [double] -or $_.value -is [bool]) {
            "$($_.value)"
        } else {
            ($_.value | ConvertTo-Json -Compress -Depth 5)
        }
    }
    "<tr class='st-$($_.status)'><td>$($_.id)</td><td>$($_.label)</td><td>$($_.status)</td><td>$([System.Web.HttpUtility]::HtmlEncode($valStr))</td></tr>"
}) -join "`n"

Add-Type -AssemblyName System.Web -ErrorAction SilentlyContinue

# QR JS 라이브러리 inline embed (단일 HTML 자체 완결 — 폐쇄망 친화)
$qrLibPath = Join-Path $root 'lib\qrcode.min.js'
$qrLibCode = if (Test-Path $qrLibPath) { Get-Content -Path $qrLibPath -Raw -Encoding UTF8 } else { '' }
$framesJson = $qr.Frames | ConvertTo-Json -Depth 6 -Compress

$html = @"
<!doctype html>
<html lang="ko"><head><meta charset="utf-8">
<title>UPIS 점검 — $($cfg.site_name) / $($snapshot.round)</title>
<style>
 body{font-family:'Pretendard','Segoe UI',sans-serif;margin:24px;background:#f5f5f5}
 h1{margin:0 0 4px;font-size:22px}
 .meta{color:#555;margin-bottom:12px}
 .summary{display:flex;gap:16px;margin-bottom:16px}
 .summary div{padding:8px 14px;border-radius:8px;background:#fff;border:1px solid #ddd}
 .summary .ok{color:#16a34a;font-weight:700}
 .summary .warn{color:#ea580c;font-weight:700}
 .summary .crit{color:#dc2626;font-weight:700}
 table{border-collapse:collapse;background:#fff;width:100%;font-size:13px;margin-bottom:24px}
 th,td{border:1px solid #ddd;padding:6px 10px;text-align:left}
 th{background:#f0f0f0}
 .st-warn{background:#fff7ed}
 .st-crit{background:#fee2e2}
 .st-pending_manual{background:#eff6ff}
 .footer{margin-top:20px;color:#555;font-size:13px}
 .qr-section{margin-top:32px;padding-top:24px;border-top:2px solid #1565c0}
 .qr-section h2{color:#1565c0;margin:0 0 8px;font-size:22px}
 .qr-grid{display:grid;grid-template-columns:repeat(auto-fit, minmax(360px, 1fr));gap:24px;margin-top:16px}
 .qr-card{background:#fff;border:1px solid #ddd;border-radius:12px;padding:20px;text-align:center;box-shadow:0 2px 8px rgba(0,0,0,0.04)}
 .qr-card .label{font-size:14px;color:#777;margin-bottom:4px}
 .qr-card .seq{font-size:28px;font-weight:700;margin-bottom:12px}
 .qr-card .seq .kind{font-size:14px;color:#2563eb}
 .qr-card svg{width:100%;max-width:480px;height:auto}
 .qr-card .info{margin-top:12px;font-size:12px;color:#555}
 .qr-card.header .seq .kind{color:#16a34a}
</style></head><body>
<h1>UPIS 점검 — $([System.Web.HttpUtility]::HtmlEncode($cfg.site_name))</h1>
<div class="meta">
  $($snapshot.round) · $($snapshot.tier.ToUpper()) · 호스트 <b>$($snapshot.host.hostname)</b>
  · 점검자 $([System.Web.HttpUtility]::HtmlEncode($cfg.inspector))
  · 수집 $($snapshot.taken_at)
</div>
<div class="summary">
  <div>총 <b>$($snapshot.summary.total_items)</b></div>
  <div>정상 <span class="ok">$ok</span></div>
  <div>경고 <span class="warn">$warn</span></div>
  <div>심각 <span class="crit">$crit</span></div>
  <div>수동 $manual</div>
</div>
<table>
<thead><tr><th>ID</th><th>항목</th><th>상태</th><th>값</th></tr></thead>
<tbody>$rows</tbody>
</table>

<div class="qr-section">
  <h2>QR 코드 — 갤럭시탭 PWA 스캐너로 캡처</h2>
  <p style="color:#555;margin:0 0 4px;">
    총 <b>$($qr.Frames.Count)</b> 프레임 — payload <b>$($qr.RawBytes)</b> B → gzip <b>$($qr.GzBytes)</b> B → base45 <b>$($qr.B45Chars)</b> chars · sha=<b>$($qr.Hash)</b>
  </p>
  <p style="color:#777;margin:0 0 8px;font-size:13px;">
    스캔 순서: 헤더(seq=0) → 데이터(seq=1..N). 임의 순서 가능. 모니터 글레어 시 화면 밝기 ↑, 카메라 거리 30~50 cm.
  </p>
  <div class="qr-grid" id="qrGrid"></div>
</div>

<div class="footer">
  agent-version $($snapshot.agent_version) · took $($snapshot.took_ms) ms
</div>

<script>
$qrLibCode
</script>
<script>
(function() {
    var frames = $framesJson;
    if (!Array.isArray(frames)) { frames = [frames]; }
    var grid = document.getElementById('qrGrid');
    frames.forEach(function(f, i) {
        var q = qrcode(0, 'M');                  // type=auto, ECC=Medium
        q.addData(JSON.stringify(f));
        q.make();
        var svg = q.createSvgTag({ scalable: true, margin: 4 });
        var isHeader = (f.seq === 0);
        var card = document.createElement('div');
        card.className = 'qr-card' + (isHeader ? ' header' : '');
        var seqLabel = isHeader
            ? '<span class="kind">HEADER</span> seq 0'
            : '<span class="kind">DATA</span> seq ' + f.seq + ' / ' + (f.total || frames.length - 1);
        var info = isHeader
            ? 'id=' + f.id + ' · total=' + f.total + ' · hash=' + f.hash
            : 'crc=' + (f.chk || '-') + ' · ' + (f.d ? f.d.length : 0) + ' chars';
        card.innerHTML = '<div class="label">Frame ' + (i + 1) + ' of ' + frames.length + '</div>'
                       + '<div class="seq">' + seqLabel + '</div>'
                       + svg
                       + '<div class="info">' + info + '</div>';
        grid.appendChild(card);
    });
})();
</script>
</body></html>
"@

# PS 4.0 호환 — BOM 없는 UTF-8 로 저장 (.NET 직접 호출)
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($summaryPath, $html, $enc)

Write-Log -Level INFO -Msg "===== DONE ====="
# [Console]::WriteLine 사용 — Write-Host 의 콘솔 버퍼 cursor 버그 회피 (Win32 0x1F).
[Console]::WriteLine("")
[Console]::WriteLine("결과:")
[Console]::WriteLine(("  snapshot : {0}" -f $snapPath))
[Console]::WriteLine(("  delta    : {0}" -f $deltaPath))
[Console]::WriteLine(("  frames   : {0}" -f $framesPath))
[Console]::WriteLine(("  summary  : {0}" -f $summaryPath))
[Console]::WriteLine("")
[Console]::WriteLine(("자동: ok={0} warn={1} crit={2} | 수동 대기: {3} | 에러: {4}" -f `
    $ok, $warn, $crit, $manual, $snapshot.summary.errors))
