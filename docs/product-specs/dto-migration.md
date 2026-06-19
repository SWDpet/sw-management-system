# 기획서 — Map→DTO 전환 (dto-migration, 코드품질 A 마무리)

- **상태**: v0.2 (codex 블로킹 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-19 (v0.1) / v0.2 동일자 (codex ❌→반영: 예외 error=문자열·403 error=맵 양형 보존, snapshots/infra-servers List 제외, 기존 ApiResponse 형태 상이)
- **스프린트명**: `dto-migration`
- **요청자**: 박욱진 — "전 항목 A" 로드맵, S4 후속(코드품질/아키텍처 B→A)
- **선행/안전망**: S4 완료, 기본 `mvn test` 391 green + JaCoCo 게이트.
- **성격**: 동작 보존 리팩토링. **응답 JSON 형태 100% 동일**이 불변식.

---

## 0. 한 줄 요약

코드품질 C의 핵심 부채인 **`Map<String,Object>` 동적 조립**(JSON 응답 `{success, data|error}` 반복)을 타입 안전한 **`ApiResult` 레코드**로 바꾼다. **위험을 줄이기 위해 전면 전환이 아니라**, 방금 분리한 `InspectReportController`(S4-a)를 **파일럿**으로 먼저 적용하고, JSON 형태 골든으로 고정한 뒤, 나머지 컨트롤러는 후속 단계로 단계 이관한다.

## 1. 배경 / 목적

- codex×Claude 평가: 코드품질 C 의 주원인 = `Map<String,Object>` 수백 곳(DocumentController 90, OpsDoc 66 …) + `@SuppressWarnings("unchecked")` 다수. 컴파일 타임 타입 안전 0, 오타·키 불일치 런타임 위험.
- 응답의 90%가 동일 패턴: 성공 `{ "success": true, "data": ... }` / 실패 `{ "success": false, "error": { "code", "message" } }`. → **단일 타입으로 표준화**하면 타입 안전 + 일관성 + 보일러플레이트 감소.

## 2. 범위 (In / Out)

**In (파일럿 — 이번 스프린트, codex 반영 축소)**
- 신규 `ApiResult` (record, `@JsonInclude(NON_NULL)`): `success`(Boolean), `data`(Object), `error`(**Object** — 맵/문자열 양형 보존). 정적 팩토리:
  - `ok()` → `{success:true}`, `ok(data)` → `{success:true, data}`
  - `fail(code, message)` → `{success:false, error:{code,message}}` (현행 403 형태)
  - `failMessage(msg)` → `{success:false, error:"msg"}` (현행 일반 예외 형태 — `result.put("error", e.getMessage())`)
- 치환 대상 = `InspectReportController` 의 **표준 envelope 엔드포인트만**: `save/get/list/find/previous-visits/delete/template`. 각 엔드포인트는 성공=`ok(data)`, 403=`fail("FORBIDDEN",..)`, 예외=`failMessage(e.getMessage())` 로 **현행 형태 그대로** 매핑.
- **제외(형태 비표준)**: `infra-servers`·`inspect-snapshots`(List 직반환), `reset-all`(`{success, deleted}` 변형). PDF/chart(byte[])·detail/preview(String view). → 본 파일럿 대상 아님.
- **기존 `ApiResponse<T>` 재사용 안 함**: 그 형태는 `{success,data,message,errorCode}` 로 현행 InspectReport 형태(`error` 중첩객체/문자열)와 달라 치환 시 JSON 이 바뀜. → 신규 `ApiResult` 로 현행 충실 보존.
- **JSON 형태 골든 테스트**(NFR-2): 치환 전/후 응답 **키 집합·error 타입(맵/문자열)** 동일 검증(키 순서 비교 금지).

**Out (후속 단계 — 별도 커밋/스프린트)**
- DocumentController(90)·OpsDocController(66)·기타 컨트롤러: 파일럿 안정화 확인 후 단계 이관.
- `@RequestBody Map` → 요청 DTO 전환: 별도(요청 바인딩은 위험 프로파일 다름).
- 내부 데이터 전달용 Map(엑셀/HWPX 빌더 등): 대상 외(외부 계약 아님).

## 3. 개발자 스토리

> 유지보수자로서, 응답을 만들 때 `result.put("success", ...)` 오타 위험 없이 `ApiResult.ok(data)` 로 타입 안전하게 쓰고, 모든 API 가 동일 응답 규약을 따르길 원한다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 (불변식) | 치환된 엔드포인트의 **응답 JSON 키·구조·값·HTTP 상태코드 100% 동일**. 프론트 무수정. |
| FR-2 | `ApiResult` 는 `@JsonInclude(JsonInclude.Include.NON_NULL)` — 성공 시 `error` 키 미출력, 실패 시 `data` 키 미출력(기존 Map 이 키를 아예 안 넣던 동작과 동일). |
| FR-3 (형태 주의) | 기존이 단순 `{success,data}`/`{success,error}` 가 아닌 변형(예: reset-all 의 `deleted`, 또는 List 직반환)은 **형태를 그대로 보존** — 무리하게 ApiResult 로 강제하지 않음(해당 건은 유지하거나 data 에 담되 키 동일성 검증). |
| FR-4 | 권한 403 응답 `{success:false, error:{code:"FORBIDDEN", message}}` 도 `ApiResult.fail("FORBIDDEN", msg)` 로 동일 출력. |

## 5. 설계

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult(Boolean success, Object data, Object error) {
    public static ApiResult ok()                 { return new ApiResult(true, null, null); }
    public static ApiResult ok(Object data)      { return new ApiResult(true, data, null); }
    // 403 등 코드형 — 현행 {success:false, error:{code,message}}
    public static ApiResult fail(String code, String message) {
        return new ApiResult(false, null, Map.of("code", code, "message", message));
    }
    // 일반 예외 — 현행 {success:false, error:"<message>"} (error 가 문자열)
    public static ApiResult failMessage(String message) {
        return new ApiResult(false, null, message);
    }
}
```
- 위치: `com.swmanager.system.response.ApiResult` (기존 `response/` 패키지, `ApiResponse` 와 별개 신설).
- 컨트롤러 매핑 예(save): 성공 `ResponseEntity.ok(ApiResult.ok(saved))`, 403 `ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN","수정 권한이 없습니다"))`, 예외 `ApiResult.failMessage(e.getMessage())`.
- `error` 가 403=맵 / 예외=문자열인 **현행 양형을 그대로 보존**.

## 6. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|---|---|
| NFR-1 | `./mvnw test` 391+ green 유지(회귀 0). |
| NFR-2 (골든) | InspectReportController 주요 엔드포인트 응답을 **MockMvc 또는 직접호출 후 Jackson 직렬화**해 JSON 키 집합·값이 치환 전과 동일함을 테스트로 고정. |
| NFR-3 | JaCoCo 게이트 유지. |
| NFR-4 | `InspectReportController` 내 `Map<String,Object>` 응답조립·`result.put` 보일러플레이트 유의 감소. |

## 7. 리스크 / 함정

| ID | 리스크 | 완화 |
|---|---|---|
| R-1 | record + Jackson 이 `error:null`/`data:null` 을 출력 → JSON 형태 변동 | `@JsonInclude(NON_NULL)` 필수(FR-2). 골든 테스트로 키 집합 검증(NFR-2). |
| R-2 | 변형 응답(reset-all `deleted`, infra-servers List)을 무리하게 표준화 → 형태 변동 | FR-3: 변형은 보존. 표준 envelope 인 것만 치환. |
| R-3 | 파일럿 성공에 취해 전면 확산 → 대규모 회귀 | 본 스프린트는 InspectReportController 한정. 나머지 Out. |
| R-4 | `success` 를 boolean vs Boolean | Boolean(객체) 로 항상 비-null → NON_NULL 영향 없음. |

## 8. 단계 (개발계획서에서 상세화)

1. Step 0: InspectReportController 의 현행 응답 형태 인벤토리(엔드포인트별 키 집합) 스냅샷.
2. Step 1: `ApiResult` 레코드 추가 + 단위 직렬화 테스트(ok/ok(data)/fail JSON 확인).
3. Step 2: InspectReportController 표준 envelope 엔드포인트 치환(FR-3 변형 보존).
4. Step 3: JSON 형태 골든 테스트 + `./mvnw test` green.
5. Step 4: codex 검증 → 커밋 → 푸시.

## 9. 완료 기준 (DoD)

- `ApiResult` 도입 + InspectReportController 표준 envelope 치환, **응답 JSON 형태 불변**(골든 통과).
- 391+ green + JaCoCo 게이트 통과 + codex 승인.
- 나머지 컨트롤러 이관 계획은 후속(본 스프린트 Out 명시).

---

### codex 검토 라인
> 기획서(본 문서) → codex → 사용자 승인 → 개발계획 → codex → 승인 → 구현 → codex 검증 → 완료.
