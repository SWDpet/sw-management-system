# 개발계획서 — OrgUnit 노드 평면 응답 List<Map>/Map→record (orgunit-node-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 14번째. 직전 `orgunit-members-dto` 후속. OrgUnitService 의 공유 헬퍼 `toDto`(5필드 노드)를 쓰는 **평면 소비처**(getChildren·getRoots·create·update + 컨트롤러 create var)를 record `OrgUnitNode` 로 치환.
- **⚠ getTree 는 대상 외(의도적 보존)**: `getTree` 는 **가변 중첩 트리**(nodeMap 에 노드 적재 후 `node.put("children",…)` 로 부모-자식 참조 연결)라 immutable record 로 바꾸려면 재귀 빌드로 알고리즘 전면 재작성(고위험). 본 sprint 는 안전한 평면 슬라이스만. `getTree` + `toDto`(헬퍼, getTree 전용으로 잔존)는 별도 후속.
- **무손실 핵심**: `toDto` 는 **`LinkedHashMap`**(키순서 결정적) → tree + **string 동치(키순서)** 검증. 전역 jackson non_null 미설정 → null 포함 기본. 소비자(org-unit-management.html JS)는 `unit_id/name/unit_type/parent_id/sort_order` 키로 접근.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: 평면 소비처의 `Map<String,Object>` 선언 8줄 → `OrgUnitNode`. **baseline 315 → 307 (−8)**.
  - service: getChildren(반환·result=2), getRoots(반환=1), create(반환=1), update(반환=1) = 5줄
  - controller: getRoots(반환=1)·getChildren(반환=1)·create 의 `Map<String,Object> created`(1) = 3줄
    (service getRoots/getChildren 반환타입이 `List<OrgUnitNode>` 가 되면 이를 위임하는 컨트롤러 반환타입도 함께 치환 필수 — `List<OrgUnitNode>` ↛ `List<Map<String,Object>>`. **컨트롤러 getTree 는 service.getTree 가 Map 잔존이라 미변경**)
- **NFR**: wire 무손실(키셋·값·타입·키순서·null 포함), 회귀 0, ratchet 307 tighten.

## record 설계 (com.swmanager.system.dto.orgunit)

```java
@JsonPropertyOrder({"unit_id", "name", "unit_type", "parent_id", "sort_order"})  // toDto put 순 고정
public record OrgUnitNode(
        @JsonProperty("unit_id") Long unitId,
        String name,
        @JsonProperty("unit_type") String unitType,
        @JsonProperty("parent_id") Long parentId,
        @JsonProperty("sort_order") Integer sortOrder) {
    public static OrgUnitNode from(OrgUnit u) {
        return new OrgUnitNode(
            u.getUnitId(), u.getName(), u.getUnitType(),
            u.getParent() != null ? u.getParent().getUnitId() : null,
            u.getSortOrder());
    }
}
```

- snake_case 키(unit_id/unit_type/parent_id/sort_order) `@JsonProperty`. `name` camelCase. **@JsonProperty 부착 컴포넌트가 앞으로 재정렬되는 Jackson 기본동작을 `@JsonPropertyOrder` 로 눌러** 현행 LinkedHashMap 순서(unit_id,name,unit_type,parent_id,sort_order) 보존.
- 타입: unitId/parentId=Long, sortOrder=Integer, name/unitType=String. `@JsonInclude` 미부착(null 포함, parent_id null 보존).
- 치환: getChildren `result.add(OrgUnitNode.from(u))`·반환 `List<OrgUnitNode>`; getRoots 반환 `List<OrgUnitNode>`(getChildren 위임); create/update `return OrgUnitNode.from(saved)`·반환 `OrgUnitNode`; **컨트롤러 getRoots·getChildren 반환타입 `List<OrgUnitNode>`**, create `OrgUnitNode created = …`.
- **`toDto`(private)는 getTree 전용으로 잔존**(getTree 의 `toDto(u)` 후 children put 보존). 5필드 매핑이 OrgUnitNode.from 과 중복되나 getTree 가변 트리 보존 위해 의도적.

## 검증 (골든 = 레거시 LinkedHashMap 복제본과 tree·string 동치)

1. `OrgUnitNodeTest`(신규): 채운 OrgUnit(부모 有/無) → `from()` 직렬화가 현행 `toDto` 복제 LinkedHashMap 과 **tree·string 동치**(키순서). snake_case 키, parent_id null(부모 없음)·값(부모 있음), null 필드 포함, 키셋=5.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 315→307.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회. (★getTree 응답 회귀 없음 = 통합/JS 영향 없음 확인)
4. codex 검토(키·@JsonPropertyOrder·parent_id null·getTree 미변경·create/update 흐름).

## 롤백
원자 커밋 → `git revert`. record 1개 신규 + service 4메서드 + controller 1지점 국소 치환.

## 커밋(승인 후)
`refactor(api): OrgUnit 노드 평면 응답 Map→OrgUnitNode record(getTree 보존) + ratchet 315→307 (§6-4)`
