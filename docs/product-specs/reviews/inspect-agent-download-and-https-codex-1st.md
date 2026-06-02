# codex 1차 검토 — inspect-agent-download 기획서 + https-reverse-proxy 런북

- 검토일: 2026-06-02 (회사 PC) · codex v0.136.0 (gpt-5.5, read-only)
- 대상: `docs/product-specs/inspect-agent-download.md`, `docs/exec-plans/https-reverse-proxy-deploy.md`

---

## 문서1 — `docs/product-specs/inspect-agent-download.md`

**최종판정 = ⚠수정필요** (기획 방향 타당, 구현 승인 전 보안·권한·패키징 세부 확정 필요)

### FR 판정
- FR-2 / FR-4: **충족**.
- FR-1: 부분충족 — `target/classes/agent/` 주입 Maven 단계(`process-resources` vs `package`), WAR 포함 여부, stale 파일 정리 방식 불명확.
- FR-3: 부분충족 — `ResponseEntity<byte[]>`+`Content-Disposition` 재사용은 타당. `Content-Type: application/zip`, `Content-Length`, 리소스 미존재 시 404/500 분기 명시 필요.
- FR-5: 부분충족 — 버전 표기 요구는 있으나 버전 소스 미확정(§7).
- FR-6: 부분충족 — SHA-256 표기는 좋으나 체크섬만으로 PowerShell 배포물 신뢰 보장 불가.

### NFR 판정
- NFR-2 / NFR-3 / NFR-4: **충족** (NFR-3 — UI 변경이라 디자인팀 자문 트리거 맞음).
- NFR-1: 부분충족 — `/ops-doc/**` 는 SecurityConfig상 인증만 걸림. `authDocument != NONE` 업무 권한 검사는 컨트롤러/템플릿 별도 처리 필요.
- NFR-5: 부분충족 — 결정적 zip 요구하나 timestamp/file order 고정 방법 미기재.

### T 판정
- T-1~T-5, T-7: **충족**. T-6: 부분충족 — 재현성 기준("동일 zip 바이트" vs "압축해제 후 파일 해시") 정의 필요.

### §7 결정 5건 — codex 권고
1. **경로**: `/ops-doc/inspect-agent` 유지 권장.
2. **권한**: 무인증 공개 비권장. 최소 `authDocument != NONE`, 가능하면 운영자/관리자 또는 `EDIT` 제한.
3. **버전 소스**: `agent-windows/manifest.json` 또는 별도 `VERSION` 단일 소스 권장 (pom version 비권장 — 앱/모듈 버전 분리 가능).
4. **zip 범위**: agent-windows 전체 포함하되 `output/`·임시파일·현장 secret/config·로그 **명시 제외**. `config/site.*.json` 민감정보 포함 여부 검토.
5. **플러그인**: `maven-assembly` 권장(descriptor로 include/exclude·reproducible 명시 용이). 단순하면 antrun도 가능.

### 추가 권고
- zip 내부 `manifest.json`(버전·빌드시각·파일목록 해시) 포함 + 페이지 표시.
- PowerShell 배포물 → Authenticode 서명 또는 서버 체크섬 + 배포 담당자 검증 절차 문서화.
- 다운로드 로그(누가·언제·어떤 버전) 남길지 결정.

---

## 문서2 — `docs/exec-plans/https-reverse-proxy-deploy.md`

**최종판정 = ⚠수정필요** (아키텍처 방향 맞음. 전제가 "Windows 외부 Tomcat11"인데 절차가 리눅스 nginx/certbot 기준 → 실행 불확실성)

### 항목별 근거
- iptime DDNS + 포트포워딩: 대체로 충족 (80/443 포워딩, 8080 비공개 원칙 OK).
- nginx + Let's Encrypt: 부분충족 — `certbot --nginx`·`/etc/letsencrypt`·`/var/www/certbot` 는 **리눅스 기준**. Windows 서버에서 ① nginx 직접 ② WSL/Linux VM ③ win-acme 중 **실행환경 확정 필요**.
- 외부 Tomcat WAR: 부분충족 — `forward-headers-strategy=framework` + `X-Forwarded-Proto` 맞음. **`X-Forwarded-Host`, `X-Forwarded-Port 443` 추가 권장**.
- secure 쿠키: 부분충족 — `http://서버:8080` 직접 접근 시 로그인 검증 깨질 수 있음 → 운영 검증은 **반드시 프록시 경유**.
- http→https / HSTS: 부분충족 — 301·HSTS 맞으나 초도 **`max-age=300`** 으로 짧게 시작 후 안정화 뒤 1년 상향. `*.iptime.org` 에서 **preload/includeSubDomains 비권장**.
- 회귀/롤백: 부분충족 — nginx 중지 후 443→8080 우회 가능하나 `secure=true` 상태면 HTTP 세션 쿠키 문제. **롤백 시 properties도 함께 되돌리는 절차** 필요.
- 공유도메인 rate-limit: 충족 — `iptime.org` 공유 위험 크므로 장기엔 본인 도메인이 안전.
- 누락 절차(일부 미충족): **nginx 헬스체크, 인증서 자동갱신 실제 검증, Windows 방화벽/Tomcat 8080 외부차단, `nginx -t` 설정검증**.

### 권고
- **실행환경 먼저 확정**: Linux nginx+certbot / Windows nginx+win-acme / WSL·VM 프록시 중 택1.
- nginx에 `X-Forwarded-Host`·`X-Forwarded-Port`·`proxy_read_timeout`·`proxy_http_version 1.1` 추가 검토.
- Tomcat 8080은 포워딩 미설정만으로 끝내지 말고 Windows 방화벽 차단 또는 connector bind 제한 명시.
- 검증에 nginx 헬스체크(`/actuator/health` 또는 로그인 페이지)·`certbot renew --dry-run`/win-acme dry-run·인증서 만료일 확인 추가.
- HSTS 초도 낮은 max-age, 안정화 후 상향. iptime.org preload 금지.

---

## 종합
- 두 문서 **⚠수정필요** — 반려 아님. 방향 OK, 세부 확정/정정으로 v0.2 가능.
- 문서1: §7 결정 5건 확정 + 권한/패키징/응답헤더/재현성 보강 후 **디자인팀 자문** 진행.
- 문서2: **Windows에서 nginx 실행 방식 확정**이 최우선(런북 절차가 거기서 갈림).
