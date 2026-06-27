package com.swmanager.system.service;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.swmanager.system.service.ExcelStyleSupport.toDouble;
import static com.swmanager.system.service.ExcelExportService.normalizeRounddownUnit;

/**
 * 설계내역서(견적) Excel 생성 진입점 — ExcelExportService 에서 분리(excel-service-split §6-5 B).
 *
 * design_estimate 섹션을 조회·파싱하고 estimateType 으로 TYPE_A/B/C/D 를 dispatch 한다.
 * 실제 양식 생성(POI)은 {@link DesignEstimateWriter}(static, S4 거대클래스 #4 분리)로 위임한다.
 * 진입점 generateDesignEstimate 는 ExcelExportService 에 facade wrapper 로도 남아 있다(public API 유지).
 */
@Service
public class DesignEstimateExcelService {

    private final DocumentService documentService;
    private final MessageResolver messages;

    public DesignEstimateExcelService(DocumentService documentService, MessageResolver messages) {
        this.documentService = documentService;
        this.messages = messages;
    }

    @SuppressWarnings("unchecked")
    public byte[] generateDesignEstimate(Integer docId) throws IOException {
        Document doc = documentService.getDocumentById(docId);

        // design_estimate 섹션 데이터 조회
        Map<String, Object> estData = doc.getDetails().stream()
                .filter(d -> "design_estimate".equals(d.getSectionKey()))
                .findFirst()
                .map(DocumentDetail::getSectionData)
                .orElseThrow(() -> new IllegalStateException(messages.get("error.export.design_data_empty")));

        String estimateType = (String) estData.getOrDefault("estimateType", "TYPE_A");
        String designDate = (String) estData.getOrDefault("designDate", "");
        String location = (String) estData.getOrDefault("location", "");
        double bidRate = toDouble(estData.get("bidRate")) / 100.0;
        int rounddownUnit = normalizeRounddownUnit(estData.get("rounddownUnit"));
        boolean vatSeparate = Boolean.TRUE.equals(estData.get("vatSeparate"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) estData.getOrDefault("items", List.of());

        // 프로젝트명
        String projNm = doc.getProject() != null ? doc.getProject().getProjNm() : "사업명";
        String orgNm = doc.getProject() != null && doc.getProject().getOrgNm() != null ?
                doc.getProject().getOrgNm() : "";

        // HW/SW 항목 분리
        List<Map<String, Object>> hwItems = items.stream()
                .filter(i -> "HW".equals(i.get("type"))).toList();
        List<Map<String, Object>> swItems = items.stream()
                .filter(i -> "SW".equals(i.get("type"))).toList();

        // 프로젝트 추가 정보
        String distNm = doc.getProject() != null && doc.getProject().getDistNm() != null ?
                doc.getProject().getDistNm() : "";
        int year = doc.getProject() != null && doc.getProject().getYear() != null ?
                doc.getProject().getYear() : java.time.LocalDate.now().getYear();

        if ("TYPE_D".equals(estimateType)) {
            // TYPE_D: SW전용 (김해시/강릉시 등) - 템플릿 기반 생성
            return DesignEstimateWriter.generateFromSwTemplate(projNm, distNm, year, designDate, bidRate, swItems);
        }
        if ("TYPE_B".equals(estimateType)) {
            // TYPE_B: 중간형 (단양군 등) - 단양군 템플릿 기반
            return DesignEstimateWriter.generateFromTypeBTemplate(projNm, distNm, year, designDate, bidRate, rounddownUnit, hwItems, swItems);
        }
        if ("TYPE_C".equals(estimateType)) {
            // TYPE_C: 복합형 (김포시 등) - 김포시 템플릿 기반
            return DesignEstimateWriter.generateFromTypeCTemplate(projNm, distNm, year, designDate, bidRate, hwItems, swItems);
        }

        // TYPE_A: 기존 템플릿 기반 생성 (밀양시 형식)
        return DesignEstimateWriter.generateFromTemplate(projNm, distNm, year, designDate, location,
                bidRate, rounddownUnit, vatSeparate, hwItems, swItems);
    }
}
