# 개발계획서 — DocumentController 파일관리(첨부/서명/날인) 응답 Map→record/ResponseEntity<?> (document-file-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락 정정(중요)**: DocumentController 는 현재 **1460줄**(controller 임계 1500 **미만** — 이전 §6-5 추출로 이미 트리밍, 메모리 "1752/거대클래스"는 stale). `giant-class-baseline.txt` 비어있음(거대클래스 0). → **풀 컨트롤러 분리는 불필요**(거대클래스 명분 소멸). 실제 핫스팟은 `Map<String,Object>` 부채. **저리스크 in-place 치환**으로 파일관리 그룹 응답 Map 제거(헌장: 핫스팟·과설계 지양).
- **클라이언트**: 첨부/서명/스캔 UI(문서 상세/작성). 응답 키 보존 필수. **디자인팀** 비해당. **DB** 무관.

## 대상(파일관리 그룹 L763-900) — 치환 7 / 보존 1

| 메서드 | 처리 |
|---|---|
| `getAttachments` GET /api/attachments/{docId} | 읽기 projection `List<Map>`(attachId/fileName/fileSize/mimeType/uploadedAt) → `List<AttachmentRow>` record. **−3선언**(814 반환·816 List·817 m) |
| `uploadAttachment` POST | 반환타입 `ResponseEntity<Map<String,Object>>`→`ResponseEntity<?>`. 성공 `Map.of(success,attachId,fileName,fileSize)`·error `Map.of` **바디 보존**(Map.of=ratchet 비대상). −1(792) |
| `deleteAttachment` POST | 반환타입→`?`. 바디 `Map.of(success)`/`Map.of(error)` 보존. −1(830) |
| `uploadSignedScan` POST | 반환타입→`?`. 바디 `Map.of(success,fileName,fileSize)`/`Map.of(error)` 보존. −1(844) |
| `deleteSignedScan` POST | 반환타입→`?`. 바디 보존. −1(887) |
| `saveSignature` POST /api/signature/save | 반환타입→`?`. **요청바디 `@RequestBody Map<String,Object> data` 보존**(lenient: `data.get("docId").toString()`·`(String)`캐스트 — participant 와 동일 무손실 한계). 바디 `Map.of` 보존. 라인(765)은 요청 Map 으로 **잔존**(보존) |
| `downloadSignedScan` GET | Map 없음(Resource 반환) — 무변 |

- **신규 record**: `AttachmentRow(Integer attachId, String fileName, Long fileSize, String mimeType, String uploadedAt)` ← `from(DocumentAttachment)` (uploadedAt = `getUploadedAt().toString()`). com.swmanager.system.dto.workplan.
- 타입(엔티티 게터): attachId=Integer, fileName=String, fileSize=Long, mimeType=String, uploadedAt=String(toString).
- **응답 wire 100% 보존**: 성공/error 바디는 현행 Map.of 그대로(키·값·status 동일). 반환타입만 `?` 로 완화(Map.of 가 그 형태로 직렬화). getAttachments 만 record(키셋·값·null 동치).
- saveSignature 요청바디는 보존(타입화는 무손실 불가, codex participant 논의 결론 동일).

## 목표 (FR/NFR)
- **FR**: 파일관리 그룹 `Map<String,Object>` 7선언(792·814·816·817·830·844·887) 제거 → record 1 + ResponseEntity<?>. **선언 250→243 (−7)**. saveSignature 765(요청바디) 보존.
- **NFR**: 응답 wire·status(200/403/400/500) 무손실, getAttachments 키셋 동치, 회귀 0, ratchet 243 tighten. URL 무변(EndpointInventory 불변).

## 검증
1. `AttachmentRowTest`(신규): AttachmentRow.from → 현행 HashMap(attachId/fileName/fileSize/mimeType/uploadedAt) 복제본과 JsonNode tree 동치(키셋 5·값·null). uploadedAt toString 보존.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 250→243.
3. `./mvnw test` 전체 green(EndpointInventory·GiantClass 불변 확인).
4. codex 검토(응답 wire·Map.of 바디 보존·요청바디 보존·status·미사용 import).

## 롤백
원자 1 커밋 → `git revert`. record 1 신규 + DocumentController 파일관리 6 메서드 응답 국소 치환(이동 없음). 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentController 첨부/서명/날인 응답 Map→AttachmentRow record/ResponseEntity<?>(요청바디·Map.of 바디 보존) + ratchet 250→243 (§6-4)`
