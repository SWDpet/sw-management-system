# [개발계획] ci-fresh-init-gate-v1

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/beyond-a-ci-fresh-init-gate.md`
- **상태**: ✅ 구현 완료(2026-06-28). ci.yml + 2종 게이트(disabledReason 포함) + RELIABILITY §8. 로컬 verify green. codex PASS·dual-review 채택(timeout/healthcheck/PR-cancel). 첫 CI는 push 후 Actions 검증. 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료) + codex NEEDS-FIX 반영
- GeoNURIS jar 커밋됨(enforcer 통과), Flyway無/ddl-auto=none, phase SQL 순수(psql -f 가능). 핵심테이블 phase1 정의.
- ⚠ **codex 발견(실질)**: RUN_DB_TESTS 게이트 없는 @SpringBootTest 3종.
  - `SwManagerApplicationTests`: contextLoads 만(DB 쿼리 없음). lazy datasource 라 localhost:5432 CLOSED 에도 부팅(로컬 검증). 단 `spring.datasource.password=${DB_PASSWORD}`(기본값 없음) → **gates job 에 DB_PASSWORD env 주입**해야 placeholder 해결.
  - `InfraSpecLoadGangjinTest`·`InspectPdfV2RenderTest`: `@ActiveProfiles("local")`(=DB_URL 기본 192.168.10.194:5880 운영DB) + @Autowired repo 쿼리 → **운영DB 직결**(회사PC만 도달, CI 불가). 로컬은 DB_PASSWORD SET+사내망이라 통과. → **RUN_DB_TESTS 게이트 추가**(프로젝트 관례 일치, plain `mvn test` 의 운영DB 접속 부작용도 해소).
- `./mvnw` git mode=100644 → chmod 선행. setup-java JDK 17(pom <java.version>17).

## 0-1. 선행 production-test 수정 (CI green 전제)
- **F1** `InfraSpecLoadGangjinTest`·`InspectPdfV2RenderTest` 에 `@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS", matches="true")` 추가(클래스 레벨). 다른 DB 테스트와 동일 패턴. import `org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable`.

## 1. `.github/workflows/ci.yml`

```yaml
name: CI
on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]
permissions:
  contents: read
concurrency:                       # codex: 동일 ref 중복 실행 취소(분 절약)
  group: ci-${{ github.ref }}
  cancel-in-progress: true
jobs:
  gates:
    runs-on: ubuntu-latest
    env:
      DB_PASSWORD: ci              # codex: ${DB_PASSWORD} placeholder 해결(컨텍스트 lazy 부팅용, 실연결 안 함)
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17', cache: maven }
      - name: Build + 게이트 (jacoco/거대클래스/ArchUnit/Map부채/골든/Enum)
        run: |
          chmod +x ./mvnw          # mvnw git mode 100644(실행권한 없음) → Linux 실행 위해 부여
          ./mvnw -B -ntp clean verify
        # DB 테스트(RUN_DB_TESTS 게이트)는 미설정 → skip. SwManagerApplicationTests=lazy 부팅.

  fresh-init-smoke:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env: { POSTGRES_DB: sw_dept_ci, POSTGRES_USER: ci, POSTGRES_PASSWORD: ci }
        ports: [ '5432:5432' ]
        options: >-
          --health-cmd "pg_isready -U ci" --health-interval 5s
          --health-timeout 5s --health-retries 10
    steps:
      - uses: actions/checkout@v4
      - name: psql client 보장
        run: sudo apt-get update && sudo apt-get install -y postgresql-client
      - name: 부트스트랩 DDL fresh-init (빈 DB)
        env: { PGPASSWORD: ci }
        run: |
          set -e
          for f in db_init_phase1.sql db_init_phase1_sigungu.sql db_init_phase2.sql; do
            echo "applying $f"
            psql -h localhost -U ci -d sw_dept_ci -v ON_ERROR_STOP=1 \
              -f "src/main/resources/$f"
          done
      - name: 핵심 테이블 sanity
        env: { PGPASSWORD: ci }
        run: |
          psql -h localhost -U ci -d sw_dept_ci -v ON_ERROR_STOP=1 -c \
            "SELECT count(*) FROM sigungu_code; SELECT count(*) FROM users; SELECT count(*) FROM sw_pjt;"
```

- `ON_ERROR_STOP=1`: SQL 오류 시 즉시 실패(부트스트랩 결함 검출).
- sanity: 테이블 존재 확인(없으면 psql 오류 → job fail). count 값 자체는 비단언(seed 유무 무관).

## 2. 문서
- `docs/RELIABILITY.md`: CI 게이트 자동화 + fresh-init smoke 항목 추가(범위=부트스트랩, V*.sql 제외 명시).
- `docs/QUALITY_SCORE.md`/`QUALITY_CHARTER.md`: "게이트 CI 강제" 반영.

## 3. 검증 절차
1. **로컬**: YAML 구조 검수(actionlint 있으면 실행, 없으면 수동) + `mvnw -o clean verify` 재확인(게이트 green) + phase SQL 멱등성은 로컬 psql 가능 시 빈 DB 적용 시도(회사PC postgres 있으면).
2. **CI**: 듀얼푸시 후 GitHub Actions 에서 gates·fresh-init-smoke 양 job green 확인(첫 실행은 push 후).
3. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 4. 리스크/완화
- **R1 테이블명 — 확정**: sigungu_code(phase1 L30)·users(L123)·sw_pjt(L240) 전부 phase1 정의 → sanity 쿼리 유효.
- **R2 첫 CI 미검증**: push 후 Actions 결과 확인·실패 시 후속 수정 커밋. YAML/스크립트는 표준 패턴(setup-java·postgres service)이라 위험 낮음.
- **R3 mvnw 실행권한 — 확정/해소**: mvnw git mode=100644(실행권한 없음) → gates job 에 `chmod +x ./mvnw` 선행 step 포함.
- production 회귀 0(워크플로/문서만).
