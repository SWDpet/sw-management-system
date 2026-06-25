# [개발계획서] UserDTO·AuthSummary 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-userdto-authsummary.md` (codex APPROVE-WITH-FIX, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE-WITH-FIX(보완 반영) + 구현검증. 신규 9테스트(UserDTO 5/AuthSummary 4). UserDTO 0%→72.2%, AuthSummary 6.7%→96.7%. 전역 LINE 52.67%→53.52%. floor LINE 0.50→0.51(INSTR 0.42 유지).

---

## 1. 작업 개요

UserDTO·AuthSummary 변환/요약 로직에 순수 단위테스트 추가. **프로덕션 코드 변경 0**, 신규 테스트 2파일.

---

## 2. 신규 테스트 (T-n)

### `UserDTOTest` (com.swmanager.system.dto)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-U1 | `fromEntity_null_returnsNull` | null → null |
| T-U2 | `fromEntity_mapsFields_andAuthNames` | User(userid/username/email + authDashboard=VIEW/authProject=EDIT/authPerson=NONE/authInfra=null) → 필드 매핑 + authDashboardNm="조회"/authProjectNm="편집"/authPersonNm="접근불가"/authInfraNm="없음" |
| T-U3 | `getAuthName_defaultBranch` | authDashboard="WEIRD" → authDashboardNm="WEIRD"(default 코드 반환) |
| T-U4 | `getUserRoleNm_branches` | builder userRole ROLE_ADMIN→"관리자", ROLE_USER→"일반사용자", "ETC"→"ETC" |
| T-U5 | `getEnabledNm_branches` | enabled true→"활성", false→"대기중", null→"대기중" |

### `AuthSummaryTest` (com.swmanager.system.util)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-A1 | `summarize_null_returnsEmpty` | summarize(null, 4) → list 비어있음, moreCount 0 |
| T-A2 | `summarize_editView_buildBadges_skipNoneAndUnknown` | authDashboard=EDIT/authProject=VIEW/authPerson=NONE/authInfra="FOO"/나머지 null → 뱃지 [대시E(edit), 사업V(view)], moreCount 0 |
| T-A3 | `summarize_maxCap_setsMoreCount` | 5개 EDIT + max=2 → list size 2, moreCount 3 |
| T-A4 | `summarize_maxZeroOrUnderLimit_returnsAll` | max=0 → 전체 반환 moreCount 0 |

- 기존 mock 불요. User 는 @Data 세터로 구성, `new AuthSummary()` 직접 생성.
- 인코딩: `spring-boot-starter-parent` 가 `project.build.sourceEncoding=UTF-8` 상속(별도 pom 변경 불요) — 한글 단언 테스트가 이번 세션 전건 green 으로 방증. 신규 파일은 UTF-8 저장.
- private getAuthName 은 fromEntity 의 authXxxNm(authDashboard/authProject/authPerson/authInfra 4개만 Nm 매핑) 경유 검증.

---

## 3. 구현 순서 (S)

| ID | 단계 |
|----|------|
| S-1 | `UserDTOTest`·`AuthSummaryTest` 작성. |
| S-2 | `./mvnw test "-Dtest=UserDTOTest,AuthSummaryTest"` → green(PowerShell 쉼표 인자 따옴표). |
| S-3 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| S-4 | JaCoCo csv 로 UserDTO·AuthSummary·전역 상승 확인 → floor 상향 판단(실측−2~2.5pp, 게인 작으면 유지). |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2 신규 green |
| NFR-2 | S-3 전체 green |
| NFR-3 | S-3 게이트 + S-4 상승 |

---

## 5. 롤백

신규 테스트만 → 회귀 위험 극소. 프로덕션 영향 0.

---

## 6. 커밋 (작업완료 후)

- 메시지: `test(coverage): UserDTO·AuthSummary 단위테스트 추가 (beyond-A)` (+ floor 상향 시 동봉)
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
