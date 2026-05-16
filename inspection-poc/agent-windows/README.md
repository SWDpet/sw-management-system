# UPIS 점검 자동화 — Windows AP 에이전트 (PoC)

점검 대상 서버(Windows Server 2012R2 이상)에서 실행되어 점검 항목을 자동 수집하고, 모니터에 QR 코드로 결과를 표시하는 모듈.

## 빠른 시작 (v0.2.0+)

### 1️⃣ 처음 사용 — 사이트 설정
```
setup.bat 더블클릭
```
대화형 wizard 가 다음을 물어봄: 지자체명 / 시스템명 / 점검자 / 사이트 코드 / DB 서버 IP·포트·계정·패스워드·OS / 접속 프로토콜(SSH/Telnet).
- 패스워드는 Windows DPAPI 로 암호화 저장 — 본 사용자 계정만 복호화 가능.
- 입력 완료 후 1 회 연결 테스트.
- `config\active.json` + `config\site.{code}.json` 생성.

### 2️⃣ 점검 실행
```
inspect.bat 더블클릭
```
저장된 `active.json` 으로 자동 점검 → `out\summary.html` 자동 표시 → 갤럭시탭 PWA 로 QR 스캔.

### 재설정 / 다른 사이트
```
setup.bat 더블클릭   # 기존 설정 덮어쓰기 확인 후 재진입
```

> ✏ **v1 호환**: 기존 `config\site.dyg.json` 그대로 사용하려면 `inspect.ps1 -ConfigPath .\config\site.dyg.json`

## 디렉토리
```
agent-windows/
├── inspect.ps1                  진입점
├── config/site.dyg.json         사이트별 설정 (임계값, 경로 등)
├── lib/
│   ├── Common.ps1               JSON/로그/임계값 유틸
│   ├── Collector.ps1            checks/ 디스패치 + 스냅샷 빌드
│   ├── Snapshot.ps1             스냅샷 저장/직전 회차 조회
│   ├── Differ.ps1               델타 계산
│   └── QrPayload.ps1            압축+base45+CRC+SHA-1+프레임 분할
├── checks/                      체크 모듈 20개 (AP 12 + GIS 6 + LED 4묶음 = 24 항목)
├── snapshots/                   회차 결과 영구 보관
└── out/                         최신 회차 산출물
    ├── delta.json
    ├── frames.json              QR 변환용 프레임 시퀀스
    └── summary.html             모니터 표시용 요약
```

## 점검 항목 (24개)

### AP 서버 — 14개
| ID | 카테고리 | 자동/수동 |
|---|---|---|
| ap.hw.cpu / memory / adapter | 구성 | A |
| ap.perf.cpu_pct / mem_pct | 성능 | A |
| ap.disk.c / d (외 마운트 자동) | 파일시스템 | A |
| ap.log.system_err / security_err | 로그 | A |
| ap.net.routes / ip | 네트워크 | A |
| ap.security.users | 보안 | A |
| ap.led.system / psu / disk / ap.cable | 육안 | M (수동 입력 대기) |

### GIS — 6개
| ID | 자동/수동 |
|---|---|
| gis.gss.running | A |
| gis.gss.log_purge | A (기본 dry-run) |
| gis.gws.running | A |
| gis.gws.log_purge | A |
| gis.gws.store_size | A |
| gis.gws.http | A |

## 실행
```powershell
# 사이트 설정 지정
.\inspect.ps1 -ConfigPath .\config\site.dyg.json

# 특정 체크만 실행 (디버깅)
.\inspect.ps1 -OnlyChecks ap-perf-cpu,gis-store-size
```

### 현장 점검자용 — 더블클릭 launcher (`inspect.bat`)
PowerShell 직접 호출이 부담스러울 때:
1. `inspect.bat` 더블클릭
2. inspect.ps1 자동 실행 + `out\summary.html` (QR 뷰어) 자동 브라우저 표출
3. 갤럭시탭 PWA 스캐너로 QR 캡처

설치:
- 디렉토리 통째로 점검 대상 서버에 복사
- `config\site.dyg.json` 의 site/inspector/IP 임계값 미리 편집
- (1회) PowerShell 실행 정책: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`
- 바탕화면에 `inspect.bat` 바로가기 만들면 1-클릭 점검 가능

## 라운드트립 검증
에이전트 산출물(`out/frames.json`)을 PoC 디코더로 검증 가능:
```powershell
cd ..\decoder
node decode.js ..\agent-windows\out\frames.json
```

## PS 5.1 호환성 주의사항
다음 4가지 함정에 걸려서 디버깅함:
1. **UTF-8 BOM 필요** — .ps1 파일이 UTF-8 BOM 없이 저장되면 한글이 깨짐
2. **strict mode** — 단일 객체 `.Count` 호출 에러. strict mode 미사용
3. **byte -shl 결과 truncation** — `[byte]65 -shl 8 = 0`. int 캐스트 필수
4. **32비트 hex 리터럴의 부호확장** — `[int64]0xEDB88320`이 음수 되는 문제. 양수 십진수로 명시

## 운영 시나리오
1. 점검자가 콘솔에 로그인 후 `.\inspect.ps1` 실행 (약 15초 소요)
2. `out\summary.html`이 자동 표시 (또는 점검자가 직접 열기)
3. 화면에 QR 2장 표출 → 갤럭시탭으로 스캔
4. 사외망 복귀 후 SW 사업관리시스템에 업로드

## SSH 매개 원격 점검 (v0.2+)

GSS 가 Windows 가 아닌 Unix(AIX/HP-UX/Linux/Solaris)에 설치된 사이트, 또는 Unix DB 서버 점검을 노트북 1대로 처리하기 위한 워크플로우.

### (1) GSS 자동 탐지 — 로컬 우선 + Unix SSH fallback
`gis-gss-running.ps1` / `gis-gss-log-purge.ps1`:
1. 로컬 Windows 에서 `Get-Process` / `Test-Path` 로 GSS 우선 탐지
2. 로컬 없음 + `config.remotes.gis_unix.enabled=true` → SSH 로 `ps -ef|grep GSS` 실행
3. 프로세스 라인에서 `/...GSS...` 토큰을 추출해 GSS 디렉토리 추정
4. 추정 디렉토리(또는 `gss_log_dir_hint`)에서 로그 파일 개수/크기 산출 (원격은 dry-run 강제)

### (2) Unix DB 원격 점검 — `inspect-remote.ps1`
Windows 노트북 → SSH → Unix DB(또는 GIS) → snapshot.json/frames.json 수집 → 로컬 summary-remote.html 에 QR 인라인 렌더.

```powershell
# unix_db (기본)
.\inspect-remote.ps1

# gis_unix
.\inspect-remote.ps1 -RemoteKey gis_unix

# bat 더블클릭
inspect-remote.bat            # = inspect-remote.bat unix_db
inspect-remote.bat gis_unix
```

전제: 점검 대상 Unix 서버에 `agent-unix/` 가 이미 설치되어 있고, `config.remotes.<key>.agent_path` 가 그 경로를 가리킬 것. perl 5+ 필수 (frames.json 생성).

### config 스키마 (remotes 섹션)
```jsonc
{
  "remotes": {
    "gis_unix": {
      "enabled": false,
      "host": "10.0.0.20",
      "user": "geonuris",
      "port": 22,
      "auth": "key",                // "key" | "password" | "plink"
      "key":  "C:\\Users\\admin\\.ssh\\id_rsa",
      "password": null,             // auth=password 일 때만
      "plink_path": null,           // auth=plink 또는 ssh.exe 미설치 시 plink.exe 절대경로
      "os": "linux",                // 정보용
      "gss_log_dir_hint": "/opt/GeoNURIS_Spatial_Server/logs"
    },
    "unix_db": {
      "enabled": false,
      "host": "10.0.0.21",
      "user": "oracle",
      "auth": "key",
      "key":  "C:\\Users\\admin\\.ssh\\id_rsa",
      "os": "aix",
      "agent_path":  "/home/oracle/agent-unix",
      "config_name": "site.dyg.json"
    }
  }
}
```

### SSH 클라이언트 우선순위
- **OpenSSH** (Windows 10 1809+ / Server 2019+ 기본 탑재) — `ssh.exe` / `scp.exe`. 키 기반 인증 권장.
- **PuTTY** fallback — `plink.exe` / `pscp.exe`. `.ppk` 키 또는 `-pw` 비밀번호 지원.

방화벽 정책 상 22/tcp 가 막혀있으면 `port` 를 변경해 점프 호스트를 이용할 것. 비밀번호 평문 저장은 PoC 한정 — 운영에서는 키 + agent-config 분리 필수.

### 1회 SSH 설정 체크리스트
1. Windows 노트북에서 `ssh -V` 확인 (없으면 PuTTY 설치)
2. 키페어 생성: `ssh-keygen -t rsa -b 4096 -f $env:USERPROFILE\.ssh\id_rsa`
3. 공개키 원격 서버에 등록: `~oracle/.ssh/authorized_keys` 에 append
4. 1회 수동 접속으로 known_hosts 등록: `ssh oracle@10.0.0.21`
5. `config\site.dyg.json` 의 `remotes.unix_db.enabled = true` 설정
6. `inspect-remote.bat` 더블클릭으로 확인

## Telnet 환경 점검 (v0.2.0+)

옛 AIX/Solaris/HP-UX 일부 사이트는 SSH 미설치 — Telnet 만 허용. v0.2.0 부터 Telnet 매개 점검 지원.

### 사용
`setup.bat` 의 "접속 프로토콜" 입력에서 `telnet` 선택. 포트 기본 23.
이후 `inspect.bat` / `inspect-remote.bat` 가 자동으로 Telnet 라우팅.

### 동작
- `lib\Telnet.ps1` — `System.Net.Sockets.TcpClient` 로 Telnet protocol 직접 구현 (plink -telnet 의 -pw 미지원 우회)
- IAC negotiation 자동 처리, login/password 자동 입력
- 명령 끝에 sentinel 토큰 (`__INSPECT_END_N_$?`) 으로 exit code 추출
- 파일 회수: SCP 대신 `cat 'path'` stdout 으로 받아 로컬 저장

### 보안 경고
- ⚠ Telnet 은 패스워드 평문 전송 — SSH 전환 가능 사이트는 SSH 우선.
- `summary-remote.html` 에 노란색 배너 자동 표시.

## GSS 경로 자동 탐지 (v0.2.0+)

GSS 가 Windows AP 가 아닌 UNIX DB 서버에 설치된 사이트 (사이트마다 경로 다름) 자동 탐지.

탐지 순서:
1. 로컬 Windows: `Test-Path C:\GeoNURIS_Spatial_Server\logs`
2. 미존재 + remotes.unix_db 활성 → 원격 SSH/Telnet 매개:
   - 1차: `ps -ef | grep GSS` → GSS/Spatial_Server/GeoNURIS 키워드 경로 추출
   - 2차 (1차 빈): `ps -ef | grep java` → `-Dcatalina.home=` Tomcat 경로 추출
3. 발견된 경로 + `/logs` 가 로그 점검 대상

## 다음 단계
- [x] AIX DB 에이전트 (ksh, 동일 컨트랙트)
- [x] QR 렌더링 자체화 (lib\qrcode.min.js 인라인 SVG)
- [x] SSH 매개 원격 점검 (Windows → Unix)
- [x] SW Manager `/api/inspection/qr-batch` 엔드포인트 (2026-05-16 e2e 검증, report.id=5)
- [x] 대화형 setup wizard + DPAPI 암호화 (v0.2.0)
- [x] Telnet 매개 점검 + GSS 자동 탐지 (v0.2.0, FR-2/FR-3)
- [ ] 갤럭시탭 PWA (BarcodeDetector + decode.js 포팅)
- [ ] Telnet 환경 파일 복사 (tftp/ftp fallback — 후속 sprint)
- [ ] agent-unix GSS 자동 점검 (후속 sprint)
