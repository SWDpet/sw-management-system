---
tags: [dev-plan, sprint, schema, qt_remarks_pattern]
sprint: "qt-remarks-users-link"
status: draft-v1
created: "2026-04-20"
---

# [개발계획서] qt_remarks_pattern × users 연결

- **작성팀**: 개발팀
- **연관 기획서**: [../design-docs/qt-remarks-users-link.md](../design-docs/qt-remarks-users-link.md) (codex 승인 v3)
- **선행 커밋**: `6b8c4a1` (S5 access-log-userid-fix 완료)
- **상태**: 초안 v1 (codex 검토 대기)
- **예상 작업 시간**: 2~3시간

---

## 0. 전제

### 0-1. 현 코드 분석 (실측)

**비고 패턴 사용 흐름**:
```
[사용자] 견적서 작성 화면(quotation-form.html)
   ↓ 패턴 드롭다운에서 선택
[JS] applyRemarksPattern(patternId)  ← 클라이언트
   ↓ pattern.content 를 textarea 에 그대로 복사
[사용자] 필요시 textarea 직접 편집
   ↓ 폼 제출
[서버] quotation.remarks 에 텍스트 저장
   ↓
[preview.html] th:text="${quotation.remarks}" — 단순 출력
```

**핵심 결론**: 견적서의 `quotation.remarks` 는 **선택 시점에 이미 치환된 텍스트의 스냅샷**. preview.html 은 단순 출력만. 따라서 **패턴 fetch 시점에 placeholder 치환** 이 자연스러운 위치.

### 0-2. 스코프 (기획 §1-4)
- ✅ 본체: `user_id` FK + 이름·부서·직급 placeholder
- ❌ 본체 제외: 전화·이메일 placeholder, 마스킹 회귀 정정 (S3-B 후속)

### 0-3. 마스킹 정책 준수
- 본 스프린트는 `users.username/dept_nm/position_title` 만 조회 → **마스킹 영향권 밖** (이름/부서/직급은 마스킹 대상 아님)
- tel/email/mobile 컬럼은 본 스프린트에서 미접근

---

## 1. 작업 순서

### Step 1 — 마이그레이션 SQL (V020)

**파일**: `swdept/sql/V020_qt_remarks_pattern_user_link.sql`

```sql
-- ============================================================
-- V020: qt_remarks_pattern.user_id FK 추가 + 7건 매핑 + 템플릿화
-- Sprint: qt-remarks-users-link (2026-04-20)
-- 근거: docs/design-docs/qt-remarks-users-link.md (v3 승인)
--
-- 멱등성 (NFR-2): expected_cnt 동적 산정으로 재실행 시 0=0 PASS
-- ============================================================

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패 (재실행 신뢰성 보호)
DO $$
BEGIN
  IF to_regclass('public.qt_remarks_pattern_v020_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table qt_remarks_pattern_v020_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 백업 — UPDATE 대상과 동일 범위 (멱등성: 재실행 시 0건 백업)
CREATE TABLE qt_remarks_pattern_v020_backup_:run_id AS
  SELECT * FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7)
     AND (user_id IS NULL OR content NOT LIKE '%{username}%');

-- (2) 컬럼/FK 추가 (멱등 — 첫 실행만 동작)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_name='qt_remarks_pattern' AND column_name='user_id') THEN
    ALTER TABLE qt_remarks_pattern
      ADD COLUMN user_id BIGINT NULL,
      ADD CONSTRAINT fk_qt_remarks_pattern_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;
  END IF;
END $$ LANGUAGE plpgsql;

-- (3) 게이트 + UPDATE + 동등 비교 (S5 패턴, 동적 expected)
DO $$
DECLARE
  expected_cnt bigint;
  actual_cnt   bigint;
  backup_cnt   bigint;
BEGIN
  -- 동적 expected — UPDATE 대상 범위와 정확히 동일 SELECT
  SELECT COUNT(*) INTO expected_cnt FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7)
     AND (user_id IS NULL OR content NOT LIKE '%{username}%');

  -- backup == expected (race condition 방지)
  SELECT COUNT(*) INTO backup_cnt FROM qt_remarks_pattern_v020_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  WITH applied AS (
    UPDATE qt_remarks_pattern SET
      user_id = CASE pattern_id
        WHEN 1 THEN 6   WHEN 2 THEN 17  WHEN 3 THEN 16
        WHEN 4 THEN 6   WHEN 5 THEN 6   WHEN 6 THEN 6   WHEN 7 THEN 6
      END,
      content = CASE pattern_id
        WHEN 1 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 2 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-9894  M.010-9755-1316  F.053-817-9987  E-mail : leeds@uitgis.com)'
        WHEN 3 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8072  M.010-2815-0957  F.02-561-9792)'
        WHEN 4 THEN '1. 본 견적의 유효기간은 발급일로부터 30일 입니다.' || E'\n' ||
                    ' ※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 5 THEN '※ 무상하자보수 기간 : 구매일로부터 1년' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 6 THEN '※ 상기 견적은 제품 공급가에 10% 요율을 적용한 금액입니다.' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 7 THEN '※ 상기 제품은 조달청 디지털몰에 등록되어 있습니다.' || E'\n' ||
                    '※ 상기 견적은 제품 공급가에 16% 요율을 적용한 금액입니다.' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
      END
    WHERE pattern_id IN (1,2,3,4,5,6,7)
      AND (user_id IS NULL OR content NOT LIKE '%{username}%')   -- 멱등성 키 (백업과 동일)
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM applied;

  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: linked % rows (backup=qt_remarks_pattern_v020_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (4) 사후 검증 — 7건 모두 user_id 매핑됨
DO $$
DECLARE missing bigint;
BEGIN
  SELECT COUNT(*) INTO missing FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7) AND user_id IS NULL;
  IF missing <> 0 THEN
    RAISE EXCEPTION 'HALT post: % rows missing user_id', missing;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
```

**멱등성 동작**:
- 첫 실행: backup=7, expected=7, actual=7 → PASS, NOTICE "linked 7 rows"
- 재실행: backup=0, expected=0, actual=0 → PASS, NOTICE "linked 0 rows"
- T2 (재실행) 기대결과 정합: "0 row UPDATE + 백업 0건"

### Step 2 — RemarksPattern 엔티티

```java
@Entity
@Table(name = "qt_remarks_pattern")
@Getter @Setter
public class RemarksPattern {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long patternId;

    @Column(name = "pattern_name", nullable = false, length = 100)
    private String patternName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** S3 추가: 담당자 user_id (NULL 허용 — 향후 신규 등록 시 미선택 케이스 대비) */
    @Column(name = "user_id")
    private Long userId;
}
```

→ 단순 `Long userId` 로 보관 (User 엔티티 fetch 부담 없음).

### Step 3 — RemarksRenderer 유틸 신규

**파일**: `src/main/java/com/swmanager/system/quotation/service/RemarksRenderer.java`

```java
@Component
public class RemarksRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(username|dept_nm|position_title)\\}");

    private final UserRepository userRepository;

    public RemarksRenderer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * content 의 {username} {dept_nm} {position_title} 를 user_id 기반 users 값으로 치환.
     *
     *  - userId == null  → content 원문 반환 (placeholder 미치환, fallback)
     *  - users 미존재    → content 원문 반환
     *  - users 존재      → 각 placeholder 를 해당 컬럼값으로 치환. NULL 컬럼은 빈 문자열.
     *  - 미정의 placeholder (예: {phone}) → 보존 (원문 그대로)
     */
    public String render(String content, Long userId) {
        if (content == null || content.isEmpty()) return content;
        if (userId == null) return content;

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return content;

        User u = userOpt.get();
        Matcher m = PLACEHOLDER.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String token = m.group(1);
            String value = switch (token) {
                case "username"        -> nullToEmpty(u.getUsername());
                case "dept_nm"         -> nullToEmpty(u.getDeptNm());
                case "position_title"  -> nullToEmpty(u.getPositionTitle());
                default                -> m.group();  // 보존
            };
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String nullToEmpty(String v) { return v == null ? "" : v; }
}
```

**플레이스홀더 정책**:
- 정의된 토큰 (`username`/`dept_nm`/`position_title`) — 치환
- 미정의 토큰 (`{phone}` 등) — Pattern 매칭에 안 잡혀서 자동 보존

### Step 4 — QuotationService.getRemarksPatterns() 변경

```java
@Transactional(readOnly = true)
public List<RemarksPatternDto> getRemarksPatterns() {
    List<RemarksPattern> raw = remarksPatternRepository.findAllByOrderBySortOrderAscPatternNameAsc();
    return raw.stream()
        .map(p -> {
            RemarksPatternDto dto = RemarksPatternDto.from(p);
            dto.setRenderedContent(remarksRenderer.render(p.getContent(), p.getUserId()));
            return dto;
        })
        .toList();
}
```

**RemarksPatternDto 신규**: 기존 엔티티 필드 + `renderedContent` (치환 결과)

```java
public class RemarksPatternDto {
    private Long patternId;
    private String patternName;
    private String content;          // 원문 (편집용)
    private String renderedContent;  // 치환 결과 (textarea 자동 입력용)
    private Integer sortOrder;
    private Long userId;
    // + getter/setter, factory from(RemarksPattern)
}
```

### Step 5 — 컨트롤러 응답 타입 변경 + 화면 JS 수정

**Controller**: `getRemarksPatterns()` return type → `List<RemarksPatternDto>`

**quotation-form.html JS** `applyRemarksPattern`:
```js
function applyRemarksPattern(patternId) {
    if (!patternId) return;
    var pattern = remarksPatterns.find(p => String(p.patternId) === String(patternId));
    if (pattern) {
        // 기존: pattern.content (placeholder 그대로)
        // 신규: 서버 치환된 결과 우선, 없으면 원문 (하위호환)
        document.getElementById('quotationRemarks').value =
            pattern.renderedContent || pattern.content;
    }
}
```

### Step 6 — 비고 패턴 관리 화면 (`remarks-pattern-list.html`) 수정

신규/수정 모달에 **담당자 선택 드롭다운** 추가 + **사용 가능 placeholder 안내**.

```html
<div class="fg">
    <label>담당자 (선택, 선택 시 placeholder 치환 활성화)</label>
    <select id="mUserId">
        <option value="">선택 없음 (placeholder 미치환)</option>
        <!-- AJAX 로드: GET /api/users/all-with-disabled -->
    </select>
    <small style="color:#A8A29E; font-size:0.72rem;">
        사용 가능 placeholder: {username}, {dept_nm}, {position_title}
    </small>
</div>
```

**신규 API**: `GET /api/users/all-with-disabled` — **활성 + 비활성 사용자 모두** 반환 (userid, username, enabled).
> 이유 (R6 완화): 기존 패턴이 비활성 사용자(`enabled=false`)를 참조 중인 경우, 편집 모달에서 그 사용자가 드롭다운에 노출되지 않으면 "선택 없음" 으로 잘못 저장될 위험.
> → 비활성 사용자는 드롭다운에 표시하되 라벨에 "(비활성)" 접미사 추가하여 운영자 인지 가능하도록.
>
> 활성만 필요한 다른 화면용 별도 엔드포인트 `GET /api/users/active` 도 함께 추가 (선택 사항).

### Step 7 — 단위 테스트

**파일**: `src/test/java/com/swmanager/system/quotation/service/RemarksRendererTest.java`

5+ 케이스:
1. `nullContent_returnsNull`
2. `nullUserId_returnsContentAsIs`
3. `userIdNotInDb_returnsContentAsIs`
4. `validUserId_substitutesAllPlaceholders`
5. `nullDeptNm_substitutesEmptyString`
6. `unknownPlaceholder_preserved` (`{phone}` 보존)
7. `multipleOccurrences_allReplaced`

`@ExtendWith(MockitoExtension.class)` + `@Mock UserRepository` + 직접 instantiate.

### Step 8 — V020 실행

S5 패턴 따라 `access_log_userid_apply.java` 와 동일한 JDBC 러너 사용 (재사용 가능).

### Step 9 — 빌드 / 재기동 / 회귀 스모크 (T1~T9 전건)

| T# | 검증 방법 | 기대 |
|----|----------|------|
| T1 | V020 첫 실행 (Step 8) | NOTICE "linked 7 rows" |
| T2 | V020 재실행 (별 RUN_ID) | NOTICE "linked 0 rows" + 백업 0건 |
| T3 | `GET /api/quotation/remarks-patterns` 응답 검사 (pattern_id=1) | renderedContent 에 "SW지원부 박욱진" 포함 |
| T4 | 동일 응답 (pattern_id=2) | renderedContent 에 "GIS사업부 이동수" 포함 |
| T5 | DB로 박욱진 dept_nm 임시 변경 → API 재호출 → 원복 | renderedContent 가 새 부서명으로 변경됨 |
| T6 | DB로 박욱진 enabled=false 임시 변경 → API 재호출 → 원복 | renderedContent 정상 치환 (정책 §T6: enabled=false 도 정보 사용) |
| T7 | 임시 패턴에 `{phone}` 포함 INSERT → API 호출 → 임시 패턴 DELETE | `{phone}` 그대로 보존 |
| T8 | 비고 패턴 관리 화면에서 신규 패턴 등록 (담당자 선택) | DB에 user_id 저장 |
| T9 | 견적서 작성 화면 → 패턴 1 선택 → textarea 값 비교 | "※ 담당자 : SW지원부 박욱진 ..." 형태 (placeholder 미잔존, 라벨/괄호/구분자 = 백업 본 동일 패턴) |

- 자동: `./mvnw test` BUILD SUCCESS
- 수동: T3/T4/T9 (브라우저), T5/T6/T7 (DB 임시 변경 → API 호출 → 원복)
- T2 검증은 Step 8 직후 즉시 실행

### Step 10 — 커밋 + 푸시

---

## 2. 테스트 매핑 (FR/NFR/T)

| 기획 ID | 검증 위치 |
|--------|----------|
| FR-1 (FK 추가) | V020 §(2) |
| FR-2/3 (7건 매핑) | V020 §(3), 사후검증 §(4) |
| FR-4 (엔티티 userId) | RemarksPattern.java |
| FR-5 (placeholder) | RemarksRenderer.java + RemarksRendererTest |
| FR-6 (런타임 치환) | QuotationService.getRemarksPatterns() + JS |
| FR-7 (NULL fallback) | RemarksRenderer.render(c, null) |
| FR-8 (UI 드롭다운) | remarks-pattern-list.html |
| NFR-1 (트랜잭션) | V020 BEGIN/COMMIT + DO blocks |
| NFR-2 (멱등성) | expected 동적 산정 |
| NFR-3 (회귀) | T9 (수동) |
| NFR-4 (미정의 placeholder 보존) | RemarksRendererTest §6 |
| NFR-5 (≥5 케이스) | 7 케이스 작성 |

---

## 3. 롤백 전략 (3-단계 드릴)

### 3-1. 운영 영향 최소화 원칙
- 코드와 DB 모두 단일 커밋 (S5 패턴) — `git revert <SHA>` 한 번으로 코드 회복
- DB는 백업 테이블이 1차 안전망

### 3-2. 단계별 절차

**[L1] 코드만 롤백 (DB 변경 보존)** — 가장 빈번한 시나리오
```bash
# 조건: V020 적용 OK 인데 RemarksRenderer/JS 등 코드에서 회귀 발견
git revert <S3 커밋 SHA>
bash server-restart.sh
# 검증: GET /api/quotation/remarks-patterns 응답에 placeholder 그대로 노출 확인
#       (기존 코드는 renderedContent 미사용 → content (template)을 그대로 textarea 에 복사)
#       → 일시적으로 사용자가 패턴 적용 시 "{username}" 같은 템플릿이 보임. 즉시 후속 패치 필요.
```
**중요 경고**: L1 단독 롤백 시 코드는 placeholder 인식 못 하므로 사용자 패턴 적용에 영향. **L2 와 함께 수행 권장**.

**[L2] DB 컨텐츠만 롤백 (스키마 보존)** — 코드는 새 버전 유지하면서 데이터만 원복
```sql
-- swdept/sql/V020_rollback_data.sql
BEGIN;
DO $$
DECLARE bk text;
BEGIN
  SELECT 'qt_remarks_pattern_v020_backup_' || (regexp_match(table_name, '_(\d{8}_\d{6})$'))[1]
    INTO bk
    FROM information_schema.tables
   WHERE table_name LIKE 'qt_remarks_pattern_v020_backup_%'
   ORDER BY table_name DESC LIMIT 1;
  EXECUTE format('UPDATE qt_remarks_pattern p SET content = b.content, user_id = NULL
                  FROM %I b WHERE b.pattern_id = p.pattern_id', bk);
END $$;
COMMIT;
```
- **검증**: `SELECT pattern_id, user_id, LEFT(content, 30) FROM qt_remarks_pattern WHERE pattern_id IN (1..7);` → user_id NULL + 원본 텍스트 복원

**[L3] 스키마까지 롤백 (FK + 컬럼 DROP)** — 최후 수단
```sql
-- swdept/sql/V020_rollback_full.sql
BEGIN;
ALTER TABLE qt_remarks_pattern DROP CONSTRAINT IF EXISTS fk_qt_remarks_pattern_user;
ALTER TABLE qt_remarks_pattern DROP COLUMN IF EXISTS user_id;
COMMIT;
```
- **선행 조건**: L2 완료 (데이터 컨텐츠 원복) — 그렇지 않으면 templated content 만 남고 user_id 사라짐 → 더 큰 혼란

### 3-3. 배포 순서
1. **앱 배포 → DB V020** (적용 시): 새 코드는 user_id 컬럼 미존재 시 `findById(null)` 분기로 안전 (RemarksRenderer §userId == null → content 원문 반환)
2. **DB V020 → 앱 배포** (적용 시): V020 후 잠시 구 코드는 placeholder 인식 못해 화면에 `{username}` 노출 가능 → **앱 배포 우선** 권장
3. 롤백 시: **L2 (데이터) 먼저 → L1 (코드)** 순서로 무회귀 보장

### 3-4. 태그
- `s3-qt-remarks-link-v1` — 단일 SHA 참조용

---

## 4. 리스크

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | UPDATE 후 견적서 작성 화면에서 placeholder 가 그대로 노출 | renderedContent 추가 + JS 치환 로직 — 본 계획에 포함 |
| R2 | RemarksRenderer 가 미정의 placeholder 를 잘못 처리 | Pattern 매칭에 정확히 3개만 등록 → 나머지는 자동 보존, **T7 케이스**로 검증 (`{phone}` 보존) |
| R3 | 사용자 정보 변경 시 패턴 사용처에 즉시 반영 | 매번 fetch 시 render → 항상 최신값 (스냅샷 X) |
| R4 | DTO 도입으로 기존 컨트롤러 응답 타입 호환 | API 응답 JSON 키는 동일 (patternId/patternName/content/sortOrder) + 추가 (renderedContent/userId) → 하위 호환 |
| R5 | `@RestController` 가 아닌 `ResponseBody` 단순 직렬화이므로 DTO 변경 시 JS 가 불일치 | JS 수정 본 계획에 포함 |
| **R6** | **비활성 사용자(`enabled=false`) 참조 패턴이 편집 모달에서 유실** — 활성만 드롭다운에 노출하면, 비활성 사용자를 참조하던 패턴 편집 시 운영자가 "선택 없음"으로 잘못 저장할 수 있음 | Step 6: `GET /api/users/all-with-disabled` 사용 + 비활성은 라벨에 "(비활성)" 접미사. 사용자가 명시적으로 다시 선택해야 변경됨 |
| **R7** | **PDF/미리보기에서 placeholder 가 노출되는 회귀** — 본 계획은 패턴 fetch 시점 치환이라 textarea 에서 끝남. 그러나 만약 사용자가 textarea 편집 없이 패턴 적용 후 즉시 저장하면 quotation.remarks 에 치환된 결과가 들어감 → preview 정상. 다만 **API 가 placeholder 그대로 응답하는 회귀 시 사용자 화면에 노출** | T7 검증 + L1 롤백 시 즉시 인지 (Step 9 T9) + RemarksRendererTest 로 자동 검증 |
| **R8** | **DB 직렬 응답 필드 추가가 외부 클라이언트 호환 영향** | API 변경은 추가 only (patternId/patternName/content/sortOrder 유지) → 기존 호출자 무영향. 외부 노출 API 아님 (관리자 내부) |

---

## 5. 릴리스 체크리스트

- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] Step 1~9 구현
- [ ] codex 구현 검증
- [ ] 사용자 확인
- [ ] git commit + push + 태그
