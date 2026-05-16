<#
.SYNOPSIS
  UPIS 점검 자동화 에이전트 — 대화형 사이트 설정 wizard.

.DESCRIPTION
  처음 사용 시 (또는 사이트 추가/변경 시) 본 스크립트를 실행해서 사이트 정보를 입력한다.
  완료 후 config\active.json + config\site.{code}.json 이 생성되고, 다음부터는
  inspect.bat 더블클릭만으로 점검 가능.

  FR-1 (inspection-agent-v2-setup):
   - Read-Host 로 항목별 입력 + 검증
   - 패스워드는 DPAPI 암호화 후 저장 (plain text 금지)
   - 입력 직후 1 회 연결 테스트 (SSH/Telnet)
   - 한글 입력 안전: chcp 65001 + [Console]::InputEncoding = UTF-8

.PARAMETER ConfigDir
  config 디렉토리 경로. 기본: ./config

.PARAMETER NonInteractive
  자동화 테스트용 — 모든 입력을 환경변수에서 받음. (V-1 검증용)
#>

[CmdletBinding()]
param(
    [string] $ConfigDir = (Join-Path $PSScriptRoot 'config'),
    [switch] $NonInteractive
)

$ErrorActionPreference = 'Stop'

# ── 한글 입력 안전화 ────────────────────────────────────────────────────────
try {
    chcp 65001 | Out-Null
    [Console]::InputEncoding  = New-Object System.Text.UTF8Encoding($false)
    [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
    $OutputEncoding           = [Console]::OutputEncoding
} catch {}

# ── lib 로드 ───────────────────────────────────────────────────────────────
$root = $PSScriptRoot
. (Join-Path $root 'lib\DPAPI.ps1')
. (Join-Path $root 'lib\Ssh.ps1')
. (Join-Path $root 'lib\Telnet.ps1')
. (Join-Path $root 'lib\Common.ps1')

# ── 헬퍼 ───────────────────────────────────────────────────────────────────
function _Banner {
    Write-Host ""
    Write-Host "  ╔══════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "  ║  UPIS 점검 자동화 에이전트 — 사이트 설정 wizard v0.2.0           ║" -ForegroundColor Cyan
    Write-Host "  ╠══════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
    Write-Host "  ║  처음 사용 시 1 회만 진행하면, 다음 실행부터 저장된 설정으로     ║" -ForegroundColor Cyan
    Write-Host "  ║  자동 점검됩니다.                                                 ║" -ForegroundColor Cyan
    Write-Host "  ║  패스워드는 Windows DPAPI 로 암호화 — 본 사용자 계정만 복호화.    ║" -ForegroundColor Cyan
    Write-Host "  ╚══════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function _AskString {
    param(
        [string] $Prompt,
        [string] $Default,
        [string] $Pattern,
        [string] $PatternHint
    )
    while ($true) {
        $hint = if ($Default) { " [기본: $Default]" } else { "" }
        $raw = Read-Host -Prompt "  $Prompt$hint"
        if (-not $raw) { $raw = $Default }
        if (-not $raw) { Write-Host "    ⚠ 값 필요" -ForegroundColor Yellow; continue }
        if ($Pattern -and ($raw -notmatch $Pattern)) {
            Write-Host "    ⚠ 형식 안 맞음 — $PatternHint" -ForegroundColor Yellow
            continue
        }
        return $raw.Trim()
    }
}

function _AskChoice {
    param([string] $Prompt, [string[]] $Choices, [string] $Default)
    $hint = "[" + ($Choices -join '|') + "]"
    while ($true) {
        $raw = Read-Host -Prompt "  $Prompt $hint [기본: $Default]"
        if (-not $raw) { return $Default }
        $raw = $raw.Trim().ToLower()
        if ($Choices -contains $raw) { return $raw }
        Write-Host "    ⚠ $hint 중 하나 선택" -ForegroundColor Yellow
    }
}

function _AskPassword {
    param([string] $Prompt)
    while ($true) {
        $sec = Read-Host -Prompt "  $Prompt (입력 시 마스킹됨)" -AsSecureString
        if ($sec.Length -lt 1) { Write-Host "    ⚠ 빈 패스워드 안 됨" -ForegroundColor Yellow; continue }
        # SecureString → plain (DPAPI 암호화 함수가 plain string 받음)
        $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
        try {
            $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
        } finally {
            [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
        return $plain
    }
}

# 지자체명 → site_code 매핑 hint (사용자가 그대로 받거나 수정 가능)
function _SuggestSiteCode {
    param([string] $SiteNameKo)
    $map = @{
        '강진군' = 'gangjin'
        '단양군' = 'danyang'
        '청주시' = 'cheongju'
        '부산시' = 'busan'
        '서울시' = 'seoul'
        '대전시' = 'daejeon'
        '광주시' = 'gwangju'
    }
    if ($map.ContainsKey($SiteNameKo)) { return $map[$SiteNameKo] }
    # 한글 → 단순 transliteration 시도 — 못 하면 빈 string (사용자가 직접 입력)
    return ''
}

# ── 메인 흐름 ──────────────────────────────────────────────────────────────
_Banner

# 1. 기존 active.json 확인
$activePath = Join-Path $ConfigDir 'active.json'
if (Test-Path $activePath) {
    Write-Host "  ⚠ 기존 설정 발견: $activePath" -ForegroundColor Yellow
    $confirm = Read-Host -Prompt "  덮어쓸까요? [y/N]"
    if ($confirm -notmatch '^[Yy]$') {
        Write-Host "`n  취소됨 — 기존 설정 유지." -ForegroundColor Yellow
        exit 0
    }
}

# 2. 항목별 입력
Write-Host "[1/3] 사이트 정보" -ForegroundColor Cyan
$siteNameKo = _AskString -Prompt "지자체명 (예: 강진군)" -Pattern '^.{1,30}$' -PatternHint '1~30자'
$systemName = _AskString -Prompt "시스템명" -Default 'UPIS' -Pattern '^.{1,30}$' -PatternHint '1~30자'
$inspector  = _AskString -Prompt "점검자명" -Default $env:USERNAME -Pattern '^.{1,30}$' -PatternHint '1~30자'

$sitecodeDefault = _SuggestSiteCode -SiteNameKo $siteNameKo
$siteCode = _AskString -Prompt "사이트 코드 (영문, 예: gangjin)" -Default $sitecodeDefault `
    -Pattern '^[a-z0-9_-]{1,32}$' -PatternHint '영문소문자/숫자/_/- 1~32자'

Write-Host ""
Write-Host "[2/3] DB 서버 접속 정보" -ForegroundColor Cyan
$dbProto = _AskChoice -Prompt "접속 프로토콜" -Choices @('ssh','telnet') -Default 'ssh'
$dbHost  = _AskString -Prompt "DB 서버 IP 또는 호스트명" `
    -Pattern '^([0-9]{1,3}\.){3}[0-9]{1,3}$|^[a-zA-Z0-9.-]{1,253}$' -PatternHint 'IPv4 또는 hostname'
$portDefault = if ($dbProto -eq 'telnet') { '23' } else { '22' }
$dbPort  = _AskString -Prompt "DB 서버 포트" -Default $portDefault -Pattern '^[0-9]{1,5}$' -PatternHint '1~65535'
$dbUser  = _AskString -Prompt "DB 서버 계정" -Default 'root' -Pattern '^[a-zA-Z0-9._-]{1,30}$' -PatternHint '영문/숫자/._-'

if ($NonInteractive) {
    $dbPwPlain = $env:SETUP_TEST_PASSWORD
    if (-not $dbPwPlain) { throw "NonInteractive 모드 — SETUP_TEST_PASSWORD 환경변수 필요" }
} else {
    $dbPwPlain = _AskPassword -Prompt "DB 서버 패스워드"
}

$dbOs = _AskChoice -Prompt "DB 서버 OS" -Choices @('aix','hpux','linux','solaris') -Default 'linux'

# 3. 입력 요약 + 최종 확인
Write-Host ""
Write-Host "[3/3] 입력 요약" -ForegroundColor Cyan
Write-Host "  지자체명     : $siteNameKo"
Write-Host "  시스템명     : $systemName"
Write-Host "  점검자       : $inspector"
Write-Host "  site_code    : $siteCode"
Write-Host ("  접속         : {0}://{1}@{2}:{3}  ({4})" -f $dbProto, $dbUser, $dbHost, $dbPort, $dbOs)
Write-Host "  패스워드     : ******** (DPAPI 암호화 후 저장)"
Write-Host ""

if (-not $NonInteractive) {
    $proceed = Read-Host -Prompt "  연결 테스트 + 저장 진행? [Y/n]"
    if ($proceed -match '^[Nn]$') { Write-Host "`n  취소됨." -ForegroundColor Yellow; exit 0 }
}

# 4. 연결 테스트
Write-Host ("`n  [연결 테스트] {0}://{1}:{2} ..." -f $dbProto, $dbHost, $dbPort) -ForegroundColor Cyan
$testRemote = [PSCustomObject]@{
    proto    = $dbProto
    host     = $dbHost
    port     = [int]$dbPort
    user     = $dbUser
    password = $dbPwPlain
    auth     = 'password'
}
$ping = Invoke-Remote -Remote $testRemote -Command "echo OK; uname -a" -TimeoutSec 15

if (-not $ping.ok) {
    Write-Host "  ⚠ 연결 실패: $($ping.stderr)" -ForegroundColor Yellow
    Write-Host "    stdout: $($ping.stdout)" -ForegroundColor DarkYellow
    if (-not $NonInteractive) {
        $retry = Read-Host -Prompt "  그래도 저장? [y/N]"
        if ($retry -notmatch '^[Yy]$') {
            Write-Host "`n  취소됨 — config 저장 안 함." -ForegroundColor Yellow
            $dbPwPlain = $null
            exit 1
        }
    }
} else {
    Write-Host "  ✓ 연결 OK" -ForegroundColor Green
    $unameLine = ($ping.stdout -split "`n" | Where-Object { $_ -notmatch '^OK' -and $_.Trim() } | Select-Object -First 1)
    if ($unameLine) { Write-Host "    원격: $unameLine" -ForegroundColor DarkGray }
}

# 5. 저장
$encPw = Protect-Password -Plain $dbPwPlain
$dbPwPlain = $null  # plain 클리어

$cfg = [ordered]@{
    site       = $siteCode
    site_name  = "$siteNameKo $systemName"
    inspector  = $inspector
    tier       = 'ap'
    hosts      = [ordered]@{ ap = [ordered]@{ expected_hostname = $null; ip_hint = $null } }
    thresholds = [ordered]@{
        'perf.cpu_pct'       = [ordered]@{ warn = 70; crit = 85 }
        'perf.mem_pct'       = [ordered]@{ warn = 75; crit = 90 }
        'perf.swap_pct'      = [ordered]@{ warn = 30; crit = 60 }
        'disk.pct'           = [ordered]@{ warn = 80; crit = 90 }
        'event.error_count' = [ordered]@{ warn = 1; crit = 10 }
        'gws.store_gb'       = [ordered]@{ warn = 15; crit = 25 }
    }
    paths = [ordered]@{
        gss_log_dir   = 'C:\GeoNURIS_Spatial_Server\logs'
        gws_log_dir   = 'C:\Program Files\GeoNURIS_GeoWeb_Server_64\logs'
        gws_store_dir = 'C:\Program Files\GeoNURIS_GeoWeb_Server_64\webapps\uwes\store'
        gws_url       = 'http://localhost:8880/uwes'
    }
    purge = [ordered]@{
        log_retain_days  = 30
        exclude_patterns = @('*DEM*', '*SLOP*')
        dry_run          = $true
    }
    remotes = [ordered]@{
        unix_db = [ordered]@{
            enabled        = $true
            proto          = $dbProto
            host           = $dbHost
            port           = [int]$dbPort
            user           = $dbUser
            password_dpapi = $encPw
            auth           = 'password'
            os             = $dbOs
            agent_path     = '/home/' + $dbUser + '/agent-unix'
            config_name    = "site.$siteCode.json"
        }
    }
    '_meta' = [ordered]@{
        created_at        = (Get-Date).ToString('o')
        agent_version     = '0.2.0'
        created_by_setup  = $true
        windows_user      = $env:USERNAME
        windows_hostname  = $env:COMPUTERNAME
    }
}

# 저장 — UTF-8 BOM (NFR-5)
if (-not (Test-Path $ConfigDir)) { New-Item -ItemType Directory -Path $ConfigDir | Out-Null }
$json = $cfg | ConvertTo-Json -Depth 10
$utf8Bom = New-Object System.Text.UTF8Encoding($true)
[System.IO.File]::WriteAllText($activePath, $json, $utf8Bom)
$sitePath = Join-Path $ConfigDir "site.$siteCode.json"
[System.IO.File]::WriteAllText($sitePath, $json, $utf8Bom)

Write-Host ""
Write-Host "  ✓ 저장 완료:" -ForegroundColor Green
Write-Host "      $activePath"
Write-Host "      $sitePath (백업)"
Write-Host ""
Write-Host "  다음 실행: " -NoNewline
Write-Host "inspect.bat 더블클릭" -ForegroundColor Yellow
Write-Host "  재설정:   " -NoNewline
Write-Host "setup.bat 더블클릭" -ForegroundColor Yellow
Write-Host ""
