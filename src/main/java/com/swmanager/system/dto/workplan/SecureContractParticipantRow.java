package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.ContractParticipant;

/**
 * /document/api/contract-participants/{projId}/secure (과업참여자 민감 조회) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 participant-rows-dto).
 * 비민감 {@link ContractParticipantRow}(9필드)에 민감 필드 ssn/certificate 를 더한 11필드.
 * 본 dto 는 EDIT 권한 게이트 {@code /secure} 엔드포인트 전용이다(감사 P1-3).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record SecureContractParticipantRow(
        Integer participantId,
        Long userId,
        String userName,
        String position,
        String roleType,
        String techGrade,
        String taskDesc,
        @JsonProperty("isSiteRep") Boolean isSiteRep,
        String tasks,
        String ssn,
        String certificate) {

    /** {@link ContractParticipant} → 민감 포함 행. user null 시 문자열 필드는 빈 문자열, userId 는 null(현행 폴백). */
    public static SecureContractParticipantRow from(ContractParticipant cp) {
        User u = cp.getUser();
        return new SecureContractParticipantRow(
                cp.getParticipantId(),
                u != null ? u.getUserSeq() : null,
                u != null ? u.getUsername() : "",
                u != null ? u.getPositionTitle() : "",
                cp.getRoleType(),
                cp.getTechGrade(),
                cp.getTaskDesc(),
                cp.getIsSiteRep(),
                u != null ? u.getTasks() : "",
                u != null ? u.getSsn() : "",
                u != null ? u.getCertificate() : "");
    }
}
