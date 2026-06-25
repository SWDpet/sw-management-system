package com.swmanager.system.dto;

import com.swmanager.system.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** UserDTO 변환 로직 단위테스트 (커버리지 상향 beyond-A, 순수). */
class UserDTOTest {

    @Test
    void fromEntity_null_returnsNull() {
        assertThat(UserDTO.fromEntity(null)).isNull();
    }

    @Test
    void fromEntity_mapsFields_andAuthNames() {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("hong");
        u.setUsername("홍길동");
        u.setEmail("hong@example.com");
        u.setAuthDashboard("VIEW");
        u.setAuthProject("EDIT");
        u.setAuthPerson("NONE");
        u.setAuthInfra(null);

        UserDTO dto = UserDTO.fromEntity(u);

        assertThat(dto.getUserSeq()).isEqualTo(1L);
        assertThat(dto.getUserid()).isEqualTo("hong");
        assertThat(dto.getUsername()).isEqualTo("홍길동");
        assertThat(dto.getEmail()).isEqualTo("hong@example.com");
        // getAuthName 4분기
        assertThat(dto.getAuthDashboardNm()).isEqualTo("조회");      // VIEW
        assertThat(dto.getAuthProjectNm()).isEqualTo("편집");        // EDIT
        assertThat(dto.getAuthPersonNm()).isEqualTo("접근불가");      // NONE
        assertThat(dto.getAuthInfraNm()).isEqualTo("없음");          // null
    }

    @Test
    void getAuthName_defaultBranch_returnsRawCode() {
        User u = new User();
        u.setAuthDashboard("WEIRD");
        assertThat(UserDTO.fromEntity(u).getAuthDashboardNm()).isEqualTo("WEIRD");
    }

    @Test
    void getUserRoleNm_branches() {
        assertThat(UserDTO.builder().userRole("ROLE_ADMIN").build().getUserRoleNm()).isEqualTo("관리자");
        assertThat(UserDTO.builder().userRole("ROLE_USER").build().getUserRoleNm()).isEqualTo("일반사용자");
        assertThat(UserDTO.builder().userRole("ROLE_ETC").build().getUserRoleNm()).isEqualTo("ROLE_ETC");
    }

    @Test
    void getEnabledNm_branches() {
        assertThat(UserDTO.builder().enabled(true).build().getEnabledNm()).isEqualTo("활성");
        assertThat(UserDTO.builder().enabled(false).build().getEnabledNm()).isEqualTo("대기중");
        assertThat(UserDTO.builder().enabled(null).build().getEnabledNm()).isEqualTo("대기중");
    }
}
