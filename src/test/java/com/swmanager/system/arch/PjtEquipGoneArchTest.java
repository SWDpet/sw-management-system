package com.swmanager.system.arch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S15 pjt-equip-decision — T4.
 *
 * PjtEquip / PjtEquipRepository 클래스가 classpath 에서 제거되었음을 검증.
 */
class PjtEquipGoneArchTest {

    @Test
    void pjt_equip_entity_class_is_gone() {
        assertThat(classExists("com.swmanager.system.domain.PjtEquip")).isFalse();
    }

    @Test
    void pjt_equip_repository_class_is_gone() {
        assertThat(classExists("com.swmanager.system.repository.PjtEquipRepository")).isFalse();
    }

    private boolean classExists(String fqcn) {
        try { Class.forName(fqcn); return true; }
        catch (ClassNotFoundException e) { return false; }
    }
}
