---
tags: [dev-plan, sprint, refactor, data-cleanup, logging]
sprint: "access-log-userid-fix"
status: draft
created: "2026-04-20"
---

# [개발계획서] access_logs.userid 오염 정제 + LogService 방어

- **작성팀**: 개발팀
- **작성일**: 2026-04-20
- **근거 기획서**: [[access-log-userid-fix]] v2 (사용자 최종승인)
- **상태**: v2 (codex 1차 검토 ⚠수정필요 반영)
- **v1→v2 변경점**:
  1. V019 게이트 강화 — `expected_cnt`를 UPDATE 대상과 동일 범위로 재정의, 불일치 시 EXCEPTION
  2. 백업 테이블 타임스탬프화 (`_YYYYMMDD_HHMMSS`), 동명 존재 시 즉시 실패
  3. Orphan Guard 예외 정책 섹션 신설 + 허용/차단 테스트 케이스 추가
  4. 롤백 절차에 실행ID(타임스탬프) 기반 복원 쿼리 + 검증 쿼리 명시

---

## 0. 전제 / 환경

### 0-1. DB
- PostgreSQL, 스키마 `public`, `postgres` 계정

### 0-2. 범위 고정
- **2가지 조치만**:
  1. `access_logs` 오염 레코드 UPDATE (27건 추정)
  2. `LogService` 상수 통일 + Orphan Guard + 테스트
- **users 테이블 수정 0** (참조만)
- **동시 레거시 정리 금지**: S9(action_type/menu_nm) 등 다른 스프린트 미침입

### 0-3. 상수 통일 기준
- `LogService.ANONYMOUS_USERID = "anonymousUser"` (Spring Security 기본값)
- 현재 코드 `"anonymous"` 오탈자 → `ANONYMOUS_USERID` 상수 사용

### 0-4. Guard 예외 정책 (v2 신설)
- **허용 특수 userid 화이트리스트** (users 테이블 조회 생략, Guard 통과):
  - `anonymousUser` — Spring Security 기본값
  - `system` — 시스템/배치 경유 로그 (예약)
  - `scheduler` — 스케줄러 경유 로그 (예약)
- 상수: `LogService.SYSTEM_USERIDS = Set.of("anonymousUser","system","scheduler")`
- Guard 판정: `userid ∉ SYSTEM_USERIDS && !userRepository.existsByUserid(userid)` → WARN + fallback to `ANONYMOUS_USERID`
- **근거**: 기획서 v2 §특수 userid 고려 (plans §265, §267)

---

## 1. 작업 순서

### Step 1 — 사전검증 (FR-0)

**1-1. 러너 작성**: `docs/exec-plans/access-log-userid-precheck.java`
- 실행 안전통제: `SET TRANSACTION READ ONLY` + `statement_timeout 10s`
- 쿼리 3종:
  ```sql
  -- (a) 오염 분포 + status 표기
  SELECT userid, COUNT(*) AS cnt,
         CASE WHEN EXISTS (SELECT 1 FROM users u WHERE u.userid = access_logs.userid) THEN 'valid'
              WHEN userid = 'anonymousUser' THEN 'anonymous'
              WHEN userid = 'anonymous' THEN 'legacy-anonymous'
              ELSE 'orphan' END AS status
    FROM access_logs GROUP BY userid ORDER BY cnt DESC;

  -- (b) 오염값별 매칭 후보 (username 1:1 매칭)
  SELECT al.userid AS dirty, u.userid AS candidate_userid, u.username, COUNT(al.log_id) AS log_cnt
    FROM access_logs al
    LEFT JOIN users u ON u.username = al.userid
   WHERE al.userid NOT IN (SELECT userid FROM users) AND al.userid <> 'anonymousUser'
   GROUP BY al.userid, u.userid, u.username;

  -- (c) 오염 레코드 total = 향후 UPDATE row count 기대값
  SELECT COUNT(*) FROM access_logs
   WHERE userid NOT IN (SELECT userid FROM users)
     AND userid NOT IN ('anonymousUser', 'anonymous');
  ```
- 결과: `docs/exec-plans/access-log-userid-precheck-result.md`

**1-2. Ambiguous 매칭 분석**

결과의 (b) 컬럼 `candidate_userid` 에 따라 3가지:
- **1:1 매칭 (단일 후보)**: 자동 UPDATE 대상
- **1:N 매칭 (동명이인)**: 사용자에게 선택 요청 → 본 스프린트에서 수동 결정 문서화
- **0 매칭 (삭제된 사용자)**: `'unknown_archived'` 또는 `'anonymousUser'` 중 선택

**사용자 승인 게이트**: (b) 결과를 감사 문서에 복사 + 매핑 확정 표로 기록 후에만 Step 2 진행.

### Step 2 — 마이그레이션 SQL (FR-1)

**2-1. 파일**: `swdept/sql/V019_access_log_userid_cleanup.sql`

```sql
-- ============================================================
-- V019: access_logs.userid 오염 정제
-- Sprint: access-log-userid-fix (2026-04-20)
-- 근거: docs/product-specs/access-log-userid-fix.md v2
-- 매핑표: docs/exec-plans/access-log-userid-mapping.md (사전검증 후 확정)
--
-- [v2 변경점]
-- - 실행ID(타임스탬프) 기반 백업 테이블 — 매회 신규 생성
-- - expected_cnt를 UPDATE 대상과 동일 범위로 고정
-- - actual_cnt != expected_cnt 시 EXCEPTION (부분 성공 커밋 금지)
-- ============================================================

-- 실행ID 주입: 아래 :run_id 를 실행 스크립트에서 YYYYMMDD_HHMMSS 로 치환
-- ex) sed "s/:run_id/$(date +%Y%m%d_%H%M%S)/g" V019_*.sql | psql

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패 (롤백 신뢰성 보호)
DO $$
BEGIN
  IF to_regclass('public.access_logs_cleanup_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table access_logs_cleanup_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 실행 전 스냅샷 백업 (NFR-7) — UPDATE 대상과 동일 범위
CREATE TABLE access_logs_cleanup_backup_:run_id AS
  SELECT * FROM access_logs
   WHERE userid IN ('박욱진', '관리자')  -- ★ 매핑표로 최종 확정된 대상만
     AND userid NOT IN (SELECT userid FROM users);

-- (2) 게이트 검증 — expected_cnt는 UPDATE 대상과 동일 범위
DO $$
DECLARE expected_cnt bigint; actual_cnt bigint; backup_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO expected_cnt FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);

  SELECT COUNT(*) INTO backup_cnt FROM access_logs_cleanup_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  -- (3) UPDATE 실행 — WHERE 범위는 expected_cnt 범위와 동일
  WITH cleanup AS (
    UPDATE access_logs SET userid = CASE userid
      WHEN '박욱진' THEN 'ukjin914'
      WHEN '관리자' THEN 'admin'
    END
    WHERE userid IN ('박욱진', '관리자')
      AND userid NOT IN (SELECT userid FROM users)  -- 멱등성
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM cleanup;

  -- (4) 동등 비교 게이트 — 불일치 시 EXCEPTION (부분 성공 커밋 금지)
  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: cleaned % rows (exact match, backup=access_logs_cleanup_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (5) 사후 검증 — 매핑 대상 잔존 0건
DO $$
DECLARE remaining bigint;
BEGIN
  SELECT COUNT(*) INTO remaining FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);
  IF remaining <> 0 THEN
    RAISE EXCEPTION 'HALT post: % orphan rows remain after UPDATE', remaining;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
```

**주**: `anonymous → anonymousUser` 통일 및 기타 ambiguous 매핑이 있을 경우, Step 1-2 매핑표 확정 후 별도 `V019B_*.sql` 로 분리 실행 (한 트랜잭션에 섞지 않음).

**2-2. Diff 리포트 러너**: `docs/exec-plans/access-log-userid-diff.java`
- 실행 후 호출
- 쿼리: 백업 테이블 vs 현재 access_logs 집계 비교
- 출력: `docs/exec-plans/access-log-userid-diff-result.md`

### Step 3 — LogService 개조 (FR-2)

**3-1. UserRepository**: `existsByUserid(String)` 메서드 확인/추가

**3-2. LogService 수정**: `src/main/java/com/swmanager/system/service/LogService.java`
```java
@Slf4j
@Service
public class LogService {
    public static final String ANONYMOUS_USERID = "anonymousUser";  // v2 상수
    /** v2: Guard 화이트리스트 — users 테이블 조회 생략 */
    public static final Set<String> SYSTEM_USERIDS =
        Set.of(ANONYMOUS_USERID, "system", "scheduler");

    @Autowired private AccessLogRepository accessLogRepository;
    @Autowired private UserRepository userRepository;  // Guard 용

    public void log(String menuNm, String actionType, String detail) {
        try {
            HttpServletRequest request = ...;
            String ip = request.getRemoteAddr();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userid = ANONYMOUS_USERID;
            String username = "";

            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
                userid = user.getUsername();
                username = user.getUser().getUsername();
            } else if (auth != null && auth.isAuthenticated()
                       && !ANONYMOUS_USERID.equals(auth.getName())) {
                userid = auth.getName();
            }

            // Orphan Guard — 화이트리스트는 통과, 그 외는 users 검증
            if (!SYSTEM_USERIDS.contains(userid)
                    && !userRepository.existsByUserid(userid)) {
                log.warn("ACCESS_LOG_USERID_ORPHAN: userid='{}' not in users — writing as {}",
                         userid, ANONYMOUS_USERID);
                userid = ANONYMOUS_USERID;
            }

            AccessLog entry = new AccessLog();
            entry.setUserid(userid);
            entry.setUsername(username);
            ...
            accessLogRepository.save(entry);
        } catch (Exception e) {
            log.error("로그 저장 실패: {}", e.getMessage());
        }
    }
}
```

### Step 4 — 테스트 작성 (FR-3)

**파일**: `src/test/java/com/swmanager/system/service/LogServiceTest.java`

7+ 시나리오:
1. `customUserDetails_principal_saves_userid`
2. `null_auth_defaults_to_anonymousUser`
3. `auth_name_anonymousUser_saves_anonymousUser`
4. `valid_auth_getName_saves_that_userid`
5. `orphan_userid_triggers_warn_and_falls_back_to_anonymousUser` (ListAppender로 WARN 검증)
6. `empty_userid_does_not_crash` (방어적)
7. **`system_userid_bypasses_orphan_guard_and_no_user_query`** (v2) — `auth.getName()="system"` → WARN 없음 + `userRepository.existsByUserid` **미호출** (verify(userRepository, never()))
8. **`scheduler_userid_bypasses_orphan_guard`** (v2) — `"scheduler"` 유사 케이스
9. **`unknown_userid_triggers_guard_even_if_looks_like_system`** (v2) — `"sys"`, `"SYSTEM"`(대소문자) 등 화이트리스트 밖 값은 정상 Guard 경로

`@SpringBootTest` + MockMvc 불필요. `@ExtendWith(MockitoExtension.class)` + `@Mock AccessLogRepository` + `@Mock UserRepository` 로 LogService 단위 테스트.

### Step 5 — V019 실행

```bash
JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
RUN_ID=$(date +%Y%m%d_%H%M%S)
# :run_id 치환 후 실행 (동명 존재 시 (0) 게이트에서 즉시 실패)
sed "s/:run_id/${RUN_ID}/g" swdept/sql/V019_access_log_userid_cleanup.sql \
  > /tmp/V019_applied_${RUN_ID}.sql
DB_PASSWORD='***' java -cp "$JAR" docs/exec-plans/legacy-contract-apply.java \
  /tmp/V019_applied_${RUN_ID}.sql
# 기대 로그: [NOTICE] PASS: cleaned N rows (exact match, backup=access_logs_cleanup_backup_<RUN_ID>)
# ★ RUN_ID 값을 본 문서 §Step 5 실행 로그 섹션에 기록 (롤백 근거)

# Diff 리포트
DB_PASSWORD='***' BACKUP_TABLE="access_logs_cleanup_backup_${RUN_ID}" \
  java -cp "$JAR" docs/exec-plans/access-log-userid-diff.java
```

**실행 로그 (실행 후 채움)**:
- `RUN_ID`: _________________
- 백업 테이블명: `access_logs_cleanup_backup_<RUN_ID>`
- 정제 row 수: _____ (= expected_cnt)

### Step 6 — 멱등성 + Guard 확인

**6-1. V019 재실행** → updated_cnt = 0, HALT 아님, 정상 종료 (NFR-8)

**6-2. Guard 동작 확인**
- 서버 재시작 후 임의 시점에 수동으로 `access_logs` INSERT via JdbcTemplate (테스트 환경)
  - 또는 통합 테스트에서 MockMvc로 유효하지 않은 세션으로 요청 → WARN 로그 확인
- `server.log` grep: `rg 'ACCESS_LOG_USERID_ORPHAN' logs/` (발생 시 1회 이상)

### Step 7 — 문서 갱신

- `docs/generated/audit/data-architecture-utilization-audit.md` S5 완료
- `docs/design-docs/data-architecture-roadmap.md` S5 완료

### Step 8 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
DB_PASSWORD='***' ./mvnw -q test -Dtest='LogServiceTest'
bash server-restart.sh
```

**회귀 스모크**:
1. `/login` → 로그인
2. `/document/list` 접근 → `access_logs` 새 레코드 userid = 로그인한 사용자의 users.userid
3. 브라우저 재시작 후 `/login` 로그인 전 `/` 접근 → `userid = anonymousUser` 저장 확인

### Step 9 — 커밋 / 푸시

---

## 2. 테스트 / 검증 (T#)

| ID | 내용 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | 사전검증 (FR-0) | precheck 러너 | 3종 쿼리 결과 생성 + Ambiguous 없음 or 수동 확정 |
| T2 | V019 실행 성공 | `[NOTICE] PASS: cleaned N rows (exact match, backup=...)` | 출력 확인 + RUN_ID 기록 |
| T3 | 매핑 대상 잔존 0건 (NFR-3) | `SELECT COUNT(*) FROM access_logs WHERE userid IN ('박욱진','관리자')` | 0 |
| T4 | 스냅샷 백업 테이블 존재 (NFR-7) | `\d access_logs_cleanup_backup_<RUN_ID>` | 테이블 존재 + 행 수 = expected_cnt |
| T4-B | 백업 중복 방지 (v2) | V019 동일 RUN_ID 재실행 | (0) 게이트에서 `HALT: backup table ... already exists` EXCEPTION |
| T5 | Diff 리포트 생성 | `ls docs/exec-plans/access-log-userid-diff-result.md` | 파일 존재 |
| T6 | 멱등성 (NFR-8) | V019 2회차 실행 | updated_cnt=0, 정상 종료 |
| T7 | 익명 식별자 통일 (NFR-9) | `rg -P '"anonymous"(?!User)' src/main/java/com/swmanager/system/service/LogService.java` | 0 hits (모두 `ANONYMOUS_USERID` 상수 또는 `"anonymousUser"` 리터럴) |
| T8 | LogService 단위 테스트 (NFR-4) | `./mvnw test -Dtest='LogServiceTest'` | 7+ 시나리오 PASS |
| T9 | Guard WARN 검증 | T8-5 케이스 | ListAppender로 `ACCESS_LOG_USERID_ORPHAN` 키워드 포함 WARN 확인 |
| T9-B | Guard 화이트리스트 우회 검증 (v2) | T8-7/8 케이스 | `verifyNoInteractions(userRepository)` PASS (조회 자체 없음 보장) |
| T10 | 회귀 스모크 (NFR-5/6) | §1 Step 8 3개 화면 | 정상 렌더링 + access_logs 새 레코드 userid 정상 |
| T11 | 컴파일 | `./mvnw -q clean compile` | BUILD SUCCESS |
| T12 | 서버 기동 | `bash server-restart.sh` | `Started` + ERROR 0 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| FR-0 실패 | V019 미실행. 원인 분석 |
| V019 트랜잭션 실패 | 자동 ROLLBACK. 백업 테이블은 (0) 게이트에서 중복이면 실패하므로 **항상 그 실행의 전용 테이블**만 남음 |
| Ambiguous 매핑 실수 발견 / 매핑 오류 롤백 | **반드시 해당 실행ID의 백업 테이블을 사용** (실행 로그에서 `backup=access_logs_cleanup_backup_<run_id>` 확인).<br>복원 쿼리:<br>`UPDATE access_logs al SET userid = b.userid FROM access_logs_cleanup_backup_<run_id> b WHERE al.log_id = b.log_id;`<br>**복원 후 검증** (필수):<br>1) `SELECT COUNT(*) FROM access_logs WHERE userid IN ('박욱진','관리자');` → 백업 row 수와 일치<br>2) `SELECT COUNT(*) FROM access_logs al JOIN access_logs_cleanup_backup_<run_id> b ON al.log_id=b.log_id WHERE al.userid<>b.userid;` → 0 |
| LogService Guard 부작용 (정상 로그도 anonymous로) | LogService.java revert, V019는 유지 (DB는 이미 정제된 상태라 Guard 없이도 안전) |
| 배포 후 회귀 | `git revert <commit>` 후 위 복원 쿼리 실행 |

**롤백 기준 커밋**: `d859492` (S2 직후)
**롤백 대상 실행ID**: V019 실행 시 로그에 출력된 `backup=access_logs_cleanup_backup_<run_id>` 를 본 문서 §Step 5 실행 로그 섹션에 **반드시 기록**.

---

## 4. 리스크·완화 재확인

| ID | 리스크 | 수준 | 본 개발계획서 적용 |
|----|--------|------|-------------------|
| R-1 | Ambiguous 매칭 잘못 | 중간 | Step 1-2 사용자 승인 게이트 + WHERE IN 으로 안전 제한 |
| R-2 | Guard 부작용 | 낮음 | T9 WARN assert + 운영 환경 초기 모니터링 |
| R-3 | users 조회 성능 | 낮음 | users 10건이라 미미 |
| R-4 | 로그 무결성 훼손 | 낮음 | actionDetail/access_time 원본 유지, userid만 수정 |
| R-5 | LogService 외 경로 재오염 | 중간 | FR-2-X 명시, 백로그 기록. 운영 WARN 로그 모니터링 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 확인 사항
- [ ] §0-2 범위 고정 (2가지 조치만)
- [ ] §1 Step 1 사전검증 → Ambiguous 매핑 확정 사용자 게이트
- [ ] §1 Step 2 V019 게이트 + 멱등성 WHERE + 스냅샷
- [ ] §1 Step 3 LogService 상수 통일 + Guard
- [ ] §1 Step 4 테스트 5+ 케이스
- [ ] §2 T1~T12 체크리스트
- [ ] §3 롤백 절차 (백업 테이블 기반)
