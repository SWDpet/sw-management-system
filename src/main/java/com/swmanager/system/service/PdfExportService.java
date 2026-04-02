package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.dto.DocumentDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * PDF 출력 서비스
 * - 문서 세트를 하나의 HTML로 렌더링 후 PDF 변환
 * - 레터헤드/직인 이미지 자동 삽입
 * - 문서 세트 병합 출력 지원
 */
@Service
public class PdfExportService {

    @Autowired private TemplateEngine templateEngine;
    @Autowired private DocumentService documentService;

    // 정도UIT 회사 정보 (레터헤드)
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

        // 섹션 데이터를 맵으로 변환
        Map<String, Map<String, Object>> sectionMap = new HashMap<>();
        if (doc.getDetails() != null) {
            for (DocumentDetail detail : doc.getDetails()) {
                sectionMap.put(detail.getSectionKey(), detail.getSectionData());
            }
        }
        context.setVariable("sections", sectionMap);

        // 서명 정보
        context.setVariable("signatures", doc.getSignatures());

        // 문서 유형별 PDF 템플릿 선택
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
     * 레터헤드 HTML 헤더 생성
     */
    public String getLetterheadHtml() {
        return "<div style='text-align:center; padding:20px; border-bottom:3px solid #1565c0;'>" +
               "<div style='font-size:24px; font-weight:bold; color:#1565c0;'>" + COMPANY_NAME + "</div>" +
               "<div style='font-size:11px; color:#666; margin-top:5px;'>" +
               COMPANY_ADDR + " | " + COMPANY_TEL + " | " + COMPANY_FAX + "</div>" +
               "</div>";
    }

    /**
     * 직인 이미지 HTML
     */
    public String getSealHtml() {
        return "<div style='text-align:right; margin-top:30px;'>" +
               "<div style='display:inline-block; border:2px solid #c00; border-radius:50%; width:60px; height:60px; " +
               "line-height:60px; text-align:center; color:#c00; font-weight:bold; font-size:11px;'>대표이사<br>직인</div>" +
               "</div>";
    }

    /**
     * 문서번호 자동 채번 텍스트
     */
    public String formatDocNo(String docNo) {
        return docNo != null ? docNo : "";
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
