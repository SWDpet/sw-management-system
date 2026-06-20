# 개발계획서 — OpsKb 요청 본문 Map→DTO (opskb-request-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 후속. `MapDebtRatchetTest` 실측=baseline=391 (조일 여유 0). 핫스팟 1개 실감축으로 ratchet tighten.
- **선행 분석**: `dto-migration`(응답 envelope, 완료)과 별개 슬라이스 = **요청 본문(request body)·헬퍼 파라미터**의 비타입 `Map<String,Object>` → 타입 DTO.
- **디자인팀**: skip (UI 키워드 0건, 백엔드 전용). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR-1**: `OpsKbController` 의 비타입 요청 Map 5건을 타입 DTO 로 치환 — **`Map<String,Object>` 선언 391→386**.
- **FR-2**: 요청 **wire 호환 무손실** — 프론트가 보내는 JSON 키·HTTP 상태·검증 메시지·로그 동작 동일.
- **NFR-1**: 회귀 0 (`mvn test` green). **NFR-2**: ratchet baseline 386 으로 tighten + 커밋.

## 대상 5건 (OpsKbController)

| 줄 | 현행 | 후 |
|---|---|---|
| L160 | `create(@RequestBody Map<String,Object> body, …)` | `create(@RequestBody OpsKbForm form, …)` |
| L196 | `update(…, @RequestBody Map<String,Object> body, …)` | `update(…, @RequestBody OpsKbForm form, …)` |
| L240 | `reject(…, @RequestBody Map<String,Object> body, …)` | `reject(…, @RequestBody RejectForm form, …)` |
| L270 | `validate(Map<String,Object> b)` | `validate(OpsKbForm f)` |
| L282 | `apply(OpsKb kb, Map<String,Object> b)` | `apply(OpsKb kb, OpsKbForm f)` |

**보존(대상 외, 8건)**: sysOptions(73/74/76)·search(124/129)·create 응답 ok HashMap(186, P6 status)·toDto(299/300, P8 dto). 매트릭스(`response-envelope-matrix.md`) 보존 방침 유지.

## DTO 설계

프론트 본문 키 근거: `kb-form.html:76` (sys_type/gubun/symptom/cause/action/prevention/keywords), `kb-list.html:124` (reject `reason`). `category` 는 폼 미전송이나 `apply()` 가 수신 시 반영 → DTO 필드 유지(수신 호환).

```java
// com.swmanager.system.dto.ops.OpsKbForm  (record)
public record OpsKbForm(
    @JsonProperty("sys_type") String sysType,   // ← snake_case 키 (전역 naming 미설정)
    String gubun, String symptom, String cause, String action,
    String keywords, String prevention, String category) {}

// com.swmanager.system.dto.ops.RejectForm
public record RejectForm(String reason) {}
```

- 단일어 키(gubun/symptom/…/reason)는 컴포넌트명=JSON키라 무어노테이션 바인딩. **`sys_type` 만 `@JsonProperty`**.
- `@RequestBody` 필수(현행 Map 도 required=true) → 본문 누락 시 400 동일.

## 호출부 치환 규칙 (행위 동일)

- `validate`: `blank((String)b.get("sys_type"))` → `blank(f.sysType())` 등. 분기·메시지 그대로.
- `apply`: `kb.setSysType((String)b.get("sys_type"))` → `kb.setSysType(f.sysType())` 등. category 는 `blank(f.category())!=null` 일 때만 set (현행 보존).
- create 로그(L162-165): `body.get("gubun")`→`form.gubun()`, `(body.get("symptom")+"").trim().length()`→`(form.symptom()+"").trim().length()` (null→"null" 양상 동일).
- reject: `blank((String)body.get("reason"))` → `blank(form.reason())`. `body!=null` 가드는 @RequestBody 필수라 불필요(제거 가능, 행위 동일).

## 검증

1. `OpsKbFormBindingTest` (신규, ObjectMapper): `{"sys_type":"X","gubun":"장애",…}` → `OpsKbForm.sysType()=="X"` 등 snake_case 바인딩 + `{"reason":"r"}`→`RejectForm`.
2. `GOLDEN_RECORD=1 ./mvnw test -Dtest=MapDebtRatchetTest` → baseline 391→386 tighten.
3. `./mvnw test` 전체 green (회귀 0).
4. codex 검토(요청 키 보존·검증분기·상태코드).

## 롤백
원자 커밋 → `git revert`. DTO 2개 신규 + 1 컨트롤러 국소 치환.

## 커밋(승인 후)
`refactor(api): OpsKbController 요청 Map→OpsKbForm/RejectForm 5건 + ratchet 391→386 (§6-4)`
