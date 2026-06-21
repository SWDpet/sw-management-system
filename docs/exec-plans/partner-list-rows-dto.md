# 개발계획서 — PartnerController 협력업체 목록 조회 응답 Map→record (partner-list-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 후속. `inspect-report-rows-dto` 와 동일 패턴(읽기 API 의 컨트롤러-로컬 `List<Map>` 응답조립을 record 로 치환). PartnerController 의 **읽기 1 엔드포인트**(`/api/list`) 대상 — 해당 컨트롤러의 `Map<String,Object>` 5개 전수가 이 메서드에 있음.
- **무손실 핵심**: 컨트롤러-로컬 `LinkedHashMap`(업체 + 중첩 contacts) 응답조립 → JsonNode tree 동치(키셋·값·null)로 무손실. **키가 snake_case**(partner_id/partner_type/main_tel/contact_id) → record 에 `@JsonNaming(SnakeCaseStrategy)` 부착으로 컴포넌트(camelCase)→snake 자동 매핑(단일어 name/note/tel/email/position/contacts 는 변화 없음).
- **디자인팀**: 비해당 — 백엔드 전용(응답 JSON 무손실, 클라이언트 무변경). **DB**: 변경 없음.

## 대상 1 메서드 + record 설계 (com.swmanager.system.dto.ops)

| 메서드 | 응답 키(현행 snake) | record |
|---|---|---|
| `list` `/api/list` | partner_id, name, partner_type, main_tel, note, contacts[{contact_id, name, position, tel, email}] | `PartnerListRow(Long partnerId, String name, String partnerType, String mainTel, String note, List<PartnerListContactRow> contacts)` + `PartnerListContactRow(Long contactId, String name, String position, String tel, String email)`, 둘 다 `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` |

- `PartnerListContactRow.from(PartnerContact)`, `PartnerListRow.from(Partner, List<PartnerListContactRow>)` (contacts 는 별도 쿼리라 호출부에서 조립 후 주입).
- ⚠ 명칭: dto.ops 에 이미 ops-doc 검색용 `PartnerRow`(id/name/type)·`PartnerContactRow`(id/name/org/pos/tel)가 존재(다른 wire) → 충돌 회피 위해 `PartnerListRow`/`PartnerListContactRow` 로 명명.
- 권한 `!canView` → 빈 리스트(`List<PartnerRow>`) 그대로 — 현행 빈 배열 경로 보존.
- 타입: partnerId/contactId = Long, 그 외 전부 String. `@JsonInclude` 미부착(null 포함).

### 보존 (record 비대상)
- `create`/`update` 등의 `Map.of("success",..., "partner_id",...)` 응답·`forbidden()` 헬퍼 = P6(도메인키)/envelope. 본 sprint 범위 외(ratchet 비대상 `Map.of`라 영향 없음). 추후 ApiResult 설계 시 별도.

## 목표 (FR/NFR)
- **FR**: `list` 의 `Map<String,Object>` 5선언 → record 2종. **`Map<String,Object>` 선언 277→272 (−5)**.
- **NFR**: wire 무손실(snake 키셋·값·타입·null·중첩 contacts), 회귀 0, ratchet 272 tighten. URL·빈배열 경로 보존.

## 검증 (골든 = 레거시 LinkedHashMap 복제본과 tree 동치)
1. `PartnerRowsTest`(신규): 채운 엔티티 → `from()` 직렬화가 현행 `LinkedHashMap` 복제본과 **JsonNode tree 동치**. 경계: snake 키명 정확(partner_id/partner_type/main_tel/contact_id), null 필드 키 보존, contacts 빈 리스트·다건.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 277→272.
3. `./mvnw test` 전체 green.
4. codex 검토(snake 키 매핑·tree 동치·null·빈배열).

## 롤백
원자 1 커밋 → `git revert`. record 2 신규 + 컨트롤러 1 메서드 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): PartnerController 협력업체 목록 조회 응답 Map→record 2종(snake_case 보존) + ratchet 277→272 (§6-4)`
