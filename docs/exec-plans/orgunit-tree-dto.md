# 개발계획서 — OrgUnitService.getTree 가변 중첩 트리 Map→재귀 record (orgunit-tree-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4. 이전 `orgunit-node-dto`(평면 노드 → OrgUnitNode record)에서 **보존**했던 getTree 의 가변 중첩 트리를 재귀 record 로 전환. OrgUnitService 의 최대 Map 클러스터(getTree 8 + toDto 2) + OrgUnitController.getTree 반환(1) = **−11**.
- **⚠ 리스크 성격**: 기계적 치환이 아니라 **알고리즘 재작성**(mutable nodeMap+children.add → 불변 재귀 record bottom-up 구성). 와이어 동치는 알고리즘 동등성에 의존 → 골든+codex 정밀 검증.
- **클라이언트**: `/api/org-units/tree`(org-unit 관리 화면 JS) — 키 `unit_id/name/unit_type/parent_id/sort_order/children` 보존 필수. **디자인팀** 비해당. **DB** 무관.

## 현행 동작 (보존 대상)
- 활성(useYn="Y") OrgUnit 전체를 sortOrder 순 조회.
- 각 노드 = toDto 5키({unit_id,name,unit_type,parent_id,sort_order}) + `children:[]`.
- parentId==null → root. parentId!=null & 부모가 활성집합에 있으면 부모 children 에 추가(순서=조회순).
- **핵심 엣지**: parentId!=null 인데 **부모가 활성집합에 없으면(필터됨) 해당 노드는 root 도 children 도 아님 → 트리에서 드롭**.

## 설계 — 재귀 record + faithful 알고리즘
- 신규 `OrgTreeNode`(com.swmanager.system.dto.orgunit): OrgUnitNode 5필드(@JsonProperty snake + @JsonPropertyOrder) + `List<OrgTreeNode> children`(순서 6번째). null 포함(@JsonInclude 미부착, parent_id null).
- 알고리즘 재작성(OrgUnitService.getTree):
  1. all = 활성 조회(sortOrder). `childrenByParent` = all 을 순서대로 그룹핑(LinkedHashMap<Long,List<OrgUnit>>, key=parentId).
  2. roots = all 중 parentId==null (조회순).
  3. `build(u)` 재귀: children = `childrenByParent.getOrDefault(u.unitId, [])` 의 각 자식을 build → OrgTreeNode(5필드 + children).
  4. return roots.map(build).
  - **orphan-drop 동치**: roots 를 parentId==null 로만 정의 → 필터된 부모를 가진 노드는 어떤 root 의 자손도 아니므로 build 미도달 → 드롭(현행과 동일). 순서: childrenByParent·roots 모두 조회순(sortOrder) 보존.
- `toDto` private 헬퍼 제거(getTree 전용이었음). OrgUnitNode.from 은 무관(유지).
- OrgUnitController.getTree 반환타입 `List<Map>`→`List<OrgTreeNode>`(URL·@ResponseBody 무변).

## 목표 (FR/NFR)
- **FR**: getTree(8)+toDto(2)+컨트롤러 반환(1) `Map<String,Object>` 11선언 제거 → 재귀 record 1. **선언 222→211 (−11)**.
- **NFR**: 응답 wire(중첩 트리 키셋·snake·값·null·children 순서·orphan-drop·root 순서) 무손실, 회귀 0, ratchet 211 tighten. URL 무변.

## 검증 (알고리즘 동등성 집중)
1. `OrgTreeNodeTest`(신규): 현행 getTree 로직 복제본(Map) ↔ 새 record 빌더를 **동일 입력셋으로 JsonNode tree 동치**. 경계:
   - 다층 트리(div→dept→team) 중첩·children 순서(sortOrder)
   - **orphan 엣지**: parentId 가 활성집합에 없는 노드 → 결과에서 드롭(양쪽 동일)
   - root 복수·순서, 빈 children([]), parent_id null(root)·snake 키 6종
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 222→211.
3. `./mvnw test` 전체 green(EndpointInventory 불변, 기존 OrgUnit 테스트 회귀 0).
4. codex 검토(**알고리즘 동등성**·orphan-drop·순서·snake 키·null·재귀 record 직렬화).

## 롤백
원자 1 커밋 → `git revert`. record 1 신규 + getTree 재작성 + toDto 제거 + 컨트롤러 반환타입. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): OrgUnitService.getTree 가변 중첩 트리 Map→OrgTreeNode 재귀 record(알고리즘 faithful 재작성, orphan-drop 보존) + ratchet 222→211 (§6-4)`
