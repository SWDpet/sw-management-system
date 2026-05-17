<#
.SYNOPSIS
  Oracle 점검 SQL 실행 lib — sqlplus 라우팅 + read-only 가드 + sysdba 접속.

.DESCRIPTION
  - Test-OracleSqlReadOnly: SQL 본문에 DML/DDL/권한 변경 키워드 차단 (라인 단위 검사).
  - Invoke-OracleSql: base64 인코딩 → 원격 임시파일 → sqlplus -s @file → 결과 파싱.
    Telnet 의 multi-line / quote escape 한계 회피, AIX 7+ 의 base64 / perl MIME::Base64 fallback.
  - Invoke-OracleCheck: checks/db-oracle.ps1 dispatcher 가 호출. .sql 읽고 Invoke-OracleSql 후 정규화.

  read-only 보장 (3중):
  1) PS 측 키워드 가드 (Test-OracleSqlReadOnly) — 호출 전 throw
  2) SQL prefix 의 SET TRANSACTION READ ONLY — Oracle 세션이 거부
  3) 점검 SQL 자체가 SELECT only — 운영자가 sysdba 사용해도 실 변경 불가
#>

. (Join-Path $PSScriptRoot 'Common.ps1')
. (Join-Path $PSScriptRoot 'DPAPI.ps1')
. (Join-Path $PSScriptRoot 'Ssh.ps1')
. (Join-Path $PSScriptRoot 'Telnet.ps1')

# 라인에 이 키워드가 있으면 reject. 대소문자 무시.
$script:OracleDenyPattern = '(?i)\b(INSERT|UPDATE|DELETE|MERGE|TRUNCATE|DROP|ALTER|CREATE|GRANT|REVOKE|COMMIT|ROLLBACK|LOCK|RENAME|FLASHBACK|EXEC|EXECUTE|CALL)\b'

# ORACLE_HOME 자동 탐지 캐시 — key=remote.host, value=경로 (또는 $null=탐지실패).
# 한 inspect 라운드의 17개 SQL 호출 중 첫 번째만 탐지 비용, 나머지는 cache hit.
$script:OracleHomeCache = @{}

function Resolve-OracleHome {
    <#
    .SYNOPSIS
      원격 Unix DB 서버에서 ORACLE_HOME 자동 탐지.
    .DESCRIPTION
      탐지 순서 (빠른 것부터):
        1) /etc/oratab — Linux/AIX 표준
        2) /var/opt/oracle/oratab — Solaris/HPUX/AIX 변형
        3) find /oracle /u01 /opt /app /home/oracle -name sqlplus (범위 한정, ~3-5s)
      결과는 host 별 캐싱. 탐지 실패시 $null.
    #>
    param($Remote)
    $key = [string]$Remote.host
    if ($script:OracleHomeCache.ContainsKey($key)) {
        return $script:OracleHomeCache[$key]
    }

    $detected = $null

    # 1) oratab 두 위치 한 번에 조회
    $r1 = Invoke-Remote -Remote $Remote -Command "cat /etc/oratab 2>/dev/null; cat /var/opt/oracle/oratab 2>/dev/null" -TimeoutSec 10
    if ($r1.ok) {
        foreach ($line in ($r1.stdout -split "`r?`n")) {
            $t = $line.Trim()
            if (-not $t -or $t.StartsWith('#')) { continue }
            $parts = $t -split ':', 3
            if ($parts.Count -ge 2) {
                # ⚠ $home 은 PS 자동 read-only 변수 — $oraHome 로 회피.
                $oraHome = $parts[1].Trim()
                if ($oraHome -and $oraHome -match '^/') { $detected = $oraHome; break }
            }
        }
    }

    # 2) find — oratab 못 찾았을 때만, 범위 한정
    if (-not $detected) {
        $r2 = Invoke-Remote -Remote $Remote -Command "find /oracle /u01 /opt /app /home/oracle -name sqlplus -type f -path '*/bin/sqlplus' 2>/dev/null | head -3" -TimeoutSec 30
        if ($r2.ok) {
            foreach ($line in ($r2.stdout -split "`r?`n")) {
                $t = $line.Trim()
                if ($t -match '^(/.+)/bin/sqlplus$') { $detected = $matches[1]; break }
            }
        }
    }

    $script:OracleHomeCache[$key] = $detected
    return $detected
}

function Test-OracleSqlReadOnly {
    param([Parameter(Mandatory)][string]$Script)
    # 주석 제거 — -- 라인주석 / /* ... */ 멀티라인주석
    $clean = $Script -replace '--[^\r\n]*',''
    $clean = $clean -replace '(?s)/\*.*?\*/',''
    foreach ($line in ($clean -split "`r?`n")) {
        $t = $line.Trim()
        if (-not $t) { continue }
        if ($t -match $script:OracleDenyPattern) {
            throw ("[oracle guard] read-only 위반 — 차단 키워드 포함: " + $t.Substring(0, [Math]::Min(80, $t.Length)))
        }
    }
}

function Invoke-OracleSql {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $Remote,
        [Parameter(Mandatory)][string] $SqlText,
        [int] $TimeoutSec = 30,
        [string[]] $ColumnNames = @()
    )
    Test-OracleSqlReadOnly -Script $SqlText | Out-Null

    if (-not $Remote.oracle -or -not $Remote.oracle.sid) {
        return @{ ok=$false; stderr='remote.oracle.sid 미설정'; stdout=''; exitCode=-1 }
    }
    $sid      = [string]$Remote.oracle.sid
    $authMode = if ($Remote.oracle.auth_mode) { [string]$Remote.oracle.auth_mode } else { 'sysdba' }
    # ORACLE_HOME — config hint 우선, 없으면 원격 자동 탐지 (host 별 cache).
    $homeHint = if ($Remote.oracle.oracle_home_hint) {
        [string]$Remote.oracle.oracle_home_hint
    } else {
        $auto = Resolve-OracleHome -Remote $Remote
        if (-not $auto) {
            return @{ ok=$false; stdout=''; exitCode=-1;
                stderr='ORACLE_HOME 자동 탐지 실패 — /etc/oratab, /var/opt/oracle/oratab 에도 없고 /oracle /u01 /opt /app 에 sqlplus 도 없음. setup-gui 의 ORACLE_HOME hint 에 수동 입력 필요.' }
        }
        $auto
    }

    # SQL block — WHENEVER 로 에러 시 즉시 exit, SET 으로 출력 정형화, READ ONLY 트랜잭션.
    $sqlBlock = @"
WHENEVER OSERROR EXIT 9
WHENEVER SQLERROR EXIT 9
SET PAGESIZE 0 HEADING OFF FEEDBACK OFF VERIFY OFF TRIMSPOOL ON LINESIZE 32767 COLSEP '|'
SET TRANSACTION READ ONLY;
$SqlText
EXIT;
"@

    # base64 인코딩으로 원격 임시파일에 떨군 뒤 sqlplus @file 호출.
    # 이유: Telnet 은 multi-line 안 보내지고, SQL 안의 quote/$ escape 가 골치아픔.
    # AIX 7+/Linux: base64 있음. AIX 6.x 등 없는 환경은 perl MIME::Base64 fallback.
    $b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($sqlBlock))
    $tmp = "/tmp/upis_ora_$([Guid]::NewGuid().ToString('N').Substring(0,8)).sql"

    $envPrefix = if ($homeHint) {
        "export ORACLE_HOME=$homeHint; export PATH=`$ORACLE_HOME/bin:`$PATH; export ORACLE_SID=$sid;"
    } else {
        "export ORACLE_SID=$sid;"
    }

    # base64 디코딩 → /tmp/<uuid>.sql (root 권한, /tmp 는 누구나 쓰기 가능).
    $decoder = "(echo '$b64' | base64 -d 2>/dev/null || echo '$b64' | perl -MMIME::Base64 -ne 'print decode_base64(`$_)') > $tmp"

    if ($authMode -eq 'sysdba') {
        # ⚠ AIX/Linux 에서 '/ as sysdba' OS 인증은 dba group 멤버만 허용 (보통 oracle 계정).
        # root 가 직접 sqlplus 호출하면 ORA-01017 (2026-05-17 강진 라운드 확인).
        # → su - <oracle_user> -c 로 oracle 계정 전환 후 sqlplus 실행.
        #   chmod 644 로 oracle 사용자가 /tmp/*.sql 읽을 수 있게.
        $oraUser = if ($Remote.oracle.os_user) { [string]$Remote.oracle.os_user } else { 'oracle' }

        # ⚠ su -c "INNER" 의 INNER 안의 `$ORACLE_HOME` / `$PATH` 는 oracle 의 shell 이 평가해야 함.
        # 하지만 outer "..." 를 먼저 parsing 하는 건 root 의 ksh — `$ORACLE_HOME` 이 root 에선 빈값이라
        # PATH 가 망가져서 'sqlplus not found' (2026-05-17 강진 라운드 확인).
        # → `\$ORACLE_HOME` / `\$PATH` 로 backslash escape. ksh 가 double-quote 안의 `\$` 를
        #   literal `$` 로 unescape → su 에 넘어가서 oracle shell 이 비로소 expand.
        # $envPrefix 와 별도로 sysdba 전용 $envInner 구성 (escape 패턴이 다름).
        $envInner = "export ORACLE_HOME=$homeHint; export PATH=\`$ORACLE_HOME/bin:\`$PATH;"
        # ORACLE_SID 는 prefix 문법으로 sqlplus 한 명령에만 적용 — oracle 의 .profile SID 와 충돌 회피.
        $inner = "$envInner ORACLE_SID=$sid sqlplus -s -L '/ as sysdba' @$tmp"
        $cmd = "$decoder; chmod 644 $tmp; su - $oraUser -c `"$inner`"; rm -f $tmp"
    } else {
        if (-not $Remote.oracle.user -or -not $Remote.oracle.password) {
            return @{ ok=$false; stderr='oracle.auth_mode=normal 인데 user/password 미지정'; stdout=''; exitCode=-1 }
        }
        $connectStr = $Remote.oracle.user + '/' + $Remote.oracle.password
        # normal mode — sqlplus 가 자체 user/password 인증, root 에서 직접 호출 OK.
        $cmd = "$envPrefix $decoder; sqlplus -s -L $connectStr @$tmp; rm -f $tmp"
    }

    # ⚠ exit `$_RC 제거 — Telnet wrapper 가 끝에 echo __INSPECT_END_<rand>_`$? 를 자동 append.
    # exit 가 shell 종료시키면 sentinel echo 가 실행 안 돼서 timeout. sqlplus 결과는
    # stdout 에서 ORA-/SP2- 패턴으로 검출 (아래 에러 라인 검사 참조).

    $r = Invoke-Remote -Remote $Remote -Command $cmd -TimeoutSec $TimeoutSec
    if (-not $r.ok) {
        return @{ ok=$false; stderr=$r.stderr; stdout=$r.stdout; exitCode=-1 }
    }
    # Oracle 에러 라인 검출 — ORA-/SP2-/ERROR + shell 의 'command not found' (sqlplus PATH 없음)
    foreach ($l in ($r.stdout -split "`r?`n")) {
        $lt = $l.Trim()
        if ($lt -match '^(ORA-|SP2-|ERROR\s)') {
            return @{ ok=$false; stderr=$lt; stdout=$r.stdout; exitCode=1 }
        }
        if ($lt -match '(?i)(ksh|bash|sh):\s*sqlplus.*not found') {
            return @{ ok=$false; stderr=$lt; stdout=$r.stdout; exitCode=127 }
        }
    }

    # rows 파싱 — '|' COLSEP. 잡음 라인(프롬프트/echo) 은 skip.
    $rows = New-Object System.Collections.ArrayList
    foreach ($line in ($r.stdout -split "`r?`n")) {
        $t = $line.Trim()
        if (-not $t) { continue }
        if ($t -match '@[\w\-]+\s+[#$]') { continue }    # telnet 프롬프트 echo (root@TMS-DB #)
        if ($t -match '^_?_INSPECT_')    { continue }    # telnet sentinel
        if ($t -match '^sqlplus')        { continue }    # 명령 echo
        if ($t -match '^echo\s+\w+\s')   { continue }    # echo 명령
        if ($t -match "^\(echo\s")        { continue }    # (echo '<base64>'...) 명령 echo — 2026-05-17 강진
        if ($t -match '^\[YOU HAVE')     { continue }    # AIX MOTD [YOU HAVE NEW MAIL]
        if ($t -match '^Unit:')          { continue }    # svmon 헤더 (defensive)
        if ($t -match '^-{5,}')          { continue }    # 구분선 ----- (defensive)

        $cols = $t -split '\s*\|\s*'
        if ($ColumnNames.Count -gt 0 -and $cols.Count -eq $ColumnNames.Count) {
            $row = [ordered]@{}
            for ($i = 0; $i -lt $cols.Count; $i++) { $row[$ColumnNames[$i]] = $cols[$i] }
            [void]$rows.Add($row)
        } else {
            [void]$rows.Add(@{ raw = $t; cols = $cols })
        }
    }
    return @{ ok=$true; stdout=$r.stdout; stderr=''; rows=@($rows); exitCode=0 }
}

function Invoke-OracleCheck {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)][string]$Id,
        [Parameter(Mandatory)][string]$Label,
        [Parameter(Mandatory)][string]$SqlPath,
        [Parameter(Mandatory)]$Config,
        [int]$TimeoutSec = 30,
        [scriptblock]$Parser,
        [string[]]$ColumnNames = @(),
        $Threshold,
        [string]$Category = 'db_oracle'
    )
    $remote = $null
    if ($Config -and $Config.remotes -and $Config.remotes.unix_db) {
        $remote = $Config.remotes.unix_db
    }
    if (-not $remote -or -not $remote.enabled) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd '(unix_db disabled)' -Status 'n/a' -Note 'remote.unix_db 미설정')
    }
    if (-not $remote.oracle -or -not $remote.oracle.sid) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd '(oracle.sid not set)' -Status 'n/a' -Note 'remote.oracle.sid 미설정 — Setup GUI 에서 입력')
    }
    if (-not (Test-Path $SqlPath)) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd '(sql not found)' -Status 'error' -Note ("sql 파일 없음: " + $SqlPath))
    }
    $sql = Get-Content -Path $SqlPath -Raw -Encoding UTF8

    try {
        $r = Invoke-OracleSql -Remote $remote -SqlText $sql -TimeoutSec $TimeoutSec -ColumnNames $ColumnNames
    } catch {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd ("sqlplus @ " + (Split-Path -Leaf $SqlPath)) -Status 'error' `
            -Note ("guard: " + $_.Exception.Message))
    }
    if (-not $r.ok) {
        return (New-CheckResult -Id $Id -Label $Label -Category $Category `
            -Cmd ("sqlplus @ " + (Split-Path -Leaf $SqlPath)) -Status 'error' `
            -Value @{ sid=$remote.oracle.sid; stderr=$r.stderr } -Raw $r.stdout)
    }

    $value = [ordered]@{ sid = $remote.oracle.sid; rows = @($r.rows) }
    $status = 'ok'
    $threshold = $Threshold

    if ($Parser) {
        try {
            $parsed = & $Parser $r.rows $remote
            if ($parsed -is [System.Collections.IDictionary]) {
                foreach ($k in $parsed.Keys) {
                    if ($k -eq '__status')    { $status = [string]$parsed[$k]; continue }
                    if ($k -eq '__threshold') { $threshold = $parsed[$k];      continue }
                    $value[$k] = $parsed[$k]
                }
            }
        } catch {
            $value['parse_error'] = $_.Exception.Message
            $status = 'error'
        }
    }

    $args = @{
        Id = $Id; Label = $Label; Category = $Category
        Cmd = ("sqlplus / as sysdba @ " + (Split-Path -Leaf $SqlPath))
        Value = $value; Status = $status; Raw = $r.stdout
    }
    if ($threshold) { $args['Threshold'] = $threshold }
    return (New-CheckResult @args)
}
