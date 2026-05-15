# UPIS 점검 자동화 — Unix 에이전트 (AIX / HP-UX / Linux / Solaris)

점검 대상 DB·AP 서버(Unix 계열)에서 POSIX sh 로 실행되는 점검 수집기. agent-windows 와 동일한 `snapshot/v1` JSON 구조를 생성. QR 인코딩은 [옵션 A](#qr-인코딩-옵션-a) 에 따라 Windows 모니터 PC 에서 별도 처리.

## 디렉토리

```
agent-unix/
├── inspect.sh                  진입점 (POSIX sh)
├── config/site.dyg.json        사이트 설정 (임계값/inspector 등)
├── lib/
│   ├── common.sh               OS 감지/시각/로깅/호스트 정보
│   ├── json.sh                 JSON 빌더 (jq 없이)
│   ├── snapshot.sh             snapshot/v1 직렬화
│   └── oracle.sh               sqlplus 래퍼 (dry-run 지원)
├── checks/
│   ├── linux/                  lscpu / free / df -PT / vmstat / journalctl / ip /
│   ├── aix/                    prtconf / svmon / df -gH / vmstat / errpt / ifconfig
│   ├── hpux/                   machinfo / bdf / sar / syslog
│   ├── solaris/                psrinfo / df -h / vmstat / fmdump
│   └── oracle/                 17개 sqlplus 체크 (OS 무관)
├── snapshots/                  회차별 영구 보관
└── out/snapshot.json           최신 회차 출력
```

## 점검 항목

### OS 공통 (각 OS 디렉토리에서 명령만 다름)
| 항목 | Linux | AIX | HP-UX | Solaris |
|---|---|---|---|---|
| ap.hw.cpu       | lscpu      | prtconf       | machinfo  | psrinfo -v   |
| ap.hw.memory    | free -m    | svmon -G      | machinfo  | prtconf      |
| ap.disk.*       | df -PT     | df -gH        | bdf       | df -h -l     |
| ap.perf.cpu_pct | vmstat 1 2 | vmstat 1 2    | sar -u    | vmstat 1 2   |
| ap.perf.mem_pct | free -m    | (vmstat)      | (machinfo)| (vmstat)     |
| ap.log.system   | journalctl | errpt         | syslog    | fmdump -e    |
| ap.net.ip       | ip addr    | ifconfig -a   | netstat -in| ifconfig -a |
| ap.os.uptime    | uptime     | uptime        | uptime    | uptime       |
| ap.security.users| getent passwd | lsuser    | passwd    | logins       |

### Oracle (17 항목 — OS 무관, sqlplus 통해)
| ID | SQL |
|---|---|
| db.archive_mode       | `SELECT log_mode FROM v$database;` |
| db.alert_errors_24h   | `SELECT COUNT(*) FROM v$diag_alert_ext WHERE message_level<=8 AND originating_timestamp > systimestamp - 1;` |
| db.redo_logs          | `SELECT group#, members, status FROM v$log;` |
| db.sga                | `SELECT SUM(value)/1048576 FROM v$sga;` |
| db.pga                | `SELECT name, value FROM v$pgastat;` |
| db.tablespace.*       | `SELECT tablespace_name, used_percent FROM dba_tablespace_usage_metrics;` |
| db.datafile_status    | `SELECT status, COUNT(*) FROM dba_data_files;` |
| db.invalid_objects    | `SELECT COUNT(*) FROM dba_objects WHERE status='INVALID';` |
| db.sessions           | `SELECT * FROM v$session;` + sessions 파라미터 |
| db.wait_events        | `SELECT event, total_waits, time_waited FROM v$system_event;` |
| db.rman_backup        | `SELECT MAX(end_time) FROM v$rman_backup_job_details;` |
| db.export_last        | `SELECT MAX(start_time) FROM dba_datapump_jobs;` |
| db.standby_lag        | `SELECT name, value FROM v$dataguard_stats;` |
| db.parameter_modified | `SELECT COUNT(*) FROM v$parameter WHERE ismodified<>'FALSE';` |
| db.undo               | `dba_tablespace_usage_metrics` WHERE tablespace_name LIKE 'UNDOTBS%' |
| db.temp               | `dba_tablespace_usage_metrics` WHERE tablespace_name='TEMP' |
| db.controlfile        | `SELECT COUNT(*) FROM v$controlfile;` |

## 실행

```sh
# 기본 (config/site.dyg.json 자동 사용)
./inspect.sh

# 다른 사이트
./inspect.sh -c config/site.other.json

# 특정 체크만 (디버깅)
./inspect.sh -o cpu-info,disk

# OS 디스패처 강제 (실기 없는 PC 에서 명령어 매핑 검증)
UPIS_OS_OVERRIDE=aix ./inspect.sh

# Oracle 드라이런 (DB 없이 mock 으로 17항목 전체 검증)
ORACLE_DRY_RUN=1 ./inspect.sh

# 실제 Oracle 연결
ORACLE_CONNECT="sys/********@dbhost:1521/UPIS as sysdba" ./inspect.sh
```

## QR 인코딩 (옵션 A)

Unix 에이전트는 `out/snapshot.json` 만 만든다. QR 화면 표시는 **Windows 모니터 PC** 에서 별도 도구로:

```powershell
# Windows 측 (inspection-poc 디렉토리에서)
.\encode-snapshot.ps1 -InputPath agent-unix\out\snapshot.json -OutDir out_unix_encoded
# → out_unix_encoded\frames.json 생성 → viewer.html 로 QR 표시 → 갤럭시탭 PWA 스캔
```

DB 서버에 Python/Perl 외 의존성을 깔지 않아도 되는 게 옵션 A 의 핵심.

## 산출물 schema (snapshot/v1)

agent-windows 와 100% 동일. 통합 라운드트립 검증 완료 (`encode-snapshot.ps1` → `decoder/decode.js`):

```json
{
  "schema": "snapshot/v1",
  "site": "dyg", "site_name": "단양군 UPIS",
  "round": "2026-05", "round_date": "2026-05-15",
  "tier": "db",
  "host": { "hostname": "...", "os": "...", "ip": "..." },
  "taken_at": "2026-05-15T17:45:33+09:00",
  "took_ms": 97325,
  "agent_version": "0.1.0-unix",
  "inspector": "홍길동",
  "items": [
    {
      "id": "db.archive_mode",
      "label": "아카이브 모드",
      "category": "dbms_config",
      "method": "auto",
      "cmd": "SELECT log_mode FROM v$database;",
      "value": { "log_mode": "ARCHIVELOG" },
      "status": "ok",
      "took_ms": 12,
      "note": "..."
    }
  ],
  "summary": { "total_items": 31, "ok": 29, "warn": 1, "crit": 1, "errors": 0, "pending_manual": 0 }
}
```

## 호환성 메모

- **POSIX sh 기준** — bash 전용 문법(`[[ ]]`, 배열, `local`, `echo -e`) 미사용. AIX `ksh`, HP-UX `sh`, Solaris `ksh`, Linux `bash` 모두 동작.
- **`perl` 의존성** — UTF-8 출력 sanitizer 가 perl 의 `Encode` 모듈을 사용. 4개 OS 전부 base 설치에 포함되어 있음. 없으면 sanitizer 만 skip 되고 (출력에 invalid byte 가 섞일 수 있음) 동작은 계속됨.
- **변수명 주의** — `GROUPS` 는 bash 의 read-only 특수변수. shell agnostic 을 위해 명시적으로 다른 이름 사용 (`REDO_GROUPS` 등).

## 검증 결과 (2026-05-15)

| 단계 | 결과 |
|---|---|
| sh -n 문법 검사 (모든 .sh 파일) | ✓ 31/31 pass |
| Linux e2e (실기) | ✓ 12 items, snapshot/v1 valid |
| AIX dispatcher (UPIS_OS_OVERRIDE=aix) | ✓ checks/aix/ 디스패치, graceful degrade |
| HP-UX dispatcher | ✓ |
| Solaris dispatcher | ✓ |
| Oracle 17개 dry-run | ✓ 17 + tablespace 추가 행 = 19 db items |
| agent-windows 인코더 라운드트립 | ✓ raw 1530B → gz 744B → b45 1116c → 2 QR frames → decoded OK |

## 다음 단계 (실기 적용 시)

- [ ] AIX 실서버 1대에서 Linux 와 동일 항목 수집 검증 (명령어/파싱 보정)
- [ ] Oracle 실서버에서 17 항목 SQL 실행 검증 (권한/v$ view 가용성)
- [ ] 각 OS 별 site.{aix,hpux,solaris}.json 임계값 튜닝
- [ ] systemd / cron / inittab 으로 회차 트리거
