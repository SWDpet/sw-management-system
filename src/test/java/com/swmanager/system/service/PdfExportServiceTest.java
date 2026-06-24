package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.i18n.MessageResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PdfExportService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입. 한글 금액 변환(순수 분기)·HTML 렌더(템플릿 분기)·
 * 실 PDF 변환(OpenHTMLToPDF → %PDF 매직바이트) 커버.
 */
class PdfExportServiceTest {

    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private PdfExportService service;

    private static final String VALID_XHTML =
            "<html><head><title>t</title></head><body><p>안녕하세요</p></body></html>";

    @BeforeEach
    void setUp() {
        service = new PdfExportService();
        ReflectionTestUtils.setField(service, "templateEngine", templateEngine);
        ReflectionTestUtils.setField(service, "documentService", documentService);
        ReflectionTestUtils.setField(service, "messages", messages);
    }

    // ===== convertToKoreanAmount (순수 static) =====

    @Test
    void convertToKoreanAmount_nullOrZero_returnsYeong() {
        assertThat(PdfExportService.convertToKoreanAmount(null)).isEqualTo("영");
        assertThat(PdfExportService.convertToKoreanAmount(0L)).isEqualTo("영");
    }

    @Test
    void convertToKoreanAmount_variousMagnitudes() {
        assertThat(PdfExportService.convertToKoreanAmount(1L)).isEqualTo("금 일원정");
        assertThat(PdfExportService.convertToKoreanAmount(50_000_000L)).isEqualTo("금 오천만원정");
        assertThat(PdfExportService.convertToKoreanAmount(100_000_000L)).isEqualTo("금 일억원정");
        // 천·백·십·일 + 만 단위 결합
        assertThat(PdfExportService.convertToKoreanAmount(12_345L)).isEqualTo("금 일만이천삼백사십오원정");
    }

    // ===== renderDocumentToHtml =====

    @Test
    void renderDocumentToHtml_usesDocTypePdfTemplate() {
        Document doc = new Document();
        doc.setDocType(DocumentType.COMMENCE);                 // → "pdf/pdf-commence"
        when(documentService.getDocumentById(1)).thenReturn(doc);
        when(templateEngine.process(eq("pdf/pdf-commence"), any(Context.class)))
                .thenReturn("<html>COMMENCE</html>");

        assertThat(service.renderDocumentToHtml(1)).isEqualTo("<html>COMMENCE</html>");
    }

    @Test
    void renderDocumentToHtml_nullDocType_usesDefaultTemplate() {
        Document doc = new Document();                          // docType null → "pdf/pdf-default"
        when(documentService.getDocumentById(2)).thenReturn(doc);
        when(templateEngine.process(eq("pdf/pdf-default"), any(Context.class)))
                .thenReturn("<html>DEFAULT</html>");

        assertThat(service.renderDocumentToHtml(2)).isEqualTo("<html>DEFAULT</html>");
    }

    // ===== convertHtmlToPdf / generatePdf (실 PDF 렌더) =====

    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    @Test
    void convertHtmlToPdf_producesPdfBytes() {
        byte[] pdf = service.convertHtmlToPdf(VALID_XHTML);
        assertThat(pdf).isNotEmpty().startsWith(PDF_MAGIC);   // PDF 매직 시그니처(바이트 비교)
    }

    @Test
    void generatePdf_rendersTemplateThenConvertsToPdf() {
        Document doc = new Document();
        doc.setDocType(DocumentType.COMMENCE);
        when(documentService.getDocumentById(3)).thenReturn(doc);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(VALID_XHTML);

        byte[] pdf = service.generatePdf(3);
        assertThat(pdf).isNotEmpty().startsWith(PDF_MAGIC);
    }

    @Test
    void convertHtmlToPdf_invalidHtml_throwsWrappedException() {
        when(messages.get(eq("error.export.pdf_conversion"), any())).thenReturn("PDF 변환 실패");
        // 닫히지 않은 태그 = 비정상 XHTML → OpenHTMLToPDF 파싱 실패 → RuntimeException 래핑
        assertThatThrownBy(() -> service.convertHtmlToPdf("<html><body><p>broken"))
                .isInstanceOf(RuntimeException.class);
    }
}
