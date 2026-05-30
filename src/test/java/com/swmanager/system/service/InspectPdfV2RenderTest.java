package com.swmanager.system.service;

import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.repository.InspectReportRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 점검내역서 v2(HTML→PDF 재설계) 렌더 검증 — 임시 검증용 (기획서: docs/exec-plans/inspect-report-redesign.md).
 *
 * <p>인증/세션을 건드리지 않고(maximumSessions(1) 회피) {@link InspectPdfService#generatePdf} 를
 * 직접 호출해 실 DB 의 report 중 점검결과가 가장 많은 건으로 PDF 를 생성, target/inspect-v2.pdf 로 저장한다.
 * <p>로컬 프로파일(dev DB 211.104.137.55:5881) 사용. DB 미도달 시 skip.
 */
@SpringBootTest
@ActiveProfiles("local")
class InspectPdfV2RenderTest {

    @Autowired private InspectReportRepository reportRepository;
    @Autowired private InspectReportService inspectReportService;
    @Autowired private InspectPdfService inspectPdfService;

    @Test
    @Transactional  // 웹앱 OSIV 와 동등 — 뷰 렌더 중 lazy 컬렉션(Infra.servers) 초기화 허용
    void renderV2Pdf() throws Exception {
        var reports = reportRepository.findAll();
        Assumptions.assumeFalse(reports.isEmpty(), "DB 에 점검 report 가 없음 — skip");

        Long chosen = null;
        int best = -1;
        for (var r : reports) {
            try {
                InspectReportDTO dto = inspectReportService.findById(r.getId());
                List<?> cr = dto.getCheckResults();
                int n = (cr == null) ? 0 : cr.size();
                if (n > best) { best = n; chosen = r.getId(); }
            } catch (Exception ignore) { /* 손상 report skip */ }
        }
        Assumptions.assumeTrue(chosen != null, "렌더 가능한 report 없음 — skip");
        System.out.println("[V2TEST] chosen reportId=" + chosen + " checkResults=" + best);

        String html = inspectPdfService.renderToHtmlV2(chosen);
        byte[] pdf = inspectPdfService.generatePdf(chosen);

        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/inspect-v2.html"), html);
        Files.write(Path.of("target/inspect-v2.pdf"), pdf);
        System.out.println("[V2TEST] html.len=" + html.length() + " pdf.bytes=" + pdf.length
                + " → target/inspect-v2.pdf");

        assertTrue(pdf.length > 3000, "PDF 가 비정상적으로 작음: " + pdf.length);
        assertEquals('%', (char) pdf[0]);  // %PDF 헤더
        assertEquals('P', (char) pdf[1]);

        // 실제 PDF 페이지를 PDFBox 로 래스터화 (OpenHtmlToPdf 출력 그대로 — Edge 미리보기와 다름)
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.pdmodel.PDDocument.load(pdf)) {
            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(doc);
            int pages = Math.min(doc.getNumberOfPages(), 6);
            for (int p = 0; p < pages; p++) {
                java.awt.image.BufferedImage img = renderer.renderImageWithDPI(p, 110);
                javax.imageio.ImageIO.write(img, "png", new java.io.File("target/pdf-page-" + (p + 1) + ".png"));
            }
            System.out.println("[V2TEST] rasterized " + pages + " page(s) → target/pdf-page-N.png");
        }
    }
}
