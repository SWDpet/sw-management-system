# 개발계획서 — DocumentParticipantController 응답 Map→record/ApiResult (요청바디 보존) (participant-save-dto)

- **상태**: v0.3 (codex 2회 논의 수렴 — A/(d) 채택. 최종승인 완료, 구현)
- **작성일**: 2026-06-22
- **맥락**: §6-4. DocumentParticipantController 의 **응답** `Map<String,Object>` 치환. 요청바디 `List<Map>` 는 보존.
- **결정 근거(codex 2회)**: 요청바디 강타입화는 strict 무손실 위반(엣지 의미 변경), presence-aware JsonNode-record 는 Jackson record 바인딩에서 누락/null 구분 실패(codex P1), @JsonAnySetter 풀구현은 무클라이언트 엔드포인트에 과설계(헌장 위반). → **요청 List<Map> 는 의도적 lenient-파싱 경계로 보존**(jsonb 파싱·P6 키 보존과 동류), **응답만 치환**.
- **클라이언트**: `/document/api/contract-participants/**` 저장소 내 호출 0. **디자인팀** 비해당. **DB** 무관.

## 치환(응답 4) / 보존(요청 2)

| 위치 | 형태 | 처리 |
|---|---|---|
| save 403(L112) | {success:false, error:{code,message}} | `ApiResult.fail("FORBIDDEN","수정 권한이 없습니다")` |
| save 404(project null) | {success:false, error:"사업을 찾을 수 없습니다."} | `ResponseEntity.status(404).body(ApiResult.failMessage("사업을 찾을 수 없습니다."))` |
| save 성공 | {success:true, count:n} | `ResponseEntity.ok(new ParticipantSaveResult(true, participantList.size()))` |
| save catch(200) | {success:false, error:msg} | `ResponseEntity.ok(ApiResult.failMessage(e.getMessage()))` |
| save 반환타입(L108) | — | `ResponseEntity<?>` |
| secure 403(L93) | **{error:{code,message}}**(success 없음) | `Map.of("error", Map.of("code","FORBIDDEN","message","민감 정보 조회 권한이 없습니다"))` (ApiResult.fail 은 success 추가→미사용. Map.of 는 ratchet 비대상) |
| **요청바디(L110)·루프 item(L131)** | `List<Map<String,Object>>` 입력 | **보존**(lenient 파싱 의도적: 누락/null·string-boolean·malformed→200. 타입화는 무손실 불가/과설계) |

- 신규 record 1종: `ParticipantSaveResult(boolean success, int count)` (com.swmanager.system.dto.workplan). 나머지 ApiResult/Map.of 재사용.
- 응답 wire 100% 보존: 성공 {success:true,count}, 404/catch {success:false,error:str}, save403 {success:false,error:{code,message}}, secure403 {error:{code,message}}.

## 목표 (FR/NFR)
- **FR**: 응답 `Map<String,Object>` 4선언(L93·L108·L112·L117) 제거 → record 1 + ApiResult + Map.of. **선언 255→251 (−4)**. 요청 2(L110·L131) 보존.
- **NFR**: 응답 wire·status(200/403/404) 무손실, 요청 파싱 의미 무변(보존), 회귀 0, ratchet 251 tighten.

## 검증
1. `ParticipantSaveResultTest`(신규): ParticipantSaveResult→{success:true,count} tree 동치(count=0 포함, success/count 항상 직렬화). secure403 Map.of→{error:{code,message}} tree 동치.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 255→251.
3. `./mvnw test` 전체 green.
4. codex 검토(응답 wire·status·요청 보존·미사용 import).

## 롤백
원자 1 커밋 → `git revert`. record 1 신규 + 컨트롤러 2 메서드 응답 국소 치환. 요청·클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentParticipantController 저장/조회 응답 Map→ParticipantSaveResult/ApiResult(요청바디 lenient 보존) + ratchet 255→251 (§6-4)`
