package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * doc-signed-scan-upload — 회사 PC 통합검증: 실제 D 드라이브(운영 저장 위치 D:\swmanager-scan)에서
 * 쓰기·교체·다운로드·삭제가 동작하는지 확인한다.
 *
 * 기존 {@link DocumentSignedScanServiceUploadTest} 는 @TempDir(보통 C:)로 서비스 전 로직을 커버하지만,
 * 운영은 별도 볼륨 D:\swmanager-scan 을 쓴다(exec-plan §B-4). 본 테스트는 동일 하네스(mock repo)로
 * scanDir 만 실제 D 드라이브 하위 임시폴더로 지정 → **운영DB 무접촉**으로 실 볼륨 동작만 검증한다.
 *
 * 게이트: RUN_DB_TESTS=true(회사 PC 마커) + D 드라이브 존재(assumeTrue). 끝나면 임시폴더 정리.
 */
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true",
        disabledReason = "회사 PC(실 D 드라이브) 전용. RUN_DB_TESTS=true 에서만 실행.")
class DocumentSignedScanServiceDDriveIntegrationTest {

    private Path scanBase;
    private DocumentRepository docRepo;
    private UserRepository userRepo;
    private DocumentSignedScanService service;

    @BeforeEach
    void setup() throws IOException {
        Path dRoot = Paths.get("D:\\swmanager-scan");
        assumeTrue(Files.isDirectory(dRoot) || dRoot.toFile().mkdirs(),
                "D:\\swmanager-scan 미존재 — 회사 PC 아님, 스킵");
        // 운영 위치 하위에 격리된 통합검증 임시폴더(실 D 볼륨 사용, 실데이터 무영향)
        scanBase = Files.createTempDirectory(dRoot, "__it_signed_scan_");

        docRepo = mock(DocumentRepository.class);
        userRepo = mock(UserRepository.class);
        service = new DocumentSignedScanService(docRepo, userRepo);
        ReflectionTestUtils.setField(service, "scanDir", scanBase.toString());
        when(docRepo.saveAndFlush(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(docRepo.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void cleanup() throws IOException {
        if (scanBase != null && Files.exists(scanBase)) {
            try (var walk = Files.walk(scanBase)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    private Document doc() {
        Document d = new Document();
        d.setDocType(DocumentType.COMMENCE);
        d.setStatus(DocumentStatus.COMPLETED);
        SwProject p = new SwProject();
        p.setProjId(108L);
        p.setProjNm("강진군UPIS유지보수");
        p.setYear(2026);
        p.setCityNm("전라남도");
        p.setDistNm("강진군");
        d.setProject(p);
        return d;
    }

    private MockMultipartFile pdf(String tag) {
        byte[] bytes = ("%PDF-1.7\n" + tag).getBytes(StandardCharsets.UTF_8);
        return new MockMultipartFile("file", "scan.pdf", "application/pdf", bytes);
    }

    /** 실 D 드라이브에 쓰기 → 교체(원자적 move) → 다운로드 → 삭제 end-to-end. */
    @Test
    void dDrive_uploadReplaceDownloadDelete_endToEnd() throws IOException {
        Document d = doc();
        when(docRepo.findById(1)).thenReturn(Optional.of(d));

        // 1) 업로드 — 실제 D 볼륨에 파일 생성
        Document afterUpload = service.uploadOrReplace(1, pdf("v1"), null);
        Path saved = Paths.get(afterUpload.getSignedScanPath());
        assertTrue(saved.toString().startsWith("D:\\"), "운영 D 볼륨에 저장돼야 함: " + saved);
        assertTrue(Files.exists(saved), "D 드라이브에 실제 파일이 있어야 함");
        assertTrue(saved.startsWith(scanBase.toAbsolutePath().normalize()), "base 하위여야 함");
        assertEquals("scan.pdf", afterUpload.getSignedScanOrigName());
        long firstSize = Files.size(saved);
        assertTrue(firstSize > 0);

        // 2) 교체 — 동일 논리경로 재업로드(원자적 move, 단일 현행 파일 유지)
        Document afterReplace = service.uploadOrReplace(1, pdf("v2-longer-content"), null);
        Path replaced = Paths.get(afterReplace.getSignedScanPath());
        assertEquals(saved, replaced, "동일 날짜·문서 → 동일 경로로 교체");
        assertTrue(Files.exists(replaced));
        try (var ls = Files.list(replaced.getParent())) {
            long current = ls.filter(p -> p.getFileName().toString().endsWith(".pdf")).count();
            assertEquals(1, current, "교체 후 현행 PDF 는 1개(임시/백업 잔존물 없음)");
        }
        assertTrue(new String(Files.readAllBytes(replaced), StandardCharsets.UTF_8).contains("v2-longer-content"),
                "교체본 내용으로 갱신돼야 함");

        // 3) 다운로드 — 실 파일 리소스 반환·내용 일치
        Resource res = service.loadForDownload(1);
        assertTrue(res.exists());
        try (var in = res.getInputStream()) {
            assertTrue(new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("v2-longer-content"));
        }

        // 4) 삭제 — 실 파일 제거 + 메타 null
        service.delete(1);
        assertFalse(Files.exists(replaced), "삭제 후 D 드라이브 파일이 없어야 함");
        assertNull(d.getSignedScanPath(), "삭제 후 경로 메타가 null 이어야 함");
    }
}
