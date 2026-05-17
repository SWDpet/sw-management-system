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
    $homeHint = if ($Remote.oracle.oracle_home_hint) { [string]$Remote.oracle.oracle_home_hint } else { '' }

    $connectStr = if ($authMode -eq 'sysdba') {
        '"/ as sysdba"'
    } else {
        if (-not $Remote.oracle.user -or -not $Remote.oracle.password) {
            return @{ ok=$false; stderr='oracle.auth_mode=normal 인데 user/password 미지정'; stdout=''; exitCode=-1 }
        }
        ($Remote.oracle.user + '/' + $Remote.oracle.password)
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
        "export ORACLE_HOME=$homeHint; export PATH=`$ORACLE_HOME/bin:`$PATH; export ORACLE_SID=$sid; "
    } else {
        "export ORACLE_SID=$sid; "
    }

    $cmd = ("{0}(echo '{1}' | base64 -d 2>/dev/null || echo '{1}' | perl -MMIME::Base64 -ne 'print decode_base64(`$_)') > {2}; sqlplus -s -L {3} @{2}; _RC=`$?; rm -f {2}; exit `$_RC" `
        -f $envPrefix, $b64, $tmp, $connectStr)

    $r = Invoke-Remote -Remote $Remote -Command $cmd -TimeoutSec $TimeoutSec
    if (-not $r.ok) {
        return @{ ok=$false; stderr=$r.stderr; stdout=$r.stdout; exitCode=-1 }
    }
    # Oracle 에러 라인 검출 — ORA-/SP2-/ERROR
    foreach ($l in ($r.stdout -split "`r?`n")) {
        $lt = $l.Trim()
        if ($lt -match '^(ORA-|SP2-|ERROR\s)') {
            return @{ ok=$false; stderr=$lt; stdout=$r.stdout; exitCode=1 }
        }
    }

    # rows 파싱 — '|' COLSEP. 잡음 라인(프롬프트/echo) 은 skip.
    $rows = New-Object System.Collections.ArrayList
    foreach ($line in ($r.stdout -split "`r?`n")) {
        $t = $line.Trim()
        if (-not $t) { continue }
        if ($t -match '@[\w\-]+\s+[#$]') { continue }    # telnet 프롬프트 echo
        if ($t -match '^_?_INSPECT_')    { continue }    # telnet sentinel
        if ($t -match '^sqlplus')        { continue }    # 명령 echo
        if ($t -match '^echo\s+\w+\s')   { continue }    # base64 명령 echo

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
