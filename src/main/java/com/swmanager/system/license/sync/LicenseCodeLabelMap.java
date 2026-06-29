package com.swmanager.system.license.sync;

import java.util.Map;

/**
 * Derby 정수 코드 → 대장(CSV) 라벨 실증 매핑 (P4, 2026-06-29 확정).
 *
 * 운영 license_registry(라벨) ↔ License4J Derby APP.LICENSES(코드)를 같은 licenseID 로 조인해
 * "회사가 실제 써온 지배적 라벨"을 도출함. 추측 아님.
 *
 * 매핑에 없는 코드값 출현 시 = 해당 건 실패 처리 + 경보(데이터 오염 방지, FR-3 정책).
 * 단, 이 필드들은 dedup 키(License ID+Product ID)·변경판정(Hardware ID/License String)에 미포함이라
 * 기존 라이선스는 본 라벨 차이로 갱신되지 않음(멱등성). 신규/갱신 건의 저장값에만 영향.
 */
public final class LicenseCodeLabelMap {

    private LicenseCodeLabelMap() {}

    /** selectedLicenseType */
    public static final Map<Integer, String> LICENSE_TYPE = Map.of(
            1, "License Text",
            2, "Floating License Text");

    /** generationSource */
    public static final Map<Integer, String> GENERATION_SOURCE = Map.of(
            1, "Manual");

    /** selectedDateTimeCheck */
    public static final Map<Integer, String> DATE_TIME_CHECK = Map.of(
            1, "On Each Run",
            0, "None");

    /** selectedActivationReturn (관측 전량 0 → 빈값) */
    public static final Map<Integer, String> ACTIVATION_RETURN = Map.of(
            0, "");

    /** selectedHardwareIDSelection (관측 전량 0 → 빈값) */
    public static final Map<Integer, String> HARDWARE_ID_SELECTION = Map.of(
            0, "");

    /**
     * 코드 → 라벨. 매핑에 없으면 null 반환(호출측이 실패 처리).
     * @param code Derby 정수 코드(null 가능)
     */
    public static String label(Map<Integer, String> map, Integer code) {
        if (code == null) return null;
        return map.get(code);
    }
}
