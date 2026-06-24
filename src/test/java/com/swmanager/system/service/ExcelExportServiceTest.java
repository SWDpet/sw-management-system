package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.dto.DocumentDTO;
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

/**
 * ExcelExportService 단위테스트 (커버리지 상향 beyond-A, mock 무관·순수 빌더).
 * facade 위임(설계/기성)은 분리 서비스에서 검증되므로 제외. 본 클래스는 절사단위 정책 헬퍼와
 * 성과 리포트·문서 목록 xlsx 빌더를 POI 재오픈으로 구조 단언.
 */
class ExcelExportServiceTest {

    private ExcelExportService service;

    @BeforeEach
    void setUp() {
        service = new ExcelExportService();   // 성과/목록 빌더는 분리 서비스 의존 없음
    }

    private Workbook open(byte[] xlsx) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(xlsx));
    }

    // ===== 절사 단위 정책 헬퍼(static) =====

    @Test
    void normalizeRounddownUnit_nullEmptyZero_returnsZero() {
        assertThat(ExcelExportService.normalizeRounddownUnit(null)).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit("")).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit("   ")).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit(0)).isZero();
    }

    @Test
    void normalizeRounddownUnit_allowedUnits_passthrough_numberOrString() {
        assertThat(ExcelExportService.normalizeRounddownUnit(1000)).isEqualTo(1000);
        assertThat(ExcelExportService.normalizeRounddownUnit(10000)).isEqualTo(10000);
        assertThat(ExcelExportService.normalizeRounddownUnit(100000)).isEqualTo(100000);
        assertThat(ExcelExportService.normalizeRounddownUnit(1000000)).isEqualTo(1000000);
        assertThat(ExcelExportService.normalizeRounddownUnit("10000")).isEqualTo(10000);   // 문자열 파싱
    }

    @Test
    void normalizeRounddownUnit_disallowedOrUnparseable_fallsBackTo1000() {
        assertThat(ExcelExportService.normalizeRounddownUnit(9999)).isEqualTo(1000);   // 허용외
        assertThat(ExcelExportService.normalizeRounddownUnit("abc")).isEqualTo(1000);  // 파싱 실패
    }

    @Test
    void toRoundDigits_mapsUnitsAndDefault() {
        assertThat(ExcelExportService.toRoundDigits(1000)).isEqualTo(-3);
        assertThat(ExcelExportService.toRoundDigits(10000)).isEqualTo(-4);
        assertThat(ExcelExportService.toRoundDigits(100000)).isEqualTo(-5);
        assertThat(ExcelExportService.toRoundDigits(1000000)).isEqualTo(-6);
        assertThat(ExcelExportService.toRoundDigits(42)).isEqualTo(-3);   // default
    }

    @Test
    void roundLabel_mapsUnitsAndDefault() {
        assertThat(ExcelExportService.roundLabel(1000)).isEqualTo("백단위 절사");
        assertThat(ExcelExportService.roundLabel(10000)).isEqualTo("천단위 절사");
        assertThat(ExcelExportService.roundLabel(100000)).isEqualTo("만단위 절사");
        assertThat(ExcelExportService.roundLabel(1000000)).isEqualTo("십만단위 절사");
        assertThat(ExcelExportService.roundLabel(42)).isEqualTo("백단위 절사");   // default
    }

    // ===== generatePerformanceReport =====

    private PerformanceSummary ps(int year, int month, int install, int planTotal, int planOntime) {
        PerformanceSummary p = new PerformanceSummary();
        p.setPeriodYear(year);
        p.setPeriodMonth(month);
        p.setInstallCount(install);
        p.setPlanTotalCount(planTotal);
        p.setPlanOntimeCount(planOntime);
        return p;
    }

    @Test
    void generatePerformanceReport_writesTitleInfoHeaderAndData() throws IOException {
        Map<String, Object> totals = new HashMap<>();
        totals.put("installTotal", 5);
        totals.put("ontimeRate", 80);
        totals.put("inspectRate", 75);

        byte[] xlsx = service.generatePerformanceReport("홍길동", 2026, 1, 2026, 6,
                List.of(ps(2026, 1, 3, 10, 8)), totals);

        try (Workbook wb = open(xlsx)) {
            Sheet sh = wb.getSheet("성과 리포트");
            assertThat(sh).isNotNull();
            // ↓ row/cell 좌표·문자열은 generatePerformanceReport 의 레이아웃과 1:1 결합(빌더 변경 시 동반 갱신).
            assertThat(sh.getRow(0).getCell(0).getStringCellValue()).isEqualTo("(주)정도UIT SW지원부 성과 리포트");
            assertThat(sh.getRow(2).getCell(1).getStringCellValue()).isEqualTo("홍길동");          // 직원명
            assertThat(sh.getRow(2).getCell(4).getStringCellValue()).isEqualTo("2026년 1월 ~ 2026년 6월");
            // 데이터 행: 기간(row5 col0) + 일정준수율 8/10=80%
            assertThat(sh.getRow(5).getCell(0).getStringCellValue()).isEqualTo("2026년 1월");
            assertThat(sh.getRow(5).getCell(9).getStringCellValue()).isEqualTo("80%");
        }
    }

    @Test
    void generatePerformanceReport_nullUserDetailsTotals_stillProduces() throws IOException {
        byte[] xlsx = service.generatePerformanceReport(null, 2026, 1, 2026, 6, null, null);
        try (Workbook wb = open(xlsx)) {
            Sheet sh = wb.getSheet("성과 리포트");
            assertThat(sh.getRow(2).getCell(1).getStringCellValue()).isEqualTo("-");   // userName null → "-"
            // details null → 데이터 행 없이 헤더(row4) 직후 합계행(row5) 배치
            assertThat(sh.getRow(5).getCell(0).getStringCellValue()).isEqualTo("합계");
        }
    }

    // ===== generateDocumentList =====

    private DocumentDTO docDto(String docNo, String title, String author, String createdAt) {
        return DocumentDTO.builder()
                .docNo(docNo).docType(DocumentType.COMMENCE)
                .cityNm("강원도").distNm("춘천시").sysNm("UPIS")
                .title(title).authorName(author).createdAt(createdAt)
                .build();
    }

    @Test
    void generateDocumentList_writesHeaderAndDescendingRows() throws IOException {
        byte[] xlsx = service.generateDocumentList(List.of(
                docDto("D-1", "착수계A", "김작성", "2026-06-01T10:00:00"),
                docDto("D-2", "착수계B", "이작성", "2026-06-02")));

        try (Workbook wb = open(xlsx)) {
            Sheet sh = wb.getSheet("사업문서 목록");
            assertThat(sh).isNotNull();
            assertThat(sh.getRow(0).getCell(0).getStringCellValue()).isEqualTo("No");
            assertThat(sh.getRow(0).getCell(8).getStringCellValue()).isEqualTo("작성일");
            // 첫 데이터행: No = total(2) - 0 = 2, 문서번호 D-1, 작성일 10자리 절단
            assertThat(sh.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(2.0);
            assertThat(sh.getRow(1).getCell(1).getStringCellValue()).isEqualTo("D-1");
            assertThat(sh.getRow(1).getCell(8).getStringCellValue()).isEqualTo("2026-06-01");   // T 이후 절단
            assertThat(sh.getRow(2).getCell(0).getNumericCellValue()).isEqualTo(1.0);
            assertThat(sh.getRow(2).getCell(8).getStringCellValue()).isEqualTo("2026-06-02");   // 이미 10자 → 무절단 경로
        }
    }

    @Test
    void generateDocumentList_empty_headerOnly() throws IOException {
        try (Workbook wb = open(service.generateDocumentList(List.of()))) {
            Sheet sh = wb.getSheet("사업문서 목록");
            assertThat(sh.getRow(0).getCell(0).getStringCellValue()).isEqualTo("No");
            assertThat(sh.getRow(1)).isNull();   // 데이터 행 없음
        }
    }
}
