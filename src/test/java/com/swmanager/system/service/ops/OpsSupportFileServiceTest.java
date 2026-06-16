package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentDetail;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentDetailRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ops-support-doc-upload — 업무지원 지원문서 저장 서비스 단위테스트.
 * (@TempDir 를 base 로 사용 — 실 D드라이브 불필요)
 */
class OpsSupportFileServiceTest {

    @TempDir Path tempDir;
    OpsDocumentRepository docRepo;
    OpsDocumentDetailRepository detailRepo;
    SigunguCodeRepository sigunguRepo;
    SysMstRepository sysMstRepo;
    UserRepository userRepo;
    OpsSupportFileService service;

    @BeforeEach
    void setup() {
        docRepo = mock(OpsDocumentRepository.class);
        detailRepo = mock(OpsDocumentDetailRepository.class);
        sigunguRepo = mock(SigunguCodeRepository.class);
        sysMstRepo = mock(SysMstRepository.class);
        userRepo = mock(UserRepository.class);
        service = new OpsSupportFileService(docRepo, detailRepo, sigunguRepo, sysMstRepo, userRepo);
        ReflectionTestUtils.setField(service, "scanDir", tempDir.toString());
        when(docRepo.saveAndFlush(any(OpsDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(docRepo.save(any(OpsDocument.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private OpsDocument supportDoc() {
        OpsDocument d = new OpsDocument();
        d.setDocId(1L);
        d.setDocType(OpsDocType.SUPPORT);
        d.setDocNo("SUP-2026-42");
        d.setSysType("UPIS");
        d.setRegionCode("51150");
        d.setCreatedAt(LocalDateTime.of(2025, 3, 4, 0, 0));
        return d;
    }

    private void mockSection(Map<String, Object> data) {
        OpsDocumentDetail detail = new OpsDocumentDetail();
        detail.setSectionKey("main");
        detail.setSectionData(data);
        when(detailRepo.findByDocument_DocIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of(detail));
    }

    private void mockSectionDefault() {
        Map<String, Object> m = new HashMap<>();
        m.put("support_target", "○○시 사업수행계획서");
        m.put("request_date", "2026-05-10");
        mockSection(m);
    }

    private void mockRegionSystem() {
        SigunguCode s = new SigunguCode();
        s.setAdmSectC("51150"); s.setSidoNm("강원특별자치도"); s.setSggNm("춘천시");
        when(sigunguRepo.findById("51150")).thenReturn(Optional.of(s));
        SysMst sys = new SysMst(); sys.setCd("UPIS"); sys.setNm("표준시스템");
        when(sysMstRepo.findById("UPIS")).thenReturn(Optional.of(sys));
    }

    private static byte[] withMagic(byte[] magic, int total) {
        byte[] b = new byte[Math.max(magic.length, total)];
        System.arraycopy(magic, 0, b, 0, magic.length);
        return b;
    }
    private static final byte[] M_PDF  = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] M_ZIP  = { 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00 };
    private static final byte[] M_OLE2 = { (byte)0xD0,(byte)0xCF,0x11,(byte)0xE0,(byte)0xA1,(byte)0xB1,0x1A,(byte)0xE1 };

    private MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, null, content);
    }

    @Test
    void upload_success_pathAndMeta() throws IOException {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();

        OpsDocument out = service.uploadOrReplace(1L, file("plan.hwpx", withMagic(M_ZIP, 64)), null);

        assertNotNull(out.getSupportFilePath());
        Path saved = Paths.get(out.getSupportFilePath());
        assertTrue(Files.exists(saved));
        assertTrue(saved.startsWith(tempDir.toAbsolutePath().normalize()));
        String sep = java.io.File.separator;
        assertTrue(out.getSupportFilePath().contains(
                String.join(sep, "업무지원", "2026", "표준시스템", "강원특별자치도", "춘천시")),
                "경로: " + out.getSupportFilePath());
        assertTrue(out.getSupportFilePath().endsWith("○○시 사업수행계획서_SUP-2026-42.hwpx"),
                "파일명: " + out.getSupportFilePath());
        assertEquals("hwpx", out.getSupportFileExt());
        assertEquals("plan.hwpx", out.getSupportFileOrigName());
        verify(docRepo).saveAndFlush(any(OpsDocument.class));
    }

    @Test
    void upload_acceptsAllAllowedFormats() throws IOException {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();

        assertNotNull(service.uploadOrReplace(1L, file("a.pdf",  withMagic(M_PDF, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.hwpx", withMagic(M_ZIP, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.xlsx", withMagic(M_ZIP, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.docx", withMagic(M_ZIP, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.hwp",  withMagic(M_OLE2, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.xls",  withMagic(M_OLE2, 32)), null).getSupportFilePath());
        assertNotNull(service.uploadOrReplace(1L, file("a.doc",  withMagic(M_OLE2, 32)), null).getSupportFilePath());
    }

    @Test
    void upload_rejects_extMagicMismatch() {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();
        // .pdf 인데 ZIP 시그니처
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(1L, file("x.pdf", withMagic(M_ZIP, 32)), null));
        // .hwpx 인데 PDF 시그니처
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(1L, file("x.hwpx", withMagic(M_PDF, 32)), null));
    }

    @Test
    void upload_rejects_disallowedExt() {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(1L, file("x.exe", withMagic(M_OLE2, 32)), null));
    }

    @Test
    void upload_rejects_emptyAndShort() {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(1L, file("x.pdf", new byte[0]), null)); // 0byte
        // OLE2 8바이트보다 짧은 .hwp
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(1L, file("x.hwp", new byte[]{(byte)0xD0,(byte)0xCF,0x11}), null));
    }

    @Test
    void upload_rejects_oversize() {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        MockMultipartFile big = new MockMultipartFile("file", "x.pdf", null, withMagic(M_PDF, 16)) {
            @Override public long getSize() { return 31L * 1024 * 1024; }
        };
        assertThrows(IllegalArgumentException.class, () -> service.uploadOrReplace(1L, big, null));
    }

    @Test
    void upload_typeGuard_rejectsNonSupport() {
        OpsDocument d = supportDoc();
        d.setDocType(OpsDocType.FAULT);
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        assertThrows(IllegalStateException.class,
                () -> service.uploadOrReplace(1L, file("a.pdf", withMagic(M_PDF, 16)), null));
    }

    @Test
    void upload_fallbackFolders_whenMissing() throws IOException {
        OpsDocument d = supportDoc();
        d.setSysType(null); d.setRegionCode(null);
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        // section 없음 → support_target/request_date null → 지원대상미상, createdAt(2025) 연도
        when(detailRepo.findByDocument_DocIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());

        OpsDocument out = service.uploadOrReplace(1L, file("a.pdf", withMagic(M_PDF, 16)), null);
        String sep = java.io.File.separator;
        assertTrue(out.getSupportFilePath().contains(
                String.join(sep, "업무지원", "2025", "시스템미상", "시도미상", "시군구미상")),
                "fallback 경로: " + out.getSupportFilePath());
        assertTrue(out.getSupportFilePath().endsWith("지원대상미상_SUP-2026-42.pdf"));
    }

    @Test
    void upload_replace_deletesOldDifferentPath() throws IOException {
        OpsDocument d = supportDoc();
        Path dir = tempDir.resolve("업무지원").resolve("2020");
        Files.createDirectories(dir);
        Path old = dir.resolve("이전_SUP-2026-42.pdf");
        Files.write(old, withMagic(M_PDF, 8));
        d.setSupportFilePath(old.toString());
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();

        service.uploadOrReplace(1L, file("new.hwpx", withMagic(M_ZIP, 32)), null);
        assertFalse(Files.exists(old), "교체 시 이전(다른 경로) 파일 삭제");
    }

    @Test
    void delete_removesFileAndMeta() throws IOException {
        OpsDocument d = supportDoc();
        Path dir = tempDir.resolve("업무지원").resolve("2026");
        Files.createDirectories(dir);
        Path f = dir.resolve("a_SUP-2026-42.pdf");
        Files.write(f, withMagic(M_PDF, 8));
        d.setSupportFilePath(f.toString());
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));

        service.delete(1L);
        assertFalse(Files.exists(f));
        assertNull(d.getSupportFilePath());
    }

    @Test
    void download_rejects_outsideBase() {
        OpsDocument d = supportDoc();
        d.setSupportFilePath(tempDir.getParent().resolve("evil-" + System.nanoTime() + ".pdf").toString());
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        assertThrows(SecurityException.class, () -> service.loadForDownload(1L));
    }

    @Test
    void helpers_extNormalize_and_magic() {
        assertEquals("pdf", OpsSupportFileService.extOf("REPORT.PDF"));
        assertEquals("hwpx", OpsSupportFileService.extOf("a.HwPx"));
        assertEquals("", OpsSupportFileService.extOf("noext"));
        assertTrue(OpsSupportFileService.magicOk("pdf", withMagic(M_PDF, 8)));
        assertTrue(OpsSupportFileService.magicOk("xlsx", withMagic(M_ZIP, 8)));
        assertTrue(OpsSupportFileService.magicOk("xls", withMagic(M_OLE2, 8)));
        assertFalse(OpsSupportFileService.magicOk("pdf", withMagic(M_ZIP, 8)));
        assertFalse(OpsSupportFileService.magicOk("hwp", new byte[]{(byte)0xD0,(byte)0xCF})); // 짧음
        assertEquals("지원대상미상", OpsSupportFileService.seg("  ///  ", "지원대상미상"));
    }
}
