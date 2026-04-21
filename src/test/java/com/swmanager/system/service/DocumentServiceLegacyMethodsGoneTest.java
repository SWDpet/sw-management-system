package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S1 inspect-comprehensive-redesign — T20.
 *
 * DocumentService 에서 제거된 saveChecklist/getChecklists/saveIssue/getIssues 메서드가
 * 리플렉션으로도 발견되지 않음을 검증.
 */
class DocumentServiceLegacyMethodsGoneTest {

    @Test
    void legacy_checklist_and_issue_methods_are_gone() {
        List<String> names = Arrays.stream(DocumentService.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();

        assertThat(names).doesNotContain(
                "saveChecklist",
                "getChecklists",
                "saveIssue",
                "getIssues"
        );
    }

    @Test
    void save_signature_still_exists() {
        // E-opt1 유지 검증
        List<String> names = Arrays.stream(DocumentService.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();
        assertThat(names).contains("saveSignature");
    }
}
