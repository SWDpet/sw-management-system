# [개발계획서] getProjectInfo 담당자 PII — VIEW→EDIT 가드 강화

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/harden-getprojectinfo-pii-edit.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. 가드 1줄 + 테스트 6(view_forbidden 신규·found/personMapping/notFound loginEdit·admin_ok/none_forbidden 유지). clean verify green, ratchet 불변, 커버리지 LINE 72.44%.

---

## 1. 작업 개요

DocumentLookupController.getProjectInfo 가드 1줄 VIEW→EDIT. DocumentLookupControllerTest 의 getProjectInfo 케이스 4건 조정(view_forbidden 신규 + found/personMapping/notFound loginEdit 전환). 다른 read API·로직 불변.

---

## 2. 구현 순서 (S-n)

### S-1 가드 강화
- DocumentLookupController.getProjectInfo: `if (!access.hasDocRead()) return ResponseEntity.status(403).build();` → `if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();` // [harden-getprojectinfo-pii] 담당자 PII=EDIT
- getUserInfo/getAttachments(다른 컨트롤러)/getPlanData 불변.

### S-2 테스트 조정 (DocumentLookupControllerTest)
- getProjectInfo_found: loginView() → loginEdit().
- getProjectInfo_personMapping: loginView() → loginEdit().
- getProjectInfo_notFound: loginView() → loginEdit().
- getProjectInfo_none_forbidden: 유지(NONE→403).
- getProjectInfo_admin_ok: 유지(admin→EDIT→200).
- **getProjectInfo_view_forbidden 신규**: loginView() → 403, verifyNoInteractions(swProjectRepository, personInfoRepository).

### S-3 검증
- `./mvnw -o clean verify` green. ratchet 불변·커버리지 비감소.

### S-4 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile + clean verify |
| NFR-2 | S-2 view_forbidden 신규 + found/personMapping/notFound loginEdit green, admin_ok/none_forbidden 유지 |
| NFR-3 | S-3 커버리지 비감소·ratchet 불변 |
| NFR-4 | S-4 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| VIEW 정당 호출 차단 | 호출처 grep=생성폼(EDIT 전용)만 — codex 확인 |
| notFound 테스트 회귀 | S-2 loginEdit 전환(codex) |
| admin 오차단 | getAuth admin→EDIT, admin_ok 유지 |

롤백: 단일 커밋 `git revert`(가드 1줄+테스트).

---

## 5. 커밋

- `fix(security): getProjectInfo 담당자 PII VIEW→EDIT 가드 강화 (harden-getprojectinfo-pii)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
