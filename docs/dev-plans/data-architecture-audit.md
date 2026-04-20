---
tags: [dev-plan, sprint, audit, data-architecture]
sprint: "data-architecture-audit"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] 데이터 아키텍처 감사

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[data-architecture-audit]] (v2 사용자 최종승인 완료)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. Step 2-1에 §7-5 전체(5개 하위통제) 명문화
2. Step 3에 테이블 미사용 기능 **예외 루프** 추가
3. Step 3-5 `#1-A` 재평가를 **4기준 점수표** + 동률 규칙으로 정량화
4. §2 체크리스트에 **T10~T12** 추가 (근거 / 매트릭스 추적성 / 안전통제 전 준수)
5. §3 롤백을 `git restore -- docs/audit docs/plans` **단일 방식**으로 통일

---

## 0. 전제 / 범위 확정

### 0-1. 본 스프린트는 "감사 + 로드맵" 만 산출. 코드·DB 변경 0
- `./mvnw clean compile` 실행 불필요 (코드 미수정)
- 서버 재기동 불필요
- 테스트 추가 불필요 (감사 결과가 산출물)
- git diff 검사 시 `src/main/java`, `src/main/resources` 변경 0 확인 (§NFR-4)

### 0-2. 작업 도구
- **감사 SQL 러너**: `docs/audit/data-architecture-scan.java` (신규, JDBC 직접 접속, 기획서 §7-5 안전통제 준수)
- **코드 스캔**: `rg` + `Grep` + `Glob`
- **문서 작성**: 마크다운 + Obsidian Dataview 호환 (기존 `docs/audit/`, `docs/plans/` 패턴)

### 0-3. 일정 타임박스 (기획서 §4 승인분)
| Phase | 하한 | 상한 | 중간 체크포인트 |
|-------|------|------|----------------|
| Phase 1 | 0.5일 | 1일 | 기초 9개 마스터 덤프 완료 |
| Phase 2 | 3일 | 5일 | 3일차 중간 리뷰 → 사용자 판단 |
| Phase 3 | 0.5일 | 0.5일 | 로드맵 초안 |
| **합계** | **4일** | **6.5일** | |

### 0-4. 기존 `sys-type-normalization` 스프린트 처리
- Phase 0에서 `docs/plans/sys-type-normalization.md` 상단에 `status: superseded-by-data-architecture-audit` 표기 + 참조 링크 추가
- 실제 삭제 아님 (추적 기록 유지)

---

## 1. 작업 순서

### Step 1 — Phase 0 준비 (0.5h)

**1-1. SQL 러너 환경 확인**
```bash
ls ~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar  # 존재 확인
java --version  # 21+ 확인
```

**1-2. `sys-type-normalization` superseded 표기**
`docs/plans/sys-type-normalization.md` 상단 메타데이터 + 본문 상단 공지 박스:
```markdown
> ⚠️ **이 스프린트는 `data-architecture-audit`로 흡수됨 (2026-04-20).**
> 사유: `sys_mst` 마스터 존재 미확인 상태에서 기획. 전수 감사 결과 반영 후 재편성.
```

**1-3. 감사 문서 뼈대 생성**
- `docs/audit/db-schema-full.md` (빈 파일)
- `docs/audit/data-architecture-master-inventory.md` (섹션 뼈대)
- `docs/audit/data-architecture-utilization-audit.md` (11개 기능 섹션 뼈대)
- `docs/plans/data-architecture-roadmap.md` (섹션 뼈대)

### Step 2 — Phase 1: 기초 마스터 인벤토리 (0.5~1일)

**2-1. SQL 러너 작성** — `docs/audit/data-architecture-scan.java` (기획서 §7-5 전체 5개 하위통제 반영)

**§7-5-1 권한 통제**:
- 실행 전 `SELECT CURRENT_USER;` 로그 + 권한 확인 쿼리 실행:
  ```sql
  SELECT privilege_type FROM information_schema.table_privileges
   WHERE grantee = CURRENT_USER AND table_schema = current_schema() LIMIT 10;
  ```
- `UPDATE/DELETE/INSERT/DROP/ALTER` 문자열이 러너 소스에 포함되지 않음을 `rg -n '(UPDATE|DELETE|INSERT|DROP|ALTER|TRUNCATE)' docs/audit/data-architecture-scan.java` 로 상시 확인 가능. `SELECT` 외 키워드 출현 0건.

**§7-5-2 런타임 통제**:
- 세션 초기화 명령 3종 (첫 트랜잭션):
  ```sql
  SET TRANSACTION READ ONLY;
  SET LOCAL statement_timeout = '30s';
  SET LOCAL lock_timeout = '5s';
  ```
- 모든 SELECT 쿼리에 `LIMIT 1000` **기본 적용** (러너 메서드 시그니처에 강제). 필요 시 명시적 상향(최대 5000).

**§7-5-3 SQL Allowlist**:
- 모든 감사 SQL을 러너 내부 정적 배열 `static final String[] QUERIES`로 하드코딩.
- **애드혹 쿼리 금지** — 러너가 파라미터로 SQL을 받지 않음 (String 파라미터 없는 정적 메서드).
- 러너 파일은 Git 추적 필수.

**§7-5-4 PII 보호**:
- `users`, `ps_info` 쿼리 시 **전체 레코드 덤프 금지**. COUNT + 샘플 최대 3건 + PII 컬럼 마스킹:
  ```sql
  SELECT user_id,
         LEFT(COALESCE(userid, ''), 2) || '***' AS userid_masked,
         LEFT(COALESCE(username, ''), 1) || '***' AS name_masked,
         LEFT(COALESCE(email, ''), 2) || '***' AS email_masked
    FROM users LIMIT 3;
  ```
- 감사 산출물 파일에 실제 PII 값 포함 금지 — 러너가 자동 마스킹 출력.
- **반출 금지**: 러너 출력은 로컬 `docs/audit/` 파일에만 저장. 이메일·메신저 등 외부 공유 금지 (문서화).

**§7-5-5 실행 환경**:
- 로컬 Java JDBC (기존 `preflight-runner.java` 패턴 재사용).
- Read replica 부재 → 단일 운영 DB 직접 접속. **업무시간 외 (야간·주말) 실행 권장**.
- 실행 전 사용자 확인 + 실행 시각을 감사 문서 상단에 기록.

**2-2. 전수 스키마 덤프 → `db-schema-full.md`**
```sql
SELECT table_name, column_name, data_type, character_maximum_length,
       is_nullable, column_default
  FROM information_schema.columns
 WHERE table_schema = current_schema()
 ORDER BY table_name, ordinal_position;
```
마크다운 표 형식으로 저장. 테이블별 섹션화.

**2-3. 9개 마스터 + 기초 기능 테이블 상세**
각 마스터/기초 테이블마다:
- 컬럼 목록 (이미 Phase 0 확인)
- 레코드 수
- 샘플 3~5건 (`users`/`ps_info`는 **PII 마스킹** 필수 — 기획서 §7-5-4)
- FK 제약 현황 (다른 테이블이 이 테이블을 참조하는지)

`data-architecture-master-inventory.md`에 저장:
```markdown
## A. 공통 마스터 레이어
### sys_mst (37건)
| cd | nm |
| --- | --- |
| UPIS | 도시계획정보체계 |
| KRAS | 부동산종합공부시스템 |
| 112 | 112시스템 |
...

**FK 참조**: 없음 (명시적 FK 미설정. 코드 참조만)
**사용 컬럼**: sw_pjt.sys_nm_en, tb_infra_master.sys_nm_en, tb_document.sys_type, inspect_report.sys_type, ps_info.sys_nm_en, tb_process_master.sys_nm_en, tb_service_purpose.sys_nm_en
```

**2-4. "마스터 아닌" 테이블 목록** — 감사 대상 테이블 역추적
- 스키마 덤프에서 9개 마스터 + 기초 기능 테이블(사업/담당자/사용자/서버) 제외
- 나머지 = 감사 대상 테이블 후보
- Phase 2에서 11개 기능 대분류에 맵핑

### Step 3 — Phase 2: 감사 대상 기능 분석 (3~5일)

감사 대상 11개 기능 각각에 아래 분석 루프:

**3-1. 기능별 사용 테이블 식별**
- Entity 클래스 검색 (`@Entity`, `@Table`)
- Repository 인터페이스 매핑 확인
- Service 계층에서 사용하는 테이블 목록화

**3-2. 컬럼별 마스터 매칭 (핵심 작업)**
각 테이블의 VARCHAR/코드성 컬럼마다:
```sql
-- 예: tb_document.status 값이 cont_stat_mst에 있는지
SELECT d.status, COUNT(*) AS doc_cnt,
       CASE WHEN m.cd IS NULL THEN '❌' ELSE '✅' END AS in_master
  FROM tb_document d LEFT JOIN cont_stat_mst m ON d.status = m.cd
 GROUP BY d.status, m.cd;
```
결과를 기능별 섹션에 표로 기록.

**3-3. 코드 하드코딩 스캔**
각 기능별로:
```bash
# Java 리터럴
rg -n --type java '"[A-Z][A-Z0-9_]+"' src/main/java/com/swmanager/system/<해당기능패키지>
# switch 문
rg -n --type java 'switch\s*\(|case ' src/main/java/com/swmanager/system/<해당기능패키지>
# Thymeleaf 하드코딩
rg -n --type html '<option value=' src/main/resources/templates/<해당기능>
```

**3-4. 유형 분류 (기획서 §3 규칙 준수)**
각 사례마다 `primary_type` 1개 + `secondary_tags` 복수 태깅.

**3-4-X. 테이블 미사용 기능 예외 루프 (v2 codex 권장 #2 추가)**

일부 감사 대상(시스템 그래프, 기타 공통 설정 등)은 전용 테이블이 없거나 극소. 이 경우 3-1~3-5의 테이블·컬럼 매칭 전제가 성립하지 않음.

**대체 기준**:
- 기능 섹션에 **"DB 미사용 판정 근거"** 블록 1개 필수: `rg` 결과 + Entity 검색 결과 + `"DB 테이블 없음, {코드/설정 파일 경로}에서만 사용 확인"` 결론 문장
- **코드/설정 파일 근거** 2건 이상 (예: Thymeleaf 템플릿 라인 + application.properties 키 + JS 상수)
- primary_type 분류는 ① (코드 하드코딩) 또는 ④ (마스터 신설) 중 선택
- secondary_tags 는 공란 허용

**3-5. `#1-A` Enum 재평가 (Phase 2 핵심 결과) — v2 점수표화 (codex 권장 #3)**

### 3-5-1. 측정 데이터 수집
- `tb_document.status` / `inspect_report.status` 값 분포 vs `cont_stat_mst` 값 SQL 비교 결과
- `tb_document.doc_type` 값 분포 vs `maint_tp_mst` 값 SQL 비교 결과
- `git log src/main/java/com/swmanager/system/constant/enums/` — `#1-A` 관련 파일 변경 이력
- API 응답 JSON 호출 샘플 (직접 호출 또는 기존 테스트 결과)

### 3-5-2. 점수표

각 Enum(`DocumentStatus`, `DocumentType`)마다 4개 기준별 점수(1~4) 부여 후 판정.

| 기준 | 측정 방법 | 점수 1 (유지) | 점수 2 (선별 롤백) | 점수 3 (후속 패치) | 점수 4 (전체 롤백) |
|------|-----------|--------------|-------------------|-------------------|-------------------|
| 마스터 존재 | SQL LEFT JOIN 일치율 | 0% (마스터 없음) | 10~50% 일치 | 50~99% 일치 | 100% 정확 일치 |
| API 계약 영향 | JSON 응답 스키마 비교 | 응답에 필드 없음 | 응답 변경 0개 필드 | 응답 Enum 변환 | 응답 스키마 전면 변경 |
| 마이그레이션 비용 | 영향 레코드 수 | 0 | ≤10 | ≤100 | >100 |
| 런타임 안정성 | 기존 테스트 영향 | 0 테스트 영향 | ≤3 테스트 수정 | ≤10 수정 | >10 또는 구조 변경 |

### 3-5-3. 판정 규칙
- **4개 점수 합계**로 실행안 선택:
  - 합계 4~6: **유지**
  - 합계 7~10: **선별 롤백** 또는 **후속 패치** (아래 동률 규칙)
  - 합계 11~14: **후속 패치** 또는 **전체 롤백**
  - 합계 15~16: **전체 롤백**
- **동률 시 우선순위 규칙**: **"후속 패치 > 유지 > 선별 롤백 > 전체 롤백"** (인프라 유지 가치 + 최소 침습 원칙)

### 3-5-4. 결과 기록 형식
`utilization-audit.md` 내 `## #1-A Enum 재평가` 섹션에 아래 표 2개 (DocumentStatus / DocumentType) 작성:

```markdown
| 기준 | 측정값 | 점수 |
| 마스터 존재 | cont_stat_mst 100% 일치 | 4 |
| API 계약 | 응답 필드 status 그대로 | 2 |
| 마이그 비용 | 15건 | 3 |
| 런타임 | 테스트 6개 영향 | 3 |
| **합계** | **12** | → **후속 패치 권고** |
```

**3-6. 중간 리뷰 (3일차)**
- Phase 2 섹션 11개 중 7개 이상 완성도 60% 이상
- 미완 시 사용자에 중간 보고 → 상한 연장 여부 판단

**3-7. Phase 2 완료 기준**
- 각 기능 섹션에 기획서 §4 "기능별 최소 산출 기준" 충족:
  - 최소 2개 컬럼 이상 마스터 매칭 분석
  - 최소 1개 구체 SQL/grep 근거
  - primary_type + secondary_tags 태깅 완료

### Step 4 — Phase 3: 로드맵 수립 (0.5일)

**4-1. 발견 사례 집계**
Phase 2 결과를 유형별·우선순위별로 집계:
```markdown
## 집계
- 유형 ② (중복 테이블): N건 → P1
- 유형 ③ (부분 중복): M건 → P1
- 유형 ① (코드 하드코딩): K건 → P2
- 유형 ④ (마스터 신설): L건 → P3
- 유형 ⑤ (레거시 제거): J건 → P3
```

**4-2. 후속 스프린트 분해**
유형·기능·규모 기준으로 스프린트 단위로 묶기. 각 스프린트:
- 이름 (예: `refactor-02-doctype-rollback`)
- 우선순위 (P1/P2/P3)
- 영향 테이블·코드
- 예상 기간
- 의존성 (다른 스프린트 선행 필요 여부)

**4-3. `#1-A` 재평가 결과 반영**
Phase 2 §3-5 결과를 별도 스프린트(`refactor-02-a1-mastermigration` 등)로 편성. `git revert` 단일안 금지, 선별 롤백/후속 패치 옵션 명시.

**4-4. 최종 로드맵 작성** — `docs/plans/data-architecture-roadmap.md`
- 스프린트 실행 순서 (dependency graph)
- 각 스프린트 1줄 요약 + 기획서 초안 링크(후속 작성 대상)

### Step 5 — 산출물 검수 및 감사 문서 갱신

**5-1. 산출물 5개 완성도 체크**
- `db-schema-full.md` — 전 테이블 포함 (Phase 1)
- `master-inventory.md` — 9개 마스터 + 기초 기능 테이블 상세 (Phase 1)
- `utilization-audit.md` — 11개 기능 섹션 + 각 섹션 최소 기준 충족 (Phase 2)
- `roadmap.md` — 최소 3~5개 스프린트 분해 (Phase 3)
- `data-architecture-scan.java` — Git 추적 (Phase 1)

**5-2. 상위 감사 문서 갱신**
`docs/audit/2026-04-18-system-audit.md` 에 "감사 외 후속 스프린트" 섹션에 `data-architecture-audit` 추가 + 산출물 링크.

**5-3. 코드/DB 무변경 확인**
```bash
git status src/main/ src/test/ src/main/resources/
# 변경 0 기대
git diff swdept/sql/  # DB 마이그 무변경 기대
```

---

## 2. 테스트 / 검증 (T#)

본 스프린트는 **문서 산출**만이라 런타임 테스트 불필요. 대신 **문서 검수 체크리스트**.

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | 코드·DB 무변경 | `git status src/ && git diff swdept/sql/` | 변경 0 |
| T2 | 감사 SQL 러너 안전통제 | `data-architecture-scan.java` 파일 grep: `TRANSACTION READ ONLY`, `statement_timeout`, `lock_timeout` | 3 패턴 모두 존재 |
| T3 | 산출물 5개 존재 | `ls docs/audit/db-schema-full.md docs/audit/data-architecture-master-inventory.md docs/audit/data-architecture-utilization-audit.md docs/plans/data-architecture-roadmap.md docs/audit/data-architecture-scan.java` | 5 파일 모두 존재 |
| T4 | Phase 2 섹션 개수 | `utilization-audit.md` H2/H3 카운트 | 11개 기능 섹션 이상 |
| T5 | 기능별 최소 산출 기준 | 각 Phase 2 섹션 내 "### 컬럼 매칭" 표 + "### 코드 근거" 블록 존재 | 각 기능에 둘 다 존재 |
| T6 | `#1-A` 재평가 결론 | `utilization-audit.md` 내 "## #1-A Enum 재평가" 섹션 | 존재 + 4가지 기준 매트릭스 + 결론(유지/선별/패치/전체) |
| T7 | 로드맵 스프린트 수 | `roadmap.md` 내 스프린트 수 | 3개 이상 (P1~P3 각 1건 이상) |
| T8 | PII 마스킹 | `master-inventory.md` 내 `users`/`ps_info` 샘플 | 실제 이메일·주민번호·이름 없음 (마스킹 확인) |
| T9 | 상위 감사 문서 링크 | `2026-04-18-system-audit.md` grep 'data-architecture-audit' | 1회 이상 링크 |
| T10 | **기능별 근거 충족률** (v2 추가) | `utilization-audit.md` 11개 기능 섹션 각각 `파일:라인` 또는 SQL 결과 포맷 근거 1건 이상 | 11개 기능 모두 충족 |
| T11 | **9개 마스터 × 11개 기능 추적성** (v2 추가) | `master-inventory.md`에서 각 마스터의 "사용 컬럼" 섹션 + `utilization-audit.md` 해당 기능 섹션의 교차 링크 | 9개 마스터 전부 최소 1개 기능과 매핑 (또는 "해당 기능 없음" 명시) |
| T12 | **§7-5 안전통제 전체 준수** (v2 추가) | `data-architecture-scan.java` grep 5개 하위통제 키워드: `TRANSACTION READ ONLY`, `statement_timeout`, `lock_timeout`, `LIMIT 1000`(또는 `setMaxRows`), 마스킹 패턴(`LEFT`+`***`) | 5개 모두 존재 + `UPDATE\|DELETE\|DROP\|ALTER\|TRUNCATE\|INSERT` 0건 |

---

## 3. 롤백 전략 (v2 — 단일 방식 통일, codex 권장 #5)

본 스프린트는 문서만 산출. 코드·DB 변경 0. 롤백은 **`git restore -- docs/audit docs/plans` 단일 방식**.

| 상황 | 조치 |
|------|------|
| 감사 SQL 오설계 | 러너 재작성. DB 영향 없음 (read-only 전용, §7-5 통제로 쓰기 불가). |
| 문서 품질 불만족 | 해당 섹션 재작성 (롤백 대신 개선). |
| Phase 2 과소 추정 | 기획서 §4 타임박스 상한(5일)까지 연장. 부족하면 사용자 재협의. |
| 전체 중단 (감사 작업 자체를 철회) | `git restore -- docs/audit docs/plans` 로 미커밋 변경 복원. 커밋된 경우 해당 PR 머지 전이면 PR 닫기. |

**원칙**: `git revert`·`git checkout`·단순 파일 삭제 등은 본 스프린트에서 사용하지 않음. `git restore`로 통일.

---

## 4. 리스크·완화 재확인

기획서 §9 리스크 R-1~R-6 그대로 유효. 추가 없음.

특히 주의:
- **R-3 피상적 분석 방지**: Phase 2 각 섹션에 구체 SQL/grep 결과 **파일:라인 근거 필수**.
- **R-4 #1-A 롤백 권고 신중**: 결정 매트릭스 4개 기준 모두 평가하고 기록. 사용자가 결론만 보지 않고 근거 따라 판단할 수 있도록.

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0 전제 (코드·DB 무변경, 일정 타임박스)
- [ ] §1 Step 1~5 작업 순서 (Phase 0/1/2/3)
- [ ] §1 Step 2-1 SQL 러너 **§7-5 5개 하위통제** (권한/런타임/allowlist/PII/실행환경)
- [ ] §1 Step 3-4-X 테이블 미사용 기능 예외 루프
- [ ] §1 Step 3-5 `#1-A` Enum 재평가 **점수표 + 판정 규칙**
- [ ] §2 **T1~T12 검수 체크리스트 12건**으로 문서 품질 보장
- [ ] §3 롤백은 **`git restore -- docs/audit docs/plans` 단일 방식**
