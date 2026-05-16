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

# Telnet option codes — 디버깅 로그용
$script:TELNET_OPTNAMES = @{
    0x00='BINARY'; 0x01='ECHO'; 0x03='SGA'; 0x05='STATUS'; 0x06='TM'
    0x18='TERM-TYPE'; 0x1F='WINSIZE'; 0x20='TERM-SPEED'; 0x21='FLOW-CTRL'
    0x22='LINEMODE'; 0x23='X-LOCATION'; 0x24='ENV'; 0x27='NEW-ENV'
}

# ── 디버그 로그 헬퍼 ──────────────────────────────────────────────────────
# $script:TelnetLogPath 가 set 되면 file 에 누적. 호출자가 Set-TelnetLog 으로 활성화.
$script:TelnetLogPath = $null
$script:TelnetLogSw   = $null   # Stopwatch

function Set-TelnetLog {
    <# 호출자가 디버그 로그를 활성화. setup.ps1 의 연결 테스트 단계에서 사용. #>
    [CmdletBinding()]
    param([string] $Path)
    $script:TelnetLogPath = $Path
    $script:TelnetLogSw   = [System.Diagnostics.Stopwatch]::StartNew()
    if ($Path) {
        $dir = Split-Path -Parent $Path
        if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
        $header = ("=" * 78) + "`r`n" +
                  "Telnet debug log - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')`r`n" +
                  ("=" * 78) + "`r`n"
        [System.IO.File]::WriteAllText($Path, $header, (New-Object System.Text.UTF8Encoding($false)))
    }
}

function _TLog {
    param([string]$Category, [string]$Message)
    if (-not $script:TelnetLogPath) { return }
    $ms = if ($script:TelnetLogSw) { $script:TelnetLogSw.ElapsedMilliseconds } else { 0 }
    $line = "[{0,7} ms] {1,-12} {2}`r`n" -f $ms, $Category, $Message
    try {
        [System.IO.File]::AppendAllText($script:TelnetLogPath, $line, (New-Object System.Text.UTF8Encoding($false)))
    } catch {}
}

function _TLogBytes {
    param([string]$Category, [byte[]]$Bytes, [int]$MaxLen = 256)
    if (-not $script:TelnetLogPath) { return }
    if (-not $Bytes -or $Bytes.Length -eq 0) {
        _TLog $Category "(empty)"
        return
    }
    $len = [Math]::Min($Bytes.Length, $MaxLen)
    $hex = ($Bytes[0..($len-1)] | ForEach-Object { '{0:X2}' -f $_ }) -join ' '
    $ascii = ($Bytes[0..($len-1)] | ForEach-Object { if ($_ -ge 0x20 -and $_ -lt 0x7F) { [char]$_ } else { '.' } }) -join ''
    $more = if ($Bytes.Length -gt $MaxLen) { "  ... +$($Bytes.Length - $MaxLen) bytes more" } else { '' }
    _TLog $Category ("len=$($Bytes.Length)  hex=$hex$more")
    _TLog $Category ("                       ascii=$ascii")
}

function _IacName([byte]$b) {
    switch ($b) {
        ([byte]0xFB) { return 'WILL' }
        ([byte]0xFC) { return 'WONT' }
        ([byte]0xFD) { return 'DO' }
        ([byte]0xFE) { return 'DONT' }
        ([byte]0xFA) { return 'SB' }
        ([byte]0xF0) { return 'SE' }
        default      { return ('0x{0:X2}' -f $b) }
    }
}

function _TelnetWriteString {
    param([System.IO.Stream]$Stream, [string]$Text, [switch]$Mask)
    $bytes = [System.Text.Encoding]::ASCII.GetBytes($Text)
    $Stream.Write($bytes, 0, $bytes.Length)
    $Stream.Flush()
    if ($Mask) {
        _TLog 'WRITE' ("len={0} (masked)" -f $bytes.Length)
    } else {
        _TLogBytes 'WRITE' $bytes 128
    }
}

function _TelnetReadUntil {
    <#
    Stream 에서 데이터 읽으면서 IAC 시퀀스 자동 응답 (모두 WONT/DONT 거부).
    $UntilRegex 매칭 또는 timeout 까지 읽음. 매칭 안 되면 빈 string 반환.
    #>
    param(
        [Parameter(Mandatory)][System.IO.Stream]$Stream,
        [Parameter(Mandatory)][string]$UntilRegex,
        [int]$TimeoutMs = 10000,
        [string]$Stage = ''
    )
    _TLog 'READ-START' ("stage=$Stage  regex='$UntilRegex'  timeout=$TimeoutMs ms")
    $buf = New-Object System.Text.StringBuilder
    $byteBuf = New-Object byte[] 4096
    $start = [System.Diagnostics.Stopwatch]::StartNew()
    $totalBytes = 0
    while ($start.ElapsedMilliseconds -lt $TimeoutMs) {
        if (-not $Stream.DataAvailable) {
            Start-Sleep -Milliseconds 50
            continue
        }
        $n = $Stream.Read($byteBuf, 0, $byteBuf.Length)
        if ($n -le 0) { break }
        $totalBytes += $n
        # 첫 256 bytes 만 hex 덤프 (로그 폭주 방지)
        if ($totalBytes -le 512) {
            _TLogBytes 'READ-RAW' ($byteBuf[0..($n-1)]) 96
        }
        # IAC 시퀀스 처리 — 데이터 byte 만 buf 에 추가
        $i = 0
        while ($i -lt $n) {
            $b = $byteBuf[$i]
            if ($b -eq $script:TELNET_IAC) {
                if ($i + 2 -lt $n) {
                    $cmd = $byteBuf[$i+1]
                    $opt = $byteBuf[$i+2]
                    $optName = if ($script:TELNET_OPTNAMES.ContainsKey([int]$opt)) { $script:TELNET_OPTNAMES[[int]$opt] } else { '0x{0:X2}' -f $opt }
                    # DO → WONT, WILL → DONT (모든 옵션 거부)
                    $reply = $null
                    $replyName = $null
                    if ($cmd -eq $script:TELNET_DO)   { $reply = @($script:TELNET_IAC, $script:TELNET_WONT, $opt); $replyName = 'WONT' }
                    elseif ($cmd -eq $script:TELNET_WILL) { $reply = @($script:TELNET_IAC, $script:TELNET_DONT, $opt); $replyName = 'DONT' }
                    $cmdName = _IacName $cmd
                    if ($reply) {
                        $Stream.Write([byte[]]$reply, 0, 3); $Stream.Flush()
                        _TLog 'IAC' ("server $cmdName $optName  =>  client $replyName")
                    } else {
                        _TLog 'IAC' ("server $cmdName $optName  =>  (no reply)")
                    }
                    $i += 3
                    continue
                } else {
                    _TLog 'IAC' "partial sequence (only $($n-$i) bytes left) - skip"
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
            _TLog 'READ-MATCH' ("stage=$Stage  matched at $($start.ElapsedMilliseconds) ms  buffer.len=$($current.Length)  tail='" + ($current.Substring([Math]::Max(0,$current.Length-80))) + "'")
            return $current
        }
    }
    $tail = $buf.ToString()
    _TLog 'READ-TIMEOUT' ("stage=$Stage  elapsed=$($start.ElapsedMilliseconds) ms  totalBytes=$totalBytes  buffer.len=$($tail.Length)")
    if ($tail.Length -gt 0) {
        _TLog 'READ-TIMEOUT' ("buffer.tail='" + ($tail.Substring([Math]::Max(0,$tail.Length-200))) + "'")
    }
    return $tail
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

    _TLog 'START' ("host=$host_  port=$port  user=$user  timeout=$TimeoutSec s  cmd='$Command'")
    $client = $null
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        _TLog 'CONNECT' "TcpClient.ConnectAsync ..."
        $connectTask = $client.ConnectAsync($host_, $port)
        try {
            if (-not $connectTask.Wait(5000)) {
                _TLog 'CONNECT-FAIL' "wait timeout 5s"
                return @{ ok = $false; stdout = ''; stderr = "connect timeout: ${host_}:${port}"; exitCode = -1 }
            }
        } catch {
            $inner = $_.Exception
            while ($inner.InnerException) { $inner = $inner.InnerException }
            _TLog 'CONNECT-FAIL' ("error: " + $inner.Message)
            return @{ ok = $false; stdout = ''; stderr = "connect failed (${host_}:${port}): $($inner.Message)"; exitCode = -1 }
        }
        _TLog 'CONNECT-OK' "connected"
        $stream = $client.GetStream()

        # 1) login prompt
        _TLog 'STAGE' "1/5 wait login prompt"
        $loginPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '(?i)(login:|user(name)?\s*:)' -TimeoutMs 8000 -Stage 'login-prompt'
        if ($loginPrompt -notmatch '(?i)(login|user)') {
            _TLog 'STAGE-FAIL' "login prompt not seen"
            return @{ ok = $false; stdout = $loginPrompt; stderr = 'login prompt not seen (서버 banner 가 login:/User: 매칭 안 됨 - 로그 buffer.tail 확인)'; exitCode = -1 }
        }
        _TelnetWriteString -Stream $stream -Text ($user + "`r`n")

        # 2) password prompt
        _TLog 'STAGE' "2/5 wait password prompt"
        $pwPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '(?i)password\s*:' -TimeoutMs 8000 -Stage 'password-prompt'
        if ($pwPrompt -notmatch '(?i)password') {
            _TLog 'STAGE-FAIL' "password prompt not seen"
            return @{ ok = $false; stdout = $pwPrompt; stderr = 'password prompt not seen'; exitCode = -1 }
        }
        _TelnetWriteString -Stream $stream -Text ($password + "`r`n") -Mask
        $password = $null

        # 3) shell prompt — $ # > 어느 하나
        _TLog 'STAGE' "3/5 wait shell prompt ([\$#>] regex)"
        $shellPrompt = _TelnetReadUntil -Stream $stream -UntilRegex '[\$#>]\s*$' -TimeoutMs 10000 -Stage 'shell-prompt'
        if ($shellPrompt -notmatch '[\$#>]\s*$') {
            if ($shellPrompt -match '(?i)(login incorrect|authentication fail|invalid password)') {
                _TLog 'STAGE-FAIL' "authentication failed"
                return @{ ok = $false; stdout = $shellPrompt; stderr = 'authentication failed (계정/패스워드 확인)'; exitCode = -1 }
            }
            _TLog 'STAGE-FAIL' "shell prompt regex not matched - server prompt 변종일 수 있음"
            return @{ ok = $false; stdout = $shellPrompt; stderr = 'shell prompt not matched (로그의 READ-TIMEOUT tail 확인 - prompt 변종이면 regex 보강 필요)'; exitCode = -1 }
        }

        # 4) 명령 + sentinel
        _TLog 'STAGE' "4/5 send command + sentinel"
        $sentinel = "__INSPECT_END_$(Get-Random)_"
        $fullCmd = "$Command; echo ${sentinel}`$?`r`n"
        _TelnetWriteString -Stream $stream -Text $fullCmd

        # 5) sentinel 매칭
        _TLog 'STAGE' "5/5 wait sentinel"
        $remainingMs = [Math]::Max(1000, $totalTimeoutMs - 20000)
        $output = _TelnetReadUntil -Stream $stream -UntilRegex "$sentinel(\d+)" -TimeoutMs $remainingMs -Stage 'sentinel'

        $match = [regex]::Match($output, "$sentinel(\d+)")
        if (-not $match.Success) {
            _TLog 'STAGE-FAIL' "sentinel not found - 명령 실행 안 됐거나 너무 느림"
            return @{ ok = $false; stdout = $output; stderr = 'sentinel not found (timeout)'; exitCode = -1 }
        }
        $exitCode = [int]$match.Groups[1].Value
        $rawOut = $output.Substring(0, $match.Index)
        $lines = $rawOut -split "[\r\n]+"
        $cleaned = ($lines | Where-Object {
            $_ -notmatch [regex]::Escape($Command) -and
            $_ -notmatch [regex]::Escape($sentinel) -and
            $_ -ne ''
        }) -join "`n"

        _TLog 'DONE' ("exitCode=$exitCode  stdout.len=$($cleaned.Length)")
        try { _TelnetWriteString -Stream $stream -Text "exit`r`n" } catch {}

        return @{ ok = $true; stdout = $cleaned; stderr = ''; exitCode = $exitCode }
    }
    catch {
        _TLog 'EXCEPTION' $_.Exception.Message
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
