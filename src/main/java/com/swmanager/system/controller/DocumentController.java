package com.swmanager.system.controller;

import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.domain.workplan.DocumentHistory;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.dto.workplan.AttachmentRow;
import com.swmanager.system.dto.workplan.BatchTargetRow;
import com.swmanager.system.dto.workplan.DocumentSaveResult;
import com.swmanager.system.dto.workplan.PlanData;
import com.swmanager.system.dto.workplan.PlanManpowerRow;
import com.swmanager.system.dto.workplan.PlanScheduleRow;
import com.swmanager.system.dto.workplan.PlanTargetRow;
import com.swmanager.system.dto.workplan.SystemAllRow;
import com.swmanager.system.dto.workplan.UserInfoRow;
import com.swmanager.system.dto.workplan.UserInfoSecureRow;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.response.ApiResult;
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
    @Autowired private PersonInfoRepository personInfoRepository;
    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private com.swmanager.system.repository.OrgUnitRepository orgUnitRepository;
    @Autowired private com.swmanager.system.service.OrgUnitService orgUnitService;
    // [S4 §6-5] sigunguCodeRepository/sysMstRepository → DocumentLookupController 로 이동(미사용 제거).
    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;
    // [S4 §6-5] processMasterRepository/servicePurposeRepository → DocumentLookupController 로 이동.
    // [S4 §6-5] contractParticipantRepository → DocumentParticipantController 로 이동.
    @Autowired private com.swmanager.system.repository.PjtTargetRepository pjtTargetRepository;
    @Autowired private com.swmanager.system.repository.PjtManpowerPlanRepository pjtManpowerPlanRepository;
    @Autowired private com.swmanager.system.repository.PjtScheduleRepository pjtScheduleRepository;
    // [S4 giant-class-split] 점검 전용 의존성(inspectReportService/inspectPdfService/
    // inspectMetricChartService/metricSnapshotRepository) → InspectReportController 로 이동.

    // === 권한 ===

    // [S4 Phase 1] 인증/권한 판정은 DocumentAccessSupport 로 일원화 — wrapper 유지(잔존 호출부 무변경).
    private CustomUserDetails getCurrentUser() { return access.getCurrentUser(); }

    private boolean isAdmin() { return access.isAdmin(); }

    private String getAuth() { return access.getAuth(); }

    // === D-01: 문서 목록 ===

    @GetMapping("/list")
    public String documentList(@RequestParam(name = "docType", required = false) String docType,
                                @RequestParam(name = "cityNm", required = false) String cityNm,
                                @RequestParam(name = "distNm", required = false) String distNm,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "authorName", required = false) String authorName,
                                @PageableDefault(size = 15) Pageable pageable,
                                Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Long authorId = null; // 관리자는 전체 조회

        Page<DocumentDTO> documents = documentService.searchDocuments(
                docType, null, cityNm, distNm, authorId, null, null, keyword, authorName, pageable);

        // 시도 목록 (드롭다운용), 시군구 목록 (선택된 시도 기준)
        List<String> cityList = documentService.getCityNames();
        List<String> distList = (cityNm != null && !cityNm.isBlank())
                ? documentService.getDistNamesByCity(cityNm)
                : List.of();

        model.addAttribute("documents", documents);
        model.addAttribute("cityList", cityList);
        model.addAttribute("distList", distList);
        model.addAttribute("docType", docType);
        model.addAttribute("cityNm", cityNm);
        model.addAttribute("distNm", distNm);
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorName", authorName);
        model.addAttribute("userAuth", auth);

        logService.log(MenuName.DOCUMENT, AccessActionType.VIEW, "문서 목록 조회");
        return "document/document-list";
    }

    /** GET /document/api/dist-list?cityNm={시도} - 시군구 카스케이드 로딩용 */
    @GetMapping("/api/dist-list")
    @ResponseBody
    public List<String> getDistList(@RequestParam String cityNm) {
        return documentService.getDistNamesByCity(cityNm);
    }

    // [S4 Phase 1] /excel-list·api/pdf·hwpx·excel·zip·bulk-zip 다운로드는 DocumentDownloadController 로 이동.

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

        logService.log(MenuName.DOCUMENT, AccessActionType.VIEW, "문서 상세 조회 (ID: " + id + ")");
        return "document/document-detail";
    }

    // === 문서 생성 (공통) ===

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false, defaultValue = "") String docType,
                              @RequestParam(required = false) Long infraId,
                              @RequestParam(required = false) Long projId,
                              @RequestParam(required = false) Integer docId,
                              @RequestParam(required = false) Long reportId,
                              Model model, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "작성 권한이 없습니다.");
            return "redirect:/document/list";
        }
        if (docType == null || docType.isBlank()) {
            rttr.addFlashAttribute("errorMessage", "문서 유형을 선택해주세요.");
            return "redirect:/document/list";
        }

        model.addAttribute("docType", docType);
        model.addAttribute("infraId", infraId);
        model.addAttribute("projId", projId);
        model.addAttribute("reportId", reportId);
        model.addAttribute("infraList", infraRepository.findAll(Sort.by("cityNm", "distNm")));
        model.addAttribute("projects", swProjectRepository.findAll(Sort.by(Sort.Direction.DESC, "year", "projId")));
        model.addAttribute("users", userRepository.findByEnabledTrue());

        // === 수정 모드: 기존 문서 데이터 로드 ===
        if (docId != null) {
            try {
                Document existingDoc = documentService.getDocumentById(docId);
                Map<String, Object> existingData = new HashMap<>();
                existingData.put("docId", existingDoc.getDocId());
                existingData.put("docNo", existingDoc.getDocNo());
                existingData.put("title", existingDoc.getTitle());
                existingData.put("sysType", existingDoc.getSysType());
                existingData.put("projId", existingDoc.getProject() != null ? existingDoc.getProject().getProjId() : null);

                Map<String, Map<String, Object>> sectionsMap = new HashMap<>();
                if (existingDoc.getDetails() != null) {
                    for (DocumentDetail d : existingDoc.getDetails()) {
                        sectionsMap.put(d.getSectionKey(), d.getSectionData());
                    }
                }
                existingData.put("sections", sectionsMap);

                // 프로젝트 정보 (시도/시군구/시스템 드롭다운 사전 세팅용)
                if (existingDoc.getProject() != null) {
                    var p = existingDoc.getProject();
                    existingData.put("projYear", p.getYear());
                    existingData.put("projCityNm", p.getCityNm());
                    existingData.put("projDistNm", p.getDistNm());
                    existingData.put("projSysNmEn", p.getSysNmEn());
                    existingData.put("projNm", p.getProjNm());
                }

                // [스프린트 5 FR-1-F] 레거시 대상 표시용 배너 텍스트
                String legacyTargetText = null;
                if (existingDoc.getProject() != null) {
                    var p = existingDoc.getProject();
                    legacyTargetText = String.format("%s %s - %s (%s)",
                            p.getCityNm() != null ? p.getCityNm() : "",
                            p.getDistNm() != null ? p.getDistNm() : "",
                            p.getSysNm() != null ? p.getSysNm() : (p.getSysNmEn() != null ? p.getSysNmEn() : ""),
                            p.getYear() != null ? p.getYear() : "");
                } else if (existingDoc.getInfra() != null) {
                    var inf = existingDoc.getInfra();
                    legacyTargetText = String.format("%s %s - %s",
                            inf.getCityNm() != null ? inf.getCityNm() : "",
                            inf.getDistNm() != null ? inf.getDistNm() : "",
                            inf.getSysNm() != null ? inf.getSysNm() : "");
                }
                if (legacyTargetText != null) legacyTargetText = legacyTargetText.trim();
                model.addAttribute("legacyTargetText", legacyTargetText);

                // [스프린트 5] 기존 레코드의 support/environment 도 템플릿 노출
                model.addAttribute("existingSupportTargetType", existingDoc.getSupportTargetType());
                model.addAttribute("existingOrgUnitId",
                        existingDoc.getOrgUnit() != null ? existingDoc.getOrgUnit().getUnitId() : null);
                model.addAttribute("existingEnvironment", existingDoc.getEnvironment());

                model.addAttribute("existingDoc", existingData);
                model.addAttribute("existingDocId", docId);
            } catch (Exception e) {
                log.error("기존 문서 로드 실패 (docId={})", docId, e);
            }
        }

        // doc-split-ops: INSPECT 는 DocumentType enum 에서 제거됐지만 점검내역서 작성 화면
        // (doc-inspect.html) 은 90% 완성된 InspectReport 흐름을 그대로 사용. 직접 라우팅.
        if ("INSPECT".equalsIgnoreCase(docType)) {
            model.addAttribute("isAdmin", isAdmin());
            return "document/doc-inspect";
        }

        // 사업문서 3 종 (COMMENCE/INTERIM/COMPLETION) 만 enum 매핑.
        DocumentType docTypeEnum;
        try {
            docTypeEnum = DocumentType.fromString(docType);
        } catch (IllegalArgumentException e) {
            docTypeEnum = null;
        }
        if (docTypeEnum != null) return docTypeEnum.templateName();

        // 알 수 없는 docType 은 안전하게 사업문서 목록으로 redirect (document-list 가 documents
        // null 일 때 NPE 던지는 걸 방지 — codex P1 회귀 방어).
        rttr.addFlashAttribute("errorMessage", "지원하지 않는 문서 유형입니다: " + docType);
        return "redirect:/document/list";
    }

    // === 문서 저장 (공통 API) ===

    @ResponseBody
    @PostMapping("/api/save")
    public ResponseEntity<?> saveDocument(@RequestBody Map<String, Object> requestData) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            CustomUserDetails cu = getCurrentUser();
            User author = cu != null ? cu.getUser() : null;

            String docTypeRaw = (String) requestData.get("docType");
            DocumentType docType;
            try {
                docType = DocumentType.fromString(docTypeRaw);
            } catch (IllegalArgumentException iae) {
                return ResponseEntity.badRequest().body(Map.of("success", false,
                        "error", Map.of("code", "INVALID_INPUT", "message", "유효하지 않은 문서유형입니다: " + docTypeRaw)));
            }
            String sysType = (String) requestData.get("sysType");
            Long infraId = requestData.get("infraId") != null ? Long.valueOf(requestData.get("infraId").toString()) : null;
            Integer contractId = requestData.get("contractId") != null ? Integer.valueOf(requestData.get("contractId").toString()) : null;
            Integer planId = requestData.get("planId") != null ? Integer.valueOf(requestData.get("planId").toString()) : null;
            Long projId = requestData.get("projId") != null ? Long.valueOf(requestData.get("projId").toString()) : null;
            String title = (String) requestData.get("title");
            Integer docId = requestData.get("docId") != null ? Integer.valueOf(requestData.get("docId").toString()) : null;

            String docNo = (String) requestData.get("docNo"); // 수동입력 문서번호

            // doc-split-ops: 사업문서 3 종 (COMMENCE/INTERIM/COMPLETION) 만 처리.
            // 운영문서 5 종 (INSPECT/FAULT/SUPPORT/INSTALL/PATCH) 은 OpsDocController 가 담당.
            if (!(DocumentType.COMMENCE == docType || DocumentType.INTERIM == docType || DocumentType.COMPLETION == docType)) {
                return ResponseEntity.badRequest().body(Map.of("success", false,
                        "error", Map.of("code", "INVALID_INPUT", "message", "사업문서(착수/기성/준공)만 이 엔드포인트에서 처리합니다.")));
            }

            // [중복 방지] 착수계/준공계 = projId 당 1건, 기성계 = projId+회차 당 1건
            if (projId != null && (DocumentType.COMMENCE == docType
                        || DocumentType.COMPLETION == docType
                        || DocumentType.INTERIM == docType)) {
                Integer round = null;
                if (DocumentType.INTERIM == docType) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> _secs = (List<Map<String, Object>>) requestData.get("sections");
                    if (_secs != null) {
                        for (Map<String, Object> sec : _secs) {
                            if ("inspector".equals(sec.get("sectionKey"))) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> sd = (Map<String, Object>) sec.get("sectionData");
                                Object r = sd != null ? sd.get("paymentRound") : null;
                                if (r != null && !r.toString().isBlank()) {
                                    try { round = Integer.parseInt(r.toString().trim()); } catch (NumberFormatException ignore) {}
                                }
                                break;
                            }
                        }
                    }
                }
                Integer dupId = documentService.findDuplicateProjDoc(projId, docType, round, docId);
                if (dupId != null) {
                    String msg = (DocumentType.INTERIM == docType)
                            ? "이 사업의 " + (round != null ? round + "차 " : "동일 회차 ") + "기성계가 이미 작성되었습니다."
                            : "이 사업의 " + DocumentDTO.getDocTypeLabel(docType) + " 가 이미 작성되었습니다.";
                    return ResponseEntity.badRequest().body(Map.of("success", false,
                            "error", Map.of("code", "DUPLICATE", "message", msg, "existingDocId", dupId)));
                }
            }

            Document doc;
            if (docId != null) {
                doc = documentService.getDocumentById(docId);
                doc.setTitle(title);
                doc.setSysType(sysType);
            } else {
                doc = documentService.createDocument(docType, sysType, infraId, contractId, planId, title, author);
            }
            // 문서번호 수동 저장 (빈칸 허용)
            if (docNo != null) {
                doc.setDocNo(docNo.trim().isEmpty() ? null : docNo.trim());
            }

            // 사업문서: 사업(proj_id) 연결 유지
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

            logService.log(MenuName.DOCUMENT, docId != null ? AccessActionType.UPDATE : AccessActionType.CREATE,
                    DocumentDTO.getDocTypeLabel(docType) + " " + (docId != null ? "수정" : "등록") + " (ID: " + doc.getDocId() + ")");
            // Note: DocumentDTO.getDocTypeLabel(DocumentType) 은 Enum 직접 전달

            return ResponseEntity.ok(new DocumentSaveResult(
                    true, doc.getDocId(), doc.getDocNo() != null ? doc.getDocNo() : ""));
        } catch (Exception e) {
            log.error("문서 저장 실패", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 문서 상태 변경 (DRAFT ↔ COMPLETED) ===

    @ResponseBody
    @PostMapping("/api/status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id,
                                          @RequestParam DocumentStatus status,
                                          @RequestParam(required = false) String comment) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        // ConverterFactory 가 DRAFT/COMPLETED 만 허용. 그 외는 400 자동 응답.

        CustomUserDetails cu = getCurrentUser();
        User actor = cu != null ? cu.getUser() : null;

        Document doc = documentService.changeStatus(id, status, actor, comment);
        logService.log(MenuName.DOCUMENT, AccessActionType.UPDATE, "문서 상태변경 (ID: " + id + " → " + status.name() + ")");

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
        logService.log(MenuName.DOCUMENT, AccessActionType.DELETE, "문서 삭제 (ID: " + id + ")");
        rttr.addFlashAttribute("successMessage", "문서가 삭제되었습니다.");
        return "redirect:/document/list";
    }

    // === D-11: PDF 미리보기/출력 ===

    @Autowired private PdfExportService pdfExportService;   // [S4] previewDocument(renderDocumentToHtml) 전용 잔존
    @Autowired private com.swmanager.system.security.DocumentAccessSupport access;  // [S4] 인증/권한 위임

    @GetMapping("/preview/{id}")
    public String previewDocument(@PathVariable Integer id, Model model) {
        String html = pdfExportService.renderDocumentToHtml(id);
        model.addAttribute("htmlContent", html);
        model.addAttribute("docId", id);
        return "document/document-preview";
    }

    // [S4 Phase 1] 단건/일괄 다운로드(pdf·hwpx·excel·zip·bulk-zip)와 zip/bulk 헬퍼는
    // DocumentDownloadController 로 이동(refactor-document-controller-split).

    // === 전자서명 API ===

    @ResponseBody
    @PostMapping("/api/signature/save")
    public ResponseEntity<?> saveSignature(@RequestBody Map<String, Object> data) {
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
            logService.log(MenuName.DOCUMENT, AccessActionType.SIGN, "전자서명 저장 (문서ID: " + docId + ", " + signerType + ")");

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 첨부파일 관리 ===

    @Autowired private DocumentAttachmentService attachmentService;

    @ResponseBody
    @PostMapping("/api/attachment/upload/{docId}")
    public ResponseEntity<?> uploadAttachment(
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
    public ResponseEntity<List<AttachmentRow>> getAttachments(@PathVariable Integer docId) {
        List<AttachmentRow> result = attachmentService.getAttachments(docId)
                .stream().map(AttachmentRow::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/attachment/delete/{attachId}")
    @ResponseBody
    public ResponseEntity<?> deleteAttachment(@PathVariable Integer attachId) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        attachmentService.deleteAttachment(attachId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // === 날인본 스캔 (doc-signed-scan-upload) ===

    @Autowired private com.swmanager.system.service.DocumentSignedScanService signedScanService;

    @ResponseBody
    @PostMapping("/api/signed-scan/upload/{docId}")
    public ResponseEntity<?> uploadSignedScan(
            @PathVariable Integer docId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }
        try {
            CustomUserDetails cu = getCurrentUser();
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
        if (!"EDIT".equals(getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT(기존 로그인만→강화)
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
        if (!"EDIT".equals(getAuth())) {
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

    // === 사용자 정보 API (현장대리인/과업참여자용) ===

    /**
     * 사용자 기본 정보 (비민감) — 감사 P1-3 조치 (2026-04-18):
     * 민감 필드(ssn/certificate/email) 는 이 엔드포인트에서 제거. 민감 정보가 필요하면
     * EDIT 권한 + {@code /api/user/{userSeq}/secure} 사용.
     */
    @ResponseBody
    @GetMapping("/api/user/{userSeq}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userSeq) {
        return userRepository.findById(userSeq)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(UserInfoRow.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 전체 정보 (민감 필드 포함) — 감사 P1-3 조치 (2026-04-18):
     * ssn / certificate / email 이 필요한 화면(예: 과업 착수 문서 작성) 전용.
     * EDIT 권한 필수 — 비-EDIT 사용자는 403.
     */
    @ResponseBody
    @GetMapping("/api/user/{userSeq}/secure")
    public ResponseEntity<?> getUserInfoSecure(@PathVariable Long userSeq) {
        if (!"EDIT".equals(getAuth())) {
            // 현행 wire 보존: {error:{code,message}} (success 키 없음 — ApiResult.fail 은 success 추가하므로 미사용)
            return ResponseEntity.status(403)
                    .body(Map.of("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다")));
        }
        return userRepository.findById(userSeq)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(UserInfoSecureRow.from(u)))
                .orElse(ResponseEntity.notFound().build());
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
            data.put("sysNmEn", p.getSysNmEn());
            data.put("client", p.getClient());
            data.put("orgNm", p.getOrgNm());
            data.put("orgLghNm", p.getOrgLghNm());
            data.put("cityNm", p.getCityNm());
            data.put("distNm", p.getDistNm());
            data.put("distCd", p.getDistCd());
            data.put("contAmt", p.getContAmt());
            data.put("contRt", p.getContRt());
            data.put("swAmt", p.getSwAmt());
            data.put("swRt", p.getSwRt());
            data.put("hwAmt", p.getHwAmt());
            data.put("contDt", p.getContDt() != null ? p.getContDt().toString() : null);
            data.put("startDt", p.getStartDt() != null ? p.getStartDt().toString() : null);
            data.put("endDt", p.getEndDt() != null ? p.getEndDt().toString() : null);
            data.put("instDt", p.getInstDt() != null ? p.getInstDt().toString() : null);
            data.put("contEnt", p.getContEnt());
            data.put("contDept", p.getContDept());
            data.put("contType", p.getContType());
            data.put("bizType", p.getBizType());
            data.put("bizCat", p.getBizCat());
            data.put("bizCatEn", p.getBizCatEn());
            data.put("maintType", p.getMaintType());
            data.put("prePay", p.getPrePay());
            data.put("payProg", p.getPayProg());
            data.put("payComp", p.getPayComp());
            data.put("year", p.getYear());

            // PersonInfo 연동 (담당자 정보)
            if (p.getPersonId() != null) {
                personInfoRepository.findById(p.getPersonId()).ifPresent(pi -> {
                    data.put("personNm", pi.getUserNm());
                    data.put("personTel", pi.getTel());
                    data.put("personEmail", pi.getEmail());
                    data.put("personDept", pi.getDeptNm());
                    data.put("personTeam", pi.getTeamNm());
                    data.put("personPos", pi.getPos());
                    data.put("personOrg", pi.getOrgNm());
                });
            }
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }

    // [S4 §6-5] 사업 검색 cascade 조회 12종(project/region/infra/projects) → DocumentLookupController 분리.

    // ========== 일괄 작성 기능 ==========

    /** 일괄 작성 페이지 */
    @GetMapping("/batch")
    public String batchPage(Model model) {
        if (!"EDIT".equals(getAuth())) {
            return "redirect:/document/list";
        }
        model.addAttribute("users", userRepository.findByEnabledTrue());
        return "document/doc-batch";
    }

    /** 일괄 대상 사업 목록 조회 API */
    @ResponseBody
    @GetMapping("/api/project-systems-all")
    public ResponseEntity<List<SystemAllRow>> getAllSystemsForYear(@RequestParam Integer year) {
        var projects = swProjectRepository.findAll().stream()
                .filter(p -> year.equals(p.getYear()))
                .filter(p -> p.getSysNmEn() != null && !p.getSysNmEn().isEmpty())
                .toList();
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        projects.forEach(p -> map.putIfAbsent(p.getSysNmEn(), p.getSysNm()));
        List<SystemAllRow> result = map.entrySet().stream()
                .map(e -> new SystemAllRow(e.getKey(), e.getValue()))
                .sorted((a, b) -> String.valueOf(a.sysNmEn()).compareTo(String.valueOf(b.sysNmEn())))
                .toList();
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/batch/targets")
    public ResponseEntity<List<BatchTargetRow>> getBatchTargets(
            @RequestParam Integer year, @RequestParam String docType,
            @RequestParam(required = false) String sysNmEn) {

        List<com.swmanager.system.domain.SwProject> projects;
        DocumentType docTypeEnum;
        try {
            docTypeEnum = DocumentType.fromString(docType);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(List.of());
        }
        if (DocumentType.INTERIM == docTypeEnum) {
            projects = swProjectRepository.findByYearAndInterimYnOrderByCityNmAscDistNmAsc(year, "Y");
        } else if (DocumentType.COMPLETION == docTypeEnum) {
            projects = swProjectRepository.findByYearAndCompletionYnOrderByCityNmAscDistNmAsc(year, "Y");
        } else {
            return ResponseEntity.badRequest().body(List.of());
        }
        if (sysNmEn != null && !sysNmEn.isEmpty()) {
            projects = projects.stream().filter(p -> sysNmEn.equals(p.getSysNmEn())).toList();
        }

        List<BatchTargetRow> result = projects.stream().map(BatchTargetRow::from).toList();
        return ResponseEntity.ok(result);
    }

    /** 일괄 자동 생성 API */
    @ResponseBody
    @PostMapping("/api/batch/generate")
    public ResponseEntity<Map<String, Object>> batchGenerate(@RequestBody Map<String, Object> requestData) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            CustomUserDetails cu = getCurrentUser();
            User author = cu != null ? cu.getUser() : null;

            String docTypeRaw = (String) requestData.get("docType");
            DocumentType docType;
            try {
                docType = DocumentType.fromString(docTypeRaw);
            } catch (IllegalArgumentException iae) {
                return ResponseEntity.badRequest().body(Map.of("error", "유효하지 않은 문서유형: " + docTypeRaw));
            }
            @SuppressWarnings("unchecked")
            List<Number> projIds = (List<Number>) requestData.get("projIds");
            @SuppressWarnings("unchecked")
            Map<String, Object> commonData = (Map<String, Object>) requestData.get("commonData");

            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Number projIdNum : projIds) {
                Long projId = projIdNum.longValue();
                try {
                    var projOpt = swProjectRepository.findById(projId);
                    if (projOpt.isEmpty()) {
                        failCount++;
                        results.add(Map.of("projId", projId, "success", false, "error", "프로젝트 없음"));
                        continue;
                    }
                    var p = projOpt.get();

                    // 문서 생성 - 기성계는 "기성금 신청 건", 준공계는 "준공계 제출 건"
                    String title = "「" + p.getProjNm() + "」" +
                            ((DocumentType.INTERIM == docType) ? "기성금 신청 건" : "준공계 제출 건");

                    Document doc = documentService.createDocument(docType, p.getSysNmEn(), null, null, null, title, author);
                    doc.setProject(p);

                    // 공문(letter) 섹션 자동 생성
                    Map<String, Object> letterData = buildBatchLetterData(p, docType, commonData);
                    documentService.saveSection(doc.getDocId(), "letter", letterData, 0);

                    // 본문 섹션 자동 생성
                    if (DocumentType.INTERIM == docType) {
                        // inspector 섹션 (기성검사원)
                        Map<String, Object> insp = new HashMap<>();
                        insp.put("name", p.getProjNm());
                        insp.put("amount", p.getContAmt() != null ? p.getContAmt().toString() : "");
                        insp.put("contractDate", p.getContDt() != null ? p.getContDt().toString() : "");
                        insp.put("periodFrom", p.getStartDt() != null ? p.getStartDt().toString() : "");
                        insp.put("periodTo", p.getEndDt() != null ? p.getEndDt().toString() : "");
                        if (commonData != null) {
                            if (commonData.get("interimYear") != null) insp.put("interimYear", commonData.get("interimYear"));
                            if (commonData.get("interimMonth") != null) insp.put("interimMonth", commonData.get("interimMonth"));
                            if (commonData.get("interimDay") != null) insp.put("interimDay", commonData.get("interimDay"));
                            Object pr = commonData.get("paymentRate");
                            if (pr != null && !pr.toString().isEmpty()) {
                                insp.put("paymentRate", pr);
                                // 기성율 × 계약금 → 금회기성금액 자동 계산
                                try {
                                    double rate = Double.parseDouble(pr.toString());
                                    long contAmt = p.getContAmt() != null ? p.getContAmt() : 0L;
                                    long paymentAmt = Math.round(contAmt * rate / 100.0);
                                    insp.put("paymentAmount", String.valueOf(paymentAmt));
                                } catch (NumberFormatException ignore) {}
                            }
                        }
                        documentService.saveSection(doc.getDocId(), "inspector", insp, 1);

                        // detail_sheet 섹션 (기성내역서)
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("name", p.getProjNm());
                        detail.put("contAmt", p.getContAmt());
                        detail.put("bidRate", p.getContRt());
                        if (commonData != null) {
                            if (commonData.get("periodText") != null) detail.put("periodText", commonData.get("periodText"));
                            if (commonData.get("prevRate") != null) detail.put("prevRate", commonData.get("prevRate"));
                        }
                        // KRAS 시스템이면 GeoNURIS for KRAS v1.0 항목 자동 추가
                        String sysEn = p.getSysNmEn() != null ? p.getSysNmEn().toUpperCase() : "";
                        String sysKo = p.getSysNm() != null ? p.getSysNm() : "";
                        if (sysEn.contains("KRAS") || sysKo.contains("KRAS")) {
                            List<Map<String, Object>> items = new ArrayList<>();
                            Map<String, Object> it = new HashMap<>();
                            it.put("name", "GeoNURIS for KRAS v1.0");
                            it.put("unitPrice", 77000000L);
                            items.add(it);
                            detail.put("items", items);
                        }
                        documentService.saveSection(doc.getDocId(), "detail_sheet", detail, 2);
                    } else {
                        Map<String, Object> compData = new HashMap<>();
                        compData.put("name", p.getProjNm());
                        compData.put("amount", p.getContAmt() != null ? p.getContAmt().toString() : "");
                        compData.put("contractDate", p.getContDt() != null ? p.getContDt().toString() : "");
                        compData.put("startDate", p.getStartDt() != null ? p.getStartDt().toString() : "");
                        compData.put("endDate", p.getEndDt() != null ? p.getEndDt().toString() : "");
                        documentService.saveSection(doc.getDocId(), "completion", compData, 1);
                    }

                    successCount++;
                    results.add(Map.of("projId", projId, "success", true, "docId", doc.getDocId(),
                            "projNm", p.getProjNm(), "cityNm", p.getCityNm(), "distNm", p.getDistNm()));

                } catch (Exception e) {
                    failCount++;
                    results.add(Map.of("projId", projId, "success", false, "error", e.getMessage()));
                }
            }

            logService.log(MenuName.DOCUMENT, AccessActionType.BATCH,
                    docType.name() + " 일괄생성 (성공: " + successCount + ", 실패: " + failCount + ")");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalCount", projIds.size(),
                    "successCount", successCount,
                    "failCount", failCount,
                    "results", results
            ));
        } catch (Exception e) {
            log.error("일괄 생성 실패", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** 일괄 생성용 공문(letter) 데이터 빌드 */
    private Map<String, Object> buildBatchLetterData(com.swmanager.system.domain.SwProject p,
                                                      DocumentType docType, Map<String, Object> commonData) {
        Map<String, Object> data = new HashMap<>();

        // 수신자 자동 생성
        String recipient = p.getOrgNm() != null ? p.getOrgNm() :
                (p.getDistNm() != null ? p.getDistNm() + "청" : p.getCityNm() + "청");
        data.put("to", recipient);

        // 공통 데이터 (담당자, 문서번호 등)
        if (commonData != null) {
            if (commonData.get("manager") != null) data.put("manager", commonData.get("manager"));
            if (commonData.get("tel") != null) data.put("tel", commonData.get("tel"));
            if (commonData.get("date") != null) data.put("date", commonData.get("date"));
        }

        // 제목 - 기성계는 "기성금 신청 건", 준공계는 "준공계 제출 건"
        String titleSuffix = DocumentType.INTERIM == docType ? "기성금 신청 건" : "준공계 제출 건";
        data.put("title", "「" + p.getProjNm() + "」" + titleSuffix);

        // 본문
        String contDtFmt = "";
        if (p.getContDt() != null) {
            contDtFmt = p.getContDt().getYear() + ". " +
                    p.getContDt().getMonthValue() + ". " +
                    p.getContDt().getDayOfMonth() + ".";
        }

        String body2;
        String attachList;
        if (DocumentType.INTERIM == docType) {
            body2 = "2. 귀 기관과 당사 간에 계약(" + contDtFmt + ")한 『" + p.getProjNm() +
                    "』과 관련하여 붙임과 같이 기성을 신청하오니 검토 후 조치하여 주시기 바랍니다.";
            attachList = "1. 기성검사원 1부.\n                          2. 기성내역서 1부.\n                          3. 점검내역서 1부.    끝.";
        } else {
            body2 = "2. 귀 기관과 당사 간에 계약(" + contDtFmt + ")한 『" + p.getProjNm() +
                    "』에 관하여 과업을 완료함에 따라 제출합니다.";
            attachList = "1. 준공계 2부.    끝.";
        }
        data.put("body", "1. 귀 기관의 무궁한 발전을 기원합니다.\n\n" + body2 + "\n\n\n※ 붙 임 : " + attachList);

        return data;
    }

    // ── Phase 2: 공정명 마스터 / 용역목적 / 사업별 참여자 API ──

    // [S4 §6-5] process-master/service-purpose 조회 → DocumentLookupController 로 이동.

    // [S4 §6-5] 과업참여자(contract-participants) 3종 → DocumentParticipantController 로 분리.

    // ========================================================
    // 사업수행계획서 (P10~P13) API
    // ========================================================

    /** 사업수행계획서 데이터 조회 */
    @ResponseBody
    @GetMapping("/api/plan/{projId}")
    public ResponseEntity<?> getPlanData(@PathVariable Long projId) {
        var pOpt = swProjectRepository.findById(projId);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        var p = pOpt.get();

        List<PlanTargetRow> targets = pjtTargetRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanTargetRow::from).toList();
        List<PlanManpowerRow> manpowerPlans = pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanManpowerRow::from).toList();
        List<PlanScheduleRow> schedules = pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanScheduleRow::from).toList();

        return ResponseEntity.ok(new PlanData(
                p.getProjPurpose(), p.getSupportType(), p.getScopeText(), p.getInspectMethod(),
                targets, manpowerPlans, schedules));
    }

    /** 사업수행계획서 데이터 저장 (overwrite) — 감사 P1-2 조치: EDIT 권한 체크 (2026-04-18) */
    @ResponseBody
    @PostMapping("/api/plan/{projId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResult> savePlanData(
            @PathVariable Long projId,
            @RequestBody Map<String, Object> body) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "수정 권한이 없습니다"));
        }
        try {
            var pOpt = swProjectRepository.findById(projId);
            if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
            var p = pOpt.get();

            // sw_pjt 4 columns
            if (body.containsKey("projPurpose"))   p.setProjPurpose((String) body.get("projPurpose"));
            if (body.containsKey("supportType"))   p.setSupportType((String) body.get("supportType"));
            if (body.containsKey("scopeText"))     p.setScopeText((String) body.get("scopeText"));
            if (body.containsKey("inspectMethod")) p.setInspectMethod((String) body.get("inspectMethod"));
            swProjectRepository.save(p);

            // targets - delete & insert
            if (body.containsKey("targets")) {
                pjtTargetRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("targets");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var t = new com.swmanager.system.domain.workplan.PjtTarget();
                    t.setProjId(projId);
                    t.setProductName((String) m.get("productName"));
                    Object q = m.get("qty");
                    t.setQty(q == null ? 1 : ((Number) q).intValue());
                    t.setSortOrder(order++);
                    pjtTargetRepository.save(t);
                }
            }

            // manpower plans
            if (body.containsKey("manpowerPlans")) {
                pjtManpowerPlanRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("manpowerPlans");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var mp = new com.swmanager.system.domain.workplan.PjtManpowerPlan();
                    mp.setProjId(projId);
                    mp.setStepName((String) m.get("stepName"));
                    String sd = (String) m.get("startDt");
                    String ed = (String) m.get("endDt");
                    if (sd != null && !sd.isEmpty()) mp.setStartDt(java.time.LocalDate.parse(sd));
                    if (ed != null && !ed.isEmpty()) mp.setEndDt(java.time.LocalDate.parse(ed));
                    mp.setGradeSpecial(asInt(m.get("gradeSpecial")));
                    mp.setGradeHigh(asInt(m.get("gradeHigh")));
                    mp.setGradeMid(asInt(m.get("gradeMid")));
                    mp.setGradeLow(asInt(m.get("gradeLow")));
                    mp.setFuncHigh(asInt(m.get("funcHigh")));
                    mp.setFuncMid(asInt(m.get("funcMid")));
                    mp.setFuncLow(asInt(m.get("funcLow")));
                    mp.setRemark((String) m.get("remark"));
                    mp.setSortOrder(order++);
                    pjtManpowerPlanRepository.save(mp);
                }
            }

            // schedules
            if (body.containsKey("schedules")) {
                pjtScheduleRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("schedules");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var s = new com.swmanager.system.domain.workplan.PjtSchedule();
                    s.setProjId(projId);
                    s.setProcessName((String) m.get("processName"));
                    s.setM01(asBool(m.get("m01"))); s.setM02(asBool(m.get("m02")));
                    s.setM03(asBool(m.get("m03"))); s.setM04(asBool(m.get("m04")));
                    s.setM05(asBool(m.get("m05"))); s.setM06(asBool(m.get("m06")));
                    s.setM07(asBool(m.get("m07"))); s.setM08(asBool(m.get("m08")));
                    s.setM09(asBool(m.get("m09"))); s.setM10(asBool(m.get("m10")));
                    s.setM11(asBool(m.get("m11"))); s.setM12(asBool(m.get("m12")));
                    s.setRemark((String) m.get("remark"));
                    s.setSortOrder(order++);
                    pjtScheduleRepository.save(s);
                }
            }

            return ResponseEntity.ok(ApiResult.ok());
        } catch (Exception e) {
            log.error("사업수행계획서 저장 실패: {}", e.getMessage(), e);
            // [codex] @Transactional 내 catch+정상 return 은 롤백을 막아 부분 저장/삭제(targets 삭제 후
            //         manpowerPlans/schedules 중 예외)가 commit 됨 → 명시적 rollback-only 로 정합성 보장.
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            // 응답 형태는 현행 보존: HTTP 200 + {success:false,error:<msg>}
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    private Integer asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
    private Boolean asBool(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        return "true".equalsIgnoreCase(o.toString()) || "1".equals(o.toString());
    }

    // [S4 giant-class-split] 점검내역서 API/미리보기(14 엔드포인트 + extractSnapshotSpecs)
    // → InspectReportController 로 분리 이동. URL·동작 동일.
}
