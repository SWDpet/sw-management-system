<#
.SYNOPSIS
  UPIS inspection agent - interactive site setup wizard (PS 4.0 compatible).

.DESCRIPTION
  On first use, run this wizard to enter site info. Creates config\active.json
  + config\site.{code}.json. Subsequent runs use the saved config.

  Korean characters in console output cause mojibake on Server 2012 R2 raster fonts
  and Write-Host 0x1F errors with -ForegroundColor. All prompts/messages are ASCII;
  user inputs (e.g. site name in Korean) are still accepted and stored UTF-8.

.PARAMETER ConfigDir
  Config directory. Default: ./config

.PARAMETER NonInteractive
  Automation mode - reads all inputs from environment variables.
#>

[CmdletBinding()]
param(
    [string] $ConfigDir,
    [switch] $NonInteractive
)

$ErrorActionPreference = 'Stop'

# PS 4.0 (Server 2012 R2) trap - $PSScriptRoot can be empty in param() default.
if (-not $PSScriptRoot) {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
} else {
    $scriptDir = $PSScriptRoot
}
if (-not $ConfigDir) {
    $ConfigDir = Join-Path $scriptDir 'config'
}

# Console encoding - UTF-8 for input (Korean site names) + output.
try {
    chcp 65001 | Out-Null
    [Console]::InputEncoding  = New-Object System.Text.UTF8Encoding($false)
    [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
    $OutputEncoding           = [Console]::OutputEncoding
} catch {}

# Server 2012 R2 + PS 4.0 콘솔 Read-Host 는 한글 IME 입력을 받지 못함 (RDP 시 더 심함).
# 한글 가능 필드는 WinForms InputBox 로 띄움 - IME 정상 동작 보장. 실패 시 콘솔 fallback.
$script:GuiAvailable = $false
try {
    Add-Type -AssemblyName Microsoft.VisualBasic -ErrorAction Stop
    $script:GuiAvailable = $true
} catch {
    # 헤드리스/제약 환경 — Read-Host fallback
}
# 자동화 모드에서는 GUI 다이얼로그가 stdin 파이프를 막으니 강제로 콘솔 경로 사용.
if ($NonInteractive) { $script:GuiAvailable = $false }

# Load libs.
$root = $scriptDir
. (Join-Path $root 'lib\DPAPI.ps1')
. (Join-Path $root 'lib\Ssh.ps1')
. (Join-Path $root 'lib\Telnet.ps1')
. (Join-Path $root 'lib\Common.ps1')

# ============================================================================
# Helpers - use [Console]::WriteLine to avoid Write-Host 0x1F trap (PS 4.0/5.x)
# ============================================================================

function _Say([string]$msg) { [Console]::WriteLine($msg) }

function _Banner {
    _Say ""
    _Say "  ============================================================"
    _Say "    UPIS Inspection Setup Wizard  v0.2.0"
    _Say "  ============================================================"
    _Say "    Run once on first use. Subsequent runs reuse saved config."
    _Say "    Password is DPAPI-encrypted (only your Win account decrypts)."
    _Say "  ============================================================"
    _Say ""
}

function _AskString {
    param(
        [string] $Prompt,
        [string] $Default,
        [string] $Pattern,
        [string] $PatternHint
    )
    while ($true) {
        $hint = if ($Default) { " [default: $Default]" } else { "" }
        $raw = Read-Host -Prompt "  $Prompt$hint"
        if (-not $raw) { $raw = $Default }
        if (-not $raw) { _Say "    [!] required"; continue }
        if ($Pattern -and ($raw -notmatch $Pattern)) {
            _Say ("    [!] invalid format - " + $PatternHint)
            continue
        }
        return $raw.Trim()
    }
}

# 한글 IME 입력이 필요한 필드 전용. WinForms InputBox 로 GUI 다이얼로그 띄움.
# GUI 실패 시 Read-Host 로 떨어지지만 그 경우 한글은 ASCII 로 입력해야 함.
function _AskStringKo {
    param(
        [string] $Prompt,
        [string] $Default,
        [string] $Pattern,
        [string] $PatternHint
    )
    if (-not $script:GuiAvailable) {
        _Say "    [warn] GUI unavailable - type in ASCII"
        return (_AskString -Prompt $Prompt -Default $Default -Pattern $Pattern -PatternHint $PatternHint)
    }
    $title = "UPIS Setup"
    while ($true) {
        $msg = $Prompt
        if ($PatternHint) { $msg = $msg + "`r`n(" + $PatternHint + ")" }
        $raw = [Microsoft.VisualBasic.Interaction]::InputBox($msg, $title, $Default)
        if (-not $raw) { $raw = $Default }
        if (-not $raw) { _Say "    [!] required"; continue }
        if ($Pattern -and ($raw -notmatch $Pattern)) {
            _Say ("    [!] invalid format - " + $PatternHint)
            continue
        }
        $raw = $raw.Trim()
        # 콘솔 raster 폰트로는 한글이 깨질 수 있어 길이만 echo.
        _Say ("    [ok] {0} captured ({1} chars)" -f $Prompt, $raw.Length)
        return $raw
    }
}

function _AskChoice {
    param([string] $Prompt, [string[]] $Choices, [string] $Default)
    $hint = "[" + ($Choices -join '|') + "]"
    while ($true) {
        $raw = Read-Host -Prompt "  $Prompt $hint [default: $Default]"
        if (-not $raw) { return $Default }
        $raw = $raw.Trim().ToLower()
        if ($Choices -contains $raw) { return $raw }
        _Say ("    [!] choose one of " + $hint)
    }
}

function _AskPassword {
    param([string] $Prompt)
    while ($true) {
        $sec = Read-Host -Prompt "  $Prompt (masked)" -AsSecureString
        if ($sec.Length -lt 1) { _Say "    [!] empty not allowed"; continue }
        $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
        try {
            $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
        } finally {
            [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
        return $plain
    }
}

# Korean site name -> site_code hint (user can override).
function _SuggestSiteCode {
    param([string] $SiteNameKo)
    $map = @{
        'gangjin' = 'gangjin'; 'danyang' = 'danyang'
        'cheongju' = 'cheongju'; 'busan' = 'busan'
        'seoul' = 'seoul'; 'daejeon' = 'daejeon'; 'gwangju' = 'gwangju'
    }
    # try lower-cased local match
    $lc = $SiteNameKo.ToLower()
    if ($map.ContainsKey($lc)) { return $map[$lc] }
    # Korean direct hash (no transliteration in PS 4.0 - user types)
    if ($SiteNameKo -eq ([char]0xAC15 + [char]0xC9C4 + [char]0xAD70)) { return 'gangjin' }   # 강진군
    if ($SiteNameKo -eq ([char]0xB2E8 + [char]0xC591 + [char]0xAD70)) { return 'danyang' }   # 단양군
    return ''
}

# ============================================================================
# Main
# ============================================================================
_Banner

# 1. Existing active.json check
$activePath = Join-Path $ConfigDir 'active.json'
if (Test-Path $activePath) {
    _Say ("  [!] existing config found: " + $activePath)
    $confirm = Read-Host -Prompt "  overwrite? [y/N]"
    if ($confirm -notmatch '^[Yy]$') {
        _Say ""
        _Say "  cancelled - keeping existing config."
        exit 0
    }
}

# 2. Inputs
_Say "[1/3] Site info"
if ($script:GuiAvailable) { _Say "  (Korean fields open a GUI dialog - IME works there)" }
$siteNameKo = _AskStringKo -Prompt "Local government name (e.g. 강진군)" -Pattern '^.{1,30}$' -PatternHint '1-30 chars'
$systemName = _AskStringKo -Prompt "System name" -Default 'UPIS' -Pattern '^.{1,30}$' -PatternHint '1-30 chars'
$inspector  = _AskStringKo -Prompt "Inspector name (e.g. 박욱진)" -Default $env:USERNAME -Pattern '^.{1,30}$' -PatternHint '1-30 chars'

$sitecodeDefault = _SuggestSiteCode -SiteNameKo $siteNameKo
$siteCode = _AskString -Prompt "Site code (lowercase ascii, e.g. gangjin)" -Default $sitecodeDefault `
    -Pattern '^[a-z0-9_-]{1,32}$' -PatternHint 'a-z 0-9 _- 1-32 chars'

_Say ""
_Say "[2/3] DB server access"
$dbProto = _AskChoice -Prompt "Protocol" -Choices @('ssh','telnet') -Default 'ssh'
# SSH 선택 시 백엔드 분기 - auto/openssh/plink. telnet 은 TcpClient 내장 (선택지 없음).
# auto: bundled plink 있으면 그거, 없으면 ssh.exe (PATH). Win Server 2012 R2 에서 OpenSSH 미설치 환경 대비.
$sshBackend = $null
if ($dbProto -eq 'ssh') {
    $sshBackend = _AskChoice -Prompt "SSH backend" -Choices @('auto','openssh','plink') -Default 'auto'
}
$dbHost  = _AskString -Prompt "DB server IP or hostname" `
    -Pattern '^([0-9]{1,3}\.){3}[0-9]{1,3}$|^[a-zA-Z0-9.-]{1,253}$' -PatternHint 'IPv4 or hostname'
$portDefault = if ($dbProto -eq 'telnet') { '23' } else { '22' }
$dbPort  = _AskString -Prompt "DB server port" -Default $portDefault -Pattern '^[0-9]{1,5}$' -PatternHint '1-65535'
$dbUser  = _AskString -Prompt "DB server account" -Default 'root' -Pattern '^[a-zA-Z0-9._-]{1,30}$' -PatternHint 'a-z 0-9 ._-'

if ($NonInteractive) {
    $dbPwPlain = $env:SETUP_TEST_PASSWORD
    if (-not $dbPwPlain) { throw "NonInteractive mode - SETUP_TEST_PASSWORD env var required" }
} else {
    $dbPwPlain = _AskPassword -Prompt "DB server password"
}

$dbOs = _AskChoice -Prompt "DB server OS" -Choices @('aix','hpux','linux','solaris') -Default 'linux'

# 3. Summary
_Say ""
_Say "[3/3] Summary"
_Say ("  site name    : " + $siteNameKo)
_Say ("  system       : " + $systemName)
_Say ("  inspector    : " + $inspector)
_Say ("  site_code    : " + $siteCode)
$accessStr = if ($sshBackend) {
    "{0}({1})://{2}@{3}:{4}  ({5})" -f $dbProto, $sshBackend, $dbUser, $dbHost, $dbPort, $dbOs
} else {
    "{0}://{1}@{2}:{3}  ({4})" -f $dbProto, $dbUser, $dbHost, $dbPort, $dbOs
}
_Say ("  access       : " + $accessStr)
_Say  "  password     : ******** (DPAPI-encrypted on save)"
_Say ""

if (-not $NonInteractive) {
    $proceed = Read-Host -Prompt "  Test connection and save? [Y/n]"
    if ($proceed -match '^[Nn]$') {
        _Say ""
        _Say "  cancelled."
        exit 0
    }
}

# 4. Connection test (Telnet 매개 시 단계별 디버그 로그 떨굼)
_Say ""
_Say ("  [test] connecting {0}://{1}:{2} ..." -f $dbProto, $dbHost, $dbPort)
$debugLogPath = Join-Path $scriptDir 'out\setup-telnet-debug.log'
if ($dbProto -eq 'telnet') {
    Set-TelnetLog -Path $debugLogPath
    _Say ("    debug log -> " + $debugLogPath)
}
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
    _Say ("  [!] connection failed: " + $ping.stderr)
    if ($ping.stdout) { _Say ("    stdout tail: " + $ping.stdout.Substring([Math]::Max(0,$ping.stdout.Length-200))) }
    if ($dbProto -eq 'telnet' -and (Test-Path $debugLogPath)) {
        _Say ""
        _Say ("  [debug log] " + $debugLogPath)
        _Say "    -> 해당 파일을 메일/USB 로 가져와 분석. 끝의 READ-TIMEOUT 이나 STAGE-FAIL 라인이 핵심."
    }
    if (-not $NonInteractive) {
        $retry = Read-Host -Prompt "  save anyway? [y/N]"
        if ($retry -notmatch '^[Yy]$') {
            _Say ""
            _Say "  cancelled - config not saved."
            $dbPwPlain = $null
            exit 1
        }
    }
} else {
    _Say "  [OK] connection OK"
    $unameLine = ($ping.stdout -split "`n" | Where-Object { $_ -notmatch '^OK' -and $_.Trim() } | Select-Object -First 1)
    if ($unameLine) { _Say ("    remote: " + $unameLine) }
    if ($dbProto -eq 'telnet') {
        _Say ("    (telnet debug log saved: " + $debugLogPath + ")")
    }
}

# 5. Save
$encPw = Protect-Password -Plain $dbPwPlain
$dbPwPlain = $null

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
            ssh_backend    = $sshBackend                  # null for telnet, 'auto'|'openssh'|'plink' for ssh
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

# Save with UTF-8 BOM (NFR-5)
if (-not (Test-Path $ConfigDir)) { New-Item -ItemType Directory -Path $ConfigDir | Out-Null }
$json = $cfg | ConvertTo-Json -Depth 10
$utf8Bom = New-Object System.Text.UTF8Encoding($true)
[System.IO.File]::WriteAllText($activePath, $json, $utf8Bom)
$sitePath = Join-Path $ConfigDir "site.$siteCode.json"
[System.IO.File]::WriteAllText($sitePath, $json, $utf8Bom)

_Say ""
_Say "  [OK] saved:"
_Say ("      " + $activePath)
_Say ("      " + $sitePath + " (backup)")
_Say ""
_Say "  next:    inspect.bat   (double-click)"
_Say "  reset:   setup.bat     (double-click)"
_Say ""
