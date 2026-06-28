# [기획서] ci-fresh-init-gate-v1 — GitHub Actions 게이트 자동화 + fresh-init smoke (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A track 2 (codex 자문 1순위 ROI). 로컬/수동 게이트를 CI 에서 자동 강제 + 빈 DB fresh-init 검증.
- **상태**: ✅ 구현 완료(2026-06-28). 신규 `.github/workflows/ci.yml`(gates + fresh-init-smoke) + Infra/InspectPdfV2 테스트 RUN_DB_TESTS 게이트 + RELIABILITY §8. 로컬 `mvnw -o clean verify` green(skip 52→54, floor 0.78/0.64 유지). codex 기획 NEEDS-FIX(실질 4건: ungated @SpringBootTest 게이팅·DB_PASSWORD env·psql-client·concurrency)→전부 반영. 구현검증 PASS. dual-review(테스트 합의5=게이트 의도/컨벤션 일치 → disabledReason 추가로 일관화 / ci.yml 합의5 중 3채택[timeout-minutes·healthcheck -d·PR만 cancel], SHA핀·psql버전핀은 official action/private repo 과함 제외, 분쟁3 refute[ephemeral CI DB 하드코딩 정상]). ⚠**첫 실제 CI 실행은 push 후 GitHub Actions 에서 검증**(로컬은 Actions 미실행). production 0(워크플로/문서/테스트게이트).

---

## 1. 배경 / 목표
프로젝트엔 JaCoCo·거대클래스·ArchUnit·Map부채·골든·Enum·PIT 게이트가 이미 있으나 **로컬/수동 실행에 머물러** 팀 품질보증이 반쪽(codex 자문). GitHub Actions 로 push/PR 마다 게이트를 자동 강제하고, 빈 PostgreSQL 에 **부트스트랩 DDL fresh-init smoke** 를 추가한다(DB 사고 방지).

**CI 차단요인 사전조사(완료):**
- GeoNURIS_License.jar = **git 커밋됨**(.gitignore 예외) + enforcer 파일존재 체크뿐(빌드 의존 아님) → CI checkout 통과.
- `mvnw verify` 는 **DB 독립**(DB 테스트는 `@EnabledIfEnvironmentVariable(RUN_DB_TESTS)` 게이팅 → 미설정 시 skip, DbInitRunner 는 isDbReachable() 선확인 후 skip, 앱 컨텍스트는 DB 불가에도 부팅). 오늘 종일 `mvnw -o clean verify` BUILD SUCCESS = 증거. → **CI 전용 profile 불요.**
- 스키마: Flyway 아님(`ddl-auto=none`), 부트스트랩 SQL `db_init_phase1 → phase1_sigungu → phase2`(멱등 CREATE IF NOT EXISTS, psql 메타명령 없음 → `psql -f` 적용 가능).

## 2. 범위
### Part A — `.github/workflows/ci.yml` (게이트 자동화)
- 트리거: `push`/`pull_request` (master).
- **Job `gates`**: ubuntu-latest → actions/checkout → setup-java(temurin 17, maven cache) → `./mvnw -B -ntp clean verify`.
  - 효과: ~1500 단위테스트 + 바운드 게이트(jacoco floor 0.78/0.64·거대클래스·ArchUnit·Map부채·골든·Enum) 전부 CI 강제. DB 테스트 skip(52), GeoNURIS enforcer 통과.

### Part B — fresh-init smoke (빈 DB 부트스트랩 검증)
- **Job `fresh-init-smoke`**: services `postgres:16`(POSTGRES_DB/USER/PASSWORD) + health check → checkout → `psql -f db_init_phase1.sql -f db_init_phase1_sigungu.sql -f db_init_phase2.sql` 적용 → exit 0 단언 + 핵심 테이블 존재 sanity(예: `\dt` 로 sigungu_code/users/sw_pjt 등 확인 또는 `SELECT count(*) FROM users`).
- ⚠ **범위 = 부트스트랩 DDL replay(phase1+phase2) 멱등성**. V*.sql 전체 replay(봉인된 9건 결함·중복버전)는 **범위 밖**(별도 선결과제, 본 smoke 는 빈DB→부트스트랩 정상 적용만 보증).

### Part C — 문서 반영
- `docs/RELIABILITY.md`(CI/fresh-init), `docs/QUALITY_SCORE.md`·`QUALITY_CHARTER.md`(CI 게이트 자동화) 갱신.

## 3. 요건
- **FR-1**: push/PR 시 gates job 이 `mvnw verify` 전 게이트 강제.
- **FR-2**: fresh-init-smoke job 이 빈 postgres 에 phase1+phase2 적용 성공 + 테이블 sanity 단언.
- **NFR**: 로컬 `mvnw -o clean verify` 무영향(워크플로는 GitHub 측 실행), YAML lint 정합, 구현 후 codex PASS + dual-review → 듀얼푸시. **첫 CI 실행은 push 후 GitHub Actions 에서 검증**(로컬 사전검증=YAML 구조 + 게이트는 이미 로컬 green).

## 4. 영향 / 리스크
- 변경: 신규 `.github/workflows/ci.yml` + 문서. production 코드 0.
- **R1 GitHub Actions 활성화**: 워크플로 push 시 SWDpet·ukjin914 양 저장소에 Actions 자동 동작(사용자 승인 = 본 트랙 선택). private repo Actions 분 소비.
- **R2 CI 첫 실행 미검증**: 로컬에서 Actions 를 못 돌리므로(act 미사용) 첫 실행은 push 후 확인. 위험완화 = `mvnw verify` 로컬 green(게이트)·psql 스크립트 멱등(부트스트랩)·YAML 구조 검수.
- **R3 의존성 다운로드**: CI 첫 run 은 maven deps online 다운로드(로컬은 -o 캐시). 공개 deps + 커밋된 GeoNURIS jar 라 해결 가능.
- **R4 fresh-init smoke 한계**: 부트스트랩만(V*.sql 전체 replay 아님) — 문서에 명시, 과대보증 금지.
