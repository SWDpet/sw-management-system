# [기획서] getProjectInfo 담당자 PII — VIEW→EDIT 가드 강화 (보안 백로그)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: read-API 하드닝(`e9cd4b8`) 후속 백로그 + dual-review 3회 지적(getProjectInfo PII 노출). [[project_swmanager_viewer_download_guard]]·[[feedback_all_pages_require_permission]].
- **상태**: ✅ **완료(2026-06-27)**. getProjectInfo 가드 hasDocRead→EDIT. 담당자 PII VIEW 노출 제거(P1-3 일관). view_forbidden 신규 + found/personMapping/notFound loginEdit. dual-review codex2/Opus3 — VIEW박탈=의도(호출처 EDIT전용 검증·테스트 고정), "EDIT" 매직스트링은 동일파일 getUserInfoSecure와 일관(전역 관용구) 유지, 분쟁3 refute(admin_ok가 admin통과 증명). 듀얼푸시.

---

## 1. 배경 / 목표

`DocumentLookupController.getProjectInfo`(GET /document/api/project/{projId})는 사업 정보와 함께 **담당자 PII(personNm/personTel/personEmail/personDept/...)** 를 반환한다. read-API 하드닝에서 **VIEW 이상(hasDocRead)** 으로 막았으나, dual-review 가 3회 "VIEW 에게 PII 노출은 P1-3(getUserInfoSecure PII=EDIT 전용) 과 비일관" 지적.

**호출처 확인(grep)**: `/document/api/project/{id}` 는 **doc-commence·doc-completion·doc-interim 생성 폼에서만** 호출. 이 폼들은 `createForm` 이 **EDIT 전용**(비-EDIT redirect). 즉 getProjectInfo 의 정당한 호출자는 **EDIT 사용자뿐** → EDIT 로 좁혀도 UX 영향 없음.

**목표**: getProjectInfo 가드를 `hasDocRead()`(VIEW+) → **EDIT**(admin 은 getAuth→EDIT 통과)로 강화. 담당자 PII 의 VIEW 노출 제거. P1-3 PII=EDIT 정책 일관.

비목표: getUserInfo(P1-3 으로 비민감화)·getAttachments(문서 상세 VIEW 접근)·getPlanData 는 VIEW 유지(PII 아님/문서 조회 성격).

---

## 2. 변경 설계

- DocumentLookupController.getProjectInfo 진입 가드: `if (!access.hasDocRead())` → `if (!"EDIT".equals(access.getAuth()))`. (admin→getAuth "EDIT" 통과 유지.)
- 응답/로직 불변(가드 레벨만 VIEW→EDIT).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | getProjectInfo: authDocument=VIEW 사용자 → 403(기존 200+PII 차단). NONE → 403(유지). |
| FR-2 | EDIT·admin → 200 + 기존 응답(동작 불변). |
| FR-3 | getUserInfo/getAttachments/getPlanData 가드 불변(VIEW+). 다른 로직 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | getProjectInfo_view_forbidden(VIEW→403) 신규 + 기존 getProjectInfo_found/personMapping/**notFound**(codex: notFound 도 loginView→EDIT 가드 후 403 되므로) 를 loginView→loginEdit 전환. admin_ok·none_forbidden 유지. |
| NFR-3 | 커버리지 비감소·floor 유지·ratchet 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 getProjectInfo 만 EDIT** ✅: PII(email/tel) 반환 + 호출자 EDIT 전용 확인 → 안전·정합. getUserInfo(비민감)·getAttachments(상세 VIEW)·getPlanData 는 VIEW 유지(과잉 차단 회피).
- **5-2 admin 통과 유지** ✅: getAuth admin→"EDIT" 매핑(테스트 admin_ok 로 보증).
- **5-3 호출자 EDIT 전용 검증 근거** ✅: doc-commence/completion/interim = createForm EDIT 가드. VIEW 는 생성 폼 미접근 → 호출 안 함.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller | DocumentLookupController | 가드 1줄(VIEW→EDIT) |
| Test | DocumentLookupControllerTest | view_forbidden 신규 + found/personMapping loginEdit 전환 |

UI/DB 변경 0. 동작 변경 = VIEW 사용자 getProjectInfo 403(의도된 PII 보호).

| 리스크 | 수준 | 완화 |
|---|---|---|
| VIEW 사용자 정당 호출 차단 | 낮음 | 호출처 grep=생성폼(EDIT 전용)만. VIEW 미호출 |
| admin 오차단 | 낮음 | getAuth admin→EDIT, admin_ok 테스트 |

---

## 7. 승인 요청

본 기획서(getProjectInfo PII EDIT 강화)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
