# 개발계획서 — QuotationController 조회응답 List<Map>→record (quotation-list-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 7번째. 직전 `opskb-todto-record`(366→362)와 동일 패턴 — **조회 API 의 `List<Map<String,Object>>` 응답 본체를 타입 record 로 치환**. QuotationController 16 Map 중 무손실 가능분은 응답조립 `List<Map>` 2건(searchQuotations·getSwClients). 나머지 envelope 반환타입(P5/P6)·요청본문은 보존(매트릭스).
- **선행 분석(consumer wire)**: `quotation-form.html` —
  - `/api/quotation/search` → JS 1506-1511 가 `q.quoteNumber/projectName/grandTotal/quoteId` + **`q.quoteDate.substring(5)`**(String 필수) 사용.
  - `/api/quotation/sw-projects/clients` → JS 1352-1369 가 `c.client/distNm/year/projId`·`item.swAmt` 사용(`projNm` 미사용이나 보존).
  - 전역 jackson non_null 미설정 → null 포함 기본. camelCase 키(전역 naming 미설정이나 컴포넌트명=키라 무어노테이션).
- **디자인팀**: skip (UI 키워드 0). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR-1**: `searchQuotations` 의 `List<Map<String,Object>>`(반환+지역변수) → `List<QuotationSearchRow>`. **−2**.
- **FR-2**: `getSwClients` 의 `List<Map<String,Object>>`(반환+지역변수) → `List<SwClientRow>`. **−2**.
- **FR-3**: wire 무손실 — 키셋·값·타입·null 포함·키순서·`quoteDate` String 폴백("") 동일.
- **NFR**: 회귀 0, **ratchet 362→358** tighten.

## 대상 4줄 (QuotationController)

| 줄 | 현행 | 후 |
|---|---|---|
| L319 | `public List<Map<String,Object>> searchQuotations(…)` | `public List<QuotationSearchRow> searchQuotations(…)` |
| L332 | `Map<String,Object> m = new LinkedHashMap<>(); …` | `QuotationSearchRow.from(q)` (지역변수 제거) |
| L538 | `public List<Map<String,Object>> getSwClients(…)` | `public List<SwClientRow> getSwClients(…)` |
| L543 | `Map<String,Object> m = new LinkedHashMap<>(); …` | `SwClientRow.from(p)` (지역변수 제거) |

**보존(대상 외)**: envelope 반환타입 `ResponseEntity<Map<String,Object>>` 11건(P5/P6 — `message/count/patternId/copied` 임의키, 매트릭스 보존. 반환타입만 `?`로 바꾸는 건 ratchet 게이밍이라 금지) · `getStats`(L689, service 위임 → 별도 후속) · `copyPatterns` 요청본문(L502, 반환타입 Map 과 동일 줄이라 순감 0 → 별도).

## DTO 설계

```java
// com.swmanager.system.quotation.dto.QuotationSearchRow  (record) — @JsonInclude 미부착(null 포함)
public record QuotationSearchRow(
        Long quoteId, String quoteNumber, String quoteDate, String category,
        String projectName, String recipient, Long grandTotal, String createdBy) {
    public static QuotationSearchRow from(QuotationDTO q) {
        return new QuotationSearchRow(
            q.getQuoteId(), q.getQuoteNumber(),
            q.getQuoteDate() != null ? q.getQuoteDate().toString() : "",   // 현행 폴백 보존(null 아님)
            q.getCategory(), q.getProjectName(), q.getRecipient(),
            q.getGrandTotal(), q.getCreatedBy());
    }
}

// com.swmanager.system.quotation.dto.SwClientRow  (record)
public record SwClientRow(
        Long projId, String client, Long swAmt, String projNm, Integer year, String distNm) {
    public static SwClientRow from(SwProject p) {
        return new SwClientRow(p.getProjId(), p.getClient(), p.getSwAmt(),
            p.getProjNm(), p.getYear(), p.getDistNm());
    }
}
```

- 컴포넌트 선언순 = 현행 Map put 순. **@JsonProperty 없음**(camelCase=컴포넌트명) → Jackson 재정렬 없음 → @JsonPropertyOrder 불필요(골든 테스트로 키순서 확인).
- 타입은 소스 게터 1:1: quoteId/grandTotal/projId/swAmt=Long, year=Integer, quoteDate=String(toString 결과). null 포함은 OpsKbDto 와 동일 근거(전역 non_null 미설정).
- Javadoc 에 `Map<String,Object>` 리터럴 미기재(ratchet 주석 카운트 함정 회피 — opskb-todto-record 학습).

## 검증

1. `QuotationSearchRowTest`·`SwClientRowTest` (또는 통합 1파일): 채운 QuotationDTO/SwProject → `from()` 직렬화 JSON 이 현행 LinkedHashMap 복제본과 **tree·string 동치**(키셋·값·키순서·null 포함). `quoteDate` null→"" 폴백, 채워진 경우 `yyyy-MM-dd` 문자열.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 362→358.
3. `./mvnw test` 전체 green(회귀 0). DB 포함(RUN_DB_TESTS=true)은 무관(표현계층, 쿼리 무변경)이나 최종 1회 확인.
4. codex 검토(키·타입·quoteDate 폴백·보존 분리).

## 롤백
원자 커밋 → `git revert`. record 2개 신규 + 1 컨트롤러 2지점.

## 커밋(승인 후)
`refactor(api): QuotationController 조회응답 List<Map>→record 2건(QuotationSearchRow/SwClientRow) + ratchet 362→358 (§6-4)`
