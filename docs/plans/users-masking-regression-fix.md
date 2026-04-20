---
tags: [plan, sprint, refactor, data-cleanup, security, masking]
sprint: "users-masking-regression-fix"
short_alias: "S3-B"
status: draft-v1
created: "2026-04-20"
---

# [기획서] users 테이블 마스킹 회귀 정정 + 마이페이지 가드

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행**: S3 qt-remarks-users-link 완료 (`acbfa3c`, `s3-qt-remarks-link-v1`)
- **근거**: S3 작업 중 발견된 회귀 (기획서 §1-3, 개발계획서 §0-3)
- **상태**: 초안 v1 (codex 검토 대기)
- **약칭**: S3-B (S3의 자연스러운 후속)

---

## 1. 배경

### 1-1. 마스킹 정책 (사용자 확정, 2026-04-20) — 단일 정책

**확정 정책 (한 문장)**: "**DB는 항상 unmasked 원본을 저장하고, 일반 화면(마이페이지·관리자·로그·디버그 포함)에서는 SensitiveMask 출력 형태로 마스킹 표시한다. 단, 문서관리·견적서(PDF 포함)에서는 항상 unmasked 원본을 노출한다.**"

| 영역 | 표시 정책 |
|------|----------|
| **DB 저장** | **항상 unmasked 원본** (정정 대상) |
| **마이페이지** | **마스킹 표시** (현재 적용 여부 사전조사 필요 — Step 1) |
| **관리자 사용자 목록 등 일반 화면** | 마스킹 표시 |
| **로그/디버그 출력** | **마스킹 표시** (값 절대 미포함, **userid + 필드명만 기록**) |
| **문서관리(document)·견적서(quotation)·견적서 PDF·실문서** | **항상 unmasked** |

### 1-2. 회귀 발견 사례 (S3 작업 중, 2026-04-20)

DB 직접 조회 결과 **단 1건의 사용자**(`user_seq=6, userid=ukjin914`) 의 민감정보가 **마스킹된 형태로 DB 저장**:

| 컬럼 | 현재값 (DB) | 정상값 (사용자 확정) |
|------|------------|--------------------|
| tel | `070-****-8093` | **`070-7113-8093`** |
| mobile | `01030562678` | **`010-3056-2678`** (hyphen 통일) |
| email | `u***@uitgis.com` | **`ukjin914@uitgis.com`** |

다른 사용자(admin / hanjun / seohg0801 / leeds 등)는 **모두 unmasked 정상 저장** 확인됨.

### 1-3. 회귀 발생 메커니즘 (코드 분석)

**파일**: `MyPageController.updateMyInfo()` (line 94~130)

```java
public String updateMyInfo(
    @RequestParam("tel") String tel,
    @RequestParam("mobile") String mobile,
    @RequestParam("email") String email, ...) {
    ...
    user.setTel(tel);            // ❌ 입력값 그대로 저장 — 가드 없음
    user.setMobile(mobile);      // ❌
    user.setEmail(email);        // ❌
    user.setSsn(ssn);            // ❌
    user.setAddress(address);    // ❌
    userRepository.save(user);
}
```

**시나리오**:
1. 마이페이지 화면 진입 → `mypage.html` 이 `${user.tel}` 등을 직접 표시
2. (현재는 unmasked 가 화면에 노출되지만, 과거 한 시점에 SensitiveMask 가 컨트롤러/뷰에 적용됐을 가능성)
3. 사용자가 수정 의도 없는 필드를 그대로 둔 채 다른 필드만 수정 후 submit
4. **마스킹 표시된 값이 form value 로 그대로 서버 전송 → 가드 없이 DB 저장**

→ user_id=6 본인이 과거 마이페이지에서 정보 수정 시도 시 발생한 1회성 회귀로 추정. **재발 방지 가드 시급**.

---

## 2. 목표

1. **데이터 정정**: `user_id=6` 의 tel/mobile/email 컬럼을 unmasked 원본으로 복원 (1건)
2. **재발 방지 가드**: `MyPageController` 에 마스킹 패턴 감지 시 DB 기존값 유지 로직 추가
3. **정책 문서화**: 마스킹 정책을 `docs/DESIGN_SYSTEM.md` + `SensitiveMask.java` javadoc 강화
4. **회귀 테스트**: 가드 단위 테스트 작성

---

## 3. 요구사항

### 3-1. 기능 요구사항 (FR)

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-1 | `user_seq=6` 의 tel/mobile/email 정정 SQL (V021) | 필수 |
| FR-2 | `MyPageController.updateMyInfo()` 에 마스킹 패턴 감지 가드 추가 | 필수 |
| FR-3 | 가드 동작: 입력값에 마스킹 패턴 감지 시 → **해당 필드는 DB 기존값 유지** + **WARN 로그(값 미포함, 필드명/userid만 기록)** + 사용자에게 **토스트 알림** | 필수 |
| FR-4 | 마스킹 패턴: 컬럼별 분리 규칙 — §3-3 표 참조 (단순 `\\*{3,}` 통합 X, 오탐 최소화) | 필수 |
| FR-5 | tel·mobile·email·ssn·address 5개 컬럼에 가드 적용 | 필수 |
| FR-6 | 정책 문서화: `docs/DESIGN_SYSTEM.md` + `SensitiveMask.java` javadoc | 필수 |
| FR-7 | 견적서/문서관리 영역에서 이미 unmasked 사용 확인 (회귀 검증, 변경 없음) | 필수 |
| FR-8 | **추가 안전판**: 입력값이 `현재 DB값과 정확히 일치하는 마스킹 패턴`이면 더 강력하게 차단 (SensitiveMask.tel(currentDb).equals(input) → 100% 회귀로 판정) | 권장 |

### 3-2. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|----|---------|
| NFR-1 | V021 마이그레이션: 트랜잭션 + 백업 테이블 + expected/actual 검증 (S5/S3 패턴) |
| NFR-2 | 멱등성: 재실행 시 0 row update (이미 정정된 값은 매핑 대상 X) |
| NFR-3 | 마이페이지 가드는 모든 필드 독립 동작 (한 필드 마스킹 감지 시 다른 필드는 정상 저장) |
| NFR-4 | 가드 단위 테스트 ≥ 5 케이스 |
| NFR-5 | 토스트 알림은 `redirectAttributes.addFlashAttribute("warningMessage", ...)` 활용 (기존 패턴) |
| NFR-6 | 다른 사용자 회귀 발견 시 동일 정책 적용 (V021은 박욱진만, 향후 발견은 별도 V0xx) |

### 3-3. 마스킹 패턴 감지 규칙 (FR-4 상세) — **컬럼별 분리** (오탐 최소화)

| 컬럼 | SensitiveMask 출력 패턴 | 감지 정규식 (확정) | 추가 차단 (FR-8) |
|------|------------------------|-------------------|-----------------|
| tel | `010-****-5678`, `070-****-9805` | `^\\d{2,4}-\\*{4}-\\d{4}$` | `SensitiveMask.tel(현재 DB값) == input` |
| mobile | tel 동일 패턴 | tel 동일 | tel 동일 |
| email | `h***@domain.com`, `u***@uitgis.com` | `^.{1,5}\\*{3,}@.+$` (앞 1~5자 후 `***` 후 `@` 도메인) | `SensitiveMask.email(현재 DB값) == input` |
| ssn | `901201-1******` | `^\\d{6}-\\d\\*{6}$` | `SensitiveMask.ssn(현재 DB값) == input` |
| address | `서울 강남구 ***`, 토큰 일부 마스킹 | `\\*{3,}` (보수적, fallback) — 1차는 FR-8 동등 비교 | **`SensitiveMask.address(현재 DB값) == input`** (오탐 0) |

**감지 우선순위**:
1. **FR-8 동등 비교** (정확): 현재 DB값을 SensitiveMask 거친 결과 == 입력값 → 100% 회귀, 차단
2. **컬럼별 정규식** (보수): 위 표 패턴 매칭 시 차단

**오탐 가능성 분석**:
- tel/mobile: 정규식이 정확한 마스킹 형태만 매칭 → 오탐 0
- email: 사용자가 `abc***@x.com` 같은 정상 입력 가능성 매우 낮지만, FR-8 동등 비교로 1차 필터
- address: 자유 입력이라 가장 위험 → FR-8 동등 비교를 1차로 사용
- ssn: 정확 패턴만 매칭 → 오탐 0

---

## 4. 데이터 정정 매핑 (V021)

| user_seq | 컬럼 | 변경 전 | 변경 후 |
|---------:|------|--------|--------|
| 6 | tel | `070-****-8093` | `070-7113-8093` |
| 6 | mobile | `01030562678` | `010-3056-2678` |
| 6 | email | `u***@uitgis.com` | `ukjin914@uitgis.com` |

**Acceptance 기준 (단일화)**: **단일 UPDATE 문, 영향 row = 1**
- 3 컬럼 동시 SET (`SET tel=..., mobile=..., email=...`)
- expected_cnt = **1**, actual_cnt = **1** 일 때만 PASS
- "3 row" 표현은 부정확 — 본 sprint 는 모두 단일 user_seq 라 1 row 가 정답
- mobile은 마스킹은 아니지만 hyphen 포맷 통일을 위해 함께 변경 (정책 OQ-1 일부)

---

## 5. DB 영향

### 5-1. 스키마 변경
- **없음** (기존 컬럼 데이터 정정만)

### 5-2. 데이터 변경
- 1 row UPDATE (3 컬럼 SET)

### 5-3. 인덱스
- 추가 불필요

### 5-4. 운영 절차 (NFR-1 부합)

V021 SQL 은 S5/S3 패턴을 따른다:

```sql
BEGIN;
-- (0) 동명 백업 테이블 존재 시 HALT
-- (1) 백업 테이블 자동 생성 (users_v021_backup_<run_id>) — user_seq=6 row 단독 백업
-- (2) expected_cnt 동적 산정 (현재 마스킹 패턴 보유 여부)
-- (3) UPDATE (단일 row 3 컬럼)
-- (4) actual_cnt 동등 검증 → 불일치 시 EXCEPTION ROLLBACK
-- (5) 사후 검증 (정정값 일치 확인)
COMMIT;
```

**백업 보관 정책**:
- 운영 안정 확인 후 (최소 7일) DROP 가능
- DROP 절차: `DROP TABLE users_v021_backup_<run_id>` 수동 실행

**롤백 절차**:
- L1 (코드만 revert): `git revert <S3-B 커밋>`
- L2 (데이터 롤백): `UPDATE users SET (tel,mobile,email) = (b.tel,b.mobile,b.email) FROM users_v021_backup_<run_id> b WHERE users.user_seq=b.user_seq`
- L3 (전체): L1 + L2 동시

---

## 6. UI/코드 변경

### 6-1. MyPageController.updateMyInfo()
- 입력값별 마스킹 감지 → 마스킹이면 DB 기존값 유지
- 감지된 필드 목록을 `warningMessage` 에 포함

### 6-2. 신규 유틸: `MaskingDetector` (제안)
```java
@Component
public class MaskingDetector {
    private static final Pattern MASKED = Pattern.compile("\\*{3,}");
    public boolean isLikelyMasked(String value) {
        return value != null && MASKED.matcher(value).find();
    }
}
```

### 6-3. mypage.html (정책 명시 대비)
- 변경 **없음** (기존 unmasked 표시 유지 확인 필요)
- 만약 `${@sensitiveMask.tel(user.tel)}` 같은 마스킹 적용이 발견되면 제거 (코드 점검 결과에 따라)

### 6-4. SensitiveMask.java javadoc 강화
```java
/**
 * 민감정보 마스킹 유틸 (S3-B 정책 명시).
 *
 * 사용처 정책:
 *  - DB 저장: 항상 unmasked
 *  - 일반 화면 (관리자 사용자 목록 등): 마스킹 표시 (호출 측에서 명시적 사용)
 *  - 문서관리·견적서·견적서 PDF: unmasked (마스킹 호출 금지)
 *  - 로그/디버그: 마스킹 표시 (값 절대 미포함, userid + 필드명만 기록)
 *
 * 회귀 방지: 마스킹된 값이 폼 submit 으로 DB 저장되는 회귀를
 *   MaskingDetector + MyPageController 가드 로 차단 (S3-B).
 */
```

---

## 7. 테스트 시나리오 (T)

### 7-1. 데이터 정정
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T1 | V021 첫 실행 | 1 row UPDATE (3 컬럼), expected=1 actual=1 |
| T2 | V021 재실행 | 0 row UPDATE (이미 정정된 값은 멱등성 키로 제외) |
| T3 | DB 검증 | user_seq=6 의 tel='070-7113-8093', mobile='010-3056-2678', email='ukjin914@uitgis.com' |

### 7-2. 가드 동작
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T4 | 일반 정상 입력 (tel='070-7113-1234') | DB 정상 저장 |
| T5 | 마스킹 입력 (tel='070-****-1234') | DB 기존값 유지 + WARN 로그 + 토스트 "tel 필드는 마스킹된 값이 감지되어 변경되지 않았습니다" |
| T6 | 일부 필드만 마스킹 (tel은 마스킹 / email은 정상) | tel만 유지, email은 변경 |
| T7 | 모든 마스킹 필드 (tel/mobile/email/ssn/address 모두 마스킹) | 모두 유지 + 토스트에 5개 필드 모두 포함 |
| T8 | NULL/빈 입력 | 가드 통과 (null 은 마스킹 패턴 X) |

### 7-3. 정책 회귀
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T9 | 견적서 비고 패턴 5건 (박욱진 매핑) 견적서 작성 화면 → 패턴 적용 | tel/email 자리에 unmasked 표시 (S3 RemarksRenderer 가 잘 동작하지만 실제로는 본 sprint 스코프에서 tel/email placeholder 미도입이라 영향 없음) |
| T10 | 정책 문서 (DESIGN_SYSTEM.md) 마스킹 정책 절 신설 확인 | 문서에 표 + 정책 명시 존재 |

---

## 8. 리스크

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | 사용자가 정당한 입력에 `***` 포함 시 가드가 차단 | 매우 드물지만 발생 가능. 토스트로 사용자 인지 가능, 다시 입력 가능 |
| R2 | 박욱진 외 다른 사용자도 회귀 보유 가능 | 본 sprint 는 박욱진만. **사후 검증 SQL 로 전체 사용자 스캔 + 별도 보고** (수동 처리) |
| R3 | 가드 추가 후 기존 마이페이지 저장 흐름 회귀 | T4~T8 회귀 테스트 + 수동 점검 |
| R4 | mypage.html 이 사실 마스킹 적용 중이면 가드만으로 부족 | Step 1 사전조사: 마이페이지 화면 마스킹 적용 여부 코드 점검 |
| R5 | 견적서/문서관리에서 마스킹 적용된 곳이 있다면 정책 위반 | Step 1 사전조사로 find. 발견 시 본 sprint 에서 제거 |
| **R6** | **로그에 민감정보 값 포함 위험** | WARN 로그는 **userid + 필드명만 기록**, 값 절대 미포함. 예: `log.warn("MASKING_GUARD_BLOCKED: userid={} fields={}", userid, blockedFields)` |
| **R7** | **address 자유 입력 오탐** | FR-8 "현재 DB값과 동일한 마스킹 패턴" 비교를 1차 적용 — 사용자가 비슷한 패턴을 새로 입력해도 DB값과 다르면 통과 |
| **R8** | **백업 테이블 누적** | DROP 절차 명시 (§5-4) + 운영 가이드 |
| **R9** | **codex 검토 1차 충돌 — 마이페이지 표시 정책 모호성** | §1-1 단일 정책 문장으로 확정 (마이페이지 = 마스킹) |

---

## 9. 결정 사항 (사용자 확정, 2026-04-20)

- **OQ-1 (정정값)**: tel=`070-7113-8093`, mobile=`010-3056-2678`, email=`ukjin914@uitgis.com`
- **OQ-2 (가드 정책)**: 마스킹 감지 시 → DB 기존값 유지 + 토스트
- **OQ-3 (마이페이지 표시)**: 현재 정책 유지 (마스킹 OK)
- **OQ-4 (스캔 범위)**: 박욱진만 (V021), 다른 사용자 발견 시 별도 처리
- **OQ-5 (정책 문서화)**: DESIGN_SYSTEM.md + SensitiveMask javadoc 모두

---

## 10. 릴리스 체크리스트

- [ ] codex 기획서 검토
- [ ] 사용자 최종승인
- [ ] 개발계획서 작성
- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] 사전조사 (마이페이지/견적서/문서관리 마스킹 적용 여부)
- [ ] V021 SQL + MaskingDetector + 가드 구현
- [ ] 단위 테스트 + 회귀 스모크
- [ ] codex 구현 검증
- [ ] 사용자 확인
- [ ] git commit + push + 태그 `s3b-masking-regression-v1`
