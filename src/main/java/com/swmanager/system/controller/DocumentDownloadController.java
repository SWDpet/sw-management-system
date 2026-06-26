package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.ExcelExportService;
import com.swmanager.system.service.HwpxExportService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.PdfExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 문서 다운로드/내보내기 컨트롤러 — DocumentController 에서 분리 (S4 Phase 1).
 *
 * <p>기획서 docs/product-specs/refactor-document-controller-split.md / 개발계획 동명 exec-plan.
 * 목록 Excel·단건 PDF/HWPX/Excel/ZIP·검색결과 일괄 ZIP. 권한은 DocumentAccessSupport.getAuth()
 * (admin→EDIT) 기준 다운로드=EDIT 가드(viewer-action-button-guard). 본문은 이동 전과 동일.
 */
@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentDownloadController {

    @Autowired private DocumentService documentService;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private HwpxExportService hwpxExportService;
    @Autowired private ExcelExportService excelExportService;
    @Autowired private LogService logService;
    @Autowired private DocumentAccessSupport access;

    /** D-01 검색 결과 Excel 다운로드 — 화면 필터(문서유형/상태/시도/시군구/검색) 그대로 적용한 전체 목록 */
    @ResponseBody
    @GetMapping("/excel-list")
    public ResponseEntity<byte[]> downloadDocumentListExcel(
            @RequestParam(name = "docType", required = false) String docType,
            @RequestParam(name = "cityNm", required = false) String cityNm,
            @RequestParam(name = "distNm", required = false) String distNm,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "authorName", required = false) String authorName) {
        if (!"EDIT".equals(access.getAuth())) {  // [viewer-action-button-guard] 다운로드=EDIT
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

    @ResponseBody
    @GetMapping("/api/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer id) {
        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT
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
        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT
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
        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT
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
        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT
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

        if (!"EDIT".equals(access.getAuth())) return ResponseEntity.status(403).build();  // [viewer-action-button-guard] 다운로드=EDIT

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
}
