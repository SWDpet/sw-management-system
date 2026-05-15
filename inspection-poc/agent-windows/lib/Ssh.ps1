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
    .SYNOPSIS  원격 Unix 서버에서 GSS 프로세스 경로 추출.
    .DESCRIPTION
      ps -ef | grep GSS 로 프로세스 라인 수집 후, 경로처럼 보이는 토큰을 휴리스틱으로 추출.
      반환: @{ running=bool; pids=@(...); paths=@(...); raw=<line> }
    #>
    param([Parameter(Mandatory)] $Remote)
    $r = Invoke-RemoteSsh -Remote $Remote -Command "ps -ef | grep -i GSS | grep -v grep"
    $lines = if ($r.stdout) { ($r.stdout -split "`n") | Where-Object { $_.Trim() -ne '' } } else { @() }

    $pids  = @()
    $paths = @()
    foreach ($ln in $lines) {
        $fields = ($ln -split '\s+', 8)
        if ($fields.Count -ge 2) { $pids += $fields[1] }
        # 경로처럼 보이는 토큰 (/ 로 시작) 중 GSS 단어 포함
        $tokens = $ln -split '\s+'
        foreach ($t in $tokens) {
            if ($t -match '^/' -and $t -match 'GSS') {
                # 실행 파일 → 디렉토리 경로
                $dir = Split-Path -Parent $t
                if ($dir -and ($paths -notcontains $dir)) { $paths += $dir }
            }
        }
    }
    return @{
        running = ($pids.Count -gt 0)
        pids    = $pids
        paths   = $paths
        raw     = ($lines -join "`n")
        ok      = $r.ok
        stderr  = $r.stderr
    }
}
