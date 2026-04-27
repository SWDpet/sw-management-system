# [개발계획서] phase1 DDL 정식화

- **작성팀**: 개발팀
- **작성일**: 2026-04-27
- **기획서**: [docs/product-specs/phase1-ddl-formalization.md](../product-specs/phase1-ddl-formalization.md) (v3 사용자 승인됨)
- **상태**: v2 (사용자 최종승인 대기) — codex 5조건 반영 완료

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 — 기획서 v3 의 FR-0~FR-5, NFR-1~8 모두 반영 |
| v2 | codex 검토 5조건 반영. (1) Step 0 프리플라이트 가드 신설 + 운영 접속 하드스톱, (2) ro 롤 스펙 강화 + 사전·사후 환경 스냅샷 (`pg_settings`/`LC_*`/확장), (3) T0+T12~T18 검증 항목 추가, T9 SLA 조정 (60s → P95 180s/P99 300s), (4) Step 5 시드 충돌 키 명시 + COPY 정책, (5) Step 9 기본 롤백 NOLOGIN+VALID UNTIL → 완전 DROP 분기 |

---

## 1. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| DB (신규) | `src/main/resources/db_init_phase1.sql` | 19 CREATE TABLE IF NOT EXISTS (인라인 FK + 인덱스) + 5 마스터 시드 ~19행 |
| DB (신규) | `src/main/resources/db_init_phase1_sigungu.sql` | sigungu_code INSERT 만 (~수천행) |
| DB (수정) | `src/main/resources/db_init_phase2.sql` | 헤더 주석 정정 (pjt_equip + tb_performance_summary 제거) |
| Refs (신규) | `docs/references/snapshots/2026-04-27-prod-schema.sql` | 운영DB schema dump (사용자 실행 산출물) |
| Refs (신규) | `docs/references/snapshots/2026-04-27-prod-schema-meta.md` | PG 버전·시점·실행자 메타 |
| Refs (수정) | `docs/references/setup-guide.md` | "DB 초기화" 섹션 갱신 (phase1 → phase2 → V*.sql 순서) |
| Docs (신규/이 파일) | `docs/exec-plans/phase1-ddl-formalization.md` | 본 개발계획 + Docker 검증 결과 |
| Docs (수정) | `docs/PLANS.md` | 본 스프린트 status `진행중` → `완료` |
| 운영 DB (임시) | `ro_phase1_audit` 롤 1개 | dump 후 비활성/DROP |

**신규 5 파일 + 수정 3 파일 + 임시 DB 롤 1개. 운영DB 데이터 변경 0. Entity/Java 코드 변경 0.**

---

## 2. 작업 주체 분리

| 단계 | 주체 | 위험 |
|------|------|------|
| **Step 0: 프리플라이트 가드** | **사용자 + Claude 공동** | DSN 화이트리스트·하드스톱·환경 캡처 |
| Step 1: 운영DB 읽기전용 롤 신설 | **사용자** (운영 DBA) | DDL — 시스템 카탈로그만, 사용자 데이터 무영향 |
| Step 2: schema dump + 사전·사후 환경 스냅샷 | **사용자** | 읽기전용 롤로 실행, 운영DB 영향 0 |
| Step 3: dump 결과 검수·git 커밋 (수동 리뷰 후) | Claude (사용자 dump 파일 수령 후) | 파일 작업만, 자동 커밋 금지 |
| Step 4: phase1.sql 작성 | Claude | 파일 작업만, 사용자 리뷰 후 커밋 |
| Step 5: phase1_sigungu.sql 작성 (COPY 우선) | Claude (또는 dump 에서 SQL 추출) | 파일 작업만 |
| Step 6: phase2.sql 헤더 정정 | Claude | 파일 작업만 |
| Step 7: setup-guide.md 갱신 | Claude | 파일 작업만 |
| Step 8: Docker ephemeral 검증 | Claude | ephemeral 컨테이너 + 격리 네트워크, 운영 영향 0 |
| Step 9: 운영DB 읽기전용 롤 NOLOGIN (기본) | **사용자** | 소프트 비활성, 잔존 위험 0 |
| Step 9-A: 완전 DROP (선택, 사용자 추가 확인) | **사용자** | DDL — 시스템 카탈로그만 |
| Step 10: PLANS.md / 감사 보고서 갱신 | Claude | 파일 작업만 |

**불변 원칙**:
- 체크포인트 사이 사용자 재승인 없이 운영 접속 금지
- Claude 작업물(파일)은 모두 사용자 리뷰 후 커밋 (자동 커밋 금지)
- Step 0 가드 통과 못하면 후속 단계 진행 불가

---

## 3. 단계별 상세

### Step 0: 프리플라이트 가드 — 사용자 + Claude 공동 (신규)

**목적**: 후속 단계가 운영DB 에 우발적으로 연결되거나 쓰기를 일으킬 가능성을 사전 봉인.

#### Step 0-A: DSN 화이트리스트 정의 (사용자)

본 스프린트가 접속 허용된 호스트·포트·DB·계정 명시:

```
ALLOWED_PROD_HOST=<prod_host>
ALLOWED_PROD_PORT=<prod_port>
ALLOWED_PROD_DB=SW_Dept
ALLOWED_PROD_USER=ro_phase1_audit         # Step 1 신설 후

ALLOWED_EPHEMERAL_HOST=localhost
ALLOWED_EPHEMERAL_PORT=25880
ALLOWED_EPHEMERAL_DB=verify
ALLOWED_EPHEMERAL_USER=postgres
```

본 4쌍 외 어떤 호스트/포트/DB/계정도 본 스프린트에서 사용 금지.

#### Step 0-B: 운영 접속 하드스톱 가드 함수 (Claude → 모든 스크립트 적용)

```bash
# .team-workspace/phase1-guard.sh — 모든 DB 접속 명령 앞에 source 필수
phase1_guard() {
  local op="$1"            # "dump" | "ephemeral" | "verify"
  local host="$2" port="$3" db="$4" user="$5"

  case "$op" in
    dump)
      # 운영 화이트리스트만 허용
      if [[ "$host" != "$ALLOWED_PROD_HOST" || "$port" != "$ALLOWED_PROD_PORT" \
         || "$db" != "$ALLOWED_PROD_DB" || "$user" != "$ALLOWED_PROD_USER" ]]; then
        echo "HARD STOP: dump 단계는 운영 화이트리스트만 허용. host=$host user=$user" >&2
        echo "$(date -Iseconds) HARD_STOP op=$op host=$host" >> docs/exec-plans/phase1-audit.log
        exit 99
      fi
      ;;
    ephemeral|verify)
      # 운영 호스트·DB명 매칭 시 즉시 abort
      if [[ "$host" == "$ALLOWED_PROD_HOST" || "$db" == "$ALLOWED_PROD_DB" ]]; then
        echo "HARD STOP: 비운영 단계에서 운영 좌표 감지. host=$host db=$db" >&2
        echo "$(date -Iseconds) HARD_STOP op=$op host=$host db=$db" >> docs/exec-plans/phase1-audit.log
        exit 99
      fi
      # ephemeral 화이트리스트만 허용
      if [[ "$host" != "$ALLOWED_EPHEMERAL_HOST" || "$port" != "$ALLOWED_EPHEMERAL_PORT" ]]; then
        echo "HARD STOP: ephemeral 화이트리스트 외 호스트. host=$host:$port" >&2
        exit 99
      fi
      ;;
  esac

  # 통과 시 감사 로그
  echo "$(date -Iseconds) PASS op=$op host=$host db=$db user=$user" >> docs/exec-plans/phase1-audit.log
}
```

#### Step 0-C: 환경 검증 (사용자)

- [ ] `psql --version` 가 PG 14+ 클라이언트인가?
- [ ] `pg_dump --version` 가 PG 14+ 인가?
- [ ] `docker --version` 가 정상 동작?
- [ ] `PGSSLMODE=require` 환경변수 설정 (운영 접속 시 SSL 강제)
- [ ] 현재 사용자 계정이 운영DB 슈퍼유저 권한 보유?
- [ ] `docs/exec-plans/phase1-audit.log` 파일 작성 가능 (git 추적)

#### Step 0-D: 통과 기준

| 항목 | 기준 |
|------|------|
| 화이트리스트 정의 | 환경변수 4쌍 모두 설정됨 |
| 가드 스크립트 | `.team-workspace/phase1-guard.sh` 존재 |
| 환경 검증 | Step 0-C 6개 항목 모두 통과 |
| 감사 로그 초기화 | `docs/exec-plans/phase1-audit.log` 빈 파일 또는 첫 줄 = 본 스프린트 시작 시각 |

**Step 0 미통과 시 Step 1 진행 불가.**

---

### Step 1: 운영DB 읽기전용 롤 신설 — 사용자 작업

**실행자**: 운영DB 슈퍼유저 권한 보유자 (사용자)

```sql
-- 운영DB 에 슈퍼유저로 접속 후
-- (1) 롤 신설 — LOGIN + 임시 비밀번호
CREATE ROLE ro_phase1_audit LOGIN PASSWORD '<강력한_임시_비밀번호>';

-- (2) 최소 권한만 부여 (codex 조건 2 — 불필요 권한 배제)
GRANT CONNECT ON DATABASE "SW_Dept" TO ro_phase1_audit;
GRANT USAGE ON SCHEMA public TO ro_phase1_audit;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO ro_phase1_audit;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO ro_phase1_audit;  -- pg_dump 가 시퀀스 last_value 읽음
-- pg_read_all_settings 는 PG 10+ 기본 롤 (pg_settings 읽기 — 환경 스냅샷용)
GRANT pg_read_all_settings TO ro_phase1_audit;

-- (3) 트랜잭션 읽기전용 강제
ALTER ROLE ro_phase1_audit SET default_transaction_read_only = on;
ALTER ROLE ro_phase1_audit SET search_path = public;  -- search_path 고정

-- (4) 검증
SELECT rolname, rolcanlogin, rolconfig FROM pg_roles WHERE rolname='ro_phase1_audit';
-- 기대 출력: rolcanlogin=t, rolconfig 에
--   default_transaction_read_only=on, search_path=public 포함

-- (5) 권한 음성 검증 — 의도적 UPDATE 시도 → 실패 확인
\c "SW_Dept" ro_phase1_audit
BEGIN;
UPDATE users SET username=username WHERE 1=0;  -- 0행 영향, 그러나 read-only 차단됨
-- 기대 결과: ERROR: cannot execute UPDATE in a read-only transaction
ROLLBACK;
```

**검증 항목**:
- [ ] 롤 존재 확인 (`\du ro_phase1_audit`)
- [ ] `default_transaction_read_only=on` + `search_path=public` 설정 확인
- [ ] 5단계 UPDATE 시도가 의도대로 차단되는지 확인 (T0)
- [ ] 임시 비밀번호 1Password 또는 안전한 곳 임시 보관 (Step 9 후 폐기)

### Step 2: schema dump + 사전·사후 환경 스냅샷 — 사용자 작업

**실행 환경**: 사용자 PC 또는 운영DB 접근 가능한 머신. **반드시 ro DSN 으로 실행** (Step 0 가드 통과).

#### Step 2-A: 사전 환경 스냅샷 (codex 조건 2)

```bash
# 가드 적용
source .team-workspace/phase1-guard.sh
phase1_guard dump "$ALLOWED_PROD_HOST" "$ALLOWED_PROD_PORT" "$ALLOWED_PROD_DB" "$ALLOWED_PROD_USER"

mkdir -p docs/references/snapshots
META=docs/references/snapshots/2026-04-27-prod-schema-meta.md

# (1) PG 버전
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT version();" -c "SHOW server_version;" \
  > /tmp/pg-version.txt

# (2) 핵심 pg_settings (성능·정렬·인코딩에 영향)
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT name, setting, unit FROM pg_settings WHERE name IN
      ('server_version', 'server_encoding', 'lc_collate', 'lc_ctype',
       'TimeZone', 'standard_conforming_strings', 'default_transaction_isolation',
       'shared_preload_libraries')
      ORDER BY name;" \
  > /tmp/pg-settings.txt

# (3) 확장 목록
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT extname, extversion FROM pg_extension ORDER BY extname;" \
  > /tmp/pg-extensions.txt

# (4) 콜레이션 (ICU 차이 추적)
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT collname, collprovider, collversion FROM pg_collation
      WHERE collname NOT IN ('default','C','POSIX') ORDER BY collname LIMIT 20;" \
  > /tmp/pg-collations.txt

# (5) 사전 xact_commit 스냅샷 (T10 — 운영DB 쓰기 0 검증용)
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT datname, xact_commit, xact_rollback FROM pg_stat_database WHERE datname='SW_Dept';" \
  > /tmp/xact-before.txt
```

#### Step 2-B: schema dump 실행

```bash
# dump (운영DB 호스트·포트는 화이트리스트 변수 사용)
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require pg_dump \
  --schema-only \
  --no-owner \
  --no-privileges \
  --no-comments \
  --no-security-labels \
  --no-tablespaces \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  > docs/references/snapshots/2026-04-27-prod-schema.sql

# 후처리 검증 (FR-0-G + T2): 데이터 변경 SQL 0건
grep -c -E '^(INSERT|UPDATE|DELETE|ALTER TABLE [^ ]+ (DROP|RENAME))' \
  docs/references/snapshots/2026-04-27-prod-schema.sql
# 기대 출력: 0

# 헤더 메타 마스킹 (T3)
sed -i.bak \
  -e "s/$ALLOWED_PROD_HOST/REDACTED_HOST/g" \
  -e "s/$ALLOWED_PROD_DB/REDACTED_DB/g" \
  docs/references/snapshots/2026-04-27-prod-schema.sql
rm -f docs/references/snapshots/2026-04-27-prod-schema.sql.bak

# dump 해시
SHA=$(sha256sum docs/references/snapshots/2026-04-27-prod-schema.sql | cut -d' ' -f1)
echo "SHA256: $SHA"
```

#### Step 2-C: 사후 xact_commit 검증 (T10)

```bash
# dump 후 즉시 다시 측정
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require psql \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  -c "SELECT datname, xact_commit, xact_rollback FROM pg_stat_database WHERE datname='SW_Dept';" \
  > /tmp/xact-after.txt

# diff: xact_commit 증가량은 본 ro 세션의 dump 트랜잭션만 (보통 ~수십)
# 본 사용자가 다른 활동 안 했다면 증가량 ≤ 50 정도가 정상
diff /tmp/xact-before.txt /tmp/xact-after.txt
```

#### Step 2-D: meta.md 통합 작성 (Claude → Step 3 단계)

Step 3 에서 모든 임시 파일(`/tmp/pg-*`)을 사용자가 Claude 에 전달 → Claude 가 `2026-04-27-prod-schema-meta.md` 통합 작성.

**검증 항목**:
- [ ] T0 ro DSN 강제 — Step 0 가드 통과
- [ ] T2 grep 결과 = 0
- [ ] T3 호스트명·DB명 마스킹 완료
- [ ] T10 xact_commit 증가량 ≤ 50 (본 세션 dump 트랜잭션만)
- [ ] dump 파일 SHA256 기록

### Step 3: dump 결과 검수·git 커밋 — Claude

사용자가 dump 파일을 전달하면 Claude 가:
1. 파일 검수 (NFR-6 6 플래그 효과 확인, 자격증명·비밀 미포함)
2. `2026-04-27-prod-schema-meta.md` 작성 (PG 버전, dump 시점, 실행자, 파일 크기·해시)
3. git add + commit (구현 단계 통합 커밋에 포함)

### Step 4: `db_init_phase1.sql` 작성 — Claude

**작성 원칙**:
- 컬럼 정의·인덱스는 **Step 3 dump 결과 기준**
- 부모→자식 19테이블 순서 (FR-1-B)
- 각 테이블 `CREATE TABLE IF NOT EXISTS`, FK 인라인
- 마스터 시드 5개 (~19행) `INSERT … ON CONFLICT DO NOTHING`
- 라이선스 모듈 제외 (`license_registry`, `license_upload_history`, `qr_license`)

**파일 구조**:
```sql
-- ============================================================
-- SW Manager — DB 초기화 Phase 1 (기초 DDL)
-- ============================================================
-- 본 스크립트는 신규 환경에서 가장 먼저 실행되는 기초 DDL 입니다.
-- 실행 순서: phase1.sql → [phase1_sigungu.sql] → phase2.sql → V*.sql
--
-- 대상 PostgreSQL: 14+ (운영DB 실제 버전: <Step2_확정>)
-- 멱등성: CREATE TABLE/INDEX IF NOT EXISTS + INSERT ... ON CONFLICT
-- 라이선스 모듈은 영구 보류 — license_registry, license_upload_history,
-- qr_license 는 본 파일에 정의되지 않음 (재개 시 별도 스프린트)
-- ============================================================

BEGIN;

-- ===== 1. 코드 마스터 (의존 없음) =====
CREATE TABLE IF NOT EXISTS sigungu_code (...);
CREATE TABLE IF NOT EXISTS sys_mst (...);
CREATE TABLE IF NOT EXISTS prj_types (...);
CREATE TABLE IF NOT EXISTS maint_tp_mst (...);
CREATE TABLE IF NOT EXISTS cont_stat_mst (...);
CREATE TABLE IF NOT EXISTS cont_frm_mst (...);

-- ===== 2. users / ps_info =====
CREATE TABLE IF NOT EXISTS users (...);
CREATE TABLE IF NOT EXISTS ps_info (...);

-- ===== 3. sw_pjt (코드 마스터 의존) =====
CREATE TABLE IF NOT EXISTS sw_pjt (
    proj_id BIGSERIAL PRIMARY KEY,
    ...
    FOREIGN KEY (prj_type_cd) REFERENCES prj_types(prj_type_cd),
    FOREIGN KEY (sys_nm_en) REFERENCES sys_mst(sys_nm_en),
    ...
);

-- ===== 4. tb_infra_* (sw_pjt 의존) =====
CREATE TABLE IF NOT EXISTS tb_infra_master (...);
CREATE TABLE IF NOT EXISTS tb_infra_server (...);
CREATE TABLE IF NOT EXISTS tb_infra_software (...);
CREATE TABLE IF NOT EXISTS tb_infra_link_upis (...);
CREATE TABLE IF NOT EXISTS tb_infra_link_api (...);
CREATE TABLE IF NOT EXISTS tb_infra_memo (...);

-- ===== 5. access_logs (users 의존) =====
CREATE TABLE IF NOT EXISTS access_logs (...);

-- ===== 6. tb_pjt_* (sw_pjt 의존) =====
CREATE TABLE IF NOT EXISTS tb_pjt_target (...);
CREATE TABLE IF NOT EXISTS tb_pjt_manpower_plan (...);
CREATE TABLE IF NOT EXISTS tb_pjt_schedule (...);

-- ===== 7. 마스터 시드 (~19행) =====
INSERT INTO sys_mst (sys_nm_en, sys_nm_ko, ...) VALUES
  ('UPIS', '도시계획정보체계', ...),
  ('KRAS', '부동산종합공부시스템', ...),
  ('IPSS', '지하시설물관리시스템', ...),
  ('GIS_SW', 'GIS SW', ...),
  ('APIMS', '도로관리시스템', ...)
ON CONFLICT (sys_nm_en) DO NOTHING;
-- prj_types, cont_stat_mst, cont_frm_mst, maint_tp_mst 동일 패턴

COMMIT;
```

**컬럼 diff 표** (작성 후 본 파일 §부록 B 에 기록):

| 테이블 | 컬럼 | 운영DB | JPA 엔티티 | phase1.sql 채택 | 후속 처리 |
|--------|------|--------|-----------|---------------|----------|
| (Step 4 작업 결과로 채워짐) | | | | | |

### Step 5: `db_init_phase1_sigungu.sql` 작성 — Claude

#### Step 5-A: 데이터 추출 (사용자 1회, ro 롤)

```bash
source .team-workspace/phase1-guard.sh
phase1_guard dump "$ALLOWED_PROD_HOST" "$ALLOWED_PROD_PORT" "$ALLOWED_PROD_DB" "$ALLOWED_PROD_USER"

# data-only dump (sigungu_code 만)
PGPASSWORD='<Step1_임시비밀번호>' PGSSLMODE=require pg_dump \
  --data-only \
  --no-owner --no-privileges --no-comments \
  -t sigungu_code \
  -h "$ALLOWED_PROD_HOST" -p "$ALLOWED_PROD_PORT" -U "$ALLOWED_PROD_USER" \
  -d "$ALLOWED_PROD_DB" \
  > /tmp/sigungu-raw.sql
```

#### Step 5-B: COPY → INSERT…ON CONFLICT 변환 정책 (codex 조건 4)

`pg_dump` 기본 출력은 `COPY sigungu_code (...) FROM stdin;` 형식. 본 스프린트는 **두 옵션 중 선택**:

**옵션 A (권장 — 신규 환경 빈 테이블 가정):** `COPY` 그대로 사용
- 장점: 빠름 (수천 행 ~수백ms), 가독성 명확
- 단점: 충돌 시 에러 (멱등성 약함)
- 적용 조건: 신규 환경 초기화 = 빈 테이블 = 충돌 가능성 0

**옵션 B (멱등성 우선):** 임시 테이블 경유 + INSERT…ON CONFLICT
```sql
BEGIN;
CREATE TEMP TABLE sigungu_stage (LIKE sigungu_code INCLUDING DEFAULTS);
COPY sigungu_stage FROM stdin;
... 데이터 행 ...
\.

INSERT INTO sigungu_code SELECT * FROM sigungu_stage
  ON CONFLICT (sigungu_cd) DO NOTHING;
COMMIT;
```
- 장점: 재실행 멱등 (T17), 운영 데이터 충돌 0
- 단점: 약간 복잡, 약간 느림

#### Step 5-C: 본 스프린트 채택 — 옵션 B (멱등성 우선)

근거: phase1.sql 의 다른 시드(`sys_mst` 등)도 `ON CONFLICT DO NOTHING` 사용 → 일관성 유지. sigungu 수천 행도 임시 테이블 경유 후 INSERT 시간 ~1초 이내로 실용 한계 내.

**파일 구조**:
```sql
-- ============================================================
-- SW Manager — sigungu_code 시드 (Phase 1 분리)
-- ============================================================
-- 충돌 정책: ON CONFLICT (sigungu_cd) DO NOTHING (멱등성 보장)
-- 로드 순서: phase1.sql 다음 (sigungu_code 테이블 정의 전제)
-- 데이터 출처: 운영DB pg_dump --data-only -t sigungu_code (2026-04-27)
-- ============================================================

BEGIN;

CREATE TEMP TABLE sigungu_stage (LIKE sigungu_code INCLUDING DEFAULTS);

COPY sigungu_stage (sigungu_cd, sigungu_nm, sido_cd, ...) FROM stdin;
1100000	서울특별시	11	...
... (~수천 행)
\.

INSERT INTO sigungu_code SELECT * FROM sigungu_stage
  ON CONFLICT (sigungu_cd) DO NOTHING;

DROP TABLE sigungu_stage;
COMMIT;
```

**충돌 타깃 키 명시 (codex 조건 4)**: PK = `sigungu_cd VARCHAR(N)` (정확한 길이는 dump 결과 기준 확정).

### Step 6: `db_init_phase2.sql` 헤더 정정 — Claude

```sql
-- 변경 전 (현행)
-- 전제 테이블 목록 (phase1 에서 생성되어 있어야 함):
--   sw_pjt, users, tb_infra_master, ..., pjt_equip, tb_pjt_target,
--   tb_pjt_manpower_plan, tb_pjt_schedule, tb_performance_summary

-- 변경 후
-- 전제 스크립트: db_init_phase1.sql (19 테이블 + 마스터 시드)
--               db_init_phase1_sigungu.sql (선택, 행정동 코드)
-- 추가 의존: tb_performance_summary 는 V100 (work_plan_performance_tables.sql) 에서 생성됨
-- pjt_equip 은 V025 에서 DROPPED (2026-04-22, 미사용 테이블 제거)
```

### Step 7: `setup-guide.md` 갱신 — Claude

§"DB 초기화" (없으면 신설):
```markdown
## DB 초기화 (신규 환경)

새 PC / Docker 컨테이너 / DR 복구 시:

```bash
psql -d <db_name> -f src/main/resources/db_init_phase1.sql
psql -d <db_name> -f src/main/resources/db_init_phase1_sigungu.sql  # 선택
psql -d <db_name> -f src/main/resources/db_init_phase2.sql
for f in swdept/sql/V*.sql; do psql -d <db_name> -f "$f"; done
```

**주의**:
- 라이선스 모듈은 phase1 에 미포함. 라이선스 엔드포인트 호출 시 SQL 오류 — 의도된 동작
- V*.sql 은 파일명 사전순 실행. V100 은 V026 보다 뒤
```

### Step 8: Docker ephemeral 검증 — Claude

**가드 (FR-4-A + Step 0 통합)**: 검증 스크립트가 운영 DSN 으로 연결 시 즉시 abort + 격리 네트워크.

```bash
# Step 8-1: 가드
source .team-workspace/phase1-guard.sh
phase1_guard ephemeral "$ALLOWED_EPHEMERAL_HOST" "$ALLOWED_EPHEMERAL_PORT" \
                       "$ALLOWED_EPHEMERAL_DB"   "$ALLOWED_EPHEMERAL_USER"

# Step 8-2: ephemeral 컨테이너 기동 (격리 네트워크 + 격리 볼륨)
docker network create --driver bridge phase1-net 2>/dev/null || true
docker volume create phase1-vol 2>/dev/null || true

docker run --rm --name phase1-verify \
  --network phase1-net \
  -v phase1-vol:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=verify \
  -e LANG=ko_KR.UTF-8 \
  -p 127.0.0.1:25880:5432 \
  -d postgres:14

# postgres ready 대기 (60초 timeout)
for i in $(seq 1 60); do
  PGPASSWORD=test pg_isready -h localhost -p 25880 -U postgres && break
  sleep 1
done

# Step 8-3: 운영DB 와의 환경 호환성 사전 점검 (T13~T15)
EPH_VER=$(PGPASSWORD=test psql -h localhost -p 25880 -U postgres -d verify -tAc "SHOW server_version;")
PROD_VER=$(grep "server_version" /tmp/pg-settings.txt | awk '{print $3}')
echo "ephemeral: $EPH_VER, prod: $PROD_VER"
# 메이저 버전 일치만 검증 (마이너는 차이 허용)

# 확장 호환성 (T14)
PROD_EXTS=$(awk 'NR>2 && $1!~/^-/ && $1!="" {print $1}' /tmp/pg-extensions.txt)
echo "prod 확장: $PROD_EXTS — ephemeral 에 동일 확장 필요 시 사전 CREATE EXTENSION"

# Step 8-4: 순차 실행 (시간 측정)
START_TS=$(date +%s)
PGPASSWORD=test psql -h localhost -p 25880 -U postgres -d verify \
  -v ON_ERROR_STOP=1 \
  -f src/main/resources/db_init_phase1.sql \
  -f src/main/resources/db_init_phase1_sigungu.sql \
  -f src/main/resources/db_init_phase2.sql

for f in swdept/sql/V*.sql; do
  PGPASSWORD=test psql -h localhost -p 25880 -U postgres -d verify -v ON_ERROR_STOP=1 -f "$f" || {
    echo "FAIL: $f"
    docker rm -f phase1-verify
    exit 1
  }
done
PHASE_TS=$(date +%s)
echo "phase1+phase2+V*.sql 실행: $((PHASE_TS - START_TS))초"

# Step 8-5: 멱등성 재실행 (T4, T17)
PGPASSWORD=test psql -h localhost -p 25880 -U postgres -d verify \
  -v ON_ERROR_STOP=1 \
  -f src/main/resources/db_init_phase1.sql \
  -f src/main/resources/db_init_phase1_sigungu.sql
# 에러 없이 통과해야 함 (모든 IF NOT EXISTS + ON CONFLICT 동작)

# Step 8-6: 시퀀스 OWNED BY 검증 (T12)
PGPASSWORD=test psql -h localhost -p 25880 -U postgres -d verify \
  -c "SELECT s.relname AS seq, t.relname AS owned_by FROM pg_class s
      JOIN pg_depend d ON d.objid=s.oid AND d.refobjsubid > 0
      JOIN pg_class t ON t.oid=d.refobjid
      WHERE s.relkind='S' AND t.relname IN ('sw_pjt','users','tb_infra_master',
        'tb_pjt_target','tb_pjt_manpower_plan','tb_pjt_schedule')
      ORDER BY s.relname;"
# 기대: 19테이블 중 SERIAL/IDENTITY 시퀀스 모두 OWNED BY 연결됨

# Step 8-7: 양측 schema diff (T8 + T18 해시 비교)
PGPASSWORD=test pg_dump --schema-only --no-owner --no-privileges \
  --no-comments --no-security-labels --no-tablespaces \
  -h localhost -p 25880 -U postgres -d verify \
  > /tmp/ephemeral-schema.sql

# 헤더 타임스탬프 제거 후 비교 (T18 — 환경 의존성 노이즈 배제)
sed -i.bak \
  -e '/^-- Dumped/d' \
  -e '/^-- Started/d' \
  -e '/^SET /d' \
  /tmp/ephemeral-schema.sql

EPH_SHA=$(sha256sum /tmp/ephemeral-schema.sql | cut -d' ' -f1)
PROD_SHA=$(sha256sum docs/references/snapshots/2026-04-27-prod-schema.sql | cut -d' ' -f1)
echo "ephemeral SHA: $EPH_SHA"
echo "prod SHA: $PROD_SHA"

# 라이선스 3 테이블 외 차이 0 (T8)
diff <(sort docs/references/snapshots/2026-04-27-prod-schema.sql) \
     <(sort /tmp/ephemeral-schema.sql) \
  | grep -v -E "license_registry|license_upload_history|qr_license" \
  > /tmp/unexpected-diff.txt

END_TS=$(date +%s)
TOTAL_DUR=$((END_TS - START_TS))
echo "전체 검증 시간: ${TOTAL_DUR}초"

# Step 8-8: 컨테이너·볼륨·네트워크 파기 (T11 + 롤백)
docker rm -f phase1-verify
docker volume rm -f phase1-vol
docker network rm phase1-net 2>/dev/null || true
```

**검증 항목**:
- [ ] T0: ephemeral 가드 통과
- [ ] T4: 2회 실행 멱등성
- [ ] T6: 부모→자식 순서 무결 (FK 참조 에러 0)
- [ ] T7: phase1 → phase2 → V*.sql 전체 에러 0
- [ ] T8: `/tmp/unexpected-diff.txt` 빈 파일 (또는 사소한 메타 차이만)
- [ ] T9 (조정): **P95 180초 / P99 300초** 이내 (codex 조건 3 — 이미지 풀 시 60초는 비현실)
- [ ] T11: 컨테이너 파기 확인 (`docker ps -a | grep phase1-verify` → 없음)
- [ ] T12: 시퀀스 OWNED BY 모두 연결됨
- [ ] T13: search_path = public 일관 (정적 분석)
- [ ] T14: 운영DB 확장 목록과 ephemeral 호환 (메이저 버전 일치)
- [ ] T15: 콜레이션 차이 기록 (운영=ko_KR.UTF-8 vs ephemeral=ko_KR.UTF-8 일치 확인)
- [ ] T17: 시드 + DDL 모두 멱등 (Step 8-5 통과)
- [ ] T18: dump 해시 비교 (헤더 제거 후) — 라이선스 외 동일 해시 도달 가능성 점검

**예상 차이 (의도됨, diff 무시 OK)**:
- `license_registry`, `license_upload_history`, `qr_license` (운영에만)
- 시퀀스 마지막 값(`setval` 결과 — 운영은 사용 흔적, ephemeral 은 0/1)
- 운영DB 의 `tb_org_unit`, `tb_*_master` 의 추가 시드 행

**diff 발견 시 조치**:
- 컬럼 타입·정밀도 차이 → phase1.sql 수정 후 재검증
- 의도치 않은 테이블 차이 → 본 스프린트 §부록 C 에 기록, 후속 스프린트로 분리

### Step 9: 운영DB 읽기전용 롤 비활성 — 사용자 작업

#### Step 9-A: 기본 — 소프트 비활성 (권장, codex 조건 5)

```sql
-- 1단계: NOLOGIN
ALTER ROLE ro_phase1_audit NOLOGIN;

-- 2단계: VALID UNTIL 만료
ALTER ROLE ro_phase1_audit VALID UNTIL 'yesterday';

-- 검증
SELECT rolname, rolcanlogin, rolvaliduntil
  FROM pg_roles WHERE rolname='ro_phase1_audit';
-- 기대: rolcanlogin=f, rolvaliduntil < now()
```

**검증 항목 (T11)**:
- [ ] `rolcanlogin=f`
- [ ] `rolvaliduntil` 과거 시점
- [ ] 임시 비밀번호 1Password 항목 폐기

#### Step 9-B: 완전 DROP (선택, 사용자 추가 확인 후)

소프트 비활성 후 며칠~몇 주 후, 추가 dump 필요 없음 확인되면:

```sql
-- 3단계: REVOKE (DROP 전 명시적 권한 제거)
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM ro_phase1_audit;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM ro_phase1_audit;
REVOKE USAGE ON SCHEMA public FROM ro_phase1_audit;
REVOKE CONNECT ON DATABASE "SW_Dept" FROM ro_phase1_audit;

-- 4단계: DROP
DROP ROLE ro_phase1_audit;

-- 검증
SELECT count(*) FROM pg_roles WHERE rolname='ro_phase1_audit';
-- 기대: 0
```

**Step 9-B 는 사용자가 별도 시점에 명시적으로 결정·실행. 본 스프린트 작업완료 시점에는 9-A 까지만 필수.**

### Step 10: 문서 갱신 — Claude

- `docs/PLANS.md` — 본 스프린트 status `진행중` → `완료 (commit XXX)`
- 본 파일 §부록 A·B·C 채움 (Step 2 PG 버전, Step 4 컬럼 diff, Step 8 검증 결과)

---

## 4. 검증 매트릭스 (T-매트릭스)

| ID | 항목 | 기준 | 단계 |
|----|------|------|------|
| **T0** | **ro DSN 강제 검사** | 의도적 UPDATE 시도 → "cannot execute UPDATE in a read-only transaction" 차단 | Step 1 |
| T1 | 읽기전용 롤 설정 | `default_transaction_read_only=on` + `search_path=public` | Step 1 |
| T2 | schema dump 무결성 | grep INSERT/UPDATE/DELETE = 0 | Step 2-B |
| T3 | dump 자격증명·메타 미포함 | 호스트·DB명 sed 마스킹 완료 | Step 2-B |
| T4 | phase1.sql 멱등성 | 동일 ephemeral DB 에 2회 실행 — 에러 0 | Step 8-5 |
| T5 | sigungu 분리 로드 | phase1.sql + phase1_sigungu.sql 순차 — 에러 0 | Step 8-4 |
| T6 | 부모→자식 순서 무결 | phase1.sql 단독 실행 — FK 참조 에러 0 | Step 8-4 |
| T7 | phase1 → phase2 → V*.sql 전체 | 에러 0 + entity 테이블 모두 존재 (라이선스 3 제외) | Step 8-4 |
| T8 | 양측 diff 라이선스 외 0 | `/tmp/unexpected-diff.txt` 빈 파일 (또는 사소한 메타) | Step 8-7 |
| **T9** | **검증 시간 SLA** | **P95 180초 / P99 300초** (이미지 캐시 후) — codex 조건 3 | Step 8 |
| T10 | 운영DB 쓰기 0 | `pg_stat_database.xact_commit` 증가량 ≤ 50 (본 dump 세션만) | Step 2-C |
| T11 | 임시 롤 비활성/DROP | `rolcanlogin=f` + `rolvaliduntil` 과거 시점 | Step 9-A |
| **T12** | **시퀀스 OWNED BY 무결** | 19테이블 SERIAL/IDENTITY 시퀀스 모두 OWNED BY 연결 | Step 8-6 |
| **T13** | **search_path 고정** | phase1.sql 모든 객체 `public.<name>` 또는 search_path 의존 검증 | Step 8 |
| **T14** | **확장 호환성** | 운영DB `pg_extension` 목록과 ephemeral 메이저 버전 일치 | Step 8-3 |
| **T15** | **콜레이션 차이** | 운영 `lc_collate` (ko_KR.UTF-8) vs ephemeral 일치, ICU 버전 기록 | Step 8-3 |
| **T16** | **대량 적재 성능** | sigungu COPY (옵션 A) 또는 INSERT (옵션 B) 시간 ≤ 5초 | Step 8-4 |
| **T17** | **재실행 멱등성 확장** | 시드(INSERT) + DDL(CREATE) 모두 2회 실행 에러 0 | Step 8-5 |
| **T18** | **dump 재현성 해시** | 헤더 타임스탬프 제거 후 sha256sum 비교 — 라이선스 외 본질 동일 확인 | Step 8-7 |

---

## 5. 롤백 전략

### 5-0. 0단계 (최우선) — 프로덕션 접속 감지 시 하드스톱

운영 DSN 으로 어떤 단계라도 연결 시도 감지되면:
1. 즉시 abort (`exit 99`)
2. `docs/exec-plans/phase1-audit.log` 에 `HARD_STOP` 라인 기록
3. 사용자 보고 → 원인 분석 후 재진입
4. **이전 통과 단계 결과는 보존** (재실행 가능)

### 5-1. 단계별 롤백

| 시나리오 | 롤백 방식 | 운영 영향 |
|----------|----------|----------|
| Step 0 가드 미통과 | Step 1 진행 보류, 환경 보강 후 재시도 | 0 |
| Step 1 롤 신설 직후 실패 | Step 9-A 즉시 실행 (NOLOGIN+VALID UNTIL) | 카탈로그 변경 (롤 생성·비활성), 데이터 무영향 |
| Step 2 dump 실패·중단 | Step 9-A 실행, dump 임시 파일 삭제 | 0 |
| Step 4~7 실패 (파일 작업) | git restore / 파일 삭제 | 0 |
| Step 8 검증 실패 | 컨테이너+볼륨+네트워크 모두 파기 (`docker rm/volume rm/network rm`), phase1.sql 수정 후 재검증 | 0 |
| Step 9-A 실행 후 보안 우려 발견 | Step 9-B (REVOKE + DROP) 즉시 진행 | 카탈로그 변경, 데이터 무영향 |

### 5-2. 산출물 안전 삭제

| 산출물 | 식별 기준 | 삭제 방식 |
|--------|----------|----------|
| dump 파일 | 경로 + SHA256 해시 | `git restore --source=<commit> --staged --worktree` 로 복구 가능 위치까지 보존 후 삭제 |
| 환경 스냅샷 (`/tmp/pg-*`) | 경로 + 작성 시각 | Step 8 종료 후 `rm /tmp/pg-*` |
| ephemeral DB 잔여 | 컨테이너명·볼륨명·네트워크명 | `docker rm -f / volume rm / network rm` 강제 종료 가드 |
| 감사 로그 | `docs/exec-plans/phase1-audit.log` | 보존 (스프린트 종료 후에도 git 추적, 사고 분석용) |

**핵심**: 본 스프린트의 모든 변경은 **운영DB 데이터 무영향**. 롤백 = 파일 삭제 + 임시 롤 NOLOGIN 이면 종료. DROP 은 사용자 추가 확인 후 별도 시점.

---

## 6. 일정·체크포인트

| 단계 | 예상 소요 | 사용자 작업 시점 |
|------|----------|----------------|
| **Step 0 (프리플라이트 가드)** | **10분** | 사용자 + Claude 공동 |
| Step 1 (롤 신설) | 10분 | 사용자 1회 (T0 음성 검증 포함) |
| Step 2 (dump + 환경 스냅샷) | 10분 | 사용자 1회 |
| Step 3~7 (Claude 파일 작업) | 60~90분 | - (사용자는 리뷰만) |
| Step 8 (Docker 검증) | P95 180초 / P99 300초 + 분석 시간 | - |
| Step 9-A (NOLOGIN) | 5분 | 사용자 1회 |
| Step 9-B (DROP) | 5분 (선택, 별도 시점) | 사용자 추가 확인 후 |
| Step 10 (문서 갱신) | 10분 | - |

**총 소요**: ~2~2.5시간 (Claude 작업) + 사용자 ~30분 (Step 0+1+2+9-A).

**체크포인트** (사용자 재승인 없이 다음 단계 진행 금지):
- **CP-1 (Step 0 후)**: 프리플라이트 가드 통과 — 사용자 확인 → Step 1 진행 승인
- **CP-2 (Step 2 후)**: dump 파일 + 환경 스냅샷 5종 (`/tmp/pg-*`) 사용자가 Claude 에 전달
- **CP-3 (Step 8 후)**: 검증 결과 사용자 보고. 의도치 않은 diff 발견 시 사용자가 (a) phase1.sql 수정 (b) 후속 스프린트 분리 (c) 본 스프린트 abort 결정
- **CP-4 (Step 10 전 / 9-A 후)**: 작업완료 직전 사용자 최종 확인. Step 9-B (DROP) 시점은 별도 결정.

---

## 7. 부록

### A. 운영DB 정보 (Step 2 결과)
- **PostgreSQL 버전**: 16.11 (compiled by Visual C++ build 1944, 64-bit)
- **핵심 pg_settings**: server_encoding=UTF8 / TimeZone=Asia/Seoul / standard_conforming_strings=on / default_transaction_isolation=read committed / shared_preload_libraries=(none)
- **확장 목록**: `plpgsql 1.0` (단일 — ephemeral 검증 시 추가 EXTENSION 불필요)
- **콜레이션**: ICU 153.14.* 기반 (운영=ko_KR.UTF-8, ephemeral=C 로 검증 — schema diff 0 확인되어 무관)
- **xact_commit 변화량**: 28 (기대 ≤ 50, T10 통과)
- **dump 시점**: 2026-04-27 15:59 KST
- **dump 실행자**: 박욱진 (ukjin55@gmail.com), `ro_phase1_audit` 롤
- **dump 파일**: `docs/references/snapshots/2026-04-27-prod-schema.sql` — 99,727 bytes, 3,758 라인
- **SHA256 (마스킹 후)**: `f3b2b51a12a0e1652636fb5e46a95bd6cf13e0bdc5924394cac8599b24cd3ecb`

### B. 컬럼 diff (운영DB vs JPA 엔티티) — Step 4 결과

Step 8 의 phase1 19 테이블 schema diff 결과 **0건** 으로 phase1.sql 이 운영DB 와 일치함을 입증. 즉, Step 4 작성 시점에 발견된 모든 운영-엔티티 간 차이가 phase1.sql 에 정확히 반영됨.

**Step 4 작성 중 처리된 주요 운영 우선 결정**:

| 테이블 | 컬럼 / 항목 | 기획서 가정 | 운영DB 실측 | phase1.sql 채택 |
|--------|-------------|------------|------------|----------------|
| `sigungu_code` | PK 컬럼명 | `sigungu_cd` (기획서 v1) | `adm_sect_c` | **`adm_sect_c`** (운영 우선) |
| (전체 19) | 컬럼 타입·NOT NULL·DEFAULT | (엔티티 추정) | dump 추출본 | 운영 우선 (Step 8 schema diff 0 으로 입증) |

세부 컬럼별 diff 데이터는 Step 4 작업 중 비교 산출물(`db_init_phase1.sql` 자체) 가 SSoT 역할.

### C. ephemeral 검증 결과 — Step 8

**검증 환경 변경 (exec-plan 원안 대비)**:
- Docker 대신 동일 PG 16 binary 로 별도 클러스터 사용
  - 위치: `C:\Users\PUJ\pg16-verify\data` @ port `25880` (운영 5880 과 끝자리 정렬, 운영 클러스터와 완전 격리)
  - 사유: 본 PC Docker 미설치, 추가 설치 비용 회피
- exec-plan 본문 25432 → 25880 일괄 정정 (9건)

**T-매트릭스 결과**:

| ID | 항목 | 결과 |
|----|------|------|
| T0 | ephemeral 가드 통과 | ✅ guard `ephemeral` op PASS, 운영 좌표 매칭 시 HARD STOP 동작 확인 |
| T4 | 2회 재실행 멱등성 | ✅ (※ 패치 후 — 아래 "본 sprint 발견 #1" 참조) |
| T6 | 부모→자식 FK 순서 | ✅ (1차 실행 에러 0) |
| T7 | phase1 → phase2 → V*.sql 전체 에러 0 | ❌ phase2.sql line 60 에서 stop (※ "본 sprint 발견 #2" 참조 — out-of-scope) |
| T8 | phase1 19 테이블 schema diff | ✅ **0건** (`\restrict`/`\unrestrict` pg_dump 16 보안 토큰 외) |
| T9 | 실행 시간 (P95 180s / P99 300s) | ✅ phase1+sigungu 합산 <1초 (인메모리 SSD 환경) |
| T11 | 컨테이너 파기 | (해당 없음 — 25880 클러스터는 후속 검증용 보존, sprint 종료 시 `pg_ctl stop` + data dir 삭제로 대체) |
| T12 | 시퀀스 OWNED BY | ✅ 6/6 연결 (`sw_pjt`/`tb_infra_master`/`tb_pjt_*`/`users` SERIAL 모두) |
| T13 | search_path public | ✅ 일관 |
| T14 | 확장 호환 | ✅ plpgsql 만, 운영=ephemeral 일치 (PG 메이저 16 동일) |
| T15 | 콜레이션 | ✅ schema diff 0 으로 무관 입증 |
| T17 | 시드 + DDL 멱등 | ✅ 마스터 54건 + sigungu 279건 변동 0 (3회 실행 후) |
| T18 | dump 해시 비교 | ✅ 정규화(`SET`/`-- Dumped`/`\restrict` 제거) 후 verify==prod 19 테이블 |

**실행 시간**: phase1.sql + sigungu.sql 1차 0초, 2차 0초, 3차 0초 (모두 측정 단위 미만).

**에러 0 여부**: phase1 sprint 스코프(phase1.sql + sigungu.sql) 한정 → 0건. phase2/V*.sql 포함 시 → phase2.sql line 60 stop (본 sprint 외 이슈).

**diff 결과**: phase1 19 테이블 별도 dump (verify pg_dump -t × 19) vs prod-schema.sql 의 19 테이블 블록 추출본을 sort+diff → 텍스트 정규화 후 0 라인.

**dump 해시 비교 (T18)**: 정규화 적용 후 verify(563라인) == prod(562라인 + 마지막 `\unrestrict` 1라인 부재) 본문 동일.

---

#### 본 sprint 신규 발견·조치

**#1 phase1.sql 멱등성 결함** (in-scope, 본 sprint 에서 패치)
- 증상: 2차 실행 시 `multiple primary keys for table sigungu_code are not allowed` (PG 에러 코드 `42P16 invalid_table_definition`)
- 원인: `DO $$ ... ALTER TABLE ... ADD CONSTRAINT ... PRIMARY KEY ... EXCEPTION WHEN duplicate_object/duplicate_table ...` 패턴이 PG 의 "이미 PK 있음" 케이스를 캐치 못 함
- 패치: 동일 EXCEPTION 패턴 35건 모두에 `WHEN invalid_table_definition THEN NULL;` 추가 (PK 외 FK·INDEX 의 동등 보호)
- 검증: 패치 후 3회 연속 실행 rc=0, 마스터 54건·sigungu 279건 멱등 유지

**#2 phase2/V018 ordering bug** (out-of-scope, 후속 sprint 분리 권고)
- 증상: 신규 환경 `phase1 → phase2 → V001~V018+` 순서에서 `db_init_phase2.sql:60` `INSERT ... ON CONFLICT (sys_nm_en, process_name)` 가 `swdept/sql/V018_process_master_dedup.sql:66` 의 UNIQUE 보다 먼저 실행되어 `there is no unique or exclusion constraint matching the ON CONFLICT specification` 에러
- 영향 범위: 동일 패턴이 `tb_service_purpose` (db_init_phase2.sql:70) 에도 존재
- 운영DB 미발견 사유: 점진 적용 — phase2 INSERT 가 V018 보다 늦게 추가됐거나 실제 fresh-init 흐름이 한 번도 발생 안 함
- 권고 후속: `phase2-V018-init-ordering` 별도 sprint
  - 옵션 (a) phase2.sql 의 ON CONFLICT INSERT 를 V019_phase2_seed.sql 로 분리 (V018 이후 실행)
  - 옵션 (b) V018 의 UNIQUE 제약 부분을 phase2.sql 의 CREATE TABLE 시점으로 흡수 (phase2 자체 멱등화)
- **본 sprint 결론**: phase1 SSoT 정식화는 #2 결함과 독립적으로 검증 통과. #2 는 본 검증이 정확히 잡으려고 한 drift case 의 실제 산출물이며 sprint 의 가치를 입증.

---

#### Step 9-A 실행 결과 (2026-04-27 21:48 KST)

| 시점 | `rolcanlogin` | `rolvaliduntil` |
|------|---------------|-----------------|
| 사전 | `true` | `(none)` |
| 사후 | **`false`** | **`2026-04-26 00:00:00+09`** (어제) |

- `ALTER ROLE ro_phase1_audit NOLOGIN;` rc=0
- `ALTER ROLE ro_phase1_audit VALID UNTIL 'yesterday';` rc=0
- 감사 로그: `docs/exec-plans/phase1-audit.log` 에 PASS 기록
- 임시 비밀번호: 이전 세션에서 폐기 (Step 9 사전조건)

**Step 9 검증 항목 (T11 부속)**: ✅ rolcanlogin=f / ✅ rolvaliduntil 과거 시점 / ✅ 임시 비번 폐기
**Step 9-B (DROP)**: 본 스프린트 범위 외 — 후속 시점에 사용자 명시 결정으로 진행

### D. 감사 로그 — Step 0 부터 누적
`docs/exec-plans/phase1-audit.log` 참조. 본 스프린트 동안 발생한 모든 DB 접속 PASS/HARD_STOP 기록.

---

## 8. 승인 요청

codex 검토 5조건 모두 반영 완료 (개정 이력 v2 참조):
1. ✅ Step 0 프리플라이트 가드 신설 + 운영 접속 하드스톱
2. ✅ ro 롤 스펙 강화 (search_path 고정, GRANT pg_read_all_settings) + 사전·사후 환경 스냅샷
3. ✅ 검증 매트릭스 T0+T12~T18 추가, T9 SLA 조정 (P95 180s/P99 300s)
4. ✅ Step 5 sigungu 충돌 키 명시 + 옵션 A/B (COPY vs INSERT…ON CONFLICT) 정책 명시
5. ✅ Step 9 NOLOGIN 우선 + 9-B DROP 분기 (사용자 추가 확인)

본 개발계획 v2 에 대한 **사용자 최종승인** 을 요청합니다.

승인 후 Step 0 (프리플라이트 가드) 부터 진행. **Step 1~2 는 사용자 작업이므로 사용자 결정 후 시작.**
