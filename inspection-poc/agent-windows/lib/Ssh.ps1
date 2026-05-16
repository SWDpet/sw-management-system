# ── SSH 헬퍼 ────────────────────────────────────────────────────
# Windows 점검 에이전트 → Unix(AIX/HP-UX/Linux/Solaris) 원격 명령 실행
#
# 우선순위:
#   1) OpenSSH (Win10/Server2019+ 기본 탑재) — 키 기반 인증 권장
#   2) PuTTY plink.exe — 키(.ppk) 또는 비밀번호 fallback
#
# 사이트 설정 (예) ─ config/site.dyg.json:
#   "remotes": {
#     "gis_unix": {
#       "host": "10.0.0.20",
#       "user": "geonuris",
#       "port": 22,
#       "auth": "key",                 # key | password | plink
#       "key": "C:\\Users\\admin\\.ssh\\id_rsa",
#       "password": null,              # auth=password 일 때만
#       "plink_path": null             # auth=plink 또는 ssh 미설치 시
#     }
#   }

. (Join-Path $PSScriptRoot 'Common.ps1')
. (Join-Path $PSScriptRoot 'DPAPI.ps1')   # Unprotect-Password — plink -pw 에 DPAPI 디코딩 패스 전달용

# 번들 도구 경로 — agent-windows/tools/plink.exe (PATH 보다 우선).
# Windows Server 2012 R2 등 OpenSSH 클라이언트 미설치 환경에서 자기완결적 배포 위해 사용.
$script:BundledToolsDir = Join-Path (Split-Path -Parent $PSScriptRoot) 'tools'

function _FindBundledTool {
    param([string]$Name)
    $p = Join-Path $script:BundledToolsDir $Name
    if (Test-Path $p) { return $p }
    return $null
}

function Test-SshAvailable {
    # 우선순위: bundled plink → ssh.exe (PATH) → plink.exe (PATH)
    # bundled plink 가 PATH 의 ssh.exe 보다 먼저 — 폐쇄망 동봉 배포 일관성 보장.
    $bundledPlink = _FindBundledTool 'plink.exe'
    if ($bundledPlink) { return @{ Client = 'plink'; Path = $bundledPlink; Source = 'bundled' } }

    $ssh = Get-Command ssh.exe -ErrorAction SilentlyContinue
    if ($ssh) { return @{ Client = 'ssh'; Path = $ssh.Source; Source = 'path' } }

    $plink = Get-Command plink.exe -ErrorAction SilentlyContinue
    if ($plink) { return @{ Client = 'plink'; Path = $plink.Source; Source = 'path' } }

    return $null
}

function Resolve-SshClient {
    param($Remote)
    # ssh_backend 명시: 'auto' | 'openssh' | 'plink'. (auth=plink / plink_path 도 plink 강제)
    $backend = if ($Remote.PSObject.Properties['ssh_backend'] -and $Remote.ssh_backend) { $Remote.ssh_backend } else { 'auto' }

    if ($backend -eq 'openssh') {
        $ssh = Get-Command ssh.exe -ErrorAction SilentlyContinue
        if (-not $ssh) { throw "ssh_backend=openssh 인데 ssh.exe 없음 (Win10/Server 2019+ 또는 OpenSSH Client 기능 설치 필요)." }
        return @{ Client = 'ssh'; Path = $ssh.Source; Source = 'path' }
    }
    if ($backend -eq 'plink' -or $Remote.auth -eq 'plink' -or $Remote.plink_path) {
        $p = $Remote.plink_path
        if (-not $p) { $p = _FindBundledTool 'plink.exe' }
        if (-not $p) { $p = (Get-Command plink.exe -ErrorAction SilentlyContinue).Source }
        if (-not $p) { throw "plink.exe not found. agent-windows/tools/plink.exe 동봉 또는 remote.plink_path 지정 (tools/README.md 참조)." }
        return @{ Client = 'plink'; Path = $p; Source = if ($p -like '*tools*plink.exe') { 'bundled' } else { 'path' } }
    }
    # 'auto' — Test-SshAvailable 의 우선순위 따름 (bundled plink → ssh.exe → PATH plink)
    $avail = Test-SshAvailable
    if (-not $avail) { throw "ssh.exe/plink.exe 둘 다 없음. agent-windows/tools/plink.exe 동봉 권장 (tools/README.md)." }
    return $avail
}

function _ResolveRemotePassword {
    <# DPAPI 우선, 폴백 plain. plink 의 -pw 인자 또는 다른 백엔드의 stdin 입력용. #>
    param($Remote)
    if ($Remote.PSObject.Properties['password_dpapi'] -and $Remote.password_dpapi) {
        try { return Unprotect-Password -Encrypted $Remote.password_dpapi } catch { return $null }
    }
    if ($Remote.PSObject.Properties['password'] -and $Remote.password) {
        return $Remote.password
    }
    return $null
}

function Invoke-RemoteSsh {
    <#
    .SYNOPSIS  원격 Unix 서버에서 명령 한 줄 실행.
    .OUTPUTS   @{ stdout=...; stderr=...; exitCode=...; ok=$true/$false }
    #>
    param(
        [Parameter(Mandatory)] $Remote,                # config remotes 의 한 객체
        [Parameter(Mandatory)] [string] $Command,      # 단일 명령(또는 sh -c "...")
        [int] $TimeoutSec = 30
    )
    $client = Resolve-SshClient -Remote $Remote
    $host_  = $Remote.host
    $user   = $Remote.user
    $port   = if ($Remote.port) { [int]$Remote.port } else { 22 }
    $target = "$user@$host_"

    $argList = New-Object System.Collections.ArrayList

    if ($client.Client -eq 'ssh') {
        # OpenSSH: -o BatchMode=yes 로 prompt 차단. 호스트키 첫 접속 자동 수락(연구실/PoC 한정).
        [void]$argList.Add('-p'); [void]$argList.Add($port)
        [void]$argList.Add('-o'); [void]$argList.Add('BatchMode=yes')
        [void]$argList.Add('-o'); [void]$argList.Add('StrictHostKeyChecking=accept-new')
        [void]$argList.Add('-o'); [void]$argList.Add(('ConnectTimeout={0}' -f $TimeoutSec))
        if ($Remote.key) {
            [void]$argList.Add('-i'); [void]$argList.Add($Remote.key)
        }
        [void]$argList.Add($target)
        [void]$argList.Add($Command)
    } else {
        # plink: -ssh -batch (호스트키 first-accept 폐쇄망 가정).
        # ⚠ 보안: -pw 는 password 가 프로세스 인자에 노출됨 (tasklist). PoC 한정. 운영 전환 시 -pwfile 또는 SSH 키 권장.
        [void]$argList.Add('-ssh')
        [void]$argList.Add('-batch')
        [void]$argList.Add('-P'); [void]$argList.Add($port)
        if ($Remote.key) {
            [void]$argList.Add('-i'); [void]$argList.Add($Remote.key)
        }
        $plainPw = _ResolveRemotePassword -Remote $Remote
        if ($plainPw) {
            [void]$argList.Add('-pw'); [void]$argList.Add($plainPw)
        }
        [void]$argList.Add($target)
        [void]$argList.Add($Command)
    }

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $client.Path
    # PS 4.0/5.1: ProcessStartInfo.ArgumentList 가 없으므로 Arguments 문자열로 직접 빌드.
    # 공백/특수문자 포함 토큰은 큰따옴표로 감싸고 내부 큰따옴표는 \" 로 이스케이프.
    $quoted = $argList | ForEach-Object {
        $s = [string]$_
        if ($s -match '\s' -and -not $s.StartsWith('"')) { '"' + ($s -replace '"','\"') + '"' } else { $s }
    }
    $psi.Arguments = ($quoted -join ' ')
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true

    $proc = [System.Diagnostics.Process]::Start($psi)
    $stdout = $proc.StandardOutput.ReadToEnd()
    $stderr = $proc.StandardError.ReadToEnd()
    if (-not $proc.WaitForExit($TimeoutSec * 1000)) {
        try { $proc.Kill() } catch {}
        return @{ stdout=$stdout; stderr="timeout after ${TimeoutSec}s"; exitCode=124; ok=$false; client=$client.Client }
    }
    return @{
        stdout   = ($stdout -as [string])
        stderr   = ($stderr -as [string])
        exitCode = $proc.ExitCode
        ok       = ($proc.ExitCode -eq 0)
        client   = $client.Client
    }
}

function Copy-RemoteFile {
    <#
    .SYNOPSIS  원격 → 로컬 파일 복사 (scp / pscp).
    #>
    param(
        [Parameter(Mandatory)] $Remote,
        [Parameter(Mandatory)] [string] $RemotePath,
        [Parameter(Mandatory)] [string] $LocalPath,
        [int] $TimeoutSec = 60
    )
    $host_  = $Remote.host
    $user   = $Remote.user
    $port   = if ($Remote.port) { [int]$Remote.port } else { 22 }

    # 번들 pscp.exe 우선 검색 (Ssh.ps1 의 plink 와 동일 정책)
    $bundledPscp = _FindBundledTool 'pscp.exe'
    $scp   = Get-Command scp.exe   -ErrorAction SilentlyContinue
    $pscp  = if ($bundledPscp) { @{ Source = $bundledPscp } } else { Get-Command pscp.exe -ErrorAction SilentlyContinue }
    $usePscp = ($Remote.auth -eq 'plink') -or (-not $scp -and $pscp)
    if ($usePscp -and -not $pscp) { throw "pscp.exe not found. agent-windows/tools/pscp.exe 동봉 또는 PATH 등록." }
    if (-not $usePscp -and -not $scp) {
        if ($pscp) { $usePscp = $true } else { throw "scp.exe / pscp.exe not found. agent-windows/tools/pscp.exe 동봉 권장." }
    }

    $args = New-Object System.Collections.ArrayList
    if ($usePscp) {
        [void]$args.Add('-batch')
        [void]$args.Add('-P'); [void]$args.Add($port)
        if ($Remote.key) { [void]$args.Add('-i'); [void]$args.Add($Remote.key) }
        if ($Remote.auth -eq 'password' -and $Remote.password) {
            [void]$args.Add('-pw'); [void]$args.Add($Remote.password)
        }
        [void]$args.Add(('{0}@{1}:{2}' -f $user, $host_, $RemotePath))
        [void]$args.Add($LocalPath)
        $exe = $pscp.Source
    } else {
        [void]$args.Add('-P'); [void]$args.Add($port)
        [void]$args.Add('-o'); [void]$args.Add('BatchMode=yes')
        [void]$args.Add('-o'); [void]$args.Add('StrictHostKeyChecking=accept-new')
        if ($Remote.key) { [void]$args.Add('-i'); [void]$args.Add($Remote.key) }
        [void]$args.Add(('{0}@{1}:{2}' -f $user, $host_, $RemotePath))
        [void]$args.Add($LocalPath)
        $exe = $scp.Source
    }

    $quoted = $args | ForEach-Object {
        $s = [string]$_
        if ($s -match '\s' -and -not $s.StartsWith('"')) { '"' + ($s -replace '"','\"') + '"' } else { $s }
    }
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName  = $exe
    $psi.Arguments = ($quoted -join ' ')
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow  = $true
    $proc = [System.Diagnostics.Process]::Start($psi)
    $stdout = $proc.StandardOutput.ReadToEnd()
    $stderr = $proc.StandardError.ReadToEnd()
    if (-not $proc.WaitForExit($TimeoutSec * 1000)) {
        try { $proc.Kill() } catch {}
        return @{ ok=$false; stderr="scp timeout ${TimeoutSec}s" }
    }
    return @{
        ok       = ($proc.ExitCode -eq 0)
        exitCode = $proc.ExitCode
        stdout   = $stdout
        stderr   = $stderr
    }
}

function Find-GssPathOnRemote {
    <#
    .SYNOPSIS  원격 Unix 서버에서 GSS 프로세스 경로 추출 (v2 — SSH/Telnet 라우팅 + java grep fallback).
    .DESCRIPTION
      1차: ps -ef | grep GSS — 프로세스 발견 시 경로 토큰 추출
      2차 (1차 결과 빈): ps -ef | grep java — Tomcat catalina.home 등에서 GSS 경로 추정 (FR-2)

      SSH/Telnet 자동 라우팅: Remote.proto 가 'telnet' 이면 Invoke-Remote 통해 Telnet 호출
      (Invoke-Remote 가 로드돼 있으면 사용, 아니면 Invoke-RemoteSsh 호출 — v1 호환).

      반환: @{ running=bool; pids=@(...); paths=@(...); detected_via=string; raw=<line> }
    #>
    param([Parameter(Mandatory)] $Remote)

    # SSH/Telnet 라우팅 — Invoke-Remote 가 로드돼 있으면 우선 (v2 라우터)
    $useRouter = $null -ne (Get-Command Invoke-Remote -ErrorAction SilentlyContinue)
    $invokerUsed = if ($useRouter) { 'Invoke-Remote' } else { 'Invoke-RemoteSsh' }

    function _InvokeOne([string]$cmd) {
        if ($useRouter) { return Invoke-Remote -Remote $Remote -Command $cmd }
        else            { return Invoke-RemoteSsh -Remote $Remote -Command $cmd }
    }

    # UNIX 경로의 상위 디렉토리 (Split-Path 는 Windows 에서 / → \ 변환하므로 직접 처리)
    function _UnixParent([string]$p) {
        if (-not $p) { return $null }
        $idx = $p.LastIndexOf('/')
        if ($idx -le 0) { return $null }
        return $p.Substring(0, $idx)
    }
    # ps 출력 라인 필터: 프롬프트/sentinel/명령에코 제거 후 진짜 ps row 만 추출
    function _PsRowFilter($lines) {
        $out = @()
        foreach ($ln in $lines) {
            $t = $ln.Trim()
            if (-not $t) { continue }
            if ($t -match '[#$]\s*$')              { continue }   # 프롬프트
            if ($t -match '__INSPECT_END_')        { continue }   # sentinel
            if ($t -match '^\s*ps\s+-ef')          { continue }   # 명령 에코
            if ($t -match '@[\w\-]+\s+[#$]')       { continue }   # prompt-echo 라인 (root@TMS-DB #)
            # AIX/Linux ps 행: UID PID PPID ... 형태 — PID 가 숫자여야 한다
            $f = $t -split '\s+'
            if ($f.Count -lt 8) { continue }
            if ($f[1] -notmatch '^\d+$') { continue }
            $out += $t
        }
        return $out
    }

    # 1차: GSS grep
    $r1 = _InvokeOne "ps -ef | grep -i GSS | grep -v grep"
    $lines1Raw = if ($r1.stdout) { ($r1.stdout -split "`n") } else { @() }
    $lines1 = _PsRowFilter $lines1Raw
    $pids  = @()
    $paths = @()
    $detectedVia = $null
    foreach ($ln in $lines1) {
        $fields = ($ln -split '\s+', 8)
        if ($fields.Count -ge 2) { $pids += $fields[1] }
        $tokens = $ln -split '\s+'
        foreach ($t in $tokens) {
            if ($t -match '^/' -and $t -match 'GSS') {
                $dir = _UnixParent $t
                if ($dir -and ($paths -notcontains $dir)) { $paths += $dir }
            }
        }
    }
    if ($pids.Count -gt 0 -or $paths.Count -gt 0) { $detectedVia = "ps-grep-GSS" }

    $raw = $r1.stdout
    # 2차 fallback: GSS 발견 0건 → java grep (FR-2)
    if ($pids.Count -eq 0 -and $paths.Count -eq 0) {
        $r2 = _InvokeOne "ps -ef | grep java | grep -v grep"
        $lines2Raw = if ($r2.stdout) { ($r2.stdout -split "`n") } else { @() }
        $lines2 = _PsRowFilter $lines2Raw
        foreach ($ln in $lines2) {
            $fields = ($ln -split '\s+', 8)
            if ($fields.Count -ge 2) { $pids += $fields[1] }
            # -Dcatalina.home=/opt/tomcat 같은 옵션에서 경로 추출
            if ($ln -match '-D(?:catalina\.home|catalina\.base)=([^\s]+)') {
                $dir = $matches[1].TrimEnd('/')
                if ($paths -notcontains $dir) { $paths += $dir }
            }
            # GSS 키워드 포함 경로
            $tokens = $ln -split '\s+'
            foreach ($t in $tokens) {
                if ($t -match '^/' -and $t -match '(?i)(GSS|spatial_server|GeoNURIS)') {
                    $dir = _UnixParent $t
                    if ($dir -and ($paths -notcontains $dir)) { $paths += $dir }
                }
            }
        }
        if ($pids.Count -gt 0 -or $paths.Count -gt 0) { $detectedVia = "ps-grep-java" }
        $raw = ($r1.stdout + "`n---java---`n" + $r2.stdout)
    }

    return @{
        running      = ($pids.Count -gt 0)
        pids         = $pids
        paths        = $paths
        detected_via = $detectedVia
        invoker_used = $invokerUsed
        proto        = if ($Remote.PSObject.Properties['proto'] -and $Remote.proto) { $Remote.proto } else { 'ssh' }
        raw          = $raw
        ok           = $r1.ok
        stderr       = $r1.stderr
    }
}
