# 개발계획서 — Partner 요청 본문 Map→DTO + 반환타입 위닝 (partner-request-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 후속 3번째 핫스팟. opskb(386)·orgunit(381) 완료 후. PartnerController 13건.
- **디자인팀**: skip (UI 키워드 0). **DB**: 변경 없음.

## OrgUnit/OpsKb 와 다른 점

PartnerController 는 응답도 `ResponseEntity<Map<String,Object>>` 라, 요청 본문만 DTO화하면 **같은 줄의 반환 타입이 남아 줄 감소가 미미**. → OrgUnit 선례처럼 **반환 타입을 `ResponseEntity<?>` 로 위닝**하고 Map.of 본문(P4 forbidden / P6 success+id)은 **그대로 보존**(무손실, 매트릭스 P6 보존 방침). JSON 무변경.

## 목표

- **FR-1**: 요청 본문 Map 3건(create/update/addContact) + 헬퍼 applyPartner + 반환타입 5건 위닝 → **`Map<String,Object>` 381→373 (−8)**.
- **FR-2**: wire 무손실 — 프론트 키·HTTP 상태·응답 JSON 동일.
- **NFR**: 회귀 0, ratchet 373 tighten.

## 대상 8줄 (PartnerController, 총 13 중)

| 줄 | 현행 | 후 |
|---|---|---|
| L40 | `ResponseEntity<Map<String,Object>> forbidden()` | `ResponseEntity<?> forbidden()` (본문 Map.of 보존) |
| L86 | `ResponseEntity<Map> create(@RequestBody Map body)` | `ResponseEntity<?> create(@RequestBody PartnerForm form)` |
| L102 | `ResponseEntity<Map> update(…, @RequestBody Map body)` | `ResponseEntity<?> update(…, PartnerForm form)` |
| L114 | `ResponseEntity<Map> delete(…)` | `ResponseEntity<?> delete(…)` |
| L122 | `applyPartner(Partner, Map body)` | `applyPartner(Partner, PartnerForm form)` |
| L133 | `ResponseEntity<Map> addContact(…)` | `ResponseEntity<?> addContact(…)` |
| L134 | `@RequestBody Map body` (addContact) | `@RequestBody ContactForm form` |
| L156 | `ResponseEntity<Map> deleteContact(…)` | `ResponseEntity<?> deleteContact(…)` |

**보존(5건)**: list() 의 `List<Map>`·`Map m`·`contacts`·`Map cm`(L56/57/60/66/69) = dto 직조립 passthrough.

## DTO 설계 (프론트 키: partner-management.html L85-87, L94)

```java
// dto/ops/PartnerForm  (create/update 본문: name/partner_type/main_tel/biz_no/note)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartnerForm(
    String name,
    @JsonProperty("partner_type") String partnerType,
    @JsonProperty("biz_no") String bizNo,
    @JsonProperty("main_tel") String mainTel,
    String note) {}

// dto/ops/ContactForm  (addContact 본문: name/position/tel, email 은 미전송이나 controller 가 읽음→수신 호환)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactForm(String name, String position, String tel, String email) {}
```

## 호출부 치환 (행위 동일)

- **applyPartner**: `p.setName(form.name())`, `setPartnerType(form.partnerType())`, `setBizNo(form.bizNo())`, `setMainTel(form.mainTel())`, `setNote(form.note())`. 현행 무가드 set 그대로.
- **create / addContact**: name 필수검증 `form.name()` 기준(현행 검증 위치 그대로 보존). **update 는 현행대로 name 검증 없음** — 리팩터 불변식상 검증 추가하지 않음(행위 동일). 응답 Map.of 그대로.
- **addContact**: name 필수검증, `c.setPosition(form.position())`/`setTel(form.tel())`/`setEmail(form.email())`. email 미전송→null (현행 동일).
- **반환타입 위닝**: `ResponseEntity<Map<String,Object>>`→`ResponseEntity<?>`. 런타임 본문(Map.of) 동일 직렬화 → JSON 무변경. forbidden() 도 동일.

## 검증
1. `PartnerFormBindingTest` (신규): create/contact 키 바인딩(snake_case 포함) + 미지키 무시.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 381→373.
3. `./mvnw test` 전체 green. 4. codex 검토.

## 롤백
원자 커밋 → `git revert`. DTO 2개 신규 + 1 컨트롤러 국소.

## 커밋(승인 후)
`refactor(api): PartnerController 요청 Map→DTO + 반환타입 위닝 8줄 + ratchet 381→373 (§6-4)`
