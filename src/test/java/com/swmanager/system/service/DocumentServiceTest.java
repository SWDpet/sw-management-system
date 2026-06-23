package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.*;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.workplan.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DocumentService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입. jdbcTemplate 폴백(SQL 결합)은 미주입(제외, codex).
 */
class DocumentServiceTest {

    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final DocumentDetailRepository documentDetailRepository = mock(DocumentDetailRepository.class);
    private final DocumentHistoryRepository documentHistoryRepository = mock(DocumentHistoryRepository.class);
    private final InfraRepository infraRepository = mock(InfraRepository.class);
    private final MessageResolver messages = mock(MessageResolver.class);
    private final com.swmanager.system.repository.InspectReportRepository inspectReportRepository =
            mock(com.swmanager.system.repository.InspectReportRepository.class);

    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService();
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "documentDetailRepository", documentDetailRepository);
        ReflectionTestUtils.setField(service, "documentHistoryRepository", documentHistoryRepository);
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
        ReflectionTestUtils.setField(service, "messages", messages);
        ReflectionTestUtils.setField(service, "inspectReportRepository", inspectReportRepository);
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ===== getDocumentById =====

    @Test
    void getDocumentById_found() {
        Document d = new Document();
        when(documentRepository.findById(1)).thenReturn(Optional.of(d));
        assertThat(service.getDocumentById(1)).isSameAs(d);
    }

    @Test
    void getDocumentById_notFound_throws() {
        when(documentRepository.findById(9)).thenReturn(Optional.empty());
        when(messages.get(eq("error.document.not_found"), any())).thenReturn("문서 없음: 9");
        assertThatThrownBy(() -> service.getDocumentById(9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9");
    }

    // ===== findDuplicateProjDoc =====

    private Document docWithProj(int docId, long projId) {
        Document d = new Document();
        d.setDocId(docId);
        SwProject p = new SwProject(); p.setProjId(projId);
        d.setProject(p);
        return d;
    }

    @Test
    void findDuplicateProjDoc_commence_returnsFirstMatch() {
        Document d = docWithProj(11, 100L);
        when(documentRepository.findByDocTypeOrderByCreatedAtDesc(DocumentType.COMMENCE))
                .thenReturn(List.of(d, docWithProj(12, 200L)));
        Integer dup = service.findDuplicateProjDoc(100L, DocumentType.COMMENCE, null, null);
        assertThat(dup).isEqualTo(11);
    }

    @Test
    void findDuplicateProjDoc_noMatch_returnsNull() {
        when(documentRepository.findByDocTypeOrderByCreatedAtDesc(DocumentType.COMMENCE))
                .thenReturn(List.of(docWithProj(12, 200L)));
        assertThat(service.findDuplicateProjDoc(100L, DocumentType.COMMENCE, null, null)).isNull();
    }

    @Test
    void findDuplicateProjDoc_nullProjId_returnsNull() {
        assertThat(service.findDuplicateProjDoc(null, DocumentType.COMMENCE, null, null)).isNull();
    }

    @Test
    void findDuplicateProjDoc_excludesSelf() {
        when(documentRepository.findByDocTypeOrderByCreatedAtDesc(DocumentType.COMMENCE))
                .thenReturn(List.of(docWithProj(11, 100L)));
        assertThat(service.findDuplicateProjDoc(100L, DocumentType.COMMENCE, null, 11)).isNull();
    }

    @Test
    void findDuplicateProjDoc_interim_matchesPaymentRound() {
        Document d = docWithProj(21, 100L);
        DocumentDetail det = new DocumentDetail();
        det.setSectionKey("inspector");
        det.setSectionData(Map.of("paymentRound", "2"));
        d.getDetails().add(det);
        when(documentRepository.findByDocTypeOrderByCreatedAtDesc(DocumentType.INTERIM))
                .thenReturn(List.of(d));
        assertThat(service.findDuplicateProjDoc(100L, DocumentType.INTERIM, 2, null)).isEqualTo(21);
        assertThat(service.findDuplicateProjDoc(100L, DocumentType.INTERIM, 3, null)).isNull();
        assertThat(service.findDuplicateProjDoc(100L, DocumentType.INTERIM, null, null)).isNull();
    }

    // ===== getDistNamesByCity =====

    @Test
    void getDistNamesByCity_blank_returnsEmpty() {
        assertThat(service.getDistNamesByCity("  ")).isEmpty();
        verify(documentRepository, never()).findDistinctDistNamesByCity(any());
    }

    @Test
    void getDistNamesByCity_delegates() {
        when(documentRepository.findDistinctDistNamesByCity("서울특별시")).thenReturn(List.of("강남구"));
        assertThat(service.getDistNamesByCity("서울특별시")).containsExactly("강남구");
    }

    // ===== createDocument =====

    @Test
    void createDocument_setsFields_recordsHistory() {
        User author = new User();
        Document saved = service.createDocument(DocumentType.COMMENCE, "UPIS", null, null, null, "착수계", author);
        assertThat(saved.getDocType()).isEqualTo(DocumentType.COMMENCE);
        assertThat(saved.getSysType()).isEqualTo("UPIS");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(saved.getAuthor()).isSameAs(author);
        ArgumentCaptor<DocumentHistory> hist = ArgumentCaptor.forClass(DocumentHistory.class);
        verify(documentHistoryRepository).save(hist.capture());     // CREATE 이력 내용 단언
        assertThat(hist.getValue().getAction()).isEqualTo("CREATE");
        assertThat(hist.getValue().getActor()).isSameAs(author);
        assertThat(hist.getValue().getDocument()).isSameAs(saved);
    }

    // ===== changeStatus =====

    @Test
    void changeStatus_changesAndRecordsHistory() {
        Document d = new Document();
        d.setStatus(DocumentStatus.DRAFT);
        when(documentRepository.findById(5)).thenReturn(Optional.of(d));
        User actor = new User();
        Document out = service.changeStatus(5, DocumentStatus.COMPLETED, actor, "완료");
        assertThat(out.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
        verify(documentHistoryRepository).save(any(DocumentHistory.class));
    }

    // ===== saveSection (existing vs new) =====

    @Test
    void saveSection_updatesExistingSection() {
        Document d = new Document();
        DocumentDetail existing = new DocumentDetail();
        existing.setSectionKey("inspector");
        d.getDetails().add(existing);
        when(documentRepository.findById(7)).thenReturn(Optional.of(d));
        when(documentDetailRepository.save(any(DocumentDetail.class))).thenAnswer(i -> i.getArgument(0));

        DocumentDetail out = service.saveSection(7, "inspector", Map.of("k", "v"), 3);
        assertThat(out).isSameAs(existing);                 // 기존 섹션 재사용
        assertThat(out.getSectionData()).containsEntry("k", "v");
        assertThat(out.getSortOrder()).isEqualTo(3);
    }

    @Test
    void saveSection_createsNewSection() {
        Document d = new Document();
        when(documentRepository.findById(8)).thenReturn(Optional.of(d));
        when(documentDetailRepository.save(any(DocumentDetail.class))).thenAnswer(i -> i.getArgument(0));
        DocumentDetail out = service.saveSection(8, "newkey", Map.of("a", 1), null);
        assertThat(out.getSectionKey()).isEqualTo("newkey");
        assertThat(out.getSortOrder()).isEqualTo(0);        // null → 0
    }
}
