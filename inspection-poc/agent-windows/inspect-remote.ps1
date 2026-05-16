<#
.SYNOPSIS
  UPIS 점검 자동화 — 원격 Unix 서버 SSH 매개 실행.
.DESCRIPTION
  Windows 노트북에서 실행:
    1) config 의 remotes.unix_db 정보로 Unix(AIX/HP-UX/Linux/Solaris) 서버에 SSH 접속
    2) 원격 agent-unix/inspect.sh 실행 → snapshot.json + frames.json 생성
    3) scp 로 결과 회수 → out\remote-unix\ 에 저장
    4) summary-remote.html (QR 인라인 렌더) 생성 + 브라우저 표시
.PARAMETER ConfigPath
  사이트 설정 JSON. (remotes.unix_db 섹션 enabled=true 필요)
.PARAMETER RemoteKey
  config 의 remotes.* 키 이름. 기본 unix_db.
.EXAMPLE
  .\inspect-remote.ps1 -ConfigPath .\config\site.dyg.json
  .\inspect-remote.ps1 -RemoteKey gis_unix    # GIS Unix 서버 점검
#>
[CmdletBinding()]
param(
    [string] $ConfigPath,
    [string] $RemoteKey  = 'unix_db'
)

$ErrorActionPreference = 'Stop'

try {
    [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
    $OutputEncoding           = [Console]::OutputEncoding
} catch {}

# PS 4.0 (Server 2012 R2) — param() default 에서 $PSScriptRoot 가 빈 문자열인 경우 회피
if ($PSScriptRoot) {
    $root = $PSScriptRoot
} else {
    $root = Split-Path -Parent $MyInvocation.MyCommand.Path
}
. (Join-Path $root 'lib\Common.ps1')
. (Join-Path $root 'lib\DPAPI.ps1')
. (Join-Path $root 'lib\Ssh.ps1')
. (Join-Path $root 'lib\Telnet.ps1')   # v2 — Invoke-Remote 라우터 + Telnet 매개

# v2: -ConfigPath 미지정 시 active.json 우선
if (-not $ConfigPath) {
    $activePath = Join-Path $root 'config\active.json'
    if (Test-Path $activePath) { $ConfigPath = $activePath }
    else { $ConfigPath = Join-Path $root 'config\site.dyg.json' }
}

Write-Log -Level INFO -Msg "===== UPIS 원격 Unix 점검 v0.2.0 ====="
Write-Log -Level INFO -Msg "config:  $ConfigPath"
Write-Log -Level INFO -Msg "remote:  $RemoteKey"

$cfg = Read-SiteConfig -Path $ConfigPath
if (-not $cfg.PSObject.Properties['remotes']) {
    throw "config에 'remotes' 섹션이 없습니다: $ConfigPath"
}
if (-not $cfg.remotes.PSObject.Properties[$RemoteKey]) {
    throw "config.remotes.$RemoteKey 가 정의되어 있지 않습니다."
}
$remote = $cfg.remotes.$RemoteKey
if (-not $remote.enabled) {
    throw "config.remotes.$RemoteKey.enabled = false. 활성화 후 다시 시도하세요."
}

# v2: password_dpapi 자동 복호화 (메모리 한정)
if ($remote.PSObject.Properties['password_dpapi'] -and $remote.password_dpapi -and
    -not ($remote.PSObject.Properties['password'] -and $remote.password)) {
    $remote | Add-Member -NotePropertyName 'password' -NotePropertyValue (Unprotect-Password -Encrypted $remote.password_dpapi) -Force
    if (-not ($remote.PSObject.Properties['auth'] -and $remote.auth)) {
        $remote | Add-Member -NotePropertyName 'auth' -NotePropertyValue 'password' -Force
    }
}

# v2: proto 결정 + 보안 경고
$proto = if ($remote.PSObject.Properties['proto'] -and $remote.proto) { $remote.proto } else { 'ssh' }
if ($proto -eq 'telnet') {
    Write-Log -Level WARN -Msg "Telnet 사용 — 패스워드 평문 전송. SSH 전환 가능 사이트는 SSH 권장."
}

$agentPath = $remote.agent_path
if (-not $agentPath) { throw "config.remotes.$RemoteKey.agent_path 미지정" }
$configName = if ($remote.config_name) { $remote.config_name } else { 'site.dyg.json' }

$outDir       = Join-Path $root 'out'
$remoteOutDir = Join-Path $outDir 'remote-unix'
if (-not (Test-Path $remoteOutDir)) { New-Item -ItemType Directory -Path $remoteOutDir | Out-Null }

# 1) 원격 환경 점검 (perl/sh 가용성) — SSH/Telnet 자동 라우팅
Write-Log -Level INFO -Msg ("원격 ping 중 (proto={0})..." -f $proto)
$ping = Invoke-Remote -Remote $remote -Command "echo OK; uname -a; which perl sh" -TimeoutSec 15
if (-not $ping.ok) {
    Write-Log -Level ERROR -Msg ("SSH 연결 실패 (exit={0}): {1}" -f $ping.exitCode, $ping.stderr)
    throw "SSH 연결 실패. 키/호스트/방화벽 확인 필요."
}
Write-Log -Level INFO -Msg ("원격: {0}" -f ($ping.stdout -split "`n" | Select-Object -First 2 | Out-String).Trim())

# 2) 원격 inspect.sh 실행 (snapshot + frames 동시 생성) — SSH/Telnet 라우팅
$remoteCmd = "cd '$agentPath' && sh ./inspect.sh -c './config/$configName' 2>&1; echo __EC__=`$?"
Write-Log -Level INFO -Msg "원격 inspect.sh 실행 중..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$run = Invoke-Remote -Remote $remote -Command $remoteCmd -TimeoutSec 180
$sw.Stop()

# stdout 끝에 __EC__=N 토큰으로 원격 exit code 추출
$ec = -1
$ecMatch = [regex]::Match($run.stdout, '__EC__=(-?\d+)')
if ($ecMatch.Success) { $ec = [int]$ecMatch.Groups[1].Value }
Write-Log -Level INFO -Msg ("원격 실행 {0} ms, exitCode={1}" -f $sw.ElapsedMilliseconds, $ec)

if ($ec -ne 0) {
    Write-Log -Level ERROR -Msg "원격 inspect.sh 비정상 종료. 마지막 출력:"
    ($run.stdout -split "`n" | Select-Object -Last 20) | ForEach-Object { Write-Log -Level ERROR -Msg "  $_" }
    throw "원격 inspect.sh 실패 (exit=$ec)"
}

# 3) 결과 회수: snapshot.json + frames.json — proto 분기
$snapRemote   = "$agentPath/out/snapshot.json"
$framesRemote = "$agentPath/out/frames.json"
$snapLocal    = Join-Path $remoteOutDir 'snapshot.json'
$framesLocal  = Join-Path $remoteOutDir 'frames.json'

if ($proto -eq 'telnet') {
    Write-Log -Level INFO -Msg "결과 회수 중 (Telnet — cat stdout, FR-3)..."
    $rs = Invoke-Remote -Remote $remote -Command "cat '$snapRemote'" -TimeoutSec 60
    if (-not $rs.ok) { throw "snapshot.json 회수 실패 (telnet cat): $($rs.stderr)" }
    [System.IO.File]::WriteAllText($snapLocal, $rs.stdout, (New-Object System.Text.UTF8Encoding($false)))
    $rf = Invoke-Remote -Remote $remote -Command "cat '$framesRemote'" -TimeoutSec 60
    if (-not $rf.ok) { throw "frames.json 회수 실패 (telnet cat): $($rf.stderr)" }
    [System.IO.File]::WriteAllText($framesLocal, $rf.stdout, (New-Object System.Text.UTF8Encoding($false)))
} else {
    Write-Log -Level INFO -Msg "결과 회수 중 (scp)..."
    $c1 = Copy-RemoteFile -Remote $remote -RemotePath $snapRemote   -LocalPath $snapLocal
    if (-not $c1.ok) { throw "snapshot.json 회수 실패: $($c1.stderr)" }
    $c2 = Copy-RemoteFile -Remote $remote -RemotePath $framesRemote -LocalPath $framesLocal
    if (-not $c2.ok) { throw "frames.json 회수 실패: $($c2.stderr)" }
}

$snapshot = Read-Json -Path $snapLocal
$frames   = Read-Json -Path $framesLocal
if ($null -eq $snapshot) { throw "snapshot.json 비어있음" }
if ($null -eq $frames)   { throw "frames.json 비어있음" }

# 헤더(seq=0) 메타 추출
$header = $frames | Where-Object { $_.seq -eq 0 } | Select-Object -First 1
$rawBytes  = if ($header -and $header.raw_bytes) { $header.raw_bytes } else { 0 }
$gzBytes   = if ($header -and $header.gz_bytes)  { $header.gz_bytes }  else { 0 }
$b45Chars  = if ($header -and $header.b45_chars) { $header.b45_chars } else { 0 }
$sha       = if ($header -and $header.hash)      { $header.hash }      else { '-' }

Write-Log -Level INFO -Msg ("frames={0} raw={1}B gz={2}B b45={3}c sha={4}" -f $frames.Count, $rawBytes, $gzBytes, $b45Chars, $sha)

# 4) summary-remote.html 생성 (Windows summary.html 과 동일 패턴, items 는 원격 snapshot)
$ok   = $snapshot.summary.ok
$warn = $snapshot.summary.warn
$crit = $snapshot.summary.crit
$manual = $snapshot.summary.pending_manual

Add-Type -AssemblyName System.Web -ErrorAction SilentlyContinue

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

$qrLibPath = Join-Path $root 'lib\qrcode.min.js'
if (-not (Test-Path $qrLibPath)) {
    throw "lib\qrcode.min.js 파일이 없습니다: $qrLibPath`n  → 반출 패키지에 lib\ 폴더가 통째로 복사됐는지 확인하세요. 이 파일이 없으면 summary-remote.html 의 QR 카드가 빈 채로 표시됩니다."
}
$qrLibCode = Get-Content -Path $qrLibPath -Raw -Encoding UTF8
if ([string]::IsNullOrWhiteSpace($qrLibCode)) {
    throw "lib\qrcode.min.js 파일이 비어있습니다: $qrLibPath ($((Get-Item $qrLibPath).Length) bytes)`n  → 정상 파일은 8 KB 이상입니다. 복사 누락 또는 파일 손상 가능."
}
$framesJson = $frames | ConvertTo-Json -Depth 6 -Compress

$siteName  = $cfg.site_name
$round     = $snapshot.round
$tier      = $snapshot.tier
$hostname  = $snapshot.host.hostname
$inspector = $cfg.inspector
$takenAt   = $snapshot.taken_at
$totalItems = $snapshot.summary.total_items
$agentVer  = $snapshot.agent_version
$tookMs    = $snapshot.took_ms

$html = @"
<!doctype html>
<html lang="ko"><head><meta charset="utf-8">
<title>UPIS 점검 (원격 Unix) — $siteName / $round</title>
<style>
 body{font-family:'Pretendard','Segoe UI',sans-serif;margin:24px;background:#f5f5f5}
 h1{margin:0 0 4px;font-size:22px}
 .badge{display:inline-block;padding:2px 10px;border-radius:12px;background:#dbeafe;color:#1e40af;font-size:12px;font-weight:700;margin-left:8px;vertical-align:middle}
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
 .qr-section{margin-top:32px;padding-top:24px;border-top:2px solid #7c3aed}
 .qr-section h2{color:#7c3aed;margin:0 0 8px;font-size:22px}
 .qr-grid{display:grid;grid-template-columns:repeat(auto-fit, minmax(360px, 1fr));gap:24px;margin-top:16px}
 .qr-card{background:#fff;border:1px solid #ddd;border-radius:12px;padding:20px;text-align:center;box-shadow:0 2px 8px rgba(0,0,0,0.04)}
 .qr-card .label{font-size:14px;color:#777;margin-bottom:4px}
 .qr-card .seq{font-size:28px;font-weight:700;margin-bottom:12px}
 .qr-card .seq .kind{font-size:14px;color:#2563eb}
 .qr-card svg{width:100%;max-width:480px;height:auto}
 .qr-card .info{margin-top:12px;font-size:12px;color:#555}
 .qr-card.header .seq .kind{color:#16a34a}
 .telnet-banner{background:#fef3c7;border:2px solid #f59e0b;color:#92400e;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-weight:600}
</style></head><body>
$(if ($proto -eq 'telnet') { '<div class="telnet-banner">⚠ <b>본 점검은 Telnet 사용</b> — 패스워드 평문 전송. SSH 전환 가능한 사이트는 SSH 사용 권장.</div>' } else { '' })
<h1>UPIS 점검 — $([System.Web.HttpUtility]::HtmlEncode($siteName)) <span class="badge">원격 Unix · $($proto.ToUpper())</span></h1>
<div class="meta">
  $round · $($tier.ToUpper()) · 호스트 <b>$hostname</b>
  · 점검자 $([System.Web.HttpUtility]::HtmlEncode($inspector))
  · $($proto.ToUpper()) $($remote.user)@$($remote.host)
  · 수집 $takenAt
</div>
<div class="summary">
  <div>총 <b>$totalItems</b></div>
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
    총 <b>$($frames.Count)</b> 프레임 — payload <b>$rawBytes</b> B → gzip <b>$gzBytes</b> B → base45 <b>$b45Chars</b> chars · sha=<b>$sha</b>
  </p>
  <p style="color:#777;margin:0 0 8px;font-size:13px;">
    원격 $($remote.host) 의 점검 결과입니다. 스캔 순서: 헤더(seq=0) → 데이터(seq=1..N). 임의 순서 가능.
  </p>
  <div class="qr-grid" id="qrGrid"></div>
</div>

<div class="footer">
  agent-version $agentVer · 원격 실행 $($sw.ElapsedMilliseconds) ms · 원격 took $tookMs ms
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
        var q = qrcode(0, 'M');
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

$summaryPath = Join-Path $outDir 'summary-remote.html'
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($summaryPath, $html, $enc)

Write-Log -Level INFO -Msg "===== DONE ====="
[Console]::WriteLine("")
[Console]::WriteLine("결과:")
[Console]::WriteLine(("  snapshot : {0}" -f $snapLocal))
[Console]::WriteLine(("  frames   : {0}" -f $framesLocal))
[Console]::WriteLine(("  summary  : {0}" -f $summaryPath))
[Console]::WriteLine("")
[Console]::WriteLine(("자동: ok={0} warn={1} crit={2} | 수동 대기: {3}" -f $ok, $warn, $crit, $manual))
