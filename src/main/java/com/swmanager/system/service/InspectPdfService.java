package com.swmanager.system.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.swmanager.system.domain.InspectMetricSnapshot;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import com.swmanager.system.repository.InspectTemplateRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.service.inspection.InspectMaintProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class InspectPdfService {

    @Autowired private TemplateEngine templateEngine;
    @Autowired private InspectReportService inspectReportService;
    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private InfraRepository infraRepository;
    @Autowired private InspectMetricChartService metricChartService;
    @Autowired private InspectMetricSnapshotRepository metricRepository;
    @Autowired private InspectTemplateRepository templateRepository;

    private File fontFile;

    /** resultText 끝의 "…NN%" 또는 "NN.N %" 형태에서 퍼센트 값 추출용. */
    private static final Pattern PCT_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%");

    // ──────────────────────────────────────────────────────────────
    //  v1 (레거시 표 중심) — 롤백 안전망으로 유지
    // ──────────────────────────────────────────────────────────────
    public String renderToHtml(Long reportId) {
        InspectReportDTO report = inspectReportService.findById(reportId);
        var project = swProjectRepository.findById(report.getPjtId()).orElse(null);

        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("project", project);

        // 점검결과를 섹션별로 분리하여 컨텍스트에 추가
        if (report.getCheckResults() != null) {
            context.setVariable("dbItems", report.getCheckResults().stream().filter(r -> "DB".equals(r.getSection())).toList());
            context.setVariable("apItems", report.getCheckResults().stream().filter(r -> "AP".equals(r.getSection())).toList());
            context.setVariable("dbmsItems", report.getCheckResults().stream().filter(r -> "DBMS".equals(r.getSection())).toList());
            context.setVariable("gisItems", report.getCheckResults().stream().filter(r -> "GIS".equals(r.getSection())).toList());
            context.setVariable("appItems", report.getCheckResults().stream().filter(r -> "APP".equals(r.getSection())).toList());
            context.setVariable("dbUsage", report.getCheckResults().stream().filter(r -> "DB_USAGE".equals(r.getSection())).toList());
            context.setVariable("apUsage", report.getCheckResults().stream().filter(r -> "AP_USAGE".equals(r.getSection())).toList());
            context.setVariable("dbmsEtc", report.getCheckResults().stream().filter(r -> "DBMS_ETC".equals(r.getSection())).toList());
            context.setVariable("appEtc", report.getCheckResults().stream().filter(r -> "APP_ETC".equals(r.getSection())).toList());
        }

        if (project != null) {
            var infraList = infraRepository.findByDistNmAndSysNmEn(
                    project.getDistNm(), project.getSysNmEn());
            context.setVariable("infraList", infraList);
        }

        String templateType = report.getSysType() != null ? report.getSysType() : "UPIS";
        List<InspectCheckResultDTO> templateItems = inspectReportService.getTemplateItems(templateType);
        context.setVariable("templateItems", templateItems);

        return templateEngine.process("pdf/pdf-inspect-report", context);
    }

    // ──────────────────────────────────────────────────────────────
    //  v2 (비주얼 재설계 — 표지/대시보드/섹션카드/부록)
    //  기획서: docs/exec-plans/inspect-report-redesign.md
    // ──────────────────────────────────────────────────────────────
    public String renderToHtmlV2(Long reportId) {
        InspectReportDTO report = inspectReportService.findById(reportId);
        var project = swProjectRepository.findById(report.getPjtId()).orElse(null);

        List<InspectCheckResultDTO> results = report.getCheckResults() != null
                ? report.getCheckResults() : List.of();

        Context ctx = new Context();
        ctx.setVariable("report", report);
        ctx.setVariable("project", project);
        ctx.setVariable("inspectMonthKo", formatMonthKo(report.getInspectMonth()));

        // 집계 배지 — 정상/주의/위반/수동
        int normal = 0, caution = 0, violation = 0, manual = 0;
        for (InspectCheckResultDTO r : results) {
            switch (codeClass(r.getResultCode())) {
                case "ok"   -> normal++;
                case "warn" -> caution++;
                case "bad"  -> violation++;
                default      -> manual++;
            }
        }
        ctx.setVariable("cntNormal", normal);
        ctx.setVariable("cntCaution", caution);
        ctx.setVariable("cntViolation", violation);
        ctx.setVariable("cntManual", manual);
        ctx.setVariable("cntTotal", results.size());

        // KPI (30일 누적 스냅샷)
        ctx.setVariable("kpi", computeKpi(report.getPjtId()));

        // 30일 추이 차트 — PNG → base64 data URI
        ctx.setVariable("chartImg", chartDataUri(report.getPjtId()));

        // 점검범위 프로파일 (maint_type + 표준보유) — 기획서 inspect-maint-profile.md
        String maintType   = project != null ? project.getMaintType() : null;
        String templateType = report.getSysType() != null ? report.getSysType() : "UPIS";
        boolean hasStandard = templateRepository
                .existsByTemplateTypeAndSectionAndUseYn(templateType, InspectMaintProfile.APP, "Y");
        Set<String> scope = InspectMaintProfile.sections(maintType, hasStandard);
        ctx.setVariable("maintBadge", InspectMaintProfile.badgeLabel(maintType));
        ctx.setVariable("maintTone",  InspectMaintProfile.badgeTone(maintType));
        ctx.setVariable("scopeChip",  InspectMaintProfile.scopeChip(maintType, hasStandard));

        // 섹션 카드 — 점검범위 내 섹션만(lean). 범위 밖 섹션은 데이터가 있어도 비표시.
        List<SectionCard> cards = new ArrayList<>();
        if (scope.contains(InspectMaintProfile.AP))   addCard(cards, results, "AP",   "AP 서버");
        if (scope.contains(InspectMaintProfile.DB))   addCard(cards, results, "DB",   "DB 서버 (OS)");
        if (scope.contains(InspectMaintProfile.DBMS)) addCard(cards, results, "DBMS", "DBMS (Oracle)");
        if (scope.contains(InspectMaintProfile.GIS))  addCard(cards, results, "GIS",  "GIS 엔진");
        if (scope.contains(InspectMaintProfile.APP))  addCard(cards, results, "APP",  "표준시스템");
        ctx.setVariable("cards", cards);

        // 핵심 이슈 (위반/주의 항목)
        List<CardRow> issues = new ArrayList<>();
        for (SectionCard c : cards) {
            for (CardRow row : c.getRows()) {
                if ("bad".equals(row.getCodeClass()) || "warn".equals(row.getCodeClass())) {
                    issues.add(new CardRow("[" + c.getTitle() + "] " + row.getLabel(),
                            row.getCodeLabel(), row.getCodeClass(), row.getText(), null));
                }
            }
        }
        ctx.setVariable("issues", issues);

        // 점검대상 사양 (UPIS 인프라)
        if (project != null) {
            ctx.setVariable("infraList", infraRepository.findByDistNmAndSysNmEn(
                    project.getDistNm(), project.getSysNmEn()));
        }

        return templateEngine.process("pdf/pdf-inspect-report-v2", ctx);
    }

    // ── v2 헬퍼 ────────────────────────────────────────────────────

    /** resultCode → 배지 클래스 (ok/warn/bad/na). null·미입력은 na(수동/대기). */
    private static String codeClass(String code) {
        if (code == null) return "na";
        return switch (code) {
            case "NORMAL"   -> "ok";
            case "PARTIAL"  -> "warn";
            case "ABNORMAL" -> "bad";
            default          -> "na"; // NOT_APPLICABLE / 기타
        };
    }

    /** "YYYY-MM" → "YYYY년 M월" (월 앞자리 0 제거). 형식 불일치 시 원본 그대로. */
    private static String formatMonthKo(String ym) {
        if (ym == null || ym.isBlank()) return "-";
        if (ym.matches("\\d{4}-\\d{2}")) {
            return ym.substring(0, 4) + "년 " + Integer.parseInt(ym.substring(5, 7)) + "월";
        }
        return ym;
    }

    /** resultCode → 한글 라벨. */
    private static String codeLabel(String code) {
        if (code == null) return "대기";
        return switch (code) {
            case "NORMAL"   -> "정상";
            case "PARTIAL"  -> "주의";
            case "ABNORMAL" -> "조치필요";
            case "NOT_APPLICABLE" -> "수동";
            default          -> "대기";
        };
    }

    /** 배지와 중복되는 단순 상태어 — 값 칸에선 제외하고 메모/점검내용을 노출. */
    private static final Set<String> BARE_STATUS = Set.of(
            "정상", "주의", "위반", "부분정상", "비정상", "해당없음", "수동", "대기", "-");

    /**
     * 값 칸에 표시할 설명 — 배지가 이미 상태를 보여주므로 단순 "정상" 중복은 피하고
     * 실제 메모/점검내용을 노출한다.
     * <ul>
     *   <li>게이지 행: resultText(측정값, 예 "89.9 %") + remarks</li>
     *   <li>일반 행: resultText(상태어 아닐 때) → remarks → itemMethod(점검 방법/내용) 순 폴백</li>
     * </ul>
     */
    private static String descOf(InspectCheckResultDTO c, boolean hasGauge) {
        String rt = c.getResultText() == null ? "" : c.getResultText().trim();
        String rm = c.getRemarks() == null ? "" : c.getRemarks().trim();
        // itemMethod 는 "점검내용||점검기준" 형식 — 구분자 || 를 가독성 좋은 · 로 치환
        String mt = c.getItemMethod() == null ? "" : c.getItemMethod().replace("||", " · ").trim();
        List<String> parts = new ArrayList<>();
        if (hasGauge) {
            if (!rt.isEmpty()) parts.add(rt);
        } else if (!rt.isEmpty() && !BARE_STATUS.contains(rt)) {
            parts.add(rt);
        }
        if (!rm.isEmpty()) parts.add(rm);
        if (parts.isEmpty() && !mt.isEmpty()) parts.add(mt);  // 폴백: 점검 방법/내용
        return parts.isEmpty() ? "—" : String.join(" · ", parts);
    }

    /** resultText 의 "…NN%" 에서 게이지 퍼센트(0~100) 추출 — 없으면 null. */
    private static Integer pctOf(String text) {
        if (text == null) return null;
        Matcher m = PCT_PATTERN.matcher(text);
        Integer last = null;
        while (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                last = (int) Math.round(Math.max(0, Math.min(100, v)));
            } catch (NumberFormatException ignored) { /* skip */ }
        }
        return last;
    }

    /** 섹션 worst 상태 순위 (높을수록 심각). */
    private static int rank(String cls) {
        return switch (cls) {
            case "bad" -> 3;
            case "warn" -> 2;
            case "ok" -> 1;
            default -> 0;
        };
    }

    private void addCard(List<SectionCard> cards, List<InspectCheckResultDTO> all,
                         String section, String title) {
        List<CardRow> rows = new ArrayList<>();
        String worst = "na";
        for (InspectCheckResultDTO c : all) {
            if (!section.equals(c.getSection())) continue;
            String cls = codeClass(c.getResultCode());
            if (rank(cls) > rank(worst)) worst = cls;
            Integer pct = pctOf(c.getResultText());
            String text = descOf(c, pct != null);
            rows.add(new CardRow(
                    c.getItemName() == null ? "" : c.getItemName(),
                    codeLabel(c.getResultCode()), cls, text, pct));
        }
        if (rows.isEmpty()) return;
        String worstLabel = switch (worst) {
            case "bad" -> "조치필요"; case "warn" -> "주의"; case "ok" -> "정상"; default -> "수동";
        };
        cards.add(new SectionCard(title, worstLabel, worst, rows));
    }

    private Kpi computeKpi(Long pjtId) {
        if (pjtId == null) return new Kpi("수집 대기", "수집 대기", "수집 대기", "0");
        OffsetDateTime since = OffsetDateTime.now().minusDays(30);
        List<InspectMetricSnapshot> rows = new ArrayList<>();
        rows.addAll(metricRepository.findRecentByPjtRole(pjtId, "AP", since));
        rows.addAll(metricRepository.findRecentByPjtRole(pjtId, "DB", since));
        if (rows.isEmpty()) return new Kpi("수집 대기", "수집 대기", "수집 대기", "0");

        double cpuSum = 0, memSum = 0, diskMax = -1;
        int cpuN = 0, memN = 0;
        Set<LocalDate> days = new HashSet<>();
        for (InspectMetricSnapshot s : rows) {
            if (s.getCpuPct() != null) { cpuSum += s.getCpuPct().doubleValue(); cpuN++; }
            if (s.getMemPct() != null) { memSum += s.getMemPct().doubleValue(); memN++; }
            if (s.getDiskPct() != null) diskMax = Math.max(diskMax, s.getDiskPct().doubleValue());
            if (s.getCollectedAt() != null) days.add(s.getCollectedAt().toLocalDate());
        }
        String cpu  = cpuN > 0 ? String.format("%.0f%%", cpuSum / cpuN) : "-";
        String mem  = memN > 0 ? String.format("%.0f%%", memSum / memN) : "-";
        String disk = diskMax >= 0 ? String.format("%.0f%%", diskMax) : "-";
        return new Kpi(cpu, mem, disk, String.valueOf(days.size()));
    }

    private String chartDataUri(Long pjtId) {
        if (pjtId == null) return null;
        try {
            byte[] png = metricChartService.renderChart(pjtId, 30);
            if (png == null || png.length == 0) return null;
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            log.warn("v2 차트 임베드 실패: {}", e.getMessage());
            return null;
        }
    }

    // ── v2 뷰모델 ──────────────────────────────────────────────────
    @Getter @AllArgsConstructor
    public static class Kpi {
        private final String cpu;   // 30일 평균
        private final String mem;   // 30일 평균
        private final String disk;  // 30일 최대
        private final String days;  // 수집 일수
    }

    @Getter @AllArgsConstructor
    public static class CardRow {
        private final String label;      // 점검 항목명
        private final String codeLabel;  // 정상/주의/위반/수동/대기
        private final String codeClass;  // ok/warn/bad/na
        private final String text;       // resultText(+remarks)
        private final Integer pct;       // 게이지 퍼센트 (없으면 null)
    }

    @Getter @AllArgsConstructor
    public static class SectionCard {
        private final String title;        // 섹션 표시명
        private final String statusLabel;  // 종합 상태 라벨
        private final String statusClass;  // 종합 상태 클래스
        private final List<CardRow> rows;
    }

    /** classpath 폰트를 임시파일로 추출 (한 번만) */
    private synchronized File getFontFile() {
        if (fontFile != null && fontFile.exists()) return fontFile;
        try {
            ClassPathResource res = new ClassPathResource("fonts/malgun.ttf");
            if (!res.exists()) {
                // Windows 시스템 폰트 직접 참조
                File sysFontFile = new File("C:/Windows/Fonts/malgun.ttf");
                if (sysFontFile.exists()) { fontFile = sysFontFile; return fontFile; }
                return null;
            }
            File tmp = Files.createTempFile("malgun", ".ttf").toFile();
            tmp.deleteOnExit();
            try (InputStream in = res.getInputStream(); FileOutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            fontFile = tmp;
            return fontFile;
        } catch (Exception e) {
            log.warn("폰트 파일 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public byte[] generatePdf(Long reportId) {
        String html = renderToHtmlV2(reportId);   // v2 비주얼 템플릿 사용
        log.info("PDF HTML(v2) 렌더링 완료, reportId={}, html길이={}", reportId, html.length());

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // 한글 폰트 등록 (File 기반)
            File font = getFontFile();
            if (font != null) {
                builder.useFont(font, "Malgun Gothic");
                log.info("한글 폰트 등록 완료: {}", font.getAbsolutePath());
            } else {
                log.warn("한글 폰트를 찾을 수 없습니다. PDF에 한글이 표시되지 않을 수 있습니다.");
            }

            builder.withHtmlContent(html, "/");
            builder.toStream(os);
            builder.run();

            byte[] result = os.toByteArray();
            log.info("PDF 생성 완료: {} bytes", result.length);
            return result;
        } catch (Exception e) {
            log.error("점검내역서 PDF 변환 실패", e);
            throw new RuntimeException("PDF 변환 중 오류: " + e.getMessage(), e);
        }
    }
}
