# [검토 초안] DB 보안 강화 — 이동 근무 + 외부망 전제

- **작성팀**: 기획팀 (초안)
- **작성일**: 2026-04-19
- **상태**: **⏸ 보류 (2026-04-19)** — codex 검토 완료, 사용자 결정에 따라 실행 연기.
  재개 트리거 및 잔존 리스크는 [[../generated/audit/dashboard#⏸-보류-결정-—-db-보안-강화-스프린트-4-연기-2026-04-19|감사 대시보드]] 참조.
- **근거**: 감사 2026-04-18 P1 1-1 후속 + 사용자 요구사항 (사무실/집/출장 노트북 3 지점 + 현 외부 공개)

---

## 1. 요구사항 / 제약

| 항목 | 내용 |
|------|------|
| 접속 지점 | 사무실, 집, 출장 노트북 (3 지점 이상, 출장은 공공 WiFi 가능성 높음) |
| DB 현황 | `211.104.137.55:5881` 인터넷 공개. PostgreSQL 15 추정. SSL 미적용 추정. `postgres` superuser 로 앱 접속 추정 |
| 비밀번호 | 환경변수(Windows User) 저장 — 감사 1-1 조치 완료, 회전은 보류 |
| 팀 규모 | 3~4명 |
| 예산 | 저/무료 우선 |
| 앱 스택 | Spring Boot / HikariCP / JPA |
| 추가 | 과거 git history 에 평문 비번 존재 가능성 있음 |

### 요구 특성
- IP whitelist 는 비현실적(주소 매번 변동)
- DB 서버 이전 불가
- 앱 재배포 최소화

---

## 2. 위협 모델

| ID | 위협 | 현실성 | 현 대응 수준 |
|----|------|-------|-------------|
| T1 | 인터넷 포트 스캐너 → 5881 브루트포스 | 매우 높음 | 없음 |
| T2 | 출장 노트북 분실/도난 → env·git 유출 | 중간 | 없음 |
| T3 | 공공 WiFi MITM | 중간 | 없음 (SSL 없음) |
| T4 | 노트북 악성코드가 User env 추출 | 중간 | 없음 |
| T5 | git history 평문 비번 잔존 → 과거 회전 전 유효 | 높음 | 회전 보류 |
| T6 | 앱 계정 `postgres` superuser → 유출 시 blast radius 큼 | 중간 | 없음 |
| T7 | PostgreSQL 자체 브루트포스 완화 기능 없음 | 중간 | 없음 |

---

## 3. 후보 아키텍처 3 종

### A. Tailscale (Zero-config WireGuard)
- 장치당 2분 셋업, Google/Microsoft SSO
- 무료 tier: 3 user × 100 device
- peer-to-peer (대부분 Tailscale 서버 우회)
- 장치 revoke 관리 콘솔에서 즉시

### B. SSH 터널
- 추가 SW 0, 서버에 SSH 접근만 있으면 가능
- 매번 터널 수동 실행, localhost 포트 관리 번거로움

### C. Cloudflare Tunnel + Access
- DB 서버 아웃바운드만 사용, SSO 통합
- PostgreSQL TCP 터널링 설정 복잡
- 감사 로그 풍부

### D. 자체 VPN (OpenVPN / WireGuard self-host)
- 통제력 최대, 운영 부담 최대
- 3~4명 규모엔 과함

**1차 선택**: **A. Tailscale** — 이동 근무·저예산·소규모 조합에 최적.

---

## 4. 제안 아키텍처 (계층적 방어)

```
L1. 네트워크: Tailscale VPN (WireGuard 암호화)
L2. DB 인증: pg_hba Tailscale 대역(100.64.0.0/10) + localhost 만
L3. 권한 최소화: swmanager_app 유저 (SELECT/INSERT/UPDATE/DELETE), postgres 원격 차단
L4. 전송 암호화: PostgreSQL SSL (sslmode=require)
L5. 자격증명: 32자 random, 쿨 회전, 1Password 등 팀 vault 로 전달
L6. 엔드포인트: BitLocker / Device Encryption + Windows Hello
L7. 감사 로그: log_connections=on, log_statement='ddl', log_min_duration_statement=1000
L8. 선택: fail2ban (PostgreSQL 실패 임계)
```

방화벽: `5881` 인터넷 인바운드 완전 차단. `listen_addresses` 도 Tailscale IP + localhost 만 바인드.

---

## 5. 단계적 실행 로드맵

### P0 — 오늘 (30분, 사전 점검)
1. Shodan/Censys 에서 `211.104.137.55` 노출 이력 확인
2. `pg_stat_activity` 로 이상 접속자 조사
3. PostgreSQL 실패 인증 로그 최근 100건 확인

### P1 — 이번 주 (2~3시간)
4. Tailscale 가입 + 노트북 3대 + DB 서버 설치
5. `postgres` 비번 회전 (강력), `swmanager_app` 유저 생성 + 최소 권한 GRANT
6. `pg_hba.conf` 를 Tailscale 대역 + localhost 로만 허용
7. 방화벽 5881 인터넷 차단, `listen_addresses` 제한
8. 앱 `application.properties` URL → Tailscale IP, username → swmanager_app

### P2 — 2주 이내
9. PostgreSQL SSL on, 앱 URL 에 `sslmode=require`
10. DB 감사 로그 활성
11. 노트북 풀 디스크 암호화 (BitLocker)
12. git history 평문 비번 잔존 검토 (회전 완료 후 과거 유효성 무효화)

### P3 — 운영 안정화
13. fail2ban 도입 검토
14. 분기 1회 비번 로테이션 정책
15. 장치 revoke 체크리스트 문서화

---

## 6. 잔존 리스크 / 수용 가능성

| 리스크 | 수용 사유 |
|--------|-----------|
| 노트북 분실 → revoke 전까지 접근 가능 | 빠른 revoke 절차로 완화 |
| 내부자 악의 | 감사 로그로 사후 대응 |
| Tailscale 자체 장애 | SLA 99.99%, 자체 운영 대비 낮은 리스크 |
| PostgreSQL / WireGuard 0-day | 패치 주기 관리 |

---

## 7. 검토 요청 질문 (codex 답변 요망)

1. **위협 모델 누락**: T1~T7 외 놓친 경로가 있는가? (예: 클라이언트 측 연결 풀 로그, JDBC driver ssl default, 백업 파일 노출 등)
2. **Tailscale 적정성**: 3~4명 규모 + 외부 이동 근무 조건에서 Tailscale 이 실제로 최적인가? 간과한 단점은?
3. **pg_hba 대역**: `100.64.0.0/10` 을 바로 허용하는 것이 안전한가? 더 좁혀야 하는가 (예: `100.x.y.z/32` per device)? 운영 부담 vs 안전성 트레이드오프?
4. **권한 분리**: `swmanager_app` GRANT 범위가 충분/과다한가? 스키마·시퀀스·함수 실행 권한에서 놓친 부분이 있는가?
5. **SSL 정책**: `sslmode=require` 로 충분한지, `verify-full` 로 가야 하는지? 인증서 관리 부담은?
6. **로드맵 우선순위**: P0~P3 순서와 기간이 현실적인가? 어떤 단계를 병렬화하거나 뒤로 미뤄도 되는가?
7. **대안 조합**: Tailscale 대신 SSH 터널 + 비번 강화만으로도 충분한 시나리오가 있는가? (팀원이 VPN 도입에 저항 시 대안)
8. **앱 측 보강**: `application.properties` / HikariCP 설정 (connection timeout, ssl params) 에 추가해야 할 항목이 있는가?
9. **운영 리스크**: 이 변경 이후 앱/배포 파이프라인에서 회귀 가능 지점은?
10. **추천 순위**: 당장 실행해야 할 TOP 3 와, 시간 없을 때 **최소한 해야 할 1개**는?

---

본 초안에 대한 codex 의 기술적 타당성 검토와 추천안(위 10개 질문 중심) 을 요청합니다.
