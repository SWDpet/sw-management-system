package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HwpxExportService.generateHwpx 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * documentService 만 mock + classpath 실 HWPX 템플릿으로 생성→결과가 zip(PK 시그니처)인지 +
 * 템플릿 경로 분기(resolveTemplatePath)·치환맵(buildReplacements) 전 templateType + 예외 커버.
 */
class HwpxExportServiceGenerateTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private HwpxExportService service;

    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};   // "PK\x03\x04"

    @BeforeEach
    void setUp() {
        service = new HwpxExportService();
        ReflectionTestUtils.setField(service, "documentService", documentService);
        ReflectionTestUtils.setField(service, "messages", messages);
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    private Document doc(DocumentType docType, String... sectionKeys) {
        SwProject p = new SwProject();
        p.setProjNm("강릉시 GIS 유지보수");
        p.setCityNm("강원도");
        p.setDistNm("강릉시");
        p.setOrgNm("강릉시청");
        p.setOrgLghNm("강릉시장");
        Document d = new Document();
        d.setProject(p);
        d.setDocType(docType);
        d.setDocNo("D-2026-001");
        for (String key : sectionKeys) {
            DocumentDetail det = new DocumentDetail();
            det.setSectionKey(key);
            det.setSectionData(new HashMap<>(Map.of("manager", "홍길동", "tel", "010-0000-0000", "date", "2026-06-01")));
            d.getDetails().add(det);
        }
        return d;
    }

    private void assertHwpxZip(byte[] out) {
        assertThat(out).isNotEmpty().startsWith(ZIP_MAGIC);   // HWPX = OOXML zip
    }

    /** HWPX(zip) 내 Contents/section0.xml 본문 추출 — 치환 반영 검증용. */
    private String readSection0(byte[] hwpx) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(hwpx))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if ("Contents/section0.xml".equals(e.getName())) {
                    return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return "";
    }

    // ===== letter (docType 분기) =====

    @Test
    void generate_letter_commence_substitutesProjName() throws IOException {
        when(documentService.getDocumentById(1)).thenReturn(doc(DocumentType.COMMENCE, "letter"));
        byte[] out = service.generateHwpx(1, "letter");
        assertHwpxZip(out);
        // 치환 반영 검증: {{용역명}} → projNm 이 실제 section0.xml 에 들어갔는지(빈 zip/무치환 회귀 차단)
        assertThat(readSection0(out)).contains("강릉시 GIS 유지보수");
    }

    @Test
    void generate_letter_interim() {
        when(documentService.getDocumentById(2)).thenReturn(doc(DocumentType.INTERIM, "letter"));
        assertHwpxZip(service.generateHwpx(2, "letter"));
    }

    @Test
    void generate_letter_completion() {
        when(documentService.getDocumentById(3)).thenReturn(doc(DocumentType.COMPLETION, "letter"));
        assertHwpxZip(service.generateHwpx(3, "letter"));
    }

    // ===== 기타 templateType =====

    @Test
    void generate_inspector() {
        when(documentService.getDocumentById(4)).thenReturn(doc(DocumentType.INTERIM, "inspector"));
        assertHwpxZip(service.generateHwpx(4, "inspector"));
    }

    @Test
    void generate_completionBody_kras() {
        when(documentService.getDocumentById(6)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(6, "completion_body"));
    }

    @Test
    void generate_completionBody_upis() {
        when(documentService.getDocumentById(7)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(7, "completion_body_upis"));
    }

    @Test
    void generate_completionFull() {
        when(documentService.getDocumentById(8)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(8, "completion_full"));
    }

    // ===== 예외 경로 (resolveTemplatePath) =====

    @Test
    void generate_letter_nullDocType_throws() {
        when(documentService.getDocumentById(9)).thenReturn(doc(null, "letter"));
        assertThatThrownBy(() -> service.generateHwpx(9, "letter"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_unknownTemplateType_throws() {
        when(documentService.getDocumentById(10)).thenReturn(doc(DocumentType.COMMENCE, "letter"));
        assertThatThrownBy(() -> service.generateHwpx(10, "no_such_type"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
