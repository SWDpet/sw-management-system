<#
.SYNOPSIS
  UPIS Inspection Setup - WinForms 단일 폼 wizard.

.DESCRIPTION
  콘솔 Read-Host wizard 의 한글 IME 미지원 + 점검 항목 확장에 따른 단계 폭증을 해소.
  WinForms 단일 폼에 사이트/DB/Oracle 필드 다 배치, Save 1회로 config 저장.

  - 1. 사이트 정보 (지자체/시스템/점검자/사이트코드)
  - 2. DB 서버 접속 (proto/ssh_backend/host/port/user/password/db_os)
  - 3. Oracle 접속 (sid/auth_mode/oracle_home hint)
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

. (Join-Path $scriptDir 'lib\DPAPI.ps1')
. (Join-Path $scriptDir 'lib\Common.ps1')

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# ─── helper ─────────────────────────────────────────────────────
function _NewLabel($Parent, [int]$X, [int]$Y, [string]$Text, [int]$W = 130) {
    $l = New-Object System.Windows.Forms.Label
    $l.Location = New-Object System.Drawing.Point($X, $Y + 3)
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

# 3. Oracle 접속
$gbOra = New-Object System.Windows.Forms.GroupBox
$gbOra.Location = New-Object System.Drawing.Point(10, 420)
$gbOra.Size     = New-Object System.Drawing.Size(605, 120)
$gbOra.Text     = "3. Oracle 접속 (sysdba 권장, 점검 SQL 은 SELECT 전용 가드)"
$form.Controls.Add($gbOra)
_NewLabel $gbOra 15 25 "SID 또는 SERVICE"
$tbOraSid  = _NewText  $gbOra 155 25 200
_NewLabel $gbOra 15 55 "Auth mode"
$cbOraAuth = _NewCombo $gbOra 155 55 200 @('sysdba','normal') 'sysdba'
_NewLabel $gbOra 15 85 "ORACLE_HOME hint"
$tbOraHome = _NewText  $gbOra 155 85 420

# status + buttons
$lblStatus = New-Object System.Windows.Forms.Label
$lblStatus.Location  = New-Object System.Drawing.Point(15, 555)
$lblStatus.Size      = New-Object System.Drawing.Size(600, 40)
$lblStatus.Text      = "Save 누르면 config\active.json + config\site.<code>.json 으로 저장. Password 는 DPAPI 암호화."
$lblStatus.ForeColor = [System.Drawing.Color]::DimGray
$form.Controls.Add($lblStatus)

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
                if ($u.oracle.sid)              { $tbOraSid.Text  = $u.oracle.sid }
                if ($u.oracle.auth_mode)        { $cbOraAuth.SelectedItem = $u.oracle.auth_mode }
                if ($u.oracle.oracle_home_hint) { $tbOraHome.Text = $u.oracle.oracle_home_hint }
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
$result = $form.ShowDialog()
if ($result -ne 'OK') {
    [System.Console]::WriteLine("[cancel] form closed without save")
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
                oracle_home_hint = if ($tbOraHome.Text.Trim()) { $tbOraHome.Text.Trim() } else { $null }
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

[System.Windows.Forms.MessageBox]::Show(
    ("저장 완료`r`n`r`n  " + $activePath + "`r`n  " + $sitePath + "`r`n`r`n다음: inspect.bat 더블클릭"),
    "Setup OK", 'OK', 'Information'
) | Out-Null
