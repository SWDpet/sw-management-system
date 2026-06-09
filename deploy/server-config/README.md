# 운영서버 설정 스냅샷 (HTTPS 리버스 프록시)

운영서버 **DESKTOP-H9IKIOT** 의 실제 동작 설정 파일을 형상관리용으로 복사해 둔 스냅샷입니다.
관련 런북: [`docs/exec-plans/https-reverse-proxy-deploy.md`](../../docs/exec-plans/https-reverse-proxy-deploy.md)

## 구성

| 스냅샷 | 라이브 경로 (운영서버) | 역할 |
|--------|----------------------|------|
| `nginx/nginx.conf`  | `D:\nginx-1.31.1\conf\nginx.conf` | 80→443 리다이렉트, win-acme ACME 챌린지, 443 TLS 종단 → Tomcat(127.0.0.1:8093) 프록시, `X-Forwarded-Proto https` 주입 |
| `tomcat/server.xml` | `C:\tomcat11\conf\server.xml`     | HTTP 커넥터(8093), `RemoteIpValve` 로 `X-Forwarded-*` 인식(`request.isSecure()=true` 보정) |

## ⚠️ 보안 — 커밋 금지 항목

다음은 **민감정보라 형상관리에서 제외**합니다 (`.gitignore` 로 차단):

- `D:\nginx-1.31.1\ssl\swbms.geonuris.net-key.pem` — **TLS 개인키**
- 그 외 `*.pem` / `*.key` / `*.pfx` / `*.jks` 인증서·키
- `C:\tomcat11\conf\tomcat-users.xml` — 관리자 계정/비밀번호

인증서는 win-acme 가 자동 갱신하며, 갱신 산출물은 위 `ssl` 폴더에만 존재합니다.

## 복원 / 적용 방법

스냅샷을 운영서버에 반영할 때:

```powershell
Copy-Item deploy\server-config\nginx\nginx.conf  D:\nginx-1.31.1\conf\nginx.conf  -Force
Copy-Item deploy\server-config\tomcat\server.xml C:\tomcat11\conf\server.xml      -Force

# nginx 설정 검증 후 reload
D:\nginx-1.31.1\nginx.exe -t
D:\nginx-1.31.1\nginx.exe -s reload

# Tomcat 재시작 (서비스명에 맞게 조정)
Restart-Service Tomcat11
```

> 적용 전 `nginx.conf` 의 `ssl_certificate*` 경로와 `proxy_pass` 포트, `server.xml` 의 커넥터 포트가
> 해당 시점 운영 환경과 일치하는지 확인할 것.
