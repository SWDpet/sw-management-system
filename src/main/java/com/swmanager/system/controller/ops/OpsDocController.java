package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
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

    /** 통합 리스트 — 5 종 모두 표시 (점검내역서 row 도 포함). */
    @GetMapping("/list")
    public String list(Model model) {
        List<OpsDocument> docs = opsDocService.findAll();
        model.addAttribute("documents", docs);
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/list";
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
