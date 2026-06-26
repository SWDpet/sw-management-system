package com.swmanager.system.controller;

import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.inspection.InfraServerRow;
import com.swmanager.system.dto.inspection.SnapshotRow;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.service.InspectMetricChartService;
import com.swmanager.system.service.InspectPdfService;
import com.swmanager.system.service.InspectReportService;
import com.swmanager.system.service.LogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 점검내역서(InspectReport) 컨트롤러 — DocumentController 에서 분리 (S4 giant-class-split).
 *
 * 클래스 레벨 @RequestMapping("/document") 을 DocumentController 와 동일하게 유지하여
 * 모든 URL(/document/api/inspect-*, /document/inspect-detail|preview) 을 100% 보존한다.
 * 기획서: docs/product-specs/giant-class-split.md / 개발계획: docs/exec-plans/giant-class-split.md
 */
@Slf4j
@Controller
@RequestMapping("/document")
@RequiredArgsConstructor
public class InspectReportController {

    private final InspectReportService inspectReportService;
    private final InspectPdfService inspectPdfService;
    private final InspectMetricChartService inspectMetricChartService;
    private final InspectMetricSnapshotRepository metricSnapshotRepository;
    private final SwProjectRepository swProjectRepository;
    private final InfraRepository infraRepository;
    private final LogService logService;

    // === 권한 헬퍼 (DocumentController 와 공용 — 분리 시 복제, S4) ===

    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) { return null; }
    }

    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }

    private String getAuth() {
        if (isAdmin()) return "EDIT";
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "NONE";
        String auth = cu.getUser().getAuthDocument();
        return (auth != null) ? auth : "NONE";
    }

    // ============================================================
    // 점검내역서 API
    // ============================================================

    /** POST /document/api/inspect-report - 저장 (신규/수정) — 감사 P1-2 조치: EDIT 권한 체크 (2026-04-18) */
    @PostMapping("/api/inspect-report")
    @ResponseBody
    public ResponseEntity<?> saveInspectReport(@RequestBody InspectReportDTO dto) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "수정 권한이 없습니다"));
        }
        try {
            return ResponseEntity.ok(ApiResult.ok(inspectReportService.save(dto)));
        } catch (Exception e) {
            log.error("점검내역서 저장 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** GET /document/api/inspect-report/{id} - 단건 조회 */
    @GetMapping("/api/inspect-report/{id}")
    @ResponseBody
    public ResponseEntity<?> getInspectReport(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResult.ok(inspectReportService.findById(id)));
        } catch (Exception e) {
            log.error("점검내역서 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** GET /document/api/inspect-reports?pjtId={pjtId} - 프로젝트별 목록 */
    @GetMapping("/api/inspect-reports")
    @ResponseBody
    public ResponseEntity<?> listInspectReports(@RequestParam Long pjtId) {
        try {
            return ResponseEntity.ok(ApiResult.ok(inspectReportService.findByProject(pjtId)));
        } catch (Exception e) {
            log.error("점검내역서 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** GET /document/api/inspect-report/find?pjtId={pjtId}&inspectMonth={YYYY-MM}
     *  inspection-report-d-v5: doc-inspect 진입 시 prefill 용.
     *  동일 (사업, 점검월) 의 기존 회차가 있으면 풀 데이터, 없으면 data=null. */
    @GetMapping("/api/inspect-report/find")
    @ResponseBody
    public ResponseEntity<?> findInspectReport(@RequestParam Long pjtId, @RequestParam String inspectMonth) {
        try {
            // 미존재 시 data=null → ok(null) 직렬화는 {success:true}(data 키 생략).
            // 프론트(doc-inspect.html:830/1821) resp.data truthy 검사라 null/undefined 동일 분기. (dto-migration)
            return ResponseEntity.ok(ApiResult.ok(inspectReportService.findByProjectAndMonth(pjtId, inspectMonth)));
        } catch (Exception e) {
            log.error("점검내역서 prefill 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** GET /document/api/inspect-report/previous-visits?pjtId={pjtId}&inspectMonth={YYYY-MM} - 이전 월 이력 조회 (신규 작성용) */
    @GetMapping("/api/inspect-report/previous-visits")
    @ResponseBody
    public ResponseEntity<?> getPreviousVisits(@RequestParam Long pjtId, @RequestParam String inspectMonth) {
        try {
            return ResponseEntity.ok(ApiResult.ok(inspectReportService.getPreviousVisits(pjtId, inspectMonth)));
        } catch (Exception e) {
            log.error("이전 월 이력 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** DELETE /document/api/inspect-report/{id} - soft delete — Phase J: 관리자(ADMIN) 전용 */
    @DeleteMapping("/api/inspect-report/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteInspectReport(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "관리자만 삭제할 수 있습니다"));
        }
        try {
            inspectReportService.delete(id);
            return ResponseEntity.ok(ApiResult.ok());
        } catch (Exception e) {
            log.error("점검내역서 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** POST /document/api/inspect/reset-all - 점검 테스트 데이터 일괄 초기화 (관리자 전용 hard delete) */
    @PostMapping("/api/inspect/reset-all")
    @ResponseBody
    public ResponseEntity<?> resetAllInspect() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isAdmin()) {
            result.put("success", false);
            result.put("error", Map.of("code", "FORBIDDEN", "message", "관리자만 초기화할 수 있습니다"));
            return ResponseEntity.status(403).body(result);
        }
        try {
            Map<String, Long> deleted = inspectReportService.resetAllInspectData();
            logService.log(MenuName.DOCUMENT, AccessActionType.DELETE, "점검 데이터 일괄 초기화: " + deleted);
            result.put("success", true);
            result.put("deleted", deleted);
        } catch (Exception e) {
            log.error("점검 데이터 일괄 초기화 실패: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /** GET /document/api/inspect-template?type={templateType} - 템플릿 조회 */
    @GetMapping("/api/inspect-template")
    @ResponseBody
    public ResponseEntity<?> getInspectTemplate(@RequestParam String type) {
        try {
            List<InspectCheckResultDTO> items = inspectReportService.getTemplateItems(type);
            return ResponseEntity.ok(ApiResult.ok(items));
        } catch (Exception e) {
            log.error("점검 템플릿 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    /** GET /document/api/infra-servers?distNm=양양군&sysNmEn=UPIS - 인프라 서버정보 조회
     *
     * 응답 키 (감사 P2 1-4 조치, 스프린트 2c 2026-04-19 / hostName 추가 2026-06-01 inspect-infra-diff-alert):
     *   serverId, serverType, hostName, ipAddr, osNm, serverModel, serialNo,
     *   cpuSpec, memorySpec, diskSpec, networkSpec, powerSpec,
     *   osDetail, rackLocation, note, softwares.
     *   민감 식별정보(MAC 주소, 계정 ID/PW 등) 는 응답에서 제외됨.
     */
    @GetMapping("/api/infra-servers")
    @ResponseBody
    public ResponseEntity<?> getInfraServers(@RequestParam String distNm, @RequestParam String sysNmEn) {
        // [감사 P2 1-4] 문서 VIEW 이상 권한 필요 (NONE 차단)
        if ("NONE".equals(getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "조회 권한이 없습니다"));
        }
        var infraList = infraRepository.findByDistNmAndSysNmEn(distNm, sysNmEn);
        if (infraList.isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        var infra = infraList.get(0);
        // [감사 P2 1-4] InfraServerRow = 16키 화이트리스트(MAC·계정ID/PW 등 민감정보 응답 제외 보존)
        List<InfraServerRow> servers = infra.getServers().stream().map(InfraServerRow::from).toList();
        return ResponseEntity.ok(servers);
    }

    /** GET /document/api/inspect-snapshots?pjtId=123 - 현장 수집 스냅샷(최신 1건/host) 조회.
     *  inspect-infra-diff-alert: 인프라 저장값 ↔ 현장 수집값 비교 source.
     *  응답: [{serverRole, hostName, cpu, memory, disk}] (allowlist 4필드만 — host_ip·status 제외, R-8).
     *  권한 VIEW 이상(NONE 403). 정상 경로 항상 200(빈 배열=현장 미수집, T-12). param=pjtId (codex 검토).
     */
    @GetMapping("/api/inspect-snapshots")
    @ResponseBody
    public ResponseEntity<?> getInspectSnapshots(@RequestParam Long pjtId) {
        // VIEW 이상 권한 필요 (NONE 차단) — getInfraServers 와 동일 정책 (NFR-8)
        if ("NONE".equals(getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "조회 권한이 없습니다"));
        }
        List<SnapshotRow> result = new ArrayList<>();
        for (var s : metricSnapshotRepository.findLatestPerRoleHost(pjtId)) {
            Map<String, String> specs = extractSnapshotSpecs(s.getRawPayload());
            result.add(new SnapshotRow(s.getServerRole(), s.getHostName(),
                    specs.get("cpu"), specs.get("memory"), specs.get("disk")));
        }
        return ResponseEntity.ok(result);
    }

    /** raw_payload(Tier 직렬화 {h, os, i:[[key,status,value],...]}) 에서 비교용 H/W 스펙 텍스트 추출.
     *  키별 규칙은 표시 로직 InspectionQrBatchService.buildResultText(L620-630) 와 동일하게 맞춤 (표시값=비교값).
     *  value(index2) 는 Map 객체. db.os.disk(mounts 배열) 는 추후 실제 스냅샷으로 확정 (R-15 잔여). (inspect-infra-diff-alert)
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> extractSnapshotSpecs(Map<String, Object> raw) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("cpu", null); out.put("memory", null); out.put("disk", null);
        if (raw == null) return out;
        Object itemsObj = raw.containsKey("i") ? raw.get("i") : raw.get("items"); // @JsonProperty("i") 우선
        if (!(itemsObj instanceof List)) return out;
        for (Object itObj : (List<Object>) itemsObj) {
            if (!(itObj instanceof List)) continue;
            List<Object> it = (List<Object>) itObj;
            if (it.size() < 3) continue;
            String key = String.valueOf(it.get(0));
            Object val = it.get(2); // [key, status, value] → value
            if (!(val instanceof Map)) continue;
            Map<String, Object> v = (Map<String, Object>) val;
            switch (key) {
                case "ap.hw.cpu" -> {
                    String name = v.get("name") != null ? String.valueOf(v.get("name")) : "";
                    String cores = v.get("cores") != null ? v.get("cores") + "코어" : "";
                    out.put("cpu", (name + " " + cores).trim());
                }
                case "ap.hw.memory" -> { if (v.get("installed_gb") != null) out.put("memory", v.get("installed_gb") + "GB"); }
                case "ap.os.disk_summary" -> { if (v.get("summary") != null) out.put("disk", String.valueOf(v.get("summary"))); }
                case "db.os.cpu_info" -> {
                    String cores = v.get("cores") != null ? v.get("cores") + "코어" : "";
                    String clock = v.get("clock_ghz") != null ? " " + v.get("clock_ghz") + "GHz" : "";
                    out.put("cpu", (cores + clock).trim());
                }
                case "db.os.mem_info" -> { if (v.get("total_gb") != null) out.put("memory", v.get("total_gb") + "GB"); }
                case "db.os.disk" -> {
                    if (v.get("summary") != null) out.put("disk", String.valueOf(v.get("summary")));
                    else if (v.get("count") != null) out.put("disk", v.get("count") + "개"); // mounts 배열형은 추후 실제 스냅샷으로 확정 (R-15)
                }
                default -> { }
            }
        }
        out.replaceAll((k, val) -> (val == null || val.trim().isEmpty()) ? null : val.trim());
        return out;
    }

    // ============================================================
    // 점검내역서 미리보기 / PDF 다운로드
    // ============================================================

    /** GET /document/api/inspect-pdf/{reportId} - 점검내역서 PDF 다운로드 */
    @GetMapping("/api/inspect-pdf/{reportId}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadInspectPdf(@PathVariable Long reportId) {
        if (!"EDIT".equals(getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT(기존 무가드→신설)
        try {
            byte[] pdf = inspectPdfService.generatePdf(reportId);
            InspectReportDTO report = inspectReportService.findById(reportId);
            String monthSuffix = "";
            if (report.getVisits() != null && !report.getVisits().isEmpty()) {
                monthSuffix = "_" + report.getVisits().get(0).getVisitMonth() + "월";
            } else if (report.getInspectMonth() != null && report.getInspectMonth().length() >= 7) {
                monthSuffix = "_" + Integer.parseInt(report.getInspectMonth().substring(5)) + "월";
            }
            String filename = (report.getDocTitle() != null ? report.getDocTitle() : "점검내역서") + monthSuffix + ".pdf";
            String encoded = java.net.URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encoded)
                    .body(pdf);
        } catch (Exception e) {
            log.error("점검내역서 PDF 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // DOCX 다운로드/서비스 제거 (2026-05-30): 점검내역서는 PDF(v2) 단일 출력으로 통일.

    /** GET /document/api/inspect-chart/preview?pjtId={pjtId}&month={YYYY-MM} — P5 메트릭 차트 미리보기 PNG.
     *  month 주면 점검주기 윈도우(직전 점검월~이번 점검월), 없으면 최근 30일. */
    @GetMapping("/api/inspect-chart/preview")
    @ResponseBody
    public ResponseEntity<byte[]> inspectChartPreview(@RequestParam Long pjtId,
                                                      @RequestParam(required = false) String month) {
        try {
            java.time.OffsetDateTime since, until;
            if (month != null && !month.isBlank()) {
                java.time.OffsetDateTime[] w =
                        com.swmanager.system.service.InspectMetricChartService.window(month, 12);
                since = w[0]; until = w[1];
            } else {
                until = java.time.OffsetDateTime.now(); since = until.minusMonths(12);
            }
            byte[] png = inspectMetricChartService.renderChart(pjtId, since, until);
            if (png == null || png.length == 0) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .header("Cache-Control", "no-cache")
                    .body(png);
        } catch (Exception e) {
            log.warn("inspect-chart preview 실패: pjt={} err={}", pjtId, e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

    /** GET /document/inspect-detail/{reportId} - 점검내역서 상세 정보 */
    @GetMapping("/inspect-detail/{reportId}")
    public String inspectDetail(@PathVariable Long reportId, Model model) {
        try {
            InspectReportDTO report = inspectReportService.findById(reportId);
            model.addAttribute("report", report);

            // 점검결과를 섹션별로 분리하여 모델에 추가
            if (report.getCheckResults() != null) {
                model.addAttribute("dbItems", report.getCheckResults().stream().filter(r -> "DB".equals(r.getSection())).toList());
                model.addAttribute("apItems", report.getCheckResults().stream().filter(r -> "AP".equals(r.getSection())).toList());
                model.addAttribute("dbmsItems", report.getCheckResults().stream().filter(r -> "DBMS".equals(r.getSection())).toList());
                model.addAttribute("gisItems", report.getCheckResults().stream().filter(r -> "GIS".equals(r.getSection())).toList());
                model.addAttribute("appItems", report.getCheckResults().stream().filter(r -> "APP".equals(r.getSection())).toList());
                model.addAttribute("dbUsage", report.getCheckResults().stream().filter(r -> "DB_USAGE".equals(r.getSection())).toList());
                model.addAttribute("apUsage", report.getCheckResults().stream().filter(r -> "AP_USAGE".equals(r.getSection())).toList());
                model.addAttribute("dbmsEtc", report.getCheckResults().stream().filter(r -> "DBMS_ETC".equals(r.getSection())).toList());
                model.addAttribute("appEtc", report.getCheckResults().stream().filter(r -> "APP_ETC".equals(r.getSection())).toList());
            }

            var project = swProjectRepository.findById(report.getPjtId()).orElse(null);
            model.addAttribute("project", project);

            if (project != null) {
                var infraList = infraRepository.findByDistNmAndSysNmEn(
                        project.getDistNm(), project.getSysNmEn());
                model.addAttribute("infraList", infraList);
            }

            // 요약 집계 (보고서 대시보드와 동일: 정상/주의/조치필요/수동)
            int cN = 0, cC = 0, cV = 0, cM = 0;
            if (report.getCheckResults() != null) {
                for (InspectCheckResultDTO r : report.getCheckResults()) {
                    String code = r.getResultCode();
                    if ("NORMAL".equals(code)) cN++;
                    else if ("PARTIAL".equals(code)) cC++;
                    else if ("ABNORMAL".equals(code)) cV++;
                    else cM++;
                }
            }
            model.addAttribute("cntNormal", cN);
            model.addAttribute("cntCaution", cC);
            model.addAttribute("cntViolation", cV);
            model.addAttribute("cntManual", cM);
            model.addAttribute("cntTotal", cN + cC + cV + cM);

            model.addAttribute("isAdmin", isAdmin());
            // [viewer-action-button-guard] PDF 다운로드 등 편집/다운로드 버튼 숨김용
            model.addAttribute("userAuth", getAuth());
            return "document/inspect-detail";
        } catch (Exception e) {
            log.error("점검내역서 상세 조회 실패: {}", e.getMessage(), e);
            return "redirect:/ops-doc/list";
        }
    }

    /** GET /document/inspect-preview/{reportId} - 점검내역서 미리보기 (v2 보고서 HTML 그대로 표출 → PDF 와 동일 디자인) */
    @GetMapping(value = "/inspect-preview/{reportId}", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String inspectPreview(@PathVariable Long reportId) {
        try {
            String html = inspectPdfService.renderToHtmlV2(reportId);
            // 미리보기 전용 상단 네비게이션 툴바를 주입 (보고서/PDF 본문에는 포함 안 됨)
            String bar =
                "<style>@media print{.preview-bar,.preview-spacer{display:none!important;}}</style>"
              + "<div class=\"preview-bar\" style=\"position:fixed;top:0;left:0;right:0;height:52px;background:#0f766e;"
              + "display:flex;align-items:center;gap:16px;padding:0 18px;z-index:9999;box-shadow:0 2px 8px rgba(0,0,0,.18);"
              + "font-family:'Malgun Gothic',sans-serif;\">"
              + "<a href=\"javascript:history.back()\" style=\"color:#fff;text-decoration:none;font-weight:600;font-size:14px;\">← 이전</a>"
              + "<a href=\"/document/inspect-detail/" + reportId + "\" style=\"color:#fff;text-decoration:none;font-weight:600;font-size:14px;\">상세</a>"
              + "<a href=\"/ops-doc/list\" style=\"color:#fff;text-decoration:none;font-weight:600;font-size:14px;\">목록</a>"
              + "<a href=\"/document/api/inspect-pdf/" + reportId + "\" style=\"margin-left:auto;color:#0f766e;background:#fff;"
              + "text-decoration:none;font-weight:700;font-size:13px;padding:7px 16px;border-radius:6px;\">PDF 다운로드</a>"
              + "</div><div class=\"preview-spacer\" style=\"height:52px;\"></div>";
            return html.replace("<body>", "<body>" + bar);
        } catch (Exception e) {
            log.error("점검내역서 미리보기 실패: {}", e.getMessage(), e);
            return "<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\"></head>"
                 + "<body style=\"font-family:'Malgun Gothic',sans-serif;padding:48px;color:#475569;\">"
                 + "점검내역서를 찾을 수 없습니다. <a href=\"/ops-doc/list\">목록으로</a></body></html>";
        }
    }
}
