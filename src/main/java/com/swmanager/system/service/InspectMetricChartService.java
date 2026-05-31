package com.swmanager.system.service;

import com.swmanager.system.domain.InspectMetricSnapshot;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * 30일 메트릭 추이 차트 — InspectPdfService(v2) 의 대시보드 추이 차트용 PNG byte[] 렌더.
 *
 * <p>기획서: docs/product-specs/inspection-report-d-v6.md §3-1
 * <p>색 정책 (디자인팀 자문 R6/D2):
 * <ul>
 *   <li>CPU : 와인 #A53F52</li>
 *   <li>Memory : 슬레이트 #44546A</li>
 *   <li>Disk : 다크 #2F3342</li>
 * </ul>
 *
 * <p>다중 호스트 시 호스트별 line 분리 (DB팀 D-3). 호스트당 3 series (CPU/MEM/DISK).
 * <p>크기: 17.4cm × 8.0cm @ 120 DPI = 약 820 × 378 px (디자인팀 R6: v4 12cm → -4cm).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InspectMetricChartService {

    private static final Color COLOR_CPU  = new Color(0xA5, 0x3F, 0x52);  // 와인
    private static final Color COLOR_MEM  = new Color(0x44, 0x54, 0x6A);  // 슬레이트
    private static final Color COLOR_DISK = new Color(0x2F, 0x33, 0x42);  // 다크
    private static final Color GRID_COLOR = new Color(0xD0, 0xCE, 0xCE);
    private static final Color BG_COLOR   = Color.WHITE;

    /** 17.4cm × 8.0cm @ 120 DPI ≒ 820 × 378. POI 임베드 시 동일 비율로 표시. */
    private static final int CHART_WIDTH  = 820;
    private static final int CHART_HEIGHT = 378;

    /** 한글 폰트 — OS 미존재 시 Java 기본 SansSerif 로 fallback. */
    private static final String[] FONT_CANDIDATES = {
            "Malgun Gothic", "Noto Sans CJK KR", "Pretendard", "AppleGothic"
    };

    private final InspectMetricSnapshotRepository repository;

    /**
     * 월별 추이 윈도우 [since, until) — 이번 점검월 기준 직전 monthsBack 개월.
     * 점검 회차마다 1점씩 누적된 월별 점들을 잇기 위함. (예: monthsBack=12 → 최근 12개월)
     * since = (이번월 − (monthsBack−1)) 1일, until = 다음달 1일. 파싱 실패 시 now 기준 폴백.
     */
    public static OffsetDateTime[] window(String thisMonth, int monthsBack) {
        try {
            java.time.ZoneOffset off = OffsetDateTime.now().getOffset();
            java.time.YearMonth tm = java.time.YearMonth.parse(thisMonth.trim());
            OffsetDateTime until = tm.plusMonths(1).atDay(1).atStartOfDay().atOffset(off);
            OffsetDateTime since = tm.minusMonths(monthsBack - 1L).atDay(1).atStartOfDay().atOffset(off);
            return new OffsetDateTime[]{ since, until };
        } catch (Exception e) {
            OffsetDateTime now = OffsetDateTime.now();
            return new OffsetDateTime[]{ now.minusMonths(monthsBack), now };
        }
    }

    /**
     * 점검주기 추이 차트 렌더 — 윈도우 [since, until).
     * (첫 회차=점검월말−30일~말, 이후=직전 점검월말~이번 점검월말. 산출은 InspectPdfService.)
     *
     * @param pjtId  SwProject.proj_id
     * @param since  윈도우 시작(포함)
     * @param until  윈도우 끝(미포함)
     * @return PNG byte[] — 데이터 0건이면 빈 차트 ("수집 대기" 안내)
     */
    public byte[] renderChart(Long pjtId, OffsetDateTime since, OffsetDateTime until) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        int hostLineCount = 0;

        // server_role = 'AP' / 'DB' 각각 호스트별 시리즈 추가
        for (String role : new String[]{"AP", "DB"}) {
            List<String> hosts = repository.findHostsByPjtRoleRange(pjtId, role, since, until);
            for (String host : hosts) {
                List<InspectMetricSnapshot> rows = repository.findRangeByPjtRoleHost(
                        pjtId, role, host, since, until);
                if (rows.isEmpty()) continue;

                String hostLabel = (host == null || host.isBlank()) ? role : (role + "·" + host);
                TimeSeries cpu  = new TimeSeries(hostLabel + " CPU");
                TimeSeries mem  = new TimeSeries(hostLabel + " MEM");
                TimeSeries disk = new TimeSeries(hostLabel + " DISK");

                for (InspectMetricSnapshot s : rows) {
                    if (s.getCollectedAt() == null) continue;
                    Date d = Date.from(s.getCollectedAt().toInstant());
                    Month m = new Month(d);   // 월 단위 — 점검 회차(월)당 1점, 같은 달 재점검 시 최신값
                    if (s.getCpuPct()  != null) cpu.addOrUpdate(m,  s.getCpuPct().doubleValue());
                    if (s.getMemPct()  != null) mem.addOrUpdate(m,  s.getMemPct().doubleValue());
                    if (s.getDiskPct() != null) disk.addOrUpdate(m, s.getDiskPct().doubleValue());
                }
                if (!cpu.isEmpty())  dataset.addSeries(cpu);
                if (!mem.isEmpty())  dataset.addSeries(mem);
                if (!disk.isEmpty()) dataset.addSeries(disk);
                hostLineCount++;
            }
        }

        if (dataset.getSeriesCount() == 0) {
            log.info("renderChart pjt={} window=[{},{}) → no data, empty chart", pjtId, since, until);
            return emptyChart("수집 대기 — agent snapshot 누적 필요");
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null, null, "사용률 (%)", dataset, true, false, false);
        styleChart(chart);
        applyLineColors(chart, dataset);

        try {
            BufferedImage img = chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bao);
            log.info("renderChart pjt={} window=[{},{}) hosts={} series={} bytes={}",
                    pjtId, since, until, hostLineCount, dataset.getSeriesCount(), bao.size());
            return bao.toByteArray();
        } catch (Exception e) {
            log.warn("renderChart PNG 직렬화 실패: {}", e.getMessage());
            return emptyChart("차트 렌더 실패");
        }
    }

    private void styleChart(JFreeChart chart) {
        Font font = pickFont(11f);
        chart.setBackgroundPaint(BG_COLOR);
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(pickFont(10f));
            chart.getLegend().setBackgroundPaint(BG_COLOR);
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(GRID_COLOR);

        DateAxis xAxis = (DateAxis) plot.getDomainAxis();
        xAxis.setDateFormatOverride(new SimpleDateFormat("yy/MM"));
        xAxis.setLabelFont(font);
        xAxis.setTickLabelFont(pickFont(9f));

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0.0, 100.0);
        yAxis.setLabelFont(font);
        yAxis.setTickLabelFont(pickFont(9f));
    }

    private void applyLineColors(JFreeChart chart, TimeSeriesCollection dataset) {
        XYPlot plot = chart.getXYPlot();
        // lines + shapes(점 마커) — 점검 회차가 1개여도 점(dot)이 보이도록 마커 ON
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        java.awt.Shape dot = new java.awt.geom.Ellipse2D.Double(-2.6, -2.6, 5.2, 5.2);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String key = String.valueOf(dataset.getSeriesKey(i));
            Color c;
            if      (key.endsWith(" CPU"))  c = COLOR_CPU;
            else if (key.endsWith(" MEM"))  c = COLOR_MEM;
            else if (key.endsWith(" DISK")) c = COLOR_DISK;
            else                             c = COLOR_DISK;
            renderer.setSeriesPaint(i, c);
            renderer.setSeriesStroke(i, new BasicStroke(1.6f));
            renderer.setSeriesShape(i, dot);
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }
        plot.setRenderer(renderer);
    }

    private byte[] emptyChart(String message) {
        try {
            BufferedImage img = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, CHART_WIDTH, CHART_HEIGHT);
            g.setColor(new Color(0x88, 0x88, 0x88));
            g.setFont(pickFont(14f));
            java.awt.FontMetrics fm = g.getFontMetrics();
            int x = (CHART_WIDTH  - fm.stringWidth(message)) / 2;
            int y = CHART_HEIGHT / 2 + fm.getAscent() / 2;
            g.drawString(message, x, y);
            g.dispose();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bao);
            return bao.toByteArray();
        } catch (Exception e) {
            log.warn("emptyChart 실패: {}", e.getMessage());
            return new byte[0];
        }
    }

    /** 한글 지원 폰트 우선 — 없으면 Java SansSerif fallback. */
    private static Font pickFont(float size) {
        String[] available = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Map<String, Boolean> set = new LinkedHashMap<>();
        for (String n : available) set.put(n, Boolean.TRUE);
        for (String cand : FONT_CANDIDATES) {
            if (set.containsKey(cand)) return new Font(cand, Font.PLAIN, (int) size);
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
    }
}
