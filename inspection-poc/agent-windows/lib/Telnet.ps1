<#
.SYNOPSIS
  Telnet 매개 원격 명령 실행 — SSH 미설치 옛 UNIX 사이트용 (FR-3).

.DESCRIPTION
  plink.exe 의 -telnet 모드는 -pw 자동 인증 미지원 (SSH 한정).
  TcpClient 로 직접 Telnet protocol 구현 — IAC negotiation, login/password 자동 입력,
  sentinel 토큰 매칭으로 명령 실행 + exit code 추출.

  ⚠ 보안 위험: Telnet 은 패스워드 평문 전송. SSH 전환 가능한 사이트는 SSH 사용.
  본 모듈은 옛 AIX/Solaris/HP-UX 사이트 (SSH 미설치) 한정 PoC.

.NOTES
  PS 4.0 호환 (Server 2012 R2). .NET 4.5+ 의 TcpClient + NetworkStream + Encoding.
#>

# Telnet IAC bytes
$script:TELNET_IAC  = 0xFF
$script:TELNET_DONT = 0xFE
$script:TELNET_DO   = 0xFD
$script:TELNET_WONT = 0xFC
$script:TELNET_WILL = 0xFB
$script:TELNET_SB   = 0xFA
$script:TELNET_SE   = 0xF0

function _TelnetWriteString {
    param([System.IO.Stream]$Stream, [string]$Text)
    $bytes = [System.Text.Encoding]::ASCII.GetBytes($Text)
    $Stream.Write($bytes, 0, $bytes.Length)
    $Stream.Flush()
}

function _TelnetReadUntil {
    <#
    Stream 에서 데이터 읽으면서 IAC 시퀀스 자동 응답 (모두 WONT/DONT 거부).
    $UntilRegex 매칭 또는 timeout 까지 읽음. 매칭 안 되면 빈 string 반환.
    #>
    param(
        [Parameter(Mandatory)][System.IO.Stream]$Stream,
        [Parameter(Mandatory)][string]$UntilRegex,
        [int]$TimeoutMs = 10000
    )
    $buf = New-Object System.Text.StringBuilder
    $byteBuf = New-Object byte[] 4096
    $start = [System.Diagnostics.Stopwatch]::StartNew()
    while ($start.ElapsedMilliseconds -lt $TimeoutMs) {
        if (-not $Stream.DataAvailable) {
            Start-Sleep -Milliseconds 50
            continue
        }
        $n = $Stream.Read($byteBuf, 0, $byteBuf.Length)
        if ($n -le 0) { break }
        # IAC 시퀀스 처리 — 데이터 byte 만 buf 에 추가
        $i = 0
        while ($i -lt $n) {
            $b = $byteBuf[$i]
            if ($b -eq $script:TELNET_IAC) {
                if ($i + 2 -lt $n) {
                    $cmd = $byteBuf[$i+1]
                    $opt = $byteBuf[$i+2]
                    # DO → WONT, WILL → DONT (모든 옵션 거부)
                    $reply = $null
                    if ($cmd -eq $script:TELNET_DO)   { $reply = @($script:TELNET_IAC, $script:TELNET_WONT, $opt) }
                    elseif ($cmd -eq $script:TELNET_WILL) { $reply = @($script:TELNET_IAC, $script:TELNET_DONT, $opt) }
                    if ($reply) { $Stream.Write([byte[]]$reply, 0, 3); $Stream.Flush() }
                    $i += 3
                    continue
                } else {
                    # 부족하면 다음 read 까지 보류 — 단순 PoC 는 skip
                    $i += 1
                    continue
                }
            }
            # 일반 byte → ASCII 로 buf 누적
            if ($b -ge 0x09 -and $b -lt 0x7F) {
                [void]$buf.Append([char]$b)
            } elseif ($b -eq 0x0A -or $b -eq 0x0D) {
                [void]$buf.Append([char]$b)
            }
            $i++
        }
        $current = $buf.ToString()
        if ($current -match $UntilRegex) {
            return $current
        }
    }
    return $buf.ToString()  # timeout — 부분 데이터 반환
}

function Invoke-RemoteTelnet {
    <#
    .SYNOPSIS
      Telnet 으로 원격 명령 1 회 실행 → { ok, stdout, stderr, exitCode }

    .PARAMETER Remote
      site config 의 remotes.unix_db 객체. 필드:
        proto = "telnet"
        host, port (default 23), user, password_dpapi (또는 password — plain, 비권장)

    .PARAMETER Command
      실행할 단일 shell 명령. 다중 줄은 `; ` 또는 `\n` 으로 join.

    .PARAMETER TimeoutSec
      전체 실행 timeout (login + 명령 실행).
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $Remote,
        [Parameter(Mandatory)][string] $Command,
        [int] $TimeoutSec = 30
    )

    $host_     = $Remote.host
    $port      = if ($Remote.port) { [int]$Remote.port } else { 23 }
    $user      = $Remote.user
    if ($Remote.PSObject.Properties['password_dpapi'] -and $Remote.password_dpapi) {
        # DPAPI 함수가 로드되어 있다고 가정 (호출자가 dot-source)
        $password = Unprotect-Password -Encrypted $Remote.password_dpapi
    } elseif ($Remote.PSObject.Properties['password'] -and $Remote.password) {
        $password = $Remote.password
    } else {
        return @{ ok = $false; stdout = ''; stderr = 'no password in remote config'; exitCode = -1 }
    }

    $totalTimeoutMs = $TimeoutSec * 1000

    $client = $null
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $connectTask = $client.ConnectAsync($host_, $port)
        try {
            if (-not $connectTask.Wait(5000)) {
                return @{ ok = $false; stdout = ''; stderr = "connect timeout: ${host_}:${port}"; exitCode = -1 }
            }
        } catch {
            # ConnectAsync 가 fail 하면 AggregateException 안에 진짜 원인이 있음
            $inner = $_.Exception
            while ($inner.InnerException) { $inner = $inner.InnerException }
            return @{ ok = $false; stdout = ''; stderr = "connect failed (${host_}:${port}): $($inner.Message)"; exitCode = -1 }
        }
        $stream = $client.GetStream()

        # login prompt 대기
        $loginPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '(?i)(login:|user(name)?\s*:)' -TimeoutMs 8000
        if ($loginPrompt -notmatch '(?i)(login|user)') {
            return @{ ok = $false; stdout = $loginPrompt; stderr = 'login prompt not seen'; exitCode = -1 }
        }
        _TelnetWriteString -Stream $stream -Text ($user + "`r`n")

        # password prompt
        $pwPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '(?i)password\s*:' -TimeoutMs 8000
        if ($pwPrompt -notmatch '(?i)password') {
            return @{ ok = $false; stdout = $pwPrompt; stderr = 'password prompt not seen'; exitCode = -1 }
        }
        _TelnetWriteString -Stream $stream -Text ($password + "`r`n")
        $password = $null  # ASAP clear

        # shell prompt 매칭 ($, #, >) — 또는 "Last login" 메시지 후
        $shellPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '[\$#>]\s*$' -TimeoutMs 10000
        if ($shellPrompt -notmatch '[\$#>]\s*$') {
            # auth fail 또는 prompt 변종
            if ($shellPrompt -match '(?i)(login incorrect|authentication fail)') {
                return @{ ok = $false; stdout = $shellPrompt; stderr = 'authentication failed'; exitCode = -1 }
            }
            return @{ ok = $false; stdout = $shellPrompt; stderr = 'shell prompt not matched'; exitCode = -1 }
        }

        # 명령 + sentinel
        $sentinel = "__INSPECT_END_$(Get-Random)_"
        $fullCmd = "$Command; echo ${sentinel}`$?`r`n"
        _TelnetWriteString -Stream $stream -Text $fullCmd

        # sentinel 매칭
        $remainingMs = [Math]::Max(1000, $totalTimeoutMs - 20000)
        $output = _TelnetReadUntil -Stream $stream -UntilRegex "$sentinel(\d+)" -TimeoutMs $remainingMs

        $match = [regex]::Match($output, "$sentinel(\d+)")
        if (-not $match.Success) {
            return @{ ok = $false; stdout = $output; stderr = 'sentinel not found (timeout)'; exitCode = -1 }
        }
        $exitCode = [int]$match.Groups[1].Value
        # sentinel 이전 데이터 추출 → 명령 echo 한 줄 제거
        $rawOut = $output.Substring(0, $match.Index)
        # 명령 echo 제거 (첫 줄에 $Command + sentinel echo 명령이 나옴)
        $lines = $rawOut -split "[\r\n]+"
        $cleaned = ($lines | Where-Object {
            $_ -notmatch [regex]::Escape($Command) -and
            $_ -notmatch [regex]::Escape($sentinel) -and
            $_ -ne ''
        }) -join "`n"

        # 정상 종료 — exit
        try { _TelnetWriteString -Stream $stream -Text "exit`r`n" } catch {}

        return @{ ok = $true; stdout = $cleaned; stderr = ''; exitCode = $exitCode }
    }
    catch {
        return @{ ok = $false; stdout = ''; stderr = $_.Exception.Message; exitCode = -1 }
    }
    finally {
        if ($client) { try { $client.Close() } catch {} }
    }
}

function Invoke-Remote {
    <#
    SSH/Telnet 라우터. lib/Ssh.ps1 의 Invoke-RemoteSsh 또는 본 모듈의 Invoke-RemoteTelnet 호출.
    Step 6 에서 inspect-remote.ps1 가 사용.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $Remote,
        [Parameter(Mandatory)][string] $Command,
        [int] $TimeoutSec = 30
    )
    $proto = if ($Remote.PSObject.Properties['proto'] -and $Remote.proto) { $Remote.proto } else { 'ssh' }
    if ($proto -eq 'telnet') {
        return Invoke-RemoteTelnet -Remote $Remote -Command $Command -TimeoutSec $TimeoutSec
    } else {
        # lib/Ssh.ps1 의 Invoke-RemoteSsh 호출 (호출자가 dot-source 해놨다고 가정)
        return Invoke-RemoteSsh -Remote $Remote -Command $Command -TimeoutSec $TimeoutSec
    }
}
