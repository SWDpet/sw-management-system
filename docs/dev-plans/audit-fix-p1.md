# [개발계획서] 감사 후속조치 스프린트 1 — P1 보안 4건

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/plans/audit-fix-p1.md](../plans/audit-fix-p1.md) (승인됨)
- **상태**: v2 (codex 재검토 대기) — 구현 중 프론트 사용처 확인 후 Option C 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 (codex ✅ 승인) |
| v2 | 구현 착수 후 발견: `doc-commence.html` 이 `/api/user/{userSeq}` 의 ssn/certificate 를 실제 사용 중. codex 판단 "Option C 엔드포인트 분리" 채택 — 기본 API 는 비민감만(FR-3-1 유지), 신규 `/secure` API 에 민감 필드 + EDIT 권한 (FR-3-4 신설). 프론트는 `/secure` 로 호출 전환. |

---

## 1. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| Config | `src/main/resources/application.properties` | `DB_PASSWORD` 기본값 제거 |
| Config | `src/main/resources/application-local.properties` | `spring.datasource.password` 평문 → `${DB_PASSWORD}` |
| DB | `src/main/resources/db_init_phase2.sql` | `inspect_report` CREATE 에 `insp_sign TEXT`/`conf_sign TEXT` 추가 + 기존 DB 보정용 `ALTER ADD COLUMN IF NOT EXISTS` |
| Backend | `src/main/java/com/swmanager/system/controller/DocumentController.java` | (a) 4 엔드포인트에 `getAuth()` EDIT 체크 추가, (b) 2 응답에서 민감 필드 제거, (c) 2 `/secure` 엔드포인트 신규 (EDIT 권한 + 민감필드 포함) |
| Frontend | `src/main/resources/templates/document/doc-commence.html` | `/api/user/{userSeq}` 호출 3곳을 `/secure` 버전으로 변경 |
| Docs | `docs/audit/2026-04-18-system-audit.md` | 1-1, 1-2, 1-3, 2-1 항목 체크박스 `☑ 조치함` |

**수정 5개 파일. 신규 없음.**

---

## 2. 파일별 상세

### 2-1. `application.properties` (라인 22)

**Before**:
```properties
spring.datasource.password=${DB_PASSWORD:1qkrdnrwls!}
```

**After**:
```properties
# DB_PASSWORD 환경변수 필수. 미설정 시 서버 부팅 실패 (의도된 동작).
spring.datasource.password=${DB_PASSWORD}
```

### 2-2. `application-local.properties` (라인 7)

기존:
```properties
spring.datasource.password=1qkrdnrwls!
```

변경:
```properties
# 로컬 환경: DB_PASSWORD 환경변수에서 읽음. 미설정 시 부팅 실패.
spring.datasource.password=${DB_PASSWORD}
```

### 2-3. `db_init_phase2.sql` — `inspect_report` 보강

**기존 CREATE TABLE (67~85 라인) 에 2컬럼 추가**:
```sql
CREATE TABLE IF NOT EXISTS inspect_report (
    id              BIGSERIAL PRIMARY KEY,
    pjt_id          BIGINT NOT NULL REFERENCES sw_pjt(proj_id) ON DELETE CASCADE,
    inspect_month   VARCHAR(7),
    sys_type        VARCHAR(20),
    doc_title       VARCHAR(300),
    insp_company    VARCHAR(100),
    insp_name       VARCHAR(50),
    conf_org        VARCHAR(100),
    conf_name       VARCHAR(50),
    insp_dbms       VARCHAR(200),
    insp_gis        VARCHAR(200),
    dbms_ip         VARCHAR(50),
    status          VARCHAR(20) DEFAULT 'DRAFT',
    insp_sign       TEXT,       -- 신규: 점검자 서명 Base64 PNG
    conf_sign       TEXT,       -- 신규: 확인자 서명 Base64 PNG
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);
```

**기존 DB 인스턴스 보정 (멱등 ALTER)** — 테이블 생성 직후 한 블록으로:
```sql
-- [감사 후속조치 P1 2-1] inspect_report 엔티티-스키마 정합성
-- 기존 DB 인스턴스에 컬럼이 없을 경우 보정. IF NOT EXISTS 로 멱등.
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS insp_sign TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS conf_sign TEXT;
```

### 2-4. `DocumentController.java` — 권한 검사 4곳 추가

기존 패턴(다른 엔드포인트와 동일):
```java
if (!"EDIT".equals(getAuth())) {
    Map<String, Object> forbidden = new LinkedHashMap<>();
    forbidden.put("success", false);
    forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "수정 권한이 없습니다"));
    return ResponseEntity.status(403).body(forbidden);
}
```

**추가 위치 4곳**:

| 라인 | 메서드 | 추가 방식 |
|------|--------|-----------|
| 1031 | `saveContractParticipants` (POST) | 메서드 시작부. 반환 타입 `Map<String,Object>` → try 진입 전 EDIT 체크 후 `result.put("error",...)` 반환 or `ResponseEntity` 로 변경 |
| 1147 | `savePlanData` (POST) | `ResponseEntity<Map<String,Object>>` 반환 — 상단에 403 ResponseEntity 반환 |
| 1256 | `saveInspectReport` (POST) | `ResponseEntity<?>` 반환 — 상단에 403 ResponseEntity 반환 |
| 1322 | `deleteInspectReport` (DELETE) | 상동 |

**`saveContractParticipants` 반환 타입 이슈**:
- 기존: `Map<String,Object>` 단순 반환
- 선택지 A: 반환 타입 유지 + `result.put("error", ...)` 로 에러 표현 (HTTP 는 200 이지만 success=false)
- 선택지 B: 반환 타입을 `ResponseEntity<Map<String,Object>>` 로 변경 (프론트 호환성 확인 필요)
- **결정**: 선택지 A — 기존 success=false 패턴 유지, HTTP 200 이지만 `code=FORBIDDEN` 필드 추가. 프론트 호환성 안전.

### 2-5. `DocumentController.java` — 민감 필드 제거 2곳

#### 2-5-1. `getUserInfo` (594 ~ 614) — 비민감만 반환
기존 `data.put` 중 **`ssn`, `certificate`, `email` 3개 제거**.  
유지: `userSeq, username, positionTitle, position, techGrade, mobile, tel, address, tasks, deptNm, teamNm, careerYears, fieldRole`

#### 2-5-2. `getContractParticipants` (1007 ~ 1027) — 비민감만 반환
기존 `m.put` 중 **`ssn`, `certificate` 2개 제거**. `tasks` 는 업무 내용이라 유지.

#### 2-5-3. `getUserInfoSecure` (신규) — 민감 필드 포함, EDIT 권한
새 엔드포인트:
```java
@GetMapping("/api/user/{userSeq}/secure")
@ResponseBody
public ResponseEntity<?> getUserInfoSecure(@PathVariable Long userSeq) {
    if (!"EDIT".equals(getAuth())) {
        return ResponseEntity.status(403).body(Map.of(
            "error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다")));
    }
    return userRepository.findById(userSeq).map(u -> {
        Map<String, Object> data = new LinkedHashMap<>();
        // 비민감 필드도 함께 반환 (프론트가 한 번에 처리하도록)
        data.put("userSeq", u.getUserSeq());
        data.put("username", u.getUsername());
        data.put("positionTitle", u.getPositionTitle());
        data.put("position", u.getPosition());
        data.put("techGrade", u.getTechGrade());
        data.put("mobile", u.getMobile());
        data.put("tel", u.getTel());
        data.put("address", u.getAddress());
        data.put("tasks", u.getTasks());
        data.put("deptNm", u.getDeptNm());
        data.put("teamNm", u.getTeamNm());
        data.put("careerYears", u.getCareerYears());
        data.put("fieldRole", u.getFieldRole());
        // 민감 필드
        data.put("ssn", u.getSsn());
        data.put("certificate", u.getCertificate());
        data.put("email", u.getEmail());
        return ResponseEntity.ok((Object) data);
    }).orElse(ResponseEntity.notFound().build());
}
```

#### 2-5-4. `getContractParticipantsSecure` (신규)
패턴 동일: `/api/contract-participants/{projId}/secure` + EDIT 권한 + ssn/certificate 포함.

### 2-6. Frontend — `doc-commence.html` 엔드포인트 교체

`grep` 결과 3 위치에서 `/document/api/user/{userSeq}` 호출:
- `:462` (fillParticipant 함수)
- `:1006` (또 다른 채우기 함수)
- `:1280` (또 다른 위치)

→ 모두 `/document/api/user/{userSeq}/secure` 로 변경. (해당 페이지는 과업참여자 편집이라 EDIT 권한 전제)

### 2-7. 감사 보고서 체크박스 업데이트 (기존 2-6 리넘버링)

### 2-6. 감사 보고서 체크박스 업데이트

`docs/audit/2026-04-18-system-audit.md` 의 1-1, 1-2, 1-3, 2-1 항목의 `☐ 조치함` → `☑ 조치함` 으로 수정.

---

## 3. 작업 순서

| Step | 작업 | 검증 |
|------|------|------|
| 1 | 프론트 민감필드 사용처 grep 조사 (`ssn`, `certificate`, `email` in templates) | 사용처 목록 확보 |
| 2 | `application.properties` 기본값 제거 | diff |
| 3 | `application-local.properties` 평문 → `${DB_PASSWORD}` | diff |
| 4 | `db_init_phase2.sql` — CREATE 에 2컬럼 + ALTER 2줄 추가 | diff |
| 5 | `DocumentController.java` — 권한 체크 4곳 + 민감 필드 2곳 제거 | diff |
| 6 | 프론트 영향 있으면 해당 템플릿 수정 (Step 1 결과 기반) | diff |
| 7 | 감사 보고서 체크박스 업데이트 (4곳) | diff |
| 8 | `./mvnw.cmd compile` | BUILD SUCCESS |
| 9 | **환경변수 `DB_PASSWORD` 설정 확인** (없으면 재기동 실패함) | 설정됨 |
| 10 | `bash server-restart.sh` | `Started SwManagerApplication` |
| 11 | 회귀 테스트 T1~T9 | 모두 PASS |
| 12 | codex Specs 중심 검증 | ✅ |
| 13 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 4. 회귀 테스트

| # | 시나리오 | 기대 결과 | Specs |
|---|---------|-----------|-------|
| T1 | `spring.datasource.password` grep in application*.properties | 평문 값(`1qkrdnrwls!`) **0 hits** | FR-1-1 |
| T2 | 환경변수 `DB_PASSWORD` 설정 + 서버 기동 | 정상 기동 | FR-1-2 |
| T3 | (의도적 실패 시나리오) `DB_PASSWORD` 언셋 후 기동 시도 | 부팅 실패 로그 `Could not resolve placeholder 'DB_PASSWORD'` | FR-1-2 |
| T4 | NOT-EDIT 권한 사용자로 `POST /document/api/contract-participants/{id}` | `success=false`, `error.code=FORBIDDEN` | FR-2-1 |
| T5 | EDIT 권한 사용자로 동일 엔드포인트 | 기존대로 정상 처리 | 회귀 |
| T6 | T4, T5 를 `/api/plan/{id}`, `/api/inspect-report`(POST/DELETE) 에도 반복 | 동일 패턴 | FR-2-1 |
| T7 | `curl /document/api/user/{userSeq}` JSON 응답 | `ssn`, `certificate`, `email` 키 **없음** (비민감만) | FR-3-1 |
| T8 | `curl /document/api/contract-participants/{projId}` JSON 응답 | `ssn`, `certificate` 키 **없음** (비민감만) | FR-3-1 |
| T7-B | (EDIT 권한 유저) `curl /document/api/user/{userSeq}/secure` | 200 + 민감 필드 포함 | FR-3-4 |
| T7-C | (비-EDIT 유저) 동일 | 403 + `code: FORBIDDEN` | FR-3-4 |
| T8-B | 동일 패턴 for `/contract-participants/{projId}/secure` | EDIT 유저 200, 비-EDIT 403 | FR-3-4 |
| T8-C | `doc-commence.html` 기능 회귀 — 과업참여자 선택 시 SSN/자격증 input 채워짐 | 정상 (EDIT 권한자 기준) | 기능 회귀 |
| T9 | InspectReport 저장/조회 → 서버 로그 | `insp_sign`/`conf_sign` 관련 SQL 오류 **없음** | FR-4-1 |
| T10 | 서명 포함 점검내역서 save → DB 에 insp_sign/conf_sign 채움 | NULL 아닌 값 저장 | FR-4-1 |

---

## 5. 롤백 전략

### 원자적 롤백
```bash
git revert <sha>
git push
bash server-restart.sh
```

### 롤백 검증 체크포인트
1. 빌드·기동 성공
2. `application.properties` 평문 비밀번호 복원 (`DB_PASSWORD` 환경변수 없어도 부팅 가능)
3. DocumentController 에 권한 검사 없음 확인
4. 응답 JSON 에 `ssn`, `certificate`, `email` 복원 확인
5. `inspect_report` 에 `insp_sign`, `conf_sign` 컬럼은 **그대로 유지** (`ALTER ADD COLUMN IF NOT EXISTS` 은 되돌릴 필요 없음 — 추가된 컬럼이 호환성 해치지 않음)

---

## 6. 빌드 · 재시작

- Java 변경(DocumentController) → **빌드 필요**
- Config / SQL / 템플릿 → **서버 재시작 필요**
- **사전 조건**: `DB_PASSWORD` 환경변수 필수 (Step 9)

---

## 7. 예상 소요

| 작업 | 시간 |
|------|------|
| 프론트 grep 조사 | 3 분 |
| Config 2개 파일 수정 | 2 분 |
| init SQL 수정 | 3 분 |
| DocumentController 6곳 수정 | 15 분 |
| 프론트 수정 (있을 시) | 10 분 |
| 보고서 체크박스 | 2 분 |
| 빌드·재시작 | 5 분 |
| 회귀 테스트 T1~T10 | 15 분 |
| codex 검증 | 5 분 |
| **합계** | **~60 분** |

---

## 8. 체크리스트

- [ ] 프론트 민감필드(`ssn`/`certificate`/`email`) 사용처 grep 조사
- [ ] `application.properties` password 기본값 제거
- [ ] `application-local.properties` 평문 제거
- [ ] `db_init_phase2.sql` inspect_report — 2컬럼 + ALTER 2줄
- [ ] `DocumentController.java` — 4 엔드포인트 auth 체크
- [ ] `DocumentController.java` — getUserInfo 에서 ssn/cert/email 제거
- [ ] `DocumentController.java` — getContractParticipants 에서 ssn/cert 제거
- [ ] 프론트 템플릿 조정 (있을 시)
- [ ] 감사 보고서 1-1, 1-2, 1-3, 2-1 체크박스 업데이트
- [ ] `DB_PASSWORD` 환경변수 설정 확인 → 서버 재시작 성공
- [ ] 회귀 테스트 T1~T10 통과 (**T3 의도적 부팅 실패 포함**)
- [ ] codex Specs 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
