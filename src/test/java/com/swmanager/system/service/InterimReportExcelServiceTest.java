package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;
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
 * InterimReportExcelService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * documentService.getDocumentById 만 mock + classpath 실 템플릿(.xlsx)으로 기성내역서를 생성한 뒤
 * POI 로 재오픈해 구조 단언(golden 대신 structural). maint_type 별 SW/HW 템플릿 분기 + 예외 커버.
 */
class InterimReportExcelServiceTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private InterimReportExcelService service;

    @BeforeEach
    void setUp() {
        service = new InterimReportExcelService(documentService, messages);
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    private Map<String, Object> item(long unitPrice) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "DBMS");
        m.put("unitPrice", unitPrice);
        return m;
    }

    /**
     * detail_sheet(+inspector) 섹션과 maint_type 을 가진 INTERIM 문서를 docId 로 stub.
     * @param detailExtra detail_sheet 에 덮어쓸 키(없으면 기본 셋)
     * @param inspector   inspector 섹션(null 이면 미포함)
     */
    private void stubDoc(int docId, String maintType, Map<String, Object> detailExtra, Map<String, Object> inspector) {
        SwProject p = new SwProject();
        p.setProjNm("강릉시 GIS 유지보수");
        p.setDistNm("강릉시");
        p.setYear(2026);
        p.setContAmt(100_000_000L);
        p.setMaintType(maintType);

        Document doc = new Document();
        doc.setProject(p);

        Map<String, Object> detail = new HashMap<>();
        detail.put("bidRate", 94.0);
        detail.put("interimMonths", 6.0);
        detail.put("completionMonths", 6.0);
        detail.put("items", List.of(item(50_000_000L), item(30_000_000L)));
        detail.put("periodText", "2026-01-01 ~ 2026-06-30");
        detail.put("prevRate", 30.0);
        detail.put("truncDigit", 3.0);
        if (detailExtra != null) detail.putAll(detailExtra);
        doc.getDetails().add(section("detail_sheet", detail));

        if (inspector != null) doc.getDetails().add(section("inspector", inspector));

        when(documentService.getDocumentById(docId)).thenReturn(doc);
    }

    private DocumentDetail section(String key, Map<String, Object> data) {
        DocumentDetail d = new DocumentDetail();
        d.setSectionKey(key);
        d.setSectionData(data);
        return d;
    }

    private Map<String, Object> inspectorAmount(long paymentAmount, int round) {
        Map<String, Object> m = new HashMap<>();
        m.put("paymentAmount", paymentAmount);
        m.put("paymentRound", (double) round);
        return m;
    }

    private Workbook open(byte[] xlsx) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(xlsx));
    }

    // ===== SW 템플릿 (maint_type 비-HW) =====

    @Test
    void generate_swTemplate_fillsCoverAndContractAmount() throws IOException {
        stubDoc(1, null, null, inspectorAmount(5_000_000L, 2));
        try (Workbook wb = open(service.generateInterimReport(1))) {
            // ↓ 좌표·값은 interim_template.xlsx 의 fill 타깃과 1:1 결합(템플릿 변경 시 동반 갱신).
            assertThat(wb.getSheet("표지").getRow(4).getCell(0).getStringCellValue())   // A5 사업명
                    .isEqualTo("강릉시 GIS 유지보수");
            // 기성 내역서: 계약금액(1억) 표기
            assertThat(wb.getSheet("기성 내역서").getRow(3).getCell(0).getStringCellValue())
                    .contains("100,000,000");
        }
    }

    @Test
    void generate_swTemplate_paymentAmountDerivedFromRate() throws IOException {
        // paymentAmount 없음 + paymentRate 50% + contAmt 1억 → 5천만으로 보정되는 분기를 실제 셀로 검증
        Map<String, Object> insp = new HashMap<>();
        insp.put("paymentRate", 50.0);
        stubDoc(2, null, null, insp);
        try (Workbook wb = open(service.generateInterimReport(2))) {
            // 기성 내역서 '바. 금회기성신청금액' 행에 파생값 50,000,000 표기 확인
            assertThat(wb.getSheet("기성 내역서").getRow(7).getCell(0).getStringCellValue())
                    .contains("50,000,000");
        }
    }

    @Test
    void generate_swTemplate_defaultsWhenValuesMissing() throws IOException {
        // bidRate/interimMonths/completionMonths/periodText 제거 → 기본값(100/6/6) + buildPeriodText 분기
        Map<String, Object> bare = new HashMap<>();
        bare.put("bidRate", 0.0);
        bare.put("interimMonths", 0.0);
        bare.put("completionMonths", 0.0);
        bare.put("periodText", "");
        stubDoc(3, null, bare, null);   // inspector 없음(emptyMap 경로)
        try (Workbook wb = open(service.generateInterimReport(3))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
        }
    }

    // ===== HW 템플릿 (maint_type HS / DHS) =====

    @Test
    void generate_hwTemplate_HS() throws IOException {
        stubDoc(4, "HS", null, inspectorAmount(40_000_000L, 1));
        try (Workbook wb = open(service.generateInterimReport(4))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            assertThat(wb.getSheetAt(0).getPhysicalNumberOfRows()).isPositive();
        }
    }

    @Test
    void generate_hwTemplate_DHS_outOfRangeTruncDigit() throws IOException {
        // maint_type DHS + truncDigit 범위밖(9) → HW 경로 + 클램프 분기 행사(예외 없이 생성).
        // 내부 클램프(9→3) 결과 셀은 HW 템플릿 좌표 미고정이라 단언하지 않고 분기 실행만 보장.
        Map<String, Object> extra = new HashMap<>();
        extra.put("truncDigit", 9.0);
        stubDoc(5, "DHS", extra, inspectorAmount(40_000_000L, 1));
        try (Workbook wb = open(service.generateInterimReport(5))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            assertThat(wb.getSheetAt(0).getPhysicalNumberOfRows()).isPositive();
        }
    }

    // ===== 섹션 누락 예외 =====

    @Test
    void generate_noDetailSheetSection_throws() {
        Document doc = new Document();   // details 비어있음
        when(documentService.getDocumentById(9)).thenReturn(doc);
        when(messages.get(eq("error.export.performance_data_empty"))).thenReturn("기성 데이터 없음");

        assertThatThrownBy(() -> service.generateInterimReport(9))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기성 데이터 없음");
    }
}
