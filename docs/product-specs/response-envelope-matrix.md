# 응답 envelope 호환성 매트릭스 (다음 세션 설계 입력)

- **상태**: v0.1 (분류 자료 — 차기 §6-4 (a)ApiResult 확장 설계의 입력)
- **작성**: 2026-06-20
- **근거**: 2026-06-20 세션 컨트롤러 스코프(InspectReport/OrgUnit/Quotation/OpsKb/OpsDoc/Document). codex 권고(Q3).
- **목적**: 기존 응답 형태를 전수 분류해 "ApiResult 가 무손실 표현 가능한가 / 확장 필요한가 / 변형 보존인가"를 고정. 다음 세션은 이 표만 보고 설계.

---

## 0. 현 ApiResult (커버 범위)

```java
record ApiResult(Boolean success, Object data, Object error)  // @JsonInclude(NON_NULL)
  ok()                 → {success:true}
  ok(data)             → {success:true, data:...}
  fail(code, message)  → {success:false, error:{code,message}}
  failMessage(message) → {success:false, error:"..."}
```

## 1. 관측된 응답 패턴 → ApiResult 커버 여부

| # | 패턴 (JSON) | 출처 예 | ApiResult | 처리 |
|---|---|---|---|---|
| P1 | `{success:true}` | Quotation updatePattern, OrgUnit delete | ✅ `ok()` | **치환 완료/가능** |
| P2 | `{success:true, data:...}` | InspectReport get/list | ✅ `ok(data)` | **치환 완료** |
| P3 | `{success:false, error:"str"}` | Quotation 예외, OpsKb 400 | ✅ `failMessage` | **치환 완료/가능** |
| P4 | `{success:false, error:{code,message}}` | OrgUnit/InspectReport 403 | ✅ `fail` | **치환 완료/가능** |
| P5 | `{success:true, message:"..."}` | Quotation update/delete (405/424) | ❌ message 키 | **확장(a) 또는 보존** |
| P6 | `{success:true, <domainKey>:v}` (count/patternId/deleted/copied/status) | Quotation 다수, OpsKb update(status) | ❌ 임의 키 | **확장(a) 또는 보존** |
| P7 | `{success:false}` (bare, error 없음) | OpsKb 404 (detail/update/approve/reject) | ❌ bare fail 팩토리 없음 | **확장(a): `fail()` 무인자** |
| P8 | dto 직반환 (`toDto(kb)` 등, envelope 아님) | OpsKb detail 200 | ➖ 비대상 | **보존** (성공시 dto 그대로) |
| P9 | List 직반환 | InspectReport snapshots/infra-servers | ➖ 비대상 | **보존** |

## 2. (a) ApiResult 확장 후보 (차기 세션, 골든 선행)

| 확장 | 표현 | 흡수 대상 |
|---|---|---|
| `fail()` 무인자 | `{success:false}` | P7 (OpsKb 404 다수) — 가장 깨끗·다빈도 |
| `ok(String key, Object val, ...)` 또는 `okWith(Map extra)` | `{success:true, <k>:v}` | P5·P6 — 단, "임의 키"를 표준에 넣을지 *설계 판단* 필요 |

> ⚠ 설계 주의(codex): P5/P6의 "임의 부가키"를 표준에 흡수하면 *표준이 다시 자유 Map 화* 될 위험. 두 갈래 중 택1:
> 1. **흡수**: `ok(data)` 의 data 에 `{message/count/...}` 를 담고 *프론트가 `resp.data.x` 로 읽도록 변경* → **프론트 수정 동반(비-무손실)**. 큰 결정.
> 2. **보존**: P5/P6 는 ApiResult 대상에서 제외하고 변형 Map 유지. **무손실 유지, 표준화율은 낮음.**
> → 권고: `fail()` 무인자만 우선 확장(P7 흡수, 무손실), P5/P6 는 보존. 표준화율보다 *무손실·안정* 우선(헌장).

## 3. 컨트롤러별 잔여 (차기 대상, hotspot 순)

| 컨트롤러 | Map | 응답 특징 | 차기 작업 |
|---|---|---|---|
| OpsDocController | ~41 | ~~가드헬퍼 `requireDocEdit/View/EditOrAdmin` 가 `ResponseEntity<Map>` 반환~~ → **(c) 완료**(commit 82a6954, 2026-06-20: 가드헬퍼 3종 `ResponseEntity<ApiResult>` 재설계). requester/feedback 평면 요청본문도 DTO화(opsdoc-request-dto, 373→371). 잔여 ~41=create/update 요청본문(nested section_data jsonb·partners list, 타입화 난도 높음)·응답조립 Map(P6 보존)·search `List<Map>`(P8/P9 보존)·list() 내부 lookup Map(보존) | 잔여는 요청DTO화 가성비·리스크 재평가 필요 |
| DocumentController | ~100 | 거대클래스(1752, ratchet baseline). 점검 분리 후 잔여. envelope+변형 혼재 | 분리(§6-5)와 묶어 |
| OpsKbController | ~18 | P7(bare false) 다수 + P6(status) + P8(dto). `forbidden()/forbiddenAdmin()` 헬퍼 | `fail()` 확장 후 P1/P3/P4/P7 치환, P6/P8 보존 |
| Quotation 잔여 | — | P5/P6 변형(message/count/patternId 등) | 보존 (또는 §2 설계 결정 후) |

## 4. 차기 세션 순서 (권고)

1. `ApiResult.fail()` 무인자 추가 + `ApiResultTest` 골든 (`{success:false}`).
2. OpsKbController: P1/P3/P4/P7 치환(net 박제 선행: 해당 엔드포인트 golden 기록 → 치환 → 매치). P6/P8 보존.
3. Map ratchet tighten.
4. (c) OpsDoc 가드헬퍼 재설계는 별도 — 인벤토리 net + 응답 golden 충분히 깐 뒤.

---

*이 표가 "남은 게 왜 단순 치환이 아닌지"의 근거다. 표준화율을 위해 무손실을 깨지 않는다(헌장 §0).*
