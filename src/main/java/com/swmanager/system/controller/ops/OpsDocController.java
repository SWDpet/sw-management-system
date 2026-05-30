package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.service.ops.OpsDocAttachmentService;
import com.swmanager.system.service.ops.OpsDocService;
import com.swmanager.system.service.ops.OpsDocSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 운영·유지보수 문서 컨트롤러 — 4 종 (FAULT/SUPPORT/INSTALL/PATCH).
 * 점검내역서(INSPECT) 는 DocumentController 에 그대로 남고, 본 컨트롤러는 4 종 + 통합 리스트만.
 *
 * 기획서: docs/product-specs/doc-split-ops.md (v3, FR-4)
 * 개발계획: docs/exec-plans/doc-split-ops.md (v2, Step 5)
 */
@Slf4j
@Controller
@RequestMapping("/ops-doc")
@RequiredArgsConstructor
public class OpsDocController {

    private final OpsDocService opsDocService;
    private final OpsDocAttachmentService attachmentService;
    private final OpsDocSignatureService signatureService;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final InspectReportRepository inspectReportRepository;
    private final SwProjectRepository swProjectRepository;

    /** 통합 리스트 — 5 종 모두 표시 (점검내역서 row 포함). 사업문서 목록과 동일 디자인 + 필터. */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String docType,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String sido,
                       @RequestParam(required = false) String sigungu,
                       @RequestParam(required = false) String sysType,
                       Model model) {
        // 지역 마스터(sigungu_code) 로드 → admSectC / sggNm 인덱스
        Map<String, SigunguCode> byCode = new HashMap<>();
        Map<String, List<SigunguCode>> bySgg = new HashMap<>();
        for (SigunguCode s : sigunguCodeRepository.findAll()) {
            if (s.getAdmSectC() != null) byCode.put(s.getAdmSectC(), s);
            if (s.getSggNm() != null) bySgg.computeIfAbsent(s.getSggNm(), k -> new ArrayList<>()).add(s);
        }

        List<OpsDocument> all = opsDocService.findAll();
        // 문서별 시도/시군구 해석 (regionCode → 코드, INSPECT → 연계 사업)
        Map<Long, String> sidoByDoc = new HashMap<>();
        Map<Long, String> sggByDoc = new HashMap<>();
        for (OpsDocument d : all) {
            String[] r = resolveRegion(d, byCode, bySgg);
            sidoByDoc.put(d.getDocId(), r[0]);
            sggByDoc.put(d.getDocId(), r[1]);
        }

        List<OpsDocument> docs = all.stream()
                .filter(d -> docType == null || docType.isBlank()
                        || (d.getDocType() != null && d.getDocType().name().equals(docType)))
                .filter(d -> status == null || status.isBlank()
                        || (d.getStatus() != null && d.getStatus().name().equals(status)))
                .filter(d -> sysType == null || sysType.isBlank() || sysType.equals(d.getSysType()))
                .filter(d -> sido == null || sido.isBlank() || sido.equals(sidoByDoc.get(d.getDocId())))
                .filter(d -> sigungu == null || sigungu.isBlank() || sigungu.equals(sggByDoc.get(d.getDocId())))
                .filter(d -> keyword == null || keyword.isBlank()
                        || (d.getDocNo() != null && d.getDocNo().contains(keyword))
                        || (d.getTitle() != null && d.getTitle().contains(keyword)))
                .toList();

        model.addAttribute("documents", docs);
        model.addAttribute("sidoByDoc", sidoByDoc);
        model.addAttribute("sggByDoc", sggByDoc);
        model.addAttribute("docType", docType);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sido", sido);
        model.addAttribute("sigungu", sigungu);
        model.addAttribute("sysType", sysType);
        model.addAttribute("sidoOptions", sigunguCodeRepository.findDistinctSidoNm());
        // 시스템 옵션은 실제 문서의 sysType distinct → 필터 값/데이터 정확 일치
        model.addAttribute("systemOptions", all.stream()
                .map(OpsDocument::getSysType)
                .filter(s -> s != null && !s.isBlank())
                .distinct().sorted().toList());
        model.addAttribute("activeMenu", "ops");
        model.addAttribute("isAdmin", isAdmin());
        return "ops-doc/list";
    }

    /** 문서의 시도/시군구 해석. [sido, sigungu] 반환 (없으면 "-"). */
    private String[] resolveRegion(OpsDocument d,
                                   Map<String, SigunguCode> byCode,
                                   Map<String, List<SigunguCode>> bySgg) {
        // 1) regionCode(행정구역코드) 직접 매핑
        if (d.getRegionCode() != null && byCode.containsKey(d.getRegionCode())) {
            SigunguCode s = byCode.get(d.getRegionCode());
            return new String[]{ nz(s.getSidoNm()), nz(s.getSggNm()) };
        }
        // 2) 점검내역서(INSPECT) → 연계 사업(SwProject)의 시도/시군구
        if (d.getDocType() == OpsDocType.INSPECT) {
            Long reportId = parseReportId(d.getDocNo());
            if (reportId != null) {
                var report = inspectReportRepository.findById(reportId).orElse(null);
                if (report != null && report.getPjtId() != null) {
                    var pj = swProjectRepository.findById(report.getPjtId()).orElse(null);
                    if (pj != null) {
                        String sgg = pj.getDistNm();
                        String sido = pj.getCityNm();
                        List<SigunguCode> cand = sgg != null ? bySgg.get(sgg) : null;
                        if (cand != null && !cand.isEmpty()) {
                            SigunguCode pick = cand.get(0);
                            if (cand.size() > 1 && sido != null) {
                                for (SigunguCode c : cand) {
                                    if (sido.equals(c.getSidoNm())) { pick = c; break; }
                                }
                            }
                            return new String[]{ nz(pick.getSidoNm()), nz(pick.getSggNm()) };
                        }
                        return new String[]{ nz(sido), nz(sgg) };
                    }
                }
            }
        }
        return new String[]{ "-", "-" };
    }

    private static String nz(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    private static Long parseReportId(String docNo) {
        if (docNo == null) return null;
        try { return Long.valueOf(docNo.substring(docNo.lastIndexOf('-') + 1)); }
        catch (Exception e) { return null; }
    }

    private boolean isAdmin() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }

    /** 신규 작성 폼 (4 종). */
    @GetMapping("/{type}/form")
    public String form(@PathVariable("type") String type, Model model) {
        OpsDocType docType = OpsDocType.fromString(type);
        if (docType == null || docType == OpsDocType.INSPECT) return "redirect:/ops-doc/list";
        model.addAttribute("docType", docType);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", "ops");
        return docType.templateName();
    }

    /** 수정 폼 (4 종). INSPECT 는 InspectReport 흐름으로 redirect. */
    @GetMapping("/{type}/{docId}/edit")
    public String editForm(@PathVariable("type") String type, @PathVariable Long docId, Model model) {
        OpsDocType docType = OpsDocType.fromString(type);
        OpsDocument doc = opsDocService.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + docId));
        if (docType == OpsDocType.INSPECT) {
            Long reportId = parseInspectReportId(doc.getDocNo());
            return reportId != null
                    ? "redirect:/document/inspect-detail/" + reportId
                    : "redirect:/ops-doc/list";
        }
        model.addAttribute("docType", docType);
        model.addAttribute("doc", doc);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeMenu", "ops");
        return docType.templateName();
    }

    /** 상세 보기. INSPECT 는 InspectReport 상세로 redirect. */
    @GetMapping("/{type}/{docId}")
    public String detail(@PathVariable("type") String type, @PathVariable Long docId, Model model) {
        OpsDocType docType = OpsDocType.fromString(type);
        OpsDocument doc = opsDocService.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + docId));
        if (docType == OpsDocType.INSPECT) {
            Long reportId = parseInspectReportId(doc.getDocNo());
            return reportId != null
                    ? "redirect:/document/inspect-detail/" + reportId
                    : "redirect:/ops-doc/list";
        }
        model.addAttribute("docType", docType);
        model.addAttribute("doc", doc);
        model.addAttribute("activeMenu", "ops");
        return docType.templateName();
    }

    /**
     * INSP-yyyy-{reportId} / INSP-yyyy-mm-{reportId} / INSP-{reportId} 의 마지막 segment 추출.
     * OpsDocLinkService 의 docNo 채번 규칙과 정합.
     */
    private Long parseInspectReportId(String docNo) {
        if (docNo == null || docNo.isBlank()) return null;
        int lastDash = docNo.lastIndexOf('-');
        if (lastDash < 0 || lastDash == docNo.length() - 1) return null;
        try {
            return Long.parseLong(docNo.substring(lastDash + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ===== API: CRUD =====

    @PostMapping("/api/{type}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable("type") String type,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        OpsDocType docType = OpsDocType.fromString(type);
        if (docType == OpsDocType.INSPECT) {
            // INSPECT 는 InspectReportService.save() → OpsDocLinkService 경로로만 생성.
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", Map.of("code", "INVALID_INPUT",
                            "message", "점검내역서는 점검 보고서 저장 흐름에서 자동 연계됩니다.")));
        }
        OpsDocument doc = new OpsDocument();
        doc.setDocType(docType);
        doc.setTitle((String) body.getOrDefault("title", docType.label()));
        doc.setSysType((String) body.get("sys_type"));
        doc.setRegionCode((String) body.get("region_code"));
        doc.setEnvironment((String) body.get("environment"));
        doc.setSupportTargetType((String) body.get("support_target_type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> sectionData = (Map<String, Object>) body.get("section_data");

        String userId = currentUser != null ? currentUser.getUsername() : null;
        OpsDocument saved = opsDocService.create(doc, sectionData, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("docId", saved.getDocId());
        result.put("docNo", saved.getDocNo());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/api/{type}/{docId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable("type") String type,
            @PathVariable Long docId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        OpsDocType docType = OpsDocType.fromString(type);
        if (docType == OpsDocType.INSPECT) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", Map.of("code", "INVALID_INPUT",
                            "message", "점검내역서는 점검 보고서 수정 흐름에서 자동 갱신됩니다.")));
        }
        OpsDocument changes = new OpsDocument();
        changes.setTitle((String) body.get("title"));
        changes.setSysType((String) body.get("sys_type"));
        changes.setRegionCode((String) body.get("region_code"));
        changes.setEnvironment((String) body.get("environment"));
        changes.setSupportTargetType((String) body.get("support_target_type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> sectionData = (Map<String, Object>) body.get("section_data");

        String userId = currentUser != null ? currentUser.getUsername() : null;
        OpsDocument updated = opsDocService.update(docId, changes, sectionData, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("docId", updated.getDocId());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/api/{docId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long docId) {
        opsDocService.delete(docId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    // ===== API: 첨부 =====

    @GetMapping("/api/attachments/{docId}")
    @ResponseBody
    public List<OpsDocumentAttachment> getAttachments(@PathVariable Long docId) {
        return attachmentService.getAttachments(docId);
    }

    @PostMapping("/api/attachment/upload/{docId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @PathVariable Long docId,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        OpsDocumentAttachment att = attachmentService.saveAttachment(docId, file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("attachId", att.getAttachId());
        result.put("fileName", att.getFileName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/api/attachment/{attachId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttachment(@PathVariable Long attachId) {
        attachmentService.deleteAttachment(attachId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
