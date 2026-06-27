# [기획서] batchGenerate·uploadSignedScan 응답 null-안전 하드닝 (백로그 #2 · correctness)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: S4 split dual-review 누적 백로그(pre-existing 실버그). [[project_swmanager_all_A_roadmap]] 하드닝.
- **상태**: ✅ **완료(2026-06-27)**. batchGenerate(성공 result HashMap·projIds null/empty→400·실패/외부 catch ExceptionMessages.safe)·uploadSignedScan(origName coalesce·catch safe). dual-review 2건 반영: safeErrorMessage 중복→`util.ExceptionMessages.safe()` 공유 추출, 실패 branch도 HashMap. 테스트 5 신규. 듀얼푸시.

---

## 1. 배경 / 목표

S4 분리 중 dual-review 가 반복 지적한 `Map.of` null-value NPE(원본부터 존재, 이동 시 보존). `java.util.Map.of` 는 null 값에 NPE → **성공 작업이 500/실패로 둔갑**하고, batchGenerate 는 **이미 생성·저장된 문서가 orphan** 으로 남는다.

| 위치 | 현재 결함 | 영향 |
|---|---|---|
| DocumentBatchController.batchGenerate 성공 result | `Map.of(...,"projNm",p.getProjNm(),"cityNm",p.getCityNm(),"distNm",p.getDistNm())` — 셋 중 null 있으면 NPE → 내부 catch → failCount++ | **문서는 createDocument+saveSection 으로 이미 저장됐는데 실패로 카운트(orphan)** |
| batchGenerate 실패 result | `Map.of("error", e.getMessage())` — getMessage()==null(예: NPE)이면 NPE → 외부 catch → **배치 전체 500** | 한 건 오류가 전체 배치를 500 으로 |
| batchGenerate projIds | `(List<Number>) requestData.get("projIds")` null → for-each NPE → 500 | 잘못된 요청이 400 아닌 500 |
| DocumentFileController.uploadSignedScan 성공 result | `Map.of("fileName", doc.getSignedScanOrigName())` — origName null 이면 NPE → 500 | **업로드 성공인데 500 응답** |

**목표**: 위 4 지점 null-안전화 — 성공은 성공으로(200), 잘못된 요청은 400 으로. 응답 형태(키)는 보존(값만 null→안전). 다른 로직 불변.

---

## 2. 변경 설계

### 2-A DocumentBatchController.batchGenerate
- **projIds null/empty 선검증**: docType 검증 직후 `if (projIds == null || projIds.isEmpty()) return badRequest("대상 사업이 없습니다.")`. (현 docType 검증과 동일 패턴.)
- **성공 result**: `Map.of` → `HashMap` 사용(또는 projNm/cityNm/distNm 를 `nz()`=null→"" 로 coalesce). null 허용·키 보존. **결정: HashMap**(값 null 그대로 담되 NPE 없음 — 현행 JSON 직렬화 동일, 키 동일).
- **실패 result(내부 catch) + 외부 catch(codex)**: 둘 다 `Map.of("error", e.getMessage())` → 공통 `safeErrorMessage(e)`(= e.getMessage()!=null ? e.getMessage() : e.getClass().getSimpleName()) 적용. 외부 catch(전체 500 응답)도 null-message NPE 방지.
  > ⚠**범위 외(codex)**: projIds 가 List 아님/원소가 null·non-Number 인 경우는 ClassCastException(별 결함군) → 여전히 500. 본 스프린트는 **null-안전 + projIds null/empty→400** 한정. 타입/원소 검증은 후속 백로그.

### 2-B DocumentFileController.uploadSignedScan
- 성공 result `Map.of("fileName", doc.getSignedScanOrigName(), ...)` → origName null-coalesce("") 또는 HashMap.

> 형태 보존: 응답 JSON 키 집합·성공/실패 의미 불변. **값이 null 일 때 NPE 대신 정상 응답**(success=true/200, 또는 fail result 의 error 문자열).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | batchGenerate: 대상 사업의 projNm/cityNm/distNm 가 null 이어도 성공 카운트(successCount++)·result 정상(문서 orphan 아님). |
| FR-2 | batchGenerate: projIds 누락/빈 배열 → 400(badRequest), 서비스 미호출. |
| FR-3 | batchGenerate: 개별 처리 예외의 getMessage()==null 이어도 해당 건만 failCount++(배치 전체 500 아님). |
| FR-4 | uploadSignedScan: signedScanOrigName==null 이어도 200 success(파일명 빈 문자열). |
| FR-5 | 정상값(비-null) 경로 응답·동작 불변. 권한 가드·로직·다른 엔드포인트 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 테스트: batchGenerate_nullNameFields_success(SwProject projNm/cityNm/distNm null→성공 카운트), batchGenerate_nullProjIds_badRequest(+ empty 배열도 400 assertion), batchGenerate_itemException_nullMessage_failsOne(**documentService.saveSection mock 이 message-null 예외(new NullPointerException()) throw → 해당 건만 failCount, 전체 200**), uploadSignedScan_nullOrigName_ok(Document mock getSignedScanOrigName null→200). 기존 테스트 불변. |
| NFR-3 | 커버리지 비감소·floor 유지·ratchet 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 HashMap vs coalesce** ✅: 성공 result 는 **HashMap**(키 다수, null 허용, 직렬화 동일). uploadSignedScan·실패 error 는 단일 값이라 coalesce 로 충분. (구현 시 일관되게 — 개발계획에서 확정.)
- **5-2 projIds 빈 배열도 400** ✅: null 과 동일 취급(생성 0건 = 잘못된 요청). docType 검증과 같은 badRequest 패턴. ⚠**프런트 영향(codex)**: 현행 빈 배열은 200 `{success,totalCount:0}` no-op → 400 으로 변경. 프런트는 대상 미선택 시 호출 안 함(빈 선택 호출은 비정상)이라 UX 영향 미미하나, 배포 시 빈 선택 호출 흐름 확인 권장.
- **5-3 형태 보존** ✅: 신규 키 추가/제거 없음. 값 null-안전만. 프런트 계약 영향 0(오히려 500→정상).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller | DocumentBatchController·DocumentFileController | null-안전 수정(소규모) |
| Test | DocumentBatchControllerTest·DocumentFileControllerTest | 신규 4 |

UI/DB 변경 0. **동작 변경 = 성공/400 정상화(버그 수정)**.

| 리스크 | 수준 | 완화 |
|---|---|---|
| HashMap 전환으로 응답 키 변동 | 낮음 | 동일 키만 put, 직렬화 결과 동일(테스트로 키 확인) |
| projIds 빈배열 400 이 기존 클라 흐름 깸 | 낮음 | 빈 배열은 애초 생성 0(의미 없는 요청). 정상 흐름은 비어있지 않음 |
| 정상 경로 회귀 | 낮음 | 기존 success/none/invalid 테스트 유지 + 신규 null 케이스 |

---

## 7. 승인 요청

본 기획서(batch/upload 응답 null-안전 하드닝)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
