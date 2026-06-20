package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;
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
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.DocumentAttachmentService;
import com.swmanager.system.service.PdfExportService;
import com.swmanager.system.service.HwpxExportService;
import com.swmanager.system.service.ExcelExportService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.domain.workplan.ProcessMaster;
import com.swmanager.system.domain.workplan.ServicePurpose;
import com.swmanager.system.domain.workplan.ContractParticipant;
import com.swmanager.system.repository.workplan.ProcessMasterRepository;
import com.swmanager.system.repository.workplan.ServicePurposeRepository;
import com.swmanager.system.repository.workplan.ContractParticipantRepository;

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
    @Autowired private ProcessMasterRepository processMasterRepository;
    @Autowired private ServicePurposeRepository servicePurposeRepository;
    @Autowired private ContractParticipantRepository contractParticipantRepository;
    @Autowired private com.swmanager.system.repository.PjtTargetRepository pjtTargetRepository;
    @Autowired private com.swmanager.system.repository.PjtManpowerPlanRepository pjtManpowerPlanRepository;
    @Autowired private com.swmanager.system.repository.PjtScheduleRepository pjtScheduleRepository;
    // [S4 giant-class-split] 점검 전용 의존성(inspectReportService/inspectPdfService/
    // inspectMetricChartService/metricSnapshotRepository) → InspectReportController 로 이동.

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

    /** D-01 검색 결과 Excel 다운로드 — 화면 필터(문서유형/상태/시도/시군구/검색) 그대로 적용한 전체 목록 */
    @ResponseBody
    @GetMapping("/excel-list")
    public ResponseEntity<byte[]> downloadDocumentListExcel(
            @RequestParam(name = "docType", required = false) String docType,
            @RequestParam(name = "cityNm", required = false) String cityNm,
            @RequestParam(name = "distNm", required = false) String distNm,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "authorName", required = false) String authorName) {
        if ("NONE".equals(getAuth())) {
            return ResponseEntity.status(403).build();
        }
        try {
            Page<DocumentDTO> documents = documentService.searchDocuments(
                    docType, null, cityNm, distNm, null, null, null, keyword, authorName,
                    org.springframework.data.domain.PageRequest.of(0, 100000));
            byte[] excelBytes = excelExportService.generateDocumentList(documents.getContent());

            // 문서유형 선택 시 해당 유형 라벨(착수계/기성계/준공계)을 파일명 prefix 로, 전체면 "사업문서목록"
            String prefix = "사업문서목록";
            if (docType != null && !docType.isBlank()) {
                try {
                    prefix = DocumentType.fromString(docType).label();
                } catch (IllegalArgumentException ignore) {}
            }
            String filename = prefix + "_" + java.time.LocalDate.now() + ".xlsx";
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");

            logService.log(MenuName.DOCUMENT, AccessActionType.VIEW, "문서 목록 Excel 다운로드");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(excelBytes);
        } catch (Exception e) {
            log.error("문서 목록 Excel 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
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
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestBody Map<String, Object> requestData) {
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

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("docId", doc.getDocId());
            result.put("docNo", doc.getDocNo() != null ? doc.getDocNo() : "");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("문서 저장 실패", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === 문서 상태 변경 (DRAFT ↔ COMPLETED) ===

    @ResponseBody
    @PostMapping("/api/status/{id}")
    public ResponseEntity<Map<String, Object>> changeStatus(@PathVariable Integer id,
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

    @Autowired private PdfExportService pdfExportService;
    @Autowired private HwpxExportService hwpxExportService;
    @Autowired private ExcelExportService excelExportService;

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
            byte[] pdfBytes = pdfExportService.generatePdf(id);

            Document doc = documentService.getDocumentById(id);
            String filename = (doc.getDocNo() != null ? doc.getDocNo() : "document") + ".pdf";
            // UTF-8 파일명 인코딩
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("PDF 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // === HWPX 한글문서 다운로드 ===

    @ResponseBody
    @GetMapping("/api/hwpx/{id}")
    public ResponseEntity<byte[]> downloadHwpx(@PathVariable Integer id,
                                                @RequestParam(defaultValue = "letter") String type) {
        try {
            byte[] hwpxBytes = hwpxExportService.generateHwpx(id, type);

            Document doc = documentService.getDocumentById(id);
            String projNm = (doc.getProject() != null && doc.getProject().getProjNm() != null)
                    ? doc.getProject().getProjNm() : "문서";
            String docTypeLabel = doc.getDocType() != null ? doc.getDocType().label() : "";
            String typeLabel = switch (type) {
                case "letter" -> "_공문";
                case "inspector" -> "";
                case "commence_body" -> "";
                case "completion_body" -> "_KRAS";
                case "completion_body_upis" -> "_UPIS";
                case "completion_full" -> "";
                default -> "_" + type;
            };
            String filename = projNm + "_" + docTypeLabel + typeLabel + ".hwpx";
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.hancom.hwpx")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(hwpxBytes);
        } catch (Exception e) {
            log.error("HWPX 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // === 설계내역서 Excel 다운로드 ===

    @ResponseBody
    @GetMapping("/api/excel/{id}")
    public ResponseEntity<byte[]> downloadExcel(@PathVariable Integer id) {
        try {
            Document doc = documentService.getDocumentById(id);
            byte[] excelBytes;
            String suffix;
            if (DocumentType.INTERIM == doc.getDocType()) {
                excelBytes = excelExportService.generateInterimReport(id);
                suffix = "_기성내역서.xlsx";
            } else {
                excelBytes = excelExportService.generateDesignEstimate(id);
                suffix = "_설계내역서.xlsx";
            }

            String projNm = (doc.getProject() != null && doc.getProject().getProjNm() != null)
                    ? doc.getProject().getProjNm() : "문서";
            String filename = projNm + suffix;
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(excelBytes);
        } catch (Exception e) {
            log.error("설계내역서 Excel 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // === 일괄 다운로드 (ZIP): HWP + Excel 전체 ===
    @ResponseBody
    @GetMapping("/api/zip/{id}")
    public ResponseEntity<byte[]> downloadZip(@PathVariable Integer id) {
        try {
            Document doc = documentService.getDocumentById(id);
            DocumentType docType = doc.getDocType();
            String projNm = (doc.getProject() != null && doc.getProject().getProjNm() != null)
                    ? doc.getProject().getProjNm() : "문서";
            String docTypeLabel = docType != null ? docType.label() : "";

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
                // 공문
                if (DocumentType.COMMENCE == docType || DocumentType.INTERIM == docType || DocumentType.COMPLETION == docType) {
                    addZipEntry(zos, projNm + "_" + docTypeLabel + "_공문.hwpx", hwpxExportService.generateHwpx(id, "letter"));
                }
                if (DocumentType.COMMENCE == docType) {
                    addZipEntry(zos, projNm + "_착수계.hwpx", hwpxExportService.generateHwpx(id, "commence_body"));
                    addZipEntry(zos, projNm + "_설계내역서.xlsx", excelExportService.generateDesignEstimate(id));
                } else if (DocumentType.INTERIM == docType) {
                    addZipEntry(zos, projNm + "_기성검사원.hwpx", hwpxExportService.generateHwpx(id, "inspector"));
                    addZipEntry(zos, projNm + "_기성내역서.xlsx", excelExportService.generateInterimReport(id));
                } else if (DocumentType.COMPLETION == docType) {
                    try { addZipEntry(zos, projNm + "_준공계_KRAS.hwpx", hwpxExportService.generateHwpx(id, "completion_body")); } catch (Exception ignore) {}
                    try { addZipEntry(zos, projNm + "_준공계_UPIS.hwpx", hwpxExportService.generateHwpx(id, "completion_body_upis")); } catch (Exception ignore) {}
                }
            }

            String filename = projNm + "_" + docTypeLabel + "_일괄.zip";
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/zip")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("일괄 ZIP 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    private void addZipEntry(java.util.zip.ZipOutputStream zos, String name, byte[] data) throws java.io.IOException {
        zos.putNextEntry(new java.util.zip.ZipEntry(name));
        zos.write(data);
        zos.closeEntry();
    }

    // === 검색목록 산출물 일괄 ZIP 다운로드 (doc-bulk-export) ===
    @ResponseBody
    @GetMapping("/api/bulk-zip")
    public ResponseEntity<byte[]> downloadBulkZip(
            @RequestParam(name = "docType", required = false) String docType,
            @RequestParam(name = "cityNm", required = false) String cityNm,
            @RequestParam(name = "distNm", required = false) String distNm,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "authorName", required = false) String authorName,
            @RequestParam(name = "type", defaultValue = "letter") String type) {

        if ("NONE".equals(getAuth())) return ResponseEntity.status(403).build();

        java.util.Set<String> allowed = java.util.Set.of("letter", "all", "inspector", "interim", "commence_body", "design", "completion");
        if (!allowed.contains(type)) return bulkErr(400, "허용되지 않은 type 입니다.");

        final int LIMIT = 200;
        Page<DocumentDTO> page = documentService.searchDocuments(
                docType, null, cityNm, distNm, null, null, null, keyword, authorName,
                org.springframework.data.domain.PageRequest.of(0, LIMIT + 1));
        long total = page.getTotalElements();
        if (total == 0) return bulkErr(400, "대상 문서가 없습니다.");
        if (total > LIMIT) return bulkErr(413, "검색 결과 " + total + "건 — 최대 " + LIMIT + "건까지 가능합니다. 필터를 좁혀주세요.");

        java.util.List<String> fails = new java.util.ArrayList<>();
        java.util.Set<String> used = new java.util.HashSet<>();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            for (DocumentDTO d : page.getContent()) {
                Integer id = d.getDocId();
                DocumentType dt = d.getDocType();
                String base = zipSafe(d.getCityNm()) + "_" + zipSafe(d.getDistNm()) + "_" + zipSafe(d.getProjNm());
                if ("all".equals(type)) {
                    bulkAddAll(zos, base, used, id, dt, fails);
                } else {
                    bulkAddSingle(zos, base, used, id, dt, type, fails);
                }
            }
            if (!fails.isEmpty()) {
                addZipEntry(zos, "_실패목록.txt", ("생성 실패/건너뜀:\n" + String.join("\n", fails)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("일괄 ZIP 생성 실패", e);
            return ResponseEntity.status(500).build();
        }

        String filename = bulkTypeLabel(type) + "_일괄_" + java.time.LocalDate.now() + ".zip";
        String encoded;
        try { encoded = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20"); }
        catch (Exception e) { encoded = "bulk.zip"; }
        return ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + encoded)
                .body(baos.toByteArray());
    }

    private ResponseEntity<byte[]> bulkErr(int status, String msg) {
        return ResponseEntity.status(status)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** 단일 유형: {base}_{산출물}.{ext}. 유형 불일치 문서는 건너뜀(_실패목록 기록). */
    private void bulkAddSingle(java.util.zip.ZipOutputStream zos, String base, java.util.Set<String> used,
                               Integer id, DocumentType dt, String type, java.util.List<String> fails) {
        try {
            switch (type) {
                case "letter" -> bulkPut(zos, used, base + "_공문", ".hwpx", hwpxExportService.generateHwpx(id, "letter"));
                case "inspector" -> {
                    if (dt == DocumentType.INTERIM) bulkPut(zos, used, base + "_기성검사원", ".hwpx", hwpxExportService.generateHwpx(id, "inspector"));
                    else fails.add(base + " — 기성계 아님(기성검사원 생략)");
                }
                case "interim" -> {
                    if (dt == DocumentType.INTERIM) bulkPut(zos, used, base + "_기성내역서", ".xlsx", excelExportService.generateInterimReport(id));
                    else fails.add(base + " — 기성계 아님(기성내역서 생략)");
                }
                case "commence_body" -> {
                    if (dt == DocumentType.COMMENCE) bulkPut(zos, used, base + "_착수계본문", ".hwpx", hwpxExportService.generateHwpx(id, "commence_body"));
                    else fails.add(base + " — 착수계 아님(착수계본문 생략)");
                }
                case "design" -> {
                    if (dt == DocumentType.COMMENCE) bulkPut(zos, used, base + "_설계내역서", ".xlsx", excelExportService.generateDesignEstimate(id));
                    else fails.add(base + " — 착수계 아님(설계내역서 생략)");
                }
                case "completion" -> {
                    if (dt == DocumentType.COMPLETION) {
                        try { bulkPut(zos, used, base + "_준공계_KRAS", ".hwpx", hwpxExportService.generateHwpx(id, "completion_body")); } catch (Exception e) { fails.add(base + "_준공계_KRAS — " + e.getMessage()); }
                        try { bulkPut(zos, used, base + "_준공계_UPIS", ".hwpx", hwpxExportService.generateHwpx(id, "completion_body_upis")); } catch (Exception e) { fails.add(base + "_준공계_UPIS — " + e.getMessage()); }
                    } else fails.add(base + " — 준공계 아님(생략)");
                }
            }
        } catch (Exception e) {
            fails.add(base + " (" + type + ") — " + e.getMessage());
        }
    }

    /** 전체 일괄: 평면(폴더 없음), 파일명 {사업명}_{산출물} (단건 다운로드 명명과 동일). */
    private void bulkAddAll(java.util.zip.ZipOutputStream zos, String base, java.util.Set<String> used, Integer id, DocumentType dt, java.util.List<String> fails) {
        try { bulkPut(zos, used, base + "_공문", ".hwpx", hwpxExportService.generateHwpx(id, "letter")); } catch (Exception e) { fails.add(base + "_공문 — " + e.getMessage()); }
        if (dt == DocumentType.COMMENCE) {
            try { bulkPut(zos, used, base + "_착수계", ".hwpx", hwpxExportService.generateHwpx(id, "commence_body")); } catch (Exception e) { fails.add(base + "_착수계 — " + e.getMessage()); }
            try { bulkPut(zos, used, base + "_설계내역서", ".xlsx", excelExportService.generateDesignEstimate(id)); } catch (Exception e) { fails.add(base + "_설계내역서 — " + e.getMessage()); }
        } else if (dt == DocumentType.INTERIM) {
            try { bulkPut(zos, used, base + "_기성검사원", ".hwpx", hwpxExportService.generateHwpx(id, "inspector")); } catch (Exception e) { fails.add(base + "_기성검사원 — " + e.getMessage()); }
            try { bulkPut(zos, used, base + "_기성내역서", ".xlsx", excelExportService.generateInterimReport(id)); } catch (Exception e) { fails.add(base + "_기성내역서 — " + e.getMessage()); }
        } else if (dt == DocumentType.COMPLETION) {
            try { bulkPut(zos, used, base + "_준공계_KRAS", ".hwpx", hwpxExportService.generateHwpx(id, "completion_body")); } catch (Exception e) { fails.add(base + "_준공계_KRAS — " + e.getMessage()); }
            try { bulkPut(zos, used, base + "_준공계_UPIS", ".hwpx", hwpxExportService.generateHwpx(id, "completion_body_upis")); } catch (Exception e) { fails.add(base + "_준공계_UPIS — " + e.getMessage()); }
        }
    }

    private void bulkPut(java.util.zip.ZipOutputStream zos, java.util.Set<String> used, String nameNoExt, String ext, byte[] data) throws java.io.IOException {
        addZipEntry(zos, bulkUnique(used, nameNoExt) + ext, data);
    }

    private String bulkUnique(java.util.Set<String> used, String base) {
        String name = base; int n = 2;
        while (used.contains(name)) name = base + "_" + (n++);
        used.add(name);
        return name;
    }

    static String zipSafe(String raw) {
        if (raw == null || raw.isBlank()) return "미상";
        String s = raw.replaceAll("[\\\\/:*?\"<>|]", "")  // 경로 구분자·금지문자
                      .replaceAll("[\\x00-\\x1F]", "")       // 제어문자
                      .replaceAll("\\.{2,}", "")             // .. 연속점(traversal 흔적) 제거
                      .replaceAll("\\s+", " ").trim();
        if (s.length() > 60) s = s.substring(0, 60).trim();
        return s.isEmpty() ? "미상" : s;
    }

    static String bulkTypeLabel(String type) {
        return switch (type) {
            case "letter" -> "공문";
            case "all" -> "전체";
            case "inspector" -> "기성검사원";
            case "interim" -> "기성내역서";
            case "commence_body" -> "착수계본문";
            case "design" -> "설계내역서";
            case "completion" -> "준공계";
            default -> "산출물";
        };
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

    // === 날인본 스캔 (doc-signed-scan-upload) ===

    @Autowired private com.swmanager.system.service.DocumentSignedScanService signedScanService;

    @ResponseBody
    @PostMapping("/api/signed-scan/upload/{docId}")
    public ResponseEntity<Map<String, Object>> uploadSignedScan(
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
        if (getCurrentUser() == null) return ResponseEntity.status(403).build();
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
    public ResponseEntity<Map<String, Object>> deleteSignedScan(@PathVariable Integer docId) {
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
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long userSeq) {
        return userRepository.findById(userSeq).map(u -> {
            Map<String, Object> data = new HashMap<>();
            data.put("userSeq", u.getUserSeq());
            data.put("username", u.getUsername());
            data.put("positionTitle", u.getPositionTitle());
            data.put("position", u.getPosition());
            data.put("techGrade", u.getTechGrade());
            data.put("mobile", u.getMobile());
            data.put("tel", u.getTel());
            data.put("address", u.getAddress());
            data.put("tasks", u.getTasks());
            data.put("deptNm", u.getDeptNm());
            data.put("teamNm", u.getTeamNm());
            data.put("careerYears", u.getCareerYears());
            data.put("fieldRole", u.getFieldRole());
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 전체 정보 (민감 필드 포함) — 감사 P1-3 조치 (2026-04-18):
     * ssn / certificate / email 이 필요한 화면(예: 과업 착수 문서 작성) 전용.
     * EDIT 권한 필수 — 비-EDIT 사용자는 403.
     */
    @ResponseBody
    @GetMapping("/api/user/{userSeq}/secure")
    public ResponseEntity<Map<String, Object>> getUserInfoSecure(@PathVariable Long userSeq) {
        if (!"EDIT".equals(getAuth())) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다"));
            return ResponseEntity.status(403).body(forbidden);
        }
        return userRepository.findById(userSeq).map(u -> {
            Map<String, Object> data = new HashMap<>();
            data.put("userSeq", u.getUserSeq());
            data.put("username", u.getUsername());
            data.put("positionTitle", u.getPositionTitle());
            data.put("position", u.getPosition());
            data.put("techGrade", u.getTechGrade());
            data.put("mobile", u.getMobile());
            data.put("tel", u.getTel());
            data.put("address", u.getAddress());
            data.put("tasks", u.getTasks());
            data.put("deptNm", u.getDeptNm());
            data.put("teamNm", u.getTeamNm());
            data.put("careerYears", u.getCareerYears());
            data.put("fieldRole", u.getFieldRole());
            // 민감 필드 — EDIT 권한자만 접근
            data.put("ssn", u.getSsn());
            data.put("certificate", u.getCertificate());
            data.put("email", u.getEmail());
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<List<Map<String, Object>>> getAllSystemsForYear(@RequestParam Integer year) {
        var projects = swProjectRepository.findAll().stream()
                .filter(p -> year.equals(p.getYear()))
                .filter(p -> p.getSysNmEn() != null && !p.getSysNmEn().isEmpty())
                .toList();
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        projects.forEach(p -> map.putIfAbsent(p.getSysNmEn(), p.getSysNm()));
        List<Map<String, Object>> result = map.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("sysNmEn", e.getKey());
            m.put("sysNm", e.getValue());
            return m;
        }).sorted((a, b) -> String.valueOf(a.get("sysNmEn")).compareTo(String.valueOf(b.get("sysNmEn")))).toList();
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/batch/targets")
    public ResponseEntity<List<Map<String, Object>>> getBatchTargets(
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

        List<Map<String, Object>> result = projects.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("projId", p.getProjId());
            m.put("projNm", p.getProjNm());
            m.put("sysNmEn", p.getSysNmEn());
            m.put("cityNm", p.getCityNm());
            m.put("distNm", p.getDistNm());
            m.put("orgNm", p.getOrgNm());
            m.put("contAmt", p.getContAmt());
            m.put("contDt", p.getContDt() != null ? p.getContDt().toString() : null);
            m.put("startDt", p.getStartDt() != null ? p.getStartDt().toString() : null);
            m.put("endDt", p.getEndDt() != null ? p.getEndDt().toString() : null);
            m.put("client", p.getClient());
            return m;
        }).toList();
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

    /** 시스템별 공정명 목록 조회 */
    @GetMapping("/api/process-master")
    @ResponseBody
    public List<Map<String, Object>> getProcessMasterList(@RequestParam String sysNmEn) {
        List<ProcessMaster> list = processMasterRepository.findBySysNmEnAndUseYnOrderBySortOrder(sysNmEn, "Y");
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessMaster pm : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("processId", pm.getProcessId());
            m.put("processName", pm.getProcessName());
            result.add(m);
        }
        return result;
    }

    /** 시스템별 용역목적/과업내용 조회 */
    @GetMapping("/api/service-purpose")
    @ResponseBody
    public List<Map<String, Object>> getServicePurposeList(
            @RequestParam String sysNmEn,
            @RequestParam(required = false) String purposeType) {
        List<ServicePurpose> list;
        if (purposeType != null && !purposeType.isEmpty()) {
            list = servicePurposeRepository.findBySysNmEnAndPurposeTypeAndUseYnOrderBySortOrder(sysNmEn, purposeType, "Y");
        } else {
            list = servicePurposeRepository.findBySysNmEnAndUseYnOrderBySortOrder(sysNmEn, "Y");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServicePurpose sp : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("purposeId", sp.getPurposeId());
            m.put("purposeType", sp.getPurposeType());
            m.put("purposeText", sp.getPurposeText());
            result.add(m);
        }
        return result;
    }

    /**
     * 사업별 과업참여자 조회 (비민감) — 감사 P1-3 조치 (2026-04-18):
     * ssn/certificate 제거. 민감 정보가 필요하면 `/secure` 엔드포인트 사용.
     */
    @GetMapping("/api/contract-participants/{projId}")
    @ResponseBody
    public List<Map<String, Object>> getContractParticipants(@PathVariable Long projId) {
        List<ContractParticipant> list = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContractParticipant cp : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("participantId", cp.getParticipantId());
            m.put("userId", cp.getUser() != null ? cp.getUser().getUserSeq() : null);
            m.put("userName", cp.getUser() != null ? cp.getUser().getUsername() : "");
            m.put("position", cp.getUser() != null ? cp.getUser().getPositionTitle() : "");
            m.put("roleType", cp.getRoleType());
            m.put("techGrade", cp.getTechGrade());
            m.put("taskDesc", cp.getTaskDesc());
            m.put("isSiteRep", cp.getIsSiteRep());
            m.put("tasks", cp.getUser() != null ? cp.getUser().getTasks() : "");
            result.add(m);
        }
        return result;
    }

    /**
     * 사업별 과업참여자 조회 (민감 필드 포함) — 감사 P1-3 조치 (2026-04-18):
     * EDIT 권한 필수.
     */
    @GetMapping("/api/contract-participants/{projId}/secure")
    @ResponseBody
    public ResponseEntity<?> getContractParticipantsSecure(@PathVariable Long projId) {
        if (!"EDIT".equals(getAuth())) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다"));
            return ResponseEntity.status(403).body(forbidden);
        }
        List<ContractParticipant> list = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContractParticipant cp : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("participantId", cp.getParticipantId());
            m.put("userId", cp.getUser() != null ? cp.getUser().getUserSeq() : null);
            m.put("userName", cp.getUser() != null ? cp.getUser().getUsername() : "");
            m.put("position", cp.getUser() != null ? cp.getUser().getPositionTitle() : "");
            m.put("roleType", cp.getRoleType());
            m.put("techGrade", cp.getTechGrade());
            m.put("taskDesc", cp.getTaskDesc());
            m.put("isSiteRep", cp.getIsSiteRep());
            m.put("tasks", cp.getUser() != null ? cp.getUser().getTasks() : "");
            // 민감 필드
            m.put("ssn", cp.getUser() != null ? cp.getUser().getSsn() : "");
            m.put("certificate", cp.getUser() != null ? cp.getUser().getCertificate() : "");
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /** 사업별 과업참여자 저장 — 감사 P1-2 조치: EDIT 권한 체크 + HTTP 403 반환 (2026-04-19) */
    @PostMapping("/api/contract-participants/{projId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveContractParticipants(
            @PathVariable Long projId,
            @RequestBody List<Map<String, Object>> participantList) {
        if (!"EDIT".equals(getAuth())) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("success", false);
            forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "수정 권한이 없습니다"));
            return ResponseEntity.status(403).body(forbidden);
        }
        Map<String, Object> result = new HashMap<>();
        try {
            var project = swProjectRepository.findById(projId).orElse(null);
            if (project == null) {
                result.put("success", false);
                result.put("error", "사업을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(result);
            }

            // 기존 참여자 삭제 후 재등록
            List<ContractParticipant> existing = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
            contractParticipantRepository.deleteAll(existing);

            int order = 0;
            for (Map<String, Object> item : participantList) {
                ContractParticipant cp = new ContractParticipant();
                cp.setProject(project);

                Object userIdObj = item.get("userId");
                if (userIdObj != null) {
                    Long userId = Long.parseLong(userIdObj.toString());
                    userRepository.findById(userId).ifPresent(cp::setUser);
                }

                cp.setRoleType((String) item.getOrDefault("roleType", "PARTICIPANT"));
                cp.setTechGrade((String) item.getOrDefault("techGrade", ""));
                cp.setTaskDesc((String) item.getOrDefault("taskDesc", ""));
                cp.setIsSiteRep(Boolean.TRUE.equals(item.get("isSiteRep")));
                cp.setSortOrder(order++);

                contractParticipantRepository.save(cp);
            }

            result.put("success", true);
            result.put("count", participantList.size());
        } catch (Exception e) {
            log.error("과업참여자 저장 실패: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    // ========================================================
    // 사업수행계획서 (P10~P13) API
    // ========================================================

    /** 사업수행계획서 데이터 조회 */
    @ResponseBody
    @GetMapping("/api/plan/{projId}")
    public ResponseEntity<Map<String, Object>> getPlanData(@PathVariable Long projId) {
        Map<String, Object> result = new HashMap<>();
        var pOpt = swProjectRepository.findById(projId);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        var p = pOpt.get();
        result.put("projPurpose", p.getProjPurpose());
        result.put("supportType", p.getSupportType());
        result.put("scopeText", p.getScopeText());
        result.put("inspectMethod", p.getInspectMethod());

        // targets
        List<Map<String, Object>> targets = new ArrayList<>();
        for (var t : pjtTargetRepository.findByProjIdOrderBySortOrderAsc(projId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("productName", t.getProductName());
            m.put("qty", t.getQty());
            targets.add(m);
        }
        result.put("targets", targets);

        // manpower plans
        List<Map<String, Object>> mps = new ArrayList<>();
        for (var mp : pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(projId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", mp.getId());
            m.put("stepName", mp.getStepName());
            m.put("startDt", mp.getStartDt() != null ? mp.getStartDt().toString() : null);
            m.put("endDt", mp.getEndDt() != null ? mp.getEndDt().toString() : null);
            m.put("gradeSpecial", mp.getGradeSpecial());
            m.put("gradeHigh", mp.getGradeHigh());
            m.put("gradeMid", mp.getGradeMid());
            m.put("gradeLow", mp.getGradeLow());
            m.put("funcHigh", mp.getFuncHigh());
            m.put("funcMid", mp.getFuncMid());
            m.put("funcLow", mp.getFuncLow());
            m.put("remark", mp.getRemark());
            mps.add(m);
        }
        result.put("manpowerPlans", mps);

        // schedules
        List<Map<String, Object>> schs = new ArrayList<>();
        for (var s : pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(projId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("processName", s.getProcessName());
            m.put("m01", s.getM01()); m.put("m02", s.getM02()); m.put("m03", s.getM03());
            m.put("m04", s.getM04()); m.put("m05", s.getM05()); m.put("m06", s.getM06());
            m.put("m07", s.getM07()); m.put("m08", s.getM08()); m.put("m09", s.getM09());
            m.put("m10", s.getM10()); m.put("m11", s.getM11()); m.put("m12", s.getM12());
            m.put("remark", s.getRemark());
            schs.add(m);
        }
        result.put("schedules", schs);

        return ResponseEntity.ok(result);
    }

    /** 사업수행계획서 데이터 저장 (overwrite) — 감사 P1-2 조치: EDIT 권한 체크 (2026-04-18) */
    @ResponseBody
    @PostMapping("/api/plan/{projId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, Object>> savePlanData(
            @PathVariable Long projId,
            @RequestBody Map<String, Object> body) {
        if (!"EDIT".equals(getAuth())) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("success", false);
            forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "수정 권한이 없습니다"));
            return ResponseEntity.status(403).body(forbidden);
        }
        Map<String, Object> result = new HashMap<>();
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

            result.put("success", true);
        } catch (Exception e) {
            log.error("사업수행계획서 저장 실패: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
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
