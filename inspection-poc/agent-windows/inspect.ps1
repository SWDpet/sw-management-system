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
 table{border-collapse:collapse;background:#fff;width:100%;font-size:13px}
 th,td{border:1px solid #ddd;padding:6px 10px;text-align:left}
 th{background:#f0f0f0}
 .st-warn{background:#fff7ed}
 .st-crit{background:#fee2e2}
 .st-pending_manual{background:#eff6ff}
 .footer{margin-top:20px;color:#555;font-size:13px}
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
<div class="footer">
  QR frames.json 생성 완료 ($($qr.Frames.Count) 프레임, $($qr.RawBytes)B → $($qr.GzBytes)B → $($qr.B45Chars)c, sha=$($qr.Hash))
</div>
</body></html>
"@
Add-Type -AssemblyName System.Web -ErrorAction SilentlyContinue
Set-Content -Path $summaryPath -Value $html -Encoding UTF8

Write-Log -Level INFO -Msg "===== DONE ====="
Write-Host ""
Write-Host "결과:"
Write-Host ("  snapshot : {0}" -f $snapPath)
Write-Host ("  delta    : {0}" -f $deltaPath)
Write-Host ("  frames   : {0}" -f $framesPath)
Write-Host ("  summary  : {0}" -f $summaryPath)
Write-Host ""
Write-Host ("자동: ok={0} warn={1} crit={2} | 수동 대기: {3} | 에러: {4}" -f `
    $ok, $warn, $crit, $manual, $snapshot.summary.errors)
