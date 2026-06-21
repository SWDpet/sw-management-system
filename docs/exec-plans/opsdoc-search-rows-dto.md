# 개발계획서 — OpsDocController 사람/요청자 검색 응답 List<Map>→record (opsdoc-search-rows-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 8번째. `opskb-todto-record`·`quotation-list-dto` 와 동일 패턴 — 조회/검색 응답의 컨트롤러-로컬 `List<Map<String,Object>>` 조립을 타입 record 로 치환. OpsDocController 의 **사람/요청자 관계자 검색 API 6종**을 묶어 처리. cascade 2종(sgg/systems)은 후속.
- **무손실 핵심**: 6종 모두 **`HashMap` 기반**이라 현행 JSON **키순서가 애초에 비결정적** → record 치환 시 tree 동치(키셋·값)만으로 무손실. 클라이언트는 응답 JSON 키로 접근. 전역 jackson non_null 미설정 → null 포함 기본(현행 put 도 null 값 담음).
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: 아래 6 메서드의 `List<Map<String,Object>>`(반환타입·`out/result` 지역·`m` 지역 = 각 3줄)을 record 로 치환. **`Map<String,Object>` 선언 358→340 (−18)**.
- **NFR**: wire 무손실(키셋·값·타입·null 포함·폴백 로직), 회귀 0, ratchet 340 tighten.

## 대상 6 메서드 + record 설계 (com.swmanager.system.dto.ops)

| 메서드 | 키(현행) | record | 폴백/주의 |
|---|---|---|---|
| `engineers` | id,name,position | `EngineerRow(Long id,String name,String position)` | position = `getPosition()!=null ? getPosition() : getPositionTitle()` |
| `requesterSearch` | id,name,org,dept,pos,tel | `RequesterRow(Long id,String name,String org,String dept,String pos,String tel)` | PersonInfo 직매핑 |
| `partnerContactSearch` | id,name,org,pos,tel | `PartnerContactRow(Long id,String name,String org,String pos,String tel)` | org = `partner!=null ? partner.getName() : null` |
| `partnerSearch` | id,name,type | `PartnerRow(Long id,String name,String type)` | Partner 직매핑 |
| `docPartners` | partner_id,name,role_label | `DocPartnerRow(@JsonProperty("partner_id") Long partnerId, String name, @JsonProperty("role_label") String roleLabel)` | name = `p!=null ? p.getName() : "#"+partnerId`; 컨트롤러가 partner 조회 후 `from(dp, p)` |
| `staffSearch` | id,name,org,pos | `StaffRow(Long id,String name,String org,String pos)` | org = orgUnit 조회명(컨트롤러 resolve 후 `of(s, org)`) |

- 키는 현행 `m.put(...)` 키 그대로. **snake_case 는 `docPartners` 만**(partner_id/role_label) → `@JsonProperty`. 나머지 camelCase = 컴포넌트명(무어노테이션).
- `@JsonInclude` 미부착(null 포함). 타입은 게터 1:1(id 류 Long, 문자 String). bool/숫자 없음.
- `from()` 팩토리에 현행 빌드 로직(폴백) 이식. 2차 조회 필요분(docPartners name·staffSearch org)은 컨트롤러가 resolve 한 값을 인자로 전달.

## 검증 (골든 = 레거시 HashMap 복제본과 tree 동치)

1. `OpsDocSearchRowsTest`(신규): 6 record 각각 채운 엔티티 → `from()` 직렬화 JSON 이 **현행 `HashMap` 복제본과 JsonNode tree 동치**(키셋·값). null 케이스(미설정 필드 null 키 존재), 폴백 케이스(engineers positionTitle, partnerContact org null, docPartners name "#id"). ※키순서는 HashMap 비결정이라 string 비교 안 함 — tree 동치만.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 358→340.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·타입·폴백·2차조회 인자·snake_case).

## 롤백
원자 커밋 → `git revert`. record 6개 신규 + 1 컨트롤러 6메서드 국소 치환.

## 커밋(승인 후)
`refactor(api): OpsDocController 사람/요청자 검색 응답 List<Map>→record 6종 + ratchet 358→340 (§6-4)`
