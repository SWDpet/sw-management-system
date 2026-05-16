# 점검 자동화 에이전트 v2 — 대화형 설정 + GSS 자동 탐지 + Telnet

> **상태**: DRAFT (사용자 검토 대기)
> **작성일**: 2026-05-16
> **선행 sprint**: `inspection-qr-batch` (v1 적재 파이프라인), `inspection-poc` (v1 SSH 매개 점검)
> **트리거**: 2026-05-16 강진군 실서버 실증 후 사용자 정리한 개선사항 3건 (`개선사항.txt`)

---

## 1. 배경

v1 (`153f2cc`) 으로 다음이 가능해졌다.
- `agent-windows` 로컬 점검 + QR 화면 표시
- `inspect-remote.ps1` 로 SSH 매개 UNIX(AIX/HP-UX/Linux/Solaris) DB 점검
- `agent-unix` 로 Oracle 17 항목 + system 항목 자동수집
- SW Manager `/api/inspection/qr-batch` 적재 (2026-05-16 e2e 검증 완료, `report.id=5`)

**그러나 강진군 UPIS-AP 실증 (2026-05-16) 후 다음 한계 노출**:

| # | 한계 | 영향 |
|---|------|------|
| 1 | `site.dyg.json` 같은 config 파일을 점검자가 수동 작성 | 사이트마다 JSON 편집 부담, 오타 위험, 비기술 점검자 운용 불가 |
| 2 | GSS 경로 (`C:\GeoNURIS_Spatial_Server\logs`) 가 사이트마다 다름. 특히 GSS 가 UNIX DB 서버에 설치된 경우가 흔함 | 윈도우 AP 서버에서 GSS 미발견 시 그냥 "n/a" 처리 — 실제로는 UNIX 쪽을 봐야 함 |
| 3 | 일부 옛 UNIX (AIX/HP-UX) 사이트가 SSH 미설치, Telnet 만 허용 | v1 의 SSH 매개 점검 불가 → 해당 사이트 점검 불가 |

---

## 2. 목적 / 비목적

### 목적
- 처음 실행 시 **대화형 wizard** 로 사이트 정보 입력 → config 자동 생성·저장. 다음 실행부터 재사용.
- GSS 경로 **자동 탐지** — 로컬 Windows 부재 시 UNIX DB 서버에서 `ps -ef|grep` 으로 동적 추출.
- **Telnet 매개 점검** 지원 — SSH 미설치 옛 사이트도 점검 가능.

### 비목적
- 갤럭시탭 PWA / SW Manager 적재 흐름 변경 X (v1 그대로 유지).
- 점검 항목 자체 추가/변경 X (별도 sprint).
- 운영 서버에 setup wizard 가 자동으로 보안 키 생성·배포 X (사용자가 키 관리).
- agent-unix 의 GSS 점검 항목 추가 X (현재 agent-unix 에는 GSS 체크 없음 — 별도 sprint 필요).

---

## 3. 기능 요구사항 (FR)

### FR-1. 대화형 setup wizard

#### FR-1-a. 실행 분기
- `inspect.bat` (또는 `inspect.ps1`) 실행 시 `config\active.json` 존재 여부 확인.
  - 부재 → `setup.ps1` 자동 진입 (또는 사용자 동의 후).
  - 존재 → 평소처럼 점검 진행.
- 점검자가 명시적으로 `setup.bat` 또는 `inspect.ps1 -Setup` 으로 재설정 가능.

#### FR-1-b. 입력 항목

| 키 | 라벨 | 검증 | 기본값 |
|---|------|------|--------|
| `site_name_ko` | 지자체명 | 한글, 1~30자 | (필수) |
| `system_name` | 시스템명 | 영문/한글, 1~30자 | "UPIS" |
| `inspector` | 점검자명 | 1~30자 | %USERNAME% |
| `site_code` | 사이트 코드 (영문) | `^[a-z0-9_-]{1,32}$` | 지자체명 자동 변환 제안 (예: 강진군→gangjin) |
| `db.proto` | 접속 프로토콜 | "ssh" \| "telnet" | "ssh" |
| `db.host` | DB 서버 IP | IPv4 또는 호스트명 | (필수) |
| `db.port` | DB 서버 포트 | 1~65535 | proto=ssh→22, telnet→23 |
| `db.user` | 계정 | 1~30자 | "root" |
| `db.password` | 패스워드 | 1자 이상, 마스킹 입력 | (필수) |
| `db.os` | OS (정보용) | "aix"\|"hpux"\|"linux"\|"solaris" | (선택) |

#### FR-1-c. 입력 검증 + 1차 연결 테스트
- 모든 항목 입력 후 1회 연결 테스트 자동 실행:
  - SSH: `ssh -o ConnectTimeout=5 ... 'echo OK; uname -a'`
  - Telnet: `plink -telnet -P {port} {user}@{host}` (`-pw` 옵션) + send `echo OK\n` (TCP 단일 명령은 어려워 PoC 한정 — 직접 telnet shell 실행 후 사용자에게 결과 확인 요청)
- 실패 시 사용자에게 "재시도/취소" 선택 제공.

#### FR-1-d. 저장
- `config\active.json` 으로 저장. 추가로 `config\site.{site_code}.json` 으로 백업 (인간 가독).
- 패스워드는 plain text 금지 — `ConvertFrom-SecureString` (DPAPI, 사용자별로만 복호화 가능) 으로 암호화한 base64 문자열 저장.
- JSON 구조 (현재 `site.dyg.json` 의 `remotes.unix_db` 영역을 활용):

```jsonc
{
  "site": "gangjin",
  "site_name": "강진군 UPIS",
  "inspector": "박욱진",
  "tier": "ap",
  "hosts": { "ap": { "expected_hostname": null, "ip_hint": null } },
  "thresholds": { /* v1 동일 */ },
  "paths": { /* v1 동일 — 다만 gss_log_dir 은 자동 탐지로 보강 (FR-2) */ },
  "purge": { /* v1 동일 */ },
  "remotes": {
    "unix_db": {
      "enabled": true,
      "proto": "ssh",                    // 신규: "ssh" | "telnet"
      "host": "10.0.0.21",
      "port": 22,
      "user": "root",
      "password_dpapi": "01000000d08c9d...",  // 신규: DPAPI 암호화. plain text 없음.
      "auth": "password",                // password | key | plink
      "os": "linux",
      "agent_path": "/home/root/agent-unix",
      "config_name": "site.gangjin.json"
    }
  },
  "_meta": {
    "created_at": "2026-05-16T19:42:00+09:00",
    "agent_version": "0.2.0",
    "created_by_setup": true
  }
}
```

#### FR-1-e. 두 번째 이후 실행
- `config\active.json` 로드. 패스워드 복호화는 메모리에서만 (`ConvertTo-SecureString` → `Marshal.SecureStringToBSTR`).
- 점검 진행. 변경 필요 시 `setup.bat` 또는 `inspect.ps1 -Setup`.

---

### FR-2. GSS 경로 자동 탐지

#### FR-2-a. 탐지 순서
1. **로컬 Windows**: `Test-Path C:\GeoNURIS_Spatial_Server` (기존 v1 동작).
2. **UNIX DB 서버 SSH/Telnet fallback** (config.remotes.unix_db.enabled 이고 로컬 미존재 시):
   1. `ps -ef | grep GSS | grep -v grep` → 결과 라인에서 `/...GSS.../` 디렉토리 추출.
   2. 위 결과 빈 경우 `ps -ef | grep java | grep -v grep` → Java -D 옵션 / Tomcat catalina home 에서 GSS 가능성 추출.
   3. 추출된 디렉토리 + `/logs` 가 존재하면 GSS 로그 점검 대상으로 확정.

#### FR-2-b. 결과 캐싱
- 한 번 탐지된 경로는 `config\active.json.gss_detected` 에 캐시:
```json
{ "gss_root": "/home/oracle/GeoNURIS_Spatial_Server",
  "log_dir": "/home/oracle/GeoNURIS_Spatial_Server/logs",
  "detected_at": "2026-05-16T19:55:00+09:00",
  "detected_via": "ps-grep-GSS" }
```
- 다음 실행 시 캐시 우선 사용. `inspect.ps1 -RescanGss` 옵션으로 강제 재탐지.

#### FR-2-c. 점검 결과 기록
- `snapshot.items` 의 `gis.gss.running` / `gis.gss.log_purge` 결과 `cmd` 필드에 탐지 경로 + 방법 표기:
```json
{ "id": "gis.gss.log_purge",
  "cmd": "remote: find /home/oracle/GeoNURIS_Spatial_Server/logs -mtime +30  (detected_via=ps-grep-GSS)" }
```

---

### FR-3. Telnet 매개 점검 지원

#### FR-3-a. 인터페이스
- `lib\Telnet.ps1` 신규 — SSH 와 동일 인터페이스 제공:
  - `Invoke-RemoteCommand -Remote $cfg.remotes.unix_db -Command "..."` → `{ ok, stdout, stderr, exitCode }`
  - `Copy-RemoteFile -Remote $cfg.remotes.unix_db -RemotePath $r -LocalPath $l` → `{ ok }`
- 구현: `plink.exe -telnet -P {port} -l {user} -pw {pw} {host}` 활용. Telnet 은 표준적 한 줄 명령 실행 / 파일 복사 지원 없음 → PoC 는 다음 두 방법:
  1. **interactive shell + read until prompt**: shell 시작 후 명령 + `exit` 자동화. 정규식으로 prompt 매칭 (예: `\$ ` / `# `).
  2. **파일 복사**: Telnet 은 SCP 없음. 대안 — `tftp` / `ftp` / `wget` (DB 서버에 가능한 클라이언트 존재 시) 또는 명령 결과를 stdout 으로 받아 로컬에서 파일로 저장.
- 본 sprint 는 1) 명령 실행만 구현. 파일 복사는 후속 sprint.

#### FR-3-b. 보안 표시
- Telnet 사용 사이트는 snapshot 의 `_meta` 에 `security_warning: "telnet_used"` 추가.
- summary.html 에 노란색 배너로 표시 ("⚠ 본 점검은 Telnet 사용 — 패스워드 평문 전송. SSH 전환 권장.").

#### FR-3-c. 점검 가능 항목 제약
- agent-unix 의 모든 항목은 명령 실행 기반 → Telnet 으로도 동작 가능.
- 단 `agent-unix/lib/qr-encode.pl` 사용한 frames.json 생성은 stdout 으로 받아 로컬 저장 (Telnet 한정 path).

---

## 4. 비기능 요구사항 (NFR)

| # | 항목 | 요구 |
|---|------|------|
| NFR-1 | 보안 | 패스워드는 DPAPI 암호화. plain text 금지. config 파일이 다른 사용자에게 유출되어도 복호화 불가. |
| NFR-2 | PS 호환 | Windows Server 2012 R2 의 PowerShell 4.0 호환 유지 (153f2cc 의 4 가지 함정 회피 패턴 그대로 적용). |
| NFR-3 | 오프라인 | 폐쇄망에서 외부 모듈 의존성 없음. plink.exe 는 반출 패키지 동봉. |
| NFR-4 | 마이그레이션 | 기존 `site.dyg.json` 형식은 v2 에서도 호환 (단 plain password 영역만 setup wizard 거치면 DPAPI 변환). |
| NFR-5 | 한글 | wizard 입력·저장은 모두 UTF-8 + BOM 보존 (Server 2012 R2 의 PS 5.x 가 BOM 없는 UTF-8 을 cp949 로 해석하는 함정 회피). |

---

## 5. 영향 파일

### 신규
- `inspection-poc/agent-windows/setup.ps1` — 대화형 wizard
- `inspection-poc/agent-windows/setup.bat` — 더블클릭 launcher
- `inspection-poc/agent-windows/lib/Telnet.ps1` — Telnet 원격 명령 실행
- `inspection-poc/agent-windows/lib/DPAPI.ps1` — 패스워드 암호화/복호화 helper
- `inspection-poc/agent-windows/lib/GssDetect.ps1` — GSS 자동 탐지 (ps-grep)

### 수정
- `inspection-poc/agent-windows/inspect.ps1` — `-Setup` 옵션 추가, active.json 자동 로드 분기
- `inspection-poc/agent-windows/inspect.bat` — active.json 부재 시 setup 안내
- `inspection-poc/agent-windows/inspect-remote.ps1` — proto=telnet 분기 추가
- `inspection-poc/agent-windows/lib/Ssh.ps1` — `Find-GssPathOnRemote` 가 GssDetect.ps1 호출
- `inspection-poc/agent-windows/checks/gis-gss-running.ps1` — `ps -ef|grep java` fallback
- `inspection-poc/agent-windows/checks/gis-gss-log-purge.ps1` — `ps -ef|grep java` fallback + 캐시된 GSS 경로 사용
- `inspection-poc/agent-windows/README.md` — setup wizard / Telnet 섹션 추가

### 비변경
- `agent-unix/*` — 그대로 (Telnet 측은 stdout 으로 받음, 변경 없음)
- SW Manager 서버 측 — 그대로 (적재 흐름 v1 유지)

---

## 6. 검증 시나리오

| # | 시나리오 | 기대 결과 |
|---|---------|-----------|
| V-1 | active.json 부재 + `inspect.bat` 실행 | setup wizard 자동 진입, 입력 완료 후 active.json 생성 |
| V-2 | 패스워드 입력 후 active.json 텍스트 확인 | DPAPI 암호화 문자열만 존재. `password=` 키워드 plain text 없음 |
| V-3 | active.json 존재 + `inspect.bat` 실행 | 평소처럼 점검 진행. 패스워드 재입력 안 함 |
| V-4 | Windows 에 GSS 미설치 + remotes.unix_db 활성 | SSH 폴백으로 GSS 경로 자동 추출. snapshot.items 의 cmd 필드에 detected_via 표기 |
| V-5 | proto="telnet" 사이트 점검 | plink -telnet 으로 명령 실행, snapshot 의 _meta.security_warning="telnet_used", summary.html 에 보안 배너 |
| V-6 | 기존 site.dyg.json 그대로 사용 | v1 동작 그대로. setup wizard 자동 진입 안 함 |
| V-7 | DPAPI 암호화 config 를 다른 Windows 사용자가 열기 | 복호화 실패, 명확한 에러 메시지 ("이 config 는 {원래 사용자}의 DPAPI 키로 암호화됨. 해당 사용자로 로그인 후 재실행 필요.") |
| V-8 | SSH/Telnet 연결 실패 시 setup wizard 재시도 흐름 | "재시도/취소" 선택 UI 동작. 취소 시 active.json 생성 안 됨 |

---

## 7. 마이그레이션

- 기존 사이트 (`site.dyg.json`) 는 v1 형식 그대로 작동. setup wizard 거치지 않음.
- v2 새 사이트는 setup wizard 강제. 기존 사이트도 보안 강화를 위해 setup wizard 재실행 권장 (선택).
- `agent_version` 을 "0.1.0" → "0.2.0" 으로 올리고, snapshot/v1 형식 그대로 (스키마 변경 X, 메타만 추가).

---

## 8. 후속 sprint (비범위)

- agent-unix 에 GSS 자동 점검 추가 (UNIX 측에서 직접 inspect.sh 실행 시).
- Telnet 환경에서 파일 복사 (tftp/ftp 폴백).
- setup wizard 의 PWA 적재 흐름 (현재 wizard 는 점검 대상 서버 정보까지만, SW Manager URL 별도 사이트 정보 필요).

---

*draft v1 — 2026-05-16 — 본 sprint 의 핵심은 "비기술 점검자가 USB 만 받아 더블클릭으로 새 사이트 점검 가능" 까지 끌어올리는 것.*
