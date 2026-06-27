# [개발계획서] DocumentController 분리 — S4 Phase 5 (조회 API, 최종)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-document-controller-split-phase5.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27, S4 split 최종)**. S-1~S-5 수행. DocumentController 529→440(순수 core)·조회 3개 DocumentLookupController 편입·personInfo 필드 제거. 6 이관+personMapping 신규(DocumentLookupController 첫 테스트). ratchet 298→301(user/personInfo 직접접근 추가, GOLDEN_RECORD). clean verify green(URL `/document/api/...` 보존 — 양 컨트롤러 동일 @RequestMapping, @SpringBootTest 부팅 검증).

## 후속 백로그 (dual-review — pre-existing)
- getProjectInfo 담당자 PII(personEmail/personTel) 무가드 노출 — getUserInfo P1-3 하드닝과 비일관(읽기 권한 정책 결정 필요)
- DocumentLookupController @RequiredArgsConstructor 9-arg positional 의존(필드 재정렬 시 테스트 깨짐) — 명시 생성자 검토

---

## 1. 작업 개요

조회 API 3개(getUserInfo/getUserInfoSecure/getProjectInfo)를 기존 `DocumentLookupController` 로 이동. personInfoRepository 필드를 DocumentController 에서 제거(getProjectInfo 전용). 순수 이동 + codex 보완 personId 매핑 테스트. DocumentLookupController 첫 테스트 신설.

---

## 2. 구현 순서 (S-n)

### S-1 DocumentLookupController 확장
- final 필드 추가(@RequiredArgsConstructor 자동 생성자): `UserRepository userRepository`, `PersonInfoRepository personInfoRepository`, `DocumentAccessSupport access`. (SwProjectRepository 기존 보유.)
- **이동**(본문 불변): getUserInfo(GET /api/user/{userSeq}), getUserInfoSecure(GET /api/user/{userSeq}/secure, EDIT 가드), getProjectInfo(GET /api/project/{projId}).
- 본문 `getAuth()` → `access.getAuth()`. import 추가: UserInfoRow·UserInfoSecureRow·**@PathVariable(codex)**·(필요시 Map/HashMap/List). PersonInfo domain import 불필요.

### S-2 DocumentController 정리
- 위 3 엔드포인트 제거.
- **personInfoRepository 필드 + PersonInfoRepository import 제거**(getProjectInfo 전용, grep 재확인). userRepository/swProjectRepository 잔존.
- import 제거: UserInfoRow·UserInfoSecureRow(이동 후 미사용 — grep). PersonInfo domain import 도 미사용 시 제거.

### S-3 테스트 이관 + personId 매핑 신규
- `DocumentLookupControllerTest.java` 신설: getUserInfo_found/notFound·getUserInfoSecure_nonEdit_forbidden/edit_found·getProjectInfo_notFound/found (6) 이관. 셋업=생성자 mock 주입(@RequiredArgsConstructor 인자 순서대로: swProject·sigungu·sysMst·infra·processMaster·servicePurpose·userRepository·personInfoRepository·access) + login 헬퍼 + `new DocumentAccessSupport()`(access).
- **codex: getProjectInfo_personMapping 신규** — swProjectRepository.findById→SwProject(setPersonId) + personInfoRepository.findById→PersonInfo(setUserNm 등) → 응답 Map 에 **personNm=getUserNm()**(+ personTel/personEmail/personDept/personTeam/personPos/personOrg) 매핑 검증. ⚠**personId 는 응답 Map 에 put 하지 않으므로 단언 금지**(codex).
- DocumentControllerTest: 해당 6 케이스 제거. 미사용 mock 정리(personInfoRepository mock·inject 제거 — userRepository/swProjectRepository 는 잔존 테스트 사용 유지). 잔존 불변.

### S-4 검증
- `./mvnw -o clean verify` — compile + 전체 테스트 + @SpringBootTest 부팅(ambiguous mapping 검출) + JaCoCo 게이트 green.
- ControllerRepositoryRatchet 초과 시 GOLDEN_RECORD=1 갱신(userRepository +1 예상, personInfo relocate net0).
- DocumentController LOC 확인(약 434, 순수 core).

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 이관 6 동일 단언 green |
| NFR-2c | S-3 getProjectInfo personId 매핑 신규 green |
| NFR-2b | S-4 @SpringBootTest 부팅(매핑 비충돌) |
| NFR-3 | S-4 JaCoCo 게이트(코드 이동→비감소) |
| NFR-4 | S-4 DocumentController 순수 core·personInfo 필드 제거 / DocumentLookupController 첫 테스트 |
| NFR-4b | S-4 ratchet GOLDEN_RECORD(필요 시) |
| NFR-5 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 3 매핑 잔존 → ambiguous | S-2 제거 후 clean verify(startup 실패) |
| @RequiredArgsConstructor 인자순서 | 신규 테스트 생성자 mock 을 필드 선언순서와 일치 |
| getUserInfoSecure EDIT 가드 누락 | 가드 동반 이동 + nonEdit 403 이관 |
| personInfo 필드 오제거(잔존 사용) | grep(getProjectInfo 유일) 재확인 |

롤백: 단일 커밋 `git revert`. 순수 구조 이동(+테스트 1건).

---

## 5. 커밋

- `refactor(document): DocumentController 조회 API DocumentLookupController 편입 — S4 Phase 5(최종)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
