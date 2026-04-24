---
tags: [dev-plan, sprint, refactor, security, masking]
sprint: "users-masking-regression-fix"
short_alias: "S3-B"
status: draft-v2
created: "2026-04-20"
---

# [개발계획서] users 마스킹 회귀 정정 + 가드

- **작성팀**: 개발팀
- **연관 기획서**: [../plans/users-masking-regression-fix.md](../plans/users-masking-regression-fix.md) (codex 승인 v4)
- **선행 커밋**: `acbfa3c` (S3 완료)
- **상태**: v2 (codex 재검토 — 5건 보강 완료)
- **예상 작업 시간**: 1~2시간

---

## 0. 사전조사 결과 (2026-04-20)

### 0-1. 마이페이지 마스킹 적용 여부
**파일**: `src/main/resources/templates/mypage.html`

```html
<input type="text" name="tel" th:value="${user.tel}" required>
<input type="text" name="mobile" th:value="${user.mobile}">
<input type="email" name="email" th:value="${user.email}" required>
<input type="text" name="ssn" th:value="${user.ssn}" maxlength="14">
<input type="text" name="address" th:value="${user.address}">
```

→ **마이페이지에 SensitiveMask 미적용**. raw DB 값 직접 표시.
→ 박욱진(user_seq=6) 마스킹 회귀 데이터의 화면 표시는 **DB 값 그대로** (`070-****-8093` 등)
→ 사용자가 마이페이지에서 보는 값 = DB 저장값 동일

### 0-2. 견적서/문서관리 SensitiveMask 적용 여부
- `src/main/resources/templates/quotation/`: SensitiveMask 호출 **0건** ✅
- `src/main/resources/templates/document/`: SensitiveMask 호출 **0건** ✅

→ **정책(unmasked) OK** — 변경 불필요

### 0-3. ⚠ 새 발견 사항 → 사용자 결정 필요

마이페이지가 마스킹 미적용 상태 → 정책 §1-1 ("마이페이지 = 마스킹 표시") 위반.

**OQ-1**: 본 S3-B 스코프에 마이페이지 화면 마스킹 적용 포함?
- ✅ **사용자 확정 (2026-04-20)**: **B 채택** — 본 sprint 는 회귀 정정 + 가드만. 마이페이지 화면 마스킹은 **별도 후속 sprint (S3-C 가칭)**

**근거**: 가드는 입력 검증 — 화면 표시 정책과 독립. 본 sprint 의 핵심은 "마스킹 값이 DB로 들어가지 않게" 차단이므로 가드만으로 충분.

### 0-4. 회귀 메커니즘 재해석

기획서 §1-3 추정 시나리오 ("화면 마스킹 → submit → DB 회귀") 는 **현재 코드로는 발생 불가** (마이페이지 마스킹 미적용 상태). 박욱진 회귀의 실제 원인은 다음 중 하나로 추정:

1. **과거 다른 시점**: SensitiveMask 도입 직후 마이페이지에 임시 적용했다가 회귀 발생 → 마스킹 표시는 롤백, 회귀 데이터만 잔존
2. **다른 입력 경로**: 관리자 화면(admin-user-list) 의 인라인 편집 + 마스킹 표시 + submit
3. **수동 INSERT/UPDATE**: 개발/테스트 중 직접 SQL 실행

**의의**: 원인 모르더라도 **가드는 미래 회귀 차단**에 충분. 본 sprint 핵심 가치 유지.

---

## 1. 작업 순서

### Step 1 — V021 마이그레이션 SQL

**파일**: `swdept/sql/V021_users_masking_regression_fix.sql`

```sql
-- ============================================================
-- V021: users user_seq=6 (박욱진) 마스킹 회귀 데이터 정정
-- Sprint: users-masking-regression-fix (S3-B, 2026-04-20)
-- 근거: docs/product-specs/users-masking-regression-fix.md (v4 승인)
--
-- 정정 매핑:
--   user_seq=6
--     tel: '070-****-8093' → '070-7113-8093'
--     mobile: '01030562678' → '010-3056-2678'  (hyphen 통일)
--     email: 'u***@uitgis.com' → 'ukjin914@uitgis.com'
--
-- 멱등성: 정정 후 재실행은 expected_cnt=0 actual_cnt=0 PASS
-- ============================================================

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패
DO $$
BEGIN
  IF to_regclass('public.users_v021_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table users_v021_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 백업 (대상 row 1건만)
CREATE TABLE users_v021_backup_:run_id AS
  SELECT user_seq, userid, tel, mobile, email
    FROM users
   WHERE user_seq = 6
     AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678');

-- (2) 게이트 + UPDATE + 동등 비교
DO $$
DECLARE
  expected_cnt bigint;
  actual_cnt   bigint;
  backup_cnt   bigint;
BEGIN
  SELECT COUNT(*) INTO expected_cnt FROM users
   WHERE user_seq = 6
     AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678');

  SELECT COUNT(*) INTO backup_cnt FROM users_v021_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  WITH applied AS (
    UPDATE users SET
      tel = '070-7113-8093',
      mobile = '010-3056-2678',
      email = 'ukjin914@uitgis.com'
    WHERE user_seq = 6
      AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678')
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM applied;

  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: fixed % users (backup=users_v021_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (3) 사후 검증 — user_seq=6 의 정정값 확인
DO $$
DECLARE
  cur_tel text;
  cur_mobile text;
  cur_email text;
BEGIN
  SELECT tel, mobile, email INTO cur_tel, cur_mobile, cur_email
    FROM users WHERE user_seq = 6;

  IF cur_tel <> '070-7113-8093' OR cur_mobile <> '010-3056-2678' OR cur_email <> 'ukjin914@uitgis.com' THEN
    RAISE EXCEPTION 'HALT post: user_seq=6 not properly fixed (tel=%, mobile=%, email=%)',
      cur_tel, cur_mobile, cur_email;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
```

### Step 2 — MaskingDetector 유틸 신규

**파일**: `src/main/java/com/swmanager/system/util/MaskingDetector.java`

```java
package com.swmanager.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 사용자 입력값에 마스킹 패턴이 포함되어 있는지 감지 (S3-B).
 *
 * 정책: DB 회귀 방지 — 마이페이지 등 폼 submit 시 마스킹된 값이 DB로
 *       다시 저장되는 현상을 차단.
 *
 * 감지 방식:
 *   1차) FR-8 동등 비교 — SensitiveMask(currentDb) == input → 100% 회귀
 *   2차) 컬럼별 정규식 fallback (tel/mobile/email/ssn/address)
 */
@Component
public class MaskingDetector {

    private static final Pattern TEL_MASK = Pattern.compile("^\\d{2,4}-\\*{4}-\\d{4}$");
    private static final Pattern EMAIL_MASK = Pattern.compile("^.{1,5}\\*{3,}@.+$");
    private static final Pattern SSN_MASK = Pattern.compile("^\\d{6}-\\d\\*{6}$");
    private static final Pattern GENERIC_MASK = Pattern.compile("\\*{3,}");

    @Autowired private SensitiveMask sensitiveMask;

    /** tel/mobile 통합 (포맷 동일) */
    public boolean isMaskedTel(String input, String currentDbValue) {
        if (input == null) return false;
        if (currentDbValue != null && sensitiveMask.tel(currentDbValue).equals(input)) return true;
        return TEL_MASK.matcher(input).matches();
    }

    public boolean isMaskedEmail(String input, String currentDbValue) {
        if (input == null) return false;
        if (currentDbValue != null && sensitiveMask.email(currentDbValue).equals(input)) return true;
        return EMAIL_MASK.matcher(input).matches();
    }

    public boolean isMaskedSsn(String input, String currentDbValue) {
        if (input == null) return false;
        if (currentDbValue != null && sensitiveMask.ssn(currentDbValue).equals(input)) return true;
        return SSN_MASK.matcher(input).matches();
    }

    public boolean isMaskedAddress(String input, String currentDbValue) {
        if (input == null) return false;
        // address 는 자유 입력 — FR-8 동등 비교를 1차로
        if (currentDbValue != null && sensitiveMask.address(currentDbValue).equals(input)) return true;
        // 2차 보수: 3자 이상 *
        return GENERIC_MASK.matcher(input).find();
    }
}
```

### Step 3 — MyPageController 가드 추가

**파일**: `src/main/java/com/swmanager/system/controller/MyPageController.java`

기존 `updateMyInfo()` 의 `user.setXxx(input)` 부분을 가드 거친 값으로 대체:

```java
@Autowired private MaskingDetector maskingDetector;

// updateMyInfo 메서드 내부 (user.setXxx 부분 변경)

List<String> blockedFields = new ArrayList<>();

if (maskingDetector.isMaskedTel(tel, user.getTel())) {
    blockedFields.add("tel");
} else {
    user.setTel(tel);
}
if (maskingDetector.isMaskedTel(mobile, user.getMobile())) {
    blockedFields.add("mobile");
} else {
    user.setMobile(mobile);
}
if (maskingDetector.isMaskedEmail(email, user.getEmail())) {
    blockedFields.add("email");
} else {
    user.setEmail(email);
}
if (maskingDetector.isMaskedSsn(ssn, user.getSsn())) {
    blockedFields.add("ssn");
} else {
    user.setSsn(ssn);
}
if (maskingDetector.isMaskedAddress(address, user.getAddress())) {
    blockedFields.add("address");
} else {
    user.setAddress(address);
}

// 가드 통과 필드들 (마스킹 무관)
user.setDeptNm(deptNm);
user.setTeamNm(teamNm);
user.setPositionTitle(positionTitle);
user.setCertificate(certificate);
user.setTechGrade(techGrade);
user.setTasks(tasks);

// blockedFields 가 있으면 WARN 로그 + 토스트 (값은 절대 미포함)
if (!blockedFields.isEmpty()) {
    log.warn("MASKING_GUARD_BLOCKED: userid={} fields={}", user.getUserid(), blockedFields);
    rttr.addFlashAttribute("warningMessage",
        "다음 필드는 마스킹된 값이 감지되어 변경되지 않았습니다: " + String.join(", ", blockedFields));
}
```

### Step 4 — SensitiveMask javadoc 강화

**파일**: `src/main/java/com/swmanager/system/util/SensitiveMask.java`

기획서 §6-4 의 javadoc 블록 추가 (정책 명시).

### Step 5 — DESIGN_SYSTEM.md 마스킹 정책 절 추가

**파일**: `docs/DESIGN.md`

기획서 §1-1 정책 표 + 가드 메커니즘 설명 추가.

### Step 6 — 단위 테스트 + 컨트롤러 행위 검증

#### 6-1. MaskingDetectorTest (단위)
**파일**: `src/test/java/com/swmanager/system/util/MaskingDetectorTest.java`

9 케이스:
1. tel 정상 입력 → false
2. tel 마스킹 패턴 입력 (`010-****-1234`) → true
3. tel FR-8 동등 비교 (`SensitiveMask.tel(currentDb) == input`) → true
4. email 정상 → false
5. email 마스킹 (`u***@x.com`) → true
6. ssn 마스킹 (`901201-1******`) → true
7. address 정상 → false
8. address 마스킹 (`*** 강남구`) → true (보수 정규식)
9. null/empty 입력 → false (NPE 없음)

#### 6-2. MyPageControllerGuardTest (컨트롤러 행위 — FR-3/NFR-3/NFR-5)
**파일**: `src/test/java/com/swmanager/system/controller/MyPageControllerGuardTest.java`

`@WebMvcTest(MyPageController.class)` 또는 `@SpringBootTest + MockMvc`:

```java
@Test void allMaskedFields_keepsDbValue_addsWarningMessage() throws Exception {
    // tel/mobile/email 모두 마스킹 패턴 → DB 미저장 + warningMessage 에 3개 필드 모두 명시
    mvc.perform(post("/mypage/update")
        .param("tel", "070-****-8093")
        .param("mobile", "010-****-2678")
        .param("email", "u***@uitgis.com")
        .param("deptNm", "SW지원부").param("teamNm", "X").param("ssn",""))
       .andExpect(status().is3xxRedirection())
       .andExpect(flash().attribute("warningMessage", containsString("tel")))
       .andExpect(flash().attribute("warningMessage", containsString("mobile")))
       .andExpect(flash().attribute("warningMessage", containsString("email")));
    // verify(userRepository, never()).save(argThat(u -> u.getTel().contains("*")))
}

@Test void mixedMaskedAndNormal_savesNonMaskedOnly() {
    // tel 마스킹 + email 정상 → email만 저장, tel은 유지
}

@Test void allNormal_savesAll_noWarning() {
    // 모두 정상 입력 → 모두 저장 + warningMessage 없음
}

@Test void blockedFields_logsWarnWithUseridAndFieldsOnly() {
    // ListAppender 로 WARN 로그 검증: userid + 필드명 포함, 값 미포함
}
```

→ **최소 4 케이스** (NFR-4 충족 + 컨트롤러 행위 + warningMessage + 로그)

### Step 7 — V021 실행 + 검증

**러너**: `docs/exec-plans/access-log-userid-apply.java` 재사용 (S5 작성). 인자: SQL 파일 경로 + RUN_ID.
> 위치: `C:\Users\ukjin\sw-management-system\docs\dev-plans\access-log-userid-apply.java`

```bash
RUN_ID=$(date +%Y%m%d_%H%M%S)
WIN_DB_PWD=$(powershell.exe -NoProfile -Command "[Environment]::GetEnvironmentVariable('DB_PASSWORD', 'User')")
# /tmp 에 컴파일된 클래스 활용 (S5 작성 시 컴파일됨)
DB_PASSWORD="$WIN_DB_PWD" java -cp ".;<jar>" -Dfile.encoding=UTF-8 access_log_userid_apply \
  swdept/sql/V021_users_masking_regression_fix.sql "$RUN_ID"
```
**확장**: 멱등성 검증을 위해 새 RUN_ID 로 1회 더 실행 — `linked 0 users` NOTICE 확인.

### Step 8 — 빌드 + 재기동 + 회귀 스모크

| T# | 검증 |
|----|------|
| T1 | V021 첫 실행 NOTICE "PASS: fixed 1 users" |
| T2 | V021 재실행 → 0 row + 백업 0건 (멱등성) |
| T3 | DB SELECT — user_seq=6 의 tel/mobile/email 정정값 확인 |
| T4 | 마이페이지 정상 입력 → 정상 저장 |
| T5 | 마이페이지 tel='070-****-1234' → 가드 차단 + 토스트 |
| T6 | 일부 필드만 마스킹 (tel만) → tel 유지, email은 변경 |
| T7 | 마이페이지 NULL/빈 입력 → 정상 (NPE 없음) |
| T8 | mvn test → MaskingDetectorTest BUILD SUCCESS |

### Step 9 — 커밋 + 푸시 + 태그

태그: `s3b-masking-regression-v1`

---

## 2. 매핑 매트릭스 (FR/NFR/T) — Step 번호 정합화

| 기획 ID | 내용 | 구현 Step |
|--------|------|----------|
| FR-1 (V021) | 마이그레이션 SQL | **Step 1** |
| FR-2/3 (가드+토스트+로그) | MyPageController 가드 | **Step 3** |
| FR-4 (컬럼별 정규식) | MaskingDetector | **Step 2** |
| FR-5 (5컬럼) | tel/mobile/email/ssn/address | **Step 3** |
| FR-6 (정책 문서화) | SensitiveMask javadoc + DESIGN_SYSTEM.md | **Step 4, 5** |
| FR-7 (견적서/문서 unmasked) | 사전조사 통과 | **Step 0-2 (이미 통과)** |
| FR-8 (동등 비교) | MaskingDetector 1차 로직 | **Step 2** |
| NFR-1 (트랜잭션/백업/검증) | V021 SQL | **Step 1** |
| NFR-2 (멱등성) | expected/actual 동등 | **Step 1** |
| NFR-3 (필드 독립) | 가드 컬럼별 분리 | **Step 3** |
| NFR-4 (≥5 케이스) | MaskingDetectorTest | **Step 6** |
| NFR-5 (warningMessage) | rttr.addFlashAttribute | **Step 3** |
| NFR-6 (다른 사용자 별도 처리) | 본 sprint 박욱진만 | **Step 1 WHERE 조건** |

---

## 3. 롤백 전략 (S5/S3 패턴)

### 3-1. RUN_ID 치환 규칙
SQL 의 `:run_id` placeholder 는 실행 전 sed 치환:
```bash
RUN_ID=$(date +%Y%m%d_%H%M%S)
sed "s/:run_id/${RUN_ID}/g" V021_users_masking_regression_fix.sql > /tmp/V021_applied_${RUN_ID}.sql
```
실행 러너는 **Java JDBC apply** (`access_log_userid_apply.java` 와 동일 형태 — 파일 인자 + RUN_ID 인자) 재사용. 단일 `Statement.execute()` 로 전체 트랜잭션 실행, NOTICE 캡처.

### 3-2. 태그
- `s3b-masking-regression-v1`

### 3-3. 단계별 롤백 절차

**L1 — 코드만 (가드 제거, 데이터는 정정 상태 유지)**
```bash
git revert <S3-B-SHA>
bash server-restart.sh
```

**L2 — 데이터 RESTORE (구체 SQL)**
```sql
BEGIN;
-- 가장 최근 백업 자동 탐색
DO $$
DECLARE bk text;
BEGIN
  SELECT 'users_v021_backup_' || (regexp_match(table_name, '_(\d{8}_\d{6})$'))[1]
    INTO bk FROM information_schema.tables
   WHERE table_name LIKE 'users_v021_backup_%'
   ORDER BY table_name DESC LIMIT 1;
  IF bk IS NULL THEN
    RAISE EXCEPTION 'HALT: no backup table found';
  END IF;
  EXECUTE format(
    'UPDATE users u SET tel=b.tel, mobile=b.mobile, email=b.email
       FROM %I b WHERE u.user_seq = b.user_seq', bk);
END $$ LANGUAGE plpgsql;
COMMIT;
```
파일 위치: `swdept/sql/V021_rollback_data.sql` ✅ **작성 완료**

**L3 — 전체 (L1 + L2)**: 실행 순서 L2 → L1 (코드 revert 전에 데이터 복원)

**검증**:
- L2 후: `SELECT tel, mobile, email FROM users WHERE user_seq=6;` → 백업 본 값(`070-****-8093` 등) 확인
- L3 후: `git log --oneline -1` 에 revert 커밋 확인 + 위 SELECT 동일

---

## 4. 리스크 (개발 단계 추가)

기획서 R1~R9 외 본 개발 단계에서 추가로 식별된 운영 리스크:

| ID | 리스크 | 완화 |
|----|-------|------|
| R10 | **JDBC 러너 RUN_ID 중복 위험** | V021 §(0) 게이트가 동명 백업 테이블 존재 시 즉시 EXCEPTION (S5 동일 패턴) |
| R11 | **백업 테이블 누적** (반복 실행 시) | DROP 절차 운영 가이드 (§5-4 기획서 참조). 본 sprint 후 7일 이상 안정 시 백업 DROP 권장 |
| R12 | **Spring Bean 순환 참조** — MaskingDetector → SensitiveMask → 만약 양방향 의존이면 위험 | SensitiveMask 는 stateless 유틸. MaskingDetector 만 SensitiveMask 참조하므로 단방향. 검증: Step 8 mvn test 시 ApplicationContext 부팅 성공 확인 |
| R13 | **컨트롤러 테스트 환경 의존** — `@WebMvcTest` 가 Spring Security 충돌 가능 | 필요 시 `@WithMockUser` + `@MockBean SecurityFilterChain` 사용. 또는 `@SpringBootTest` 로 분리하고 `@EnabledIfEnvironmentVariable("RUN_DB_TESTS","true")` 마킹 (S5 패턴 따름) |
| R14 | **마이페이지 마스킹 미적용 상태에서 가드만 동작 시 사용자 혼란** | 본 sprint 는 가드만 (사용자 결정 OQ-1 B). 화면 마스킹은 별도 sprint. 본 가드는 **현재 화면이 unmasked → 사용자가 정상 값을 보고 정상 입력하는 정상 흐름에서는 발동 0회**. 회귀 시점 입력에서만 발동. |

---

## 5. 릴리스 체크리스트

- [ ] OQ-1 사용자 답변
- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] Step 2~9 구현
- [ ] codex 구현 검증
- [ ] 사용자 확인
- [ ] git commit + push + 태그
