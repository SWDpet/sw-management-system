package com.swmanager.system.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * workplan-target-infra-cascade FR-10 — 대상 표시 라벨 fallback 단위테스트.
 */
class WorkPlanDTOTargetLabelTest {

    @Test
    void contracted_usesInfraFields() {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setCityNm("경기도"); d.setDistNm("김포시"); d.setSysNm("GeoNURIS");
        assertEquals("김포시 - GeoNURIS", d.getTargetLabel());
    }

    @Test
    void nonContracted_usesRegionFields() {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setRegionCityNm("경상남도"); d.setRegionDistNm("고성군"); d.setTargetSysNm("UPIS");
        assertEquals("고성군 - UPIS", d.getTargetLabel());
    }

    @Test
    void regionFields_takePrecedenceOverInfra() {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setCityNm("경기도"); d.setDistNm("김포시"); d.setSysNm("GeoNURIS");
        d.setRegionDistNm("고성군"); d.setTargetSysNm("UPIS");
        assertEquals("고성군 - UPIS", d.getTargetLabel());
    }

    @Test
    void onlyCity_noDist_usesCity() {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setRegionCityNm("울산광역시"); d.setTargetSysNm("UPIS");
        assertEquals("울산광역시 - UPIS", d.getTargetLabel());
    }

    @Test
    void distOnly_noSys() {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setDistNm("김포시");
        assertEquals("김포시", d.getTargetLabel());
    }

    @Test
    void empty_returnsEmptyString() {
        assertEquals("", new WorkPlanDTO().getTargetLabel());
    }
}
