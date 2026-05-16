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

function Test-SshAvailable {
    # ssh.exe 우선, 없으면 plink.exe 탐색
    $ssh = Get-Command ssh.exe -ErrorAction SilentlyContinue
    if ($ssh) { return @{ Client = 'ssh'; Path = $ssh.Source } }

    $plink = Get-Command plink.exe -ErrorAction SilentlyContinue
    if ($plink) { return @{ Client = 'plink'; Path = $plink.Source } }

    return $null
}

function Resolve-SshClient {
    param($Remote)
    # auth=plink 또는 plink_path 지정 시 plink 강제
    if ($Remote.auth -eq 'plink' -or $Remote.plink_path) {
        $p = $Remote.plink_path
        if (-not $p) { $p = (Get-Command plink.exe -ErrorAction SilentlyContinue).Source }
        if (-not $p) { throw "plink.exe not found (auth=plink). Install PuTTY or set remote.plink_path." }
        return @{ Client = 'plink'; Path = $p }
    }
    $avail = Test-SshAvailable
    if (-not $avail) { throw "Neither ssh.exe nor plink.exe found on PATH." }
    return $avail
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
        # plink: -batch 로 prompt 차단. 키(.ppk) 또는 비밀번호.
        [void]$argList.Add('-batch')
        [void]$argList.Add('-P'); [void]$argList.Add($port)
        if ($Remote.key) {
            [void]$argList.Add('-i'); [void]$argList.Add($Remote.key)
        }
        if ($Remote.auth -eq 'password' -and $Remote.password) {
            [void]$argList.Add('-pw'); [void]$argList.Add($Remote.password)
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

    $scp   = Get-Command scp.exe   -ErrorAction SilentlyContinue
    $pscp  = Get-Command pscp.exe  -ErrorAction SilentlyContinue
    $usePscp = ($Remote.auth -eq 'plink') -or (-not $scp -and $pscp)
    if ($usePscp -and -not $pscp) { throw "pscp.exe not found." }
    if (-not $usePscp -and -not $scp) {
        if ($pscp) { $usePscp = $true } else { throw "scp.exe / pscp.exe not found." }
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

    # 1차: GSS grep
    $r1 = _InvokeOne "ps -ef | grep -i GSS | grep -v grep"
    $lines1 = if ($r1.stdout) { ($r1.stdout -split "`n") | Where-Object { $_.Trim() -ne '' } } else { @() }
    $pids  = @()
    $paths = @()
    $detectedVia = $null
    foreach ($ln in $lines1) {
        $fields = ($ln -split '\s+', 8)
        if ($fields.Count -ge 2) { $pids += $fields[1] }
        $tokens = $ln -split '\s+'
        foreach ($t in $tokens) {
            if ($t -match '^/' -and $t -match 'GSS') {
                $dir = Split-Path -Parent $t
                if ($dir -and ($paths -notcontains $dir)) { $paths += $dir }
            }
        }
    }
    if ($pids.Count -gt 0 -or $paths.Count -gt 0) { $detectedVia = "ps-grep-GSS" }

    $raw = $r1.stdout
    # 2차 fallback: GSS 발견 0건 → java grep (FR-2)
    if ($pids.Count -eq 0 -and $paths.Count -eq 0) {
        $r2 = _InvokeOne "ps -ef | grep java | grep -v grep"
        $lines2 = if ($r2.stdout) { ($r2.stdout -split "`n") | Where-Object { $_.Trim() -ne '' } } else { @() }
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
                    $dir = Split-Path -Parent $t
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
