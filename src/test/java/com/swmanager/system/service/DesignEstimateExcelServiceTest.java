package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DesignEstimateExcelService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * documentService.getDocumentById 만 mock 하고 classpath 의 실 템플릿(.xlsx)으로 설계내역서를
 * 생성한 뒤, 산출 바이트를 POI 로 다시 열어 시트·핵심 셀을 구조적으로 단언(golden 대신 structural).
 * TYPE_A/B/C/D 디스패치 전 경로 + 섹션 누락 예외를 커버.
 */
class DesignEstimateExcelServiceTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private DesignEstimateExcelService service;

    @BeforeEach
    void setUp() {
        service = new DesignEstimateExcelService(documentService, messages);
        // happy-path 에서 우발적 메시지 조회가 있어도 null 대신 키를 결정적으로 반환(셀 무음 null 회피).
        // 특정 키 stub(예외 테스트)이 우선하므로 충돌 없음.
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    private Map<String, Object> item(String type, String name, double rate, long unitPrice) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", type);
        m.put("name", name);
        m.put("rate", rate);
        m.put("unitPrice", unitPrice);
        return m;
    }

    /** 지정 estimateType + 대표 HW/SW 항목을 가진 design_estimate 문서를 docId 로 stub. */
    private void stubDoc(int docId, String estimateType, List<Map<String, Object>> items) {
        SwProject p = new SwProject();
        p.setProjNm("밀양시 GIS 유지보수");
        p.setDistNm("밀양시");
        p.setYear(2026);
        p.setOrgNm("밀양시청");

        Document doc = new Document();
        doc.setProject(p);

        DocumentDetail det = new DocumentDetail();
        det.setSectionKey("design_estimate");
        Map<String, Object> data = new HashMap<>();
        data.put("estimateType", estimateType);
        data.put("designDate", "2026-06-01");
        data.put("location", "경상남도 밀양시");
        data.put("bidRate", 97.0);          // /100 → 0.97
        data.put("rounddownUnit", 1000);
        data.put("vatSeparate", false);
        data.put("items", items);
        det.setSectionData(data);
        doc.getDetails().add(det);

        when(documentService.getDocumentById(docId)).thenReturn(doc);
    }

    private List<Map<String, Object>> sampleItems() {
        return List.of(
                item("HW", "DB서버", 8.0, 39201925L),
                item("HW", "AP서버", 8.0, 29352195L),
                item("SW", "DBMS", 16.0, 9160249L),
                item("SW", "GIS엔진", 16.0, 50233623L)
        );
    }

    private Workbook open(byte[] xlsx) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(xlsx));
    }

    // ===== TYPE_A (밀양시 양식, 4 시트) =====

    @Test
    void generate_typeA_fillsCoverAndSummary() throws IOException {
        stubDoc(1, "TYPE_A", sampleItems());

        byte[] xlsx = service.generateDesignEstimate(1);

        try (Workbook wb = open(xlsx)) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(4);   // 표지/갑지/총괄표/유지보수등급측정표
            // ↓ 좌표·값은 design_estimate_template.xlsx 의 fill 타깃과 1:1 결합(템플릿 변경 시 동반 갱신).
            // 표지(sheet0) A5(row4,col0) = 사업명
            assertThat(wb.getSheetAt(0).getRow(4).getCell(0).getStringCellValue())
                    .isEqualTo("밀양시 GIS 유지보수");
            // 총괄표(sheet2) HW 첫 항목 품명 B5(row4,col1)
            Sheet summary = wb.getSheetAt(2);
            assertThat(summary.getRow(4).getCell(1).getStringCellValue()).isEqualTo("DB서버");
        }
    }

    @Test
    void generate_defaultsToTypeA_whenTypeMissing() throws IOException {
        // estimateType 미지정 → getOrDefault("TYPE_A") 경로
        SwProject p = new SwProject();
        p.setProjNm("기본형 사업");
        Document doc = new Document();
        doc.setProject(p);
        DocumentDetail det = new DocumentDetail();
        det.setSectionKey("design_estimate");
        Map<String, Object> data = new HashMap<>();
        data.put("items", List.of());   // estimateType 없음
        det.setSectionData(data);
        doc.getDetails().add(det);
        when(documentService.getDocumentById(2)).thenReturn(doc);

        byte[] xlsx = service.generateDesignEstimate(2);

        try (Workbook wb = open(xlsx)) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(3);
            assertThat(wb.getSheetAt(0).getRow(4).getCell(0).getStringCellValue()).isEqualTo("기본형 사업");
        }
    }

    // ===== TYPE_B / TYPE_C / TYPE_D 디스패치 — 각 전용 템플릿 생성 경로 =====

    @Test
    void generate_typeB_producesValidWorkbook() throws IOException {
        stubDoc(3, "TYPE_B", sampleItems());
        try (Workbook wb = open(service.generateDesignEstimate(3))) {
            // dispatch smoke: 전용 템플릿이 로드돼 내용이 채워졌음을 확인(상세 셀 단언은 TYPE_A).
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            assertThat(wb.getSheetAt(0).getPhysicalNumberOfRows()).isPositive();
        }
    }

    @Test
    void generate_typeC_producesValidWorkbook() throws IOException {
        stubDoc(4, "TYPE_C", sampleItems());
        try (Workbook wb = open(service.generateDesignEstimate(4))) {
            // dispatch smoke: 전용 템플릿이 로드돼 내용이 채워졌음을 확인(상세 셀 단언은 TYPE_A).
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            assertThat(wb.getSheetAt(0).getPhysicalNumberOfRows()).isPositive();
        }
    }

    @Test
    void generate_typeD_usesSwTemplate() throws IOException {
        // TYPE_D = SW 전용 → generateFromSwTemplate
        stubDoc(5, "TYPE_D", List.of(
                item("SW", "DBMS", 16.0, 9160249L),
                item("SW", "GIS엔진", 16.0, 50233623L)));
        try (Workbook wb = open(service.generateDesignEstimate(5))) {
            // dispatch smoke: 전용 템플릿이 로드돼 내용이 채워졌음을 확인(상세 셀 단언은 TYPE_A).
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            assertThat(wb.getSheetAt(0).getPhysicalNumberOfRows()).isPositive();
        }
    }

    // ===== 섹션 누락 예외 =====

    @Test
    void generate_noDesignEstimateSection_throws() {
        Document doc = new Document();   // details 비어있음
        when(documentService.getDocumentById(9)).thenReturn(doc);
        when(messages.get(eq("error.export.design_data_empty"))).thenReturn("설계 데이터 없음");

        assertThatThrownBy(() -> service.generateDesignEstimate(9))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("설계 데이터 없음");   // 에러키 메시지 계약 확인
    }
}
