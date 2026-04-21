package com.swmanager.system.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S9 access-log-action-and-menu-sync — MenuName 상수 개수·존재 검증.
 */
class MenuNameTest {

    // T2: MenuName 정확히 16개 (기존 11 + 신규 5)
    @Test
    void menu_name_has_exactly_16_public_string_constants() {
        List<Field> stringConstants = Arrays.stream(MenuName.class.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .filter(f -> f.getType() == String.class)
                .toList();

        assertThat(stringConstants).hasSize(16);
    }

    // S9 신규 5종 존재 확인
    @Test
    void s9_new_menus_exist() {
        assertThat(MenuName.QR_LICENSE).isEqualTo("QR라이선스");
        assertThat(MenuName.LICENSE_REGISTRY).isEqualTo("라이선스대장");
        assertThat(MenuName.GEONURIS_LICENSE).isEqualTo("GeoNURIS라이선스");
        assertThat(MenuName.QUOTATION).isEqualTo("견적서");
        assertThat(MenuName.SIGNUP).isEqualTo("회원가입");
    }
}
