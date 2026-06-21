# 개발계획서 — OpsKb 조회 응답 toDto Map→record (opskb-todto-record)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 6번째. 직전 `opskb-request-dto`(391→386)가 **요청 본문**을 DTO화하며 `toDto`(P8 dto)는 "보존"으로 남겼다. 본 스프린트는 그 `toDto` 응답 dto가 **JS만 JSON 키로 소비**(Thymeleaf SpEL 무관)이며 전역 Jackson null-포함 기본이라 **record 치환이 바이트 무손실**임을 근거로, 매트릭스 §3 OpsKb "P8 보존" 항을 *무손실 한정으로* 타입화한다.
- **선행 분석(envelope 매트릭스 재검증)**: 매트릭스 §4가 권고한 envelope 치환(P1/P3/P4/P7)은 **이미 완료** — `ApiResult.fail()` 무인자 존재(`ApiResult.java:37`), `forbidden()`/`forbiddenAdmin()`·detail 404·approve/reject/delete 전부 `ApiResult` 사용. 잔여 8 Map 중 **무손실 타입화 가능분 = `toDto` 경로 4줄**.
- **디자인팀**: skip (UI 키워드 0건, 백엔드 전용). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR-1**: `toDto()` 의 `Map<String,Object>` + 이를 흘리는 `search()` 반환/지역변수를 타입 record `OpsKbDto` 로 치환 — **`Map<String,Object>` 선언 366→362 (−4)**.
- **FR-2**: 응답 **wire 무손실** — JSON 키 17개·값·타입·null 포함 동작·`action` 폴백 로직 동일. HTTP 상태 불변.
- **NFR-1**: 회귀 0 (`mvnw test` green). **NFR-2**: ratchet baseline 362 tighten + 커밋.

## 무손실 근거 (선행 wire 확인)

| 항목 | 확인 |
|---|---|
| 소비자 | `kb-list.html` JS(`k.kb_id`/`kb_code`/`sys_type`/`reject_reason`/`case_count`/`gubun`/`symptom`/`cause`/`action`/`summary`/`status`) + 추천(doc-fault/support/patch/install) — **전부 JSON 키 접근**. Thymeleaf SpEL `toDto` 미사용 |
| null 포함 | 전역 `spring.jackson.default-property-inclusion` **미설정** → 기본=null 포함. 현행 LinkedHashMap 도 17키 항상 put(값 null 포함). plain record(@JsonInclude 미부착) → 17 component 전부 직렬화 = **동치** |
| 키 순서 | JS 는 키 접근(순서 무관). record 컴포넌트 선언순=현행 put 순으로 맞춰 시각 동치까지 보존 |
| naming | 전역 snake naming 미설정 → 다어절 키만 `@JsonProperty` 명시(`kb_id`/`kb_code`/`sys_type`/`case_count`/`reject_reason`/`reviewed_by`/`created_by`) |
| action 폴백 | 현행 `action = (action 비어있지 않으면) action : causeDesc`. `from()` 팩토리에 동일 이식 |

## 대상 4줄 (OpsKbController)

| 줄 | 현행 | 후 |
|---|---|---|
| L126 | `public List<Map<String,Object>> search(…)` | `public List<OpsKbDto> search(…)` |
| L131 | `List<Map<String,Object>> out = new ArrayList<>();` | `List<OpsKbDto> out = new ArrayList<>();` |
| L301 | `private Map<String,Object> toDto(OpsKb k) {` | `private OpsKbDto toDto(OpsKb k) {` |
| L302 | `Map<String,Object> m = new LinkedHashMap<>(); … return m;` | `return OpsKbDto.from(k);` (지역변수 제거) |

- `detail()`(L156) `ResponseEntity.ok(toDto(kb))` — 반환타입 `?` 통과, 선언 없음 → count 무영향, 행위 동일.

**보존(대상 외, 잔여 4 Map)**: `sysOptions`(L75/76/78, Thymeleaf `${s.cd/s.nm}` SpEL record-accessor 리스크 → 별도 검증 후 후속) · `create` 응답 ok HashMap(L188, P6) · `update` Map.of(L221, P6). 매트릭스 보존 방침 유지.

## DTO 설계

```java
// com.swmanager.system.dto.ops.OpsKbDto  (record)  — @JsonInclude 미부착(null 포함 보존)
public record OpsKbDto(
    @JsonProperty("kb_id") Long kbId,
    @JsonProperty("kb_code") String kbCode,
    String gubun,
    @JsonProperty("sys_type") String sysType,
    String category,
    String symptom,
    String cause,
    String summary,
    String action,
    String prevention,
    String keywords,
    @JsonProperty("case_count") Integer caseCount,
    String source,
    String status,
    @JsonProperty("reject_reason") String rejectReason,
    @JsonProperty("reviewed_by") String reviewedBy,
    @JsonProperty("created_by") String createdBy) {

    public static OpsKbDto from(OpsKb k) {
        String action = (k.getAction() != null && !k.getAction().isBlank()) ? k.getAction() : k.getCauseDesc();
        return new OpsKbDto(
            k.getKbId(), k.getKbCode(), k.getGubun(), k.getSysType(), k.getCategory(),
            k.getSymptom(), k.getCause(), k.getSummary(), action, k.getPrevention(),
            k.getKeywords(), k.getCaseCount(), k.getSource(), k.getStatus(),
            k.getRejectReason(), k.getReviewedBy(), k.getCreatedBy());
    }
}
```

- 컴포넌트 선언순 = 현행 `toDto` put 순(kb_id…created_by) 동일.
- `caseCount` 는 엔티티 `Integer` 그대로(현행 Map 값도 동일 타입). 단일어 키(gubun/category/symptom/cause/summary/action/prevention/keywords/source/status)는 무어노테이션 바인딩.

## 검증

1. `OpsKbDtoSerializationTest` (신규, ObjectMapper): 엔티티 채운 뒤 `from(kb)` 직렬화 JSON 의 키셋=17키·snake_case·`action` 폴백(action 공백→causeDesc) + null 필드 포함(예: reject_reason=null 키 존재) 확인. 가능하면 **현행 LinkedHashMap 동일 구성과 JSON tree 동치** 골든 비교.
2. `GOLDEN_RECORD=1 ./mvnw test -Dtest=MapDebtRatchetTest` → baseline 366→362 tighten.
3. `./mvnw test` 전체 green (회귀 0).
4. codex 검토(키 보존·null 포함·action 폴백·search/detail 반환 흐름).

## 롤백
원자 커밋 → `git revert`. DTO 1개 신규 + 1 컨트롤러 국소 치환(search 반환/out/toDto).

## 커밋(승인 후)
`refactor(api): OpsKbController 조회응답 toDto Map→OpsKbDto record + ratchet 366→362 (§6-4)`
