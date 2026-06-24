package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentDetail;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentDetailRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpsDocService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 생성자 주입이라 직접 생성. 필수 섹션키 검증·M2 엔지니어/요청자 검증·채번·
 * COMPLETED approvedAt 동기화·조건부 수정·detail 교체를 create()/update() 경유로 커버.
 */
class OpsDocServiceTest {

    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);
    private final OpsDocumentDetailRepository opsDocumentDetailRepository = mock(OpsDocumentDetailRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrgUnitRepository orgUnitRepository = mock(OrgUnitRepository.class);

    private OpsDocService service;

    private static final long SW_TEAM_ID = 10L;

    @BeforeEach
    void setUp() {
        service = new OpsDocService(opsDocumentRepository, opsDocumentDetailRepository,
                userRepository, orgUnitRepository);
        when(opsDocumentRepository.save(any(OpsDocument.class))).thenAnswer(i -> i.getArgument(0));
    }

    /** 타입별 필수 section_data 키를 모두 채운 맵. */
    private Map<String, Object> validSection(OpsDocType t) {
        Map<String, Object> m = new HashMap<>();
        switch (t) {
            case FAULT   -> { m.put("fault_date", "2026-06-01"); m.put("severity", "HIGH"); m.put("symptom", "x"); m.put("action", "x"); }
            case SUPPORT -> { m.put("request_date", "2026-06-01"); m.put("request_channel", "PHONE"); m.put("requester", "x"); m.put("support_target", "x"); m.put("support_content", "x"); }
            case INSTALL -> { m.put("install_date", "2026-06-01"); m.put("pre_check_completed", "Y"); m.put("version", "1.0"); m.put("verification", "x"); }
            case PATCH   -> { m.put("patch_date", "2026-06-01"); m.put("patch_kind", "HOTFIX"); m.put("target", "x"); m.put("version", "1.0"); m.put("rollback_plan", "x"); }
            default -> { }
        }
        return m;
    }

    private OpsDocument doc(OpsDocType type) {
        OpsDocument d = new OpsDocument();
        d.setDocType(type);
        d.setTitle("문서");
        return d;
    }

    /** SW지원팀 소속 활성 엔지니어 + 요청자 1명을 채운 FAULT 문서. */
    private OpsDocument validFault() {
        OpsDocument d = doc(OpsDocType.FAULT);
        User eng = new User();
        eng.setEnabled(true);
        eng.setOrgUnitId(SW_TEAM_ID);
        d.setEngineer(eng);
        d.setRequesterStaffId(1L);   // 요청자 정확히 1명
        return d;
    }

    private void stubSwTeam() {
        OrgUnit team = new OrgUnit();
        team.setUnitId(SW_TEAM_ID);
        when(orgUnitRepository.findFirstByNameAndUnitType("SW지원팀", "TEAM")).thenReturn(Optional.of(team));
    }

    // ===== validateSectionData (create 경유) =====

    @Test
    void create_install_valid_generatesDocNo_setsDefaults_savesDetail() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(null);  // 첫 채번 → seq 1
        OpsDocument saved = service.create(doc(OpsDocType.INSTALL), validSection(OpsDocType.INSTALL), "user1");

        assertThat(saved.getDocNo()).startsWith(OpsDocType.INSTALL.docNoPrefix() + "-").endsWith("-1");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.DRAFT);   // null → DRAFT
        assertThat(saved.getCreatedBy()).isEqualTo("user1");
        assertThat(saved.getUpdatedBy()).isEqualTo("user1");
        verify(opsDocumentDetailRepository).save(any(OpsDocumentDetail.class));  // detail 저장
    }

    @Test
    void create_missingRequiredKey_throws() {
        Map<String, Object> sec = validSection(OpsDocType.INSTALL);
        sec.remove("version");
        assertThatThrownBy(() -> service.create(doc(OpsDocType.INSTALL), sec, "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
    }

    @Test
    void create_blankRequiredValue_throws() {
        Map<String, Object> sec = validSection(OpsDocType.INSTALL);
        sec.put("version", "   ");   // blank → 누락 취급
        assertThatThrownBy(() -> service.create(doc(OpsDocType.INSTALL), sec, "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
    }

    @Test
    void create_nullSectionData_forTypedDoc_throws() {
        assertThatThrownBy(() -> service.create(doc(OpsDocType.PATCH), null, "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("section_data");
    }

    @Test
    void create_inspectType_exemptFromSectionValidation_noDetailSaved() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(null);
        OpsDocument saved = service.create(doc(OpsDocType.INSPECT), null, "u");  // INSPECT 면제 + sectionData 없음
        assertThat(saved.getDocNo()).startsWith(OpsDocType.INSPECT.docNoPrefix() + "-");
        verify(opsDocumentDetailRepository, never()).save(any());   // detail 없음
    }

    // ===== 채번 (generateDocNo) =====

    @Test
    void create_docNoSequence_incrementsFromMax() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(7);
        OpsDocument saved = service.create(doc(OpsDocType.INSTALL), validSection(OpsDocType.INSTALL), "u");
        assertThat(saved.getDocNo()).endsWith("-8");   // max 7 → 8
    }

    @Test
    void create_preservesExplicitDocNo() {
        OpsDocument d = doc(OpsDocType.INSTALL);
        d.setDocNo("CUSTOM-1");
        OpsDocument saved = service.create(d, validSection(OpsDocType.INSTALL), "u");
        assertThat(saved.getDocNo()).isEqualTo("CUSTOM-1");
        verify(opsDocumentRepository, never()).findMaxDocNoSeq(anyString());
    }

    // ===== 상태/작성자 =====

    @Test
    void create_completedStatus_setsApprovedAt() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(null);
        OpsDocument d = doc(OpsDocType.INSTALL);
        d.setStatus(DocumentStatus.COMPLETED);
        OpsDocument saved = service.create(d, validSection(OpsDocType.INSTALL), "u");
        assertThat(saved.getApprovedAt()).isNotNull();
    }

    @Test
    void create_resolvesAuthorFromCurrentUserId() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(null);
        User u = new User();
        when(userRepository.findByUserid("user1")).thenReturn(Optional.of(u));
        OpsDocument saved = service.create(doc(OpsDocType.INSTALL), validSection(OpsDocType.INSTALL), "user1");
        assertThat(saved.getAuthor()).isSameAs(u);
    }

    // ===== validateRelations (FAULT/SUPPORT, create 경유) =====

    @Test
    void create_fault_requesterCountZero_throws() {
        OpsDocument d = validFault();
        d.setRequesterStaffId(null);   // 요청자 0명
        assertThatThrownBy(() -> service.create(d, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정확히 1명");
    }

    @Test
    void create_fault_requesterCountTwo_throws() {
        OpsDocument d = validFault();
        d.setRequesterContactId(2L);   // staffId(1) + contactId(2) = 2명
        assertThatThrownBy(() -> service.create(d, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정확히 1명");
    }

    @Test
    void create_fault_engineerNull_throws() {
        OpsDocument d = validFault();
        d.setEngineer(null);
        assertThatThrownBy(() -> service.create(d, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("엔지니어는 필수");
    }

    @Test
    void create_fault_engineerDisabled_throws() {
        OpsDocument d = validFault();
        d.getEngineer().setEnabled(false);
        assertThatThrownBy(() -> service.create(d, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비활성");
    }

    @Test
    void create_fault_engineerWrongTeam_throws() {
        OpsDocument d = validFault();
        d.getEngineer().setOrgUnitId(999L);   // SW지원팀(10) 아님
        stubSwTeam();
        assertThatThrownBy(() -> service.create(d, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SW지원팀");
    }

    @Test
    void create_fault_valid_passes() {
        when(opsDocumentRepository.findMaxDocNoSeq(anyString())).thenReturn(null);
        stubSwTeam();
        OpsDocument saved = service.create(validFault(), validSection(OpsDocType.FAULT), "u");
        assertThat(saved.getDocNo()).startsWith(OpsDocType.FAULT.docNoPrefix() + "-");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.DRAFT);
    }

    // ===== update =====

    @Test
    void update_notFound_throws() {
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(5L, doc(OpsDocType.INSTALL), null, "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_appliesNonNullChangesOnly() {
        OpsDocument existing = doc(OpsDocType.INSTALL);
        existing.setTitle("old"); existing.setSysType("OLD");
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.of(existing));

        OpsDocument changes = new OpsDocument();   // docType null, title null
        changes.setSysType("NEW");
        OpsDocument out = service.update(5L, changes, validSection(OpsDocType.INSTALL), "editor");

        assertThat(out.getTitle()).isEqualTo("old");        // null 변경 → 보존
        assertThat(out.getSysType()).isEqualTo("NEW");      // 비-null → 반영
        assertThat(out.getUpdatedBy()).isEqualTo("editor");
    }

    @Test
    void update_transitionToCompleted_setsApprovedAt() {
        OpsDocument existing = doc(OpsDocType.INSTALL);
        existing.setStatus(DocumentStatus.DRAFT);
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.of(existing));

        OpsDocument changes = new OpsDocument();
        changes.setStatus(DocumentStatus.COMPLETED);
        OpsDocument out = service.update(5L, changes, validSection(OpsDocType.INSTALL), "u");

        assertThat(out.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(out.getApprovedAt()).isNotNull();
    }

    @Test
    void update_alreadyCompleted_doesNotResetApprovedAt() {
        OpsDocument existing = doc(OpsDocType.INSTALL);
        existing.setStatus(DocumentStatus.COMPLETED);
        LocalDateTime original = LocalDateTime.of(2026, 1, 1, 0, 0);
        existing.setApprovedAt(original);
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.of(existing));

        OpsDocument changes = new OpsDocument();
        changes.setStatus(DocumentStatus.COMPLETED);
        OpsDocument out = service.update(5L, changes, validSection(OpsDocType.INSTALL), "u");

        assertThat(out.getApprovedAt()).isEqualTo(original);   // 기존 승인일 보존
    }

    @Test
    void update_fault_revalidatesRelations_throwsOnInvalid() {
        OpsDocument existing = doc(OpsDocType.FAULT);
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.of(existing));

        OpsDocument changes = new OpsDocument();   // 엔지니어·요청자 미지정 → 요청자 0명으로 검증 실패
        assertThatThrownBy(() -> service.update(5L, changes, validSection(OpsDocType.FAULT), "u"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정확히 1명");   // 의도한 관계검증 실패임을 고정(무관한 예외와 구분)
    }

    @Test
    void update_replacesDetail_whenSectionDataProvided() {
        OpsDocument existing = doc(OpsDocType.INSTALL);
        when(opsDocumentRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.update(5L, new OpsDocument(), validSection(OpsDocType.INSTALL), "u");

        verify(opsDocumentDetailRepository).deleteByDocument_DocId(5L);   // 기존 detail 삭제
        verify(opsDocumentDetailRepository).save(any(OpsDocumentDetail.class));
    }

    // ===== 위임 =====

    @Test
    void delegations_findByIdAndDelete() {
        OpsDocument d = new OpsDocument();
        when(opsDocumentRepository.findById(3L)).thenReturn(Optional.of(d));
        assertThat(service.findById(3L)).containsSame(d);

        service.delete(3L);
        verify(opsDocumentRepository).deleteById(3L);
    }
}
