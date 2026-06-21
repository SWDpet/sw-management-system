package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.ContractParticipant;

/**
 * /document/api/contract-participants/{projId} (과업참여자 비민감 조회) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 participant-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 *
 * <p><b>민감 필드 분리(감사 P1-3)</b>: 본 dto 는 ssn/certificate 를 일절 포함하지 않는다(9필드).
 * 민감 필드는 EDIT 게이트 {@code /secure} 엔드포인트의 {@link SecureContractParticipantRow} 전용.
 * {@code isSiteRep} 은 boolean 컴포넌트라 Jackson is-접두 깎임 방지로 {@link JsonProperty} 고정.
 */
public record ContractParticipantRow(
        Integer participantId,
        Long userId,
        String userName,
        String position,
        String roleType,
        String techGrade,
        String taskDesc,
        @JsonProperty("isSiteRep") Boolean isSiteRep,
        String tasks) {

    /** {@link ContractParticipant} → 행. user null 시 userName/position/tasks 는 빈 문자열, userId 는 null(현행 폴백). */
    public static ContractParticipantRow from(ContractParticipant cp) {
        User u = cp.getUser();
        return new ContractParticipantRow(
                cp.getParticipantId(),
                u != null ? u.getUserSeq() : null,
                u != null ? u.getUsername() : "",
                u != null ? u.getPositionTitle() : "",
                cp.getRoleType(),
                cp.getTechGrade(),
                cp.getTaskDesc(),
                cp.getIsSiteRep(),
                u != null ? u.getTasks() : "");
    }
}
