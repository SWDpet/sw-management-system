---
tags: [runbook, deploy, https, infra, nginx, windows]
sprint: "https-reverse-proxy"
status: approved
created: "2026-06-02"
updated: "2026-06-02"
---

# [운영 런북] 전체 HTTPS 전환 — iptime DDNS + Windows nginx + win-acme(Let's Encrypt)

- **작성**: 2026-06-02 (회사 PC) · **v0.4.1 ✅ 승인** — codex 5차 승인 + 사용자 최종승인(2026-06-02). 다음 = 인프라 실행(iptime DDNS·nginx·win-acme — 사용자 직접 진행).
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
- ACME 챌린지용 webroot 준비: `C:\nginx\html\.well-known\acme-challenge\` (Step 4 발급에서 사용).

### Step 4 — nginx **80 챌린지 서버블록 먼저** 적용 (★ codex 2차 #1 순서 정정 — 발급 전 80 응답 필수)
> http-01 챌린지는 `http://xxxx.iptime.org/.well-known/acme-challenge/...` 가 **80번 nginx에서 응답**해야 발급됨. 443 인증서가 아직 없으므로 **80 블록만** 먼저 올린다.
`C:\nginx\conf\nginx.conf` (80 블록만, 경로는 `/` 사용):
```nginx
server {
  listen 80;
  server_name xxxx.iptime.org;
  location /.well-known/acme-challenge/ { root C:/nginx/html; }   # win-acme 챌린지 (redirect보다 우선 매칭)
  location / { return 301 https://$host$request_uri; }            # http→https (발급 후에도 유지)
}
```
- **`C:\nginx\nginx.exe -t`** → 서비스 start. 외부망에서 `http://xxxx.iptime.org/.well-known/acme-challenge/ping` 류로 80 도달(404라도 nginx 응답) 확인 후 다음 단계.

### Step 5 — win-acme 로 인증서 발급 (이제 80 응답하므로 http-01 성공)
- `C:\tools\win-acme\wacs.exe` 실행. **검증 = filesystem(webroot) http-01**:
  - 대상 도메인 `xxxx.iptime.org`, webroot = `C:\nginx\html` (win-acme 가 `.well-known/acme-challenge`에 챌린지 파일 기록 → Step 4 의 80 nginx 가 응답).
- **PEM 내보내기**: `--store pemfiles --pemfilespath C:\nginx\ssl` (nginx는 PEM 사용).
- ⚠ **발급 후 실제 PEM 파일명 확인** — win-acme 버전·도메인에 따라 `xxxx.iptime.org-chain.pem`/`-key.pem` 과 다를 수 있음 → `C:\nginx\ssl` 실제 파일명을 Step 6 `ssl_certificate*` 경로에 반영.
- **갱신 후 nginx reload**: win-acme post-renewal 스크립트로 `C:\nginx\nginx.exe -s reload` 등록.
- win-acme 가 **자동갱신 예약작업(Task Scheduler)** 생성 (갱신 검증은 §4).

### Step 6 — nginx **443 TLS 블록 추가** + reload
Step 4 의 80 블록 아래에 443 server 블록을 추가 (`C:\nginx\conf\nginx.conf`):
```nginx
server {
  listen 443 ssl;
  server_name xxxx.iptime.org;
  ssl_certificate     C:/nginx/ssl/xxxx.iptime.org-chain.pem;   # ★ Step 5 실제 파일명으로
  ssl_certificate_key C:/nginx/ssl/xxxx.iptime.org-key.pem;     # ★ Step 5 실제 파일명으로
  ssl_protocols       TLSv1.2 TLSv1.3;                          # 최소 하드닝 (cipher/OCSP 등은 보안 sprint)

  client_max_body_size 50m;   # ★ nginx 기본 1MB → 문서/첨부 업로드 413 회귀 방지 (앱 업로드 한도에 맞춰 조정)

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
- 적용 전 **`C:\nginx\nginx.exe -t`** → `nginx -s reload`.

### Step 7 — 애플리케이션 설정 (ROOT.war 재배포)
`application-prod.properties` — ⚠ **`.properties` 는 값 뒤 inline 주석 미지원**(codex 2차 #2: `key=framework   # ...` 는 값 전체가 `framework   # ...` 로 파싱됨). 주석은 **반드시 별도 줄**로:
```properties
# ★ 프록시 뒤 X-Forwarded-* 인식 (없으면 secure쿠키/리다이렉트/isSecure 오동작)
server.forward-headers-strategy=framework
# server.servlet.session.cookie.secure=true  (이미 설정됨)
```

## 4. 검증 체크리스트
- [ ] `nginx -t` 통과, 서비스 자동시작 동작
- [ ] `https://xxxx.iptime.org` 전 페이지 200, `http://` 접속 시 301
- [ ] **로그인/세션은 반드시 프록시(443) 경유로 검증** — `http://서버:8080` 직접 접근은 `secure=true` 라 로그인 깨질 수 있음(정상, 동작 설명일 뿐)
- [ ] **8080 외부차단 검증(보안)** (codex 2차 #3) — 외부망에서 `http://<공인IP 또는 도메인>:8080` 접속이 **연결거부/타임아웃으로 실패**하는지 확인. (위 secure 쿠키 항목과 별개의 차단 검증)
- [ ] 무한 리다이렉트 없음, `request.isSecure()==true` (forward-headers 동작)
- [ ] **업로드 회귀** — `client_max_body_size` 적용 후 앱 최대 크기 파일 업로드 정상(413 없음)
- [ ] 갤럭시탭 외부망에서 QR 스캐너 **카메라 권한 정상**
- [ ] 콘솔 mixed-content 경고 0
- [ ] nginx 헬스체크: 로그인 페이지(또는 `/actuator/health`) 200 모니터링
- [ ] **win-acme 발급/갱신 검증** (codex #4 — 명령 확정. win-acme 엔 별도 dry-run 플래그가 없고 **staging 으로 리허설**하는 것이 표준):
  - **발급 플로우 리허설 (Let's Encrypt staging — 운영 rate-limit 무소모)**:
    `wacs.exe --test --notaskscheduler --source manual --host xxxx.iptime.org --validation filesystem --webroot C:\nginx\html --store pemfiles --pemfilespath C:\nginx\ssl-staging`
    (`--notaskscheduler` = staging 리허설이 운영 자동갱신 예약작업을 건드리지 않게 — codex 5차 권고)
    → ⚠ **staging 산출물은 반드시 별도 경로(`C:\nginx\ssl-staging`)** 로 (codex 4차 — 운영 PEM 경로 `C:\nginx\ssl` 에 쓰면 신뢰 안 되는 staging 인증서가 운영본을 덮어 reload 시 장애). 챌린지·webroot·PEM 경로 검증 후 **`C:\nginx\ssl-staging` 폴더째 삭제**. 운영 PEM 경로 `C:\nginx\ssl` 에는 Step 5 production 산출물만 둔다.
  - **자동갱신 스케줄·설정 점검 (무소모)**: `wacs.exe --renew` (force 없이) → 평시 `no renewals due` 출력이면 스케줄/설정 정상.
  - **Task Scheduler 작업**(win-acme renew) 존재·다음 실행시각·**인증서 만료일** 확인.
  - ⚠ `wacs.exe --renew --force` 는 **운영 인증서 강제 재발급**(production rate-limit 소모) → 리허설 용도로 쓰지 말 것.
- [ ] HSTS 안정화 후 `max-age` 31536000 으로 상향(별도 단계)

## 5. 롤백
- **무중단**: nginx 서비스 중지 → Tomcat(8080 http) 그대로. (단 8080 외부차단 했으면 아래 방식별로 외부 복귀 필요)
- ⚠ **8080 외부 복귀는 차단 방식별 분기** (codex 2차 #5):
  - **방화벽 인바운드 차단**으로 막은 경우 → 해당 Defender 규칙 비활성화 + iptime 포트포워딩(외부 8080→서버 8080) 임시 복구.
  - **Tomcat `server.xml` Connector `address="127.0.0.1"`** 로 막은 경우 → `address` 제거(또는 `0.0.0.0`)로 되돌리고 **Tomcat 재기동** + 포트포워딩 복구. (nginx 중지만으론 외부 HTTP 복귀 안 됨)
- ⚠ **properties 동반 롤백** (codex) — `secure=true` + http 환경이면 세션 쿠키 문제 → HTTP 로 완전 복귀 시 `forward-headers-strategy` 제거 + `cookie.secure=false` 로 ROOT.war 재배포.

## 6. 리스크 / 주의
- ⚠ **`*.iptime.org` 공유 도메인** — Let's Encrypt 등록 도메인 rate-limit 공유 위험 큼. 막히거나 장기 운영이면 **본인 도메인** 권장.
- win-acme http-01 갱신은 외부 80 상시 개방 전제 → 포트포워딩·방화벽 80 인바운드 유지.
- ⚠ **TLS 설정 최소형** (codex 2차) — `ssl_protocols TLSv1.2/1.3` 만 지정. cipher suite·OCSP stapling·session ticket 등 본격 하드닝은 별도 보안 sprint. 초기 운영은 이 위험을 감수하는 상태로 명시.
- nginx 기본 `client_max_body_size 1MB` → Step 6 에서 `50m` 로 상향했으나 **앱 실제 업로드 한도와 일치**시켜야 함(과소 시 413, 과대 시 DoS 면적).
- 인터넷 노출 = 공격면 증가 → 보안 헤더(HSTS preload는 자도메인 확보 후)·접근제한·rate-limit 별도 sprint 권장.

## 7. 변경 이력
- **2026-06-02 ✅ codex 승인** (회사 PC) — codex 5차: v0.4.1 승인(차단 이슈 없음, 4차 치명 리스크 해소 확인). 경미 권고 1건 반영 — staging 리허설 명령에 `--notaskscheduler` 추가(운영 예약작업 미간섭). 다음 = 사용자 최종승인 → 인프라 실행.
- **2026-06-02 v0.4.1** (회사 PC) — codex 4차 검토: #4 명령은 구체화 확인. **신규 치명 리스크 1건** 정정 — staging(`--test`) 리허설 출력 경로를 운영 PEM 경로 `C:\nginx\ssl` → **전용 `C:\nginx\ssl-staging`** 로 분리(운영 인증서 덮어쓰기/혼입 방지), 리허설 후 폴더 삭제 명시. 다음 = 사용자 승인.
- **2026-06-02 v0.4** (회사 PC) — codex 3차 검토 반영: 2차 #1·#2·#3·#5 해소 확인됨. 잔여 **#4** 정정 — win-acme 는 별도 dry-run 플래그가 없으므로 **staging(`--test`) 발급 리허설** + `--renew`(force 없는) 스케줄 점검으로 명령 확정, `--renew --force`(운영 강제 재발급) 는 리허설 금지 명시. 다음 = 사용자 승인.
- **2026-06-02 v0.3** (회사 PC) — codex 2차 검토(⚠수정필요) 반영: **#1 절차 순서 정정**(nginx 80 챌린지 서버 먼저 → 발급 → 443 추가, Step 4~6 재배치), **#2 properties inline 주석 제거**(별도 줄), #3 8080 외부차단 검증 체크 추가, #4 win-acme dry-run 명령 구체화(버전 확인·Task Scheduler·만료일), #5 롤백 8080 차단 방식별(방화벽/Connector) 분기. + 추가 리스크: PEM 파일명 확인, `client_max_body_size 50m`(413 회귀), `ssl_protocols` 최소 하드닝. 다음 = codex 3차 또는 사용자 승인.
- **2026-06-02 v0.2** (회사 PC) — codex 1차 반영: 실행환경 **Windows nginx+win-acme** 확정(리눅스 certbot→win-acme), 8080 외부차단·`nginx -t`·헬스체크·갱신 dry-run 추가, nginx에 X-Forwarded-Host/Port·proxy_http_version 1.1·proxy_read_timeout 추가, HSTS 초도 max-age=300·preload 금지, 롤백에 properties 동반 명시, secure 쿠키 프록시경유 검증.
- **2026-06-02 v0.1** (회사 PC) — 초안(리눅스 nginx+certbot 기준).
