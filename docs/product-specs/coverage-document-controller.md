# [기획서] DocumentController 커버리지 보강 (B)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 기존 `DocumentControllerTest` 확장. 순수 단위(Mockito + SecurityContextHolder, DB·MockMvc 불요).
- **상태**: ✅ 구현 완료(2026-06-27). DocumentControllerTest +10(27 total), DocumentController 57.6→98.4% LINE(188/191), 전역 LINE 75.38→76.03%·INSTR 61.5→62.09%, floor 0.73→0.74·0.59→0.60 상향. mvnw -o clean verify 1376 green. 구현 후 codex 검증 PASS. (사전 codex는 test-only 미구현 글리치라 생략, 구현 후 검증이 실질 게이트.)
- ⚠구현 발견: findDuplicateProjDoc(Integer 반환)의 Mockito 기본값이 0(non-null)→unstubbed 시 중복분기 오진입, 명시적 thenReturn(null)로 해결. line 286 "사업문서만 처리"는 fromString 3종 한정으로 도달불가→제외.

---

## 1. 배경 / 목표

`DocumentController`(371줄, **LINE 57.6% / miss 81**)는 S4 분리 후 남은 문서 목록·상세·생성폼·저장·상태변경·삭제·미리보기 컨트롤러다. 기존 `DocumentControllerTest`(20개)는 가드(NONE/EDIT)·조회·`createForm` 기본·저장/삭제 가드만 덮어 57.6%에 머문다. 미커버 81줄은 **`createForm` 수정모드 rich 분기**와 **`saveDocument` 성공/중복 경로**, **`changeStatus` 성공**이다.

- 의존성 전부 mock(documentService·infraRepository·swProjectRepository·userRepository·logService·pdfExportService·access=실제 DocumentAccessSupport). **기존 setUp/inject/login* 재사용.**
- **기존 테스트에 케이스 추가**(파일 신규 아님). production 변경 0 (test-only). 권한 가드 약화 0.

### 미커버 표면

| 영역 | 미커버 분기 |
|---|---|
| `createForm` 수정모드(docId≠null) | project≠null → existingData(projYear/city/dist/sysNmEn/projNm) + legacyTargetText 사업 포맷 + details→sectionsMap + orgUnit≠null + support/environment / infra≠null(project=null) → legacyTargetText 인프라 포맷 / getDocumentById throws → catch 로그 |
| `saveDocument` 성공 | create(docId=null→createDocument) / update(docId≠null→getDocumentById+setTitle/sysType) / 중복검사(COMMENCE dup→400 DUPLICATE, INTERIM round 파싱 sections→inspector→paymentRound) / docNo 빈칸→null / sections 루프(saveSection) / 200 DocumentSaveResult / 500 catch |
| `changeStatus` 성공 | EDIT → changeStatus + log + 200 {success,status} |

> ⚠ line 286-289 "사업문서만 처리" 분기는 `DocumentType.fromString` 이 COMMENCE/INTERIM/COMPLETION 3종만 반환(그 외는 286 직전 catch 에서 이미 400)하므로 **도달 불가** → 테스트 대상 제외(정직성).

---

## 2. 변경 설계 — DocumentControllerTest 케이스 추가

`Document`(@Getter@Setter): setDocId/setTitle/setSysType/setDocNo/setProject/setInfra/setDetails/setOrgUnit/setSupportTargetType/setEnvironment. `SwProject`(@Getter@Setter), `Infra`, `DocumentDetail`(getSectionKey/getSectionData), `OrgUnit`(getUnitId) 구성. `saveDocument` 입력은 `Map<String,Object>`.

### A. createForm 수정모드 — project 채움
- `existing` Document: docId=7, title, project=SwProject(projId/year/cityNm="강원도"/distNm="춘천시"/sysNmEn/sysNm/projNm), details=List(DocumentDetail(sectionKey="cover", sectionData=Map)), orgUnit=OrgUnit(unitId=3), supportTargetType/environment 세팅. `getDocumentById(7)` mock.
- 단언: model `existingDoc`(Map) 존재 + `legacyTargetText` non-null(사업 포맷) + `existingOrgUnitId`=3 + `existingEnvironment` 존재. view=COMMENCE 템플릿.

### B. createForm 수정모드 — infra 폴백(project=null)
- `existing`: project=null, infra=Infra(cityNm/distNm/sysNm). → legacyTargetText 인프라 포맷 분기. 단언 non-null.

### C. createForm 수정모드 — getDocumentById 예외
- `getDocumentById(9)` throws → catch 로그, 폼은 정상 반환(view=템플릿, 예외 전파 없음).

### D. saveDocument 성공 — create
- EDIT 로그인. requestData={docType:"COMMENCE", projId:"10", title:"t", docNo:"D-1", sections:[{sectionKey:"cover", sectionData:{}}]}. findDuplicateProjDoc(10,COMMENCE,null,null)=null. createDocument(...) 반환 doc(docId=100). swProjectRepository.findById(10)=Optional.of(project). → 200, body=DocumentSaveResult(true,100,...). verify createDocument + saveSection(100,"cover",..,0) + log(CREATE).

### E. saveDocument 성공 — update + docNo 빈칸
- docId="100" → getDocumentById(100) 반환. docNo="  "(공백)→null 세팅 분기. → 200. verify getDocumentById + log(UPDATE), createDocument never.

### F. saveDocument 중복 — COMMENCE / INTERIM round
- COMMENCE: findDuplicateProjDoc(projId,COMMENCE,null,null)=55 → 400 DUPLICATE(existingDocId=55).
- INTERIM: sections=[{sectionKey:"inspector", sectionData:{paymentRound:"2"}}], findDuplicateProjDoc(projId,INTERIM,2,null)=77 → 400(round 메시지).

### G. saveDocument 500
- create 경로에서 documentService.createDocument throws → 500 + error=ExceptionMessages.safe.

### H. changeStatus 성공
- EDIT, changeStatus(1,COMPLETED,actor,comment) 반환 doc(status=COMPLETED) → 200 {success:true,status}. verify log(UPDATE).

---

## 3. 기능 / 비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | createForm 수정모드(project/infra/예외), saveDocument(create/update/중복 2종/docNo/sections/500), changeStatus 성공을 단위테스트로 박제. |
| FR-2 | 권한 가드(EDIT) 보존 — 약화 0. |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS(DB 불필요). |
| NFR-2 | DocumentController **57.6 → 90%+ LINE**(도달불가 286 분기 제외). |
| NFR-3 | floor 실측 후 상향 검토(게인 작으면 유지). ratchet/PIT 불변. |
| NFR-4 | production 변경 0. 구현 후 codex 검증 PASS → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `controller/DocumentControllerTest.java` | 기존 파일 케이스 추가 |

production 변경 0.

| 리스크 | 수준 | 완화 |
|---|---|---|
| Document/SwProject/DocumentDetail 구성 boilerplate | 낮음 | @Getter@Setter 세터로 최소 필드만 구성. |
| saveDocument Map 입력 키 다수 | 중 | 실제 컨트롤러 키(docType/projId/docNo/sections/sectionKey/sectionData/paymentRound) 그대로 사용. |
| SecurityContextHolder 누수 | 낮음 | 기존 @AfterEach clearContext() 유지. |
| 도달불가 분기(286) 무리한 커버 시도 | 낮음 | 제외 명시(정직성). |

---

## 5. 승인 요청

본 기획서(DocumentController 커버리지) 사용자 최종승인을 요청합니다. 사전 codex 검토는 직전 라운드처럼 "미구현=FAIL" 글리치가 예상되어 생략하고, **구현 후 codex 검증을 실질 게이트**로 사용합니다(test-only·production 0). 의견: 1) 도달불가 286 분기 제외 타당성, 2) saveDocument 중복/round 분기 커버 범위 충분성.
