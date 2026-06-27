package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.dto.workplan.AttachmentRow;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.service.DocumentAttachmentService;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.DocumentSignedScanService;
import com.swmanager.system.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 문서 산출물 파일 컨트롤러 — 전자서명/첨부파일/날인본 스캔 (S4 Phase 4 — DocumentController 에서 분리).
 *
 * <p>기획서/개발계획 docs/{product-specs,exec-plans}/refactor-document-controller-split-phase4.md.
 * 권한은 DocumentAccessSupport(admin→EDIT) 기준 EDIT 가드. 날인본 다운로드도 다운로드=EDIT
 * (viewer-action-button-guard) 보존. 본문은 이동 전과 동일.
 */
@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentFileController {

    @Autowired private DocumentService documentService;
    @Autowired private DocumentAttachmentService attachmentService;
    @Autowired private DocumentSignedScanService signedScanService;
    @Autowired private LogService logService;
    @Autowired private DocumentAccessSupport access;

    // === 전자서명 API ===

    @ResponseBody
    @PostMapping("/api/signature/save")
    public ResponseEntity<?> saveSignature(@RequestBody Map<String, Object> data) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            Integer docId = Integer.valueOf(data.get("docId").toString());
            String signerType = (String) data.get("signerType");
            String signerName = (String) data.get("signerName");
            String signerOrg = (String) data.get("signerOrg");
            String signatureImage = (String) data.get("signatureImage"); // Base64 data URL

            documentService.saveSignature(docId, signerType, signerName, signerOrg, signatureImage);
            logService.log(MenuName.DOCUMENT, AccessActionType.SIGN, "전자서명 저장 (문서ID: " + docId + ", " + signerType + ")");

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 첨부파일 관리 ===

    @ResponseBody
    @PostMapping("/api/attachment/upload/{docId}")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Integer docId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            var attachment = attachmentService.saveAttachment(docId, file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "attachId", attachment.getAttachId(),
                    "fileName", attachment.getFileName(),
                    "fileSize", attachment.getFileSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @ResponseBody
    @GetMapping("/api/attachments/{docId}")
    public ResponseEntity<List<AttachmentRow>> getAttachments(@PathVariable Integer docId) {
        List<AttachmentRow> result = attachmentService.getAttachments(docId)
                .stream().map(AttachmentRow::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/attachment/delete/{attachId}")
    @ResponseBody
    public ResponseEntity<?> deleteAttachment(@PathVariable Integer attachId) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        attachmentService.deleteAttachment(attachId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // === 날인본 스캔 (doc-signed-scan-upload) ===

    @ResponseBody
    @PostMapping("/api/signed-scan/upload/{docId}")
    public ResponseEntity<?> uploadSignedScan(
            @PathVariable Integer docId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        try {
            CustomUserDetails cu = access.getCurrentUser();
            Long uploaderSeq = (cu != null) ? cu.getUser().getUserSeq() : null;
            var doc = signedScanService.uploadOrReplace(docId, file, uploaderSeq);
            logService.log(MenuName.DOCUMENT, AccessActionType.UPLOAD, "날인본 스캔 업로드 (문서ID: " + docId + ")");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileName", doc.getSignedScanOrigName(),
                    "fileSize", doc.getSignedScanSize() != null ? doc.getSignedScanSize() : 0
            ));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/signed-scan/{docId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadSignedScan(@PathVariable Integer docId) {
        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT(기존 로그인만→강화)
        try {
            var resource = signedScanService.loadForDownload(docId);
            String origName = signedScanService.originalName(docId);
            String encoded = java.net.URLEncoder.encode(origName, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            logService.log(MenuName.DOCUMENT, AccessActionType.DOWNLOAD, "날인본 스캔 다운로드 (문서ID: " + docId + ")");
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @ResponseBody
    @PostMapping("/api/signed-scan/delete/{docId}")
    public ResponseEntity<?> deleteSignedScan(@PathVariable Integer docId) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        try {
            signedScanService.delete(docId);
            logService.log(MenuName.DOCUMENT, AccessActionType.DELETE, "날인본 스캔 삭제 (문서ID: " + docId + ")");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
