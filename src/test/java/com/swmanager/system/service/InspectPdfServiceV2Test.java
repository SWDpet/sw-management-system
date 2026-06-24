package com.swmanager.system.service;

import com.swmanager.system.domain.InspectMetricSnapshot;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import com.swmanager.system.repository.InspectTemplateRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.service.inspection.InspectMaintProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InspectPdfService.renderToHtmlV2 (v2 비주얼 재설계) 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 *
 * <p>v1 표 렌더는 {@link InspectPdfServiceTest} 가 담당. 본 클래스는 v2 의 집계배지·KPI·차트 data URI·
 * 점검범위 프로파일·섹션카드·핵심이슈 빌드를 captured {@link Context} 로 검증한다.
 * (외부 브라우저/openhtmltopdf 가 필요한 generatePdf 는 단위테스트 불가 — 제외.)</p>
 */
class InspectPdfServiceV2Test {

    private final InspectReportService inspectReportService = mock(InspectReportService.class);
    private final SwProjectRepository swProjectRepository = mock(SwProjectRepository.class);
    private final InfraRepository infraRepository = mock(InfraRepository.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final InspectMetricChartService metricChartService = mock(InspectMetricChartService.class);
    private final InspectMetricSnapshotRepository metricRepository = mock(InspectMetricSnapshotRepository.class);
    private final InspectTemplateRepository templateRepository = mock(InspectTemplateRepository.class);

    private InspectPdfService service;

    @BeforeEach
    void setUp() {
        service = new InspectPdfService();
        ReflectionTestUtils.setField(service, "inspectReportService", inspectReportService);
        ReflectionTestUtils.setField(service, "swProjectRepository", swProjectRepository);
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
        ReflectionTestUtils.setField(service, "templateEngine", templateEngine);
        ReflectionTestUtils.setField(service, "metricChartService", metricChartService);
        ReflectionTestUtils.setField(service, "metricRepository", metricRepository);
        ReflectionTestUtils.setField(service, "templateRepository", templateRepository);
        // plain mock() (MockitoExtension 미사용) → 미사용 stub 무해, lenient 불필요.
        when(infraRepository.findByDistNmAndSysNmEn(any(), any())).thenReturn(List.of());
        when(metricRepository.findRangeByPjtRole(anyLong(), anyString(), any(), any())).thenReturn(List.of());
        when(metricChartService.renderChart(anyLong(), any(), any())).thenReturn(null);
        when(templateEngine.process(eq("pdf/pdf-inspect-report-v2"), any(Context.class))).thenReturn("html-v2");
    }

    // ── 헬퍼 빌더 ────────────────────────────────────────────────────

    private InspectCheckResultDTO cr(String section, String code, String itemName,
                                     String resultText, String remarks, String itemMethod) {
        InspectCheckResultDTO c = new InspectCheckResultDTO();
        c.setSection(section);
        c.setResultCode(code);
        c.setItemName(itemName);
        c.setResultText(resultText);
        c.setRemarks(remarks);
        c.setItemMethod(itemMethod);
        return c;
    }

    private InspectCheckResultDTO cr(String section, String code) {
        return cr(section, code, "항목-" + section, null, null, null);
    }

    private InspectReportDTO report(Long pjtId, String sysType, String month,
                                    List<InspectCheckResultDTO> results) {
        InspectReportDTO r = new InspectReportDTO();
        r.setPjtId(pjtId);
        r.setSysType(sysType);
        r.setInspectMonth(month);
        r.setCheckResults(results);
        return r;
    }

    private InspectMetricSnapshot snap(String cpu, String mem, String disk, OffsetDateTime at) {
        InspectMetricSnapshot s = new InspectMetricSnapshot();
        if (cpu != null) s.setCpuPct(new BigDecimal(cpu));
        if (mem != null) s.setMemPct(new BigDecimal(mem));
        if (disk != null) s.setDiskPct(new BigDecimal(disk));
        s.setCollectedAt(at);
        return s;
    }

    /** renderToHtmlV2 호출 후 templateEngine 에 전달된 Context 를 캡처한다. */
    private Context renderAndCapture(Long reportId) {
        String out = service.renderToHtmlV2(reportId);
        assertThat(out).isEqualTo("html-v2");
        ArgumentCaptor<Context> cap = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("pdf/pdf-inspect-report-v2"), cap.capture());
        return cap.getValue();
    }

    // ── 집계 배지(codeClass 카운트) ──────────────────────────────────

    @Test
    void v2_badgeCounts_classifyByResultCode() {
        // 전체 프로파일(maintType null) → AP/DB/DBMS/GIS 섹션 모두 범위 내
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(
                cr("AP", "NORMAL"), cr("AP", "PARTIAL"),
                cr("DB", "ABNORMAL"), cr("GIS", "NOT_APPLICABLE"),
                cr("DBMS", null)));   // null 코드 → manual
        when(inspectReportService.findById(1L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(1L);
        assertThat(ctx.getVariable("cntNormal")).isEqualTo(1);
        assertThat(ctx.getVariable("cntCaution")).isEqualTo(1);
        assertThat(ctx.getVariable("cntViolation")).isEqualTo(1);
        assertThat(ctx.getVariable("cntManual")).isEqualTo(2);   // NOT_APPLICABLE + null
        assertThat(ctx.getVariable("cntTotal")).isEqualTo(5);
    }

    @Test
    void v2_nullCheckResults_zeroCounts() {
        when(inspectReportService.findById(2L)).thenReturn(report(11L, "UPIS", "2026-06", null));
        when(swProjectRepository.findById(11L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(2L);
        assertThat(ctx.getVariable("cntTotal")).isEqualTo(0);
        assertThat(ctx.getVariable("cntNormal")).isEqualTo(0);
        assertThat(ctx.getVariable("cards")).asList().isEmpty();
        assertThat(ctx.getVariable("issues")).asList().isEmpty();
    }

    // ── 섹션 카드(addCard: 필터·worst 랭크·게이지·desc) ──────────────

    @Test
    void v2_sectionCards_builtForScopeWithWorstStatusAndGauge() {
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(
                // AP: 정상 + 위반 → worst=bad(조치필요). 게이지(89.9%) 행 포함.
                cr("AP", "NORMAL", "CPU 사용률", "89.9 %", null, null),
                cr("AP", "ABNORMAL", "메모리", null, "스왑 과다", "ps -ef||기준 80%"),
                // GIS: 주의만 → worst=warn(주의)
                cr("GIS", "PARTIAL", "엔진 상태", "응답 지연", null, null)));
        when(inspectReportService.findById(3L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(3L);
        @SuppressWarnings("unchecked")
        List<InspectPdfService.SectionCard> cards =
                (List<InspectPdfService.SectionCard>) ctx.getVariable("cards");

        // 데이터 있는 섹션(AP/GIS)만 카드 생성. DB/DBMS 는 결과 없어 스킵.
        assertThat(cards).hasSize(2);
        InspectPdfService.SectionCard ap = cards.stream()
                .filter(c -> c.getTitle().equals("AP 서버")).findFirst().orElseThrow();
        assertThat(ap.getStatusClass()).isEqualTo("bad");           // worst = ABNORMAL
        assertThat(ap.getStatusLabel()).isEqualTo("조치필요");
        assertThat(ap.getRows()).hasSize(2);

        // 게이지 행: pctOf("89.9 %") = 90, descOf 는 측정값 노출
        InspectPdfService.CardRow gauge = ap.getRows().stream()
                .filter(r -> r.getLabel().equals("CPU 사용률")).findFirst().orElseThrow();
        assertThat(gauge.getPct()).isEqualTo(90);
        assertThat(gauge.getText()).isEqualTo("89.9 %");
        assertThat(gauge.getCodeLabel()).isEqualTo("정상");

        // 비게이지 행: resultText 없음 → remarks · itemMethod(|| → ·) 폴백
        InspectPdfService.CardRow mem = ap.getRows().stream()
                .filter(r -> r.getLabel().equals("메모리")).findFirst().orElseThrow();
        assertThat(mem.getPct()).isNull();
        assertThat(mem.getText()).isEqualTo("스왑 과다");           // remarks 우선
        assertThat(mem.getCodeLabel()).isEqualTo("조치필요");

        InspectPdfService.SectionCard gis = cards.stream()
                .filter(c -> c.getTitle().equals("GIS 엔진")).findFirst().orElseThrow();
        assertThat(gis.getStatusClass()).isEqualTo("warn");
    }

    @Test
    void v2_descFallsBackToItemMethod_whenNoTextOrRemarks() {
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(
                cr("GIS", "NORMAL", "헬스체크", "정상", null, "curl 호출||200 OK")));   // resultText=상태어 → 제외
        when(inspectReportService.findById(4L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(4L);
        @SuppressWarnings("unchecked")
        List<InspectPdfService.SectionCard> cards =
                (List<InspectPdfService.SectionCard>) ctx.getVariable("cards");
        InspectPdfService.CardRow row = cards.get(0).getRows().get(0);
        assertThat(row.getText()).isEqualTo("curl 호출 · 200 OK");   // || → · 치환, itemMethod 폴백
    }

    // ── 점검범위 프로파일(scope) ────────────────────────────────────

    @Test
    void v2_scope_swMaintType_limitsToGisOnly() {
        // maintType "SW" → GIS 만 범위. AP/DB/DBMS 데이터 있어도 카드 미생성.
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(
                cr("AP", "NORMAL"), cr("DB", "NORMAL"), cr("GIS", "NORMAL")));
        when(inspectReportService.findById(5L)).thenReturn(rpt);
        SwProject p = new SwProject();
        p.setDistNm("강릉시"); p.setSysNmEn("UPIS"); p.setMaintType("SW");
        when(swProjectRepository.findById(10L)).thenReturn(Optional.of(p));

        Context ctx = renderAndCapture(5L);
        @SuppressWarnings("unchecked")
        List<InspectPdfService.SectionCard> cards =
                (List<InspectPdfService.SectionCard>) ctx.getVariable("cards");
        assertThat(cards).extracting(InspectPdfService.SectionCard::getTitle)
                .containsExactly("GIS 엔진");
        assertThat(ctx.getVariable("maintBadge")).isEqualTo(InspectMaintProfile.badgeLabel("SW"));
        assertThat(ctx.getVariable("maintTone")).isEqualTo("teal");
        verify(infraRepository).findByDistNmAndSysNmEn("강릉시", "UPIS");   // 프로젝트 있음 → 인프라 조회
    }

    @Test
    void v2_scope_includesAppWhenStandardTemplateExists() {
        // maintType null(전체) + 표준템플릿 보유 → APP 섹션 포함
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(cr("APP", "NORMAL")));
        when(inspectReportService.findById(6L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());
        when(templateRepository.existsByTemplateTypeAndSectionAndUseYn("UPIS", InspectMaintProfile.APP, "Y"))
                .thenReturn(true);

        Context ctx = renderAndCapture(6L);
        @SuppressWarnings("unchecked")
        List<InspectPdfService.SectionCard> cards =
                (List<InspectPdfService.SectionCard>) ctx.getVariable("cards");
        assertThat(cards).extracting(InspectPdfService.SectionCard::getTitle)
                .contains("표준시스템");
    }

    // ── 핵심 이슈(bad/warn 만 수집) ─────────────────────────────────

    @Test
    void v2_issues_collectBadAndWarnRowsOnly() {
        InspectReportDTO rpt = report(10L, "UPIS", "2026-06", List.of(
                cr("AP", "NORMAL", "정상항목", null, null, null),
                cr("AP", "ABNORMAL", "위반항목", null, "조치 요망", null),
                cr("GIS", "PARTIAL", "주의항목", null, "관찰 필요", null)));
        when(inspectReportService.findById(7L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(7L);
        @SuppressWarnings("unchecked")
        List<InspectPdfService.CardRow> issues =
                (List<InspectPdfService.CardRow>) ctx.getVariable("issues");
        assertThat(issues).hasSize(2);
        assertThat(issues).extracting(InspectPdfService.CardRow::getLabel)
                .containsExactlyInAnyOrder("[AP 서버] 위반항목", "[GIS 엔진] 주의항목");
        assertThat(issues).extracting(InspectPdfService.CardRow::getCodeClass)
                .containsExactlyInAnyOrder("bad", "warn");
    }

    // ── KPI(computeKpi) ─────────────────────────────────────────────

    @Test
    void v2_kpi_aggregatesCpuMemAvgDiskMaxAndDistinctDays() {
        InspectReportDTO rpt = report(20L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(8L)).thenReturn(rpt);
        when(swProjectRepository.findById(20L)).thenReturn(Optional.empty());

        OffsetDateTime d1 = OffsetDateTime.parse("2026-06-01T10:00:00+09:00");
        OffsetDateTime d1b = OffsetDateTime.parse("2026-06-01T20:00:00+09:00");   // 같은 날
        OffsetDateTime d2 = OffsetDateTime.parse("2026-06-02T10:00:00+09:00");
        when(metricRepository.findRangeByPjtRole(eq(20L), eq("AP"), any(), any()))
                .thenReturn(List.of(
                        snap("40", "50", "60", d1),
                        snap("60", "70", "90", d1b)));   // disk 90 = max
        when(metricRepository.findRangeByPjtRole(eq(20L), eq("DB"), any(), any()))
                .thenReturn(List.of(snap("80", "30", "55", d2)));

        Context ctx = renderAndCapture(8L);
        InspectPdfService.Kpi kpi = (InspectPdfService.Kpi) ctx.getVariable("kpi");
        assertThat(kpi.getCpu()).isEqualTo("60%");    // (40+60+80)/3 = 60
        assertThat(kpi.getMem()).isEqualTo("50%");    // (50+70+30)/3 = 50
        assertThat(kpi.getDisk()).isEqualTo("90%");   // max(60,90,55)
        assertThat(kpi.getDays()).isEqualTo("2");     // 06-01, 06-02
    }

    @Test
    void v2_kpi_nonDivisibleAverage_roundsHalfUpToWholePercent() {
        // 평균이 정수로 안 떨어지는 케이스로 %.0f 반올림(내림·올림 양방향) 고정.
        InspectReportDTO rpt = report(20L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(18L)).thenReturn(rpt);
        when(swProjectRepository.findById(20L)).thenReturn(Optional.empty());
        OffsetDateTime d = OffsetDateTime.parse("2026-06-01T10:00:00+09:00");
        // cpu (33+33+34)/3 = 33.33 → "33%"(내림), mem (67+67+66)/3 = 66.67 → "67%"(올림)
        when(metricRepository.findRangeByPjtRole(eq(20L), eq("AP"), any(), any()))
                .thenReturn(List.of(snap("33", "67", "10", d), snap("33", "67", "20", d)));
        when(metricRepository.findRangeByPjtRole(eq(20L), eq("DB"), any(), any()))
                .thenReturn(List.of(snap("34", "66", "30", d)));

        Context ctx = renderAndCapture(18L);
        InspectPdfService.Kpi kpi = (InspectPdfService.Kpi) ctx.getVariable("kpi");
        assertThat(kpi.getCpu()).isEqualTo("33%");    // 33.33 반내림
        assertThat(kpi.getMem()).isEqualTo("67%");    // 66.67 반올림
        assertThat(kpi.getDisk()).isEqualTo("30%");   // max(10,20,30)
    }

    @Test
    void v2_kpi_emptySnapshots_returnsCollectingPlaceholder() {
        InspectReportDTO rpt = report(20L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(9L)).thenReturn(rpt);
        when(swProjectRepository.findById(20L)).thenReturn(Optional.empty());
        // findRangeByPjtRole → 기본 stub 으로 빈 리스트

        Context ctx = renderAndCapture(9L);
        InspectPdfService.Kpi kpi = (InspectPdfService.Kpi) ctx.getVariable("kpi");
        assertThat(kpi.getCpu()).isEqualTo("수집 대기");
        assertThat(kpi.getDays()).isEqualTo("0");
    }

    @Test
    void v2_kpi_nullPjtId_returnsCollectingPlaceholder() {
        InspectReportDTO rpt = report(null, "UPIS", "2026-06", List.of());   // pjtId null
        when(inspectReportService.findById(11L)).thenReturn(rpt);
        // null 계약 명시: pjtId null 이면 프로젝트 조회는 null 키로 호출되어 빈 결과 → project=null,
        // computeKpi/chartDataUri 는 pjtId null 가드로 메트릭/차트 조회를 건너뛴다.
        when(swProjectRepository.findById(isNull())).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(11L);
        InspectPdfService.Kpi kpi = (InspectPdfService.Kpi) ctx.getVariable("kpi");
        assertThat(kpi.getCpu()).isEqualTo("수집 대기");
        verify(swProjectRepository).findById(isNull());                       // 프로젝트 조회는 수행
        verify(metricRepository, never()).findRangeByPjtRole(any(), any(), any(), any());   // 메트릭은 가드로 스킵
        verify(metricChartService, never()).renderChart(any(), any(), any());              // 차트도 가드로 스킵
    }

    @Test
    void v2_kpi_partialNullMetrics_showsDashForMissingMetric() {
        InspectReportDTO rpt = report(20L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(12L)).thenReturn(rpt);
        when(swProjectRepository.findById(20L)).thenReturn(Optional.empty());
        OffsetDateTime d = OffsetDateTime.parse("2026-06-01T10:00:00+09:00");
        // cpu 만 존재, mem/disk null → mem/disk "-"
        when(metricRepository.findRangeByPjtRole(eq(20L), eq("AP"), any(), any()))
                .thenReturn(List.of(snap("42", null, null, d)));

        Context ctx = renderAndCapture(12L);
        InspectPdfService.Kpi kpi = (InspectPdfService.Kpi) ctx.getVariable("kpi");
        assertThat(kpi.getCpu()).isEqualTo("42%");
        assertThat(kpi.getMem()).isEqualTo("-");
        assertThat(kpi.getDisk()).isEqualTo("-");
    }

    // ── 차트 data URI(chartDataUri) ─────────────────────────────────

    @Test
    void v2_chart_bytesEncodedAsDataUri() {
        InspectReportDTO rpt = report(30L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(13L)).thenReturn(rpt);
        when(swProjectRepository.findById(30L)).thenReturn(Optional.empty());
        when(metricChartService.renderChart(eq(30L), any(), any()))
                .thenReturn(new byte[]{1, 2, 3});

        Context ctx = renderAndCapture(13L);
        assertThat((String) ctx.getVariable("chartImg"))
                .isEqualTo("data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));
    }

    @Test
    void v2_chart_emptyBytes_yieldsNull() {
        InspectReportDTO rpt = report(30L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(14L)).thenReturn(rpt);
        when(swProjectRepository.findById(30L)).thenReturn(Optional.empty());
        when(metricChartService.renderChart(eq(30L), any(), any())).thenReturn(new byte[0]);

        Context ctx = renderAndCapture(14L);
        assertThat(ctx.getVariable("chartImg")).isNull();
    }

    @Test
    void v2_chart_renderThrows_swallowedToNull() {
        InspectReportDTO rpt = report(30L, "UPIS", "2026-06", List.of());
        when(inspectReportService.findById(15L)).thenReturn(rpt);
        when(swProjectRepository.findById(30L)).thenReturn(Optional.empty());
        when(metricChartService.renderChart(eq(30L), any(), any()))
                .thenThrow(new RuntimeException("render boom"));

        Context ctx = renderAndCapture(15L);
        assertThat(ctx.getVariable("chartImg")).isNull();   // catch → null
    }

    // ── inspectMonthKo 포맷 + sysType 기본값 ────────────────────────

    @Test
    void v2_inspectMonthKo_formatsYearMonth() {
        InspectReportDTO rpt = report(10L, null, "2026-06", List.of());   // sysType null → UPIS 기본
        when(inspectReportService.findById(16L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(16L);
        assertThat(ctx.getVariable("inspectMonthKo")).isEqualTo("2026년 6월");
        // sysType null → 표준템플릿 조회 시 "UPIS" 기본값 사용
        verify(templateRepository).existsByTemplateTypeAndSectionAndUseYn(
                eq("UPIS"), eq(InspectMaintProfile.APP), eq("Y"));
    }

    @Test
    void v2_inspectMonthKo_blankMonth_returnsDash() {
        InspectReportDTO rpt = report(10L, "UPIS", null, List.of());   // month null
        when(inspectReportService.findById(17L)).thenReturn(rpt);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.empty());

        Context ctx = renderAndCapture(17L);
        assertThat(ctx.getVariable("inspectMonthKo")).isEqualTo("-");
    }
}
