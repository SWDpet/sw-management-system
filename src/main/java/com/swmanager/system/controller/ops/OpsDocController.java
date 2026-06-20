package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.domain.ops.OpsDocumentDetail;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocPartner;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import com.swmanager.system.domain.ops.OpsKbFeedback;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocPartnerRepository;
import com.swmanager.system.repository.ops.OpsKbFeedbackRepository;
import com.swmanager.system.repository.ops.PartnerContactRepository;
import com.swmanager.system.repository.ops.PartnerRepository;
import com.swmanager.system.repository.ops.StaffRepository;
import org.springframework.data.domain.PageRequest;
import com.swmanager.system.service.ops.OpsDocAttachmentService;
import com.swmanager.system.service.inspection.InspectMaintProfile;
import com.swmanager.system.service.ops.KbMatcher;
import com.swmanager.system.service.ops.OpsDocService;
import com.swmanager.system.service.ops.OpsDocSignatureService;
import com.swmanager.system.service.ops.OpsSupportFileService;
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
    private final OpsSupportFileService supportFileService;   // [ops-support-doc-upload]
    private final OpsDocAttachmentService attachmentService;
    private final OpsDocSignatureService signatureService;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final InspectReportRepository inspectReportRepository;
    private final SwProjectRepository swProjectRepository;
    private final UserRepository userRepository;             // [M2]
    private final PersonInfoRepository personInfoRepository; // [M2]
    private final OrgUnitRepository orgUnitRepository;        // [M2]
    private final PartnerContactRepository partnerContactRepository; // [M2/P3]
    private final KbMatcher kbMatcher;                                // [M3]
    private final OpsKbFeedbackRepository opsKbFeedbackRepository;     // [M3/P5]
    private final PartnerRepository partnerRepository;                 // [M2/FR-M2-4]
    private final OpsDocPartnerRepository opsDocPartnerRepository;     // [M2/FR-M2-4]
    private final SysMstRepository sysMstRepository;                   // [region-cascade] 시스템 마스터
    private final StaffRepository staffRepository;                     // [staff] 직원 디렉터리

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
        // 점검내역서(INSPECT) 한정 유지보수유형 배지 (연계 사업 maint_type)
        Map<Long, String> maintLabelByDoc = new HashMap<>();
        Map<Long, String> maintToneByDoc = new HashMap<>();
        for (OpsDocument d : all) {
            String[] r = resolveRegion(d, byCode, bySgg);
            sidoByDoc.put(d.getDocId(), r[0]);
            sggByDoc.put(d.getDocId(), r[1]);
            String mt = resolveMaintType(d);
            if (mt != null) {
                maintLabelByDoc.put(d.getDocId(), InspectMaintProfile.badgeLabel(mt));
                maintToneByDoc.put(d.getDocId(), InspectMaintProfile.badgeTone(mt));
            }
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
        model.addAttribute("maintLabelByDoc", maintLabelByDoc);
        model.addAttribute("maintToneByDoc", maintToneByDoc);
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

    /** 점검내역서(INSPECT) 의 연계 사업 maint_type (없으면 null). 유형 배지용. */
    private String resolveMaintType(OpsDocument d) {
        if (d.getDocType() != OpsDocType.INSPECT) return null;
        Long reportId = parseReportId(d.getDocNo());
        if (reportId == null) return null;
        var report = inspectReportRepository.findById(reportId).orElse(null);
        if (report == null || report.getPjtId() == null) return null;
        var pj = swProjectRepository.findById(report.getPjtId()).orElse(null);
        return pj != null ? pj.getMaintType() : null;
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
        model.addAttribute("mode", "new");
        model.addAttribute("activeMenu", "ops");
        model.addAttribute("sidoList", sigunguCodeRepository.findDistinctSidoNm());  // [region-cascade]
        return docType.templateName();
    }

    /** doc 의 main 섹션 section_data(jsonb 맵) 추출 — 상세/수정 프리필용. */
    private Map<String, Object> mainSectionData(OpsDocument doc) {
        if (doc == null || doc.getDetails() == null) return java.util.Map.of();
        return doc.getDetails().stream()
                .filter(d -> "main".equals(d.getSectionKey()))
                .findFirst()
                .map(OpsDocumentDetail::getSectionData)
                .orElse(java.util.Map.of());
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
        model.addAttribute("mode", "edit");
        model.addAttribute("sectionData", mainSectionData(doc));
        model.addAttribute("activeMenu", "ops");
        model.addAttribute("sidoList", sigunguCodeRepository.findDistinctSidoNm());  // [region-cascade]
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
        model.addAttribute("isEdit", true);   // 상세=기존 문서 → 템플릿 ${!isEdit}/${isEdit} 분기 null 방지(백지 해소)
        model.addAttribute("mode", "view");    // 읽기전용 상세 + 수정/삭제 버튼
        model.addAttribute("sectionData", mainSectionData(doc));
        model.addAttribute("activeMenu", "ops");
        model.addAttribute("sidoList", sigunguCodeRepository.findDistinctSidoNm());  // [region-cascade]
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
    public ResponseEntity<?> create(
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
        ResponseEntity<ApiResult> denied = requireDocEdit(currentUser);  // [M2 codex#5]
        if (denied != null) return denied;

        OpsDocument doc = new OpsDocument();
        doc.setDocType(docType);
        doc.setTitle((String) body.getOrDefault("title", docType.label()));
        doc.setSysType((String) body.get("sys_type"));
        doc.setRegionCode((String) body.get("region_code"));
        doc.setEnvironment((String) body.get("environment"));
        doc.setSupportTargetType((String) body.get("support_target_type"));
        doc.setStatus(DocumentStatus.COMPLETED);   // 저장=작성완료 (사용자 결정 2026-06-17)
        applyRelations(doc, body);   // [M2] 엔지니어·요청자

        @SuppressWarnings("unchecked")
        Map<String, Object> sectionData = (Map<String, Object>) body.get("section_data");

        String userId = currentUser != null ? currentUser.getUsername() : null;
        OpsDocument saved = opsDocService.create(doc, sectionData, userId);
        savePartners(saved.getDocId(), body, false);   // [FR-M2-4] 협력업체

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("docId", saved.getDocId());
        result.put("docNo", saved.getDocNo());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/api/{type}/{docId}")
    @ResponseBody
    public ResponseEntity<?> update(
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
        ResponseEntity<ApiResult> denied = requireDocEdit(currentUser);  // [M2 codex#5]
        if (denied != null) return denied;

        OpsDocument changes = new OpsDocument();
        changes.setDocType(docType);
        changes.setTitle((String) body.get("title"));
        changes.setSysType((String) body.get("sys_type"));
        changes.setRegionCode((String) body.get("region_code"));
        changes.setEnvironment((String) body.get("environment"));
        changes.setSupportTargetType((String) body.get("support_target_type"));
        changes.setStatus(DocumentStatus.COMPLETED);   // 수정 저장도 작성완료 유지(null→DRAFT 복귀 방지)
        applyRelations(changes, body);   // [M2] 엔지니어·요청자

        @SuppressWarnings("unchecked")
        Map<String, Object> sectionData = (Map<String, Object>) body.get("section_data");

        String userId = currentUser != null ? currentUser.getUsername() : null;
        OpsDocument updated = opsDocService.update(docId, changes, sectionData, userId);
        savePartners(docId, body, true);   // [FR-M2-4] 협력업체 교체

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("docId", updated.getDocId());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/api/{docId}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long docId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEditOrAdmin(currentUser);  // [S1 보안가드]
        if (denied != null) return denied;
        opsDocService.delete(docId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    // ===== [M2] 관계자 (엔지니어 / 요청자) =====

    /** authDocument=EDIT 가드 (codex #5). 허용이면 null, 아니면 403. */
    private ResponseEntity<ApiResult> requireDocEdit(CustomUserDetails u) {
        String auth = (u != null && u.getUser() != null) ? u.getUser().getAuthDocument() : null;
        // ROLE_ADMIN 우회 — 다른 모든 편집 가드(KB/사업 canEdit, requireDocEditOrAdmin)와 일관.
        boolean admin = u != null && u.getUser() != null && "ROLE_ADMIN".equals(u.getUser().getUserRole());
        if (!admin && !"EDIT".equals(auth)) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "문서 편집 권한(authDocument=EDIT 또는 관리자)이 필요합니다."));
        }
        return null;
    }

    /** authDocument!=NONE(또는 ADMIN) 조회 가드. 허용이면 null, 아니면 403. (ops-support-doc-upload FR-8) */
    private ResponseEntity<ApiResult> requireDocView(CustomUserDetails u) {
        String auth = (u != null && u.getUser() != null) ? u.getUser().getAuthDocument() : null;
        boolean admin = u != null && u.getUser() != null && "ROLE_ADMIN".equals(u.getUser().getUserRole());
        if (!admin && (auth == null || "NONE".equals(auth))) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "문서 조회 권한이 필요합니다."));
        }
        return null;
    }

    /** 업로드/삭제 가드 — authDocument=EDIT 또는 ROLE_ADMIN (FR-8). requireDocEdit(EDIT-only)와 분리. */
    private ResponseEntity<ApiResult> requireDocEditOrAdmin(CustomUserDetails u) {
        String auth = (u != null && u.getUser() != null) ? u.getUser().getAuthDocument() : null;
        boolean admin = u != null && u.getUser() != null && "ROLE_ADMIN".equals(u.getUser().getUserRole());
        if (admin || "EDIT".equals(auth)) return null;
        return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "문서 편집 권한(authDocument=EDIT)이 필요합니다."));
    }

    // ===== [ops-support-doc-upload] 업무지원 지원문서 업로드/다운로드/삭제 =====

    @ResponseBody
    @PostMapping("/api/support-file/upload/{docId}")
    public ResponseEntity<?> uploadSupportFile(
            @PathVariable Long docId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEditOrAdmin(currentUser);
        if (denied != null) return denied;
        try {
            Long uploaderSeq = (currentUser != null && currentUser.getUser() != null)
                    ? currentUser.getUser().getUserSeq() : null;
            OpsDocument doc = supportFileService.uploadOrReplace(docId, file, uploaderSeq);
            log.info("[ops-support-file] 업로드 docId={} name={}", docId, doc.getSupportFileOrigName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileName", doc.getSupportFileOrigName(),
                    "fileSize", doc.getSupportFileSize() != null ? doc.getSupportFileSize() : 0
            ));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.status(400).body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", e.getMessage())));
        } catch (Exception e) {
            log.warn("[ops-support-file] 업로드 실패 docId={}", docId, e);
            return ResponseEntity.status(500).body(Map.of("success", false,
                    "error", Map.of("code", "SERVER_ERROR", "message", "업로드 중 오류가 발생했습니다.")));
        }
    }

    @GetMapping("/api/support-file/{docId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadSupportFile(
            @PathVariable Long docId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (requireDocView(currentUser) != null) return ResponseEntity.status(403).build();
        try {
            org.springframework.core.io.Resource resource = supportFileService.loadForDownload(docId);
            String origName = supportFileService.originalName(docId);
            String encoded = java.net.URLEncoder.encode(origName, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            log.info("[ops-support-file] 다운로드 docId={}", docId);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @ResponseBody
    @PostMapping("/api/support-file/delete/{docId}")
    public ResponseEntity<?> deleteSupportFile(
            @PathVariable Long docId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEditOrAdmin(currentUser);
        if (denied != null) return denied;
        try {
            supportFileService.delete(docId);
            log.info("[ops-support-file] 삭제 docId={}", docId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.status(400).body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", e.getMessage())));
        } catch (Exception e) {
            log.warn("[ops-support-file] 삭제 실패 docId={}", docId, e);
            return ResponseEntity.status(500).body(Map.of("success", false,
                    "error", Map.of("code", "SERVER_ERROR", "message", "삭제 중 오류가 발생했습니다.")));
        }
    }

    /** body 의 engineer_id / requester_kind(PERSON|CONTACT) + requester_id 를 doc 에 반영. */
    private void applyRelations(OpsDocument doc, Map<String, Object> body) {
        Object engId = body.get("engineer_id");
        if (engId instanceof Number n) {
            userRepository.findById(n.longValue()).ifPresent(doc::setEngineer);
        }
        String kind = (String) body.get("requester_kind");
        Object rid = body.get("requester_id");
        doc.setRequesterPerson(null);
        doc.setRequesterContactId(null);
        doc.setRequesterStaffId(null);
        if (rid instanceof Number n) {
            if ("PERSON".equals(kind)) {
                personInfoRepository.findById(n.longValue()).ifPresent(doc::setRequesterPerson);
            } else if ("CONTACT".equals(kind)) {
                doc.setRequesterContactId(n.longValue());   // 업체담당자(tb_partner_contact)
            } else if ("STAFF".equals(kind)) {
                doc.setRequesterStaffId(n.longValue());     // 직원(tb_staff)
            }
        }
    }

    /** 담당 엔지니어 풀 — SW지원팀 활성 사용자 (FR-M2-1 드롭다운). */
    @GetMapping("/api/engineers")
    @ResponseBody
    public List<Map<String, Object>> engineers() {
        List<Map<String, Object>> result = new ArrayList<>();
        Long swTeamId = orgUnitRepository.findFirstByNameAndUnitType("SW지원팀", "TEAM")
                .map(OrgUnit::getUnitId).orElse(null);
        if (swTeamId == null) return result;
        for (User u : userRepository.findByOrgUnitIdAndEnabledTrueOrderByUsernameAsc(swTeamId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getUserSeq());
            m.put("name", u.getUsername());
            m.put("position", u.getPosition() != null ? u.getPosition() : u.getPositionTitle());
            result.add(m);
        }
        return result;
    }

    /** 요청자(공무원) 검색 — ps_info (FR-M2-2). */
    @GetMapping("/api/requester/search")
    @ResponseBody
    public List<Map<String, Object>> requesterSearch(@RequestParam("kw") String kw) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PersonInfo p : personInfoRepository.findAllByKeyword(kw == null ? "" : kw, PageRequest.of(0, 10))) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getUserNm());
            m.put("org", p.getOrgNm());
            m.put("dept", p.getDeptNm());
            m.put("pos", p.getPos());
            m.put("tel", p.getTel());
            result.add(m);
        }
        return result;
    }

    /** 요청자(공무원) 인라인 신규 등록 — ops-doc 전용(authDocument), ps_info 최소필드 (FR-M2-5). */
    @PostMapping("/api/requester")
    @ResponseBody
    public ResponseEntity<?> requesterCreate(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEdit(currentUser);
        if (denied != null) return denied;
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "이름은 필수입니다.")));
        }
        PersonInfo p = new PersonInfo();
        p.setUserNm(name);
        p.setOrgNm((String) body.get("org"));
        p.setDeptNm((String) body.get("dept"));
        p.setPos((String) body.get("pos"));
        p.setTel((String) body.get("tel"));
        p.setCityNm((String) body.get("city"));
        PersonInfo saved = personInfoRepository.save(p);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", saved.getId());
        result.put("name", saved.getUserNm());
        return ResponseEntity.ok(result);
    }

    /** 요청자(업체담당자) 검색 — tb_partner_contact (FR-M2-4, P3). */
    @GetMapping("/api/partner-contact/search")
    @ResponseBody
    public List<Map<String, Object>> partnerContactSearch(@RequestParam("kw") String kw) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PartnerContact c : partnerContactRepository.searchActive(kw == null ? "" : kw)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getContactId());
            m.put("name", c.getName());
            m.put("org", c.getPartner() != null ? c.getPartner().getName() : null);
            m.put("pos", c.getPosition());
            m.put("tel", c.getTel());
            result.add(m);
        }
        return result;
    }

    /** [M3] 증상→원인→조치 KB 추천 (규칙 매처). */
    @GetMapping("/api/kb/recommend")
    @ResponseBody
    public List<Map<String, Object>> kbRecommend(
            @RequestParam("docType") String docType,
            @RequestParam(value = "sysType", required = false) String sysType,
            @RequestParam(value = "symptom", required = false) String symptom) {
        String gubun = "FAULT".equalsIgnoreCase(docType) ? "장애"
                : ("SUPPORT".equalsIgnoreCase(docType) ? "지원" : null);
        return kbMatcher.recommend(gubun, sysType, symptom == null ? "" : symptom, 5);
    }

    /** [M3/P5] 추천 채택 피드백 적재 (APPLIED/IGNORED). */
    @PostMapping("/api/kb/feedback")
    @ResponseBody
    public ResponseEntity<?> kbFeedback(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEdit(currentUser);
        if (denied != null) return denied;
        Object kbId = body.get("kb_id");
        if (!(kbId instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "kb_id 가 필요합니다.")));
        }
        OpsKbFeedback fb = new OpsKbFeedback();
        fb.setKbId(((Number) kbId).longValue());
        if (body.get("doc_id") instanceof Number n) fb.setDocId(n.longValue());
        fb.setFbAction("IGNORED".equals(body.get("fb_action")) ? "IGNORED" : "APPLIED");
        opsKbFeedbackRepository.save(fb);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** [FR-M2-4] 협력업체(업체 단위) 검색. */
    @GetMapping("/api/partner/search")
    @ResponseBody
    public List<Map<String, Object>> partnerSearch(@RequestParam("kw") String kw) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Partner p : partnerRepository.findByUseYnAndNameContainingOrderByNameAsc("Y", kw == null ? "" : kw)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getPartnerId());
            m.put("name", p.getName());
            m.put("type", p.getPartnerType());
            out.add(m);
        }
        return out;
    }

    /** [FR-M2-4] 문서 협력업체 목록 (수정 폼 프리필). */
    @GetMapping("/api/{docId}/partners")
    @ResponseBody
    public List<Map<String, Object>> docPartners(@PathVariable Long docId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (OpsDocPartner dp : opsDocPartnerRepository.findByDocId(docId)) {
            Partner p = partnerRepository.findById(dp.getPartnerId()).orElse(null);
            Map<String, Object> m = new HashMap<>();
            m.put("partner_id", dp.getPartnerId());
            m.put("name", p != null ? p.getName() : ("#" + dp.getPartnerId()));
            m.put("role_label", dp.getRoleLabel());
            out.add(m);
        }
        return out;
    }

    /** body.partners[{partner_id, role_label}] → tb_ops_doc_partner 저장. */
    private void savePartners(Long docId, Map<String, Object> body, boolean replace) {
        if (replace) opsDocPartnerRepository.deleteByDocId(docId);
        Object pj = body.get("partners");
        if (pj instanceof List<?> list) {
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> pm && pm.get("partner_id") instanceof Number n) {
                    String role = pm.get("role_label") != null ? pm.get("role_label").toString() : "";
                    if (!seen.add(n.longValue() + "|" + role)) continue;  // (업체,역할) 중복 방지
                    OpsDocPartner dp = new OpsDocPartner();
                    dp.setDocId(docId);
                    dp.setPartnerId(n.longValue());
                    dp.setRoleLabel(role);
                    opsDocPartnerRepository.save(dp);
                }
            }
        }
    }

    /** 요청자(직원) 검색 — tb_staff 재직자. */
    @GetMapping("/api/staff/search")
    @ResponseBody
    public List<Map<String, Object>> staffSearch(@RequestParam("kw") String kw) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Staff s : staffRepository.searchActive(kw == null ? "" : kw)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getStaffId());
            m.put("name", s.getName());
            String unit = null;
            if (s.getOrgUnitId() != null) {
                OrgUnit ou = orgUnitRepository.findById(s.getOrgUnitId()).orElse(null);
                if (ou != null) unit = ou.getName();
            }
            m.put("org", unit);
            m.put("pos", s.getPosition());
            out.add(m);
        }
        return out;
    }

    // ===== [ops-doc-region-cascade] 시도→시군구→시스템 =====

    /** 시도 → 시군구 목록 (self-행=본청/도청 포함). */
    @GetMapping("/api/sgg")
    @ResponseBody
    public List<Map<String, Object>> cascadeSgg(@RequestParam("sido") String sido) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SigunguCode s : sigunguCodeRepository.findBySidoNmOrderBySggNm(sido)) {
            Map<String, Object> m = new HashMap<>();
            m.put("admSectC", s.getAdmSectC());
            m.put("sggNm", s.getSggNm());
            m.put("isUnit", s.getSggNm() != null && s.getSggNm().equals(s.getSidoNm()));
            out.add(m);
        }
        return out;
    }

    /** 시스템 마스터(sys_mst) 전체 — 계약사업(sw_pjt) 무관. 영업지원 등 미계약도 선택 가능. */
    @GetMapping("/api/systems")
    @ResponseBody
    public List<Map<String, Object>> cascadeSystems() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysMst s : sysMstRepository.findAll(org.springframework.data.domain.Sort.by("nm"))) {
            Map<String, Object> m = new HashMap<>();
            m.put("cd", s.getCd());
            m.put("nm", s.getNm());
            out.add(m);
        }
        return out;
    }

    /** 행정구역코드 → 시도/시군구 (수정·상세 프리필). */
    @GetMapping("/api/region")
    @ResponseBody
    public Map<String, Object> cascadeRegion(@RequestParam("admSectC") String admSectC) {
        Map<String, Object> m = new HashMap<>();
        SigunguCode s = sigunguCodeRepository.findById(admSectC).orElse(null);
        if (s != null) {
            m.put("admSectC", s.getAdmSectC());
            m.put("sido", s.getSidoNm());
            m.put("sgg", s.getSggNm());
        }
        return m;
    }

    /** [M2] 수정 폼 관계자 프리필 (engineer/requester). */
    @GetMapping("/api/{docId}/relations")
    @ResponseBody
    public Map<String, Object> relations(@PathVariable Long docId) {
        Map<String, Object> m = new HashMap<>();
        OpsDocument d = opsDocService.findById(docId).orElse(null);
        if (d == null) return m;
        if (d.getEngineer() != null) m.put("engineer_id", d.getEngineer().getUserSeq());
        if (d.getRequesterPerson() != null) {
            m.put("requester_kind", "PERSON");
            m.put("requester_id", d.getRequesterPerson().getId());
            m.put("requester_label", nz(d.getRequesterPerson().getUserNm()) + " / " + nz(d.getRequesterPerson().getOrgNm()));
        } else if (d.getRequesterContactId() != null) {
            m.put("requester_kind", "CONTACT");
            m.put("requester_id", d.getRequesterContactId());
            PartnerContact c = partnerContactRepository.findById(d.getRequesterContactId()).orElse(null);
            m.put("requester_label", c != null
                    ? (nz(c.getName()) + (c.getPartner() != null ? " / " + nz(c.getPartner().getName()) : ""))
                    : ("업체담당자 #" + d.getRequesterContactId()));
        } else if (d.getRequesterStaffId() != null) {
            m.put("requester_kind", "STAFF");
            m.put("requester_id", d.getRequesterStaffId());
            Staff st = staffRepository.findById(d.getRequesterStaffId()).orElse(null);
            m.put("requester_label", st != null
                    ? (nz(st.getName()) + (st.getPosition() != null ? " " + st.getPosition() : ""))
                    : ("직원 #" + d.getRequesterStaffId()));
        }
        return m;
    }

    // ===== API: 첨부 =====

    @GetMapping("/api/attachments/{docId}")
    @ResponseBody
    public ResponseEntity<?> getAttachments(@PathVariable Long docId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocView(currentUser);  // [S1 보안가드]
        if (denied != null) return denied;
        return ResponseEntity.ok(attachmentService.getAttachments(docId));
    }

    @PostMapping("/api/attachment/upload/{docId}")
    @ResponseBody
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long docId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws java.io.IOException {
        ResponseEntity<ApiResult> denied = requireDocEditOrAdmin(currentUser);  // [S1 보안가드]
        if (denied != null) return denied;
        OpsDocumentAttachment att = attachmentService.saveAttachment(docId, file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("attachId", att.getAttachId());
        result.put("fileName", att.getFileName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/api/attachment/{attachId}")
    @ResponseBody
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResponseEntity<ApiResult> denied = requireDocEditOrAdmin(currentUser);  // [S1 보안가드]
        if (denied != null) return denied;
        attachmentService.deleteAttachment(attachId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
