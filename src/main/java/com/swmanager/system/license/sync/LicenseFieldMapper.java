package com.swmanager.system.license.sync;

import com.swmanager.system.license.domain.LicenseRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Derby raw row(컬럼명→값 Map) → LicenseRegistry 매핑 + preflight 검증 (P5).
 *
 * - 63 표준필드 매핑(코드→라벨은 LicenseCodeLabelMap, SMALLINT→Boolean, VARCHAR 날짜 파싱).
 * - preflight: 필수값(License ID/Product ID/License String) 결손, 미매핑 코드값 → 실패(사유 반환).
 * - License String 다중라인 그대로 보존(CSV 텍스트 미경유 — D2).
 */
@Slf4j
@Component
public class LicenseFieldMapper {

    private static final DateTimeFormatter GEN_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 매핑 결과: registry(성공) 또는 error(실패) */
    public record MapResult(LicenseRegistry registry, String error) {
        public boolean isOk() { return registry != null; }
        static MapResult ok(LicenseRegistry r) { return new MapResult(r, null); }
        static MapResult fail(String e) { return new MapResult(null, e); }
    }

    public MapResult map(Map<String, Object> row) {
        try {
            LicenseRegistry r = new LicenseRegistry();

            // ── 필수 키 (preflight) ──
            // Product ID = productName (+ " " + productVersion, 버전 non-blank 시). 기존 대장 형식과 일치.
            String licenseId   = str(row, "licenseID");
            String productName = str(row, "productName");
            String productVer  = str(row, "productVersion");
            String licString   = str(row, "licenseString");   // 다중라인 보존
            if (isBlank(licenseId))    return MapResult.fail("License ID 결손");
            if (isBlank(productName))  return MapResult.fail("Product 이름 결손 (licenseID=" + licenseId + ")");
            if (isBlank(licString))    return MapResult.fail("License String 결손 (licenseID=" + licenseId + ")");
            String productId = isBlank(productVer) ? productName : productName + " " + productVer;
            r.setLicenseId(licenseId);
            r.setProductId(productId);
            r.setLicenseString(licString);

            // ── 코드 → 라벨 (코드가 있는데 매핑표에 없으면 차단) ──
            Integer ltCode = intg(row, "selectedLicenseType");
            Integer gsCode = intg(row, "generationSource");
            Integer dcCode = intg(row, "selectedDateTimeCheck");
            Integer arCode = intg(row, "selectedActivationReturn");
            Integer hsCode = intg(row, "selectedHardwareIDSelection");
            if (unmapped(LicenseCodeLabelMap.LICENSE_TYPE, ltCode))          return MapResult.fail(failMsg("License Type", ltCode, licenseId));
            if (unmapped(LicenseCodeLabelMap.GENERATION_SOURCE, gsCode))     return MapResult.fail(failMsg("Generation Source", gsCode, licenseId));
            if (unmapped(LicenseCodeLabelMap.DATE_TIME_CHECK, dcCode))       return MapResult.fail(failMsg("Date Time Check", dcCode, licenseId));
            if (unmapped(LicenseCodeLabelMap.ACTIVATION_RETURN, arCode))     return MapResult.fail(failMsg("Activation Return", arCode, licenseId));
            if (unmapped(LicenseCodeLabelMap.HARDWARE_ID_SELECTION, hsCode)) return MapResult.fail(failMsg("Hardware ID Selection", hsCode, licenseId));
            r.setLicenseType(LicenseCodeLabelMap.label(LicenseCodeLabelMap.LICENSE_TYPE, ltCode));
            r.setGenerationSource(LicenseCodeLabelMap.label(LicenseCodeLabelMap.GENERATION_SOURCE, gsCode));
            r.setDateTimeCheck(LicenseCodeLabelMap.label(LicenseCodeLabelMap.DATE_TIME_CHECK, dcCode));
            r.setActivationReturn(LicenseCodeLabelMap.label(LicenseCodeLabelMap.ACTIVATION_RETURN, arCode));
            r.setHardwareIdSelection(LicenseCodeLabelMap.label(LicenseCodeLabelMap.HARDWARE_ID_SELECTION, hsCode));

            // ── 직통 문자열 ──
            r.setValidProductEdition(str(row, "validProductEdition"));
            r.setValidProductVersion(str(row, "validProductVersion"));
            r.setHardwareId(str(row, "hardwareID"));                 // 원본 && 다중값 보존
            r.setRegisteredTo(str(row, "userRegisteredTo"));
            r.setFullName(str(row, "userFullName"));
            r.setEmail(str(row, "userEMail"));
            r.setCompany(str(row, "userCompany"));
            r.setTelephone(str(row, "userTelephone"));
            r.setFax(str(row, "userFax"));
            r.setStreet(str(row, "userStreet"));
            r.setCity(str(row, "userCity"));
            r.setZipCode(str(row, "userZipCode"));
            r.setCountry(str(row, "userCountry"));
            r.setIpBlocks(str(row, "activationAllowedIPs"));
            r.setInternalHiddenString(str(row, "internalHiddenString"));
            r.setSupersededLicenseIds(str(row, "supersededFloatingLicenses"));
            r.setNtpServer(str(row, "ntpServer"));
            r.setWebServer(str(row, "webServer"));
            r.setUnsignedCustomFeatures(str(row, "unsignedCustomFeatures"));
            r.setSignedCustomFeatures(str(row, "signedCustomFeatures"));

            // ── 날짜 ──
            r.setGenerationDateTime(parseGenDt(str(row, "generationDateTime")));

            // ── 정수 ──
            r.setValidityPeriod(intg(row, "validityPeriod"));
            r.setMaintenancePeriod(intg(row, "maintenancePeriod"));
            r.setQuantity(intg(row, "quantity"));
            r.setAllowedUseCount(intg(row, "allowedUseCount"));
            r.setActivationPeriod(intg(row, "activationPeriod"));
            r.setAllowedActivationCount(intg(row, "allowedActivationCount"));
            r.setAllowedDeactivationCount(intg(row, "allowedDeactivationCount"));
            r.setGenerationTimeOffsetFromActivationTime(intg(row, "generationTimetoActivationOffset"));
            r.setFloatingLicenseCheckPeriod(intg(row, "floatingLicenseCheckPeriod"));
            r.setFloatingLicenseServerConnectionCheckPeriod(intg(row, "floatingLicenseServerConnectionCheckPeriod"));
            r.setMaxInactivePeriod(intg(row, "maxInactivePeriod"));
            r.setMaximumReChecksBeforeDrop(intg(row, "serverMaximumReCheck"));
            r.setCurrentUseCount(intg(row, "useCount"));
            r.setAllowedUseTimeLimit(intg(row, "allowedUseTimeLimit"));
            r.setNtpServerTimeout(intg(row, "ntpServerTimeout"));
            r.setWebServerTimeout(intg(row, "webServerTimeout"));

            // ── 불리언 (SMALLINT 0/1) ──
            r.setActivationRequired(bool(row, "activationRequired"));
            r.setAutoActivationsDisabled(bool(row, "disableAutoActivations"));
            r.setManualActivationsDisabled(bool(row, "disableManualActivations"));
            r.setOnlineKeyLeaseDisabled(bool(row, "disableLease"));
            r.setDeactivationsDisabled(bool(row, "disableAutoDeactivations"));
            r.setDontKeepDeactivationRecords(bool(row, "dkdr"));
            r.setIpBlocksAllow(bool(row, "allowIPs"));
            r.setRejectModificationKeyUsage(bool(row, "rejectModificationKeyUsage"));
            r.setSetGenerationTimeToActivationTime(bool(row, "generationTimetoActivation"));
            r.setUseCustomerNameInValidation(bool(row, "useCustomerNameValidation"));
            r.setUseCompanyNameInValidation(bool(row, "useCompanyNameValidation"));
            r.setFloatingAllowMultipleInstances(bool(row, "floatingAllowMultipleInstances"));
            r.setDontKeepReleasedLicenseUsage(bool(row, "dkookur"));
            r.setNtpServerCheck(bool(row, "ntpServerCheck"));
            r.setWebServerCheck(bool(row, "webServerCheck"));
            r.setQueryLocalAdServer(bool(row, "queryLocalADServer"));

            return MapResult.ok(r);
        } catch (Exception e) {
            return MapResult.fail("매핑 예외: " + e.getMessage());
        }
    }

    // ===== 헬퍼 =====

    /** 코드가 존재(non-null)하는데 매핑표에 없으면 true (미매핑 = 차단 대상) */
    private static boolean unmapped(Map<Integer, String> map, Integer code) {
        return code != null && !map.containsKey(code);
    }

    private static String failMsg(String field, Object code, String licenseId) {
        return "미매핑 코드: " + field + "=" + code + " (licenseID=" + licenseId + ")";
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? null : v.toString();
    }

    private static Integer intg(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString().trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /** SMALLINT 0/1 → Boolean (null 유지) */
    private static Boolean bool(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue() != 0;
        String s = v.toString().trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }

    private static LocalDateTime parseGenDt(String s) {
        if (isBlank(s)) return null;
        try { return LocalDateTime.parse(s.trim(), GEN_DT); }
        catch (Exception e) { return null; }       // 포맷 외 → null(기존 파서와 동일 관용)
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
