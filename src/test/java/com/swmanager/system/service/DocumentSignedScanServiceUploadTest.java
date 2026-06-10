package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * doc-signed-scan-upload — 게이트(FR-8) / PDF검증(FR-2) / 업로드·교체·삭제 통합 단위테스트.
 * (@TempDir 를 base 로 사용 — 실 D드라이브 불필요, 집 PC 실행 가능)
 */
class DocumentSignedScanServiceUploadTest {

    @TempDir Path tempDir;
    DocumentRepository docRepo;
    UserRepository userRepo;
    DocumentSignedScanService service;

    @BeforeEach
    void setup() {
        docRepo = mock(DocumentRepository.class);
        userRepo = mock(UserRepository.class);
        service = new DocumentSignedScanService(docRepo, userRepo);
        ReflectionTestUtils.setField(service, "scanDir", tempDir.toString());
        when(docRepo.saveAndFlush(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(docRepo.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Document doc(DocumentType type, DocumentStatus status) {
        Document d = new Document();
        d.setDocType(type);
        d.setStatus(status);
        SwProject p = new SwProject();
        p.setProjId(108L);
        p.setProjNm("강진군UPIS유지보수");
        p.setYear(2026);
        p.setCityNm("전라남도");
        p.setDistNm("강진군");
        d.setProject(p);
        return d;
    }

    private MockMultipartFile pdf() {
        byte[] bytes = "%PDF-1.7\n test content".getBytes(StandardCharsets.US_ASCII);
        return new MockMultipartFile("file", "scan.pdf", "application/pdf", bytes);
    }

    @Test
    void upload_success_writesFileAndSetsMeta() throws IOException {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));

        Document out = service.uploadOrReplace(1, pdf(), null);

        assertNotNull(out.getSignedScanPath());
        Path saved = Paths.get(out.getSignedScanPath());
        assertTrue(Files.exists(saved), "파일이 저장돼야 함");
        assertTrue(saved.startsWith(tempDir.toAbsolutePath().normalize()), "base 하위여야 함");
        assertEquals("scan.pdf", out.getSignedScanOrigName());
        // 새 폴더 구조: {문서종류}/{사업연도}/{시도}/{시군구}/{사업명}_{문서종류}.pdf
        String sep = java.io.File.separator;
        assertTrue(out.getSignedScanPath().contains(
                String.join(sep, "착수계", "2026", "전라남도", "강진군")),
                "문서종류/연도/시도/시군구 경로여야 함: " + out.getSignedScanPath());
        assertTrue(out.getSignedScanPath().endsWith("강진군UPIS유지보수_착수계.pdf"),
                "파일명은 {사업명}_{문서종류}.pdf: " + out.getSignedScanPath());
        verify(docRepo).saveAndFlush(any(Document.class));
    }

    @Test
    void upload_nullProjectFields_usesFallbackFolders() throws IOException {
        Document d = new Document();
        d.setDocType(DocumentType.INTERIM);
        d.setStatus(DocumentStatus.DRAFT);
        SwProject p = new SwProject();
        p.setProjNm("연도지역미상사업"); // year/cityNm/distNm 모두 null
        d.setProject(p);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));

        Document out = service.uploadOrReplace(1, pdf(), null);

        String sep = java.io.File.separator;
        assertTrue(out.getSignedScanPath().contains(
                String.join(sep, "기성계", "연도미상", "시도미상", "시군구미상")),
                "null 필드는 미상 폴더로: " + out.getSignedScanPath());
        assertTrue(out.getSignedScanPath().endsWith("연도지역미상사업_기성계.pdf"));
    }

    @Test
    void upload_allowsDraftStatus() throws IOException {
        // 게이트 제거(2026-06-09): 사업문서는 상태 무관 업로드 허용
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.DRAFT);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        assertNotNull(service.uploadOrReplace(1, pdf(), null).getSignedScanPath());
    }

    @Test
    void upload_rejects_nonPdf() {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        MockMultipartFile notPdf = new MockMultipartFile("file", "x.pdf", "application/pdf",
                "PKnotapdf".getBytes(StandardCharsets.US_ASCII));
        assertThrows(IllegalArgumentException.class, () -> service.uploadOrReplace(1, notPdf, null));
    }

    @Test
    void upload_replace_deletesOldDifferentPath() throws IOException {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        Path dir = tempDir.resolve("108_강진군UPIS유지보수").resolve("착수계");
        Files.createDirectories(dir);
        Path old = dir.resolve("착수계_강진군UPIS유지보수_20000101.pdf");
        Files.write(old, "%PDF-old".getBytes(StandardCharsets.US_ASCII));
        d.setSignedScanPath(old.toString());
        when(docRepo.findById(1)).thenReturn(Optional.of(d));

        service.uploadOrReplace(1, pdf(), null);

        assertFalse(Files.exists(old), "교체 시 이전(다른 경로) 파일은 삭제돼야 함");
    }

    @Test
    void delete_removesFileAndMeta() throws IOException {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        Path dir = tempDir.resolve("108_x").resolve("착수계");
        Files.createDirectories(dir);
        Path f = dir.resolve("a.pdf");
        Files.write(f, "%PDF-x".getBytes(StandardCharsets.US_ASCII));
        d.setSignedScanPath(f.toString());
        when(docRepo.findById(1)).thenReturn(Optional.of(d));

        service.delete(1);

        assertFalse(Files.exists(f), "삭제 시 파일 제거");
        assertNull(d.getSignedScanPath(), "메타 null");
    }

    // === codex 2차 보강: FR-2 contentType/크기, FR-9 base 경계 ===

    @Test
    void upload_rejects_oversize() {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        MockMultipartFile big = new MockMultipartFile("file", "x.pdf", "application/pdf",
                "%PDF-".getBytes(StandardCharsets.US_ASCII)) {
            @Override public long getSize() { return 31L * 1024 * 1024; } // 30MB 초과(실 할당 없이)
        };
        assertThrows(IllegalArgumentException.class, () -> service.uploadOrReplace(1, big, null));
    }

    @Test
    void upload_accepts_octetStreamWithPdfMagic() throws IOException {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        MockMultipartFile f = new MockMultipartFile("file", "x.pdf", "application/octet-stream",
                "%PDF-1.4 x".getBytes(StandardCharsets.US_ASCII));
        assertNotNull(service.uploadOrReplace(1, f, null).getSignedScanPath());
    }

    @Test
    void upload_rejects_invalidContentType() {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        MockMultipartFile png = new MockMultipartFile("file", "x.png", "image/png",
                "%PDF-1.4 x".getBytes(StandardCharsets.US_ASCII)); // 매직은 PDF여도 MIME가 명시적 비-PDF면 차단
        assertThrows(IllegalArgumentException.class, () -> service.uploadOrReplace(1, png, null));
    }

    @Test
    void upload_accepts_nullContentType() throws IOException {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        MockMultipartFile f = new MockMultipartFile("file", "x.pdf", null,
                "%PDF-1.4 x".getBytes(StandardCharsets.US_ASCII)); // null 정책: 매직바이트로 허용
        assertNotNull(service.uploadOrReplace(1, f, null).getSignedScanPath());
    }

    @Test
    void download_rejects_outsideBase() {
        Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
        d.setSignedScanPath(tempDir.getParent().resolve("evil-" + System.nanoTime() + ".pdf").toString());
        when(docRepo.findById(1)).thenReturn(Optional.of(d));
        assertThrows(SecurityException.class, () -> service.loadForDownload(1));
    }

    @Test
    void delete_doesNotTouch_outsideBase() throws IOException {
        Path outside = Files.createTempFile("outside-scan", ".pdf");
        try {
            Files.write(outside, "%PDF-x".getBytes(StandardCharsets.US_ASCII));
            Document d = doc(DocumentType.COMMENCE, DocumentStatus.COMPLETED);
            d.setSignedScanPath(outside.toString());
            when(docRepo.findById(1)).thenReturn(Optional.of(d));

            service.delete(1);

            assertTrue(Files.exists(outside), "base 밖 파일은 삭제하지 않아야 함");
            assertNull(d.getSignedScanPath(), "메타는 정리");
        } finally {
            Files.deleteIfExists(outside);
        }
    }
}
