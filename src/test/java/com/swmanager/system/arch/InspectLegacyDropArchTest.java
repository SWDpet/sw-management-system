package com.swmanager.system.arch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S1 inspect-comprehensive-redesign — 삭제된 레거시 클래스 부재 검증.
 *
 * T13: `InspectChecklist`, `InspectIssue`, 관련 Repository 가 classpath 에 없음을 확인.
 * (DocumentSignature 는 §3-E E-opt1 로 유지되므로 검증 대상 아님)
 */
class InspectLegacyDropArchTest {

    @Test
    void inspect_checklist_class_is_gone() {
        assertThat(classExists("com.swmanager.system.domain.workplan.InspectChecklist")).isFalse();
    }

    @Test
    void inspect_issue_class_is_gone() {
        assertThat(classExists("com.swmanager.system.domain.workplan.InspectIssue")).isFalse();
    }

    @Test
    void inspect_checklist_repository_is_gone() {
        assertThat(classExists("com.swmanager.system.repository.workplan.InspectChecklistRepository")).isFalse();
    }

    @Test
    void inspect_issue_repository_is_gone() {
        assertThat(classExists("com.swmanager.system.repository.workplan.InspectIssueRepository")).isFalse();
    }

    @Test
    void document_signature_still_exists() {
        // E-opt1 검증: Signature 는 유지되어야 함
        assertThat(classExists("com.swmanager.system.domain.workplan.DocumentSignature")).isTrue();
        assertThat(classExists("com.swmanager.system.repository.workplan.DocumentSignatureRepository")).isTrue();
    }

    private boolean classExists(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
