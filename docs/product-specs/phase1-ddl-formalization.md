# [기획서] phase1 DDL 정식화

- **작성팀**: 기획팀
- **작성일**: 2026-04-27
- **선행**: 감사 후속조치 스프린트 2a (`b93e8bc`, FR-2-2-D 에서 명시한 후속 작업)
- **상태**: v3 — codex 검토 6조건 반영 완료 (사용자 최종승인 대기)
- **개정 이력**:
  - v1 (2026-04-27): 초안 작성. phase1 20테이블 + 마스터 시드.
  - v2 (2026-04-27): DB팀 자문 반영. (a) `tb_performance_summary` 제외 → 19테이블, (b) `pg_dump --schema-only` 1회 추출 단계 추가 (FR-0), (c) FK 인라인 정의 확정, (d) `sigungu_code` 별도 파일 분리, (e) 인덱스는 pg_dump 결과 기준.
  - v3 (2026-04-27): codex 검토 6조건 반영. (1) pg_dump 플래그 4종 추가, (2) 읽기전용 롤 + DSN 분리 + `default_transaction_read_only=on` 의무화, (3) 19테이블 정식 명단 + 생성 순서 정의 (인라인 FK 유지 + 부모→자식 순서 명문화), (4) PostgreSQL 버전 명시 + 멱등성 구현 세부 확정, (5) sigungu PK·충돌정책 명시, (6) exec-plan 에 ephemeral 검증 + diff + 롤백 절차 요구.

---

## 1. 배경 / 목표

### 배경
감사 2026-04-18 P2 2-2 조치(스프린트 2a) 에서 `db_init_phase2.sql` 헤더에 phase1 전제 테이블 21개를 명시했으나, **phase1 DDL 자체는 부재**. 현 시점 신규 환경 초기화는 다음 중 하나에 의존:
1. 운영DB 의 수동 백업·복원 (문서화 안 됨)
2. JPA `ddl-auto` 임시 변경 (현재 `none`)
3. 기억과 추측

→ 신규 PC/CI/테스트 환경 셋업 시 진실의 출처(SoT) 가 운영DB 인스턴스 단 하나. 표류 발생 시 추적 불가.

### 목표
1. **phase1 의 정식 DDL 파일 신설** (`db_init_phase1.sql`) — **19개 테이블** (`pjt_equip` 제외 — V025 DROPPED, `tb_performance_summary` 제외 — V100 SoT).
2. **운영DB 무영향 (쓰기 0)** — 본 스프린트는 디스크 파일 + Docker 검증 + 1회 읽기전용 schema dump 만, 운영DB 1바이트도 안 변함.
3. **라이선스 모듈 제외** — `license_registry`, `license_upload_history`, `qr_license` 영구 보류 (재개 시까지).
4. **phase2.sql 헤더 정정** — `pjt_equip` + `tb_performance_summary` 제거.
5. **운영DB schema 스냅샷 1회 확보** (`pg_dump --schema-only`) — phase1.sql 의 정확성 보장 + 향후 별도 스프린트("ddl-auto=validate 전환")의 기준선.

### 비목표
- JPA `ddl-auto` 정책 변경 — **본 스프린트 범위 밖** (별도 스프린트로 분리)
- Flyway/Liquibase 도입 — 본 스프린트 범위 밖
- 운영DB 와 phase1 DDL 의 컬럼 단위 정합 검증 — 별도 스프린트로 분리
- 라이선스 모듈 DDL — 영구 보류

---

## 2. 사용자 시나리오

### 2-1. 신규 개발자 PC 셋업
1. git clone 후 Docker postgres 컨테이너 기동
2. `psql -f db_init_phase1.sql` → 20 테이블 + 마스터 시드 생성
3. `psql -f db_init_phase2.sql` → 9 테이블 추가 (계약 참여자, 문서관리, 공정마스터 등)
4. `psql -f swdept/sql/V*.sql` 순차 실행 → 견적/inspect/work_plan 등 누적 마이그레이션 적용
5. 서버 기동 → 모든 모듈 정상 (라이선스 제외)

### 2-2. 재해 복구 / DR
- 운영DB 손실 시 phase1.sql + phase2.sql + V*.sql 만으로 스키마 완전 복원 가능 (데이터는 백업에서)

### 2-3. 운영자 (영향 0)
- 운영DB 는 본 스프린트 결과물에 노출되지 않음. 기존 사용자 경험 동일.

---

## 3. 기능 요건 (FR)

### FR-0. 운영DB schema 스냅샷 1회 추출 (읽기전용 강제)

| ID | 내용 |
|----|------|
| FR-0-A | **읽기전용 롤 사전 신설** (운영·테스트 DB 공유 → 우발적 쓰기 봉인): `CREATE ROLE ro_phase1_audit LOGIN PASSWORD '...';` `GRANT CONNECT ON DATABASE SW_Dept TO ro_phase1_audit;` `GRANT USAGE ON SCHEMA public TO ro_phase1_audit;` `GRANT SELECT ON ALL TABLES IN SCHEMA public TO ro_phase1_audit;` `ALTER ROLE ro_phase1_audit SET default_transaction_read_only = on;`. 본 롤은 dump 후 즉시 DROP 또는 `NOLOGIN` 으로 비활성. |
| FR-0-B | **DSN 분리**: 본 롤 전용 DSN 을 `.env` 또는 1Password 별도 키 (`DB_READONLY_AUDIT_URL`) 로 보관. 기존 운영 DSN(`DB_URL`)과 절대 혼용 금지. |
| FR-0-C | **dump 명령어 (확정)**: `pg_dump --schema-only --no-owner --no-privileges --no-comments --no-security-labels --no-tablespaces -h <prod_host> -U ro_phase1_audit -d SW_Dept > docs/references/snapshots/2026-04-27-prod-schema.sql`. 6 플래그 모두 필수. |
| FR-0-D | dump 결과를 git 커밋. 단 헤더의 호스트명·DB명 등 환경 메타는 추출 전 마스킹 또는 sed 후처리. |
| FR-0-E | **PostgreSQL 버전 기록**: dump 추출 시 `psql -c "SELECT version();"` 결과를 `docs/references/snapshots/2026-04-27-prod-schema-meta.md` 에 기록. 본 결과로 phase1.sql 의 멱등성 구현 (FR-1-D) PG 버전 분기 결정. |
| FR-0-F | 실행 주체: **사용자** (운영 DB 자격증명 보유자). Claude 는 명령어와 절차만 안내. |
| FR-0-G | dump 후 검증: dump 파일에 `ALTER`/`UPDATE`/`DELETE`/`INSERT` 가 0건임을 grep 으로 확인 (schema-only 보장). 1건이라도 발견되면 즉시 중단·재추출. |

### FR-1. `db_init_phase1.sql` 신설

| ID | 내용 |
|----|------|
| FR-1-A | 신규 파일 `src/main/resources/db_init_phase1.sql` 생성. 본 파일은 phase2.sql 보다 먼저 실행되는 **기초 DDL** 임을 상단 주석에 명시. 라이선스 모듈 제외 안내, 대상 PG 버전, 멱등성 보장 방식 명시. |
| FR-1-B | **19 테이블 정식 명단 + 생성 순서** (부모→자식, 인라인 FK 보존): 1.`sigungu_code` → 2.`sys_mst` → 3.`prj_types` → 4.`maint_tp_mst` → 5.`cont_stat_mst` → 6.`cont_frm_mst` → 7.`users` → 8.`ps_info` → 9.`sw_pjt` (FK: prj_types, sys_mst, cont_stat_mst, cont_frm_mst, maint_tp_mst, sigungu_code) → 10.`tb_infra_master` (FK: sw_pjt, sys_mst, sigungu_code) → 11.`tb_infra_server` (FK: tb_infra_master) → 12.`tb_infra_software` (FK: tb_infra_master, tb_infra_server) → 13.`tb_infra_link_upis` (FK: tb_infra_master) → 14.`tb_infra_link_api` (FK: tb_infra_master) → 15.`tb_infra_memo` (FK: tb_infra_master, users) → 16.`access_logs` (FK: users) → 17.`tb_pjt_target` (FK: sw_pjt) → 18.`tb_pjt_manpower_plan` (FK: sw_pjt, users) → 19.`tb_pjt_schedule` (FK: sw_pjt). (`pjt_equip` 제외 — V025 DROPPED, `tb_performance_summary` 제외 — V100 SoT) |
| FR-1-C | 컬럼 정의는 **FR-0 의 운영DB schema dump 기준**. JPA 엔티티와 차이가 있는 컬럼은 별도 표(`docs/exec-plans/phase1-ddl-formalization.md` 의 §컬럼 diff)로 기록 → 본 스프린트는 운영DB 기준 반영, 엔티티 차이 정리는 후속 스프린트. |
| FR-1-D | **FK 정의: `CREATE TABLE` 내부 인라인 (DB팀 권장)** + **codex 우려 해소 방식**: 생성 순서를 부모→자식으로 명문화 (FR-1-B). 멱등성: `CREATE TABLE IF NOT EXISTS` 가 테이블 건너뛰면 인라인 FK 도 함께 건너뜀 → 자동 보장. PG 14+ 환경 가정. (codex 권장 "FK 후행 분리" 는 본 1회용 신규 환경 초기화에 과도한 복잡성으로 판단 — 의사결정 §6-7 참조) |
| FR-1-E | 인덱스: **FR-0 의 dump 결과에 포함된 실제 운영DB 인덱스만** phase1.sql 에 반영. 추측 인덱스 추가 안 함. PRIMARY KEY/UNIQUE 외 인덱스는 `CREATE INDEX IF NOT EXISTS` (PG 9.5+) 로 멱등 보장. |
| FR-1-F | 마스터 시드 INSERT … ON CONFLICT DO NOTHING: `sys_mst`(5건: UPIS/KRAS/IPSS/GIS_SW/APIMS), `prj_types`(3건: 유지보수/신규/기타), `cont_stat_mst`(5건), `cont_frm_mst`(3건), `maint_tp_mst`(3건) — 합 ~19행. 각 마스터의 PK 컬럼명·코드값은 dump 결과에서 추출하여 정확히 일치시킴. **운영DB 데이터와 충돌 없음** — `ON CONFLICT (<pk>) DO NOTHING` 보장. |
| FR-1-G | `sigungu_code` 시드는 **별도 파일** `src/main/resources/db_init_phase1_sigungu.sql` 로 분리. PK = `sigungu_cd VARCHAR(N)` (정확한 컬럼명·길이는 dump 결과 기준). 모든 INSERT 는 `ON CONFLICT (sigungu_cd) DO NOTHING`. 로드 순서: phase1.sql (CREATE TABLE 포함) → phase1_sigungu.sql (INSERT 만). 행정동 코드 전수(~수천건). 신규 환경 선택적 로드. |
| FR-1-H | **PostgreSQL 버전 호환성**: 대상 = **PG 14+** (운영DB 실제 버전은 FR-0-E 결과로 최종 확정). 멱등성 구현은 PG 14 기준으로 작성: `CREATE TABLE IF NOT EXISTS` (전 버전), `CREATE INDEX IF NOT EXISTS` (PG 9.5+), `ALTER TABLE … ADD CONSTRAINT IF NOT EXISTS` 미사용 (PG 16+ 만 지원, 인라인 FK 로 회피). |

### FR-2. `db_init_phase2.sql` 헤더 정정

| ID | 내용 |
|----|------|
| FR-2-A | 헤더의 "전제 테이블 목록" 21개에서 **`pjt_equip` 제거** (V025 DROPPED 반영). |
| FR-2-B | 헤더의 "전제 테이블 목록" 에서 **`tb_performance_summary` 제거** (V100 SoT 확정 — DB팀 자문 ① 결과). |
| FR-2-C | "phase1 DDL 정리는 향후 별도 스프린트…" 문구를 "phase1 DDL 은 `db_init_phase1.sql` (19 테이블) 에 정의됨" 으로 갱신. |

### FR-3. 라이선스 모듈 제외 명시

| ID | 내용 |
|----|------|
| FR-3-A | `license_registry`, `license_upload_history`, `qr_license` 는 **본 스프린트 미포함**. phase1.sql 상단 주석에 "라이선스 모듈은 영구 보류 (재개 시 별도 스프린트)" 한 줄 추가. |
| FR-3-B | 라이선스 entity 자체는 그대로 유지. 신규 환경에서 라이선스 엔드포인트 호출 시 테이블 부재로 SQL 오류 발생 — **의도된 동작**. |

### FR-4. Docker ephemeral 컨테이너 검증 (codex 조건 6)

| ID | 내용 |
|----|------|
| FR-4-A | 검증용 docker 명령어 가이드 (실제 docker-compose.yml 은 본 스프린트 범위 밖): `docker run --rm --name phase1-verify -e POSTGRES_PASSWORD=test -d postgres:14`. **운영 DSN 절대 미사용 — 명시적 가드**: 검증 스크립트는 `DB_URL` 환경변수 값 검사하여 prod 호스트 매칭 시 즉시 abort. |
| FR-4-B | 검증 시퀀스 (4단계): (1) ephemeral 컨테이너 기동 → (2) phase1.sql → phase1_sigungu.sql → phase2.sql → V002~V026, V100 순차 실행 후 에러 0 + `\dt` 결과로 모든 entity 테이블 존재 확인 (라이선스 entity 3개는 의도적 누락) → (3) `pg_dump --schema-only` 양측(prod-schema.sql vs ephemeral) 비교 (diff) → (4) 컨테이너 파기 (`docker rm -f phase1-verify`). |
| FR-4-C | **diff 허용 오차**: 라이선스 테이블 3개 (`license_registry`, `license_upload_history`, `qr_license`) 만 prod 에 있고 ephemeral 에 없음 — 의도된 차이. 그 외 차이 발견 시 phase1.sql 수정 후 재검증. |
| FR-4-D | 검증 결과(diff 결과 포함)는 `docs/exec-plans/phase1-ddl-formalization.md` 에 첨부. |
| FR-4-E | **롤백 절차**: ephemeral DB 는 컨테이너 파기로 자동 롤백. phase1.sql 자체 변경의 git revert 도 명시. **운영DB 영향 0 — 롤백할 운영 변경 없음.** |
| FR-4-F | **운영DB 쓰기 미발생** — 검증은 docker 컨테이너 1회 실행으로 종결. (FR-0 의 schema dump 만 운영DB 읽기) |

### FR-5. README / setup-guide 갱신

| ID | 내용 |
|----|------|
| FR-5-A | `docs/references/setup-guide.md` 의 "DB 초기화" 섹션(있으면)에 phase1.sql 실행 단계 추가. 없으면 신설. |
| FR-5-B | 신규 환경 셋업 절차: phase1 → phase2 → V*.sql 순서, 라이선스 제외 안내. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | **운영DB 쓰기 0 (필수)** — 본 스프린트의 어떤 작업도 운영DB 에 SQL `CREATE/ALTER/INSERT/UPDATE/DELETE` 를 실행하지 않음. 검증은 Docker 컨테이너에서만. 운영DB 접근은 FR-0 의 `pg_dump --schema-only` 1회 (읽기전용) 만 허용. |
| NFR-2 | **운영중 모듈 무영향** — 견적서, 사업현황, 서버관리, 라이선스 사용자 경험 변화 0. |
| NFR-3 | phase1.sql 멱등성 — 동일 DB 에 2회 실행해도 에러 없음. `CREATE TABLE IF NOT EXISTS` 가 테이블+인라인 FK+인라인 인덱스 모두 건너뜀, 시드는 `ON CONFLICT DO NOTHING`. |
| NFR-4 | 신규 환경에서 phase1 → phase2 → V*.sql 전체 실행 시간 60초 이내 (Docker postgres 14 기준). |
| NFR-5 | 본 스프린트 결과물(파일) 들이 git 커밋되어 추적성 확보. |
| NFR-6 | FR-0 의 schema dump 가 자격증명·민감정보·환경 메타를 포함하지 않을 것 (`--no-owner --no-privileges --no-comments --no-security-labels --no-tablespaces` 6개 플래그 필수, 호스트명·DB명 등 헤더 메타 마스킹). |
| NFR-7 | **대상 PostgreSQL 버전: PG 14+** 가정 (운영DB 실제 버전은 FR-0-E 결과로 확정). 멱등성 구현은 PG 14 호환 SQL 만 사용. |
| NFR-8 | **운영 DSN 격리**: FR-0 은 읽기전용 롤 전용 DSN(`DB_READONLY_AUDIT_URL`) 사용. FR-4 검증은 ephemeral 컨테이너 전용 DSN 사용. 운영 `DB_URL` 절대 노출 금지. |

---

## 5. DB팀 자문 결과 (2026-04-27 반영)

### ① `tb_performance_summary` 의 SoT 위치 — ✅ 확정
- V100 (work_plan_performance_tables.sql:341) 이 SoT. workplan/performance 기능과 묶인 테이블.
- **조치**: phase1.sql 제외, phase2.sql 헤더에서도 제거 (FR-1-B, FR-2-B 반영).

### ② FK 정의 멱등 보장 — ✅ 인라인 확정
- `CREATE TABLE IF NOT EXISTS` 가 테이블 자체를 건너뛰면 인라인 FK 도 함께 건너뜀 → 멱등성 자동 보장.
- DO 블록 + pg_constraint 검사 패턴은 가독성·디버깅 부담으로 기각.
- **조치**: FR-1-D 인라인 정의로 단순화.

### ③ 마스터 시드 범위 — ✅ 확정
- **포함 (5개 마스터, 총 ~19행)**: `sys_mst`(5건), `prj_types`(3건), `cont_stat_mst`(5건), `cont_frm_mst`(3건), `maint_tp_mst`(3건). 신규 환경 화면 동작 최소 세트.
- **분리 (별도 파일)**: `sigungu_code`(수천 건) → `db_init_phase1_sigungu.sql` 로 분리. 운영DB dump 또는 공공데이터 import 권장.
- **제외**: `users`(첫 관리자는 화면 가입 또는 별도 INSERT).
- **조치**: FR-1-F, FR-1-G 반영.

### ④ 운영DB 컬럼 정합 검증 — ✅ 본 스프린트 범위 확장
- DB팀 의견: 별도 스프린트로 미루면 phase1.sql 이 "엔티티만 보고 작성한 추측" 이 됨. `pg_dump --schema-only` 는 읽기전용으로 운영 영향 0 → 본 스프린트에 포함.
- **조치**: FR-0 신설 (schema dump 1회 추출).

### ⑤ 인덱스 정의 범위 — ✅ 운영DB dump 기준
- DB팀 의견: 자체 추측 인덱스는 위험(불필요 인덱스 → 쓰기 성능 저하). FR-0 의 dump 결과에 포함된 실제 인덱스만 반영.
- **조치**: FR-1-E 에 반영.

### DB팀 종합 의견
**기획서 v2 승인 (FR-0 추가 + 19테이블 + 인라인 FK + 시드 5개 + sigungu 분리 + 인덱스 dump 기준 5개 변경 모두 반영됨).**

---

## 6. 의사결정 / 우려사항

### 6-1. 운영DB 무영향 — ✅ 절대 원칙
- 본 스프린트의 어떤 단계에서도 운영DB 에 SQL 실행하지 않음.
- 검증은 Docker postgres 컨테이너 격리 환경에서만.
- 만약 운영DB 스키마 추출이 필요해지면(자문 ④ 답변에 따라), `pg_dump --schema-only` 만 사용 (읽기 전용).

### 6-2. 라이선스 모듈 영구 보류 — ✅ 확정
- 사용자 지침(2026-04-27): "라이선스 부분은 재개 전까지 그대로 영원히 보류임"
- phase1.sql 에서 명시적 제외, 주석으로 명기.

### 6-3. JPA `ddl-auto` 변경 — ✅ 본 스프린트 범위 밖
- 현행 `none` 유지. validate 전환은 별도 스프린트("스키마 diff 정리 + validate 전환") 에서 4단계로 분리:
  1. 운영DB `pg_dump --schema-only` 추출
  2. 엔티티 코드와 diff
  3. 차이 일괄 정리 (운영DB ALTER 또는 엔티티 수정)
  4. validate 전환

### 6-4. Flyway 도입 — ✅ 본 스프린트 범위 밖
- V*.sql 적용 이력 추적은 향후 별도 스프린트.

### 6-5. `pjt_equip` 처리 — ✅ 제외 확정
- V025 DROPPED. phase1.sql 에 포함하지 않고, phase2.sql 헤더에서도 제거.

### 6-6. 마이그레이션 도구 선택 — ✅ 현행 수동 V*.sql 유지
- 본 스프린트는 phase1.sql 신설만 다룸.

### 6-7. FK 위치: 인라인 vs 후행 — ✅ 인라인 + 생성 순서 명문화 확정
- DB팀 권장: 인라인 (CREATE TABLE 내부) — `IF NOT EXISTS` 가 자동 멱등 보장.
- codex 권장: 후행 분리 (별도 ALTER TABLE 섹션) — 복구·재실행 용이.
- **확정안**: **인라인 유지** + codex 우려(테이블 생성 순서) 는 **FR-1-B 의 명시적 부모→자식 순서**로 해소. 본 스프린트는 신규 환경 1회 초기화용이므로 부분 재실행 시나리오는 빈도 낮음. 인라인이 가독성·유지보수성 더 높음.

### 6-8. 운영 우발적 쓰기 봉인 — ✅ 읽기전용 롤 신설 (codex 조건 2)
- 운영·테스트 DB 공유 환경에서 `pg_dump` 실행자 실수 방지.
- FR-0-A 의 `ro_phase1_audit` 롤 + `default_transaction_read_only=on` + DSN 분리 3단 안전망.
- dump 후 즉시 롤 비활성화로 잔존 위험 0.

---

## 7. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| DB (신규) | `src/main/resources/db_init_phase1.sql` | **19** CREATE TABLE (인라인 FK + 인덱스) + 마스터 시드 5개 (~19행) |
| DB (신규) | `src/main/resources/db_init_phase1_sigungu.sql` | sigungu_code 시드 분리 (수천 행) |
| DB (수정) | `src/main/resources/db_init_phase2.sql` | 헤더 주석 정정 (pjt_equip + tb_performance_summary 제거, phase1.sql 참조 추가) |
| Docs (신규) | `docs/references/snapshots/2026-04-27-prod-schema.sql` | 운영DB schema dump 1회 (FR-0 결과, ro 롤 + 6 플래그) |
| Docs (신규) | `docs/references/snapshots/2026-04-27-prod-schema-meta.md` | PG 버전·dump 시점·실행자 메타 |
| Docs (신규/수정) | `docs/references/setup-guide.md` | 신규 환경 DB 초기화 절차 (phase1 → phase2 → V*.sql) |
| Docs (신규) | `docs/exec-plans/phase1-ddl-formalization.md` | 개발계획 + Docker 검증 결과 |
| Docs (수정) | `docs/PLANS.md` | 본 스프린트 항목 status 갱신 |

**신규 5 파일 + 수정 2 파일 + DB 임시 롤 1개 (dump 후 비활성). 운영DB 쓰기 0 (읽기전용 dump 1회만). Entity/Java 코드 변경 0.**

---

## 8. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| phase1.sql 컬럼 정의가 JPA 엔티티와 미세 차이 → 향후 validate 전환 시 부팅 실패 | 중간 | FR-0 의 운영DB schema dump 가 진실의 출처. 엔티티-DB diff 발견 시 별도 표로 기록 → 후속 스프린트에서 정리. |
| 마스터 시드 INSERT 가 운영DB 데이터와 충돌 | 낮음 | 본 스프린트는 운영DB 미실행. 신규 환경 초기화 시에도 `ON CONFLICT DO NOTHING` 멱등 — 안전. |
| FR-0 의 schema dump 에 자격증명·민감정보 포함 | 중간 | `--no-owner --no-privileges` 옵션 강제 (NFR-6). dump 파일 git 커밋 전 1회 검토. |
| `pg_dump` 실행 시 운영DB 부하 | 낮음 | schema-only 는 시스템 카탈로그만 읽음 → 데이터 행 미접근. 부하 무시할 수준. |
| Docker 검증 시 V*.sql 일부 비멱등으로 에러 | 낮음 | 신규 컨테이너 1회 실행이므로 영향 없음. 에러 발견 시 본 스프린트에서 V*.sql 멱등화 후속 작업으로 분리. |
| 라이선스 entity 가 부팅 시 테이블 부재로 오류? | 낮음 | 라이선스 모듈은 lazy-loaded — 엔드포인트 호출 시점에만 SQL 실행. 부팅 자체는 영향 없음. (`ddl-auto=none` 유지로 부팅 시 schema validation 안 함) |
| 읽기전용 롤 신설 자체가 운영DB 변경 (DDL) | 낮음 | `CREATE ROLE`/`GRANT` 은 시스템 카탈로그 변경이지만 사용자 데이터·테이블 무영향. dump 후 즉시 비활성/DROP 으로 잔존 0. 운영자에 의해 통제된 1회 작업. |
| FR-0 dump 시 운영DB 와 ephemeral 검증 결과 diff 발견 (라이선스 외) | 중간 | FR-4-C 에서 의도된 차이(라이선스 3개) 외 발견 시 phase1.sql 수정 후 재검증. 본 스프린트에서 해결 가능한 수준이면 처리, 큰 차이면 후속 스프린트로 분리. |

---

## 9. 승인 요청

- DB팀 자문 5건 반영 완료 (§5).
- codex 검토 6조건 반영 완료 (개정 이력 v3 참조):
  1. ✅ `pg_dump` 플래그 4종 추가 (FR-0-C, NFR-6)
  2. ✅ 읽기전용 롤 + DSN 분리 + `default_transaction_read_only=on` (FR-0-A/B, §6-8)
  3. ✅ 19테이블 정식 명단 + 부모→자식 생성 순서 (FR-1-B)
  4. ✅ PostgreSQL 14+ 명시 + 멱등성 구현 세부 (FR-1-D/E/H, NFR-7)
  5. ✅ sigungu PK 체계 + 충돌정책 + 로드 순서 (FR-1-G)
  6. ✅ ephemeral 검증 + diff + 롤백 절차 (FR-4 전반)

본 기획서 v3 에 대한 **사용자 최종승인** 을 요청합니다.

승인 후 개발계획 (`docs/exec-plans/phase1-ddl-formalization.md`) 작성 단계로 이행.
