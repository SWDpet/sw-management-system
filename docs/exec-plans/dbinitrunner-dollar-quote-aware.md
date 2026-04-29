---
tags: [dev-plan, sprint, refactor, parser, db-init, prod-fix]
sprint: "dbinitrunner-dollar-quote-aware"
status: draft-v3
created: "2026-04-29"
---

# [개발계획서] DbInitRunner SQL splitter — dollar-quote 인식 + 운영 결함 자동 수정

- **작성팀**: 개발팀
- **작성일**: 2026-04-29
- **근거 기획서**: `docs/product-specs/dbinitrunner-dollar-quote-aware.md` v5 (codex 1~5차 검토 모두 반영, 5차 승인)
- **상태**: 초안 v3.2 (시행 단계에서 v3.1 의 phase1 직후 shim 가정이 phase2.sql 자기-완결적 구조와 충돌 발견 → codex 5차 자문 D-1+D-3 채택, 사용자 즉시 채택)
- **개정 이력**:
  - v1 (2026-04-29): 초안.
  - v2 (2026-04-29): codex 1차 검토 반영 — (a) **Step 1 fixture 명칭을 Step 4 와 정합화** (실행 단계 파일 mismatch 차단), (b) **Step 7 T-C 를 필수로 격상** — Java splitter 출력 → JDBC 순차 실행 통합테스트 추가 (NFR-3 d 게이트 직접 충족), (c) `tryReadDollarTag` / `isIdentifierChar` helper 를 **ASCII subset 으로 정확히 한정** (`c < 0x80 && (Character.isLetterOrDigit(c) || c=='_')`), (d) prevChar 갱신 정책 "마지막 소비 문자 기준" 명시.
  - v3 (2026-04-29): **codex 3차 자문 반영 — Step 7 ephemeral 통합 검증 단계의 init-ordering 결함 보강**.
    - **발견**: Step 7-2 에서 `psql -f phase2.sql` 이 line 60 `ON CONFLICT (sys_nm_en, process_name)` 에서 rc=3 실패. UNIQUE 가 swdept/sql/V018_process_master_dedup.sql (별도 sprint migration, Flyway 미관리) 에서 추가됨 — ephemeral 빈 DB 에 미적용 상태이기 때문.
    - **결정 (codex 권고 옵션 D, 사용자 채택)**: ephemeral 전용 shim fixture 1파일을 phase1+sigungu 후 / phase2 전 단계에 주입하여 prod 의 실제 상태를 시뮬레이션. 제품 마이그레이션 (db_init_phase2.sql, swdept/sql/) 은 무변경. 후속 sprint `phase2-V018-init-ordering` 가 V018 을 phase2 내부로 재배치 완료 시 v4 에서 shim 제거.
    - **변경 범위**:
      - (a) Step 7 절차를 7-1 / 7-1a / 7-1b / 7-2 로 재구성 — 7-1a shim fixture 생성, 7-1b preflight T-0, 7-2 순차 실행.
      - (b) 신규 fixture: `src/test/resources/ephemeral/V018_constraints_only.sql` — `CREATE UNIQUE INDEX IF NOT EXISTS` 2건만 포함 (V018 의 UNIQUE 부분만 추출, BEGIN/COMMIT/사전·사후 검증 DO 블록 제외).
      - (c) `DbInitRunnerIntegrationTest.java` 의 setup 순서를 phase1 → sigungu → **shim** → phase2 로 변경 (reqd test fixture).
      - (d) §3 롤백 / §4 fixture 갱신 정책 / §5 산출물 / §6 후속 sprint 메모에 shim 관련 항목 추가.
      - (e) PR 본문 (R-10 4항목) 에 "ephemeral 전용 fixture (제품 미포함)" 명시 + 후속 sprint 의 shim 제거 약속 추가.
  - v3.1 (2026-04-29): **codex 4차 검토 승인 + 비차단 권고 5건 반영**:
    - (i) **번호 일관성** — Step 7 라벨을 `7-1 / 7-1a / 7-1b / 7-2` 로 통일 (changelog 의 "7-1c" → "7-1b" 정정).
    - (ii) **Preflight guard (T-0)** — Step 7-1b 신설: shim 적용 직전 `to_regclass('public.tb_process_master')` / `to_regclass('public.tb_service_purpose')` 존재 확인 (테이블 미존재 시 원인 구분 용이).
    - (iii) **Env 일관성** — shell PGPASSWORD 와 IT 의 비번 환경변수 모두 `PGPASSWORD` 로 통일 (PG 표준).
    - (iv) **Search path 명시** — shim SQL 최상단에 `SET search_path = public;` 추가.
    - (v) **Commit 분리** — Step 8-1 에 Commit 5 추가 (Step 7 ephemeral shim + IntegrationTest 단일 커밋 — v4 제거 리버전 간결).
  - v3.2 (2026-04-29): **시행 단계 새 결함 발견 + codex 5차 자문 D-1+D-3 채택**:
    - **발견**: v3.1 의 `phase1 → sigungu → preflight → shim → phase2` 시나리오에서 preflight T-0 가 `p_ok=f, s_ok=f` 반환. 두 테이블 (`tb_process_master`, `tb_service_purpose`) 이 phase2.sql:34/43 에서 생성됨 — 즉 phase1+sigungu 후에는 미존재. shim 단독 실행 시 ERROR `relation does not exist`.
    - **codex 5차 권고 채택**: T-A 는 D-1 (phase2 두 번 실행 + 1차 로그 가드) / T-C 는 D-3 (Java IT 가 stmts 분리 후 첫 INSERT 직전 shim 동적 삽입). D-2 (line-range 분할) / D-4 (phase2.sql 변경) 비채택.
    - **변경 범위**:
      - (a) Step 7-2 시나리오 변경 — `phase1 → sigungu → phase2(1차, ON_ERROR_STOP=off, 로그캡처) → 1차 로그 allow-list 가드 → shim → phase2(2차, ON_ERROR_STOP=on)`.
      - (b) Step 7-1b preflight 위치 변경 — phase1+sigungu 후가 아닌 **phase2 1차 후** 로 이동 (그 시점에 두 테이블 존재).
      - (c) `DbInitRunnerIntegrationTest.setup()` 갱신 — D-3 패턴 적용. stmts 분리 → 첫 INSERT 직전까지 DDL 실행 → shim 적용 → phase2 전체 멱등 재실행 (errors=0 검증).
      - (d) T-매트릭스 T-A 기대값 명확화: 1차 ERROR 정확히 2건 (allow-list 매칭) + 2차 rc=0 + 4 제약 적용.
      - (e) shim SQL 자체는 v3.1 의 내용 (UNIQUE 2건 + SET search_path) 그대로 — 시점만 변경.

---

## 0. 전제 / 환경

### 0-1. 기획서 게이트 PASS
- v5 codex 5차 검토: **승인** ("잔존 미흡 없음")
- NFR-7 사전 검증 SQL 양쪽 prod 실행 결과: **4/4 = 0** (양쪽 모두) — 기 실행 완료 (2026-04-29)
  - 구 prod 211.104.137.55:5881: 4건 모두 0
  - 신규 prod 192.168.10.194:5880: 4건 모두 0

### 0-2. 도구 / 환경
- JDK 17, Maven 3.x (`./mvnw`)
- PostgreSQL 16 binary: `C:\Users\PUJ\PostgreSQL\16\bin\`
- ephemeral PG 클러스터 (재사용): `C:\Users\PUJ\pg16-verify\data` (port `25880`, 현재 stopped — Step 7 시점에 재기동)
- IDE: 본 PC `C:\Users\PUJ\eclipse-workspace\swmanager`

### 0-3. 범위 고정
- **Java 변경 1파일**: `src/main/java/com/swmanager/system/config/DbInitRunner.java`
- **Java 신규 1파일**: `src/test/java/com/swmanager/system/config/DbInitRunnerTest.java`
- **Java 신규 1파일 (v3)**: `src/test/java/com/swmanager/system/config/DbInitRunnerIntegrationTest.java`
- **Test fixture 신규 4개**:
  - `src/test/resources/db_init_phase2.do-block1.expected.txt`
  - `src/test/resources/db_init_phase2.do-block2.expected.txt`
  - `src/test/resources/db_init_phase2.baseline-non-do-hashes.txt`
  - `src/test/resources/ephemeral/V018_constraints_only.sql` (v3 — ephemeral 전용 shim, 제품 마이그레이션 미포함)
- 문서: `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` (본 문서) + `docs/PLANS.md` 갱신
- **제품 SQL 파일 변경 0** (phase2.sql / swdept/sql/* 그대로). shim fixture 는 test resources 한정.

---

## 1. 작업 순서

### Step 1 — 베이스라인 측정 (NFR-3 c 회귀 게이트 fixture)

**목적**: 변경 전 splitter 출력에서 DO 블록 외 단순 statement 의 trim + SHA-256 목록을 fixture 화. 변경 후 동일 입력 → 동일 출력 (의미적 동치) 검증.

**1-1. 베이스라인 측정 스크립트 — 일회성 단위테스트**
- 신규: `src/test/java/com/swmanager/system/config/DbInitRunnerBaselineTest.java` (Step 6 완료 후 삭제 또는 `@Disabled` 마킹)
- 동작:
  - phase2.sql 을 ClassPathResource 로 로드
  - **변경 전 splitter 로직** (현 `DbInitRunner.run()` 의 lines 43-74 그대로 복제 — 별도 static helper) 실행
  - 출력 statement 리스트의 (a) count, (b) 각 statement 의 trim + SHA-256 목록을 표준 출력으로 dump
- 실행: `./mvnw test -Dtest=DbInitRunnerBaselineTest`
- 결과 capture → 다음 **3개 fixture 파일** 에 저장 (Step 4 단위테스트가 직접 로드 — v2 명칭 정합화):
  - `src/test/resources/db_init_phase2.do-block1.expected.txt` — phase2.sql:500-511 본문 (정규화 후 — 개행 `\n` / 좌우 trim / 라인 내 trailing whitespace 제거)
  - `src/test/resources/db_init_phase2.do-block2.expected.txt` — phase2.sql:528-535 본문 (정규화 후)
  - `src/test/resources/db_init_phase2.baseline-non-do-hashes.txt` — DO 블록 외 statement (단순 CREATE TABLE / 단일 ALTER / INSERT) 의 trim + SHA-256 목록. 한 줄당 1 hex64. DO 블록 본문은 본 파일에서 제외 (Step 4 의 `ut12_c_nonDoStatementsUnchanged` 가 같은 필터링으로 비교)

**1-2. DO 블록 위치 line-range 추출**
- phase2.sql 의 `DO $$` 시작 line 과 `END $$;` 종료 line 매칭. 현 phase2.sql 기준:
  - 블록 1: lines 500-511
  - 블록 2: lines 528-535
- 본 line-range 는 fixture 파일 헤더에 명시 + 단위테스트 코드에 상수로 보관 (Step 4 에서 사용)

**진행 게이트**: 베이스라인 fixture 2개 파일 생성 + commit. fixture 파일은 본 PR 에 포함.

### Step 2 — DbInitRunner.java 리팩토링 (FR-2-A/B)

**목적**: 인라인 splitter 로직을 별도 static 메서드로 추출. **이 단계까지는 dollar-quote 인식 추가 없음** — 순수 추출 (semantic-preserving refactor).

**2-1. 메서드 시그니처**:
```java
package com.swmanager.system.config;

import java.util.ArrayList;
import java.util.List;

public class DbInitRunner implements ApplicationRunner {
    // ... 기존 코드 ...

    /**
     * SQL 텍스트를 statement 단위로 분리한다.
     *
     * <p>처리 규칙:
     * <ul>
     *   <li>라인 주석 ({@code -- ...}) — NORMAL 상태에서만 strip</li>
     *   <li>단일 인용 ({@code '...'}) — 내부 {@code ;} / {@code --} / {@code $...$} 보호. {@code ''} escape 처리</li>
     *   <li>dollar-quote ({@code $$...$$} / {@code $tag$...$tag$}) — 내부 {@code ;} / {@code '} / {@code --} 보호.
     *       tag 는 ASCII subset {@code [A-Za-z_][A-Za-z0-9_]*}, 대소문자 민감</li>
     *   <li>식별자 직후의 {@code $tag$} 는 dollar-quote 시작으로 간주하지 않음 (보수적 boundary)</li>
     * </ul>
     *
     * @param rawSql 입력 SQL 텍스트 (개행은 {@code \n})
     * @return trim 된 statement 리스트 (빈 statement 제외)
     */
    static List<String> splitSqlStatements(String rawSql) {
        // 본 메서드는 Step 2 에서 '현 splitter 의미 보존' 로 추출,
        // Step 3 에서 dollar-quote 인식 추가
        // ...
    }
}
```

**2-2. 추출 로직** (Step 2 — 의미 보존, dollar-quote 미포함):
- 현 `run()` 의 lines 43-74 로직을 그대로 함수 본문으로 옮김
- `run()` 메서드는 `splitSqlStatements(sql)` 만 호출
- 단위테스트 미작성 — 이 단계의 정확성은 Step 5 의 베이스라인 비교로 자동 검증

**진행 게이트**: `./mvnw clean compile` 성공. `./mvnw test` 기존 PASS (변경 0).

### Step 3 — dollar-quote 인식 + line-comment 처리 모델 변경 (FR-1)

**목적**: 기획서 §5-4 상태머신 구현. line-comment pre-pass 제거 + IN_DOLLAR_QUOTE 상태 추가.

**3-1. 상태머신 enum 정의**:
```java
private enum SplitterState {
    NORMAL, IN_LINE_COMMENT, IN_SINGLE_QUOTE, IN_DOLLAR_QUOTE
}
```

**3-2. 알고리즘 (의사 코드)**:
```
State state = NORMAL;
String dollarTag = null;        // IN_DOLLAR_QUOTE 진입 시 저장 (예: "$$" 또는 "$plpgsql$")
char prevChar = '\0';
StringBuilder sb = new StringBuilder();
List<String> stmts = new ArrayList<>();

for (int i = 0; i < raw.length(); i++) {
    char c = raw.charAt(i);
    switch (state) {
        case NORMAL:
            if (c == '-' && i + 1 < raw.length() && raw.charAt(i + 1) == '-') {
                state = IN_LINE_COMMENT;
                i++; // skip second '-'
                // line-comment 본문은 본문에 append 안함 (strip)
            } else if (c == '\'') {
                state = IN_SINGLE_QUOTE;
                sb.append(c);
            } else if (c == '$' && !isIdentifierChar(prevChar)) {
                String tag = tryReadDollarTag(raw, i);
                if (tag != null) {
                    state = IN_DOLLAR_QUOTE;
                    dollarTag = tag;
                    sb.append(tag);
                    i += tag.length() - 1; // tag 만큼 진행
                } else {
                    sb.append(c);
                }
            } else if (c == ';') {
                String s = sb.toString().trim();
                if (!s.isEmpty()) stmts.add(s);
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
            break;

        case IN_LINE_COMMENT:
            if (c == '\n') {
                state = NORMAL;
                sb.append(c); // 개행은 본문 보존 (line-comment 만 strip)
            }
            // 그 외 char 는 strip
            break;

        case IN_SINGLE_QUOTE:
            if (c == '\'') {
                if (i + 1 < raw.length() && raw.charAt(i + 1) == '\'') {
                    // escape '': 두 ' 모두 보존, idx + 1 skip
                    sb.append("''");
                    i++;
                } else {
                    state = NORMAL;
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            break;

        case IN_DOLLAR_QUOTE:
            if (c == '$' && raw.startsWith(dollarTag, i)) {
                // 닫힘 토큰 정확 매칭 (대소문자 민감)
                state = NORMAL;
                sb.append(dollarTag);
                i += dollarTag.length() - 1;
                dollarTag = null;
            } else {
                sb.append(c);
            }
            break;
    }
    prevChar = c;
}
// EOF — 잔여 sb 처리
String last = sb.toString().trim();
if (!last.isEmpty()) stmts.add(last);
return stmts;
```

**3-3. helper 메서드 (v2 — ASCII subset 명시):

기획서 §FR-1-B 의 "ASCII subset = `[A-Za-z_][A-Za-z0-9_]*`" 정책을 정확히 구현. `Character.isLetter/isLetterOrDigit` 는 non-ASCII 도 허용하므로 ASCII 가드 (`c < 0x80`) 추가:

```java
/** ASCII 식별자 시작 문자: A-Z, a-z, _ */
private static boolean isAsciiIdStart(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
}

/** ASCII 식별자 후속 문자: A-Z, a-z, 0-9, _ */
private static boolean isAsciiIdCont(char c) {
    return isAsciiIdStart(c) || (c >= '0' && c <= '9');
}

/**
 * boundary 검사 — NORMAL 상태에서 '$' 직전 문자가 식별자 문자였는지.
 * 식별자 문자 (ASCII id start/cont) 또는 '$' 면 dollar-quote 시작 후보 아님.
 */
private static boolean isIdentifierChar(char c) {
    return isAsciiIdCont(c) || c == '$';
}

/**
 * raw[i] 가 '$' 일 때, dollar-quote 시작 토큰을 시도 매칭.
 * 매칭 성공 시 토큰 문자열 (예: "$$", "$plpgsql$") 반환.
 * 매칭 실패 시 null 반환.
 *
 * 매칭 규칙 (ASCII subset):
 *   $tag$  where tag = [A-Za-z_][A-Za-z0-9_]*  (선택, 빈 tag = $$)
 */
private static String tryReadDollarTag(String raw, int i) {
    if (i >= raw.length() || raw.charAt(i) != '$') return null;
    int j = i + 1;
    // tag 시작 문자: ASCII 알파벳 또는 _ (있어도 됨)
    if (j < raw.length() && isAsciiIdStart(raw.charAt(j))) {
        // tag 본문 — ASCII 영숫자/_
        while (j < raw.length() && isAsciiIdCont(raw.charAt(j))) j++;
    }
    if (j < raw.length() && raw.charAt(j) == '$') {
        return raw.substring(i, j + 1);
    }
    return null;
}
```

**3-4. prevChar 갱신 정책 (v2 명시 — codex 권고 4)**:
- `prevChar` 는 **루프 본문에서 실제로 처리(append/skip)한 마지막 문자** 기준으로 갱신.
- 일반 진행: `prevChar = c;` (기본 케이스)
- skip 케이스 (`i++` 로 다음 char 도 같이 진행한 경우 — 예: `''` escape, dollar-tag 토큰 전체 진행) → **마지막 소비 문자** 가 `prevChar` 로 들어가야 함:
  - `''` escape: `prevChar = '\''` (escape 후의 `'`)
  - dollar-tag 토큰 전체: `prevChar = '$'` (토큰의 마지막 `$`)
  - 즉 `prevChar = raw.charAt(i)` 를 토큰 종료 idx 기준으로 갱신
- IN_LINE_COMMENT 의 strip 된 char 들은 `prevChar` 갱신 안함 (본문에 들어가지 않은 char) — 단 `\n` 은 NORMAL 복귀 시 본문 append 되므로 `prevChar = '\n'`

**진행 게이트**: `./mvnw clean compile` 성공. (테스트는 Step 4 에서)

### Step 4 — 단위테스트 작성 (FR-2-C/D, UT-1 ~ UT-15)

**4-1. 신규 파일**: `src/test/java/com/swmanager/system/config/DbInitRunnerTest.java`

```java
package com.swmanager.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DbInitRunnerTest {

    // UT-1: 단순 2 statement
    @Test void ut1_simpleTwoStatements() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "CREATE TABLE foo(); CREATE TABLE bar();");
        assertEquals(2, stmts.size());
    }

    // UT-2: 라인 주석 strip
    @Test void ut2_lineCommentStripped() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "-- comment\nCREATE TABLE foo();");
        assertEquals(1, stmts.size());
        assertFalse(stmts.get(0).contains("comment"));
    }

    // UT-3: 단일 인용 안의 ; 보호
    @Test void ut3_singleQuoteSemicolonProtected() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('a;b');");
        assertEquals(1, stmts.size());
    }

    // UT-4: 단일 인용 escape
    @Test void ut4_singleQuoteEscape() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('it''s');");
        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0).contains("''"));
    }

    // UT-5: DO $$ 블록 단일 statement 보존
    @Test void ut5_doDollarQuoteSingle() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN PERFORM 1; PERFORM 2; END $$;");
        assertEquals(1, stmts.size());
    }

    // UT-6: 태그 dollar-quote
    @Test void ut6_taggedDollarQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $plpgsql$ BEGIN PERFORM 1; END $plpgsql$;");
        assertEquals(1, stmts.size());
    }

    // UT-7: DO 블록 + 단일 인용 혼합
    @Test void ut7_doBlockWithSingleQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN RAISE NOTICE 'a;b;c'; END $$;");
        assertEquals(1, stmts.size());
    }

    // UT-8: DO 블록 + 외부 statement 3개
    @Test void ut8_doBlockBetweenStatements() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "CREATE TABLE x(); DO $$ BEGIN PERFORM 1; END $$; CREATE TABLE y();");
        assertEquals(3, stmts.size());
    }

    // UT-9: 단일 인용 안의 $$ 리터럴
    @Test void ut9_dollarQuoteLiteralInsideSingleQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "INSERT INTO t VALUES ('contains $$ literal');");
        assertEquals(1, stmts.size());
    }

    // UT-10: 미종료 dollar-quote — panic 없이 종료
    @Test void ut10_unterminatedDollarQuote() {
        List<String> stmts = DbInitRunner.splitSqlStatements(
            "DO $$ BEGIN PERFORM 1");
        assertEquals(1, stmts.size());
    }

    // UT-11: 빈 statement 무시
    @Test void ut11_emptyStatementsIgnored() {
        List<String> stmts = DbInitRunner.splitSqlStatements(";;;");
        assertEquals(0, stmts.size());
    }

    // UT-13 (v2): DO 블록 내부 라인 주석 보존
    @Test void ut13_doBlockPreservesLineComment() {
        String input = "DO $$ BEGIN -- 주석 한국어\n  PERFORM 1;\n  -- 다른 주석\n  PERFORM 2;\nEND $$;";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0).contains("-- 주석 한국어"));
        assertTrue(stmts.get(0).contains("-- 다른 주석"));
    }

    // UT-14 (v2): 대소문자 다른 dollar-tag 미매칭
    @Test void ut14_caseSensitiveDollarTag() {
        String input = "DO $TAG$ BEGIN PERFORM 1; END $tag$ ; SELECT 1;";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        // $tag$ 가 닫힘 토큰으로 인식 안됨 → 끝까지 IN_DOLLAR_QUOTE → 단일 statement
        assertEquals(1, stmts.size());
    }

    // UT-15 (v2): 식별자 직후 boundary
    @Test void ut15_identifierBoundaryDoesNotEnterDollarQuote() {
        String input = "SELECT foo$$ FROM t; CREATE TABLE x();";
        List<String> stmts = DbInitRunner.splitSqlStatements(input);
        assertEquals(2, stmts.size());
    }

    // UT-12 (v3 회귀 게이트 — 의미적 동치):
    //   (b) DO 블록 단일 보존 — 3 sub-gate
    //   (c) 일반 SQL 순서 유지 — DO 블록 외 statement 의 trim + SHA-256 목록이 베이스라인과 동일
    //   (d) PG 파서 검증은 통합 단계 (Step 7) 에서

    @Test void ut12_b1_doBlocksMatchLineRangeFixture() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        // line-range fixture 로드
        String block1 = loadFixture("db_init_phase2.do-block1.expected.txt"); // lines 500-511
        String block2 = loadFixture("db_init_phase2.do-block2.expected.txt"); // lines 528-535
        // 정확히 1개 statement 가 block1 본문과 일치 (정규화 후)
        long matches1 = stmts.stream().filter(s -> normalize(s).equals(normalize(block1))).count();
        long matches2 = stmts.stream().filter(s -> normalize(s).equals(normalize(block2))).count();
        assertEquals(1, matches1, "DO 블록 1 (lines 500-511) 가 정확히 1 statement 로 보존되어야 함");
        assertEquals(1, matches2, "DO 블록 2 (lines 528-535) 가 정확히 1 statement 로 보존되어야 함");
    }

    @Test void ut12_b2_doBlocksHaveDoEndPrefixSuffix() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        long doStmts = stmts.stream()
            .filter(s -> s.startsWith("DO $$"))
            .filter(s -> s.endsWith("END $$"))
            .count();
        assertEquals(2, doStmts, "phase2.sql 의 DO 블록 2건 모두 DO $$ ... END $$ 형태");
    }

    @Test void ut12_b3_noLeakageOfDoBodyToOtherStatements() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        // DO 블록 본문의 핵심 키워드가 DO 외 statement 에 누설 안됨
        String[] doBodyKeywords = {
            "fk_tb_document_org_unit",
            "ck_tb_document_support_target_type",
            "ck_tb_document_environment",
            "fk_tb_document_region_code"
        };
        for (String kw : doBodyKeywords) {
            long leaks = stmts.stream()
                .filter(s -> !s.startsWith("DO $$"))
                .filter(s -> s.contains(kw))
                .count();
            assertEquals(0, leaks, "키워드 '" + kw + "' 가 DO 외 statement 로 누설되면 안됨");
        }
    }

    @Test void ut12_c_nonDoStatementsUnchanged() throws Exception {
        String phase2 = loadPhase2();
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        // DO 블록 외 statement 의 trim + SHA-256 목록 추출
        List<String> hashes = stmts.stream()
            .filter(s -> !s.startsWith("DO $$"))
            .map(s -> sha256(normalize(s)))
            .collect(Collectors.toList());
        // 베이스라인 fixture 와 비교
        List<String> baseline = loadBaselineHashes(); // src/test/resources/db_init_phase2.baseline-non-do-hashes.txt
        assertEquals(baseline, hashes, "DO 블록 외 statement 의 SHA-256 목록이 베이스라인과 동일");
    }

    // helpers
    private static String loadPhase2() throws Exception {
        ClassPathResource r = new ClassPathResource("db_init_phase2.sql");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
    private static String loadFixture(String name) throws Exception {
        ClassPathResource r = new ClassPathResource(name);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
    private static List<String> loadBaselineHashes() throws Exception {
        ClassPathResource r = new ClassPathResource("db_init_phase2.baseline-non-do-hashes.txt");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().filter(l -> !l.isBlank()).collect(Collectors.toList());
        }
    }
    private static String normalize(String s) {
        // 개행 통일 + 좌우 trim + 라인 내 trailing whitespace 제거
        return s.lines().map(String::stripTrailing).collect(Collectors.joining("\n")).trim();
    }
    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
```

**4-2. 단위테스트 fixture 파일** (Step 1 에서 생성):
- `src/test/resources/db_init_phase2.do-block1.expected.txt` — phase2.sql:500-511 본문 (정규화 후)
- `src/test/resources/db_init_phase2.do-block2.expected.txt` — phase2.sql:528-535 본문
- `src/test/resources/db_init_phase2.baseline-non-do-hashes.txt` — DO 외 statement 의 SHA-256 목록 (한 줄당 1 hex64)

**진행 게이트**: 단위테스트 컴파일 성공 + Step 5 에서 PASS

### Step 5 — 단위테스트 실행 + 베이스라인 검증 (NFR-2 + NFR-3 b/c)

**5-1. 실행**:
```bash
./mvnw test -Dtest=DbInitRunnerTest
```

**5-2. 기대 결과**:
- UT-1 ~ UT-15 (UT-12 는 4개 sub-gate b1/b2/b3/c 로 분리) → **18 PASS / 0 FAIL**
- 회귀 게이트 통과: DO 외 statement 의 SHA-256 목록 베이스라인 일치

**진행 게이트**: 18/18 PASS. 1건이라도 FAIL 이면 Step 3 의 splitter 알고리즘 디버그.

### Step 6 — 베이스라인 측정 스크립트 정리

- Step 1 의 `DbInitRunnerBaselineTest.java` 를 `@Disabled("베이스라인 측정용 — fixture 갱신 시에만 활성화")` 마킹 또는 삭제. fixture 파일은 유지.
- 정책 문서: fixture 갱신 절차를 본 개발계획서 §롤백 직후 짧은 절(§Z fixture 갱신) 에 명시 → phase2.sql 변경 PR 시 베이스라인 재측정 + fixture 갱신.

### Step 7 — ephemeral 통합 검증 (NFR-3 d, T-A) — v3 재구성

**목적**: 변경 후 splitter 가 phase2.sql 전문을 PG 파서가 받을 때 syntax error 없이 통과하는지 ephemeral 클러스터로 검증.

**v3 변경**: phase2.sql 의 init-ordering 결함 (line 60 ON CONFLICT 가 V018 의 UNIQUE 를 전제) 을 ephemeral shim fixture 로 우회. shim 은 prod 의 실제 상태 (V018 수동 적용됨) 를 시뮬레이션 — splitter 검증의 신호/잡음을 분리.

**7-1. ephemeral 클러스터 재기동**:
```bash
"C:/Users/PUJ/PostgreSQL/16/bin/pg_ctl.exe" -D "C:/Users/PUJ/pg16-verify/data" -l "C:/Users/PUJ/pg16-verify/logfile" start
```
- 기동 실패 시 `data/postmaster.pid` 정리 후 재시도 (phase1 스프린트 보존 클러스터)

**7-1a. shim fixture 생성** (`src/test/resources/ephemeral/V018_constraints_only.sql`):
```sql
-- ephemeral 전용 shim — phase2.sql 의 ON CONFLICT 가 전제하는 UNIQUE 만 추출.
-- V018_process_master_dedup.sql 의 (4) 절 UNIQUE 부분만 (사전·사후 DO 블록, BEGIN/COMMIT, DELETE, NOT NULL 전환 제외).
-- 후속 sprint phase2-V018-init-ordering 이 V018 을 phase2 내부로 재배치 완료 시 본 파일 제거 (exec plan v4).
-- ⚠ 본 파일은 src/test/resources/ephemeral/ 한정 — 제품 마이그레이션 / 운영 DB 적용 금지.

SET search_path = public;

CREATE UNIQUE INDEX IF NOT EXISTS uq_tb_process_master_sysnm_process
  ON public.tb_process_master (sys_nm_en, process_name);

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON public.tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));
```

**근거**: phase2.sql 내 명시 컬럼 ON CONFLICT 사용처 (line 60: `(sys_nm_en, process_name)`, line 70: `(sys_nm_en, purpose_type, md5(purpose_text))`) 가 사용하는 UNIQUE 만. line 546/564/574 의 카테고리 ON CONFLICT 들은 phase2.sql 내부 PK 로 처리. 컬럼 미지정 ON CONFLICT (line 201/223/247/261/284) 는 PG 가 PK 로 처리 — shim 불필요.

**7-1b. Preflight 가드 T-0** (v3.2 — phase2 1차 후 로 이동: 두 테이블이 phase2.sql:34/43 에서 생성되므로 phase1+sigungu 후에는 미존재):
```bash
PGPASSWORD=ephemeral "C:/Users/PUJ/PostgreSQL/16/bin/psql.exe" -h localhost -p 25880 -U postgres -d sw_test2 -v ON_ERROR_STOP=1 -c "SELECT to_regclass('public.tb_process_master') IS NOT NULL AS p_ok, to_regclass('public.tb_service_purpose') IS NOT NULL AS s_ok;"
```
**기대**: phase2 1차 실행 (DDL 적용) 후 두 컬럼 모두 `t`. 어느 하나라도 `f`/NULL 이면 phase2 1차 의 CREATE TABLE 단계가 의도와 다르게 진행된 것 — shim/2차 진행 중단.

**7-2. 신규 DB 생성 + 두 번 실행 시나리오 (v3.2 — D-1 채택)**:
```bash
set -e
export PGPASSWORD=ephemeral
PSQL="C:/Users/PUJ/PostgreSQL/16/bin/psql.exe"

# DB 재생성
"$PSQL" -h localhost -p 25880 -U postgres -d postgres -c "DROP DATABASE IF EXISTS sw_test2;"
"$PSQL" -h localhost -p 25880 -U postgres -d postgres -c "CREATE DATABASE sw_test2;"

# phase1 + sigungu — ON_ERROR_STOP=on
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 -v ON_ERROR_STOP=1 -f src/main/resources/db_init_phase1.sql
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 -v ON_ERROR_STOP=1 -f src/main/resources/db_init_phase1_sigungu.sql

# phase2 1차 — ON_ERROR_STOP=off + stderr 캡처. 멱등 → CREATE TABLE 들 모두 적용.
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 -f src/main/resources/db_init_phase2.sql > /tmp/p2_pass1.log 2>&1 || true

# 7-1b — preflight T-0 (phase2 1차 후 두 테이블 존재 검증)
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 -v ON_ERROR_STOP=1 -c "SELECT to_regclass('public.tb_process_master') IS NOT NULL AS p_ok, to_regclass('public.tb_service_purpose') IS NOT NULL AS s_ok;"

# 7-2a — 1차 로그 allow-list 가드 (codex 5차 권고, v3.2 시행 단계 V024/V026 발견 반영)
# 1차 stderr 의 ERROR 라인은 정확히 5건이어야 함 — 모두 prod-shim 으로 시뮬레이션된 sprint migration 의존:
#   V018 — "no unique or exclusion constraint matching the ON CONFLICT specification" × 2 (tb_process_master / tb_service_purpose)
#   V024 — relation "qt_category_mst" does not exist × 1
#   V026 — relation "work_plan_type_mst" does not exist + relation "work_plan_status_mst" does not exist × 2
# 그 외 ERROR 발견 시 abort.
TOTAL_ERR=$(grep ' ERROR:' /tmp/p2_pass1.log | wc -l)
A1=$(grep -c 'no unique or exclusion constraint matching the ON CONFLICT specification' /tmp/p2_pass1.log)
A2=$(grep -c 'relation "qt_category_mst" does not exist' /tmp/p2_pass1.log)
A3=$(grep -c 'relation "work_plan_type_mst" does not exist' /tmp/p2_pass1.log)
A4=$(grep -c 'relation "work_plan_status_mst" does not exist' /tmp/p2_pass1.log)
ALLOWED_TOTAL=$((A1 + A2 + A3 + A4))
if [ "$TOTAL_ERR" != "5" ] || [ "$ALLOWED_TOTAL" != "5" ] || [ "$A1" != "2" ] || [ "$A2" != "1" ] || [ "$A3" != "1" ] || [ "$A4" != "1" ]; then
  echo "ABORT: phase2 1차 로그 가드 실패 — total=$TOTAL_ERR allowed=$ALLOWED_TOTAL (V018=$A1 V024=$A2 V026type=$A3 V026status=$A4)"
  grep ' ERROR:' /tmp/p2_pass1.log
  exit 1
fi
echo "PASS: phase2 1차 로그 가드 — ERROR 정확히 5건 (V018×2 + V024×1 + V026×2 allow-list 매칭)"

# 7-1a — ephemeral 전용 shim 적용 (이제 두 테이블 존재)
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 -v ON_ERROR_STOP=1 -f src/test/resources/ephemeral/V018_constraints_only.sql

# phase2 2차 — ON_ERROR_STOP=on, --single-transaction. 멱등 → ON CONFLICT 정상 작동, DO 블록 정상 실행.
"$PSQL" -h localhost -p 25880 -U postgres -d sw_test2 --single-transaction -v ON_ERROR_STOP=1 -f src/main/resources/db_init_phase2.sql
```

**기대**: 모든 명시적 게이트 (phase1, sigungu, preflight, shim, phase2 2차) rc=0. phase2 1차는 의도된 두 ERROR 만 발생 후 종료 (rc 무시, 로그 가드 통과).

**7-3. T-매트릭스 (v3.2 — D-1 + D-3 반영)**:

| ID | 검증 항목 | 기대 | 분류 |
|----|----------|------|------|
| T-A | phase1+sigungu+**phase2(1차)+로그가드+shim+phase2(2차)** 시나리오 (D-1) | phase1/sigungu/preflight/shim/phase2-2차 모두 rc=0. phase2-1차 ERROR 정확히 5건 (V018×2 + V024×1 + V026×2 allow-list 매칭) | **필수** |
| T-B | 4개 제약 적용 확인 | `SELECT conname FROM pg_constraint WHERE conname IN ('fk_tb_document_org_unit', 'ck_tb_document_support_target_type', 'ck_tb_document_environment', 'fk_tb_document_region_code');` → 4행 (T-A 후) | **필수** |
| T-C (D-3 채택) | **Java DbInitRunner 통합 시뮬** — DbInitRunnerIntegrationTest 가 phase1 → sigungu → **phase2 stmts 의 첫 INSERT 직전까지 (DDL only) → shim → phase2 전체 멱등 재실행** 시퀀스로 setup. `phase2 재실행 errors=0` + 4개 제약 적용 검증 | errors=0 + 4 제약 적용 | **필수** |

**T-C 통합테스트 — 신규 파일** `src/test/java/com/swmanager/system/config/DbInitRunnerIntegrationTest.java`:

```java
package com.swmanager.system.config;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.core.io.ClassPathResource;
import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NFR-3(d) 게이트 — Java splitter 출력 → JDBC 순차 실행 검증.
 *
 * 환경: ephemeral PG 클러스터 (localhost:25880, phase1 스프린트 보존).
 * 사전조건: phase1.sql + phase1_sigungu.sql 까지 적용된 fresh DB 가 있어야 함.
 *   본 테스트는 sw_test2 DB 를 매 회 DROP/CREATE 후 phase1 → sigungu → phase2 순차 실행.
 *
 * SkipCondition: ephemeral 클러스터 미기동 시 skip (개발자 환경 비차단).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DbInitRunnerIntegrationTest {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:25880/sw_test2";
    private static final String JDBC_ADMIN_URL = "jdbc:postgresql://localhost:25880/postgres";
    private static final String DB_USER = "postgres";
    // v3.1: shell 측 PGPASSWORD 와 변수명 통일 (PG 표준).
    // ephemeral 클러스터 비번이 다르면 PGPASSWORD 환경변수를 export 후 mvn 실행.
    private static final String DB_PASS = System.getenv().getOrDefault("PGPASSWORD", "ephemeral");

    private static JdbcTemplate jdbc;
    private static JdbcTemplate adminJdbc;

    @BeforeAll
    static void setup() throws Exception {
        adminJdbc = jdbc(JDBC_ADMIN_URL);
        // ephemeral 클러스터 가용성 체크 (skip 조건)
        try { adminJdbc.queryForObject("SELECT 1", Integer.class); }
        catch (Exception e) {
            Assumptions.abort("ephemeral PG (localhost:25880) 미기동 — 통합테스트 skip");
        }
        adminJdbc.execute("DROP DATABASE IF EXISTS sw_test2");
        adminJdbc.execute("CREATE DATABASE sw_test2");
        jdbc = jdbc(JDBC_URL);
        execSqlFile(jdbc, "db_init_phase1.sql");
        execSqlFile(jdbc, "db_init_phase1_sigungu.sql");
        // v3.2 (D-3): phase2 stmts 분리 → 첫 INSERT 직전까지 DDL only 실행 → shim 적용
        // (두 테이블 tb_process_master / tb_service_purpose 는 phase2.sql 안에서 생성되므로
        //  shim 적용 시점은 phase2 1차 DDL 단계 후로 잡아야 함.)
        String phase2 = loadResource("db_init_phase2.sql");
        List<String> phase2Stmts = DbInitRunner.splitSqlStatements(phase2);
        int firstInsertIdx = -1;
        for (int i = 0; i < phase2Stmts.size(); i++) {
            if (phase2Stmts.get(i).toUpperCase().startsWith("INSERT INTO")) {
                firstInsertIdx = i;
                break;
            }
        }
        if (firstInsertIdx < 0) firstInsertIdx = phase2Stmts.size();
        for (int i = 0; i < firstInsertIdx; i++) jdbc.execute(phase2Stmts.get(i));
        execSqlFile(jdbc, "ephemeral/V018_constraints_only.sql");
        // 본 setup 의 잔여 phase2 (INSERT 부터 끝까지) 는 본 테스트 메서드가 phase2 전체를
        // 멱등 재실행하므로 굳이 split 하지 않아도 됨 — testMethod 가 직접 실행.
    }

    @AfterAll
    static void teardown() {
        if (adminJdbc != null) adminJdbc.execute("DROP DATABASE IF EXISTS sw_test2");
    }

    @Test @Order(1)
    void tC_splitterOutputExecutesWithoutSyntaxError() throws Exception {
        String phase2 = loadResource("db_init_phase2.sql");
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        int errors = 0;
        for (String stmt : stmts) {
            try { jdbc.execute(stmt); }
            catch (Exception e) {
                // catch 패턴은 DbInitRunner 와 동일하지만, 본 테스트에서는 수치만 카운트
                errors++;
            }
        }
        // DbInitRunner 의 catch DEBUG swallow 모방 — 단 splitter fix 후에는 phase2.sql 의
        // 모든 statement 가 syntax error 없이 통과해야 함 (멱등 가드는 PG 에서 정상 동작).
        // 단 IF NOT EXISTS 가 false 인 stmt 들은 PG 가 silently no-op 하므로 catch 발생 0 예상.
        assertEquals(0, errors,
            "splitter 출력의 모든 statement 가 PG syntax error 없이 통과해야 함 (errors=" + errors + ")");
    }

    @Test @Order(2)
    void tC_fourConstraintsAppliedAfterRun() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint WHERE conname IN " +
            "('fk_tb_document_org_unit','ck_tb_document_support_target_type'," +
            " 'ck_tb_document_environment','fk_tb_document_region_code')",
            Integer.class);
        assertEquals(4, cnt, "4개 제약 모두 적용 — DO 블록이 splitter 에서 정상 보존됨");
    }

    private static JdbcTemplate jdbc(String url) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, DB_USER, DB_PASS);
        return new JdbcTemplate(ds);
    }
    private static void execSqlFile(JdbcTemplate j, String name) throws Exception {
        String sql = loadResource(name);
        for (String s : DbInitRunner.splitSqlStatements(sql)) {
            j.execute(s);
        }
    }
    private static String loadResource(String name) throws Exception {
        ClassPathResource r = new ClassPathResource(name);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
```

**진행 게이트**: T-A + T-B + T-C 모두 PASS. T-C 가 ephemeral 클러스터 미기동 시 skip 되더라도 단위테스트 + T-A/T-B 는 필수.

### Step 8 — PR 작성 + 리뷰

**8-1. 커밋 단위 분리** (v3.1 — Commit 5 분리, codex 권고):
- Commit 1: Step 1 베이스라인 fixture 추가 (3 파일)
- Commit 2: Step 2 splitter 추출 리팩토링
- Commit 3: Step 3 dollar-quote 인식 + line-comment 모델 변경
- Commit 4: Step 4-5 단위테스트 + UT 18 PASS
- **Commit 5 (v3.1 신규)**: Step 7 ephemeral shim fixture (`src/test/resources/ephemeral/V018_constraints_only.sql`) + `DbInitRunnerIntegrationTest.java`. 별도 커밋으로 분리하여 후속 sprint v4 의 shim 제거 리버전을 단순 1-commit revert 로 처리 가능하도록.

**8-2. PR 본문 템플릿 (R-10 4항목 의무)**:
```markdown
# DbInitRunner SQL splitter — dollar-quote 인식 + 운영 결함 자동 수정

## ⚠ 운영DB 자동 변경 위험 고지
본 PR 은 머지 + 운영 톰캣 재시작 시점에 양쪽 prod (구 211.104.137.55:5881 + 신규 192.168.10.194:5880)
DB 에 다음 4개 제약을 자동 추가합니다:
- fk_tb_document_org_unit
- ck_tb_document_support_target_type
- ck_tb_document_environment
- fk_tb_document_region_code

## 배포 체크리스트 (R-10 의무)

- [ ] (i) **사전 검증 SQL §9-1 양쪽 prod 실행 + 4건 모두 0 PASS** (재확인 — 2026-04-29 1차 PASS 완료)
- [ ] (ii) **운영 톰캣 재시작 maintenance window 확보** — splitter fix WAR 배포 + 톰캣 재시작 동일 윈도우
- [ ] (iii) **롤백 한계 인지**: WAR 되돌림만으로 4개 제약 DROP 안됨. 롤백 필요 시 별도 SQL 수동 실행:
      ```sql
      ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS fk_tb_document_org_unit;
      ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS ck_tb_document_support_target_type;
      ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS ck_tb_document_environment;
      ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS fk_tb_document_region_code;
      ```
- [ ] (iv) **사후 모니터링**: 운영 톰캣 재시작 후 catalina.out / server.log 의 DbInitRunner DEBUG/WARN 로그 확인 + §9-2 사후 검증 SQL 실행 (4/4 적용 확인) — 양쪽 prod 모두

## 변경 사항
- DbInitRunner.java: SQL splitter 가 dollar-quote (`$$`, `$tag$`) 를 단일 statement 로 보존
- 단위테스트 18건 (UT-1~UT-15 + UT-12 의 sub-gate b1/b2/b3/c)
- 통합테스트 1건 (DbInitRunnerIntegrationTest — T-A/T-B/T-C ephemeral 검증)
- Ephemeral 전용 shim fixture 1개 (`src/test/resources/ephemeral/V018_constraints_only.sql`) — **임시**, 후속 sprint `phase2-V018-init-ordering` 완료 시 제거 (exec plan v4)
- 회귀 게이트: phase2.sql 의 DO 외 statement SHA-256 목록 베이스라인 일치

## Ephemeral shim 명시 (v3 신규)
본 PR 의 `src/test/resources/ephemeral/V018_constraints_only.sql` 는 **테스트 한정 fixture** 이며 제품 마이그레이션 (`db_init_phase2.sql`, `swdept/sql/V018_*.sql`) 에는 미포함. 후속 sprint 가 phase2.sql 의 init-ordering 을 정정한 후 본 fixture 는 자동 제거됨 (exec plan v4 절차).
```

**8-3. codex 검토 의뢰** (구현 이후): `bash .team-workspace/codex-trace.sh` 또는 `codex exec ... "구현 commit + 단위테스트 검토"`.

### Step 9 — 운영 배포 + 사후 검증 (NFR-8)

**9-1. WAR 빌드 + 운영 배포** — 별도 WAR 배포 메모리 절차 (`project_swmanager_deploy.md`) 따름. ROOT.war 로 출력.

**9-2. 사후 검증 SQL 양쪽 prod 실행** (기획서 §9-2):
```sql
SELECT conname, contype FROM pg_constraint
 WHERE conname IN ('fk_tb_document_org_unit', 'ck_tb_document_support_target_type',
                   'ck_tb_document_environment', 'fk_tb_document_region_code')
 ORDER BY conname;
```

**기대**: 4행 출력 (4/4 적용). contype: f / c / c / f.

**진행 게이트**: 양쪽 prod 모두 4/4 PASS → 본 스프린트 "작업완료".

---

## 2. 단위테스트 매트릭스 요약

| ID | 케이스 | 메서드 | 위치 |
|----|------|----|----|
| UT-1 | 단순 2 statement | `ut1_simpleTwoStatements` | Step 4 |
| UT-2 | 라인 주석 strip | `ut2_lineCommentStripped` | Step 4 |
| UT-3 | 단일 인용 안 ; 보호 | `ut3_singleQuoteSemicolonProtected` | Step 4 |
| UT-4 | 단일 인용 escape | `ut4_singleQuoteEscape` | Step 4 |
| UT-5 | DO `$$` 단일 보존 | `ut5_doDollarQuoteSingle` | Step 4 |
| UT-6 | 태그 dollar-quote | `ut6_taggedDollarQuote` | Step 4 |
| UT-7 | DO + 단일 인용 혼합 | `ut7_doBlockWithSingleQuote` | Step 4 |
| UT-8 | DO 블록 + 외부 3 statement | `ut8_doBlockBetweenStatements` | Step 4 |
| UT-9 | 단일 인용 안 `$$` 리터럴 | `ut9_dollarQuoteLiteralInsideSingleQuote` | Step 4 |
| UT-10 | 미종료 dollar-quote | `ut10_unterminatedDollarQuote` | Step 4 |
| UT-11 | 빈 statement 무시 | `ut11_emptyStatementsIgnored` | Step 4 |
| UT-12 b1 | DO 블록 line-range 정확 매칭 | `ut12_b1_doBlocksMatchLineRangeFixture` | Step 4 |
| UT-12 b2 | DO `$$` / END `$$` prefix·suffix | `ut12_b2_doBlocksHaveDoEndPrefixSuffix` | Step 4 |
| UT-12 b3 | 본문 외부 누설 0 | `ut12_b3_noLeakageOfDoBodyToOtherStatements` | Step 4 |
| UT-12 c | DO 외 SHA-256 목록 일치 | `ut12_c_nonDoStatementsUnchanged` | Step 4 |
| UT-13 | DO 블록 안 라인 주석 보존 | `ut13_doBlockPreservesLineComment` | Step 4 |
| UT-14 | 대소문자 다른 tag 미매칭 | `ut14_caseSensitiveDollarTag` | Step 4 |
| UT-15 | 식별자 직후 boundary | `ut15_identifierBoundaryDoesNotEnterDollarQuote` | Step 4 |

**합계**: 18 케이스 / 18 PASS 기대.

---

## 3. 롤백 절차

### 3-1. 본 PR 자체 롤백 (commit revert)
```bash
git revert <commit-3 + commit-4 + commit-2 + commit-1 의 SHA>
git push
```
WAR 재빌드 + 배포 → 다음 톰캣 재시작 시점에 원래 splitter 로 복귀.

### 3-2. 운영DB 의 4개 제약 DROP (필요 시)
**중요**: WAR 되돌림만으로는 운영DB 의 4개 제약이 자동 DROP 되지 않음. DDL 은 일방향. 별도 수동 실행:

```sql
-- 양쪽 prod 동일 적용
ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS fk_tb_document_org_unit;
ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS ck_tb_document_support_target_type;
ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS ck_tb_document_environment;
ALTER TABLE tb_document DROP CONSTRAINT IF EXISTS fk_tb_document_region_code;
```

### 3-3. 단위테스트 롤백 (Step 1 fixture 만 유지하고 알고리즘만 되돌리는 경우)
- DbInitRunner.java 의 splitter 알고리즘만 git revert
- 단위테스트는 유지 — 알고리즘 변경 시 회귀 검증 자산으로 활용

---

## 4. fixture 갱신 정책 (drift 회피)

phase2.sql 이 변경되는 PR 마다:
1. Step 1 베이스라인 측정 스크립트 활성화 (`@Disabled` 해제 또는 `mvn -Dtest=DbInitRunnerBaselineTest` 강제)
2. 출력 fixture 3 파일 갱신 (`do-block1.expected.txt`, `do-block2.expected.txt`, `baseline-non-do-hashes.txt`)
3. PR review 단계에서 fixture diff 가 의도와 일치하는지 확인 (PR 작성자 책임 + 리뷰어 체크)

DO 블록의 line-range 도 변경 가능 — fixture 헤더 주석에 line-range 명시.

### 4-1. Shim fixture 제거 정책 (v3 신규)

`src/test/resources/ephemeral/V018_constraints_only.sql` 는 **임시 fixture** — 후속 sprint `phase2-V018-init-ordering` 의 v4 작업 완료 시 제거 예정.

**제거 조건** (3 항목 모두 충족 시 본 fixture 삭제):
1. phase2.sql 가 V018 의 UNIQUE 2건을 INSERT 보다 먼저 적용하도록 재배치 완료
2. ephemeral 에서 phase1 → sigungu → phase2 만으로 rc=0 (shim 없이) PASS 확인
3. `DbInitRunnerIntegrationTest.setup()` 의 shim execSqlFile 호출 라인도 함께 제거

---

## 5. 산출물 요약

| 계층 | 파일 | 비고 |
|------|------|------|
| 코드 | `src/main/java/com/swmanager/system/config/DbInitRunner.java` | splitSqlStatements 추출 + 상태머신 |
| 테스트 | `src/test/java/com/swmanager/system/config/DbInitRunnerTest.java` | UT-1~UT-15 (18 케이스) |
| 통합테스트 (v2 신규) | `src/test/java/com/swmanager/system/config/DbInitRunnerIntegrationTest.java` | T-C — Java splitter → JDBC 실행 + 4 제약 적용 확인. setup 에 shim 포함 (v3) |
| 테스트 fixture | `src/test/resources/db_init_phase2.do-block1.expected.txt` | 신규 |
| 테스트 fixture | `src/test/resources/db_init_phase2.do-block2.expected.txt` | 신규 |
| 테스트 fixture | `src/test/resources/db_init_phase2.baseline-non-do-hashes.txt` | 신규 |
| Ephemeral shim (v3 신규) | `src/test/resources/ephemeral/V018_constraints_only.sql` | **임시** — 후속 sprint 종료 시 제거. 제품 마이그레이션 미포함 |
| 임시 베이스라인 | `src/test/java/com/swmanager/system/config/DbInitRunnerBaselineTest.java` | `@Disabled` 또는 삭제 |
| 문서 | `docs/exec-plans/dbinitrunner-dollar-quote-aware.md` | 본 문서 |
| 문서 | `docs/PLANS.md` | 스프린트 표 추가 + 후속 백로그 등록 (자동 DDL 모델 개선 + shim 제거) |

**검증 산출물**:
- 단위테스트 18 PASS
- ephemeral 클러스터 phase2.sql 실행 rc=0 + 4개 제약 적용
- 양쪽 prod 사후 검증 4/4 PASS

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 + 사용자 최종승인 요청.

### 승인 전 확인 사항
- [x] 기획서 v5 codex 5차 승인 완료
- [x] NFR-7 사전 검증 PASS (2026-04-29)
- [x] 9 단계 작업 순서 (Step 1~9)
- [x] 단위테스트 매트릭스 18 케이스
- [x] PR 본문 템플릿 R-10 4항목 의무
- [x] 롤백 절차 (3 단계 — PR revert / 제약 DROP / 단위테스트 처리)
- [x] fixture 갱신 정책 (drift 회피)
- [x] codex 1차 검토 결과 반영 (v2 — fixture 명칭 정합 / T-C 필수 격상 / ASCII helper 보정 / prevChar 정책 명시)
- [x] codex 2차 검토 / Step 1~6 구현 완료 (단위테스트 18/18 PASS — 2026-04-29)
- [x] codex 3차 자문 (Step 7 init-ordering 결함) — 옵션 D shim 채택, 사용자 1차 승인 (2026-04-29)
- [x] v3 개정 (Step 7 7-1a/7-1c 추가 / shim 명시 / IntegrationTest setup 갱신 / R-10 항목 추가)
- [ ] codex 4차 검토 (v3 개정안)
- [ ] 사용자 "최종승인" v3 → Step 7 구현 진입

### 다음 절차
1. codex 4차 검토 → 사용자 "반영" 시 v4 개정 (필요 시)
2. 사용자 "최종승인" v3 → **Step 7-1a shim fixture 생성 + IntegrationTest 작성** 부터 잔여 구현
3. T-A/T-B/T-C 모두 PASS 확인 → Step 8 PR + Step 9 운영 배포
