# 개발계획서 — OpsDoc 깨끗한 요청본문 Map→DTO (opsdoc-request-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 4번째. opskb(386)·orgunit(381)·partner(373) 후. **매트릭스 (c) 가드헬퍼 재설계는 이미 완료**(commit 82a6954, 어제) — 매트릭스 §3 정정함. 잔여 중 **저위험 평면 요청본문 2건만** 이번 범위.
- **범위 외(이번 제외)**: create/update 복잡본문(nested section_data jsonb·partners list, 고위험), 응답조립 Map(P6 보존), search `List<Map>`(P8/P9 보존), list() 내부 lookup Map.

## 목표

- **FR-1**: `requesterCreate`·`kbFeedback` 비타입 요청 Map 2건 → DTO. **`Map<String,Object>` 373→371**.
- **FR-2**: wire 무손실. **NFR**: 회귀 0, ratchet 371 tighten.

## 대상 2건 (OpsDocController, 총 41 중)

| 줄 | 현행 | 후 |
|---|---|---|
| L548 | `requesterCreate(@RequestBody Map body, …)` | `requesterCreate(@RequestBody RequesterForm form, …)` |
| L604 | `kbFeedback(@RequestBody Map body, …)` | `kbFeedback(@RequestBody FeedbackForm form, …)` |

## DTO (프론트 키: ops-doc-relations.js)

```java
// dto/ops/RequesterForm  (saveRequester L87: name/org/dept/pos/tel, city 는 미전송이나 controller 가 cityNm 반영)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RequesterForm(String name, String org, String dept, String pos, String tel, String city) {}

// dto/ops/FeedbackForm  (opsKbFeedback L197: kb_id/doc_id/fb_action, doc_id 는 명시 null 가능)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedbackForm(@JsonProperty("kb_id") Long kbId, @JsonProperty("doc_id") Long docId,
                           @JsonProperty("fb_action") String fbAction) {}
```

## 호출부 치환 (행위 동일)

- **requesterCreate**: name 필수검증·400 본문 동일. `p.setOrgNm(form.org())`/dept/pos/tel/city(form.city(), 미전송→null 현행 동일). 응답 HashMap(P6) 그대로.
- **kbFeedback**: `if (form.kbId()==null)` ← 현행 `!(kbId instanceof Number)` 등가(프론트는 항상 숫자 전송, 누락/null→400 동일). `if (form.docId()!=null) fb.setDocId(...)` ← 명시 null 가능 보존. `fb_action` 동일 삼항. 응답 Map.of 그대로.

## 검증
1. `OpsDocFormBindingTest` (신규): requester 키 + feedback snake_case/숫자 바인딩 + doc_id null + 미지키 무시.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 373→371.
3. `./mvnw test` 전체 green. 4. codex 검토.

## 롤백
원자 커밋 → `git revert`. DTO 2개 신규 + 1 컨트롤러 국소(2 메서드).

## 커밋(승인 후)
`refactor(api): OpsDocController requester/feedback 요청 Map→DTO 2건 + ratchet 373→371 (§6-4)`
