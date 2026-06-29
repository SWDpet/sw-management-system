package com.swmanager.system.license.sync;

import com.swmanager.system.license.domain.LicenseRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LicenseFieldMapper 단위 테스트 (P10, T-2/T-3).
 * Derby raw row(Map) → LicenseRegistry 매핑, 코드→라벨 실증매핑, preflight 실패, 미매핑 코드 차단,
 * License String 다중라인 보존을 검증. DB/Derby 불필요(순수 단위).
 */
class LicenseFieldMapperTest {

    private final LicenseFieldMapper mapper = new LicenseFieldMapper();

    private Map<String, Object> baseRow() {
        Map<String, Object> r = new HashMap<>();
        r.put("licenseID", 1580432781021L);
        r.put("productName", "LSA Basic");
        r.put("productVersion", "2.5");
        r.put("licenseString", "# UGIS License (id: 1580432781021)\nLINE2\nLINE3");
        r.put("selectedLicenseType", 1);
        r.put("generationSource", 1);
        r.put("selectedDateTimeCheck", 1);
        r.put("selectedActivationReturn", 0);
        r.put("selectedHardwareIDSelection", 0);
        r.put("hardwareID", "abc&&def");
        r.put("userRegisteredTo", "Hong Gildong");
        r.put("validityPeriod", 365);
        r.put("quantity", 1);
        r.put("activationRequired", 1);     // SMALLINT → true
        r.put("queryLocalADServer", 0);     // SMALLINT → false
        r.put("generationDateTime", "2020-01-31 10:04");
        return r;
    }

    @Test
    @DisplayName("정상 행 — 코드→라벨/불리언/날짜/다중라인 매핑")
    void mapsValidRow() {
        LicenseFieldMapper.MapResult res = mapper.map(baseRow());

        assertThat(res.isOk()).isTrue();
        LicenseRegistry r = res.registry();
        assertThat(r.getLicenseId()).isEqualTo("1580432781021");
        assertThat(r.getProductId()).isEqualTo("LSA Basic 2.5");        // productName + " " + version
        assertThat(r.getLicenseType()).isEqualTo("License Text");          // code 1
        assertThat(r.getGenerationSource()).isEqualTo("Manual");           // code 1
        assertThat(r.getDateTimeCheck()).isEqualTo("On Each Run");         // code 1
        assertThat(r.getActivationReturn()).isEqualTo("");                 // code 0
        assertThat(r.getHardwareId()).isEqualTo("abc&&def");               // && 보존
        assertThat(r.getActivationRequired()).isTrue();
        assertThat(r.getQueryLocalAdServer()).isFalse();
        assertThat(r.getValidityPeriod()).isEqualTo(365);
        assertThat(r.getGenerationDateTime().toString()).isEqualTo("2020-01-31T10:04");
        // 다중라인 License String 원형 보존
        assertThat(r.getLicenseString()).contains("\n").contains("LINE2").contains("LINE3");
    }

    @Test
    @DisplayName("Floating 타입 — selectedLicenseType=2")
    void mapsFloatingType() {
        Map<String, Object> row = baseRow();
        row.put("selectedLicenseType", 2);
        assertThat(mapper.map(row).registry().getLicenseType()).isEqualTo("Floating License Text");
    }

    @Test
    @DisplayName("License String 결손 → 실패")
    void failsOnMissingLicenseString() {
        Map<String, Object> row = baseRow();
        row.remove("licenseString");
        LicenseFieldMapper.MapResult res = mapper.map(row);
        assertThat(res.isOk()).isFalse();
        assertThat(res.error()).contains("License String");
    }

    @Test
    @DisplayName("Product 이름 결손 → 실패")
    void failsOnMissingProductId() {
        Map<String, Object> row = baseRow();
        row.remove("productName");
        assertThat(mapper.map(row).isOk()).isFalse();
    }

    @Test
    @DisplayName("버전 없으면 Product ID = productName 단독")
    void productIdWithoutVersion() {
        Map<String, Object> row = baseRow();
        row.remove("productVersion");
        assertThat(mapper.map(row).registry().getProductId()).isEqualTo("LSA Basic");
        row.put("productVersion", "");   // 빈 문자열도 동일
        assertThat(mapper.map(row).registry().getProductId()).isEqualTo("LSA Basic");
    }

    @Test
    @DisplayName("미매핑 코드값 → 실패(차단)")
    void failsOnUnmappedCode() {
        Map<String, Object> row = baseRow();
        row.put("selectedLicenseType", 99);     // 매핑표에 없음
        LicenseFieldMapper.MapResult res = mapper.map(row);
        assertThat(res.isOk()).isFalse();
        assertThat(res.error()).contains("미매핑 코드").contains("License Type");
    }

    @Test
    @DisplayName("코드 null → 라벨 null 허용(실패 아님)")
    void allowsNullCode() {
        Map<String, Object> row = baseRow();
        row.remove("selectedDateTimeCheck");
        LicenseFieldMapper.MapResult res = mapper.map(row);
        assertThat(res.isOk()).isTrue();
        assertThat(res.registry().getDateTimeCheck()).isNull();
    }
}
