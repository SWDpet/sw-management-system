# [기획서] Testcontainers DB 통합테스트 기반 구축 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A track 5 (codex 자문 후순위). 운영DB 없이 신선 postgres 컨테이너로 DB 통합테스트.
- **상태**: ✅ 구현 완료(2026-06-29). `BootstrapSchemaContainerTest`(@DataJpaTest+@ServiceConnection+@Testcontainers disabledWithoutDocker) + Testcontainers deps(spring-boot-testcontainers·postgresql·junit-jupiter, BOM 1.19.3). 로컬(Docker 없음) `mvnw verify` green=테스트 skip(54→55) 무회귀+`-o` 캐시 동작. codex APPROVE-WITH-FIX(disabledWithoutDocker·ddl-auto=none·fail-fast·온라인 resolve·단순엔티티 전부 반영). dual-review: **실제 버그 검출**(Opus) — phase1_sigungu 가 sigungu_code 에 279행 시드라 `count().isZero()` 가 CI서 실패할 것 → baseline 방식으로 수정(시드 적재도 함께 박제) + 스트림 close. ⚠**실제 실행 검증은 CI(Docker)** — push 후 gates job.

---

## 1. 배경 / 목표
현재 DB 통합테스트는 `@EnabledIfEnvironmentVariable(RUN_DB_TESTS)` 게이팅 + `@ActiveProfiles("local")` 로 **운영DB(192.168.10.194) 직결** → CI/사내망밖에서 skip. CI 의 fresh-init-smoke 는 "부트스트랩 DDL 이 빈 DB 에 적용되나"만 검증(JPA 매핑·CRUD 미검증).

**목표**: Testcontainers 로 **신선 postgres:16 + 부트스트랩 스키마**를 띄워 운영DB 없이 DB 통합테스트를 실행하는 기반(base) 구축. 신선 DB 에 JPA 엔티티 매핑·리포지토리 CRUD 가 동작함을 검증(fresh-init-smoke 보다 한 단계 깊음).

**제약(정직)**:
- ⚠ **회사 PC 에 Docker 미설치**(CLI·Desktop 없음) → **로컬 실행/검증 불가**. CI(GitHub Actions ubuntu-latest=Docker 보유)에서만 실행·검증.
- → 본 트랙은 PIT/CI/CSRF 처럼 로컬 green 으로 사전검증 불가. **검증은 push 후 CI gates job(Docker)** 에서. (CI 트랙과 동일 방식.)

## 2. 범위
### D1 — Testcontainers 의존성 (test scope)
- `org.testcontainers:postgresql`, `org.testcontainers:junit-jupiter` (버전은 spring-boot-dependencies BOM 관리 — Spring Boot 3.2.1).

### D2 — Docker-게이팅된 Testcontainers 통합테스트 (`BootstrapSchemaContainerTest`)
- `@DataJpaTest`(JPA 슬라이스 — DbInitRunner/웹 미로딩으로 단순화) + `@AutoConfigureTestDatabase(replace=NONE)` + `@Testcontainers` + `@ServiceConnection`(컨테이너 datasource 자동 와이어, Spring Boot 3.1+).
- **Docker-게이팅**: 클래스 레벨 `@EnabledIf("isDockerAvailable")`(= `DockerClientFactory.instance().isDockerAvailable()`) → **Docker 없으면 클래스 전체 skip**(컨테이너 start 도 안 함, 로컬 `mvnw verify` 무회귀). Docker 있으면 실행.
- **스키마 적용**: `@BeforeAll` 에서 컨테이너 JDBC 로 `db_init_phase1 → phase1_sigungu → phase2` 를 `Statement.execute(전체파일)` 로 적용(psql -f 와 동등 — pg JDBC 는 멀티스테이트먼트+`$$` DO블록 서버측 처리. Spring `@Sql`/ScriptUtils 는 `$$` 분리 결함이라 미사용).
- **검증**: 리포지토리 CRUD(예: 단순 엔티티 save→find 라운드트립 + `count()`)로 신선 스키마+JPA 매핑 동작 박제.

### D3 — 문서
- `docs/RELIABILITY.md` §8(CI) 에 Testcontainers 통합테스트 항목 추가(Docker 게이팅·CI 전용 실행 명시).

## 3. 요건
- **FR-1**: Docker 가용 환경(CI)에서 신선 postgres 에 부트스트랩 스키마 적용 + 리포지토리 CRUD 통과.
- **FR-2**: Docker 미가용(로컬 회사PC/집/출장)에서 클래스 skip → `mvnw -o clean verify` green 유지(무회귀).
- **NFR**: JaCoCo floor 유지(테스트가 커버 추가 → 하락 없음), CI gates job green(Docker 로 실행), codex + dual-review. **검증은 CI push 후**(로컬 Docker 부재).

## 4. 영향 / 리스크
- 변경: pom test 의존성 2개 + 신규 테스트 1개 + 문서. production 코드 0.
- **R1 (高) 로컬 Docker 부재 = CI-only 검증**: 첫 실행 결함은 push 후 CI 에서만 발견(iteration 느림). 완화 = 표준 Testcontainers 패턴·Docker 게이팅으로 로컬 무회귀 보장.
- **R2 무회귀**: Docker-게이팅이 정확해야 로컬 verify 가 안 깨짐(@EnabledIf 가 컨테이너 start 전에 평가 → skip). 핵심 검증 포인트.
- **R3 CI 시간**: postgres:16 이미지 pull + 컨테이너 + 스키마 ≈ +30~60s(gates job). 허용.
- **R4 스키마 멀티스테이트먼트**: `$$` DO블록 → `@Sql` 대신 JDBC `execute(전체파일)`(fresh-init-smoke 가 psql -f 로 입증한 동일 SQL).
