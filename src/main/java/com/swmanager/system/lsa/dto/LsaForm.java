package com.swmanager.system.lsa.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * LSA 작성 폼 바인딩(@ModelAttribute). record 불가(폼 바인딩) → @Getter/@Setter.
 */
@Getter
@Setter
public class LsaForm {
    private String cityNm;   // 시도
    private String distNm;   // 시군구
    private String deptNm;   // 부서
    private String teamNm;   // 팀
    private String userNm;   // 이름
    private String tel;      // 전화
    private String email;    // 이메일
    private String version;  // 버전
    private String issuer;   // 발급자(관리자만 폼값 사용; 비관리자는 서버가 로그인 실명 강제)
}
