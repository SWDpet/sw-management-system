package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UserApiController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 필드주입(userRepository) reflection. 경량 사용자 조회 2종 위임 + DTO 매핑.
 */
class UserApiControllerTest {

    private UserApiController controller;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new UserApiController();
        userRepository = mock(UserRepository.class);
        Field f = UserApiController.class.getDeclaredField("userRepository");
        f.setAccessible(true);
        f.set(controller, userRepository);
    }

    private static User user(long seq, String userid) {
        User u = new User();
        u.setUserSeq(seq);
        u.setUserid(userid);
        u.setUsername("이름" + seq);
        return u;
    }

    @Test
    void getAllWithDisabled_mapsRows() { // T-U1
        when(userRepository.findAll()).thenReturn(List.of(user(1L, "a"), user(2L, "b")));
        var rows = controller.getAllWithDisabled();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).userid()).isEqualTo("a");
    }

    @Test
    void getAllWithDisabled_empty() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThat(controller.getAllWithDisabled()).isEmpty();
    }

    @Test
    void getActive_mapsRows() { // T-U2
        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user(3L, "c")));
        var rows = controller.getActive();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).userid()).isEqualTo("c");
    }
}
