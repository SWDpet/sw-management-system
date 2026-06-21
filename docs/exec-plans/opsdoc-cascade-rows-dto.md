# 개발계획서 — OpsDocController 지역/시스템 cascade 응답 List<Map>→record (opsdoc-cascade-rows-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 9번째. 직전 `opsdoc-search-rows-dto` 의 직속 후속 — OpsDocController 의 컨트롤러-로컬 `List<Map>` 응답조립 중 **cascade 2종**(`cascadeSgg` 시도→시군구, `cascadeSystems` 시스템 마스터)을 record 로 치환. 이로써 OpsDocController 의 read-path 응답조립 Map 정리 마무리.
- **무손실 핵심**: 둘 다 `HashMap` 기반 → 키순서 비결정 → tree 동치(키셋·값)만으로 무손실. 클라이언트는 응답 JSON 키로 접근. 전역 jackson non_null 미설정 → null 포함 기본.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: `cascadeSgg`·`cascadeSystems` 의 `List<Map<String,Object>>`(반환타입·`out`·`m` = 각 3줄) → record 2종. **`Map<String,Object>` 선언 340→334 (−6)**.
- **NFR**: wire 무손실, 회귀 0, ratchet 334 tighten.

## 대상 2 메서드 + record 설계 (com.swmanager.system.dto.ops)

| 메서드 | 키(현행) | record | 주의 |
|---|---|---|---|
| `cascadeSgg` | admSectC, sggNm, isUnit(**boolean**) | `CascadeSggRow(String admSectC, String sggNm, @JsonProperty("isUnit") boolean isUnit)` | isUnit = `sggNm!=null && sggNm.equals(sidoNm)`(현행 식). **`@JsonProperty("isUnit")` 명시** — record boolean 컴포넌트의 is-접두 처리로 키가 "unit" 으로 깎이는 함정 차단 |
| `cascadeSystems` | cd, nm | `CascadeSystemRow(String cd, String nm)` | SysMst 직매핑 |

- camelCase 키는 컴포넌트명(무어노테이션). isUnit 만 안전상 @JsonProperty.
- `@JsonInclude` 미부착(null 포함). admSectC/sggNm/cd/nm = String, isUnit = primitive boolean(현행도 boolean 식 결과, null 불가).
- `from()` 팩토리에 현행 빌드 식 이식.

## 검증 (골든 = 레거시 HashMap 복제본과 tree 동치)

1. `OpsDocCascadeRowsTest`(신규): 두 record 각각 채운 엔티티 → `from()` 직렬화가 현행 `HashMap` 복제본과 **JsonNode tree 동치**. 특히 **`isUnit` 키 이름·boolean 값(true/false)** 확인(sggNm==sidoNm→true / 다름→false), null 필드(sggNm null) 포함.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 340→334.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·isUnit boolean·tree 동치).

## 롤백
원자 커밋 → `git revert`. record 2개 신규 + 1 컨트롤러 2메서드 국소 치환.

## 커밋(승인 후)
`refactor(api): OpsDocController cascade 응답 List<Map>→record 2종(CascadeSggRow/CascadeSystemRow) + ratchet 340→334 (§6-4)`
