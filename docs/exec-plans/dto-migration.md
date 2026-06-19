# 개발계획서 — Map→DTO 전환 파일럿 (dto-migration)

- **상태**: v2 (codex 블로킹 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-19 (v1) / v2 동일자 (codex ❌: find 의 data=null 보존 → 프론트 null-safe 검증으로 행위동일 확인, 불변식 완화)
- **기획서**: `docs/product-specs/dto-migration.md` (v0.2, codex 승인·사용자 승인)
- **안전망**: 기본 `mvn test` 391 green + JaCoCo 게이트. 골든(JSON 형태) 테스트로 무손실 고정.
- **불변식**: 응답 JSON 구조·**HTTP 상태코드 행위 동일**. (※ 단 1건 예외 — 아래 [find data=null] — 은 프론트 null-safe 검증으로 *행위 동일*. byte-identical 이 아니라 *consumer 행위 동일*이 기준.)

## ⚠ codex 블로킹 반영 — find 의 data=null

`findInspectReport` 미존재 prefill 시 현행 = `{success:true, data:null}`(키 존재). `ApiResult.ok(null)`+`@JsonInclude(NON_NULL)` = `{success:true}`(data 키 생략). **byte 는 다름**.
→ **프론트 검증**: `doc-inspect.html:830` `if (resp.success && resp.data)`, `:1821` `if (!resp.success || !resp.data) return;`. JS 에서 `null` 과 `undefined`(키 생략) **모두 falsy** → 두 호출부 모두 동일 분기. **행위 동일(무손실 아님, 행위 무영향)**. 본 변경 수용. 그 외 성공 분기(get/list/template 등)는 비-null data 라 영향 없음.

---

## Step 0 — 현행 응답 형태 스냅샷 (InspectReportController 7 엔드포인트)

| # | 메서드 | 성공 | 권한거부 | 예외(catch) |
|---|---|---|---|---|
| 1 | saveInspectReport (POST) | 200 `{success:true,data:saved}` | 403 `{success:false,error:{code:"FORBIDDEN",message:"수정 권한이 없습니다"}}` | **200** `{success:false,error:"<msg>"}` |
| 2 | getInspectReport (GET) | 200 `{success:true,data:dto}` | — | 200 `{success:false,error:"<msg>"}` |
| 3 | listInspectReports | 200 `{success:true,data:list}` | — | 200 `{...error:"<msg>"}` |
| 4 | findInspectReport | 200 `{success:true,data:...}` | — | 200 `{...error:"<msg>"}` |
| 5 | getPreviousVisits | 200 `{success:true,data:...}` | — | 200 `{...error:"<msg>"}` |
| 6 | deleteInspectReport (DELETE) | 200 `{success:true}` (data 없음) | 403 `{...error:{code:"FORBIDDEN",message:"관리자만 삭제할 수 있습니다"}}` | 200 `{...error:"<msg>"}` |
| 7 | getInspectTemplate | 200 `{success:true,data:items}` | — | 200 `{...error:"<msg>"}` |

**핵심 함정**: 예외 경로도 마지막 `return ResponseEntity.ok(result)` 라 **상태 200**(500 아님). reset-all 의 권한거부도 동일 패턴이나 reset-all 은 `deleted` 변형이라 **대상 외**(기획서 Out).

## Step 1 — ApiResult 레코드 + 직렬화 단위 테스트

`src/main/java/com/swmanager/system/response/ApiResult.java`:
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult(Boolean success, Object data, Object error) {
    public static ApiResult ok()                 { return new ApiResult(true, null, null); }
    public static ApiResult ok(Object data)      { return new ApiResult(true, data, null); }
    public static ApiResult fail(String code, String message) {
        return new ApiResult(false, null, java.util.Map.of("code", code, "message", message));
    }
    public static ApiResult failMessage(String message) {
        return new ApiResult(false, null, message);
    }
}
```
**단위 테스트** `ApiResultTest` (ObjectMapper 직렬화):
- `ok()` → `{"success":true}` (data·error 키 없음)
- `ok(d)` → `{"success":true,"data":...}` (error 키 없음)
- `fail("FORBIDDEN","x")` → `{"success":false,"error":{"code":"FORBIDDEN","message":"x"}}` (data 키 없음)
- `failMessage("x")` → `{"success":false,"error":"x"}`

## Step 2 — 7 엔드포인트 치환 (InspectReportController)

각 메서드의 Map 조립을 ApiResult 팩토리로 치환. **상태코드 보존**:
- 성공: `return ResponseEntity.ok(ApiResult.ok(<data>));` (6은 `ApiResult.ok()`)
- 403: `return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "<현행 메시지>"));`
- 예외: `return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));` (**200 유지**)
- 기존 `Map<String,Object> result`/`LinkedHashMap`/`result.put(...)` 제거.

치환 안 함: infra-servers, inspect-snapshots, reset-all, pdf/chart/detail/preview (기획서 Out).

## Step 3 — JSON 형태 골든 테스트

`InspectReportControllerDtoShapeTest` (직접호출 + ObjectMapper, DB 불필요한 경로 중심):
- 각 팩토리 분기의 JSON 키 집합·`error` 타입(맵 vs 문자열)이 Step 0 표와 동일.
- 가능 범위에서 컨트롤러 메서드 직접 호출(서비스 mock) → ResponseEntity 본문을 ObjectMapper 로 직렬화해 키 검증. (S1 가드 테스트 패턴 재사용)
- 최소: save 403/예외, delete 403/성공(data 없음), get 성공/예외, **find 미존재(data=null) → ok(null) 직렬화가 `{success:true}`(data 생략)임을 명시 검증** + 프론트 행위동일 근거 주석.

## Step 4 — 검증 + codex + 커밋

- `./mvnw test` 391+ green + JaCoCo 게이트.
- codex 검증(상태코드 보존·error 양형·키 집합).
- 커밋 `refactor(api): InspectReportController 응답 Map→ApiResult (dto-migration 파일럿)` → 푸시.

---

## 검증 매트릭스

| 항목 | Step |
|---|---|
| FR-1 JSON 형태 보존 | Step 1(단위 직렬화) + Step 3(골든) |
| FR-2 NON_NULL(키 생략) | Step 1 |
| 예외=200 상태 보존 | Step 2 + Step 3 |
| 403 error=맵 / 예외 error=문자열 | Step 1·3 |
| NFR-1 회귀 0 | Step 4 (391 green) |
| NFR-2 골든 | Step 3 |

## 롤백
- 원자 커밋 → `git revert`. ApiResult 추가 + 1 컨트롤러 치환이라 영향 국소.

## 후속 (본 스프린트 Out)
- DocumentController(90)·OpsDocController(66) 등 단계 이관은 파일럿 안정화 후 별도 커밋/스프린트.
