package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.User;

/**
 * /document/api/user/{userSeq} (비민감 사용자 정보) 응답 dto — 13키.
 *
 * 기존 DocumentController.getUserInfo 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-userinfo-rows-dto). 키셋·값 동치로 무손실. null 포함(@JsonInclude 미부착).
 *
 * <p><b>민감필드 비노출(감사 P1-3)</b>: ssn/certificate/email 은 본 record 에 포함하지 않는다.
 * 민감 정보는 EDIT 권한 + {@link UserInfoSecureRow}(/secure) 전용.
 */
public record UserInfoRow(Long userSeq, String username, String positionTitle, String position,
                          String techGrade, String mobile, String tel, String address, String tasks,
                          String deptNm, String teamNm, String careerYears, String fieldRole) {

    public static UserInfoRow from(User u) {
        return new UserInfoRow(u.getUserSeq(), u.getUsername(), u.getPositionTitle(), u.getPosition(),
                u.getTechGrade(), u.getMobile(), u.getTel(), u.getAddress(), u.getTasks(),
                u.getDeptNm(), u.getTeamNm(), u.getCareerYears(), u.getFieldRole());
    }
}
