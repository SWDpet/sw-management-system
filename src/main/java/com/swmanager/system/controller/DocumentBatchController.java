package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.workplan.BatchTargetRow;
import com.swmanager.system.dto.workplan.SystemAllRow;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.util.ExceptionMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 착수계/기성/준공 문서 일괄 작성 컨트롤러 — DocumentController 에서 분리 (S4 Phase 2).
 *
 * <p>기획서/개발계획 docs/{product-specs,exec-plans}/refactor-document-controller-split-phase2.md.
 * 연도별 대상 사업 조회 → 문서 일괄 생성(공문 letter + 본문 섹션). 권한은 DocumentAccessSupport
 * (admin→EDIT) 기준 EDIT 가드. 본문은 이동 전과 동일.
 */
@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentBatchController {

    @Autowired private DocumentService documentService;
    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;
    @Autowired private DocumentAccessSupport access;

    // ========== 일괄 작성 기능 ==========

    /** 일괄 작성 페이지 */
    @GetMapping("/batch")
    public String batchPage(Model model) {
        if (!"EDIT".equals(access.getAuth())) {
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

        List<SwProject> projects;
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
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            CustomUserDetails cu = access.getCurrentUser();
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
            if (projIds == null || projIds.isEmpty()) {  // [harden-nullsafe] 잘못된 요청 → 400(500 아님)
                return ResponseEntity.badRequest().body(Map.of("error", "대상 사업이 없습니다."));
            }
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
                    // [harden-nullsafe] projNm/cityNm/distNm 가 null 이어도 NPE 없이 성공 카운트(Map.of→HashMap).
                    Map<String, Object> ok = new HashMap<>();
                    ok.put("projId", projId);
                    ok.put("success", true);
                    ok.put("docId", doc.getDocId());
                    ok.put("projNm", p.getProjNm());
                    ok.put("cityNm", p.getCityNm());
                    ok.put("distNm", p.getDistNm());
                    results.add(ok);

                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> fail = new HashMap<>();  // [harden-nullsafe] 성공 branch 와 일관 + 방어
                    fail.put("projId", projId);
                    fail.put("success", false);
                    fail.put("error", ExceptionMessages.safe(e));
                    results.add(fail);
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
            return ResponseEntity.status(500).body(Map.of("error", ExceptionMessages.safe(e)));
        }
    }

    /** 일괄 생성용 공문(letter) 데이터 빌드 */
    private Map<String, Object> buildBatchLetterData(SwProject p,
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
}
