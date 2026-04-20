---
tags: [plan, sprint, refactor, data-cleanup, logging]
sprint: "access-log-userid-fix"
status: draft-v2
created: "2026-04-20"
---

# [기획서] access_logs.userid 오염 정제 + 로깅 유틸 방어

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행 커밋**: `d859492` (S2 process-master-dedup 완료)
- **근거**: [[data-architecture-roadmap]] v2 P1 Wave 1 S5 / [[data-architecture-utilization-audit]] §7-3
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (codex v1 피드백 5건 반영)
1. **기준 익명 식별자 `anonymousUser` 통일** — 현재 `LogService.java:28` 기본값 `"anonymous"` vs DB 실값 `"anonymousUser"` 불일치. 전역 상수 `LogService.ANONYMOUS_USERID = "anonymousUser"` 단일 소스
2. **FR-1 실행 게이트** — 사전 SELECT 결과와 UPDATE row count 일치 시만 COMMIT. 트랜잭션 내 검증
3. **SQL 멱등성** — `WHERE userid IN (대상 목록) AND userid NOT IN (SELECT userid FROM users)` 조건으로 재실행 시 0건 UPDATE
4. **NFR 운영 안전장치** — 실행 전 스냅샷(백업 테이블 또는 CSV) + 실행 후 diff 리포트
5. **테스트 확대** — `null auth`, `anonymousUser auth`, WARN 로그 메시지 검증, LogService 외 경로 최소 통합 점검

---

## 1. 배경 / 목표

### 배경 — 27건 userid 이름 오염

Batch C 스캔 결과 `access_logs.userid`(3,885건 중):
- ✅ 9종 정상 매칭 (admin 1418, hanjun 992, seohg0801 739, ukjin914 640, test02 36, parksh 13, yeohj 8, ybkang 4, jeongsj 4)
- ❌ **`박욱진` 16건** — 사용자 **이름(username)** 이 userid 필드에 저장됨
- ❌ **`관리자` 11건** — 동일
- ℹ️ `anonymousUser` 4건 — Spring Security 기본값, 로그인 전 접근 기록 (정상)

### 원인 분석

`LogService.java:22-52` 현재 로직:
```java
if (auth.getPrincipal() instanceof CustomUserDetails) {
    userid = user.getUsername();              // CustomUserDetails.getUsername() = userid ✅
    username = user.getUser().getUsername();   // User.getUsername() = 실명
} else if (auth != null) {
    userid = auth.getName();                   // 정상 반환
}
```

**현재 LogService는 올바름**. 그러나 DB에는 이미 오염 27건 존재. 가능한 원인:
1. **과거 로직 버그** — 이전 커밋에서 `user.getUser().getUsername()`을 userid 필드에 잘못 저장 (Spring Security의 `UserDetails.getUsername()` 관례가 "username"이라 혼동)
2. **다른 경로** — 로그인 직후(CustomUserDetails 매핑 전), `anonymousUser` → 실명 처리 시점의 race condition 등
3. **테스트 데이터** — 개발/테스트 중 수동 INSERT

### 목표
1. **27건 데이터 정제** — `박욱진`/`관리자` userid를 `users.userid` 정상 값으로 UPDATE
2. **로깅 유틸 방어 강화** — 혹시 모를 재발 방지를 위한 Guard 추가:
   - `userid` 값이 **실제 users.userid에 없는 경우 'anonymousUser'로 강제** (또는 `unknown` prefix)
   - `username`/`userid` 혼동 가능성 차단
3. **선택적** — DB 레벨 제약 (CHECK 또는 FK) 검토
4. **회귀 테스트** — 테스트 작성

---

## 2. 기능 요건 (FR)

### FR-0. 사전 검증

```sql
-- (1) 오염 분포 재확인
SELECT userid, COUNT(*) AS cnt,
       CASE WHEN EXISTS (SELECT 1 FROM users WHERE users.userid = access_logs.userid) THEN '✅'
            WHEN userid = 'anonymousUser' THEN 'ℹ️ 정상 익명'
            ELSE '❌ 오염' END AS status
  FROM access_logs GROUP BY userid ORDER BY cnt DESC;

-- (2) 오염값과 매칭 가능한 users 탐색 (실명 기준)
SELECT al.userid AS dirty, u.userid AS candidate_userid, u.username, COUNT(al.log_id) AS log_cnt
  FROM access_logs al
  LEFT JOIN users u ON u.username = al.userid
 WHERE al.userid NOT IN (SELECT userid FROM users)
   AND al.userid <> 'anonymousUser'
 GROUP BY al.userid, u.userid, u.username;
```

**기대 결과**:
- (1) 9종 정상 + 1종 anonymous + **2~N종 오염**
- (2) `박욱진 → ukjin914 (username=박욱진)`, `관리자 → admin (username=관리자)` 같은 1:1 매칭 또는 ambiguous

**Ambiguous 처리**:
- 동명이인으로 여러 users 매칭 시 **매칭 후보 전부 출력** → 사용자 선택 필요 (개발계획서 단계)
- 매칭 0건 (예: 삭제된 사용자) 시 `'unknown_archived'` 대체 고려

### FR-1. 데이터 정제 (UPDATE, v2 게이트 + 멱등성 강화)

#### 실행 게이트 (v2 codex 권장 #2)
트랜잭션 내 **사전 COUNT = UPDATE row count 일치 시에만 COMMIT**:

```sql
BEGIN;

-- (1) 사전 COUNT 기록
DO $$
DECLARE expected_cnt bigint; updated_cnt bigint;
BEGIN
  -- 예: 박욱진 + 관리자 건수
  SELECT COUNT(*) INTO expected_cnt FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);  -- 멱등성: 이미 정제됐으면 0

  -- (2) UPDATE with WHERE guard (멱등성)
  WITH u1 AS (
    UPDATE access_logs SET userid = 'ukjin914'
     WHERE userid = '박욱진' AND userid NOT IN (SELECT userid FROM users)
     RETURNING 1
  ),
  u2 AS (
    UPDATE access_logs SET userid = 'admin'
     WHERE userid = '관리자' AND userid NOT IN (SELECT userid FROM users)
     RETURNING 1
  )
  SELECT (SELECT COUNT(*) FROM u1) + (SELECT COUNT(*) FROM u2) INTO updated_cnt;

  -- (3) 일치 검증
  IF expected_cnt <> updated_cnt THEN
    RAISE EXCEPTION 'HALT: expected % rows but updated % rows', expected_cnt, updated_cnt;
  END IF;
  RAISE NOTICE 'PASS: cleaned % rows', updated_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
```

#### 멱등성 보장
- `WHERE userid NOT IN (SELECT userid FROM users)` 조건으로 이미 정제된 값은 매칭 안 됨
- 재실행 시: expected_cnt = 0, updated_cnt = 0 → HALT 아님, 정상 종료
- 매칭 1:1 건만 자동 정제. Ambiguous 건은 수동 검토 (개발계획서 단계)

#### 실행 전 스냅샷 (v2 codex 권장 #4)
```sql
-- 오염 레코드 백업 (복원 가능)
CREATE TABLE IF NOT EXISTS access_logs_cleanup_backup_20260420 AS
  SELECT * FROM access_logs
   WHERE userid NOT IN (SELECT userid FROM users)
     AND userid <> 'anonymousUser';
```

### FR-1-X. 실행 후 Diff 리포트
```sql
-- 실행 전/후 userid 집계 비교 (docs/dev-plans/access-log-userid-diff.md 로 기록)
SELECT 'before' AS phase, userid, COUNT(*) FROM access_logs_cleanup_backup_20260420 GROUP BY userid
UNION ALL
SELECT 'after', al.userid, COUNT(*)
  FROM access_logs al
  JOIN access_logs_cleanup_backup_20260420 b ON al.log_id = b.log_id
 GROUP BY al.userid;
```

### FR-2. LogService 방어 강화 (v2 익명 식별자 상수화)

#### v2 주요 변경
- 현재 `LogService.java:28`: `String userid = "anonymous"` ← **DB 실값 `"anonymousUser"`와 불일치**
- 전역 상수로 통일: `public static final String ANONYMOUS_USERID = "anonymousUser"`

```java
@Service
public class LogService {
    public static final String ANONYMOUS_USERID = "anonymousUser";  // v2 통일 상수

    public void log(String menuNm, String actionType, String detail) {
        try {
            ...
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userid = ANONYMOUS_USERID;  // v2: 기본값 통일
            String username = "";

            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
                userid = user.getUsername();               // userid
                username = user.getUser().getUsername();   // 실명
            } else if (auth != null && auth.isAuthenticated()
                       && !ANONYMOUS_USERID.equals(auth.getName())) {
                userid = auth.getName();
            }

            // v2 Guard: userid가 실제 users에 없고 ANONYMOUS_USERID도 아니면
            //          WARN 로그 + 저장은 ANONYMOUS_USERID로 강제 (오염 방지)
            if (!ANONYMOUS_USERID.equals(userid)
                    && !userRepository.existsByUserid(userid)) {
                log.warn("ACCESS_LOG_USERID_ORPHAN: userid='{}' not in users — writing as {}",
                         userid, ANONYMOUS_USERID);
                userid = ANONYMOUS_USERID;
            }

            // AccessLog 저장...
        } catch (Exception e) { ... }
    }
}
```

- `UserRepository.existsByUserid(String)` 메서드 신설 또는 `findByUserid(...).isPresent()` 재사용
- 빈번한 조회 성능 우려 있으나 users 10건이라 미미 (§4-4)

### FR-2-X. 재발 방지 완결성 (v2 codex 권장 #5)
"재발 방지 완료"로 단정하지 않음. LogService 외 경로 인지:
- **Spring Security AuditApplicationEvent** — 감사 이벤트 리스너 없음 확인 (rg 결과)
- **배치 작업** — 현재 배치 기능 없음 (Schedule/Cron 미사용)
- **CSV 업로드** — license_registry 업로드는 별도 파이프라인이라 access_logs 미사용
- 미래에 신규 경로 추가 시 Guard 확산 필요 — 백로그 항목 `access-log-userid-guard-extend`로 기록

### FR-3. 테스트 (v2 확장 — 5+ 시나리오)

1. **단위 테스트** — `LogServiceTest`:
   - (a) `CustomUserDetails` 로그인 → userid = CustomUserDetails.getUsername()
   - (b) `auth == null` → userid = `ANONYMOUS_USERID`
   - (c) `auth.getName() == ANONYMOUS_USERID` → userid = `ANONYMOUS_USERID`
   - (d) `auth.getName()` 이 users에 존재하는 userid → 정상 저장
   - (e) **Orphan Guard + WARN 검증** (v2): userid가 users에 없으면 WARN 로그 **발생** + 저장은 `ANONYMOUS_USERID`. `ListAppender` 로 WARN 메시지에 `ACCESS_LOG_USERID_ORPHAN` 키워드 및 원 userid 포함 검증
   - (f) **LogService 외 경로 통합 점검** (v2): `AccessLogRepository.save()` 직접 호출은 테스트 범위 외 (LogService 경유만 지원) — 회귀 테스트 README에 명시
2. **회귀 테스트** — 로그인 후 임의 메뉴 접근 → access_logs 새 레코드 userid 값이 users.userid 와 일치

### 범위 외
- `access_logs.userid`에 FK 제약 추가 → 감사 로그는 삭제된 사용자도 보존해야 하므로 FK 부적합. 범위 외.
- 다른 오염 필드(`menu_nm` 4종 누락 등)는 S9 `access-log-action-and-menu-sync`에서 처리
- `action_type` Enum 전환은 S9
- anonymousUser 4건 유지 (정상 기록)

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공 |
| NFR-2 | 서버 재기동 정상, ERROR 0 |
| NFR-3 | **오염 정제 검증**: 실행 후<br>`SELECT userid, COUNT(*) FROM access_logs WHERE userid NOT IN (SELECT userid FROM users) AND userid <> 'anonymousUser' GROUP BY userid` → **0건** (또는 ambiguous로 보류된 건만 — 명시적 기록) |
| NFR-4 | LogService 단위 테스트 **5+건** PASS (CustomUserDetails / null auth / anonymous auth / valid auth.getName / orphan guard + WARN assert) |
| NFR-7 (v2 추가) | **실행 전 스냅샷** 생성 (`access_logs_cleanup_backup_20260420` 테이블) + **Diff 리포트** 생성 (`docs/dev-plans/access-log-userid-diff.md`) |
| NFR-8 (v2 추가) | **멱등성 검증** — V019 재실행 시 updated_cnt = 0 (이미 정제 완료), HALT 아님, 정상 종료 |
| NFR-9 (v2 추가) | **익명 식별자 상수 통일** — `rg 'anonymous(?!User)' src/main/java/com/swmanager/system/service/LogService.java` 0 hits |
| NFR-5 | 회귀: 로그인 후 메뉴 접근 → access_logs 새 레코드 userid가 users.userid와 일치 |
| NFR-6 | Guard 동작 확인: 비정상 userid 입력 시 `ACCESS_LOG_USERID_ORPHAN` WARN 로그 + 저장은 anonymousUser |

---

## 4. 의사결정 / 우려사항

### 4-1. Ambiguous 매칭 처리 — ✅ 수동 검토 후 UPDATE
- FR-0 (2)에서 동명이인/삭제된 사용자 발견 시 사용자 확인 필요
- 자동화 금지 (실수 방지)

### 4-2. Guard 도입 위치 — ✅ LogService 내부
- 모든 로그 기록이 `LogService.log()`를 거치므로 단일 지점 Guard 가장 효율적
- 다른 경로(예: Spring Security AuditEvent) 별도 확인 — 본 스프린트 범위 외로 보류

### 4-3. DB 레벨 제약 — ❌ 본 스프린트 제외
- CHECK/FK 추가 시 삭제된 사용자 로그 보존 불가
- 애플리케이션 레벨 Guard로 충분 판단

### 4-4. 캐시 필요 여부 — ❌ 본 스프린트 제외
- `existsByUserid` 호출 빈도 우려 있으나 users 테이블 10건 기준 미미
- 성능 이슈 발생 시 별도 캐시 스프린트

### 4-5. 감사 외 영역 — ✅ 없음
- access_logs 는 감사 대상 (§기능 7)
- users 테이블은 참조만 (수정 0)

### 4-6. 'system' 같은 비즈니스 userid — 🔍 개발계획서 확인
- 배치 작업 등이 'system' 같은 특수 userid를 쓰는지 확인 필요
- FR-0에서 전수 파악 후 Guard 예외 목록에 추가

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 마이그레이션 SQL | `swdept/sql/V019_access_log_userid_cleanup.sql` | 신규 |
| 사전검증 러너 | `docs/dev-plans/access-log-userid-precheck.java` | 신규 |
| LogService | `src/main/java/com/swmanager/system/service/LogService.java` | 수정 (Guard 추가) |
| UserRepository | `src/main/java/com/swmanager/system/repository/UserRepository.java` | 수정 (existsByUserid 추가 시) |
| 단위 테스트 | `src/test/java/com/swmanager/system/service/LogServiceTest.java` | 신규 |
| 감사 리포트 | `docs/audit/data-architecture-utilization-audit.md` | 수정 (S5 완료 체크) |
| 로드맵 | `docs/plans/data-architecture-roadmap.md` | 수정 (S5 완료) |

**합계**: 신규 3파일, 수정 4파일. Java 수정 2파일. **DB 변경**: 27건 UPDATE.

---

## 6. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | Ambiguous 매칭으로 잘못된 UPDATE | 중간 | FR-0 (2) 결과 사용자 검토 필수. 1:1 매칭만 자동화 |
| R-2 | Guard 추가로 정상 로그 누락 | 낮음 | WARN 로그에 원 userid 기록, `userid=anonymousUser`로 저장해 추적 가능 |
| R-3 | UserRepository 조회 성능 | 낮음 | users 10건이라 미미. 향후 캐시 별도 스프린트 |
| R-4 | 감사 로그 무결성 훼손 (과거 로그 수정) | 낮음 | 감사 로그의 `userid`만 수정, `actionDetail`/`access_time` 등 이력은 원본 유지 |
| R-5 | 새로운 오염 경로 미식별 | 중간 | LogService 외 경로(AuditEvent, CSV 업로드 등)는 본 스프린트 범위 외. 별도 모니터링 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

### 승인 전 확인 사항
- [x] 원인 분석 (LogService 현재 로직은 OK, DB 오염은 과거 잔존)
- [x] Guard 도입으로 재발 방지
- [x] users 테이블 수정 0건 (참조만)
- [ ] Ambiguous 매칭 건 개발계획서 단계에서 사용자 확인

### 다음 절차
1. 사용자 "반영" → v2
2. 사용자 "최종승인" → **[개발팀]** 개발계획서 작성
