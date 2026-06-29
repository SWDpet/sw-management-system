# RELIABILITY.md — 안정성·세션·복구 정책

> ✅ **2026-06-19 코드 대조 검증 (S2 문서 drift 정정)** — §3 DB 좌표를 현행 내부망 정책으로 갱신.
> 근거: `server-restart.sh` + `application.properties` + `SecurityLoginProperties` + JPA 설정 + `docs/DB_CONNECTION.md`

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

- 프로덕션: `SW_Dept` — **사무실 내부망에서만** 접속 (`${DB_HOST}:${DB_PORT}/${DB_NAME}`, 실제 좌표는 1Password/운영팀)
  - ⚠ **2026-06-09 정책 변경**: 기존 외부 공인 IP(`211.104.137.55:5881`) 경로는 보안 취약으로 **차단·폐지**. 집/출장지는 DB 직접 접속 불가 → 코드 작업 + GitHub pull 만. 상세: [`docs/DB_CONNECTION.md`](DB_CONNECTION.md)
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

## 8. CI 게이트 (GitHub Actions) — beyond-A `ci-fresh-init-gate-v1`

`.github/workflows/ci.yml` 이 push/PR(master) 마다 자동 실행:

- **`gates` job**: `./mvnw -B -ntp clean verify` — 단위테스트 + 전 게이트(JaCoCo floor 0.78/0.64·거대클래스 ratchet·ArchUnit·Map부채·골든·Enum sync)를 **CI 에서 강제**. DB 통합테스트(`RUN_DB_TESTS` 게이트)는 skip, GeoNURIS jar 는 checkout 포함, `DB_PASSWORD=ci` 는 컨텍스트 lazy 부팅용(실 DB 미연결).
- **`fresh-init-smoke` job**: `postgres:16` service container 의 빈 DB 에 부트스트랩 DDL `db_init_phase1 → phase1_sigungu → phase2`(`psql -f`, `ON_ERROR_STOP=1`) 적용 + 핵심 테이블(sigungu_code/users/sw_pjt) 존재 sanity. **빈DB→부트스트랩 replay 멱등성 검증**.
  - ⚠ **범위 = 부트스트랩(phase1+phase2)만.** V*.sql 전체 replay(봉인된 9건 결함·중복버전, "빈DB→운영동등 단일경로 부재")는 **범위 밖** — 별도 선결과제(차후 Testcontainers/CI 확장).
- DB 통합테스트는 사내망 운영DB(192.168.10.194) 전용이라 CI 미실행(`@EnabledIfEnvironmentVariable(RUN_DB_TESTS)`). 운영DB 검증은 회사 PC 에서 `RUN_DB_TESTS=true` 로 별도 수행.
- **Testcontainers 통합테스트**(`BootstrapSchemaContainerTest`, beyond-A): 운영DB 비접촉 — 신선 `postgres:16`(Testcontainers)에 부트스트랩 스키마(phase1+sigungu+phase2) 적용 후 JPA 리포지토리 CRUD·파생쿼리 검증(fresh-init-smoke 보다 깊음, 시드 279행 적재도 박제). `@Testcontainers(disabledWithoutDocker=true)` → **Docker 미가용(로컬 회사PC/집/출장)은 클래스 skip**(verify 무회귀), **Docker 가용(CI gates job ubuntu)에서만 실행**. ⚠로컬 Docker 부재로 본 테스트 검증은 CI 에서 수행. 신규 deps 추가로 첫 빌드는 온라인 1회 필요(이후 `-o` 캐시).

---

*Last updated: 2026-06-29 · §8 Testcontainers 통합테스트 추가 (beyond-A)*
