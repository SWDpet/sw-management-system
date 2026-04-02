package com.swmanager.system.license.service;

import com.swmanager.system.license.domain.LicenseRegistry;
import com.swmanager.system.license.domain.LicenseUploadHistory;
import com.swmanager.system.license.repository.LicenseRegistryRepository;
import com.swmanager.system.license.repository.LicenseUploadHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 라이선스 대장 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseRegistryService {
    
    private final LicenseRegistryRepository licenseRegistryRepository;
    private final LicenseUploadHistoryRepository uploadHistoryRepository;
    
    /**
     * CSV 파일 업로드 및 파싱
     */
    @Transactional
    public Map<String, Object> uploadCsvFile(MultipartFile file, String uploadedBy) {
        log.info("=== CSV 파일 업로드 시작 ===");
        
        int totalCount = 0;
        int successCount = 0;
        int duplicateCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            // 헤더 읽어서 컬럼명→인덱스 맵 생성
            String header = reader.readLine();
            lineNumber++;
            log.info("CSV 헤더: {}", header);
            Map<String, Integer> headerMap = buildHeaderMap(header);
            log.info("헤더 컬럼 수: {}", headerMap.size());
            
            List<LicenseRegistry> registries = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalCount++;
                
                if (line.trim().isEmpty()) {
                    log.debug("빈 라인 스킵: {}", lineNumber);
                    totalCount--;
                    continue;
                }
                
                try {
                    LicenseRegistry registry = parseCsvLine(line, headerMap);

                    // 중복 체크: License ID + Product ID 조합
                    Optional<LicenseRegistry> existing = licenseRegistryRepository
                            .findByLicenseIdAndProductId(registry.getLicenseId(), registry.getProductId());

                    if (existing.isPresent()) {
                        LicenseRegistry ex = existing.get();
                        boolean hardwareChanged = !Objects.equals(ex.getHardwareId(),     registry.getHardwareId());
                        boolean stringChanged   = !Objects.equals(ex.getLicenseString(),  registry.getLicenseString());

                        if (hardwareChanged || stringChanged) {
                            // Hardware ID 또는 License String이 바뀐 경우 → 전체 필드 업데이트
                            copyAllFields(registry, ex);
                            ex.setUploadDate(LocalDateTime.now());
                            ex.setUploadedBy(uploadedBy);
                            registries.add(ex);   // saveAll로 merge됨 (id 있음)
                            successCount++;
                            log.info("라이선스 업데이트 - License ID: {}, 변경: hardware={}, string={}",
                                    ex.getLicenseId(), hardwareChanged, stringChanged);
                        } else {
                            duplicateCount++;
                            log.debug("완전 중복 스킵 - License ID: {}, Product ID: {}",
                                    registry.getLicenseId(), registry.getProductId());
                        }
                    } else {
                        registry.setUploadDate(LocalDateTime.now());
                        registry.setUploadedBy(uploadedBy);
                        registries.add(registry);
                        successCount++;
                    }

                } catch (Exception e) {
                    log.error("라인 {} 파싱 실패: {}", lineNumber, e.getMessage());
                    errors.add("라인 " + lineNumber + ": " + e.getMessage());
                    failCount++;
                }
            }
            
            // 일괄 저장
            if (!registries.isEmpty()) {
                try {
                    licenseRegistryRepository.saveAll(registries);
                    log.info("라이선스 {} 건 저장 완료 (중복 {}건 스킵)", registries.size(), duplicateCount);
                } catch (Exception e) {
                    log.error("데이터베이스 저장 실패", e);
                    errors.add("데이터베이스 저장 실패: " + e.getMessage());
                    failCount += registries.size();
                    successCount = 0;
                }
            }
            
            // 업로드 이력 저장
            try {
                LicenseUploadHistory history = new LicenseUploadHistory();
                history.setFileName(fileName);
                history.setTotalCount(totalCount);
                history.setSuccessCount(successCount);
                history.setFailCount(failCount);
                history.setUploadedBy(uploadedBy);
                history.setUploadDate(LocalDateTime.now());
                
                uploadHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("업로드 이력 저장 실패", e);
                // 이력 저장 실패는 전체 프로세스를 중단하지 않음
            }
            
        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패", e);
            errors.add("파일 읽기 오류: " + e.getMessage());
            failCount++;
        } catch (Exception e) {
            log.error("CSV 업로드 중 예상치 못한 오류", e);
            errors.add("예상치 못한 오류: " + e.getMessage());
            failCount++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("newCount", successCount);
        result.put("duplicateCount", duplicateCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        log.info("CSV 업로드 완료 - 총: {}건, 신규: {}건, 중복: {}건, 실패: {}건", 
                 totalCount, successCount, duplicateCount, failCount);
        
        return result;
    }
    
    /**
     * CSV 파싱 결과(src)의 모든 CSV 필드를 기존 엔티티(dest)에 복사
     * id, uploadDate, uploadedBy 등 시스템 필드는 dest 것을 유지
     */
    private void copyAllFields(LicenseRegistry src, LicenseRegistry dest) {
        dest.setProductId(src.getProductId());
        dest.setLicenseType(src.getLicenseType());
        dest.setValidProductEdition(src.getValidProductEdition());
        dest.setValidProductVersion(src.getValidProductVersion());
        dest.setValidityPeriod(src.getValidityPeriod());
        dest.setMaintenancePeriod(src.getMaintenancePeriod());
        dest.setGenerationSource(src.getGenerationSource());
        dest.setGenerationDateTime(src.getGenerationDateTime());
        dest.setQuantity(src.getQuantity());
        dest.setAllowedUseCount(src.getAllowedUseCount());
        dest.setHardwareId(src.getHardwareId());
        dest.setRegisteredTo(src.getRegisteredTo());
        dest.setFullName(src.getFullName());
        dest.setEmail(src.getEmail());
        dest.setCompany(src.getCompany());
        dest.setTelephone(src.getTelephone());
        dest.setFax(src.getFax());
        dest.setStreet(src.getStreet());
        dest.setCity(src.getCity());
        dest.setZipCode(src.getZipCode());
        dest.setCountry(src.getCountry());
        dest.setActivationRequired(src.getActivationRequired());
        dest.setAutoActivationsDisabled(src.getAutoActivationsDisabled());
        dest.setManualActivationsDisabled(src.getManualActivationsDisabled());
        dest.setOnlineKeyLeaseDisabled(src.getOnlineKeyLeaseDisabled());
        dest.setDeactivationsDisabled(src.getDeactivationsDisabled());
        dest.setActivationPeriod(src.getActivationPeriod());
        dest.setAllowedActivationCount(src.getAllowedActivationCount());
        dest.setAllowedDeactivationCount(src.getAllowedDeactivationCount());
        dest.setDontKeepDeactivationRecords(src.getDontKeepDeactivationRecords());
        dest.setIpBlocks(src.getIpBlocks());
        dest.setIpBlocksAllow(src.getIpBlocksAllow());
        dest.setActivationReturn(src.getActivationReturn());
        dest.setRejectModificationKeyUsage(src.getRejectModificationKeyUsage());
        dest.setSetGenerationTimeToActivationTime(src.getSetGenerationTimeToActivationTime());
        dest.setGenerationTimeOffsetFromActivationTime(src.getGenerationTimeOffsetFromActivationTime());
        dest.setHardwareIdSelection(src.getHardwareIdSelection());
        dest.setInternalHiddenString(src.getInternalHiddenString());
        dest.setUseCustomerNameInValidation(src.getUseCustomerNameInValidation());
        dest.setUseCompanyNameInValidation(src.getUseCompanyNameInValidation());
        dest.setFloatingLicenseCheckPeriod(src.getFloatingLicenseCheckPeriod());
        dest.setFloatingLicenseServerConnectionCheckPeriod(src.getFloatingLicenseServerConnectionCheckPeriod());
        dest.setFloatingAllowMultipleInstances(src.getFloatingAllowMultipleInstances());
        dest.setSupersededLicenseIds(src.getSupersededLicenseIds());
        dest.setMaxInactivePeriod(src.getMaxInactivePeriod());
        dest.setMaximumReChecksBeforeDrop(src.getMaximumReChecksBeforeDrop());
        dest.setDontKeepReleasedLicenseUsage(src.getDontKeepReleasedLicenseUsage());
        dest.setCurrentUseCount(src.getCurrentUseCount());
        dest.setAllowedUseCountLimit(src.getAllowedUseCountLimit());
        dest.setCurrentUseTime(src.getCurrentUseTime());
        dest.setAllowedUseTimeLimit(src.getAllowedUseTimeLimit());
        dest.setDateTimeCheck(src.getDateTimeCheck());
        dest.setNtpServerCheck(src.getNtpServerCheck());
        dest.setNtpServer(src.getNtpServer());
        dest.setNtpServerTimeout(src.getNtpServerTimeout());
        dest.setWebServerCheck(src.getWebServerCheck());
        dest.setWebServer(src.getWebServer());
        dest.setWebServerTimeout(src.getWebServerTimeout());
        dest.setQueryLocalAdServer(src.getQueryLocalAdServer());
        dest.setUnsignedCustomFeatures(src.getUnsignedCustomFeatures());
        dest.setSignedCustomFeatures(src.getSignedCustomFeatures());
        dest.setLicenseString(src.getLicenseString());
    }

    /**
     * RFC4180 CSV 파서 - 따옴표로 감싼 필드(쉼표 포함) 정상 처리
     */
    private List<String> parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"'); i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        fields.add(sb.toString());
        return fields;
    }

    /** CSV 헤더 행으로 컬럼명 → 인덱스 맵 생성 */
    private Map<String, Integer> buildHeaderMap(String headerLine) {
        List<String> headers = parseCsvFields(headerLine);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i).trim(), i);
        }
        return map;
    }

    private String getStr(List<String> f, Map<String, Integer> hdr, String col) {
        Integer idx = hdr.get(col);
        if (idx == null || idx >= f.size()) return "";
        return f.get(idx).trim();
    }
    private Integer getInt(List<String> f, Map<String, Integer> hdr, String col) {
        return parseInteger(getStr(f, hdr, col));
    }
    private Boolean getBool(List<String> f, Map<String, Integer> hdr, String col) {
        return parseBoolean(getStr(f, hdr, col));
    }

    /**
     * CSV 라인 파싱 (헤더맵 기반 - 컬럼 순서/수 무관)
     */
    private LicenseRegistry parseCsvLine(String line, Map<String, Integer> hdr) {
        List<String> f = parseCsvFields(line);
        LicenseRegistry registry = new LicenseRegistry();
        registry.setLicenseId(getStr(f, hdr, "License ID"));
        registry.setProductId(getStr(f, hdr, "Product ID"));
        registry.setLicenseType(getStr(f, hdr, "License Type"));
        registry.setValidProductEdition(getStr(f, hdr, "Valid Product Edition"));
        registry.setValidProductVersion(getStr(f, hdr, "Valid Product Version"));
        registry.setValidityPeriod(getInt(f, hdr, "Validity Period"));
        registry.setMaintenancePeriod(getInt(f, hdr, "Maintenance Period"));
        registry.setGenerationSource(getStr(f, hdr, "Generation Source"));
        registry.setGenerationDateTime(parseDateTime(getStr(f, hdr, "Generation Date Time")));
        registry.setQuantity(getInt(f, hdr, "Quantity"));
        registry.setAllowedUseCount(getInt(f, hdr, "Allowed Use Count"));
        registry.setHardwareId(getStr(f, hdr, "Hardware ID"));
        registry.setRegisteredTo(getStr(f, hdr, "Registered To"));
        registry.setFullName(getStr(f, hdr, "Full Name"));
        registry.setEmail(getStr(f, hdr, "E-Mail"));
        registry.setCompany(getStr(f, hdr, "Company"));
        registry.setTelephone(getStr(f, hdr, "Telephone"));
        registry.setFax(getStr(f, hdr, "Fax"));
        registry.setStreet(getStr(f, hdr, "Street"));
        registry.setCity(getStr(f, hdr, "City"));
        registry.setZipCode(getStr(f, hdr, "Zip Code"));
        registry.setCountry(getStr(f, hdr, "Country"));
        registry.setActivationRequired(getBool(f, hdr, "Activation Required"));
        registry.setAutoActivationsDisabled(getBool(f, hdr, "Auto Activations Disabled"));
        registry.setManualActivationsDisabled(getBool(f, hdr, "Manual Activations Disabled"));
        registry.setOnlineKeyLeaseDisabled(getBool(f, hdr, "Online Key Lease Disabled"));
        registry.setDeactivationsDisabled(getBool(f, hdr, "Deactivations Disabled"));
        registry.setActivationPeriod(getInt(f, hdr, "Activation Period"));
        registry.setAllowedActivationCount(getInt(f, hdr, "Allowed Activation Count"));
        registry.setAllowedDeactivationCount(getInt(f, hdr, "Allowed Deactivation Count"));
        registry.setDontKeepDeactivationRecords(getBool(f, hdr, "Dont Keep Deactivation Records"));
        registry.setIpBlocks(getStr(f, hdr, "IP Blocks"));
        registry.setIpBlocksAllow(getBool(f, hdr, "IP Blocks Allow"));
        registry.setActivationReturn(getStr(f, hdr, "Activation Return"));
        registry.setRejectModificationKeyUsage(getBool(f, hdr, "Reject Modification Key Usage"));
        registry.setSetGenerationTimeToActivationTime(getBool(f, hdr, "Set Generation Time to Activation Time"));
        registry.setGenerationTimeOffsetFromActivationTime(getInt(f, hdr, "Generation Time Offset From Activation Time"));
        registry.setHardwareIdSelection(getStr(f, hdr, "Hardware ID Selection"));
        registry.setInternalHiddenString(getStr(f, hdr, "Internal Hidden String"));
        registry.setUseCustomerNameInValidation(getBool(f, hdr, "Use Customer Name in Validation"));
        registry.setUseCompanyNameInValidation(getBool(f, hdr, "Use Company Name in Validation"));
        registry.setFloatingLicenseCheckPeriod(getInt(f, hdr, "Floating License Check Period"));
        registry.setFloatingLicenseServerConnectionCheckPeriod(getInt(f, hdr, "Floating License Server Connection Check Period"));
        registry.setFloatingAllowMultipleInstances(getBool(f, hdr, "Floating Allow Multiple Instances"));
        registry.setSupersededLicenseIds(getStr(f, hdr, "Superseded License IDs"));
        registry.setMaxInactivePeriod(getInt(f, hdr, "Max Inactive Period"));
        registry.setMaximumReChecksBeforeDrop(getInt(f, hdr, "Maximum Re-Checks Before Drop"));
        registry.setDontKeepReleasedLicenseUsage(getBool(f, hdr, "Don't Keep Released License Usage"));
        registry.setCurrentUseCount(getInt(f, hdr, "Current Use Count"));
        registry.setAllowedUseCountLimit(getInt(f, hdr, "Allowed Use Count Limit"));
        registry.setCurrentUseTime(getInt(f, hdr, "Current Use Time"));
        registry.setAllowedUseTimeLimit(getInt(f, hdr, "Allowed Use Time Limit"));
        registry.setDateTimeCheck(getStr(f, hdr, "Date Time Check"));
        registry.setNtpServerCheck(getBool(f, hdr, "NTP Server Check"));
        registry.setNtpServer(getStr(f, hdr, "NTP Server"));
        registry.setNtpServerTimeout(getInt(f, hdr, "NTP Server Timeout"));
        registry.setWebServerCheck(getBool(f, hdr, "Web Server Check"));
        registry.setWebServer(getStr(f, hdr, "Web Server"));
        registry.setWebServerTimeout(getInt(f, hdr, "Web Server Timeout"));
        registry.setQueryLocalAdServer(getBool(f, hdr, "Query Local AD Server"));
        registry.setUnsignedCustomFeatures(getStr(f, hdr, "Unsigned Custom Features"));
        registry.setSignedCustomFeatures(getStr(f, hdr, "Signed Custom Features"));
        registry.setLicenseString(getStr(f, hdr, "License String"));
        return registry;
    }

        /**
     * 정수 파싱
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Boolean 파싱
     */
    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return "True".equalsIgnoreCase(value.trim()) || "true".equalsIgnoreCase(value.trim());
    }
    
    /**
     * 날짜 파싱
     */
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            // CSV 형식: 2025-04-25 11:10
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(value.trim(), formatter);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", value);
            return null;
        }
    }
    
    /**
     * 전체 라이선스 조회
     */
    public List<LicenseRegistry> getAllLicenses() {
        log.info("=== 전체 라이선스 조회 ===");
        return licenseRegistryRepository.findAll();
    }
    
    /**
     * 특정 제품의 라이선스 조회
     */
    public List<LicenseRegistry> getLicensesByProduct(String productId) {
        log.info("=== 제품별 라이선스 조회 - productId: {} ===", productId);
        return licenseRegistryRepository.findByProductId(productId);
    }
    
    /**
     * ID로 라이선스 조회
     */
    public LicenseRegistry getLicenseById(Long id) {
        log.info("=== 라이선스 조회 - ID: {} ===", id);
        return licenseRegistryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("라이선스를 찾을 수 없습니다: " + id));
    }
    
    /**
     * 제품별 통계 (product_id별 라이선스 개수)
     */
    public Map<String, Long> getProductStatistics() {
        log.info("=== 제품별 통계 계산 ===");
        
        List<LicenseRegistry> allLicenses = licenseRegistryRepository.findAll();
        
        Map<String, Long> stats = allLicenses.stream()
                .collect(Collectors.groupingBy(
                    LicenseRegistry::getProductId,
                    Collectors.counting()
                ));
        
        log.info("제품별 통계: {}", stats);
        
        return stats;
    }
    
    /**
     * 최근 업로드 날짜 조회
     */
    public LocalDateTime getLatestUploadDate() {
        List<LicenseRegistry> licenses = licenseRegistryRepository.findAll();
        
        return licenses.stream()
                .map(LicenseRegistry::getUploadDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
    
    /**
     * 최신 업로드 날짜(LocalDate) 반환
     */
    public LocalDate getLatestUploadLocalDate() {
        LocalDateTime dt = getLatestUploadDate();
        return dt != null ? dt.toLocalDate() : null;
    }

    /**
     * 신규 건수 - 가장 최근 업로드 날짜(일자)와 같은 것
     */
    public long getNewCount() {
        LocalDate latestDate = getLatestUploadLocalDate();
        if (latestDate == null) return 0L;
        LocalDateTime start = latestDate.atStartOfDay();
        LocalDateTime end   = latestDate.plusDays(1).atStartOfDay();
        return licenseRegistryRepository.countByUploadDateBetween(start, end);
    }

    /**
     * 신규 라이선스 목록 - 가장 최근 업로드 날짜(일자)와 같은 것
     */
    public java.util.List<LicenseRegistry> getNewLicenses() {
        LocalDate latestDate = getLatestUploadLocalDate();
        if (latestDate == null) return java.util.Collections.emptyList();
        LocalDateTime start = latestDate.atStartOfDay();
        LocalDateTime end   = latestDate.plusDays(1).atStartOfDay();
        return licenseRegistryRepository.findByUploadDateBetweenOrderByIdDesc(start, end);
    }

    /**
     * 총 개수
     */
    public long getTotalCount() {
        return licenseRegistryRepository.count();
    }
    
    /**
     * 곧 만료되는 라이선스 조회
     */
    public List<LicenseRegistry> getExpiringLicenses(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        return licenseRegistryRepository.findAll().stream()
                .filter(lic -> lic.getExpiryDate() != null)
                .filter(lic -> lic.getExpiryDate().isAfter(now))
                .filter(lic -> lic.getExpiryDate().isBefore(futureDate))
                .collect(Collectors.toList());
    }
    
    /**
     * 만료된 라이선스 조회
     */
    public List<LicenseRegistry> getExpiredLicenses() {
        LocalDateTime now = LocalDateTime.now();
        
        return licenseRegistryRepository.findAll().stream()
                .filter(lic -> lic.getExpiryDate() != null)
                .filter(lic -> lic.getExpiryDate().isBefore(now))
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 업로드 이력 조회
     */
    public List<LicenseUploadHistory> getRecentUploadHistory() {
        return uploadHistoryRepository.findTop10ByOrderByUploadDateDesc();
    }
    
    /**
     * 전체 통계 조회
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = licenseRegistryRepository.count();
        long activeCount = licenseRegistryRepository.findAll().stream()
                .filter(lic -> !lic.isExpired())
                .count();
        long expiredCount = licenseRegistryRepository.findAll().stream()
                .filter(LicenseRegistry::isExpired)
                .count();
        
        long newCount      = getNewCount();
        long existingCount = totalCount - newCount;
        LocalDate latestUploadLocalDate = getLatestUploadLocalDate();

        stats.put("totalCount", totalCount);
        stats.put("activeCount", activeCount);
        stats.put("expiredCount", expiredCount);
        stats.put("latestUploadDate", getLatestUploadDate());
        stats.put("newCount", newCount);
        stats.put("existingCount", existingCount);
        stats.put("latestUploadLocalDate", latestUploadLocalDate);
        
        return stats;
    }
    
    // ========== 페이징 조회 메서드 ==========
    
    /**
     * 전체 라이선스 조회 (페이징)
     */
    public Page<LicenseRegistry> getAllLicenses(Pageable pageable) {
        return licenseRegistryRepository.findAll(pageable);
    }
    
    /**
     * Product ID로 필터링 (페이징)
     */
    public Page<LicenseRegistry> getLicensesByProductId(String productId, Pageable pageable) {
        return licenseRegistryRepository.findByProductId(productId, pageable);
    }
    
    /**
     * Full Name으로 검색 (페이징)
     */
    public Page<LicenseRegistry> searchByFullName(String keyword, Pageable pageable) {
        return licenseRegistryRepository.searchByFullName(keyword, pageable);
    }
    
    /**
     * Registered To로 검색 (페이징)
     */
    public Page<LicenseRegistry> searchByRegisteredTo(String keyword, Pageable pageable) {
        return licenseRegistryRepository.searchByRegisteredTo(keyword, pageable);
    }
    
    /**
     * Company로 검색 (페이징)
     */
    public Page<LicenseRegistry> searchByCompany(String keyword, Pageable pageable) {
        return licenseRegistryRepository.searchByCompany(keyword, pageable);
    }
    
    /**
     * Hardware ID로 검색 (페이징)
     */
    public Page<LicenseRegistry> searchByHardwareId(String keyword, Pageable pageable) {
        return licenseRegistryRepository.searchByHardwareId(keyword, pageable);
    }
    
    /**
     * 라이선스 정보 수정
     */
    @Transactional
    public LicenseRegistry updateLicense(Long id, LicenseRegistry updatedLicense) {
        log.info("=== 라이선스 수정: ID {} ===", id);
        
        LicenseRegistry existingLicense = licenseRegistryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("라이선스를 찾을 수 없습니다: ID " + id));
        
        // 수정 가능한 필드 업데이트
        existingLicense.setProductId(updatedLicense.getProductId());
        existingLicense.setFullName(updatedLicense.getFullName());
        existingLicense.setRegisteredTo(updatedLicense.getRegisteredTo());
        existingLicense.setCompany(updatedLicense.getCompany());
        existingLicense.setEmail(updatedLicense.getEmail());
        existingLicense.setTelephone(updatedLicense.getTelephone());
        existingLicense.setCountry(updatedLicense.getCountry());
        
        existingLicense.setGenerationDateTime(updatedLicense.getGenerationDateTime());
        existingLicense.setValidityPeriod(updatedLicense.getValidityPeriod());
        
        existingLicense.setHardwareId(updatedLicense.getHardwareId());
        existingLicense.setLicenseType(updatedLicense.getLicenseType());
        existingLicense.setLicenseString(updatedLicense.getLicenseString());
        
        LicenseRegistry savedLicense = licenseRegistryRepository.save(existingLicense);
        
        log.info("라이선스 수정 완료: ID {}, License ID {}", id, savedLicense.getLicenseId());
        
        return savedLicense;
    }
}
