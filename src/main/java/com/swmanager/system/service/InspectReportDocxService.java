package com.swmanager.system.service;

import com.swmanager.system.constant.enums.InspectResultCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.InspectVisitLogDTO;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.util.DocxTemplateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 점검내역서 시안D v5 양식 docx 생성기.
 *
 * <p>파이프라인:
 *   {@code InspectReport(id) → InspectReportDTO + SwProject → 변수 맵 → 템플릿 치환 → byte[]}
 *
 * <p>템플릿: {@code resources/templates/inspection-report/시안D_v5_template.docx}.
 * placeholder 는 {@code ${section.key}} 형식. 자세한 키는 {@link #buildVars} 참조.
 *
 * <p>점검결과 매핑: 각 섹션(AP/DB/DBMS/GIS/APP) 의 InspectCheckResult 를 sortOrder 기준
 * 정렬해서 1..N 위치로 매핑 (예: AP 14건 → ap.r1 ~ ap.r14). 시안D v5 의 점검표 row
 * 순서가 inspect_template.sort_order 순서와 동일하다는 가정.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InspectReportDocxService {

    private static final String TEMPLATE_PATH = "templates/inspection-report/시안D_v5_template.docx";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    /** 시안D v5 의 점검표 행 수 (sort_order 1..N) */
    private static final Map<String, Integer> SECTION_ROW_COUNT = Map.of(
            "AP",   14,
            "DB",   24,
            "DBMS", 17,
            "GIS",    6,
            "APP",  14
    );

    /** 시안D 의 점검표 prefix — placeholder 키와 일치 */
    private static final Map<String, String> SECTION_PREFIX = Map.of(
            "AP",   "ap",
            "DB",   "db",
            "DBMS", "dbms",
            "GIS",  "gis",
            "APP",  "app"
    );

    private final InspectReportService inspectReportService;
    private final SwProjectRepository swProjectRepository;

    /**
     * @param reportId InspectReport.id
     * @return docx 바이트 배열
     */
    public byte[] generate(Long reportId) {
        InspectReportDTO report = inspectReportService.findById(reportId);
        SwProject project = report.getPjtId() != null
                ? swProjectRepository.findById(report.getPjtId()).orElse(null)
                : null;

        Map<String, String> vars = buildVars(report, project);

        try (InputStream in = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             XWPFDocument doc = new XWPFDocument(in)) {
            DocxTemplateProcessor.process(doc, vars);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("docx 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 다운로드용 파일명. URL encoding 은 Controller 측에서.
     */
    public String fileName(Long reportId) {
        try {
            InspectReportDTO r = inspectReportService.findById(reportId);
            String month = r.getInspectMonth() != null ? r.getInspectMonth() : "unknown";
            return "점검내역서_" + month + "_" + reportId + ".docx";
        } catch (Exception e) {
            return "점검내역서_" + reportId + ".docx";
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  변수 맵 빌더
    // ──────────────────────────────────────────────────────────────
    Map<String, String> buildVars(InspectReportDTO report, SwProject project) {
        Map<String, String> m = new HashMap<>();

        String clientOrg     = project != null && project.getOrgNm() != null ? project.getOrgNm() : "발주처";
        String inspectorOrg  = project != null && project.getContEnt() != null ? project.getContEnt() : "수행사";
        String projectName   = project != null && project.getProjNm() != null ? project.getProjNm() : (report.getDocTitle() != null ? report.getDocTitle() : "사업명 미설정");
        String systemName    = project != null && project.getSysNm() != null ? project.getSysNm() : "표준시스템";
        String inspectorName = blankIfNull(report.getInspectorUsername());
        String clientName    = blankIfNull(report.getConfirmerName());
        String inspectMonth  = blankIfNull(report.getInspectMonth());

        // ── 표지 / Cover ─────────────────────────────────────────
        m.put("cover.headerLine",        clientOrg + "  |  " + inspectorOrg);
        m.put("cover.docSubtitle",       "월간 운영·유지관리 점검 보고");
        m.put("cover.projectFullName",   projectName);
        m.put("cover.notice",            "본 보고서는 " + clientOrg + " " + systemName + " 운영장비의 "
                + roundShort(inspectMonth) + " 정기점검 결과를 정리한 것입니다.");
        m.put("cover.roundLabel",        roundLabel(inspectMonth));
        m.put("cover.reportDate",        LocalDate.now().format(DATE_FMT));
        m.put("cover.scope",             "AP · DB · GIS · 표준시스템");
        m.put("cover.clientOrg",         clientOrg);
        m.put("cover.inspectorOrg",      inspectorOrg);
        m.put("cover.clientName",        defaultIfBlank(clientName, "(공무원 담당자)"));
        m.put("cover.inspectorName",     defaultIfBlank(inspectorName, "(점검자)"));

        // ── SUMMARY 본문 ─────────────────────────────────────────
        List<InspectCheckResultDTO> all = report.getCheckResults() != null ? report.getCheckResults() : List.of();
        long ok    = all.stream().filter(c -> "NORMAL".equals(c.getResultCode())).count();
        long warn  = all.stream().filter(c -> "PARTIAL".equals(c.getResultCode())).count();
        long crit  = all.stream().filter(c -> "ABNORMAL".equals(c.getResultCode())).count();
        long manual= all.stream().filter(c -> "NOT_APPLICABLE".equals(c.getResultCode())).count();
        long total = all.size();

        // 섹션별 카운트로 본문 보강
        StringBuilder body = new StringBuilder();
        body.append("본 회차는 ").append(clientOrg).append(" ").append(systemName)
            .append(" 운영장비의 ").append(roundShort(inspectMonth))
            .append(" 정기점검입니다. ");
        body.append("점검항목 ").append(total).append(" 건 — 정상 ").append(ok)
            .append(" / 부분정상 ").append(warn).append(" / 비정상 ").append(crit)
            .append(" / 해당없음 ").append(manual).append(". ");
        String sectionLine = sectionSummaryLine(all);
        if (!sectionLine.isBlank()) body.append(sectionLine);
        // 점검자 입력 핵심 발견사항이 있으면 본문 말미에 추가
        if (report.getKeyFindings() != null && !report.getKeyFindings().isBlank()) {
            body.append(" ").append(report.getKeyFindings().trim());
        }
        m.put("summary.body", body.toString());

        // SUMMARY 표 4 셀 카운트 (자동수집 + 수동 점검 모두 포함)
        m.put("summary.totalCnt",     total + " 건");
        m.put("summary.okCnt",        ok    + " 건");
        m.put("summary.warnCritCnt", (warn + crit) + " 건");
        m.put("summary.manualCnt",    manual + " 건");

        // findings — 비정상/경고 항목 top 3 자동 나열
        String findings = topIssues(all, 3);
        m.put("summary.findings", findings.isEmpty()
                ? "본 회차 점검 결과 비정상/경고 항목 없음 — 안정 운영 중."
                : findings);

        // ── HISTORY (12개월) ─────────────────────────────────────
        Map<Integer, InspectVisitLogDTO> visitsByMonth = new HashMap<>();
        if (report.getVisits() != null) {
            for (InspectVisitLogDTO v : report.getVisits()) {
                if (v.getVisitMonth() != null) {
                    try { visitsByMonth.put(Integer.parseInt(v.getVisitMonth().trim()), v); } catch (Exception ignored) {}
                }
            }
        }
        // previousVisits 도 흡수
        if (report.getPreviousVisits() != null) {
            for (InspectVisitLogDTO v : report.getPreviousVisits()) {
                if (v.getVisitMonth() != null) {
                    try {
                        int mon = Integer.parseInt(v.getVisitMonth().trim());
                        visitsByMonth.putIfAbsent(mon, v);
                    } catch (Exception ignored) {}
                }
            }
        }
        String year = (project != null && project.getYear() != null)
                ? String.valueOf(project.getYear())
                : (inspectMonth.length() >= 4 ? inspectMonth.substring(0, 4) : "");
        for (int mon = 1; mon <= 12; mon++) {
            String key = String.format("history.M%02d.", mon);
            InspectVisitLogDTO v = visitsByMonth.get(mon);
            m.put(key + "year",    v != null && v.getVisitYear() != null ? v.getVisitYear() : year);
            m.put(key + "month",   v != null && v.getVisitMonth() != null ? v.getVisitMonth() : String.valueOf(mon));
            m.put(key + "day",     v != null ? blankIfNull(v.getVisitDay()) : "");
            m.put(key + "work",    v != null ? blankIfNull(v.getTask()) : "");
            m.put(key + "symptom", v != null ? blankIfNull(v.getSymptom()) : "");
            m.put(key + "action",  v != null ? blankIfNull(v.getAction()) : "");
        }

        // ── TARGETS (사양 표) — 정적 default + 사이트별 조정 가능 ───
        m.put("targets.note",       "· DB 서버 / AP 서버 / 소프트웨어 — 본 회차 점검대상 H/W·S/W 사양.");
        m.put("targets.db.model",   "DB 서버 모델 미설정");
        m.put("targets.db.cpu",     "");
        m.put("targets.db.memory",  "");
        m.put("targets.db.disk",    "");
        m.put("targets.db.network", "");
        m.put("targets.db.power",   "이중화 전원");
        m.put("targets.db.os",      "");
        m.put("targets.ap.model",   "AP 서버 모델 미설정");
        m.put("targets.ap.cpu",     "");
        m.put("targets.ap.memory",  "");
        m.put("targets.ap.disk",    "");
        m.put("targets.ap.network", "");
        m.put("targets.ap.power",   "이중화 전원");
        m.put("targets.ap.os",      "");

        // ── AP / DB / DBMS 헤더 표 (정적) ────────────────────────
        m.put("ap.note",       "· AP 서버 점검 — 시안D v5.");
        m.put("ap.extraNote",  extraNote("AP", all));
        m.put("ap.head.os",    "");
        m.put("ap.head.cpu",   "");
        m.put("ap.head.usage", "AP 서버");
        m.put("ap.head.memory","");
        m.put("ap.head.model", "");
        m.put("ap.head.disk",  "");

        m.put("db.note",          "· DB 서버 점검 — 시안D v5.");
        m.put("db.extraNote",     extraNote("DB", all));
        m.put("db.head.model",    "");
        m.put("db.head.os",       "");
        m.put("db.head.hostname", "");
        m.put("db.head.usage",    "DB 서버");

        m.put("dbms.note",          "· DBMS (Oracle) 점검 — 시안D v5.");
        m.put("dbms.extraNote",     extraNote("DBMS", all));
        m.put("dbms.head.target",   "");
        m.put("dbms.head.software", "Oracle DB");
        m.put("dbms.head.os",       "");
        m.put("dbms.head.ip",       blankIfNull(report.getDbmsIp()));

        m.put("gis.note",      "· GIS 엔진 (GSS / GWS) 점검 — 시안D v5.");
        m.put("gis.extraNote", extraNote("GIS", all));
        m.put("gis.uwesNote",  "· 보조 — UWES Store 자동수집 메트릭.");

        m.put("app.note", "· 표준시스템(UPIS) 메뉴 점검 — 14 개 항목.");

        m.put("history.note", "· " + year + "년 점검·장애조치 이력 요약.");

        // ── NEXT ROUND (수동 입력 — Phase C) ─────────────────────
        m.put("next.scheduleNote",      blankIfNull(report.getNextScheduleNote()));
        m.put("next.recommendation1",   bullet(report.getRecommendation1(), 1));
        m.put("next.recommendation2",   bullet(report.getRecommendation2(), 2));
        m.put("next.recommendation3",   bullet(report.getRecommendation3(), 3));
        m.put("next.followup1",         bullet(report.getFollowup1(), 1));
        m.put("next.followup2",         bullet(report.getFollowup2(), 2));
        m.put("next.followup3",         bullet(report.getFollowup3(), 3));

        // ── 점검표 결과/메모 매핑 ─────────────────────────────────
        Map<String, List<InspectCheckResultDTO>> bySection = new LinkedHashMap<>();
        for (InspectCheckResultDTO c : all) {
            String sec = c.getSection() != null ? c.getSection() : "";
            bySection.computeIfAbsent(sec, k -> new java.util.ArrayList<>()).add(c);
        }
        for (var entry : SECTION_PREFIX.entrySet()) {
            String section = entry.getKey();
            String prefix  = entry.getValue();
            int rows = SECTION_ROW_COUNT.getOrDefault(section, 0);
            List<InspectCheckResultDTO> items = bySection.getOrDefault(section, List.of()).stream()
                    .sorted(Comparator.comparing(c -> Optional.ofNullable(c.getSortOrder()).orElse(0)))
                    .toList();
            for (int i = 1; i <= rows; i++) {
                InspectCheckResultDTO item = (i - 1) < items.size() ? items.get(i - 1) : null;
                m.put(prefix + ".r" + i + ".result",
                        item != null ? resultCodeToLabel(item.getResultCode()) : "");
                m.put(prefix + ".r" + i + ".memo", item != null ? memoOf(item) : "");
            }
        }

        return m;
    }

    // ──────────────────────────────────────────────────────────────
    //  유틸
    // ──────────────────────────────────────────────────────────────
    private static String roundLabel(String yyyyMm) {
        if (yyyyMm == null || yyyyMm.length() < 7) return "정기점검";
        String[] p = yyyyMm.split("-");
        return p[0] + "년 " + Integer.parseInt(p[1]) + "월 정기점검";
    }

    private static String roundShort(String yyyyMm) {
        if (yyyyMm == null || yyyyMm.length() < 7) return "당월";
        return Integer.parseInt(yyyyMm.split("-")[1]) + "월";
    }

    /** "NORMAL"/"PARTIAL"/... 또는 한글 label 모두 받아서 한글 label 로 반환. */
    private static String resultCodeToLabel(String raw) {
        if (raw == null || raw.isBlank()) return "";
        // 이미 한글 label 인 경우 그대로
        InspectResultCode code = InspectResultCode.fromKoLabel(raw);
        if (code != null) return code.getLabel();
        try {
            return InspectResultCode.valueOf(raw).getLabel();
        } catch (IllegalArgumentException e) {
            return raw;
        }
    }

    private static String memoOf(InspectCheckResultDTO c) {
        // result_text 가 자동수집 값(예: '15.2 %'), remarks 가 추가 표지 → 둘 다 있으면 result_text 우선
        String r = blankIfNull(c.getResultText());
        String n = blankIfNull(c.getRemarks());
        if (!r.isEmpty() && !n.isEmpty()) return r + " (" + n + ")";
        return !r.isEmpty() ? r : n;
    }

    private static String blankIfNull(String s) { return s == null ? "" : s; }
    private static String defaultIfBlank(String s, String def) { return s == null || s.isBlank() ? def : s; }

    /** 시안D v5 의 권고/후속조치는 "1) ..." 처럼 번호 prefix 가 자연스럽다. 비어있으면 빈 문자열. */
    private static String bullet(String value, int n) {
        if (value == null || value.isBlank()) return "";
        String v = value.trim();
        // 이미 사용자가 "1)" 등을 직접 입력했으면 그대로 사용
        if (v.matches("^\\d+[\\)\\.].*")) return v;
        return n + ") " + v;
    }

    /** 섹션별 ok/warn/crit 분포 — SUMMARY 본문 보강. */
    private static String sectionSummaryLine(List<InspectCheckResultDTO> all) {
        if (all.isEmpty()) return "";
        Map<String, long[]> counts = new LinkedHashMap<>();
        String[] sectionOrder = { "AP", "DB", "DBMS", "GIS", "APP" };
        for (String s : sectionOrder) counts.put(s, new long[]{0, 0, 0, 0}); // ok, warn, crit, manual
        for (InspectCheckResultDTO c : all) {
            String sec = c.getSection() != null ? c.getSection() : "";
            long[] cnt = counts.get(sec);
            if (cnt == null) continue;
            switch (String.valueOf(c.getResultCode())) {
                case "NORMAL"         -> cnt[0]++;
                case "PARTIAL"        -> cnt[1]++;
                case "ABNORMAL"       -> cnt[2]++;
                case "NOT_APPLICABLE" -> cnt[3]++;
            }
        }
        StringBuilder sb = new StringBuilder("섹션별: ");
        boolean first = true;
        Map<String, String> label = Map.of(
                "AP", "AP", "DB", "DB", "DBMS", "DBMS", "GIS", "GIS", "APP", "표준시스템");
        for (String s : sectionOrder) {
            long[] cnt = counts.get(s);
            long secTotal = cnt[0] + cnt[1] + cnt[2] + cnt[3];
            if (secTotal == 0) continue;
            if (!first) sb.append(" · ");
            first = false;
            sb.append(label.getOrDefault(s, s)).append(" 정상 ").append(cnt[0]);
            if (cnt[1] > 0) sb.append("/경고 ").append(cnt[1]);
            if (cnt[2] > 0) sb.append("/비정상 ").append(cnt[2]);
            if (cnt[3] > 0) sb.append("/육안 ").append(cnt[3]);
        }
        return first ? "" : sb.append(".").toString();
    }

    /** 비정상/경고 항목 top N 을 한 문장으로 묶어 반환. */
    private static String topIssues(List<InspectCheckResultDTO> all, int limit) {
        List<InspectCheckResultDTO> issues = all.stream()
                .filter(c -> "ABNORMAL".equals(c.getResultCode()) || "PARTIAL".equals(c.getResultCode()))
                .sorted(Comparator.comparing((InspectCheckResultDTO c) ->
                        "ABNORMAL".equals(c.getResultCode()) ? 0 : 1))
                .limit(limit)
                .toList();
        if (issues.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("주요 발견사항 — ");
        for (int i = 0; i < issues.size(); i++) {
            if (i > 0) sb.append(" / ");
            InspectCheckResultDTO it = issues.get(i);
            sb.append("(").append(it.getSection() != null ? it.getSection() : "").append(") ")
              .append(blankIfNull(it.getItemName()));
            String memo = memoOf(it);
            if (!memo.isBlank()) sb.append(" — ").append(memo);
        }
        sb.append(".");
        return sb.toString();
    }

    /** 섹션별 ※ 추가 점검 한 줄. 자동수집된 result_text 의 itemName 키워드(CPU/Memory/Disk/Tablespace 등) 추출. */
    private static String extraNote(String section, List<InspectCheckResultDTO> all) {
        List<String> picks = new java.util.ArrayList<>();
        String[] keywords = switch (section) {
            case "AP", "DB" -> new String[]{"CPU", "Memory", "Disk", "디스크"};
            case "DBMS"     -> new String[]{"Tablespace", "테이블", "SGA", "PGA", "Archive"};
            case "GIS"      -> new String[]{"Store", "log", "GSS", "GWS"};
            default          -> new String[0];
        };
        for (InspectCheckResultDTO c : all) {
            if (!section.equals(c.getSection())) continue;
            String name = blankIfNull(c.getItemName());
            for (String kw : keywords) {
                if (name.contains(kw)) {
                    String val = blankIfNull(c.getResultText());
                    if (val.isBlank()) val = blankIfNull(c.getRemarks());
                    if (!val.isBlank()) {
                        picks.add(name + " " + val);
                    }
                    break;
                }
            }
        }
        if (picks.isEmpty()) return "";
        return "※ 추가 점검 — " + String.join(" · ", picks);
    }
}
