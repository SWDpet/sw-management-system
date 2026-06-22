package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.User;

/**
 * /document/api/user/{userSeq}/secure (민감 포함 사용자 정보, EDIT 전용) 응답 dto — 16키.
 *
 * 기존 DocumentController.getUserInfoSecure 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-userinfo-rows-dto). 비민감 13키 + 민감 3키(ssn/certificate/email). 키셋·값 동치로 무손실.
 * 비민감 {@link UserInfoRow}(13키)와 키셋이 달라 별도 타입(민감 화이트리스트 분리).
 */
public record UserInfoSecureRow(Long userSeq, String username, String positionTitle, String position,
                                String techGrade, String mobile, String tel, String address, String tasks,
                                String deptNm, String teamNm, String careerYears, String fieldRole,
                                String ssn, String certificate, String email) {

    public static UserInfoSecureRow from(User u) {
        return new UserInfoSecureRow(u.getUserSeq(), u.getUsername(), u.getPositionTitle(), u.getPosition(),
                u.getTechGrade(), u.getMobile(), u.getTel(), u.getAddress(), u.getTasks(),
                u.getDeptNm(), u.getTeamNm(), u.getCareerYears(), u.getFieldRole(),
                u.getSsn(), u.getCertificate(), u.getEmail());
    }
}
