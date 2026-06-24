package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpsDocLinkService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 생성자 주입이라 직접 생성. 점검내역서→INSPECT row 연계: docNo 3포맷 룩업·신규/갱신·
 * 작성자 해석(숫자PK/userid/inspUserId 폴백)·예외 격리(REQUIRES_NEW) 커버.
 */
class OpsDocLinkServiceTest {

    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private OpsDocLinkService service;

    @BeforeEach
    void setUp() {
        service = new OpsDocLinkService(opsDocumentRepository, userRepository);
    }

    private InspectReport report(Long id, String month, String docTitle, String createdBy, Long inspUserId) {
        InspectReport r = new InspectReport();
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "inspectMonth", month);
        ReflectionTestUtils.setField(r, "sysType", "UPIS");
        ReflectionTestUtils.setField(r, "docTitle", docTitle);
        ReflectionTestUtils.setField(r, "createdBy", createdBy);
        ReflectionTestUtils.setField(r, "inspUserId", inspUserId);
        return r;
    }

    /** save 로 전달된 OpsDocument 캡처. */
    private OpsDocument captureSaved() {
        ArgumentCaptor<OpsDocument> cap = ArgumentCaptor.forClass(OpsDocument.class);
        verify(opsDocumentRepository).save(cap.capture());
        return cap.getValue();
    }

    @Test
    void linkInspectReport_createsNewDoc_whenNoneFound() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());

        service.linkInspectReport(report(5L, "2026-06", null, null, null));

        OpsDocument saved = captureSaved();
        assertThat(saved.getDocType()).isEqualTo(OpsDocType.INSPECT);
        assertThat(saved.getDocNo()).isEqualTo("INSP-2026-5");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(saved.getApprovedAt()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("점검내역서");   // docTitle null → 기본값
        assertThat(saved.getSysType()).isEqualTo("UPIS");
    }

    @Test
    void linkInspectReport_updatesExisting_foundByPrimaryDocNo() {
        OpsDocument existing = new OpsDocument();
        existing.setApprovedAt(LocalDateTime.of(2026, 1, 1, 0, 0));   // 기존 승인일 보존 확인용
        when(opsDocumentRepository.findByDocNo("INSP-2026-5")).thenReturn(Optional.of(existing));

        service.linkInspectReport(report(5L, "2026-06", "특별점검", null, null));

        OpsDocument saved = captureSaved();
        assertThat(saved).isSameAs(existing);                 // 기존 row 재사용
        assertThat(saved.getTitle()).isEqualTo("특별점검");
        assertThat(saved.getApprovedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));  // 미덮어씀
    }

    @Test
    void linkInspectReport_monthlyFormatFallback_match() {
        OpsDocument existing = new OpsDocument();
        when(opsDocumentRepository.findByDocNo("INSP-2026-5")).thenReturn(Optional.empty());
        when(opsDocumentRepository.findByDocNo("INSP-2026-06-5")).thenReturn(Optional.of(existing));  // 중간 포맷

        service.linkInspectReport(report(5L, "2026-06", null, null, null));

        assertThat(captureSaved()).isSameAs(existing);
    }

    @Test
    void linkInspectReport_numericCreatedBy_resolvesById() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());
        User u = new User();
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));

        service.linkInspectReport(report(5L, "2026-06", null, "7", null));

        assertThat(captureSaved().getAuthor()).isSameAs(u);
    }

    @Test
    void linkInspectReport_useridCreatedBy_resolvesByUserid() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());
        User u = new User();
        when(userRepository.findByUserid("jdoe")).thenReturn(Optional.of(u));

        service.linkInspectReport(report(5L, "2026-06", null, "jdoe", null));

        assertThat(captureSaved().getAuthor()).isSameAs(u);
    }

    @Test
    void linkInspectReport_inspUserIdFallback_whenCreatedByBlank() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());
        User u = new User();
        when(userRepository.findById(9L)).thenReturn(Optional.of(u));

        service.linkInspectReport(report(5L, "2026-06", null, null, 9L));

        assertThat(captureSaved().getAuthor()).isSameAs(u);
    }

    @Test
    void linkInspectReport_nullMonth_usesCurrentYear() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());
        // 서비스가 내부적으로 LocalDate.now() 를 읽으므로 연말 경계 flaky 회피 — 호출 전후 연도 모두 허용.
        int yBefore = java.time.LocalDate.now().getYear();
        service.linkInspectReport(report(5L, null, null, null, null));
        int yAfter = java.time.LocalDate.now().getYear();

        assertThat(captureSaved().getDocNo()).isIn("INSP-" + yBefore + "-5", "INSP-" + yAfter + "-5");
    }

    @Test
    void linkInspectReport_saveThrows_isSwallowed_byTryCatch() {
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());
        when(opsDocumentRepository.save(any())).thenThrow(new RuntimeException("DB down"));

        // 본 단위테스트는 Spring 프록시 미경유라 @Transactional(REQUIRES_NEW) 자체가 아닌
        // 메서드 내 try/catch 의 예외 삼킴만 검증(전파 격리는 통합테스트 영역).
        assertThatCode(() -> service.linkInspectReport(report(5L, "2026-06", null, null, null)))
                .doesNotThrowAnyException();
    }
}
