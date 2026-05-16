# 개발계획서 — inspection-agent v2 (대화형 설정 + GSS 자동탐지 + Telnet)

> **상태**: DRAFT (사용자 검토 대기)
> **기획서**: `@docs/product-specs/inspection-agent-v2-setup.md` (승인 2026-05-16)
> **모드**: 속도 모드 — 중간 codex 검토 생략, 구현 완료 후 전체 검증만
> **선행 commit**: `153f2cc` (v1 SSH 매개 점검), `298b735` (qrcode.min.js fail-loud 가드)

---

## 1. Step 분해

각 step 은 독립적으로 검증 가능. Step 1~3 은 lib 신규 (의존성 없음, 병렬 가능). Step 4~6 은 lib 위에 빌드. Step 7 은 기존 checks 수정. Step 8 은 문서.

| Step | 산출물 | LOC 추정 | 의존 |
|------|-------|----------|------|
| **1** | `lib/DPAPI.ps1` — Protect/Unprotect-Password | 40 | — |
| **2** | `lib/Telnet.ps1` — Invoke-RemoteTelnet (plink -telnet) | 120 | — |
| **3** | `lib/GssDetect.ps1` — Find-GssPath (ps-grep GSS/java) | 80 | — |
| **4** | `setup.ps1` + `setup.bat` — 대화형 wizard | 250 | 1, 2 |
| **5** | `inspect.ps1` + `inspect.bat` — active.json 로드 + `-Setup` 분기 | 30 (수정) | 4 |
| **6** | `inspect-remote.ps1` — proto=telnet 분기 | 40 (수정) | 2 |
| **7** | `checks/gis-gss-running.ps1` + `gis-gss-log-purge.ps1` — java grep fallback, 캐시 경로 활용 | 60 (수정) | 3 |
| **8** | `README.md` — setup wizard / Telnet 섹션 | 80 | — |
| **9** | 검증 (V-1~V-8) | — | 1~8 |

**총 LOC**: 신규 약 490, 수정 약 130. 작업 시간 예상 4-6 시간 (테스트 포함).

---

## 2. Step 상세

### Step 1 — lib/DPAPI.ps1 (NFR-1)

**함수**:
```powershell
function Protect-Password {
  param([Parameter(Mandatory)][string] $Plain)
  # SecureString → DPAPI 암호화 base64 (CurrentUser scope)
  # 반환: base64 문자열
}

function Unprotect-Password {
  param([Parameter(Mandatory)][string] $Encrypted)
  # base64 → SecureString → plain string (메모리에서만)
  # 반환: plain string (호출 직후 사용 후 즉시 폐기 권장)
}
```

**핵심**:
- `ConvertFrom-SecureString` (DPAPI CurrentUser scope) — Windows 사용자별로만 복호화 가능 (NFR-1).
- PS 4.0 호환 (Server 2012 R2). `Marshal.SecureStringToBSTR` 로 plain 추출 (PS 5+ 의 `ConvertFrom-SecureString -AsPlainText` 미사용).

**검증** (`.scratch/test-dpapi.ps1`):
```powershell
$pw = "1qkrdnrwls!"
$enc = Protect-Password -Plain $pw
$dec = Unprotect-Password -Encrypted $enc
if ($dec -ne $pw) { throw "라운드트립 실패" }
Write-Host "OK enc.len=$($enc.Length) dec='$dec'"
# 다른 사용자/PC 에서 복호화 시도 → throw 가 발생하는지도 수동 검증 (NFR-1)
```

---

### Step 2 — lib/Telnet.ps1 (FR-3-a)

**함수**:
```powershell
function Test-PlinkAvailable { ... }  # plink.exe 존재 확인. 없으면 throw + 안내.

function Invoke-RemoteTelnet {
  param(
    [Parameter(Mandatory)] $Remote,         # config.remotes.unix_db
    [Parameter(Mandatory)][string] $Command,
    [int] $TimeoutSec = 30
  )
  # 1. password DPAPI 복호화 (Unprotect-Password)
  # 2. plink.exe -telnet -P {port} -l {user} -pw {pw} {host} 로 interactive shell
  # 3. 명령 + sentinel ("echo __END__$?") 전송
  # 4. stdout 에서 sentinel 보일 때까지 read, exit code 추출
  # 5. 반환: { ok, stdout, stderr, exitCode }
}

function Copy-RemoteFile-Telnet {
  # PoC 비범위 — throw "Telnet 환경에서 파일 복사 미지원. 명령 stdout 으로 받으세요."
}
```

**핵심**:
- `plink.exe` 의 `-telnet` 모드 활용.
- prompt 매칭: `$` / `#` / `> ` 어느 하나 + sentinel 토큰.
- 패스워드는 메모리에서만 plain. plink 실행 인자에 전달 후 즉시 변수 clear.

**검증** (loopback 불가 — 실제 Telnet 서버 필요. PoC 는 다음 출장 V-5 에서 실증):
- 단위: prompt 매칭 정규식 unit test (`.scratch/test-telnet-parser.ps1`).

---

### Step 3 — lib/GssDetect.ps1 (FR-2)

**함수**:
```powershell
function Find-GssPath {
  param(
    [Parameter(Mandatory)] $Config,
    [switch] $ForceRescan
  )
  # 1. 캐시 ($Config._gss_cache.gss_root) 있고 -ForceRescan 아니면 캐시 반환.
  # 2. 로컬 Windows: Test-Path $Config.paths.gss_log_dir → 있으면 로컬 정보 반환.
  # 3. remotes.unix_db.enabled → SSH/Telnet 폴백:
  #    a. Invoke-RemoteCommand "ps -ef | grep GSS | grep -v grep" → 경로 추출
  #    b. 빈 결과면 "ps -ef | grep java | grep -v grep" → Java -D 또는 catalina home 에서 추출
  #    c. 추출된 dir + /logs 존재 확인 (test -d)
  # 4. 캐시 저장 ($Config._gss_cache) → 호출자가 active.json 에 persist.
  # 5. 반환: { gss_root, log_dir, detected_via, detected_at, source: "local"|"remote" }
}
```

**정규식 발췌 (테스트 필요)**:
- GSS line: `/[^\s]+GSS[^\s/]*\b` → "/home/oracle/GeoNURIS_Spatial_Server/bin/gss-server" → root = "/home/oracle/GeoNURIS_Spatial_Server"
- java line: `-Dcatalina\.home=([^\s]+)` 또는 `/[^\s]+/bin/java` → java 설치 경로

**검증** (`.scratch/test-gssdetect.ps1`):
- Mock `ps -ef` 출력 4 종 (GSS 발견 / java 발견 / 둘 다 없음 / 로컬 우선) 으로 unit test.

---

### Step 4 — setup.ps1 + setup.bat (FR-1)

**setup.ps1 흐름**:
```
1. Banner 출력 ("UPIS 점검 자동화 — 사이트 설정 v0.2.0")
2. 기존 active.json 존재 시 "덮어쓰기/취소?" 확인
3. Read-Host 반복:
   - 지자체명, 시스템명, 점검자명 (default %USERNAME%)
   - site_code (자동 변환 제안 + 사용자 수정 가능)
     · 자동 변환 규칙: 매핑 테이블 (강진군→gangjin, 단양군→danyang, 그 외→사용자 입력)
   - 접속 프로토콜 [ssh|telnet] (default ssh)
   - DB host (IPv4 또는 hostname 정규식 검증)
   - DB port (default 22 또는 23)
   - DB user (default root)
   - DB password (Read-Host -AsSecureString)
   - DB OS [aix|hpux|linux|solaris] (선택)
4. 입력 요약 출력 + 사용자 최종 확인 ("저장? [y/n]")
5. 연결 테스트 1회 실행 (Invoke-RemoteSsh 또는 Invoke-RemoteTelnet 으로 "echo OK; uname -a")
   - 실패 → 재시도/취소 선택
6. active.json + site.{site_code}.json 저장 (UTF-8 BOM, NFR-5)
   - password 는 Protect-Password 로 암호화한 password_dpapi
7. "다음 실행은 inspect.bat 더블클릭" 안내
```

**setup.bat**:
```bat
@echo off
chcp 65001 > nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1"
pause
```

**한글 입력 안전**: Read-Host 는 console 인코딩에 따라 cp949 입력될 수 있음 → 입력 받기 전 `chcp 65001` + `[Console]::InputEncoding = UTF8` 강제 (오늘 office.md mojibake 함정 회피).

---

### Step 5 — inspect.ps1 / inspect.bat (FR-1-a, FR-1-e)

**inspect.ps1 변경 (line 18-22 영역)**:
```powershell
param(
    [string] $ConfigPath,            # 변경: 기본값 제거 — active.json 자동 사용
    [string[]] $OnlyChecks,
    [switch] $Setup                  # 신규: -Setup → setup.ps1 호출 후 종료
)

if ($Setup) {
    & (Join-Path $PSScriptRoot 'setup.ps1')
    exit 0
}

# active.json 자동 로드
if (-not $ConfigPath) {
    $activePath = Join-Path $PSScriptRoot 'config\active.json'
    if (-not (Test-Path $activePath)) {
        Write-Host "[INFO] config\active.json 부재 — setup wizard 자동 실행" -ForegroundColor Yellow
        & (Join-Path $PSScriptRoot 'setup.ps1')
        if (-not (Test-Path $activePath)) {
            Write-Host "[INFO] setup 취소됨 — 종료" -ForegroundColor Yellow
            exit 0
        }
    }
    $ConfigPath = $activePath
}
```

**inspect.bat**: 메시지만 갱신.

---

### Step 6 — inspect-remote.ps1 (FR-3)

**변경 (line 39-46 remotes 검증 영역)**:
```powershell
$remote = $cfg.remotes.$RemoteKey
if (-not $remote.enabled) { throw "..." }

# 신규: proto 분기
$proto = if ($remote.PSObject.Properties['proto']) { $remote.proto } else { 'ssh' }

# 신규: Telnet 모드면 보안 경고
if ($proto -eq 'telnet') {
    Write-Log -Level WARN -Msg "Telnet 사용 — 패스워드 평문 전송. SSH 전환 권장."
    $snapshotMeta = @{ security_warning = 'telnet_used' }
}

# 1) 원격 ping — Invoke-RemoteSsh 대신 라우팅 함수 사용
$ping = Invoke-Remote -Remote $remote -Proto $proto -Command "echo OK; uname -a"
```

**라우팅 함수** (lib/Common.ps1 또는 lib/RemoteRouter.ps1 신규):
```powershell
function Invoke-Remote {
    param($Remote, [string]$Proto, [string]$Command, [int]$TimeoutSec=30)
    if ($Proto -eq 'telnet') { return Invoke-RemoteTelnet -Remote $Remote -Command $Command -TimeoutSec $TimeoutSec }
    else                     { return Invoke-RemoteSsh    -Remote $Remote -Command $Command -TimeoutSec $TimeoutSec }
}
```

**summary-remote.html 변경**: `_meta.security_warning='telnet_used'` 면 노란색 배너 추가 (인라인 HTML).

---

### Step 7 — checks/gis-gss-*.ps1 (FR-2)

**gis-gss-running.ps1 / gis-gss-log-purge.ps1** 공통:
- 현재: 로컬 Test-Path → 미존재 시 SSH 폴백으로 `ps -ef | grep GSS`
- 변경: Step 3 의 `Find-GssPath` 호출로 통합 — GSS grep 빈 결과 시 자동으로 java grep fallback 시도.
- 결과 캐시는 $cfg._gss_cache 에 저장 → 동일 실행에서 두 check 가 재탐지 안 함.
- snapshot 의 item.cmd 필드에 `detected_via` 표기 (FR-2-c).

---

### Step 8 — README

- "처음 사용 — setup wizard" 섹션 신규 (앞부분으로)
- "Telnet 환경 점검" 섹션 신규
- "GSS 자동 탐지" 섹션 신규
- 기존 v1 사용 흐름은 "고급" 섹션으로 이동

---

### Step 9 — 검증 (V-1 ~ V-8)

기획서의 8 시나리오 그대로. **이번 세션에선 V-1, V-2, V-6 (IUHOME 로컬 검증 가능)** 만 1차 검증, V-3~V-5, V-7, V-8 은 다음 출장 (강진군 실서버) 검증.

- **V-1** (setup 자동 진입): IUHOME 에서 `inspect.bat` 실행 → wizard 진입 확인.
- **V-2** (DPAPI 검증): active.json 텍스트 열어 `password=` 없고 `password_dpapi` 있는지 grep.
- **V-6** (v1 호환): 기존 `site.dyg.json` 그대로 두고 `inspect.ps1 -ConfigPath site.dyg.json` 실행 → 동작 OK.

---

## 3. 작업 순서 + 의존성

```
[Step 1: DPAPI]   [Step 2: Telnet]   [Step 3: GssDetect]   ← 병렬 가능
       └──────────┬──────────┘                │
                  │                           │
              [Step 4: setup]                 │
                  │                           │
              [Step 5: inspect.ps1]           │
                  │                           │
              [Step 6: inspect-remote]   [Step 7: checks/gss]
                  │                           │
                  └────────── [Step 8: README]
                                  │
                          [Step 9: 검증 V-1/2/6]
```

권장 진행 순서: 1 → 3 → 2 → 4 → 5 → 7 → 6 → 8 → 9.

각 step 완료 시 단위 검증 (있다면) + git wip commit. (`feedback_session_resume_commit.md` 룰)

---

## 4. wip 커밋 컨벤션

- step 단위: `wip(inspection-agent-v2): step{N} {short} — 다음: step{N+1} {short}`
- 예: `wip(inspection-agent-v2): step1 DPAPI helper OK (라운드트립 검증) — 다음: step3 GssDetect`

---

## 5. 위험 / 미확정

| # | 위험 | 완화 |
|---|------|------|
| R-1 | plink.exe 의 Telnet prompt 변종 (사이트마다 다름: `$ `, `# `, `> `, `bash-4.2$`, ...) | sentinel 토큰 활용 — prompt 매칭 대신 `echo __END__$?` 으로 종료 감지 (Step 2) |
| R-2 | DPAPI 키가 사용자 프로파일 + PC 에 묶여 — config 다른 PC 로 옮기면 복호화 불가 | NFR-1 의 의도된 동작. 명확한 에러 메시지 (V-7) |
| R-3 | Server 2012 R2 의 PS 4.0 에서 ConvertFrom-SecureString 동작 차이 | Step 1 에서 PS 4.0 + PS 5.1 양쪽 테스트 |
| R-4 | 자동 site_code 매핑 (강진군→gangjin) 사전 부재 시 사용자 입력 강제 | Step 4 의 입력 단계에서 항상 default 표시 + 사용자 수정 가능 |

---

## 6. 비범위 재확인

- agent-unix GSS 자동 점검 항목 추가 (별도 sprint)
- Telnet 환경 파일 복사 (tftp/ftp 폴백) (별도 sprint)
- SW Manager URL 별도 사이트 정보 wizard (별도 sprint)
- v1 → v2 자동 migration 도구 (현재는 v1 형식 그대로 동작)

---

*draft v1 — 2026-05-16 — 사용자 검토 후 구현 진입*
