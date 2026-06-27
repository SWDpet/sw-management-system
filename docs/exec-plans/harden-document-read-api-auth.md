# [개발계획서] 문서 read API 권한 가드 보강 (보안 하드닝 #1)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/harden-document-read-api-auth.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. 구현: 가드는 `"NONE".equals` denylist 대신 **`DocumentAccessSupport.hasDocRead()` allowlist(VIEW∥EDIT, admin→EDIT, fail-closed)** 로 채택(dual-review fail-open/중복/매직스트링 합의 반영). 4 엔드포인트 적용 + none_forbidden 4 + admin_ok 4(admin 통과 증명=dual-review admin오탐 반증) + 기존 read 테스트 loginView. clean verify green, ratchet 불변. 커버리지 LINE 72.05%.

---

## 1. 작업 개요

문서 read API 4종에 `access.getAuth()=="NONE"` → 403 가드 추가(VIEW 이상 통과). 3 컨트롤러 각 1~2줄. 테스트: none_forbidden 4 신규 + 기존 read 테스트 loginView 보강. UI/DB 변경 0.

---

## 2. 구현 순서 (S-n)

### S-1 가드 추가 (프로덕션)
- DocumentLookupController.getUserInfo: 진입부 `if ("NONE".equals(access.getAuth())) return ResponseEntity.status(403).build();`
- DocumentLookupController.getProjectInfo: 동일(ResponseEntity<Map<String,Object>> → `status(403).build()`).
- DocumentFileController.getAttachments: 동일(ResponseEntity<List<AttachmentRow>> → `status(403).build()`).
- DocumentPlanController.getPlanData: 동일(ResponseEntity<?> → `status(403).build()`).
- 각 컨트롤러 이미 `access` 필드 보유(추가 주입 불필요).

### S-2 테스트
- **none_forbidden 신규 4**(codex: verifyNoInteractions 는 메서드 내 전 레포 대상):
  - DocumentLookupControllerTest: getUserInfo_none_forbidden(403, verifyNoInteractions(userRepository)), getProjectInfo_none_forbidden(403, verifyNoInteractions(**swProjectRepository, personInfoRepository**)).
  - DocumentFileControllerTest: getAttachments_none_forbidden(403, verifyNoInteractions(attachmentService)).
  - DocumentPlanControllerTest: getPlanData_none_forbidden(403, verifyNoInteractions(**swProjectRepository, pjtTargetRepository, pjtManpowerPlanRepository, pjtScheduleRepository**)).
  - **loginNone() = login("NONE","ROLE_USER") 헬퍼를 3 테스트클래스 모두에 신규 추가**(현재 없음 — codex).
- **기존 테스트 loginView 보강(무로그인→NONE→403 회귀 방지)** — 해당 4 엔드포인트 호출 전체:
  - DocumentLookupControllerTest: getUserInfo_found·getUserInfo_notFound·getProjectInfo_found·getProjectInfo_notFound·getProjectInfo_personMapping → loginView() 추가. (getUserInfoSecure_* 는 이미 로그인.)
  - DocumentFileControllerTest: getAttachments_returnsRows → loginView().
  - DocumentPlanControllerTest: getPlanData_notFound·getPlanData_found → loginView().

### S-3 검증
- `./mvnw -o clean verify` — compile + 전체 green + JaCoCo 게이트. ratchet 불변(가드만 추가, repo 접근 변화 없음).

### S-4 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile + clean verify |
| NFR-2 | S-2 none_forbidden 4 + 기존 read 테스트 loginView 보강 green |
| NFR-3 | S-3 커버리지 비감소·ratchet 불변 |
| NFR-4 | S-4 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 기존 read 테스트(무로그인) 403 회귀 | S-2 로 loginView 일괄 보강(notFound 포함) |
| VIEW 사용자 오차단 | getAuth VIEW→통과, none_forbidden 은 NONE 만 |
| access 필드 누락 컨트롤러 | 3 컨트롤러 모두 access 보유 확인됨 |

롤백: 단일 커밋 `git revert`. 가드 추가라 되돌리면 원상.

---

## 5. 커밋

- `fix(security): 문서 read API 4종 VIEW 권한 가드 추가 (harden-document-read-api-auth)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
