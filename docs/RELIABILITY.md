# RELIABILITY.md — 안정성·세션·복구 정책

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `server-restart.sh` + `application.properties` + `SecurityLoginProperties` + JPA 설정

---

## 1. 서버 기동

```bash
bash server-restart.sh
```

- 기존 포트(8080) PID 종료 → Maven `spring-boot:run` 재기동
- DB_PASSWORD 자동 로드 (Windows User 환경변수)
- 기동 타임아웃: ~8~15초 (실측)
- 기동 완료 기준: 포트 8080 응답

---

## 2. 세션·로그인 정책

**Spring Boot session**:
- `server.servlet.session.timeout` 기본값 (설정 시 `application.properties`)

**로그인 잠금** (`SecurityLoginProperties`):
- 실패 횟수 임계치 초과 시 일시 계정 잠금
- 근거: `SecurityLoginPropertiesValidationTest` 테스트 PASS

---

## 3. DB 연결

- 프로덕션: `postgresql://211.104.137.55:5881/SW_Dept`
- 비밀번호: `DB_PASSWORD` 환경변수 (credential 비커밋 원칙, `AGENTS.md §5`)
- 연결 풀: Spring Boot 기본 (HikariCP)

---

## 4. JPA DDL 정책

- `spring.jpa.hibernate.ddl-auto=none` (권장) — 스키마 변경은 **V*** 마이그레이션 SQL 로만
- 마이그 이력: `swdept/sql/V017_*.sql` ~ `V026_*.sql`
- DbInitRunner: 서버 기동 시 `src/main/resources/db_init_phase2.sql` 실행 (ON CONFLICT DO NOTHING 기반 멱등)

---

## 5. 복구·롤백 정책

### 5-1. DB 롤백
- 각 V### 마이그레이션과 쌍으로 `V###_rollback.sql` 보관 (S5 이후 관행)
- 백업 테이블: `_backup_<run_id>` (S5/S1/S10 패턴)
- 롤백 기준 SHA: 각 스프린트 개발계획 Step 0 기록

### 5-2. 애플리케이션 롤백
- `git revert <commit>` — 각 스프린트 commit 원자 단위
- Phase 분리 commit 원칙 (docs-renewal-01 참조)

---

## 6. 로그

- `logs/swmanager.log` (일반), `logs/swmanager-error.log` (ERROR)
- `server.log` — 기동 stdout/stderr
- access_logs 테이블 (S5/S9 리팩터 완료) — Orphan Guard + fail-soft label

---

## 7. 장애 대응 체크리스트 (발췌)

자세한 배포 체크리스트: `docs/exec-plans/archive/quotation-deploy.md`

1. 서버 기동 실패 → `server.log` 확인 + `./mvnw clean compile` 재빌드
2. DB 연결 실패 → DB_PASSWORD 환경변수 확인
3. 마이그 실패 → `V###_rollback.sql` 실행 + 백업 테이블 복원
4. 권한 문제 → `AuthLevel` / `CustomUserDetails` 검증

---

*Last updated: 2026-04-24 · docs-renewal-01 P1*
