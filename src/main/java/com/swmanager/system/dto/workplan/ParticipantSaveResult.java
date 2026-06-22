package com.swmanager.system.dto.workplan;

/**
 * /document/api/contract-participants/{projId} 과업참여자 저장 성공(200) 응답 dto — `{success:true, count:n}`.
 *
 * 기존 DocumentParticipantController.saveContractParticipants 의 컨트롤러-로컬 HashMap 응답조립을 record 로
 * 대체한다(§6-4 participant-save-dto). 키셋·값 동치로 무손실. success/count 는 primitive 라 항상 직렬화.
 * (실패/권한 응답은 ApiResult 재사용, 요청바디는 lenient 파싱 보존)
 */
public record ParticipantSaveResult(boolean success, int count) {
}
