# 개발계획서 — InspectionQrBatch 예외·AdminUser 민감필드 응답 Map→record (qrbatch-adminuser-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 후속. 읽기/예외 응답의 컨트롤러-로컬 고정형 `Map<String,Object>` 조립을 record 로 치환(이전 sprint 동일 패턴). 두 컨트롤러의 잔여 Map 전수.
- **무손실 핵심**: 컨트롤러-로컬 `LinkedHashMap` 고정형 조립 → JsonNode tree 동치(키셋·값·null)로 무손실. 클라이언트는 응답 JSON 키로 접근.
- **디자인팀**: 비해당(백엔드 전용). **DB**: 변경 없음.

## 대상 2 메서드 + record 설계 (원자 2 커밋)

| # | 메서드 | 응답 키 | record |
|---|---|---|---|
| C1 | `InspectionQrBatchController.handleSiteNotMapped` (`@ExceptionHandler`, 422) | error("site_not_mapped"), site, hint | `SiteNotMappedError(String error, String site, String hint)` (com.swmanager.system.dto.inspection) — 핸들러에서 `e.getSiteCode()`·상수 hint 주입 |
| C2 | `AdminUserController` 민감필드 조회 성공 응답(200) | field, value | `AdminSensitiveFieldRow(String field, String value)` (com.swmanager.system.dto) — **value null→"" fallback 보존**(현행 L370). 민감필드 화이트리스트 응답에 신규 키 추가 금지 |

- C1: 반환타입 `ResponseEntity<Map<String,Object>>` → `ResponseEntity<SiteNotMappedError>`. HTTP 422·키 3개·값 보존.
- C2: 성공 응답만 record(field/value 2키). 동일 메서드의 400 `Map.of("success",false,"error",{...})`(허용외 필드)은 Map.of(ratchet 비대상)이라 보존 — 반환타입 `ResponseEntity<?>`.
- 타입: 전부 String. `@JsonInclude` 미부착(C1 은 null 없음, C2 value 는 "" fallback 으로 null 미발생).

## 목표 (FR/NFR)
- **FR**: 위 2 메서드의 `Map<String,Object>` 선언(InspectionQrBatch 2 + AdminUser 1 = 3) → record 2종. **`Map<String,Object>` 선언 258→255 (−3)**.
- **NFR**: wire 무손실(키셋·값·null·민감 화이트리스트 유지), HTTP status(422/200) 보존, 회귀 0, ratchet 255 tighten.

## 검증 (골든 = 레거시 LinkedHashMap 복제본과 tree 동치)
1. `MiscEnvelopeRowsTest`(신규): 두 record 직렬화가 현행 `LinkedHashMap` 복제본과 **JsonNode tree 동치**. 경계: SiteNotMappedError 3키, AdminSensitiveFieldRow value 일반/null→"" 2키.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 258→255.
3. `./mvnw test` 전체 green.
4. codex 검토(키·값·민감 화이트리스트·status 보존).

## 롤백
원자 2 커밋 → 커밋별 `git revert`. record 2 신규 + 컨트롤러 2 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
- C1 `refactor(api): InspectionQrBatchController site_not_mapped 예외 응답 Map→SiteNotMappedError record + ratchet 258→256 (§6-4)`
- C2 `refactor(api): AdminUserController 민감필드 조회 응답 Map→AdminSensitiveFieldRow record(화이트리스트 보존) + ratchet 256→255 (§6-4)`
