---
tags: [runbook, deploy, https, infra, nginx]
sprint: "https-reverse-proxy"
status: draft
created: "2026-06-02"
---

# [운영 런북] 전체 HTTPS 전환 — iptime DDNS + nginx + Let's Encrypt

- **작성**: 2026-06-02 (회사 PC / ASUS TUF A16)
- **상태**: draft v0.1 — codex 검토·사용자 승인 전
- **성격**: 코드 변경 최소(properties 1~2줄) + **인프라(프록시·인증서) 중심 런북**
- **관련**: [[project_swmanager_deploy]] (WAR 운영 배포), [[inspection-qr-batch]] (QR), [[machines]]

---

## 0. 목적 / 배경

- **전 페이지 HTTPS 전환.** QR 스캐너 PWA가 카메라(`getUserMedia`/`BarcodeDetector`)를 쓰는데 브라우저는 **secure context(HTTPS)** 에서만 허용 → 갤럭시탭 점검 흐름의 전제.
- 전체 HTTPS = QR HTTPS의 **상위집합**. 프록시로 앱 전체를 443에 올리면 QR 카메라는 자동 충족.
- ⚠ 운영은 **외부 Tomcat 11 + ROOT.war**(Windows) → Spring `server.ssl.*` 안 먹음 → TLS는 **nginx(리버스 프록시)에서 종단**.

## 1. 확정 구성 (사용자 결정 2026-06-02)

- **도메인**: iptime 공유기 **무료 DDNS** `xxxx.iptime.org`
- **프록시**: **nginx** (TLS 종단) → Tomcat `127.0.0.1:8080`
- **인증서**: **Let's Encrypt** (`certbot`, 90일 자동갱신)

```
[외부 갤탭/PC] ──https://xxxx.iptime.org──▶ [iptime 공유기 공인IP:80/443]
   ──포트포워딩──▶ [사내서버:80/443  nginx TLS종단] ──http──▶ [Tomcat 127.0.0.1:8080 (ROOT.war)]
```

## 2. 사전 점검 (현황 — 코드 측은 거의 준비됨)

- `application-prod.properties`: `server.servlet.session.cookie.secure=true` **이미 설정됨**.
- mixed-content: 템플릿/정적자원 절대 `http://` 실로딩 **0건**(검출된 건 전부 `xmlns:th` 네임스페이스 식별자 — 무해). 2026-06-02 확인.
- 외부 톰캣 배포 절차는 [[project_swmanager_deploy]] 그대로.

## 3. 절차

### Step 1 — iptime DDNS
- iptime 관리자(`192.168.0.1`) → 고급설정 → 특수기능 → **DDNS** → `xxxx.iptime.org` 등록·갱신 확인.

### Step 2 — 포트포워딩
- iptime → NAT/라우터 관리 → 포트포워드:
  - 외부 **80** → 사내서버 IP **80** (LE HTTP-01 챌린지용)
  - 외부 **443** → 사내서버 IP **443**
- (Tomcat 8080은 외부로 열지 않음 — 내부에서 nginx만 접근)

### Step 3 — nginx 설치 + certbot 인증서 발급
- 서버에 nginx 설치. 80에 임시 server 블록(ACME 챌린지 경로) 후:
  ```
  certbot --nginx -d xxxx.iptime.org
  ```
- 발급 성공 시 90일 자동갱신 타이머 등록 확인(`certbot renew --dry-run`).

### Step 4 — nginx 설정 (TLS 종단 + 프록시)
```nginx
server {
  listen 80;
  server_name xxxx.iptime.org;
  location /.well-known/acme-challenge/ { root /var/www/certbot; }  # 갱신용
  location / { return 301 https://$host$request_uri; }              # http→https 강제
}
server {
  listen 443 ssl;
  server_name xxxx.iptime.org;
  ssl_certificate     /etc/letsencrypt/live/xxxx.iptime.org/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/xxxx.iptime.org/privkey.pem;
  add_header Strict-Transport-Security "max-age=31536000" always;   # HSTS

  location / {
    proxy_pass         http://127.0.0.1:8080;
    proxy_set_header   Host $host;
    proxy_set_header   X-Real-IP $remote_addr;
    proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header   X-Forwarded-Proto https;   # ★ 앱이 HTTPS 인지
  }
  # team-monitor SSE 는 운영(prod)에서 제외되므로 별도 buffering 설정 불필요(현행). 필요 시 application.properties 주석 가이드 참조.
}
```

### Step 5 — 애플리케이션 설정 (1줄 추가)
`application-prod.properties`:
```properties
server.forward-headers-strategy=framework   # ★ 프록시 뒤 X-Forwarded-Proto 인식 (없으면 secure쿠키/리다이렉트/isSecure 오동작)
# server.servlet.session.cookie.secure=true  # 이미 설정됨
```
- 이 변경은 ROOT.war 재배포 필요. (프록시/인증서 작업과 분리 가능)

## 4. 검증 체크리스트
- [ ] `https://xxxx.iptime.org` 전 페이지 200, `http://` 접속 시 301 리다이렉트
- [ ] 로그인/세션 유지(secure 쿠키 정상 발급), 무한 리다이렉트 없음
- [ ] 갤럭시탭 외부망에서 QR 스캐너 **카메라 권한 정상**
- [ ] 브라우저 콘솔 mixed-content 경고 0
- [ ] 앱 `request.isSecure()==true` (forward-headers 동작)
- [ ] `certbot renew --dry-run` 성공 (자동갱신 보장)

## 5. 롤백
- **무중단·앱 무변경 롤백**: nginx만 정지하면 Tomcat(8080 http)은 그대로 동작. 포트포워딩 443→8080 임시 우회 또는 내부 접속으로 복귀.
- properties 변경(Step 5)을 되돌릴 땐 ROOT.war 재배포.

## 6. 리스크 / 주의
- ⚠ **`*.iptime.org` 공유 도메인** — Let's Encrypt HTTP-01 발급이 공유 도메인 rate-limit에 걸릴 가능성(될 때가 많으나 보장 아님). 막히면 → DNS-01(iptime DNS 제어 제약) 또는 **본인 도메인 발급**(장기 운영 권장).
- 인터넷 노출 = 공격면 증가 → 후속으로 보안 헤더(HSTS preload·CSP)·접근제한·rate-limit 별도 sprint 권장(본 전환과 분리).
- HTTP-01 갱신은 외부 80 상시 개방 전제 → 포트포워딩 유지 필요.

## 7. 변경 이력
- **2026-06-02 v0.1** (회사 PC) — 초안. iptime DDNS + nginx + LE 확정(사용자). 다음 = codex 검토 → 승인 → 실행.
