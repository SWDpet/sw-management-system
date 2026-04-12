package com.swmanager.system.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.dto.DocumentDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * PDF 출력 서비스
 * - Thymeleaf HTML 렌더링 → OpenHTMLToPDF로 PDF 변환
 * - 한글 폰트 지원 (NotoSansKR)
 */
@Slf4j
@Service
public class PdfExportService {

    @Autowired private TemplateEngine templateEngine;
    @Autowired private DocumentService documentService;

    private static final String COMPANY_NAME = "(주)정도UIT";
    private static final String COMPANY_TEL = "T 02)561-7117";
    private static final String COMPANY_FAX = "F 02)561-9792";
    private static final String COMPANY_ADDR = "서울특별시 강남구 테헤란로 38길 29 5F";

    /**
     * 문서를 HTML 문자열로 렌더링 (PDF 변환용)
     */
    public String renderDocumentToHtml(Integer docId) {
        Document doc = documentService.getDocumentById(docId);
        DocumentDTO dto = DocumentDTO.fromEntity(doc);

        Context context = new Context();
        context.setVariable("doc", dto);
        context.setVariable("document", doc);
        context.setVariable("companyName", COMPANY_NAME);
        context.setVariable("companyTel", COMPANY_TEL);
        context.setVariable("companyFax", COMPANY_FAX);
        context.setVariable("companyAddr", COMPANY_ADDR);

        Map<String, Map<String, Object>> sectionMap = new LinkedHashMap<>();
        if (doc.getDetails() != null) {
            doc.getDetails().stream()
                .sorted(Comparator.comparingInt(d -> d.getSortOrder() != null ? d.getSortOrder() : 0))
                .forEach(detail -> sectionMap.put(detail.getSectionKey(), detail.getSectionData()));
        }
        context.setVariable("sections", sectionMap);
        context.setVariable("signatures", doc.getSignatures());

        String templateName = switch (doc.getDocType()) {
            case "COMMENCE" -> "pdf/pdf-commence";
            case "INTERIM" -> "pdf/pdf-interim";
            case "COMPLETION" -> "pdf/pdf-completion";
            case "INSPECT" -> "pdf/pdf-inspect";
            case "FAULT" -> "pdf/pdf-fault";
            case "SUPPORT" -> "pdf/pdf-support";
            case "INSTALL" -> "pdf/pdf-install";
            case "PATCH" -> "pdf/pdf-patch";
            default -> "pdf/pdf-default";
        };

        return templateEngine.process(templateName, context);
    }

    /**
     * HTML을 PDF 바이트 배열로 변환 (OpenHTMLToPDF)
     */
    public byte[] convertHtmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // 한글 폰트 등록 (classpath에 있는 경우)
            try {
                ClassPathResource fontResource = new ClassPathResource("fonts/NotoSansKR-Regular.ttf");
                if (fontResource.exists()) {
                    try (InputStream fontStream = fontResource.getInputStream()) {
                        builder.useFont(() -> fontStream, "Noto Sans KR");
                    }
                }
            } catch (Exception e) {
                log.debug("한글 폰트 로드 실패, 기본 폰트 사용: {}", e.getMessage());
            }

            builder.withHtmlContent(html, "/");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF 변환 실패", e);
            throw new RuntimeException("PDF 변환 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 문서 ID로 바로 PDF 바이트 생성
     */
    public byte[] generatePdf(Integer docId) {
        String html = renderDocumentToHtml(docId);
        return convertHtmlToPdf(html);
    }

    /**
     * 한글 금액 변환 (서버사이드)
     */
    public static String convertToKoreanAmount(Long amount) {
        if (amount == null || amount == 0) return "영";
        String[] units = {"", "만", "억", "조"};
        String[] nums = {"영", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};

        List<String> parts = new ArrayList<>();
        int unitIdx = 0;
        long remaining = amount;

        while (remaining > 0) {
            int part = (int)(remaining % 10000);
            if (part > 0) {
                StringBuilder sb = new StringBuilder();
                int thousands = part / 1000;
                if (thousands > 0) sb.append(nums[thousands]).append("천");
                int hundreds = (part % 1000) / 100;
                if (hundreds > 0) sb.append(nums[hundreds]).append("백");
                int tens = (part % 100) / 10;
                if (tens > 0) sb.append(nums[tens]).append("십");
                int ones = part % 10;
                if (ones > 0) sb.append(nums[ones]);
                sb.append(units[unitIdx]);
                parts.add(0, sb.toString());
            }
            remaining /= 10000;
            unitIdx++;
        }
        return "금 " + String.join("", parts) + "원정";
    }
}
