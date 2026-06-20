# 개발계획서 — OpsDoc create/update 복잡 요청본문 Map→DTO (opsdoc-createupdate-dto)

- **상태**: v0.1 (— 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 5번째. 평면 요청본문 4컨트롤러(391→371) 후 **복잡 본문** 착수. OpsDoc create/update = nested `section_data`(동적 jsonb)·`partners` list 포함 핵심 문서 CRUD.
- **디자인팀**: skip (UI 키워드 0). **DB**: 변경 없음.
- **선행 discovery(wire 타입 전수 확인)**: doc-fault.html:187·ops-doc-relations.js:118/244 — `engineer_id`=`Number(ev)`(숫자), `requester_id`=서버 숫자 id, `partners`=`[{partner_id:숫자, role_label:문자}]`, `section_data`=중첩 객체. → `instanceof Number` 판정과 DTO `Long` 바인딩 **동치 확인**.

## 목표

- **FR-1**: create/update 요청 Map + applyRelations/savePartners 헬퍼 6줄 → `OpsDocForm`. **`Map<String,Object>` 371→366 (−5)**.
- **FR-2**: wire 무손실 — 본문 키·타입·HTTP 상태·응답 JSON·관계자/협력사 저장 로직 동일.
- **NFR**: 회귀 0, ratchet 366 tighten.

## 대상 6줄 (OpsDocController, 41 중)

| 줄 | 현행 | 후 |
|---|---|---|
| L306 | `create(@RequestBody Map body)` | `create(@RequestBody OpsDocForm form)` |
| L328 | `Map sectionData = (Map) body.get("section_data")` | `form.sectionData()` (지역변수 제거) |
| L346 | `update(@RequestBody Map body)` | `update(@RequestBody OpsDocForm form)` |
| L367 | `Map sectionData = (Map) body.get("section_data")` | `form.sectionData()` |
| L486 | `applyRelations(OpsDocument, Map body)` | `applyRelations(OpsDocument, OpsDocForm form)` |
| L653 | `savePartners(Long, Map body, boolean)` | `savePartners(Long, OpsDocForm form, boolean)` |

> 순감 −5: 컨트롤러 6줄 제거, `OpsDocForm.sectionData` 필드 1줄 신설(동적 jsonb=Map 유지, 보존 성격).

## DTO 설계

```java
// dto/ops/OpsDocForm
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpsDocForm(
    String title,
    @JsonProperty("sys_type") String sysType,
    @JsonProperty("region_code") String regionCode,
    String environment,
    @JsonProperty("support_target_type") String supportTargetType,
    @JsonProperty("engineer_id") Long engineerId,
    @JsonProperty("requester_kind") String requesterKind,
    @JsonProperty("requester_id") Long requesterId,
    @JsonProperty("section_data") Map<String, Object> sectionData,   // 동적 jsonb — Map 유지
    List<PartnerRef> partners) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PartnerRef(@JsonProperty("partner_id") Long partnerId,
                             @JsonProperty("role_label") String roleLabel) {}
}
```

- `environment`/`support_target_type`: fault 본문엔 없으나 support/install/patch 가 전송 → nullable 필드 유지.

## 호출부 치환 (행위 동일)

- **create title**: `form.title() != null ? form.title() : docType.label()` ← 현행 `getOrDefault("title", label)`. 프론트는 title 항상 전송(빈문자 가능)→키 present→값 사용으로 등가(빈문자도 기존처럼 label 로 안 바뀜).
- **update title**: `changes.setTitle(form.title())` ← 현행 `body.get("title")` 직접. 동일.
- 나머지 setter: sysType/regionCode/environment/supportTargetType form 게터로 1:1.
- **applyRelations**: `if (form.engineerId()!=null) userRepository.findById(form.engineerId())...` ← `instanceof Number` 등가. requesterPerson/Contact/Staff null 리셋 후 `if (form.requesterId()!=null)` 분기 동일(PERSON/CONTACT/STAFF).
- **savePartners**: `form.partners()` null/빈 가드 + `PartnerRef.partnerId()!=null` + `(업체,역할)` 중복 dedup(seen) 동일. role `roleLabel()!=null?:""`.
- 응답조립 HashMap(P6 docId/docNo) 그대로. INSPECT 차단·requireDocEdit 가드 그대로.

## 검증 (golden net = DTO 바인딩 + 전수 wire 분석 + 회귀 + codex)

1. `OpsDocFormBindingTest` 확장 또는 신규 `OpsDocCreateFormBindingTest`: 중첩 section_data 맵 보존, partners 리스트(partner_id 숫자/role_label), engineer_id/requester_id 숫자, snake_case, 미지키 무시.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 371→366.
3. `./mvnw test` 전체 green(회귀 0). 4. codex 검토(wire 타입 동치·관계자/협력사 로직·title 폴백).

## 롤백
원자 커밋 → `git revert`. DTO 1개 신규 + 1 컨트롤러 4지점(create/update/applyRelations/savePartners).

## 커밋(승인 후)
`refactor(api): OpsDocController create/update 요청 Map→OpsDocForm + ratchet 371→366 (§6-4)`
