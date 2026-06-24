package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import com.swmanager.system.repository.ops.OpsDocumentAttachmentRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
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
import static org.mockito.Mockito.*;

/**
 * OpsDocAttachmentService 단위테스트 (커버리지 상향 beyond-A, mock 기반·실 파일은 @TempDir).
 * 생성자 주입 + @Value 디렉토리는 ReflectionTestUtils 로 임시폴더 주입. 업로드 디스크 기록·
 * 메타데이터 영속·삭제 시 파일+row 제거 커버.
 */
class OpsDocAttachmentServiceTest {

    private final OpsDocumentAttachmentRepository attachmentRepository = mock(OpsDocumentAttachmentRepository.class);
    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);

    @TempDir
    Path tempDir;

    private OpsDocAttachmentService service;

    @BeforeEach
    void setUp() {
        service = new OpsDocAttachmentService(attachmentRepository, opsDocumentRepository);
        ReflectionTestUtils.setField(service, "opsUploadDir", tempDir.toString());
        when(attachmentRepository.save(any(OpsDocumentAttachment.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void saveAttachment_docNotFound_throws() {
        when(opsDocumentRepository.findById(9L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("f", "a.pdf", "application/pdf", new byte[]{1, 2});
        assertThatThrownBy(() -> service.saveAttachment(9L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void saveAttachment_writesFileToDisk_andPersistsMetadata() throws IOException {
        when(opsDocumentRepository.findById(1L)).thenReturn(Optional.of(new OpsDocument()));
        byte[] content = "PDFDATA".getBytes();
        MockMultipartFile file = new MockMultipartFile("f", "보고서.pdf", "application/pdf", content);

        OpsDocumentAttachment att = service.saveAttachment(1L, file);

        assertThat(att.getFileName()).isEqualTo("보고서.pdf");
        assertThat(att.getFilePath()).endsWith(".pdf");        // UUID + 원본 확장자
        assertThat(att.getFileSize()).isEqualTo((long) content.length);
        assertThat(att.getMimeType()).isEqualTo("application/pdf");
        // 실제 디스크 기록 확인
        Path written = Paths.get(att.getFilePath());
        assertThat(Files.exists(written)).isTrue();
        assertThat(Files.readAllBytes(written)).isEqualTo(content);
        assertThat(written.startsWith(tempDir)).isTrue();      // 지정 업로드 폴더 하위
        verify(attachmentRepository).save(att);                // 메타데이터 영속 호출 확인
    }

    @Test
    void saveAttachment_noExtension_savesWithoutExt() throws IOException {
        when(opsDocumentRepository.findById(1L)).thenReturn(Optional.of(new OpsDocument()));
        MockMultipartFile file = new MockMultipartFile("f", "noext", "text/plain", new byte[]{9});

        OpsDocumentAttachment att = service.saveAttachment(1L, file);

        assertThat(att.getFileName()).isEqualTo("noext");
        // 확장자 없음 — 전체 경로가 아닌 파일명 컴포넌트만 검사(tempDir 경로의 dot 오탐 회피)
        assertThat(Paths.get(att.getFilePath()).getFileName().toString()).doesNotContain(".");
        assertThat(Files.exists(Paths.get(att.getFilePath()))).isTrue();
    }

    @Test
    void getAttachments_delegates() {
        List<OpsDocumentAttachment> list = List.of(new OpsDocumentAttachment());
        when(attachmentRepository.findByDocument_DocIdOrderByUploadedAtDesc(2L)).thenReturn(list);
        assertThat(service.getAttachments(2L)).isSameAs(list);
    }

    @Test
    void deleteAttachment_removesFileAndRow() throws IOException {
        Path f = Files.createFile(tempDir.resolve("todelete.bin"));
        OpsDocumentAttachment att = new OpsDocumentAttachment();
        att.setFilePath(f.toString());
        when(attachmentRepository.findById(5L)).thenReturn(Optional.of(att));

        service.deleteAttachment(5L);

        assertThat(Files.exists(f)).isFalse();        // 파일 삭제
        verify(attachmentRepository).deleteById(5L);  // row 삭제
    }

    @Test
    void deleteAttachment_notFound_throws() {
        when(attachmentRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteAttachment(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
