<#
.SYNOPSIS
  UPIS Inspection Setup - WinForms 단일 폼 wizard.

.DESCRIPTION
  콘솔 Read-Host wizard 의 한글 IME 미지원 + 점검 항목 확장에 따른 단계 폭증을 해소.
  WinForms 단일 폼에 사이트/DB/Oracle 필드 다 배치, Save 1회로 config 저장.

  - 1. 사이트 정보 (지자체/시스템/점검자/사이트코드)
  - 2. DB 서버 접속 (proto/ssh_backend/host/port/user/password/db_os)
  - 3. Oracle 접속 (sid/auth_mode) — ORACLE_HOME 은 원격 자동 탐지 (oratab → find)
  - 임계값은 기본값 자동. 변경 필요시 site.<code>.json 직접 수정.

  자동화/CI 는 setup.ps1 (-NonInteractive) 콘솔 진입점 별도 유지.

.PARAMETER ConfigDir
  Config 저장 디렉토리. 기본 ./config

.NOTES
  PS 4.0 (Server 2012 R2) 호환. WinForms / Drawing assembly.
#>

[CmdletBinding()]
param([string] $ConfigDir)

$ErrorActionPreference = 'Stop'
if (-not $PSScriptRoot) { $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path } else { $scriptDir = $PSScriptRoot }
if (-not $ConfigDir)    { $ConfigDir = Join-Path $scriptDir 'config' }

# ─── trace 로그 (PS 자체 출력 + setup-gui.trace.log 이중) ──────────────
# bat 의 redirect 가 PowerShell GUI hang 단계에서 못 잡을 가능성 대비.
$script:TraceLogPath = Join-Path $scriptDir 'setup-gui.trace.log'
function _Log {
    param([string]$msg)
    $ts = (Get-Date).ToString('HH:mm:ss.fff')
    $line = "[$ts] $msg"
    try { [System.Console]::WriteLine($line) } catch {}
    try { [System.IO.File]::AppendAllText($script:TraceLogPath, $line + "`r`n", (New-Object System.Text.UTF8Encoding($false))) } catch {}
}
# 새 run 마다 trace 로그 truncate
try { [System.IO.File]::WriteAllText($script:TraceLogPath, "=== setup-gui.ps1 start ($(Get-Date -Format 'o')) ===`r`n", (New-Object System.Text.UTF8Encoding($false))) } catch {}

try {
    _Log "stage=init  scriptDir=$scriptDir  ConfigDir=$ConfigDir"
    _Log "stage=init  PS=$($PSVersionTable.PSVersion)  Apt=$([Threading.Thread]::CurrentThread.GetApartmentState())"

    _Log "stage=lib-load  DPAPI.ps1 / Common.ps1 / Ssh.ps1 / Telnet.ps1 / UnixCmd.ps1"
    . (Join-Path $scriptDir 'lib\DPAPI.ps1')
    . (Join-Path $scriptDir 'lib\Common.ps1')
    # Test Connection 버튼이 Invoke-Remote 호출 → ssh/telnet 라우터 lib 필요
    . (Join-Path $scriptDir 'lib\Ssh.ps1')
    . (Join-Path $scriptDir 'lib\Telnet.ps1')
    . (Join-Path $scriptDir 'lib\UnixCmd.ps1')

    _Log "stage=add-type  System.Windows.Forms + System.Drawing"
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing
    _Log "stage=add-type  done"

# ─── helper ─────────────────────────────────────────────────────
function _NewLabel($Parent, [int]$X, [int]$Y, [string]$Text, [int]$W = 130) {
    $l = New-Object System.Windows.Forms.Label
    # ($Y + 3) 명시 grouping — PS 4.0 은 '$X, $Y + 3' 을 @($X, $Y, 3) 3-element array 로 파싱해
    # Point 의 3-arg constructor 찾고 'Cannot find overload' 에러. PS 5+ 는 우연히 통과.
    $l.Location = New-Object System.Drawing.Point($X, ($Y + 3))
    $l.Size     = New-Object System.Drawing.Size($W, 20)
    $l.Text     = $Text
    $Parent.Controls.Add($l)
}
function _NewText($Parent, [int]$X, [int]$Y, [int]$W = 400, [string]$Default = '', [switch]$Password) {
    $t = New-Object System.Windows.Forms.TextBox
    $t.Location = New-Object System.Drawing.Point($X, $Y)
    $t.Size     = New-Object System.Drawing.Size($W, 22)
    $t.Text     = $Default
    if ($Password) { $t.UseSystemPasswordChar = $true }
    $Parent.Controls.Add($t)
    return $t
}
function _NewCombo($Parent, [int]$X, [int]$Y, [int]$W = 200, [string[]]$Items, [string]$Default) {
    $c = New-Object System.Windows.Forms.ComboBox
    $c.Location      = New-Object System.Drawing.Point($X, $Y)
    $c.Size          = New-Object System.Drawing.Size($W, 22)
    $c.DropDownStyle = 'DropDownList'
    foreach ($i in $Items) { [void]$c.Items.Add($i) }
    if ($Default) { $c.SelectedItem = $Default }
    $Parent.Controls.Add($c)
    return $c
}

# ─── form ───────────────────────────────────────────────────────
$form = New-Object System.Windows.Forms.Form
$form.Text            = "UPIS Inspection Setup  v0.3.0"
$form.Size            = New-Object System.Drawing.Size(640, 700)
$form.StartPosition   = "CenterScreen"
$form.FormBorderStyle = "FixedDialog"
$form.MaximizeBox     = $false
$form.Font            = New-Object System.Drawing.Font("Segoe UI", 9)

# 1. 사이트 정보
$gbSite = New-Object System.Windows.Forms.GroupBox
$gbSite.Location = New-Object System.Drawing.Point(10, 10)
$gbSite.Size     = New-Object System.Drawing.Size(605, 150)
$gbSite.Text     = "1. 사이트 정보"
$form.Controls.Add($gbSite)
_NewLabel $gbSite 15 25 "지자체 이름 (한글)"
$tbSiteNameKo = _NewText $gbSite 155 25 420
_NewLabel $gbSite 15 55 "시스템 명"
$tbSystemName = _NewText $gbSite 155 55 420 'UPIS'
_NewLabel $gbSite 15 85 "점검자 이름 (한글)"
$tbInspector  = _NewText $gbSite 155 85 420 $env:USERNAME
_NewLabel $gbSite 15 115 "사이트 코드 (ascii)"
$tbSiteCode   = _NewText $gbSite 155 115 200

# 2. DB 서버 접속
$gbDb = New-Object System.Windows.Forms.GroupBox
$gbDb.Location = New-Object System.Drawing.Point(10, 170)
$gbDb.Size     = New-Object System.Drawing.Size(605, 240)
$gbDb.Text     = "2. DB 서버 접속"
$form.Controls.Add($gbDb)
_NewLabel  $gbDb 15 25 "Protocol"
$cbProto      = _NewCombo $gbDb 155 25 150 @('ssh','telnet') 'telnet'
_NewLabel  $gbDb 15 55 "SSH backend"
$cbSshBackend = _NewCombo $gbDb 155 55 150 @('auto','openssh','plink') 'auto'
_NewLabel  $gbDb 15 85 "Host (IP/hostname)"
$tbDbHost     = _NewText  $gbDb 155 85 420
_NewLabel  $gbDb 15 115 "Port"
$tbDbPort     = _NewText  $gbDb 155 115 100 '23'
_NewLabel  $gbDb 15 145 "Account"
$tbDbUser     = _NewText  $gbDb 155 145 200 'root'
_NewLabel  $gbDb 15 175 "Password"
$tbDbPwd      = _NewText  $gbDb 155 175 200 '' -Password
_NewLabel  $gbDb 15 205 "DB OS"
$cbDbOs       = _NewCombo $gbDb 155 205 150 @('aix','linux','solaris','hpux') 'aix'

# proto 변경 시 SSH backend 활성/비활성 + port 기본 토글
$onProtoChange = {
    if ($cbProto.SelectedItem -eq 'ssh') {
        $cbSshBackend.Enabled = $true
        if ($tbDbPort.Text -eq '23') { $tbDbPort.Text = '22' }
    } else {
        $cbSshBackend.Enabled = $false
        if ($tbDbPort.Text -eq '22') { $tbDbPort.Text = '23' }
    }
}
$cbProto.Add_SelectedIndexChanged($onProtoChange)
& $onProtoChange   # 초기 상태 반영

# 3. Oracle 접속 — ORACLE_HOME 은 원격 자동 탐지 (lib/Oracle.ps1 Resolve-OracleHome).
# hint 칸은 GUI 에서 제거 (config 직접 수정 시 oracle_home_hint 키로 override 가능).
$gbOra = New-Object System.Windows.Forms.GroupBox
$gbOra.Location = New-Object System.Drawing.Point(10, 420)
$gbOra.Size     = New-Object System.Drawing.Size(605, 90)
$gbOra.Text     = "3. Oracle 접속 (sysdba 권장, 점검 SQL 은 SELECT 전용 가드 / ORACLE_HOME 자동탐지)"
$form.Controls.Add($gbOra)
_NewLabel $gbOra 15 25 "SID 또는 SERVICE"
$tbOraSid  = _NewText  $gbOra 155 25 200
_NewLabel $gbOra 15 55 "Auth mode"
$cbOraAuth = _NewCombo $gbOra 155 55 200 @('sysdba','normal') 'sysdba'

# status + buttons
$lblStatus = New-Object System.Windows.Forms.Label
$lblStatus.Location  = New-Object System.Drawing.Point(15, 555)
$lblStatus.Size      = New-Object System.Drawing.Size(600, 40)
$lblStatus.Text      = "Test 로 연결 사전 확인 → Save 로 config 저장. Password 는 DPAPI 암호화."
$lblStatus.ForeColor = [System.Drawing.Color]::DimGray
$form.Controls.Add($lblStatus)

# Test Connection — TCP 확인 → 로그인+명령 확인 → 결과 진단 (IP오류 vs 비번오류 vs 성공 분리).
$btnTest = New-Object System.Windows.Forms.Button
$btnTest.Location     = New-Object System.Drawing.Point(15, 605)
$btnTest.Size         = New-Object System.Drawing.Size(150, 32)
$btnTest.Text         = "Test Connection"
$form.Controls.Add($btnTest)
$btnTest.Add_Click({
    # 입력 사전 검증 (host/port/user/password 만 있으면 됨, 사이트코드 등은 Save 때 검증)
    if (-not $tbDbHost.Text.Trim()) {
        [System.Windows.Forms.MessageBox]::Show("DB Host 비어있음", "Test 검증", 'OK', 'Warning') | Out-Null; return
    }
    if ($tbDbPort.Text -notmatch '^[0-9]{1,5}$') {
        [System.Windows.Forms.MessageBox]::Show("DB Port 1~65535", "Test 검증", 'OK', 'Warning') | Out-Null; return
    }
    if (-not $tbDbUser.Text.Trim()) {
        [System.Windows.Forms.MessageBox]::Show("DB Account 비어있음", "Test 검증", 'OK', 'Warning') | Out-Null; return
    }
    if (-not $tbDbPwd.Text) {
        [System.Windows.Forms.MessageBox]::Show("DB Password 비어있음", "Test 검증", 'OK', 'Warning') | Out-Null; return
    }

    $dbHostStr = $tbDbHost.Text.Trim()
    $dbPortNum = [int]$tbDbPort.Text
    $btnTest.Enabled = $false
    $btnTest.Text    = "Testing..."
    $form.Refresh()

    try {
        # ─── 1단계: TCP 소켓 연결 (IP/port/방화벽 진단) ────────────────
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcpOk = $false
        $tcpErr = ''
        try {
            $iar = $tcp.BeginConnect($dbHostStr, $dbPortNum, $null, $null)
            if ($iar.AsyncWaitHandle.WaitOne(5000)) {
                $tcp.EndConnect($iar)
                $tcpOk = $true
            } else {
                $tcpErr = "TCP connect timeout (5s)"
            }
        } catch {
            $tcpErr = $_.Exception.Message
        } finally {
            $tcp.Close()
        }

        if (-not $tcpOk) {
            [System.Windows.Forms.MessageBox]::Show(
                ("[1] TCP 연결 실패 - 네트워크 / IP / Port 문제`r`n`r`n" +
                 "대상: " + $dbHostStr + ":" + $dbPortNum + "`r`n" +
                 "사유: " + $tcpErr + "`r`n`r`n" +
                 "확인사항:`r`n" +
                 "  - Host (IP/호스트명) 정확한지`r`n" +
                 "  - Port 번호 (telnet=23, ssh=22) 맞는지`r`n" +
                 "  - 방화벽/네트워크 (ping 가능?)`r`n" +
                 "  - 원격 서버 telnet/ssh 데몬 기동 여부"),
                "Test 실패: 네트워크", 'OK', 'Error') | Out-Null
            return
        }

        # ─── 2단계: 로그인 + 단순 명령 echo ────────────────────────────
        # 임시 DPAPI 암호화 → Invoke-Remote 가 password_dpapi 를 unprotect 함.
        # ⚠ PSCustomObject 필수 — lib/Telnet.ps1, lib/Ssh.ps1 의 라우터/백엔드가 $Remote.PSObject.Properties['xxx']
        #   패턴을 쓰는데, hashtable 은 PSObject.Properties 로 사용자 키를 노출하지 않아서 proto 인식 실패 →
        #   ssh fallback → Process.Start("") 'file name has not been provided' 발생 (2026-05-17 강진 확인).
        $encPwTemp = Protect-Password -Plain $tbDbPwd.Text
        $testRemote = [PSCustomObject]@{
            enabled        = $true
            proto          = $cbProto.SelectedItem
            ssh_backend    = if ($cbProto.SelectedItem -eq 'ssh') { $cbSshBackend.SelectedItem } else { $null }
            host           = $dbHostStr
            port           = $dbPortNum
            user           = $tbDbUser.Text.Trim()
            password_dpapi = $encPwTemp
            auth           = 'password'
            os             = $cbDbOs.SelectedItem
        }

        $loginErr = ''
        try {
            $r = Invoke-Remote -Remote $testRemote -Command "echo UPISPINGOK2026SETUPTEST" -TimeoutSec 15
        } catch {
            $loginErr = "Invoke-Remote 예외: " + $_.Exception.Message
            $r = @{ ok = $false; stdout = ''; stderr = $loginErr }
        }

        # marker 가 stdout 에 나오면 로그인+명령 실행 성공으로 간주.
        # `$r.ok` 는 sentinel 파싱 결과라 false positive 가능 (terminal wrap 으로 sentinel regex miss 등) →
        # marker 단독 검증이 더 신뢰성 높음. underscore 제거된 marker 라 wrap cosmetic 영향 없음.
        if ($r.stdout -match 'UPISPINGOK2026SETUPTEST') {
            [System.Windows.Forms.MessageBox]::Show(
                ("[성공] 모든 단계 정상`r`n`r`n" +
                 "  1) TCP 연결: OK (" + $dbHostStr + ":" + $dbPortNum + ")`r`n" +
                 "  2) " + $cbProto.SelectedItem + " 로그인: OK (" + $tbDbUser.Text.Trim() + ")`r`n" +
                 "  3) 원격 명령 echo: OK`r`n`r`n" +
                 "Save 누르면 이 설정으로 config 저장됩니다."),
                "Test OK", 'OK', 'Information') | Out-Null
        } else {
            $errMsg = if ($r.stderr) { [string]$r.stderr } else { [string]$r.stdout }
            $diagnosis = ''
            if ($errMsg -match '3004-007|invalid (login|username|password)|Login incorrect|Permission denied') {
                $diagnosis = "비밀번호 / 사용자명 불일치 또는 계정 잠김"
                $hint = "  - Password 재입력 (대소문자, 공백)`r`n" +
                        "  - User 명 (보통 root)`r`n" +
                        "  - AIX 라면 'lsuser -a account_locked unsuccessful_login_count <user>' 로 잠김 확인`r`n" +
                        "  - 잠겼으면 'chuser account_locked=false unsuccessful_login_count=0 <user>'"
            } elseif ($errMsg -match 'timeout|timed out|sentinel|prompt') {
                $diagnosis = "로그인 후 prompt/sentinel 인식 실패 (timeout)"
                $hint = "  - 비번은 맞을 가능성 — 로그인은 됐으나 prompt 패턴 불일치`r`n" +
                        "  - 셸이 ksh/bash 가 아닌 경우 prompt 변종일 수 있음`r`n" +
                        "  - setup-gui.trace.log 와 inspect 로그 확인"
            } else {
                $diagnosis = "원인 미상"
                $hint = "  - 아래 stderr 메시지 확인"
            }

            $errPreview = if ($errMsg.Length -gt 500) { $errMsg.Substring(0, 500) + " ..." } else { $errMsg }
            [System.Windows.Forms.MessageBox]::Show(
                ("[2] TCP 는 OK, 로그인/명령 실패`r`n`r`n" +
                 "진단: " + $diagnosis + "`r`n`r`n" +
                 "확인사항:`r`n" + $hint + "`r`n`r`n" +
                 "stderr / stdout (앞 500자):`r`n" + $errPreview),
                "Test 실패: 로그인", 'OK', 'Error') | Out-Null
        }
    } finally {
        $btnTest.Enabled = $true
        $btnTest.Text    = "Test Connection"
    }
})

$btnSave = New-Object System.Windows.Forms.Button
$btnSave.Location     = New-Object System.Drawing.Point(420, 605)
$btnSave.Size         = New-Object System.Drawing.Size(90, 32)
$btnSave.Text         = "Save"
$btnSave.DialogResult = 'OK'
$form.Controls.Add($btnSave)
$form.AcceptButton = $btnSave

$btnCancel = New-Object System.Windows.Forms.Button
$btnCancel.Location     = New-Object System.Drawing.Point(525, 605)
$btnCancel.Size         = New-Object System.Drawing.Size(90, 32)
$btnCancel.Text         = "Cancel"
$btnCancel.DialogResult = 'Cancel'
$form.Controls.Add($btnCancel)
$form.CancelButton = $btnCancel

# 기존 active.json 있으면 미리 채움
$activePath = Join-Path $ConfigDir 'active.json'
if (Test-Path $activePath) {
    try {
        $existing = Get-Content $activePath -Raw -Encoding UTF8 | ConvertFrom-Json
        $parts = ($existing.site_name -split ' ', 2)
        if ($parts.Count -eq 2) { $tbSiteNameKo.Text = $parts[0]; $tbSystemName.Text = $parts[1] }
        else                    { $tbSiteNameKo.Text = $existing.site_name }
        $tbInspector.Text = $existing.inspector
        $tbSiteCode.Text  = $existing.site
        if ($existing.remotes -and $existing.remotes.unix_db) {
            $u = $existing.remotes.unix_db
            if ($u.proto)       { $cbProto.SelectedItem = $u.proto }
            if ($u.ssh_backend) { $cbSshBackend.SelectedItem = $u.ssh_backend }
            $tbDbHost.Text = $u.host
            $tbDbPort.Text = [string]$u.port
            $tbDbUser.Text = $u.user
            if ($u.os)          { $cbDbOs.SelectedItem = $u.os }
            if ($u.oracle) {
                if ($u.oracle.sid)       { $tbOraSid.Text  = $u.oracle.sid }
                if ($u.oracle.auth_mode) { $cbOraAuth.SelectedItem = $u.oracle.auth_mode }
                # oracle_home_hint 은 GUI 미노출 (자동 탐지) — 기존 config 값 있으면 cfg 빌드 시 보존
            }
        }
        $lblStatus.Text      = "기존 config 발견 — 필드 미리 채움. Save 시 덮어씀."
        $lblStatus.ForeColor = [System.Drawing.Color]::DarkBlue
    } catch {
        $lblStatus.Text      = "기존 config 읽기 실패 — 새로 입력하세요."
        $lblStatus.ForeColor = [System.Drawing.Color]::DarkRed
    }
}

# 폼 띄움
_Log "stage=show-dialog  calling form.ShowDialog()  (여기서 hang 이면 GUI 가 init 단계에서 멈춤)"
$result = $form.ShowDialog()
_Log "stage=show-dialog  returned: $result"
if ($result -ne 'OK') {
    _Log "stage=cancel  form closed without save"
    exit 0
}

# 입력 검증
function _Validate {
    if (-not $tbSiteNameKo.Text.Trim()) { return "지자체 이름 비어있음" }
    if (-not $tbSystemName.Text.Trim()) { return "시스템 명 비어있음" }
    if (-not $tbInspector.Text.Trim())  { return "점검자 비어있음" }
    if ($tbSiteCode.Text -notmatch '^[a-z0-9_-]{1,32}$') { return "사이트 코드 형식: a-z 0-9 _- (1~32 chars)" }
    if (-not $tbDbHost.Text.Trim()) { return "DB Host 비어있음" }
    if ($tbDbPort.Text -notmatch '^[0-9]{1,5}$') { return "DB Port 1~65535" }
    if (-not $tbDbUser.Text.Trim()) { return "DB Account 비어있음" }
    if (-not $tbDbPwd.Text)         { return "DB Password 비어있음" }
    return $null
}
$err = _Validate
if ($err) {
    [System.Windows.Forms.MessageBox]::Show($err, "Setup error", 'OK', 'Error') | Out-Null
    exit 1
}

# cfg 빌드 + 저장
$siteCode = $tbSiteCode.Text.Trim()
$encPw    = Protect-Password -Plain $tbDbPwd.Text
$tbDbPwd.Text = ''

$cfg = [ordered]@{
    site       = $siteCode
    site_name  = ($tbSiteNameKo.Text.Trim() + ' ' + $tbSystemName.Text.Trim())
    inspector  = $tbInspector.Text.Trim()
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
    event_excludes = @(
        [ordered]@{ provider = 'Schannel'; id = 36887 }
        [ordered]@{ provider = 'Schannel'; id = 36888 }
    )
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
            proto          = $cbProto.SelectedItem
            ssh_backend    = if ($cbProto.SelectedItem -eq 'ssh') { $cbSshBackend.SelectedItem } else { $null }
            host           = $tbDbHost.Text.Trim()
            port           = [int]$tbDbPort.Text
            user           = $tbDbUser.Text.Trim()
            password_dpapi = $encPw
            auth           = 'password'
            os             = $cbDbOs.SelectedItem
            oracle         = [ordered]@{
                sid              = $tbOraSid.Text.Trim()
                auth_mode        = $cbOraAuth.SelectedItem
                # ORACLE_HOME 은 lib/Oracle.ps1 Resolve-OracleHome 가 원격 탐지 (oratab/find, host 별 캐시).
                # 자동 탐지 실패 시에만 config 파일에서 수동 override (이 키에 경로 직접 입력).
                oracle_home_hint = if ($existing -and $existing.remotes -and $existing.remotes.unix_db -and $existing.remotes.unix_db.oracle -and $existing.remotes.unix_db.oracle.oracle_home_hint) { $existing.remotes.unix_db.oracle.oracle_home_hint } else { $null }
            }
            config_name    = "site.$siteCode.json"
        }
    }
    '_meta' = [ordered]@{
        created_at        = (Get-Date).ToString('o')
        agent_version     = '0.3.0'
        setup_via         = 'gui'
        windows_user      = $env:USERNAME
        windows_hostname  = $env:COMPUTERNAME
    }
}

if (-not (Test-Path $ConfigDir)) { New-Item -ItemType Directory -Path $ConfigDir | Out-Null }
$utf8Bom = New-Object System.Text.UTF8Encoding($true)
$json = $cfg | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText($activePath, $json, $utf8Bom)
$sitePath = Join-Path $ConfigDir "site.$siteCode.json"
[System.IO.File]::WriteAllText($sitePath, $json, $utf8Bom)

_Log "stage=save  writing cfg to $activePath and $sitePath"
[System.Windows.Forms.MessageBox]::Show(
    ("저장 완료`r`n`r`n  " + $activePath + "`r`n  " + $sitePath + "`r`n`r`n다음: inspect.bat 더블클릭"),
    "Setup OK", 'OK', 'Information'
) | Out-Null

    _Log "stage=done  exit 0"
} catch {
    _Log ("[FATAL] " + $_.Exception.GetType().FullName + ": " + $_.Exception.Message)
    _Log ($_.ScriptStackTrace)
    try {
        [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Setup ERROR", 'OK', 'Error') | Out-Null
    } catch {}
    throw
}
