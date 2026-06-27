# [기획서] DocumentController 분리 — S4 Phase 2 (착수계 일괄 batch)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 로드맵 S4 거대클래스 분리. Phase 1(다운로드, `e332d5f`) 후속.
- **상태**: ✅ **완료(2026-06-27)**. DocumentBatchController 신설, DocumentController 1034→799(-235, 원본 1373 대비 -574). 커버리지 LINE 71.09% 상승(성공경로 테스트). dual-review codex 0/Opus 12(합의 11=전부 batchGenerate 원본 pre-existing 동작 바이트 동일 이동→순수이동 원칙상 무변경, 후속 하드닝 백로그). 듀얼푸시.

---

## 1. 배경 / 목표

Phase 1 로 DocumentController 1373→1034. 잔존 책임 중 **착수계 일괄 생성(batch)** 이 가장 큰 단일 그룹(≈245줄)이며 독립 기능(연도별 시스템 대상 → 공문 문서 일괄 생성). 이를 신규 `DocumentBatchController` 로 추출.

**목표**: batch 4 엔드포인트 + 헬퍼를 분리. **동작 100% 동일(순수 이동)**, baseline 감소-only. 인증은 Phase 1 의 `DocumentAccessSupport` 재사용.

비목표: 서명/첨부/signed-scan·조회 API·plan 은 후속 Phase.

---

## 2. 변경 설계

### 2-A 신규 `DocumentBatchController` (@Controller, @RequestMapping("/document"))
- 이동 엔드포인트(매핑·시그니처·본문 불변):
  - `GET /batch` (batchPage)
  - `GET /api/project-systems-all` (getAllSystemsForYear)
  - `GET /api/batch/targets` (getBatchTargets)
  - `POST /api/batch/generate` (batchGenerate)
- 이동 private 헬퍼: `buildBatchLetterData` (batchGenerate 전용, 호출처 1).
- 주입: DocumentService, SwProjectRepository, LogService, UserRepository, DocumentAccessSupport.
- getAuth()/getCurrentUser() 호출 → `access.*` 로 치환.
- import(codex): SystemAllRow·BatchTargetRow·DocumentType·Document·User·CustomUserDetails·MenuName·AccessActionType·Model·ResponseEntity·@Slf4j·web 애노테이션 + 표준컬렉션(ArrayList/HashMap/List/Map). ⚠**원본 DocumentController 에서 SystemAllRow·BatchTargetRow import 제거**.

### 2-B DocumentController 정리
- 위 4 엔드포인트 + buildBatchLetterData 제거.
- ⚠**필드 제거 판단**: documentService·swProjectRepository·logService·userRepository 는 core(list/detail/create/save 등)에서도 사용 → **잔존**(제거 금지). batch 전용 필드 없음(제거 0). 결과: DocumentController 1034 → 약 790줄.

> ⚠**정정(codex)**: `/document` 는 이미 Controller/Download/Lookup/Participant **4개** 공존 → batch 는 *추가* 컨트롤러. full path 비충돌(`/batch`, `/api/batch/*`, `/api/project-systems-all` 는 현재 DocumentController 단독 소유 → 이동 후 원본 제거만 정확하면 안전). @SpringBootTest 부팅이 ambiguous mapping 자동검출.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 이동 4 엔드포인트의 URL·HTTP 메서드·요청/응답·권한 가드(batchPage/batchGenerate=EDIT)가 이전과 완전 동일. |
| FR-2 | batchGenerate 의 대상조회·문서생성(documentService.createDocument)·실패카운트·buildBatchLetterData 공문데이터 매핑 불변. |
| FR-3 | DocumentController 잔존(core CRUD·preview·서명/첨부·조회·plan)·DocumentDownloadController 동작 불변. |
| FR-4 | 신규 기능·로직 변경·UI 변경 0(순수 이동). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 기존 DocumentControllerTest 의 batchPage*/batchTargets*/allSystemsForYear*/batchGenerate* (9) → DocumentBatchControllerTest 로 이관(inject 대상 DocumentBatchController + access 주입), 동일 단언. 잔존 테스트 불변. |
| NFR-2c | **codex: batchGenerate 성공 경로 테스트 1건 신규**(현재 9건은 거부/invalid/missing-fail 만 — "동작 동일" 게이트 강화). 성공 케이스에서 documentService.createDocument 호출 + 생성 카운트 검증(buildBatchLetterData 공문데이터 포함). |
| NFR-2b | @SpringBootTest(DashboardAndAdminLogsSecurityIntegrationTest) 부팅 성공(매핑 비충돌). |
| NFR-3 | 전역 커버리지 비감소(코드 이동). floor 유지. |
| NFR-4 | DocumentController LOC 추가 감소(baseline 개선). |
| NFR-5 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 Phase 2 = batch** ✅: 잔존 최대 단일 그룹(≈245줄)·독립 기능·자기완결(buildBatchLetterData 외 공유 private 없음, asInt/asBool 은 plan 전용이라 무관).
- **5-2 공유 필드 잔존** ✅: documentService/swProjectRepository/logService/userRepository 는 core 도 사용 → 양 컨트롤러 주입(중복 주입은 정상, 필드 제거 아님). batch 전용 필드 없어 DocumentController 필드 수 불변.
- **5-3 DocumentAccessSupport 재사용** ✅: Phase 1 컴포넌트 그대로 주입(getAuth admin→EDIT 일관).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller(신규) | `controller/DocumentBatchController.java` | 신규 |
| Controller(수정) | `controller/DocumentController.java` | batch 4+헬퍼 제거 |
| Test(신규/이관) | `controller/DocumentBatchControllerTest.java` | 이관 |
| Test(수정) | `controller/DocumentControllerTest.java` | batch 케이스 제거 |

UI/DB/API 계약 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 매핑 충돌(동 base path) | 중 | 제거 후 clean verify(startup 실패로 즉시 검출) |
| batchGenerate 복잡 로직 이동 오류 | 중 | 본문 바이트 동일 이동 + 기존 9 테스트 이관(동일 단언) |
| 빈 주입 누락 | 중 | 컴파일 + verify |
| 권한 가드 누락 이동 | 높음 | EDIT 가드 동반 이동 + batchPage/batchGenerate 거부 테스트 이관 |

---

## 7. 승인 요청

본 기획서(S4 Phase 2)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
