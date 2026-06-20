# 개발계획서 — OrgUnit 요청 본문 Map→DTO (orgunit-request-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 후속, `opskb-request-dto`(386 tighten 완료, commit 550edb2) 다음 핫스팟. 동일 패턴(비타입 `@RequestBody Map` → 타입 DTO).
- **디자인팀**: skip (UI 키워드 0). **DB**: 변경 없음.

## 목표

- **FR-1**: `OrgUnitController` 요청 본문 Map 5건 → DTO 치환. **`Map<String,Object>` 386→381**.
- **FR-2**: wire 무손실 — 프론트(org-unit-management.html) 전송 키·HTTP 상태·서비스 호출 인자 동일.
- **NFR-1**: 회귀 0. **NFR-2**: ratchet 381 tighten + 커밋.

## 대상 5건 (OrgUnitController, 총 11 중)

| 줄 | 현행 | 후 |
|---|---|---|
| L69 | `create(@RequestBody Map body)` | `create(@RequestBody OrgUnitForm form)` |
| L84 | `update(@PathVariable Long, @RequestBody Map body)` | `update(…, OrgUnitForm form)` |
| L112 | `createStaff(@RequestBody Map body)` | `createStaff(@RequestBody StaffForm form)` |
| L125 | `updateStaff(@PathVariable Long, @RequestBody Map body)` | `updateStaff(…, StaffForm form)` |
| L144 | `applyStaff(Staff, Map body)` | `applyStaff(Staff, StaffForm form)` |

**보존(6건)**: getTree/Roots/Children/Members 서비스 반환 `List<Map>`(L37/43/49/56), create 서비스결과 `created`(L75), createStaff 응답 ok(L117, P6 staff_id).

## DTO 설계 (프론트 키 근거: org-unit-management.html)

```java
// dto/orgunit/OrgUnitForm  (L313 create: parent_id/unit_type/name/sort_order, L280 update: name/sort_order(/use_yn))
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgUnitForm(
    @JsonProperty("parent_id") Long parentId,
    @JsonProperty("unit_type") String unitType,
    String name,
    @JsonProperty("sort_order") Integer sortOrder,
    @JsonProperty("use_yn") String useYn) {}

// dto/orgunit/StaffForm  (L250 create: name/position/org_unit_id/active, L256 update: name/position/active)
@JsonIgnoreProperties(ignoreUnknown = true)
public record StaffForm(
    String name,
    String position,
    @JsonProperty("org_unit_id") Long orgUnitId,
    Boolean active,
    String tel) {}
```

- JSON 숫자 → `Long parent_id`/`Integer sort_order`/`Long org_unit_id` 직접 바인딩 → 기존 `((Number)…).longValue()` 캐스팅 제거(단순화, 동일 결과).

## 호출부 치환 (행위 동일)

- **create**: `parentId = form.parentId()`, `unitType=form.unitType()`, `name=form.name()`, `sortOrder = form.sortOrder()!=null ? form.sortOrder() : 0`. 서비스 호출 인자 동일.
- **update**: `name=form.name()`, `sortOrder=form.sortOrder()`(null 가능, 동일), `useYn=form.useYn()`.
- **applyStaff**:
  - `if (form.name()!=null) s.setName(form.name())`
  - `s.setPosition(form.position())`
  - `if (form.orgUnitId()!=null) s.setOrgUnitId(form.orgUnitId())` ← **`containsKey`→`!=null` 등가**: 프론트는 생성 시 값 전송, 수정 시 키 생략(명시적 null 미전송). 생략→null→skip→기존 유닛 보존(현행 동일).
  - `if (form.active()!=null) s.setActive(form.active())` (`Boolean.TRUE.equals` == 값 자체)
  - `s.setTel(form.tel())` ← 수정 시 tel 미전송→null 덮어쓰기. **현행 동작 그대로 보존**(기존 동작, 본 스프린트는 변경 안 함).

## 검증
1. `OrgUnitFormBindingTest` (신규): create/update/staff JSON 키 바인딩(snake_case·숫자형) + 미지키 무시.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 386→381.
3. `./mvnw test` 전체 green. 4. codex 검토.

## 롤백
원자 커밋 → `git revert`. DTO 2개 신규 + 1 컨트롤러 국소.

## 커밋(승인 후)
`refactor(api): OrgUnitController 요청 Map→OrgUnitForm/StaffForm 5건 + ratchet 386→381 (§6-4)`
