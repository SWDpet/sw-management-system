# [기획서] OpsDocController 검색·캐스케이드·관계자 분기 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 마지막 대형 비-license 컨트롤러. 기존 `OpsDocControllerTest`(46) 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). OpsDocControllerTest +20(48→68) → OpsDocController **78.6→98% LINE**(393/401)·BRANCH 205/300. 전역 LINE 77.24→77.91%·INSTR 63.16→63.72%. **floor 0.74→0.75·0.60→0.61 상향**(버퍼 2.9/2.7pp). `mvnw -o clean verify` 1420 green. codex 기획 APPROVE-WITH-FIX·개발계획 APPROVE-WITH-FIX·구현 PASS. dual-review(codex1/Opus8) 합의2 반영(import 정렬·multi-candidate sggByDoc 단언), 분쟁7 codex refute. 잔여 8줄=isAdmin 방어 catch 등 의도적 제외.
- **codex 보완**: ① C1에 `resolveRegion` 복수후보 sido-pick 분기(cand.size()>1, project cityNm이 2번째 후보만 매칭) 명시 케이스 추가 ② isAdmin catch는 "도달불가"가 아니라 **의도적 제외**(SecurityContext mock으로 도달 가능하나 방어적·저가치)로 재서술 ③ C4/C5는 create 경유 시 검증/가드 통과 form 필수, 캡처는 OpsDocument를 받는 경계에서 ④ requesterSearch `findAllByKeyword` 반환타입 정확 stub ⑤ Row 단언은 실필드(id/name/partner명/staff직급/orgUnit명/sys nm) ⑥ docPartners partner found/missing 양분기 ⑦ relations CONTACT found-label + `"업체담당자 #id"` 폴백 양분기.

---

## 1. 배경 / 목표

`OpsDocController`(782줄, **LINE 78.6% / miss 86**)는 세션11(`931c77a`)에서 list/폼라우팅/CRUD/첨부 가드를 덮어 10.4→78.4%가 됐으나, **read-only 검색·캐스케이드·관계자 프리필 엔드포인트와 INSPECT 지역/유형 해석 헬퍼**가 미커버로 남았다. 이들은 대부분 repository 위임 + `*Row.from()` 매퍼 호출이라 mock + 필드 단언으로 안전하게 박제 가능하다. 마지막 대형 비-license 컨트롤러를 95%대로 끌어올린다.

JaCoCo 미커버 라인 실측(`OpsDocController.java.html` nc): 125-126, 180-194, 209, 218, 226, 458-460, 493-497, 504-517, 530-533, 542-543, 581-582, 624-625, 635-637, 646-656, 666-672, 684-685, 695-696, 727-739.

## 2. 대상 분기 (클러스터)

| # | 영역(라인) | 내용 |
|---|---|---|
| C1 | list INSPECT 해석(125-126,180-194,209) | `resolveRegion` INSPECT 분기 — **(a) sgg 후보 단일** pick=cand.get(0) **(b) 복수후보(cand.size()>1)에서 project cityNm이 2번째 후보 sidoNm만 매칭→pick=2번째** 명시 + `resolveMaintType` + maint 배지 label/tone. 후보 없을 때 `{nz(sido),nz(sgg)}` 폴백도 포함 |
| C2 | parseReportId catch(218) | docNo 파싱 실패 → null |
| C3 | support-file 500 catch(458-460,495-497) | upload/delete 일반 Exception(서비스 RuntimeException) → 500 SERVER_ERROR |
| C4 | applyRelations(504,513,515,517) | engineer 세팅 + requester PERSON/CONTACT/STAFF 분기 (create 경유) |
| C5 | savePartners(646-656) | partners 리스트 (업체,역할) 중복 제거 + 저장 (create 경유) |
| C6 | engineers(530-533) | SW지원팀 found → `EngineerRow.from` |
| C7 | requesterSearch/partnerContactSearch/partnerSearch/staffSearch(542-543,581-582,624-625,666-672) | 검색 위임 + Row 매핑(staff 는 orgUnit 라벨 조회 분기 포함) |
| C8 | docPartners(635-637) | docId 별 partner 조인 → `DocPartnerRow.from` |
| C9 | cascadeSgg/cascadeSystems(684-685,695-696) | 시도→시군구, 시스템 마스터 전체 |
| C10 | relations(727-739) | 수정폼 프리필 CONTACT/STAFF requester 분기(label 폴백 포함) |

**의도적 제외**: `isAdmin()` catch(226)는 SecurityContext mock으로 `getAuthorities()` 예외를 주입하면 도달 가능하나, 정상 경로에선 안 타는 방어적 catch이고 ROI 낮아 **의도적으로 제외**(도달불가 아님 — 정직성 명시). 잔여 1줄 허용.

## 3. 변경 — `OpsDocControllerTest` 케이스 추가 (테스트만)
- 기존 17 mock 생성자주입 setUp·헬퍼(`model()`/`user()`/`bodyMap()`) 재사용. 신규 ~12 테스트.
- **C1**: `OpsDocument`(docType=INSPECT, docNo="…-42") + `inspectReportRepository.findById(42)`→report(pjtId=7) + `swProjectRepository.findById(7)`→project(cityNm/distNm/maintType) + `sigunguCodeRepository.findAll()`로 bySgg 후보 구성(복수 시 sido 매칭 pick). model 의 sidoByDoc/sggByDoc/maintLabelByDoc 값 단언.
- **C3**: `supportFileService.store(...)` `thenThrow(new RuntimeException())` → status 500 + ApiResult.fail SERVER_ERROR.
- **C4/C5**: create(EDIT) 경유 form 에 engineerId + requesterKind(PERSON/CONTACT/STAFF) + partners(중복쌍 포함) → `ArgumentCaptor<OpsDocument>` 로 setEngineer/requesterPerson/requesterContactId/requesterStaffId 반영 + `opsDocPartnerRepository.save` 호출횟수(중복 1건 제거) 단언.
- **C6~C10**: 각 검색/캐스케이드 엔드포인트 — repo stub 1 엔티티 → 반환 리스트 크기 + **Row 레코드 필드 exact 단언**(빈 결과 위임 아닌 실데이터 1건; 구현 시 각 `*Row.from/of` 필드명 소스 대조). **C7 staff**: orgUnit found(라벨 세팅)/null(unit=null) 양분기. **C8 docPartners**: partner found/missing(`DocPartnerRow.from(dp,null)` 폴백) 양분기. **C10 relations**: CONTACT found-label(`name / partner명`)·CONTACT 폴백(`업체담당자 #id`, partnerContact=null)·STAFF found 분기.
- **requesterSearch**: `findAllByKeyword` 반환타입 정확 stub(구현 시 Page/Slice/List 소스 확인 — list() 의 findAllByKeyword 는 Page, requesterSearch 는 동일 메서드라 Page 추정 → stub 일치).

## 4. 요건
- **FR**: 위 C1~C10 분기 박제(Row 필드/캡처/상태 단언). 검색·프리필 회귀 방어.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, OpsDocController 78.6→~95% LINE, JaCoCo floor 유지(전역 게인 작아 버퍼 흡수 예상), ratchet 불변, 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `controller/ops/OpsDocControllerTest.java` 케이스 추가. production 0.
- 리스크: Row 매퍼(`EngineerRow.from` 등) 필드명 정확 확인 필요(구현 시 소스 대조). create 경유 C4/C5 는 saveDocument 가드(EDIT)·검증 통과 form 구성 필요(기존 create 테스트 패턴 재사용).
- isAdmin catch 1줄은 잔여(방어적, 정직성 명시).
