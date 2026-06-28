package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.User;
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
import org.springframework.core.io.Resource;
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
import java.util.stream.Stream;

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
        assertFalse(OpsSupportFileService.magicOk("txt", withMagic(M_PDF, 8)));   // 허용계열 외 → 기본 false
        assertEquals("지원대상미상", OpsSupportFileService.seg("  ///  ", "지원대상미상"));
    }

    // T7b: sectionValue 가 "main" 아닌 섹션만 있을 때 루프 통과→null(supportTarget 미상 fallback)
    @Test
    void upload_nonMainSectionOnly_supportTargetFallback() throws IOException {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        OpsDocumentDetail other = new OpsDocumentDetail();
        other.setSectionKey("other");
        other.setSectionData(new HashMap<>(Map.of("support_target", "무시될값")));
        when(detailRepo.findByDocument_DocIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of(other));
        mockRegionSystem();

        OpsDocument out = service.uploadOrReplace(1L, file("a.pdf", pdf((byte) 1)), null);
        // "main" 섹션 부재 → sectionValue 가 루프를 돌아 null 반환 → supportTarget "지원대상미상".
        // 파일명은 {supportTarget}_{docNo}.{ext} 이므로 docNo(SUP-2026-42)로 끝난다(연도 무관, 연도는 폴더).
        assertTrue(out.getSupportFilePath().endsWith("지원대상미상_SUP-2026-42.pdf"),
                "비-main 섹션 fallback: " + out.getSupportFilePath());
    }

    // ===== beyond-A 보강: 조회 성공/거부·uploaderSeq·동일경로 backup·not-found 람다 =====

    /** %PDF- 매직 + 구분용 마지막 바이트(내용 동일성 단언용). */
    private static byte[] pdf(byte tag) {
        byte[] b = new byte[20];
        System.arraycopy(M_PDF, 0, b, 0, M_PDF.length);
        b[19] = tag;
        return b;
    }

    // T1: originalName
    @Test
    void originalName_found_returnsOrigName() {
        OpsDocument d = supportDoc();
        d.setSupportFileOrigName("plan.hwpx");
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        assertEquals("plan.hwpx", service.originalName(1L));
    }

    @Test
    void originalName_notFound_returnsFallback() {
        when(docRepo.findById(9L)).thenReturn(Optional.empty());
        assertEquals("support-file", service.originalName(9L));
    }

    // T2: loadForDownload 성공 경로
    @Test
    void loadForDownload_success_returnsResource() throws IOException {
        OpsDocument d = supportDoc();
        Path dir = tempDir.resolve("업무지원").resolve("2026");
        Files.createDirectories(dir);
        Path f = dir.resolve("a_SUP-2026-42.pdf");
        Files.write(f, pdf((byte) 7));
        d.setSupportFilePath(f.toString());
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));

        Resource r = service.loadForDownload(1L);
        assertTrue(r.exists());
        assertEquals("a_SUP-2026-42.pdf", r.getFilename());
    }

    // T3: loadForDownload 거부(null·not-exists)
    @Test
    void loadForDownload_nullPath_throws() {
        OpsDocument d = supportDoc();
        d.setSupportFilePath(null);
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.loadForDownload(1L));
        assertTrue(ex.getMessage().contains("지원문서가 없습니다"));
    }

    @Test
    void loadForDownload_notExists_throws() {
        OpsDocument d = supportDoc();
        d.setSupportFilePath(tempDir.resolve("업무지원").resolve("2026").resolve("missing_SUP.pdf").toString());
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.loadForDownload(1L));
        assertTrue(ex.getMessage().contains("파일이 존재하지 않습니다"));
    }

    // T4: uploaderSeq != null → userRepository 조회 + setUploadedBy
    @Test
    void upload_uploaderSeq_setsUploadedBy() throws IOException {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();
        User u = new User();
        u.setUserSeq(5L);
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));

        OpsDocument out = service.uploadOrReplace(1L, file("a.pdf", pdf((byte) 1)), 5L);

        assertSame(u, out.getSupportFileUploadedBy());
        verify(userRepo).findById(5L);
    }

    // T5: 동일 target 경로 교체 → backup→원자이동→backup 삭제
    @Test
    void upload_sameTargetPath_backupRotate() throws IOException {
        OpsDocument d = supportDoc();
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        mockSectionDefault();
        mockRegionSystem();

        OpsDocument out1 = service.uploadOrReplace(1L, file("a.pdf", pdf((byte) 1)), null);
        Path target1 = Paths.get(out1.getSupportFilePath());

        // 동일 docId·동일 section/region·동일 ext → 동일 파일명 → 동일 target(backup 분기 진입)
        OpsDocument out2 = service.uploadOrReplace(1L, file("a.pdf", pdf((byte) 2)), null);
        Path target2 = Paths.get(out2.getSupportFilePath());

        assertEquals(target1, target2);                                  // 동일 target → backup 분기
        assertArrayEquals(pdf((byte) 2), Files.readAllBytes(target2));   // 원자이동: 최종 내용 = 2차 payload
        try (Stream<Path> s = Files.list(target2.getParent())) {
            assertEquals(0L, s.filter(p -> p.getFileName().toString().contains(".bak-")).count(),
                    "성공 후 .bak- 백업 잔존 없음");
        }
    }

    // T6: findById not-found orElseThrow 람다 3종
    @Test
    void upload_docNotFound_throws() {
        when(docRepo.findById(7L)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadOrReplace(7L, file("a.pdf", pdf((byte) 1)), null));
        assertTrue(ex.getMessage().contains("문서를 찾을 수 없습니다"));
    }

    @Test
    void delete_docNotFound_throws() {
        when(docRepo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.delete(7L));
    }

    @Test
    void download_docNotFound_throws() {
        when(docRepo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.loadForDownload(7L));
    }

    // T7: yearFolder "연도미상"(request_date 없음 + createdAt null)
    @Test
    void upload_yearFolder_unknown_whenNoDateAndNoCreatedAt() throws IOException {
        OpsDocument d = supportDoc();
        d.setCreatedAt(null);
        when(docRepo.findById(1L)).thenReturn(Optional.of(d));
        when(detailRepo.findByDocument_DocIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());

        OpsDocument out = service.uploadOrReplace(1L, file("a.pdf", pdf((byte) 1)), null);
        String sep = java.io.File.separator;
        assertTrue(out.getSupportFilePath().contains(sep + "연도미상" + sep),
                "연도미상 경로: " + out.getSupportFilePath());
    }
}
