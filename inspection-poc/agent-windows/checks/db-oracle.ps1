param($Config)
. (Join-Path $PSScriptRoot '..\lib\Oracle.ps1')

# sql/oracle/*.sql 폴더의 .sql 파일을 차례로 실행. 새 항목 추가는 sql 파일 + sqlMeta 한 줄.
$sqlDir = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql\oracle'
if (-not (Test-Path $sqlDir)) {
    return New-CheckResult -Id 'db.oracle' -Label 'Oracle 점검' -Category 'db_oracle' `
        -Cmd '(sql dir missing)' -Status 'n/a' -Note ("sql 디렉토리 없음: " + $sqlDir)
}

# 항목별 metadata. parser 가 없으면 raw rows 만 capture (status='ok').
# 컬럼명 정의돼 있고 row.cols 와 길이 일치하면 자동으로 named row 로 변환됨.
$sqlMeta = @{
    '01-archive-mode' = @{
        id='db.oracle.archive_mode'; label='Oracle Archive Log 모드'
        columns=@('log_mode')
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{ __status='n/a' } }
            $m = if ($rows[0].log_mode) { [string]$rows[0].log_mode } else { [string]$rows[0].cols[0] }
            $st = switch -Regex ($m) {
                'ARCHIVELOG'   { 'ok'; break }
                'NOARCHIVELOG' { 'crit'; break }
                default        { 'warn' }
            }
            return @{ log_mode = $m; __status = $st }
        }
    }
    '02-alert-errors' = @{
        id='db.oracle.alert_errors_24h'; label='Oracle Alert Log 에러 (24h)'
        columns=@('error_count')
        threshold=@{ warn=1; crit=10 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $cnt = if ($rows[0].error_count -ne $null) { [int]$rows[0].error_count } else { [int]$rows[0].cols[0] }
            $st = if     ($cnt -ge 10) { 'crit' }
                  elseif ($cnt -ge 1)  { 'warn' }
                  else                  { 'ok'   }
            return @{ error_count = $cnt; __status = $st }
        }
    }
    '03-redo-logs'      = @{ id='db.oracle.redo_logs';       label='Oracle Redo Log 그룹' }
    '04-sga'            = @{ id='db.oracle.sga';             label='Oracle SGA 메모리' }
    '05-pga'            = @{ id='db.oracle.pga';             label='Oracle PGA 메모리' }
    '06-tablespace' = @{
        id='db.oracle.tablespace'; label='Oracle Tablespace 사용률'
        columns=@('tablespace_name','used_pct')
        threshold=@{ warn=80; crit=90 }
        parser={ param($rows, $remote)
            $worst = 0.0
            $items = @()
            foreach ($r in $rows) {
                $name = if ($r.tablespace_name) { [string]$r.tablespace_name } else { [string]$r.cols[0] }
                $pct  = if ($r.used_pct)        { [double]$r.used_pct }        else { [double]$r.cols[1] }
                $items += @{ name = $name; used_pct = $pct }
                if ($pct -gt $worst) { $worst = $pct }
            }
            $st = if     ($worst -ge 90) { 'crit' }
                  elseif ($worst -ge 80) { 'warn' }
                  else                    { 'ok'   }
            return @{ tablespaces = @($items); worst_pct = $worst; __status = $st }
        }
    }
    '07-datafile-status'  = @{ id='db.oracle.datafile_status'; label='Oracle Datafile 상태' }
    '08-invalid-objects'  = @{ id='db.oracle.invalid_objects'; label='Oracle Invalid 객체' }
    '09-sessions' = @{
        id='db.oracle.sessions'; label='Oracle 세션 사용률'
        columns=@('combined')   # 단일 컬럼에 '|' 로 묶어서 SELECT
        threshold=@{ warn=70; crit=90 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            # 결과는 'ACTIVE|TOTAL|LIMIT' 형태 한 줄
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $parts = $raw -split '\s*\|\s*'
            if ($parts.Count -lt 3) { return @{} }
            $active = [int]$parts[0]
            $total  = [int]$parts[1]
            $lim    = [int]$parts[2]
            $pct = if ($lim -gt 0) { [math]::Round($total * 100.0 / $lim, 1) } else { 0 }
            $st = if     ($pct -ge 90) { 'crit' }
                  elseif ($pct -ge 70) { 'warn' }
                  else                  { 'ok'   }
            return @{ active=$active; total=$total; sess_limit=$lim; used_pct=$pct; __status=$st }
        }
    }
    '10-wait-events'        = @{ id='db.oracle.wait_events';    label='Oracle Top Wait Events' }
    '11-rman-backup'        = @{ id='db.oracle.rman_backup';    label='Oracle RMAN 백업 이력' }
    '12-export-last'        = @{ id='db.oracle.export_last';    label='Oracle 마지막 EXPORT' }
    '13-standby-lag'        = @{ id='db.oracle.standby_lag';    label='Oracle Standby Lag' }
    '14-parameter-modified' = @{ id='db.oracle.param_modified'; label='Oracle 비기본 파라미터' }
    '15-undo'               = @{ id='db.oracle.undo';           label='Oracle UNDO 사용률' }
    '16-temp'               = @{ id='db.oracle.temp';           label='Oracle TEMP 사용률' }
    '17-controlfile' = @{
        id='db.oracle.controlfile'; label='Oracle Controlfile 다중화'
        columns=@('count')
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $c = if ($rows[0].count -ne $null) { [int]$rows[0].count } else { [int]$rows[0].cols[0] }
            $st = if ($c -lt 2) { 'warn' } else { 'ok' }
            return @{ count = $c; __status = $st }
        }
    }
}

$results = @()
$sqlFiles = Get-ChildItem -Path $sqlDir -Filter '*.sql' -ErrorAction SilentlyContinue | Sort-Object Name
if (-not $sqlFiles -or $sqlFiles.Count -eq 0) {
    return New-CheckResult -Id 'db.oracle' -Label 'Oracle 점검' -Category 'db_oracle' `
        -Cmd '(no sql files)' -Status 'n/a' -Note ("sql/oracle 폴더 비어있음: " + $sqlDir)
}

foreach ($f in $sqlFiles) {
    $base = $f.BaseName
    $meta = if ($sqlMeta.ContainsKey($base)) { $sqlMeta[$base] } else { @{ id="db.oracle.$base"; label=$base } }

    $args = @{
        Id      = $meta.id
        Label   = $meta.label
        SqlPath = $f.FullName
        Config  = $Config
    }
    if ($meta.columns)   { $args['ColumnNames'] = $meta.columns }
    if ($meta.parser)    { $args['Parser']      = $meta.parser }
    if ($meta.threshold) { $args['Threshold']   = $meta.threshold }

    $results += Invoke-OracleCheck @args
}
return $results
