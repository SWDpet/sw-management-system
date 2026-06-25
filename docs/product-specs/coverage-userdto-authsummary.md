# [기획서] UserDTO·AuthSummary 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트. 순수 단위 타깃 마무리(컨트롤러 통합 전). 선행 `coverage-exception-handler`(`d3a52d6`).
- **상태**: ✅ 완료 (2026-06-25). codex APPROVE-WITH-FIX(보완 반영) + 구현검증. 신규 9테스트. UserDTO 0%→72.2%, AuthSummary 6.7%→96.7%. 전역 LINE 52.67%→53.52%.

---

## 1. 배경 / 목표

남은 순수(운영DB·mock 무관) 고가치 타깃:
| 클래스 | 현재 LINE | 미커버 | 로직 |
|---|---|---|---|
| UserDTO | **0%** | 72 | fromEntity + getAuthName + getUserRoleNm/getEnabledNm |
| AuthSummary (util) | **6.7%** | 28 | summarize(User,max): 10권한 스캔→뱃지/moreCount |

**목표**: 두 클래스 변환·요약 로직에 순수 단위테스트 추가. 프로덕션 코드 변경 0.

---

## 2. 테스트 대상

### 2-1. UserDTO
- `fromEntity(null)` → null
- `fromEntity(user)` → 대표 필드(userSeq/userid/username/email 등) 매핑, password 미매핑(필드 자체 없음)
- `getAuthName`(fromEntity 의 authXxxNm 경유): null→"없음", NONE→"접근불가", VIEW→"조회", EDIT→"편집", 기타→원본코드
- `getUserRoleNm`(instance): ROLE_ADMIN→"관리자", ROLE_USER→"일반사용자", 기타→원본
- `getEnabledNm`(instance): TRUE→"활성", FALSE/null→"대기중"

### 2-2. AuthSummary
- `summarize(null, n)` → 빈 리스트, moreCount 0
- `summarize` 혼합 권한: EDIT→label+"E"(edit), VIEW→label+"V"(view), NONE/null/빈→skip, 알수없는값→skip
- max 초과: list = 앞 max 개, moreCount = 전체−max
- max<=0 또는 size<=max → 전체, moreCount 0

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 신규 `UserDTOTest`·`AuthSummaryTest`(순수, User 엔티티 @Data 세터로 구성). |
| FR-2 | UserDTO getAuthName 4분기+default, getUserRoleNm/getEnabledNm 분기, AuthSummary EDIT/VIEW/skip/max-cap/moreCount 단언. |
| FR-3 | 프로덕션 코드 무변경. 테스트만. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 신규 테스트 green. |
| NFR-2 | 전체 `./mvnw test` green. |
| NFR-3 | JaCoCo 게이트 통과 + 두 클래스·전역 상승. floor 상향은 구현 후 실측 판단. |

---

## 5. 의사결정

- **5-1 private getAuthName** ✅: public fromEntity 의 authDashboardNm/authProjectNm/authPersonNm/authInfraNm 경유 간접 검증(4개 auth 필드에 각 값 주입).
- **5-2 AuthSummary 는 @Component bean** ✅: `new AuthSummary()` 직접 생성(상태없음) 가능.
- **5-3 도메인 getter-only(InfraLinkApi/User 등) 제외** ✅: 단순 getter 호출 커버는 가치 낮음. 실로직(변환/요약)만 타깃.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `dto/UserDTOTest`·`util/AuthSummaryTest` | 신규 |
| Docs | 본 기획서 + 개발계획서 | 신규 |

프로덕션/DB/API/UI 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| User 필드명/세터 불일치 | 낮음 | @Data 세터, 컴파일 게이트 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
