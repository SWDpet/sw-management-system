package com.swmanager.system.quotation.service;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RemarksRenderer 단위 테스트 (S3 qt-remarks-users-link).
 *
 * 검증 매트릭스 (기획서 T# 매핑):
 *  T-A nullContent_returnsNull
 *  T-B nullUserId_returnsContentAsIs                  (FR-7)
 *  T-C userIdNotInDb_returnsContentAsIs               (FR-7 fallback)
 *  T-D validUserId_substitutesAllPlaceholders         (FR-5/6)
 *  T-E nullDeptNm_substitutesEmptyString
 *  T-F unknownPlaceholder_preserved                   (T7 — {phone} 보존, NFR-4)
 *  T-G multipleOccurrences_allReplaced
 *  T-H nullContent_userIdSet_returnsNull
 */
@ExtendWith(MockitoExtension.class)
class RemarksRendererTest {

    @Mock UserRepository userRepository;

    RemarksRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new RemarksRenderer(userRepository);
    }

    @Test
    void T_A_nullContent_returnsNull() {
        assertThat(renderer.render(null, 1L)).isNull();
        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void T_B_nullUserId_returnsContentAsIs() {
        String c = "※ 담당자 : {dept_nm} {username}";
        assertThat(renderer.render(c, null)).isEqualTo(c);
        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void T_C_userIdNotInDb_returnsContentAsIs() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        String c = "※ 담당자 : {dept_nm} {username}";
        assertThat(renderer.render(c, 999L)).isEqualTo(c);
    }

    @Test
    void T_D_validUserId_substitutesAllPlaceholders() {
        User u = mockUser(6L, "박욱진", "SW지원부", "이사");
        when(userRepository.findById(6L)).thenReturn(Optional.of(u));

        String c = "※ 담당자 : {dept_nm} {username} {position_title} (T.070-...)";
        String r = renderer.render(c, 6L);
        assertThat(r).isEqualTo("※ 담당자 : SW지원부 박욱진 이사 (T.070-...)");
    }

    @Test
    void T_E_nullDeptNm_substitutesEmptyString() {
        User u = mockUser(7L, "테스터", null, null);
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));

        String c = "[{dept_nm}] {username} {position_title}";
        String r = renderer.render(c, 7L);
        assertThat(r).isEqualTo("[] 테스터 ");
    }

    @Test
    void T_F_unknownPlaceholder_preserved() {
        User u = mockUser(6L, "박욱진", "SW지원부", "이사");
        lenient().when(userRepository.findById(6L)).thenReturn(Optional.of(u));

        String c = "{username} ({phone}) [{position_title}]";
        String r = renderer.render(c, 6L);
        assertThat(r).isEqualTo("박욱진 ({phone}) [이사]");
    }

    @Test
    void T_G_multipleOccurrences_allReplaced() {
        User u = mockUser(6L, "박욱진", "SW지원부", "이사");
        when(userRepository.findById(6L)).thenReturn(Optional.of(u));

        String c = "{username}/{username}/{dept_nm}/{username}";
        String r = renderer.render(c, 6L);
        assertThat(r).isEqualTo("박욱진/박욱진/SW지원부/박욱진");
    }

    @Test
    void T_H_emptyContent_userIdSet_returnsEmpty() {
        assertThat(renderer.render("", 6L)).isEmpty();
        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    // ─── 헬퍼 ───
    private static User mockUser(Long id, String username, String dept, String position) {
        User u = new User();
        u.setUserSeq(id);
        u.setUsername(username);
        u.setDeptNm(dept);
        u.setPositionTitle(position);
        return u;
    }
}
