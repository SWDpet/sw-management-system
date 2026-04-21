package com.swmanager.system.license.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.license.domain.LicenseRegistry;
import com.swmanager.system.license.domain.LicenseUploadHistory;
import com.swmanager.system.license.service.LicenseRegistryService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 라이선스 대장 관리 컨트롤러 (auth_license 권한 기반)
 */
@Slf4j
@Controller
@RequestMapping("/license/registry")
@RequiredArgsConstructor
public class LicenseRegistryController {
    
    private final LicenseRegistryService registryService;
    private final LogService logService;
    
    // ========================================
    // 권한 체크 헬퍼 메서드
    // ========================================
    
    /**
     * 조회 권한 체크 (VIEW 또는 EDIT)
     */
    /** ADMIN 여부 */
    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** VIEW 이상 권한 (ADMIN 포함) */
    private boolean hasViewPermission(CustomUserDetails userDetails) {
        if (isAdmin(userDetails)) return true;
        String auth = userDetails.getUser().getAuthLicense();
        return "VIEW".equals(auth) || "EDIT".equals(auth);
    }

    /** EDIT 권한 (ADMIN 포함) */
    private boolean hasEditPermission(CustomUserDetails userDetails) {
        if (isAdmin(userDetails)) return true;
        return "EDIT".equals(userDetails.getUser().getAuthLicense());
    }
    
    // ========================================
    // CSV 업로드 (EDIT 권한 필요)
    // ========================================
    
    /**
     * 업로드 페이지
     * GET /license/registry/upload
     */
    @GetMapping("/upload")
    public String uploadPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        log.info("=== 라이선스 대장 업로드 페이지 ===");
        
        // 권한 체크
        if (!hasEditPermission(userDetails)) {
            log.warn("업로드 권한 없음 - 사용자: {}", userDetails.getUsername());
            model.addAttribute("error", true);
            model.addAttribute("message", "업로드 권한이 없습니다. (EDIT 권한 필요)");
            return "license/registry-list";
        }
        
        // 최근 업로드 이력
        List<LicenseUploadHistory> history = registryService.getRecentUploadHistory();
        model.addAttribute("history", history);
        
        // 현재 대장 정보
        long totalCount = registryService.getTotalCount();
        LocalDateTime latestUploadDate = registryService.getLatestUploadDate();
        
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("latestUploadDate", latestUploadDate);
        
        return "license/registry-upload";
    }
    
    /**
     * CSV 파일 업로드 처리
     * POST /license/registry/upload
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        try {
            log.info("=== CSV 파일 업로드 요청 ===");
            
            // 권한 체크
            if (!hasEditPermission(userDetails)) {
                log.warn("업로드 권한 없음 - 사용자: {}", userDetails.getUsername());
                model.addAttribute("error", true);
                model.addAttribute("message", "업로드 권한이 없습니다.");
                return "license/registry-upload";
            }
            
            log.info("파일명: {}, 크기: {} bytes, ContentType: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            if (file.isEmpty()) {
                model.addAttribute("error", true);
                model.addAttribute("message", "파일을 선택해주세요.");
                return "license/registry-upload";
            }

            // 파일 확장자 확인
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                model.addAttribute("error", true);
                model.addAttribute("message", "CSV 파일만 업로드 가능합니다.");
                return "license/registry-upload";
            }

            // MIME 타입 검증
            String contentType = file.getContentType();
            if (contentType != null
                    && !contentType.equals("text/csv")
                    && !contentType.equals("application/vnd.ms-excel")
                    && !contentType.equals("text/plain")
                    && !contentType.equals("application/octet-stream")) {
                log.warn("허용되지 않는 MIME 타입: {} - 파일: {}", contentType, fileName);
                model.addAttribute("error", true);
                model.addAttribute("message", "허용되지 않는 파일 형식입니다. (CSV 파일만 가능)");
                return "license/registry-upload";
            }

            // 파일 크기 제한 (10MB)
            long maxFileSize = 10 * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                log.warn("파일 크기 초과: {} bytes - 파일: {}", file.getSize(), fileName);
                model.addAttribute("error", true);
                model.addAttribute("message", "파일 크기는 10MB를 초과할 수 없습니다.");
                return "license/registry-upload";
            }

            // 파일명 보안 검증 (경로 탐색 방지)
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.warn("위험한 파일명 감지: {}", fileName);
                model.addAttribute("error", true);
                model.addAttribute("message", "유효하지 않은 파일명입니다.");
                return "license/registry-upload";
            }
            
            String uploadedBy = userDetails.getUsername();
            
            // CSV 업로드 처리
            Map<String, Object> result = registryService.uploadCsvFile(file, uploadedBy);
            
            int totalCount = result.get("totalCount") != null ? (Integer) result.get("totalCount") : 0;
            int successCount = result.get("successCount") != null ? (Integer) result.get("successCount") : 0;
            int duplicateCount = result.get("duplicateCount") != null ? (Integer) result.get("duplicateCount") : 0;
            int failCount = result.get("failCount") != null ? (Integer) result.get("failCount") : 0;
            
            log.info("CSV 업로드 완료 - 총: {}건, 신규: {}건, 중복: {}건, 실패: {}건", 
                     totalCount, successCount, duplicateCount, failCount);
            
            // 로그 기록
            logService.log(MenuName.LICENSE_REGISTRY, AccessActionType.CREATE,
                String.format("CSV 업로드 완료 - 파일: %s, 총: %d건, 신규: %d건, 중복: %d건, 실패: %d건 - 사용자: %s",
                    fileName, totalCount, successCount, duplicateCount, failCount, uploadedBy));
            
            // RedirectAttributes 사용 (리다이렉트 후에도 메시지 유지)
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("uploadMessage", 
                String.format("✅ 업로드 완료: 총 %d건 중 신규 %d건 등록, 중복 %d건 스킵, 실패 %d건", 
                              totalCount, successCount, duplicateCount, failCount));
            
            return "redirect:/license/registry/list";
            
        } catch (Exception e) {
            log.error("CSV 업로드 실패", e);
            
            model.addAttribute("error", true);
            model.addAttribute("message", "업로드에 실패했습니다: " + e.getMessage());
            
            return "license/registry-upload";
        }
    }
    
    // ========================================
    // 대장 목록 (VIEW 또는 EDIT 권한 필요)
    // ========================================
    
    /**
     * 라이선스 대장 목록
     * GET /license/registry/list
     */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String productId,
                      @AuthenticationPrincipal CustomUserDetails userDetails,
                      Model model) {
        log.info("=== 라이선스 대장 목록 ===");
        
        // 권한 체크
        if (!hasViewPermission(userDetails)) {
            log.warn("조회 권한 없음 - 사용자: {}", userDetails.getUsername());
            model.addAttribute("error", true);
            model.addAttribute("message", "라이선스 대장 조회 권한이 없습니다.");
            return "redirect:/";
        }
        
        log.info("제품 필터: {}", productId);
        
        List<LicenseRegistry> licenses;
        
        if (productId != null && !productId.isEmpty()) {
            licenses = registryService.getLicensesByProduct(productId);
        } else {
            licenses = registryService.getAllLicenses();
        }
        
        // 최근 업로드 날짜
        LocalDateTime latestUploadDate = registryService.getLatestUploadDate();
        
        // 제품별 통계
        Map<String, Long> productStats = registryService.getProductStatistics();
        
        model.addAttribute("licenses", licenses);
        model.addAttribute("latestUploadDate", latestUploadDate);
        model.addAttribute("productStats", productStats);
        model.addAttribute("selectedProductId", productId);
        
        // ✨ EDIT 권한 여부를 모델에 추가
        model.addAttribute("hasEditPermission", hasEditPermission(userDetails));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        model.addAttribute("formatter", formatter);
        
        log.info("라이선스 {} 건 조회 완료", licenses.size());
        
        return "license/registry-list";
    }
    
    // ========================================
    // 상세 조회 (VIEW 또는 EDIT 권한 필요)
    // ========================================
    
    /**
     * 라이선스 상세
     * GET /license/registry/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        log.info("=== 라이선스 상세 조회 - ID: {} ===", id);
        
        // 권한 체크
        if (!hasViewPermission(userDetails)) {
            log.warn("조회 권한 없음 - 사용자: {}", userDetails.getUsername());
            model.addAttribute("error", true);
            model.addAttribute("message", "라이선스 조회 권한이 없습니다.");
            return "redirect:/";
        }
        
        try {
            LicenseRegistry license = registryService.getLicenseById(id);
            
            model.addAttribute("license", license);
            model.addAttribute("hasEditPermission", hasEditPermission(userDetails));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            model.addAttribute("formatter", formatter);
            
            return "license/registry-detail";
            
        } catch (Exception e) {
            log.error("라이선스 조회 실패", e);
            model.addAttribute("error", true);
            model.addAttribute("message", "라이선스를 찾을 수 없습니다.");
            return "redirect:/license/registry/list";
        }
    }
    
    /**
     * 라이선스 수정 처리
     * POST /license/registry/update
     */
    @PostMapping("/update")
    public String update(@RequestParam Long id,
                        @ModelAttribute LicenseRegistry updatedLicense,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        log.info("=== 라이선스 수정 - ID: {} ===", id);
        
        // 권한 체크 (EDIT 권한만 허용)
        if (!hasEditPermission(userDetails)) {
            log.warn("수정 권한 없음 - 사용자: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", "라이선스 수정 권한이 없습니다.");
            return "redirect:/license/registry/detail/" + id;
        }
        
        try {
            LicenseRegistry savedLicense = registryService.updateLicense(id, updatedLicense);
            
            // 로그 기록
            logService.log(MenuName.LICENSE_REGISTRY, AccessActionType.UPDATE,
                String.format("라이선스 수정 완료 - License ID: %s, Product: %s - 사용자: %s",
                    savedLicense.getLicenseId(), savedLicense.getProductId(), userDetails.getUsername()));
            
            log.info("라이선스 수정 완료 - ID: {}", id);
            
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "라이선스가 성공적으로 수정되었습니다.");
            
            return "redirect:/license/registry/detail/" + id;
            
        } catch (Exception e) {
            log.error("라이선스 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", "라이선스 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/license/registry/detail/" + id;
        }
    }
    
    // ========================================
    // 다운로드 (VIEW 또는 EDIT 권한 필요)
    // ========================================
    
    /**
     * 라이선스 파일 다운로드
     * GET /license/registry/download/{id}
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 권한 체크 (EDIT 권한만 허용)
            if (!hasEditPermission(userDetails)) {
                log.warn("다운로드 권한 없음 - 사용자: {} (EDIT 권한 필요)", userDetails.getUsername());
                return ResponseEntity.status(403).build();
            }
            
            log.info("=== 라이선스 파일 다운로드 - ID: {} ===", id);
            
            LicenseRegistry license = registryService.getLicenseById(id);
            String licenseString = license.getLicenseString();
            
            byte[] bytes = licenseString.getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            
            String filename = license.getLicenseId() + ".lic";
            
            // 로그 기록
            logService.log(MenuName.LICENSE_REGISTRY, AccessActionType.DOWNLOAD,
                String.format("라이선스 파일 다운로드 - License ID: %s, Product: %s - 사용자: %s",
                    license.getLicenseId(), license.getProductId(), userDetails.getUsername()));
            
            log.info("다운로드 준비 완료 - 파일명: {}, 크기: {} bytes", filename, bytes.length);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(bytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("다운로드 실패", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========================================
    // 통계 (대시보드용)
    // ========================================
    
    /**
     * 대시보드 통계 API
     * GET /license/registry/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("=== 대시보드 통계 조회 ===");
        
        // 권한 체크
        if (!hasViewPermission(userDetails)) {
            return Map.of(
                "totalCount", 0,
                "error", "권한 없음"
            );
        }
        
        long totalCount = registryService.getTotalCount();
        LocalDateTime latestUploadDate = registryService.getLatestUploadDate();
        List<LicenseRegistry> expiringLicenses = registryService.getExpiringLicenses(30);
        List<LicenseRegistry> expiredLicenses = registryService.getExpiredLicenses();
        Map<String, Long> productStats = registryService.getProductStatistics();
        
        long newCount      = registryService.getNewCount();
        long existingCount = totalCount - newCount;
        java.time.LocalDate latestUploadLocalDate = registryService.getLatestUploadLocalDate();

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalCount",            totalCount);
        result.put("newCount",              newCount);
        result.put("existingCount",         existingCount);
        result.put("latestUploadDate",      latestUploadDate != null ? latestUploadDate.toString() : "");
        result.put("latestUploadLocalDate", latestUploadLocalDate != null ? latestUploadLocalDate.toString() : "");
        result.put("expiringCount",         expiringLicenses.size());
        result.put("expiredCount",          expiredLicenses.size());
        result.put("productStats",          productStats);
        return result;
    }
    
    // ========================================
    // CSV 다운로드 (VIEW 권한 이상 필요)
    // ========================================
    
    /**
     * CSV 다운로드
     * GET /license/registry/download/csv
     */
    @GetMapping("/download/csv")
    public ResponseEntity<Resource> downloadCsv(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("=== CSV 다운로드 요청 ===");
        
        // 권한 체크 (VIEW 이상)
        if (!hasViewPermission(userDetails)) {
            log.warn("다운로드 권한 없음 - 사용자: {}", userDetails.getUsername());
            return ResponseEntity.status(403).build();
        }
        
        try {
            // 모든 라이선스 조회
            List<LicenseRegistry> licenses = registryService.getAllLicenses();
            
            // CSV 생성
            StringBuilder csv = new StringBuilder();
            
            // CSV 헤더 (63개 컬럼)
            csv.append("License ID,Product ID,License Type,Valid Product Edition,Valid Product Version,");
            csv.append("Validity Period,Maintenance Period,Generation Source,Generation Date Time,Quantity,");
            csv.append("Allowed Use Count,Hardware ID,Registered To,Full Name,E-Mail,Company,");
            csv.append("Telephone,Fax,Street,City,Zip Code,Country,");
            csv.append("Activation Required,Auto Activations Disabled,Manual Activations Disabled,");
            csv.append("Online Key Lease Disabled,Deactivations Disabled,Activation Period,");
            csv.append("Allowed Activation Count,Allowed Deactivation Count,Dont Keep Deactivation Records,");
            csv.append("IP Blocks,IP Blocks Allow,Activation Return,Reject Modification Key Usage,");
            csv.append("Set Generation Time to Activation Time,Generation Time Offset From Activation Time,");
            csv.append("Hardware ID Selection,Internal Hidden String,Use Customer Name in Validation,");
            csv.append("Use Company Name in Validation,Floating License Check Period,");
            csv.append("Floating License Server Connection Check Period,Floating Allow Multiple Instances,");
            csv.append("Superseded License IDs,Max Inactive Period,Maximum Re-Checks Before Drop,");
            csv.append("Don't Keep Released License Usage,Current Use Count,Allowed Use Count Limit,");
            csv.append("Current Use Time,Allowed Use Time Limit,Date Time Check,NTP Server Check,");
            csv.append("NTP Server,NTP Server Timeout,Web Server Check,Web Server,Web Server Timeout,");
            csv.append("Query Local AD Server,Unsigned Custom Features,Signed Custom Features,License String\n");
            
            // 데이터 행
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (LicenseRegistry lic : licenses) {
                csv.append(escapeCsv(lic.getLicenseId())).append(",");
                csv.append(escapeCsv(lic.getProductId())).append(",");
                csv.append(escapeCsv(lic.getLicenseType())).append(",");
                csv.append(escapeCsv(lic.getValidProductEdition())).append(",");
                csv.append(escapeCsv(lic.getValidProductVersion())).append(",");
                csv.append(lic.getValidityPeriod() != null ? lic.getValidityPeriod() : "").append(",");
                csv.append(lic.getMaintenancePeriod() != null ? lic.getMaintenancePeriod() : "").append(",");
                csv.append(escapeCsv(lic.getGenerationSource())).append(",");
                csv.append(lic.getGenerationDateTime() != null ? lic.getGenerationDateTime().format(dateFormatter) : "").append(",");
                csv.append(lic.getQuantity() != null ? lic.getQuantity() : "").append(",");
                csv.append(lic.getAllowedUseCount() != null ? lic.getAllowedUseCount() : "").append(",");
                csv.append(escapeCsv(lic.getHardwareId())).append(",");
                csv.append(escapeCsv(lic.getRegisteredTo())).append(",");
                csv.append(escapeCsv(lic.getFullName())).append(",");
                csv.append(escapeCsv(lic.getEmail())).append(",");
                csv.append(escapeCsv(lic.getCompany())).append(",");
                csv.append(escapeCsv(lic.getTelephone())).append(",");
                csv.append(escapeCsv(lic.getFax())).append(",");
                csv.append(escapeCsv(lic.getStreet())).append(",");
                csv.append(escapeCsv(lic.getCity())).append(",");
                csv.append(escapeCsv(lic.getZipCode())).append(",");
                csv.append(escapeCsv(lic.getCountry())).append(",");
                csv.append(lic.getActivationRequired() != null ? lic.getActivationRequired() : "").append(",");
                csv.append(lic.getAutoActivationsDisabled() != null ? lic.getAutoActivationsDisabled() : "").append(",");
                csv.append(lic.getManualActivationsDisabled() != null ? lic.getManualActivationsDisabled() : "").append(",");
                csv.append(lic.getOnlineKeyLeaseDisabled() != null ? lic.getOnlineKeyLeaseDisabled() : "").append(",");
                csv.append(lic.getDeactivationsDisabled() != null ? lic.getDeactivationsDisabled() : "").append(",");
                csv.append(lic.getActivationPeriod() != null ? lic.getActivationPeriod() : "").append(",");
                csv.append(lic.getAllowedActivationCount() != null ? lic.getAllowedActivationCount() : "").append(",");
                csv.append(lic.getAllowedDeactivationCount() != null ? lic.getAllowedDeactivationCount() : "").append(",");
                csv.append(lic.getDontKeepDeactivationRecords() != null ? lic.getDontKeepDeactivationRecords() : "").append(",");
                csv.append(escapeCsv(lic.getIpBlocks())).append(",");
                csv.append(lic.getIpBlocksAllow() != null ? lic.getIpBlocksAllow() : "").append(",");
                csv.append(escapeCsv(lic.getActivationReturn())).append(",");
                csv.append(lic.getRejectModificationKeyUsage() != null ? lic.getRejectModificationKeyUsage() : "").append(",");
                csv.append(lic.getSetGenerationTimeToActivationTime() != null ? lic.getSetGenerationTimeToActivationTime() : "").append(",");
                csv.append(lic.getGenerationTimeOffsetFromActivationTime() != null ? lic.getGenerationTimeOffsetFromActivationTime() : "").append(",");
                csv.append(escapeCsv(lic.getHardwareIdSelection())).append(",");
                csv.append(escapeCsv(lic.getInternalHiddenString())).append(",");
                csv.append(lic.getUseCustomerNameInValidation() != null ? lic.getUseCustomerNameInValidation() : "").append(",");
                csv.append(lic.getUseCompanyNameInValidation() != null ? lic.getUseCompanyNameInValidation() : "").append(",");
                csv.append(lic.getFloatingLicenseCheckPeriod() != null ? lic.getFloatingLicenseCheckPeriod() : "").append(",");
                csv.append(lic.getFloatingLicenseServerConnectionCheckPeriod() != null ? lic.getFloatingLicenseServerConnectionCheckPeriod() : "").append(",");
                csv.append(lic.getFloatingAllowMultipleInstances() != null ? lic.getFloatingAllowMultipleInstances() : "").append(",");
                csv.append(escapeCsv(lic.getSupersededLicenseIds())).append(",");
                csv.append(lic.getMaxInactivePeriod() != null ? lic.getMaxInactivePeriod() : "").append(",");
                csv.append(lic.getMaximumReChecksBeforeDrop() != null ? lic.getMaximumReChecksBeforeDrop() : "").append(",");
                csv.append(lic.getDontKeepReleasedLicenseUsage() != null ? lic.getDontKeepReleasedLicenseUsage() : "").append(",");
                csv.append(lic.getCurrentUseCount() != null ? lic.getCurrentUseCount() : "").append(",");
                csv.append(lic.getAllowedUseCountLimit() != null ? lic.getAllowedUseCountLimit() : "").append(",");
                csv.append(lic.getCurrentUseTime() != null ? lic.getCurrentUseTime() : "").append(",");
                csv.append(lic.getAllowedUseTimeLimit() != null ? lic.getAllowedUseTimeLimit() : "").append(",");
                csv.append(escapeCsv(lic.getDateTimeCheck())).append(",");
                csv.append(lic.getNtpServerCheck() != null ? lic.getNtpServerCheck() : "").append(",");
                csv.append(escapeCsv(lic.getNtpServer())).append(",");
                csv.append(lic.getNtpServerTimeout() != null ? lic.getNtpServerTimeout() : "").append(",");
                csv.append(lic.getWebServerCheck() != null ? lic.getWebServerCheck() : "").append(",");
                csv.append(escapeCsv(lic.getWebServer())).append(",");
                csv.append(lic.getWebServerTimeout() != null ? lic.getWebServerTimeout() : "").append(",");
                csv.append(lic.getQueryLocalAdServer() != null ? lic.getQueryLocalAdServer() : "").append(",");
                csv.append(escapeCsv(lic.getUnsignedCustomFeatures())).append(",");
                csv.append(escapeCsv(lic.getSignedCustomFeatures())).append(",");
                csv.append(escapeCsv(lic.getLicenseString())).append("\n");
            }
            
            // 파일명 생성
            String fileName = "license_registry_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            // ByteArrayResource 생성
            byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            
            // 로그 기록
            logService.log(MenuName.LICENSE_REGISTRY, AccessActionType.DOWNLOAD,
                String.format("CSV 다운로드 완료 - 총 %d건 - 사용자: %s", licenses.size(), userDetails.getUsername()));
            
            log.info("CSV 다운로드 완료 - {} 건", licenses.size());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .contentLength(bytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("CSV 다운로드 실패", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * CSV 값 이스케이프 처리
     */
    private String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // 콤마, 따옴표, 줄바꿈이 있으면 따옴표로 감싸기
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // 따옴표를 두 개로 이스케이프
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
}

