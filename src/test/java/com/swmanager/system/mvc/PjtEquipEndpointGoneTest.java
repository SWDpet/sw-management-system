package com.swmanager.system.mvc;

import com.swmanager.system.controller.SwController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S15 pjt-equip-decision — T5/T6.
 *
 * SwController 에서 /api/equip/{projId} 핸들러 메서드가 제거되었음을 리플렉션으로 검증.
 *
 * NOTE: Spring Boot 에서 /projects/api/equip/1 을 직접 호출해 404 를 강제 검증하려 해도,
 *       다른 와일드카드 매핑/정적 리소스/에러 핸들러로 인해 응답 코드가 200 이 나올 수 있어
 *       "핸들러 메서드 부재" 라는 구조적 조건으로 회귀 방지.
 */
class PjtEquipEndpointGoneTest {

    @Test
    void swcontroller_getEquipList_method_is_gone() {
        List<String> names = Arrays.stream(SwController.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();
        assertThat(names).doesNotContain("getEquipList");
    }

    @Test
    void swcontroller_saveEquipList_method_is_gone() {
        List<String> names = Arrays.stream(SwController.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();
        assertThat(names).doesNotContain("saveEquipList");
    }

    @Test
    void swcontroller_has_no_pjtEquipRepository_field() {
        List<String> fieldTypes = Arrays.stream(SwController.class.getDeclaredFields())
                .map(f -> f.getType().getSimpleName())
                .toList();
        assertThat(fieldTypes).noneMatch(t -> t.contains("PjtEquip"));
    }
}
