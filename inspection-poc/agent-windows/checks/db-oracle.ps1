param($Config)
. (Join-Path $PSScriptRoot '..\lib\Oracle.ps1')

# sql/oracle/*.sql 폴더의 .sql 파일을 차례로 실행. 새 항목 추가는 sql 파일 + sqlMeta 한 줄.
$sqlDir = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql\oracle'
if (-not (Test-Path $sqlDir)) {
    return New-CheckResult -Id 'db.oracle' -Label 'Oracle 점검' -Category 'db_oracle' `
        -Cmd '(sql dir missing)' -Status 'n/a' -Note ("sql 디렉토리 없음: " + $sqlDir)
}

# 항목별 metadata + parser. parser 미정의면 raw rows 만 capture (status='ok').
# columns 정의돼 있고 row.cols 와 길이 일치하면 자동으로 named row 로 변환됨 (Invoke-OracleSql).
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
    '03-redo-logs' = @{
        id='db.oracle.redo_logs'; label='Oracle Redo Log 그룹'
        columns=@('combined')   # groups|members|invalid_count
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            if ($p.Count -lt 3) { return @{ raw = $raw } }
            $groups  = [int]$p[0]; $members = [int]$p[1]; $invalid = [int]$p[2]
            $st = if ($invalid -gt 0) { 'warn' } else { 'ok' }
            return @{ groups=$groups; members=$members; invalid_status=$invalid; __status=$st }
        }
    }
    '04-sga' = @{
        id='db.oracle.sga'; label='Oracle SGA 구성'
        columns=@('total_mb')
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $mb = if ($rows[0].total_mb -ne $null) { [int]$rows[0].total_mb } else { [int]$rows[0].cols[0] }
            return @{ total_mb = $mb; total_gb = [math]::Round($mb / 1024.0, 2) }
        }
    }
    '05-pga' = @{
        id='db.oracle.pga'; label='Oracle PGA 사용량'
        columns=@('combined')   # target_mb|allocated_mb
        threshold=@{ warn=80; crit=90 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            if ($p.Count -lt 2) { return @{ raw=$raw } }
            $target = [int]$p[0]; $alloc = [int]$p[1]
            $pct = if ($target -gt 0) { [math]::Round($alloc * 100.0 / $target, 1) } else { 0 }
            $st = if     ($pct -ge 90) { 'crit' }
                  elseif ($pct -ge 80) { 'warn' }
                  else                  { 'ok'   }
            return @{ target_mb=$target; allocated_mb=$alloc; used_pct=$pct; __status=$st }
        }
    }
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
    '07-datafile-status' = @{
        id='db.oracle.datafile_status'; label='Oracle Datafile 상태'
        columns=@('combined')   # total|offline
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            if ($p.Count -lt 2) { return @{ raw=$raw } }
            $total = [int]$p[0]; $offline = [int]$p[1]
            $st = if ($offline -gt 0) { 'crit' } else { 'ok' }
            return @{ total=$total; offline=$offline; __status=$st }
        }
    }
    '08-invalid-objects' = @{
        id='db.oracle.invalid_objects'; label='Oracle INVALID 객체 수'
        columns=@('count')
        threshold=@{ warn=1; crit=50 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $c = if ($rows[0].count -ne $null) { [int]$rows[0].count } else { [int]$rows[0].cols[0] }
            $st = if     ($c -ge 50) { 'crit' }
                  elseif ($c -ge 1)  { 'warn' }
                  else                { 'ok'   }
            return @{ count = $c; __status = $st }
        }
    }
    '09-sessions' = @{
        id='db.oracle.sessions'; label='Oracle 세션 사용률'
        columns=@('combined')   # active|total|sess_limit
        threshold=@{ warn=70; crit=90 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            if ($p.Count -lt 3) { return @{} }
            $active = [int]$p[0]; $total = [int]$p[1]; $lim = [int]$p[2]
            $pct = if ($lim -gt 0) { [math]::Round($total * 100.0 / $lim, 1) } else { 0 }
            $st = if     ($pct -ge 90) { 'crit' }
                  elseif ($pct -ge 70) { 'warn' }
                  else                  { 'ok'   }
            return @{ active=$active; total=$total; sess_limit=$lim; used_pct=$pct; __status=$st }
        }
    }
    '10-wait-events' = @{
        id='db.oracle.wait_events'; label='Oracle Top Wait Events (Idle 제외 top5)'
        columns=@('event','total_waits','time_waited')
        parser={ param($rows, $remote)
            $items = @()
            foreach ($r in $rows) {
                $event = if ($r.event) { [string]$r.event } else { [string]$r.cols[0] }
                $waits = if ($r.total_waits -ne $null) { [long]$r.total_waits } else { [long]$r.cols[1] }
                $time  = if ($r.time_waited -ne $null) { [long]$r.time_waited } else { [long]$r.cols[2] }
                $items += @{ event=$event; total_waits=$waits; time_waited=$time }
            }
            return @{ top_events = @($items) }
        }
    }
    '11-rman-backup' = @{
        id='db.oracle.rman_backup'; label='Oracle RMAN 백업 이력 (7d)'
        columns=@('combined')   # last_job|last_success
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{ __status='warn'; note='no rows' } }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            $lastJob     = if ($p.Count -ge 1) { $p[0] } else { '' }
            $lastSuccess = if ($p.Count -ge 2) { $p[1] } else { '' }
            $st = if (-not $lastSuccess -or $lastSuccess -eq '') { 'crit' } else { 'ok' }
            return @{ last_job=$lastJob; last_success=$lastSuccess; __status=$st }
        }
    }
    '12-export-last' = @{
        id='db.oracle.export_last'; label='Oracle 마지막 Datapump Export'
        columns=@('last_export')
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{ __status='warn'; last_export=$null } }
            $v = if ($rows[0].last_export) { [string]$rows[0].last_export } else { [string]$rows[0].cols[0] }
            $st = if (-not $v) { 'warn' } else { 'ok' }
            return @{ last_export=$v; __status=$st }
        }
    }
    '13-standby-lag' = @{
        id='db.oracle.standby_lag'; label='Oracle Standby Lag (Data Guard)'
        columns=@('combined')   # apply_lag|transport_lag
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{ __status='n/a'; note='DG 환경 아님' } }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            $apply     = if ($p.Count -ge 1) { $p[0] } else { '' }
            $transport = if ($p.Count -ge 2) { $p[1] } else { '' }
            if (-not $apply -and -not $transport) {
                return @{ apply_lag=$null; transport_lag=$null; __status='n/a'; note='DG 환경 아님' }
            }
            return @{ apply_lag=$apply; transport_lag=$transport }
        }
    }
    '14-parameter-modified' = @{
        id='db.oracle.param_modified'; label='Oracle 동적 변경된 파라미터 수'
        columns=@('count')
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $c = if ($rows[0].count -ne $null) { [int]$rows[0].count } else { [int]$rows[0].cols[0] }
            return @{ modified_count = $c }
        }
    }
    '15-undo' = @{
        id='db.oracle.undo'; label='Oracle UNDO Tablespace 사용률'
        columns=@('combined')   # tablespace_name|used_pct
        threshold=@{ warn=80; crit=90 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $raw = if ($rows[0].combined) { [string]$rows[0].combined } else { [string]$rows[0].raw }
            $p = $raw -split '\s*\|\s*'
            if ($p.Count -lt 2) { return @{ raw=$raw } }
            $ts = $p[0]; $pct = [double]$p[1]
            $st = if     ($pct -ge 90) { 'crit' }
                  elseif ($pct -ge 80) { 'warn' }
                  else                  { 'ok'   }
            return @{ tablespace=$ts; used_pct=$pct; __status=$st }
        }
    }
    '16-temp' = @{
        id='db.oracle.temp'; label='Oracle TEMP Tablespace 사용률'
        columns=@('used_pct')
        threshold=@{ warn=80; crit=90 }
        parser={ param($rows, $remote)
            if (-not $rows -or $rows.Count -eq 0) { return @{} }
            $pct = if ($rows[0].used_pct -ne $null) { [double]$rows[0].used_pct } else { [double]$rows[0].cols[0] }
            $st = if     ($pct -ge 90) { 'crit' }
                  elseif ($pct -ge 80) { 'warn' }
                  else                  { 'ok'   }
            return @{ used_pct=$pct; __status=$st }
        }
    }
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
