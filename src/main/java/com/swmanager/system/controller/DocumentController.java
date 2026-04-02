package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentHistory;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ContractRepository;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.DocumentAttachmentService;
import com.swmanager.system.service.PdfExportService;
import com.swmanager.system.service.LogService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentController {

    @Autowired private DocumentService documentService;
    @Autowired private InfraRepository infraRepository;
    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private LogService logService;

    // === 권한 ===

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

    // === D-01: 문서 목록 ===

    @GetMapping("/list")
    public String documentList(@RequestParam(name = "docType", required = false) String docType,
                                @RequestParam(name = "status", required = false) String status,
                                @RequestParam(name = "infraId", required = false) Long infraId,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @PageableDefault(size = 15) Pageable pageable,
                                Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Long authorId = null; // 관리자는 전체 조회

        Page<DocumentDTO> documents = documentService.searchDocuments(
                docType, status, infraId, authorId, null, null, keyword, pageable);

        List<Infra> infraList = infraRepository.findAll(Sort.by(Sort.Direction.ASC, "cityNm", "distNm"));

        model.addAttribute("documents", documents);
        model.addAttribute("infraList", infraList);
        model.addAttribute("docType", docType);
        model.addAttribute("status", status);
        model.addAttribute("infraId", infraId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userAuth", auth);

        logService.log("문서관리", "조회", "문서 목록 조회");
        return "document/document-list";
    }

    // === D-10: 문서 상세/이력 ===

    @GetMapping("/detail/{id}")
    public String documentDetail(@PathVariable Integer id, Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Document doc = documentService.getDocumentById(id);
        DocumentDTO dto = DocumentDTO.fromEntity(doc);
        List<DocumentHistory> histories = documentService.getDocumentHistory(id);

        model.addAttribute("doc", dto);
        model.addAttribute("document", doc);
        model.addAttribute("histories", histories);
        model.addAttribute("userAuth", auth);
        model.addAttribute("users", userRepository.findByEnabledTrue());

        logService.log("문서관리", "조회", "문서 상세 조회 (ID: " + id + ")");
        return "document/document-detail";
    }

    // === 문서 생성 (공통) ===

    @GetMapping("/create")
    public String createForm(@RequestParam String docType,
                              @RequestParam(required = false) Long infraId,
                              @RequestParam(required = false) Integer contractId,
                              @RequestParam(required = false) Long projId,
                              Model model, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "작성 권한이 없습니다.");
            return "redirect:/document/list";
        }

        model.addAttribute("docType", docType);
        model.addAttribute("infraId", infraId);
        model.addAttribute("contractId", contractId);
        model.addAttribute("projId", projId);
        model.addAttribute("infraList", infraRepository.findAll(Sort.by("cityNm", "distNm")));
        model.addAttribute("contracts", contractRepository.findAll());
        model.addAttribute("projects", swProjectRepository.findAll(Sort.by(Sort.Direction.DESC, "year", "projId")));
        model.addAttribute("users", userRepository.findByEnabledTrue());

        // 문서유형별 템플릿 분기
        String template = switch (docType) {
            case "COMMENCE" -> "document/doc-commence";
            case "INTERIM" -> "document/doc-interim";
            case "COMPLETION" -> "document/doc-completion";
            case "INSPECT" -> "document/doc-inspect";
            case "FAULT" -> "document/doc-fault";
            case "SUPPORT" -> "document/doc-support";
            case "INSTALL" -> "document/doc-install";
            case "PATCH" -> "document/doc-patch";
            default -> "document/document-list";
        };
        return template;
    }

    // === 문서 저장 (공통 API) ===

    @ResponseBody
    @PostMapping("/api/save")
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestBody Map<String, Object> requestData) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            CustomUserDetails cu = getCurrentUser();
            User author = cu != null ? cu.getUser() : null;

            String docType = (String) requestData.get("docType");
            String sysType = (String) requestData.get("sysType");
            Long infraId = requestData.get("infraId") != null ? Long.valueOf(requestData.get("infraId").toString()) : null;
            Integer contractId = requestData.get("contractId") != null ? Integer.valueOf(requestData.get("contractId").toString()) : null;
            Integer planId = requestData.get("planId") != null ? Integer.valueOf(requestData.get("planId").toString()) : null;
            Long projId = requestData.get("projId") != null ? Long.valueOf(requestData.get("projId").toString()) : null;
            String title = (String) requestData.get("title");
            Integer docId = requestData.get("docId") != null ? Integer.valueOf(requestData.get("docId").toString()) : null;

            Document doc;
            if (docId != null) {
                doc = documentService.getDocumentById(docId);
                doc.setTitle(title);
                doc.setSysType(sysType);
            } else {
                doc = documentService.createDocument(docType, sysType, infraId, contractId, planId, title, author);
            }

            // sw_pjt 연결
            if (projId != null) {
                doc.setProject(swProjectRepository.findById(projId).orElse(null));
            }

            // 섹션 데이터 저장
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sections = (List<Map<String, Object>>) requestData.get("sections");
            if (sections != null) {
                for (int i = 0; i < sections.size(); i++) {
                    Map<String, Object> sec = sections.get(i);
                    String sectionKey = (String) sec.get("sectionKey");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sectionData = (Map<String, Object>) sec.get("sectionData");
                    documentService.saveSection(doc.getDocId(), sectionKey, sectionData, i);
                }
            }

            logService.log("문서관리", docId != null ? "수정" : "등록",
                    DocumentDTO.getDocTypeLabel(docType) + " " + (docId != null ? "수정" : "등록") + " (ID: " + doc.getDocId() + ")");

            return ResponseEntity.ok(Map.of("success", true, "docId", doc.getDocId(), "docNo", doc.getDocNo()));
        } catch (Exception e) {
            log.error("문서 저장 실패", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 문서 상태 변경 ===

    @ResponseBody
    @PostMapping("/api/status/{id}")
    public ResponseEntity<Map<String, Object>> changeStatus(@PathVariable Integer id,
                                                             @RequestParam String status,
                                                             @RequestParam(required = false) String comment) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        CustomUserDetails cu = getCurrentUser();
        User actor = cu != null ? cu.getUser() : null;

        Document doc = documentService.changeStatus(id, status, actor, comment);
        logService.log("문서관리", "상태변경", "문서 상태변경 (ID: " + id + " → " + status + ")");

        return ResponseEntity.ok(Map.of("success", true, "status", doc.getStatus()));
    }

    // === 문서 삭제 ===

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Integer id, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/document/list";
        }

        documentService.deleteDocument(id);
        logService.log("문서관리", "삭제", "문서 삭제 (ID: " + id + ")");
        rttr.addFlashAttribute("successMessage", "문서가 삭제되었습니다.");
        return "redirect:/document/list";
    }

    // === D-11: PDF 미리보기/출력 ===

    @Autowired private PdfExportService pdfExportService;

    @GetMapping("/preview/{id}")
    public String previewDocument(@PathVariable Integer id, Model model) {
        String html = pdfExportService.renderDocumentToHtml(id);
        model.addAttribute("htmlContent", html);
        model.addAttribute("docId", id);
        return "document/document-preview";
    }

    @ResponseBody
    @GetMapping("/api/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer id) {
        try {
            String html = pdfExportService.renderDocumentToHtml(id);
            // HTML을 PDF로 변환 (실제 구현 시 OpenHTMLToPDF 또는 wkhtmltopdf 사용)
            byte[] pdfBytes = html.getBytes("UTF-8"); // placeholder - 실제로는 PDF 변환 필요

            Document doc = documentService.getDocumentById(id);
            String filename = (doc.getDocNo() != null ? doc.getDocNo() : "document") + ".pdf";

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("PDF 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // === 전자서명 API ===

    @ResponseBody
    @PostMapping("/api/signature/save")
    public ResponseEntity<Map<String, Object>> saveSignature(@RequestBody Map<String, Object> data) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            Integer docId = Integer.valueOf(data.get("docId").toString());
            String signerType = (String) data.get("signerType");
            String signerName = (String) data.get("signerName");
            String signerOrg = (String) data.get("signerOrg");
            String signatureImage = (String) data.get("signatureImage"); // Base64 data URL

            documentService.saveSignature(docId, signerType, signerName, signerOrg, signatureImage);
            logService.log("문서관리", "서명", "전자서명 저장 (문서ID: " + docId + ", " + signerType + ")");

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 첨부파일 관리 ===

    @Autowired private DocumentAttachmentService attachmentService;

    @ResponseBody
    @PostMapping("/api/attachment/upload/{docId}")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @PathVariable Integer docId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (!"EDIT".equals(getAuth())) {
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
    public ResponseEntity<List<Map<String, Object>>> getAttachments(@PathVariable Integer docId) {
        var attachments = attachmentService.getAttachments(docId);
        List<Map<String, Object>> result = attachments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("attachId", a.getAttachId());
            m.put("fileName", a.getFileName());
            m.put("fileSize", a.getFileSize());
            m.put("mimeType", a.getMimeType());
            m.put("uploadedAt", a.getUploadedAt().toString());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/attachment/delete/{attachId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttachment(@PathVariable Integer attachId) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        attachmentService.deleteAttachment(attachId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // === sw_pjt 프로젝트 정보 API (착수계/기성계/준공계용) ===

    @ResponseBody
    @GetMapping("/api/project/{projId}")
    public ResponseEntity<Map<String, Object>> getProjectInfo(@PathVariable Long projId) {
        return swProjectRepository.findById(projId).map(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("projId", p.getProjId());
            data.put("projNm", p.getProjNm());
            data.put("sysNm", p.getSysNm());
            data.put("client", p.getClient());
            data.put("orgNm", p.getOrgNm());
            data.put("cityNm", p.getCityNm());
            data.put("distNm", p.getDistNm());
            data.put("contAmt", p.getContAmt());
            data.put("swAmt", p.getSwAmt());
            data.put("contDt", p.getContDt() != null ? p.getContDt().toString() : null);
            data.put("startDt", p.getStartDt() != null ? p.getStartDt().toString() : null);
            data.put("endDt", p.getEndDt() != null ? p.getEndDt().toString() : null);
            data.put("instDt", p.getInstDt() != null ? p.getInstDt().toString() : null);
            data.put("contEnt", p.getContEnt());
            data.put("contDept", p.getContDept());
            data.put("contType", p.getContType());
            data.put("bizType", p.getBizType());
            data.put("bizCat", p.getBizCat());
            data.put("prePay", p.getPrePay());
            data.put("payProg", p.getPayProg());
            data.put("payComp", p.getPayComp());
            data.put("year", p.getYear());
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }

    // === 사업 검색 3단계 필터 API ===

    /** 1단계: 연도 목록 */
    @ResponseBody
    @GetMapping("/api/project-years")
    public ResponseEntity<List<Integer>> getProjectYears() {
        return ResponseEntity.ok(swProjectRepository.findDistinctYears());
    }

    /** 2단계: 연도 선택 → 지자체 목록 */
    @ResponseBody
    @GetMapping("/api/project-cities")
    public ResponseEntity<List<Map<String, String>>> getProjectCities(@RequestParam Integer year) {
        var list = swProjectRepository.findDistinctCityDistByYear(year);
        List<Map<String, String>> result = list.stream().map(row -> {
            Map<String, String> m = new HashMap<>();
            m.put("cityNm", (String) row[0]);
            m.put("distNm", (String) row[1]);
            m.put("label", row[0] + " " + row[1]);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /** 3단계: 연도+지자체 → 시스템영문명 목록 */
    @ResponseBody
    @GetMapping("/api/project-systems")
    public ResponseEntity<List<String>> getProjectSystems(
            @RequestParam Integer year, @RequestParam String cityNm, @RequestParam String distNm) {
        return ResponseEntity.ok(swProjectRepository.findDistinctSysNmEnByYearAndCity(year, cityNm, distNm));
    }

    /** 최종: 연도+지자체+시스템 → 사업 목록 */
    @ResponseBody
    @GetMapping("/api/projects")
    public ResponseEntity<List<Map<String, Object>>> getProjectsFiltered(
            @RequestParam Integer year, @RequestParam String cityNm,
            @RequestParam String distNm, @RequestParam String sysNmEn) {
        var projects = swProjectRepository.findByYearAndCityNmAndDistNmAndSysNmEnOrderByProjIdDesc(
                year, cityNm, distNm, sysNmEn);
        List<Map<String, Object>> result = projects.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("projId", p.getProjId());
            m.put("year", p.getYear());
            m.put("projNm", p.getProjNm());
            m.put("sysNm", p.getSysNm());
            m.put("sysNmEn", p.getSysNmEn());
            m.put("contAmt", p.getContAmt());
            m.put("cityNm", p.getCityNm());
            m.put("distNm", p.getDistNm());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
