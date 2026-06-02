---
tags: [runbook, deploy, https, infra, nginx, windows]
sprint: "https-reverse-proxy"
status: draft
created: "2026-06-02"
updated: "2026-06-02"
---

# [운영 런북] 전체 HTTPS 전환 — iptime DDNS + Windows nginx + win-acme(Let's Encrypt)

- **작성**: 2026-06-02 (회사 PC) · **v0.2** — codex 1차 검토(⚠수정필요) 반영
- **성격**: 코드 변경 최소(properties 1줄) + **인프라(프록시·인증서) 중심 런북**
- **codex 1차 원문**: `docs/product-specs/reviews/inspect-agent-download-and-https-codex-1st.md`
- **관련**: [[project_swmanager_deploy]], [[inspection-qr-batch]], [[machines]]

---

## 0. 목적 / 배경

- **전 페이지 HTTPS 전환.** QR 스캐너 PWA 카메라(`getUserMedia`/`BarcodeDetector`)는 **secure context(HTTPS)** 필수 → 갤럭시탭 점검 흐름 전제.
- 전체 HTTPS = QR HTTPS의 상위집합. 프록시로 앱 전체를 443에 올리면 QR 자동 충족.
- ⚠ 운영 = **Windows + 외부 Tomcat 11 + ROOT.war** → Spring `server.ssl.*` 안 먹음 → TLS는 **nginx에서 종단**.

## 1. 확정 구성 (사용자 결정 2026-06-02)

- **도메인**: iptime 무료 DDNS `xxxx.iptime.org`
- **프록시**: **Windows 네이티브 nginx** (TLS 종단) → Tomcat `127.0.0.1:8080`
- **인증서**: **win-acme (WACS)** 로 Let's Encrypt 발급·자동갱신 (Windows 환경 — certbot 아님)

```
[외부 갤탭/PC] ──https://xxxx.iptime.org──▶ [iptime 공유기 공인IP:80/443]
   ──포트포워딩──▶ [Windows 서버:80/443  nginx TLS종단] ──http──▶ [Tomcat 127.0.0.1:8080 (ROOT.war)]
```

## 2. 사전 점검 (코드 측 거의 준비됨)

- `application-prod.properties`: `server.servlet.session.cookie.secure=true` 이미 설정됨.
- mixed-content: 템플릿/정적자원 절대 `http://` 실로딩 0건(검출된 `xmlns:th` 는 네임스페이스 식별자, 무해). 2026-06-02 확인.
- WAR 배포 절차는 [[project_swmanager_deploy]] 그대로.

## 3. 절차 (Windows 기준)

### Step 1 — iptime DDNS
- iptime 관리자(`192.168.0.1`) → 고급설정 → 특수기능 → **DDNS** → `xxxx.iptime.org` 등록·정상 갱신 확인.

### Step 2 — 포트포워딩 + 8080 차단
- iptime 포트포워드: 외부 **80→서버 80**, 외부 **443→서버 443**. (win-acme http-01 챌린지가 80 사용)
- ⚠ **Tomcat 8080 외부 차단** (codex) — 포워딩 미설정만으로 끝내지 말고:
  - Windows Defender 방화벽 인바운드 규칙으로 8080 외부 차단, **또는** Tomcat `server.xml` Connector `address="127.0.0.1"` 바인드 제한.

### Step 3 — Windows nginx 설치
- nginx for Windows 를 `C:\nginx\` 에 설치. 서비스 등록(`nssm`/`winsw`)으로 부팅 자동시작 권장.
- ACME 챌린지용 webroot 준비: `C:\nginx\html\.well-known\acme-challenge\` (Step 5에서 사용).

### Step 4 — win-acme 로 인증서 발급
- `C:\tools\win-acme\wacs.exe` 실행. **검증 = filesystem(webroot) http-01**:
  - 대상 도메인 `xxxx.iptime.org`, webroot = `C:\nginx\html` (win-acme 가 `.well-known/acme-challenge`에 챌린지 파일 기록 → nginx 80이 응답).
- **PEM 내보내기**: `--store pemfiles --pemfilespath C:\nginx\ssl` (nginx는 PEM 사용).
- **갱신 후 nginx reload**: win-acme 설치 스크립트(post-renewal)로 `C:\nginx\nginx.exe -s reload` 실행하도록 등록.
- win-acme 가 **자동갱신 예약작업(Task Scheduler)** 생성 → `wacs.exe --renew --force` 또는 dry-run 으로 검증(아래 §4).

### Step 5 — nginx 설정 (TLS 종단 + 프록시)
`C:\nginx\conf\nginx.conf` (경로는 `/` 사용):
```nginx
server {
  listen 80;
  server_name xxxx.iptime.org;
  location /.well-known/acme-challenge/ { root C:/nginx/html; }   # win-acme 갱신 챌린지
  location / { return 301 https://$host$request_uri; }            # http→https
}
server {
  listen 443 ssl;
  server_name xxxx.iptime.org;
  ssl_certificate     C:/nginx/ssl/xxxx.iptime.org-chain.pem;
  ssl_certificate_key C:/nginx/ssl/xxxx.iptime.org-key.pem;

  # HSTS — 초도엔 짧게(300s), 안정화 후 31536000 상향. *.iptime.org 이므로 preload/includeSubDomains 금지
  add_header Strict-Transport-Security "max-age=300" always;

  location / {
    proxy_pass         http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_read_timeout 300s;
    proxy_set_header   Host              $host;
    proxy_set_header   X-Real-IP         $remote_addr;
    proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header   X-Forwarded-Proto https;   # ★ 앱이 HTTPS 인지
    proxy_set_header   X-Forwarded-Host  $host;    # codex 권고
    proxy_set_header   X-Forwarded-Port  443;      # codex 권고
  }
}
```
- 적용 전 **`C:\nginx\nginx.exe -t`** 로 설정 검증 → `nginx -s reload`.

### Step 6 — 애플리케이션 설정 (1줄, ROOT.war 재배포)
`application-prod.properties`:
```properties
server.forward-headers-strategy=framework   # ★ 프록시 뒤 X-Forwarded-* 인식 (없으면 secure쿠키/리다이렉트/isSecure 오동작)
# server.servlet.session.cookie.secure=true  # 이미 설정됨
```

## 4. 검증 체크리스트
- [ ] `nginx -t` 통과, 서비스 자동시작 동작
- [ ] `https://xxxx.iptime.org` 전 페이지 200, `http://` 접속 시 301
- [ ] **로그인/세션은 반드시 프록시(443) 경유로 검증** — `http://서버:8080` 직접 접근은 `secure=true` 라 로그인 깨질 수 있음(정상)
- [ ] 무한 리다이렉트 없음, `request.isSecure()==true` (forward-headers 동작)
- [ ] 갤럭시탭 외부망에서 QR 스캐너 **카메라 권한 정상**
- [ ] 콘솔 mixed-content 경고 0
- [ ] nginx 헬스체크: 로그인 페이지(또는 `/actuator/health`) 200 모니터링
- [ ] **win-acme 갱신 dry-run** 성공(`wacs.exe` renewal 검증) + 인증서 만료일 확인
- [ ] HSTS 안정화 후 `max-age` 31536000 으로 상향(별도 단계)

## 5. 롤백
- **무중단**: nginx 서비스 중지 → Tomcat(8080 http) 그대로. (단 8080 외부차단 했으면 내부/임시 포워딩으로 접근)
- ⚠ **properties 동반 롤백** (codex) — `secure=true` + http 환경이면 세션 쿠키 문제 → HTTP 로 완전 복귀 시 `forward-headers-strategy` 제거 + `cookie.secure=false` 로 ROOT.war 재배포.

## 6. 리스크 / 주의
- ⚠ **`*.iptime.org` 공유 도메인** — Let's Encrypt 등록 도메인 rate-limit 공유 위험 큼. 막히거나 장기 운영이면 **본인 도메인** 권장.
- win-acme http-01 갱신은 외부 80 상시 개방 전제 → 포트포워딩·방화벽 80 인바운드 유지.
- 인터넷 노출 = 공격면 증가 → 보안 헤더(HSTS preload는 자도메인 확보 후)·접근제한·rate-limit 별도 sprint 권장.

## 7. 변경 이력
- **2026-06-02 v0.2** (회사 PC) — codex 1차 반영: 실행환경 **Windows nginx+win-acme** 확정(리눅스 certbot→win-acme), 8080 외부차단·`nginx -t`·헬스체크·갱신 dry-run 추가, nginx에 X-Forwarded-Host/Port·proxy_http_version 1.1·proxy_read_timeout 추가, HSTS 초도 max-age=300·preload 금지, 롤백에 properties 동반 명시, secure 쿠키 프록시경유 검증.
- **2026-06-02 v0.1** (회사 PC) — 초안(리눅스 nginx+certbot 기준).
