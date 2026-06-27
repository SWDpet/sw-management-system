# [기획서] DocumentController 분리 — S4 Phase 5 (조회 API user/project, 최종)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 로드맵 S4 거대클래스 분리. Phase 1~4 후속 — **마지막 비-core 그룹**.
- **상태**: ✅ **완료(2026-06-27, S4 split 최종)**. 조회 API 3개 DocumentLookupController 편입, DocumentController 529→440(원본 1373 대비 -933, ~68%↓, **순수 core**), personInfoRepository 필드 제거. DocumentLookupController 첫 테스트(7) 확보. ratchet 298→301. 커버리지 LINE 72.04%. dual-review codex1/Opus6(합의2=pre-existing·생성자순서주석, 분쟁5 refute). 듀얼푸시.

---

## 1. 배경 / 목표

Phase 1~4 로 DocumentController 1373→529. 마지막 비-core 그룹은 **조회 API(사용자/사업 정보)** 3 엔드포인트(getUserInfo·getUserInfoSecure·getProjectInfo, ≈95줄). 이들은 조회 성격이므로 **이미 존재하는 `DocumentLookupController`**(사업 검색/cascade/infra/process 등 read-only lookup 모음, 현재 테스트 無)에 **편입**한다.

**목표**: 3 엔드포인트를 DocumentLookupController 로 이동 → DocumentController 는 순수 core(list/detail/create/save/status/delete/preview)만 남김(≈434줄). **동작 100% 동일(순수 이동)**. 부수효과로 **DocumentLookupController 첫 단위테스트 추가**(이동 3 엔드포인트 커버).

비목표: core CRUD 추가 분리(불필요 — 한 도메인). 하드닝 백로그(Phase 1~4 누적)는 별도.

---

## 2. 변경 설계

### 2-A DocumentLookupController 확장(@RequiredArgsConstructor, @RequestMapping("/document"))
- 이동 엔드포인트(매핑·시그니처·본문 불변):
  - `GET /api/user/{userSeq}` (getUserInfo)
  - `GET /api/user/{userSeq}/secure` (getUserInfoSecure) — EDIT 가드
  - `GET /api/project/{projId}` (getProjectInfo)
- **추가 final 필드(생성자 주입)**: UserRepository, PersonInfoRepository, DocumentAccessSupport. (SwProjectRepository 는 기존 보유 → 재사용.)
- 본문 `getAuth()` → `access.getAuth()`. import: UserInfoRow·UserInfoSecureRow·Map·List 등. ⚠**PersonInfo domain import 불필요**(codex — getProjectInfo 가 `findById(..).ifPresent(pi -> ..)` 추론 사용).

### 2-B DocumentController 정리
- 위 3 엔드포인트 제거.
- 필드: userRepository·swProjectRepository 잔존(core 사용: detail/create 등). ⚠**personInfoRepository 는 getProjectInfo 전용(유일 사용) → 필드+import 제거**(codex 정정). UserInfoRow·UserInfoSecureRow import 제거(이동으로 미사용 — grep 확인).
- 결과: DocumentController 529 → 약 434줄(순수 core).

> `/document` 공유 컨트롤러군에 신규 클래스 추가 아님(기존 DocumentLookupController 확장). full path 비충돌(user/project 경로는 현재 DocumentController 단독 소유 → 이동 후 원본 제거만 정확하면 안전). @SpringBootTest 부팅이 ambiguous mapping 자동검출.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 3 엔드포인트의 URL·메서드·요청/응답·권한(getUserInfoSecure=EDIT, 나머지 무가드 현행 유지)이 이전과 완전 동일. |
| FR-2 | getUserInfo(비민감 UserInfoRow)·getUserInfoSecure(민감 UserInfoSecureRow, EDIT)·getProjectInfo(사업+PersonInfo Map) 본문·필드매핑 불변. |
| FR-3 | DocumentController 잔존(core)·DocumentLookupController 기존 13 엔드포인트·기타 분리 컨트롤러 동작 불변. |
| FR-4 | 신규 기능·로직 변경·UI 변경 0(순수 이동). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 기존 DocumentControllerTest 의 getUserInfo*/getUserInfoSecure*/getProjectInfo* (6) → **신규 DocumentLookupControllerTest** 로 이관, 동일 단언. 잔존 테스트 불변. |
| NFR-2c | **codex: getProjectInfo personId 매핑 테스트 1건 추가** — swProjectRepository.findById→SwProject(personId 세팅) + personInfoRepository.findById→PersonInfo(이름/연락처 등) → 응답 Map 에 담당자 필드(personNm 등) 매핑 검증. |
| NFR-2b | @SpringBootTest 부팅 성공(매핑 비충돌). |
| NFR-3 | 전역 커버리지 비감소(코드 이동). floor 유지. |
| NFR-4 | DocumentController 순수 core 화(LOC 감소). DocumentLookupController 첫 테스트 확보. |
| NFR-4b | ControllerRepositoryRatchet: DocumentLookupController 가 userRepository·personInfoRepository 직접접근 추가 → 카운트 증가 예상. 정당한 이동이면 GOLDEN_RECORD 갱신. |
| NFR-5 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 신규 컨트롤러 대신 DocumentLookupController 편입** ✅: user/project 조회는 read-only lookup 도메인 → 기존 lookup 컨트롤러에 응집(클래스 난립 방지). 부수효과로 무테스트였던 lookup 컨트롤러에 첫 테스트 추가.
- **5-2 필드 제거 없음** ✅: 이동 대상이 쓰는 repo 3종 모두 core 잔존 → DocumentController 필드 불변(이동만).
- **5-3 무가드 read 현행 유지** ✅: getUserInfo/getProjectInfo 무가드는 원본 동작 — 순수 이동이라 그대로 보존(권한 정책 변경은 백로그).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller(수정) | `controller/DocumentLookupController.java` | 3 엔드포인트 + 필드 3 추가 |
| Controller(수정) | `controller/DocumentController.java` | 3 엔드포인트 제거 |
| Test(신규) | `controller/DocumentLookupControllerTest.java` | 이관 6 |
| Test(수정) | `controller/DocumentControllerTest.java` | 해당 케이스 제거 |
| Build | `golden/controller-repo-arch-baseline.txt` | ratchet 갱신(예상) |

UI/DB/API 계약 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 3 매핑 잔존 → ambiguous | 중 | 제거 후 clean verify(startup 실패로 즉시 검출) |
| @RequiredArgsConstructor 인자순서/누락 | 중 | 컴파일 + 신규 테스트 생성자 mock 주입 |
| getUserInfoSecure EDIT 가드 누락 이동 | 높음 | 가드 동반 이동 + nonEdit 403 테스트 이관 |
| import 누락/잔존 | 낮음 | 컴파일 + grep |

---

## 7. 승인 요청

본 기획서(S4 Phase 5, 최종)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
