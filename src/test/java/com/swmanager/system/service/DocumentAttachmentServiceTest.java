package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentAttachment;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.workplan.DocumentAttachmentRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DocumentAttachmentService 단위테스트 (커버리지 상향 beyond-A, mock 기반·실 파일은 @TempDir).
 * 필드 주입이라 ReflectionTestUtils 로 mock + @Value 업로드 디렉토리 주입. 업로드 디스크 기록·
 * 메타데이터 영속·삭제 시 파일+row 제거 커버.
 */
class DocumentAttachmentServiceTest {

    private final DocumentAttachmentRepository attachmentRepository = mock(DocumentAttachmentRepository.class);
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    @TempDir
    Path tempDir;

    private DocumentAttachmentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentAttachmentService();
        ReflectionTestUtils.setField(service, "attachmentRepository", attachmentRepository);
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "messages", messages);
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        when(attachmentRepository.save(any(DocumentAttachment.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void saveAttachment_docNotFound_throws() {
        when(documentRepository.findById(9)).thenReturn(Optional.empty());
        when(messages.get(eq("error.document.not_found"), any())).thenReturn("문서 없음: 9");
        MockMultipartFile file = new MockMultipartFile("f", "a.pdf", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> service.saveAttachment(9, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9");
    }

    @Test
    void saveAttachment_writesFileToDisk_andPersistsMetadata() throws IOException {
        when(documentRepository.findById(1)).thenReturn(Optional.of(new Document()));
        byte[] content = "DATA".getBytes();
        MockMultipartFile file = new MockMultipartFile("f", "첨부.hwp", "application/x-hwp", content);

        DocumentAttachment att = service.saveAttachment(1, file);

        assertThat(att.getFileName()).isEqualTo("첨부.hwp");
        assertThat(att.getFilePath()).endsWith(".hwp");
        assertThat(att.getFileSize()).isEqualTo((long) content.length);
        assertThat(att.getMimeType()).isEqualTo("application/x-hwp");
        Path written = Paths.get(att.getFilePath());
        assertThat(Files.exists(written)).isTrue();
        assertThat(Files.readAllBytes(written)).isEqualTo(content);
        assertThat(written.startsWith(tempDir)).isTrue();
        verify(attachmentRepository).save(att);                // 메타데이터 영속 호출 확인
    }

    @Test
    void getAttachments_delegates() {
        List<DocumentAttachment> list = List.of(new DocumentAttachment());
        when(attachmentRepository.findByDocument_DocId(2)).thenReturn(list);
        assertThat(service.getAttachments(2)).isSameAs(list);
    }

    @Test
    void deleteAttachment_removesFileAndRow() throws IOException {
        Path f = Files.createFile(tempDir.resolve("todelete.bin"));
        DocumentAttachment att = new DocumentAttachment();
        att.setFilePath(f.toString());
        when(attachmentRepository.findById(5)).thenReturn(Optional.of(att));

        service.deleteAttachment(5);

        assertThat(Files.exists(f)).isFalse();
        verify(attachmentRepository).deleteById(5);
    }

    @Test
    void deleteAttachment_notFound_throws() {
        when(attachmentRepository.findById(9)).thenReturn(Optional.empty());
        when(messages.get("error.attachment.not_found")).thenReturn("첨부 없음");
        assertThatThrownBy(() -> service.deleteAttachment(9))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
