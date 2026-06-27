# [개발계획서] DocumentController 분리 — S4 Phase 2 (착수계 batch)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-document-controller-split-phase2.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. S-1~S-5 수행. DocumentController 1034→799·DocumentBatchController(batch 4+buildBatchLetterData). 9 이관+성공경로 신규. ⚠ControllerRepositoryRatchet baseline 295→297(batch가 새 컨트롤러서 swProject/userRepository 직접접근 — 이동의 기계적 +2, GOLDEN_RECORD 갱신). clean verify green.

## 후속 하드닝 백로그 (dual-review Opus 11건 — 전부 batchGenerate 원본 pre-existing, 이번 순수이동서 미변경)
- Map.of NPE: 성공 result(projNm/cityNm/distNm null)·catch(e.getMessage() null) → HashMap/String.valueOf
- projIds null/타입 미검증 → 400 선검증(현재 500)
- read API(project-systems-all/batch/targets) 무가드 → EDIT 가드 여부 보안결정 필요
- per-project 트랜잭션 경계(부분커밋) → 서비스 @Transactional
- 수신자 "null청" 폴백·doc.setProject 영속·paymentRate 무로그 swallow

---

## 1. 작업 개요

batch 4 엔드포인트 + buildBatchLetterData 를 신규 `DocumentBatchController` 로 이동. 순수 이동(로직/시그니처/매핑/가드 불변) + codex 보완 success 테스트 1건. Phase 1 의 `DocumentAccessSupport` 재사용.

---

## 2. 구현 순서 (S-n)

### S-1 DocumentBatchController 신설
- `controller/DocumentBatchController.java` (@Controller @RequestMapping("/document"), @Slf4j, 필드주입 — DocumentController 스타일).
- 주입: DocumentService, SwProjectRepository, LogService, UserRepository, DocumentAccessSupport access.
- **이동**(DocumentController 에서 잘라내기, 본문 불변):
  - batchPage(/batch), getAllSystemsForYear(/api/project-systems-all), getBatchTargets(/api/batch/targets), batchGenerate(/api/batch/generate).
  - private buildBatchLetterData.
- 본문 `getAuth()`/`getCurrentUser()` → `access.getAuth()`/`access.getCurrentUser()`.
- import: SystemAllRow·BatchTargetRow·DocumentType·Document·User·CustomUserDetails·MenuName·AccessActionType·Model·ResponseEntity·web 애노테이션·java.util.*.

### S-2 DocumentController 정리
- 위 4 엔드포인트 + buildBatchLetterData 제거.
- **SystemAllRow·BatchTargetRow import 제거**(이동으로 미사용). 다른 잔존 코드가 쓰는지 grep 확인 후 제거.
- 공유 필드(documentService/swProjectRepository/logService/userRepository) 잔존(core 사용). batch 전용 필드 없음.

### S-3 테스트 이관
- `DocumentBatchControllerTest.java` 신설: batchPage*/batchTargets*/allSystemsForYear*/batchGenerate* (9) 이관. 셋업=DocumentControllerTest 패턴(필드주입 reflection + login 헬퍼), 주입 documentService/swProjectRepository/logService/userRepository + `new DocumentAccessSupport()`(access).
- **codex: batchGenerate 성공경로 1건 신규(단언 구체화)** — EDIT 로그인 + swProjectRepository.findById 로 대상 사업(SwProject 필드 세팅) stub + documentService.createDocument → docId stub. 단언: 응답 **successCount=1·failCount=0·totalCount=1·results[0].docId**(exact) + documentService.createDocument 호출 + **saveSection(docId, "letter", letterData, 0) 을 ArgumentCaptor<Map> 로 캡처**해 commonData 반영(to/manager/tel/date)·title·body 키 존재 검증(buildBatchLetterData 경유). ⚠구현 시 batchGenerate 본문의 정확한 service 메서드명(createDocument/saveSection) 재확인.
- DocumentControllerTest: 이관 batch 케이스 제거. 잔존 불변(access 주입 유지).

### S-4 검증
- `./mvnw -o clean verify` — compile + 전체 테스트 + @SpringBootTest 부팅(ambiguous mapping 검출) + JaCoCo 게이트 green.
- DocumentController LOC 확인(약 790 목표).

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 이관 9 동일 단언 green |
| NFR-2b | S-4 @SpringBootTest 부팅(매핑 비충돌) |
| NFR-2c | S-3 batchGenerate 성공경로 신규 green |
| NFR-3 | S-4 JaCoCo 게이트(코드 이동→비감소) |
| NFR-4 | S-4 DocumentController LOC 감소 |
| NFR-5 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 4 매핑 잔존 → ambiguous | S-2 제거 후 clean verify(startup 실패로 즉시 검출) |
| batchGenerate 본문 이동 오류 | 바이트 동일 이동 + 9 이관 + success 신규 |
| import 누락/잔존 | 컴파일 + grep 확인 |

롤백: 단일 커밋 `git revert`. 순수 구조 이동(+테스트 1건)이라 프로덕션 의미 변화 0.

---

## 5. 커밋

- `refactor(document): DocumentController 착수계 batch 분리 — S4 Phase 2`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
