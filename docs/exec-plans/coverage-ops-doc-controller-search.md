# 개발계획서 — OpsDocController 검색·캐스케이드·관계자 분기 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +20테스트 green, OpsDocController 78.6→98% LINE. 구현 후 codex PASS + dual-review 합의2 반영. floor 0.75/0.61 상향. `mvnw -o clean verify` 1420 green.
- **codex 보완 반영**: create type="FAULT"(비-INSPECT) 명시 · **검색/캐스케이드 read 엔드포인트는 무가드(currentUser 파라미터 없음)** → 인증 불필요 · 모든 repo stub 은 실 시그니처(Optional.of / PageImpl) 정확 매칭 · relations 4케이스 각각 독립 테스트(스텁 격리).
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-ops-doc-controller-search.md` (codex APPROVE-WITH-FIX 반영)
- **안전망**: 현 green 스위트(`./mvnw -o clean verify`) + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `OpsDocControllerTest`(46) 확장(신규 파일 아님). 검증된 17 mock setUp·헬퍼 재사용. 커밋 1개(원자).

---

## Step 0 — baseline 고정 + 사실 확인(완료)

- 전역 LINE 77.24%·INSTR 63.16%(floor 0.74/0.60, 버퍼 ~3.2pp). 대상 OpsDocController LINE 78.6%(miss 86, nc 라인 실측 기획서 §1).
- **확정 사실**(소스 대조 완료):
  - `OpsDocForm(title, sysType, regionCode, environment, supportTargetType, engineerId:Long, requesterKind:String, requesterId:Long, sectionData:Map, partners:List<PartnerRef>)`. `PartnerRef(partnerId:Long, roleLabel:String)`.
  - create `@PostMapping("/api/{type}")`: INSPECT→400, `requireDocEdit`(EDIT) 가드 → doc 빌드 → `applyRelations` → `opsDocService.create(doc, sectionData, userId)`→saved → `savePartners(saved.docId, form, false)`.
  - Row 매퍼: `EngineerRow(userSeq, username, position?:positionTitle)` · `RequesterRow(id, userNm, orgNm, deptNm, pos, tel)` · `PartnerContactRow(contactId, name, partner?.name, position, tel)` · `PartnerRow(partnerId, name, partnerType)` · `DocPartnerRow(partnerId, p!=null?p.name:"#"+id, roleLabel)` · `StaffRow.of(staffId, name, org, position)` · `CascadeSggRow(admSectC, sggNm, sggNm.equals(sidoNm))` · `CascadeSystemRow(cd, nm)`.
  - `requesterSearch`/`list` 의 `personInfoRepository.findAllByKeyword(kw, Pageable)` → **Page 반환**(stub `new PageImpl<>(List.of(p))`).

**검증:** 없음(준비).

## Step 1 — C1/C2: list INSPECT 지역·유형 해석 + parseReportId

**1-1.** `list_inspectDoc_singleCandidate_resolvesRegionAndMaintBadge`:
- `OpsDocument`(docType=INSPECT, docNo="OPS-INSPECT-42", regionCode=null) 1건 → `opsDocService.findAll()`.
- `inspectReportRepository.findById(42L)` → report(pjtId=7L), `swProjectRepository.findById(7L)` → project(cityNm="서울특별시", distNm="종로구", maintType="HS").
- `sigunguCodeRepository.findAll()` → [SigunguCode(admSectC,sidoNm="서울특별시",sggNm="종로구")] (bySgg["종로구"]=단일후보) + `findDistinctSidoNm()` stub.
- 단언: model `sidoByDoc[docId]`="서울특별시"·`sggByDoc[docId]`="종로구"·`maintLabelByDoc[docId]`=`InspectMaintProfile.badgeLabel("HS")`·`maintToneByDoc` 동일. view="ops-doc/list".

**1-2.** `list_inspectDoc_multiCandidate_picksBySido`(codex 보완):
- bySgg["공통"]= **후보 2개**: c0(sidoNm="부산광역시",sggNm="공통"), c1(sidoNm="서울특별시",sggNm="공통"). project cityNm="서울특별시"(=c1만 매칭), distNm="공통".
- 단언: 결과 sido="서울특별시"(=c1, **cand.get(0)인 c0 아님** → `cand.size()>1 && sido` 루프 커버).

**1-3.** `list_inspectDoc_noCandidate_fallsBackToProjectNames`: bySgg에 distNm 키 없음 → `{nz(cityNm),nz(distNm)}` 폴백 단언.

**1-4.** `list_inspectDoc_unparseableDocNo_skipsResolution`(C2): docNo="OPS-INSPECT-XX"(parseReportId catch→null) → sidoByDoc="-"·maintLabel 미존재(resolveMaintType null).

**검증:** `./mvnw -o -Dtest=OpsDocControllerTest test` green(부분).

## Step 2 — C3: support-file 500 catch

**2-1.** `uploadSupportFile_serverError_returns500`: editUser, `supportFileService.store(...)` `thenThrow(new RuntimeException("disk"))` → status 500 + `ApiResult.fail` code SERVER_ERROR. (400 catch 는 기존 테스트가 IllegalArgument 로 이미 커버 — 확인 후 중복 시 생략.)
**2-2.** `deleteSupportFile_serverError_returns500`: `supportFileService.delete` `thenThrow(RuntimeException)` → 500 SERVER_ERROR.

## Step 3 — C4/C5: applyRelations + savePartners (create 경유)

**3-1.** `create_withRelationsAndPartners_appliesAll`(EDIT):
- form= engineerId=5L, requesterKind="PERSON", requesterId=9L, partners=[ref(3,"주관"), ref(3,"주관")(중복), ref(7,"협력")].
- `userRepository.findById(5L)`→User · `personInfoRepository.findById(9L)`→PersonInfo · `opsDocService.create(any, any, any)`→saved(docId=100L).
- `ArgumentCaptor<OpsDocument>` (opsDocService.create 1번째 인자) → setEngineer 반영·setRequesterPerson 반영(PERSON). `opsDocPartnerRepository.save` **2회**(중복 (3,주관) 1건 제거) verify + 캡처로 partnerId/roleLabel 단언.
**3-2.** `create_requesterContact_setsContactId`: requesterKind="CONTACT", requesterId=11L → 캡처 doc.requesterContactId=11(repo 조회 없음). **CONTACT/STAFF는 personInfo 조회 안 함** 단언(never).
**3-3.** `create_requesterStaff_setsStaffId`: requesterKind="STAFF", requesterId=22L → requesterStaffId=22.

## Step 4 — C6~C10: 검색/캐스케이드/관계자

각 1 stub 엔티티 → 리스트 size 1 + Row 필드 exact 단언:
- **engineers()**: `orgUnitRepository.findFirstByNameAndUnitType("SW지원팀","TEAM")`→OrgUnit(unitId=3) + `userRepository.findByOrgUnitIdAndEnabledTrueOrderByUsernameAsc(3L)`→[User(userSeq/username/position)] → EngineerRow 필드. + **swTeam null → 빈 리스트** 분기 1건.
- **requesterSearch("kw")**: `findAllByKeyword("kw", Pageable)`→PageImpl([PersonInfo]) → RequesterRow 6필드.
- **partnerContactSearch**: `searchActive`→[PartnerContact(contactId,name,partner(name),position,tel)] → PartnerContactRow(org=partner.name).
- **partnerSearch**: `findByUseYnAndNameContainingOrderByNameAsc("Y","kw")`→[Partner] → PartnerRow(id,name,type).
- **docPartners(docId)**(C8): `opsDocPartnerRepository.findByDocId`→[dp1,dp2]; `partnerRepository.findById`(dp1→Partner found, dp2→empty) → DocPartnerRow name="업체명" vs "#partnerId" **양분기**.
- **staffSearch**(C7): `staffRepository.searchActive`→[s1(orgUnitId=4), s2(orgUnitId=null)]; `orgUnitRepository.findById(4)`→OrgUnit(name="A팀") → StaffRow org="A팀"(found) vs null(orgUnitId=null) **양분기**.
- **cascadeSgg("서울특별시")**: `findBySidoNmOrderBySggNm`→[SigunguCode] → CascadeSggRow(isUnit sgg==sido 양케이스 가능 시 1건).
- **cascadeSystems()**: `sysMstRepository.findAll(Sort)`→[SysMst(cd,nm)] → CascadeSystemRow.
- **relations(docId)**(C10): `opsDocService.findById`→OpsDocument; (a)CONTACT requesterContactId set + `partnerContactRepository.findById`→found(label "name / partner명") · (b)CONTACT found=empty→폴백 "업체담당자 #id" · (c)STAFF requesterStaffId set + `staffRepository.findById`→found(label "name position"). + d==null→빈 Map 1건.

**검증:** `./mvnw -o -Dtest=OpsDocControllerTest test` 전체 green.

## Step 5 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1400 → +N).
- OpsDocController LINE 78.6 → **~95%** 확인(JaCoCo html; 잔여=isAdmin catch 1줄 등 의도적 제외).
- 전역 LINE/INSTR 재측정. 게인 작으면 floor 유지. ratchet(거대클래스·Map)·PIT 게이트 불변 확인.

## Step 6 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기 누락·brittle·가드 약화 0·Row 필드 정확).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): OpsDocController 검색·캐스케이드·관계자·INSPECT지역해석 분기 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1·C2 INSPECT 해석/parse catch | Step 1 |
| FR C3 500 catch | Step 2 |
| FR C4·C5 relations/partners dedup | Step 3 |
| FR C6~C10 검색/캐스케이드/관계자 Row | Step 4 |
| NFR verify SUCCESS·78.6→~95% | Step 5 |
| NFR floor/ratchet/PIT 불변 | Step 5 |
| NFR production 0·듀얼푸시 | Step 6 |

## 롤백

- 단일 테스트 파일에 케이스 추가만 → 문제 시 해당 메서드 제거 또는 `git checkout`. production 무영향.
